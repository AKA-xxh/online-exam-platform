import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import { message } from 'antd';
import { useAuthStore } from '@/stores/authStore';

/**
 * Axios 封装
 *
 * 核心功能：
 * 1. 请求拦截：自动携带 Token
 * 2. 响应拦截：统一解析返回数据、处理 Token 过期、处理错误
 * 3. 请求队列：Token 刷新期间挂起的请求暂存，刷新成功后重放
 */

// ==================== 创建 Axios 实例 ====================

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ==================== Token 刷新相关 ====================

let isRefreshing = false;
let pendingRequests: Array<{ resolve: (token: string) => void; reject: (err: Error) => void }> = [];
let authErrorShown = false;
let lastErrorMsg = ''; // 防重复通用错误

function showErrorOnce(msg: string) {
  if (lastErrorMsg === msg) return;
  lastErrorMsg = msg;
  message.error(msg);
  setTimeout(() => { lastErrorMsg = ''; }, 2000);
}

function addPendingRequest(resolve: (token: string) => void, reject: (err: Error) => void) {
  pendingRequests.push({ resolve, reject });
}

function resolvePendingRequests(token: string) {
  pendingRequests.forEach(p => p.resolve(token));
  pendingRequests = [];
}

function rejectPendingRequests() {
  pendingRequests.forEach(p => p.reject(new Error('Token 刷新失败')));
  pendingRequests = [];
}

function showAuthErrorOnce(msg: string) {
  if (authErrorShown) return;
  authErrorShown = true;
  message.error(msg);
  setTimeout(() => { authErrorShown = false; }, 3000);
}

// ==================== 请求拦截器 ====================

request.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 从 Zustand store 获取 Token（注意：不要在组件外使用 hook，直接读 store state）
    const token = useAuthStore.getState().accessToken;
    if (token && config.headers) {
      config.headers.Authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

// ==================== 响应拦截器 ====================

request.interceptors.response.use(
  (response) => {
    const res = response.data;

    // 统一返回格式 { code, message, data, timestamp }
    if (res.code !== undefined && res.code !== 200) {
      // 业务错误
      showErrorOnce(res.message || '请求失败');
      return Promise.reject(new Error(res.message || '请求失败'));
    }

    // 直接返回 data 字段，让调用方少写一层 .data
    return res.data ?? res;
  },
  async (error: AxiosError) => {
    // 网络错误
    if (!error.response) {
      showErrorOnce('网络连接失败，请检查网络');
      return Promise.reject(error);
    }

    const { status } = error.response;

    switch (status) {
      case 401:
        // Token 过期，尝试刷新
        if (!isRefreshing) {
          isRefreshing = true;
          try {
            const authStore = useAuthStore.getState();
            const newToken = await authStore.doRefreshToken();
            isRefreshing = false;
            resolvePendingRequests(newToken);
            if (error.config) {
              error.config.headers.Authorization = `Bearer ${newToken}`;
              return request(error.config);
            }
          } catch {
            isRefreshing = false;
            rejectPendingRequests();
            showAuthErrorOnce('登录已过期，请重新登录');
            setTimeout(() => {
              useAuthStore.getState().logout();
              window.location.href = '/login';
            }, 800);
          }
        } else {
          // 正在刷新中，暂存请求
          return new Promise((resolve, reject) => {
            addPendingRequest(
              (token: string) => {
                if (error.config) {
                  error.config.headers.Authorization = `Bearer ${token}`;
                  resolve(request(error.config));
                }
                resolve(null as any);
              },
              reject,
            );
          });
        }
        break;

      case 403:
        showErrorOnce('权限不足');
        break;

      case 404:
        showErrorOnce('请求的资源不存在');
        break;

      case 500:
        showErrorOnce('服务器内部错误');
        break;

      default:
        showErrorOnce(`请求失败 (${status})`);
        break;
    }

    return Promise.reject(error);
  },
);

export default request;
