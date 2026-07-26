import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import request from '@/services/request';

/**
 * 认证状态管理
 *
 * 存储用户登录后的 Token 和基本信息。
 * 使用 Zustand 的 persist 中间件将 Token 持久化到 localStorage，
 * 这样刷新页面不会丢失登录状态。
 */

interface UserInfo {
  userId: number;
  username: string;
  realName: string;
  nickname: string;
  avatar: string;
  phone: string;
  email: string;
  userType: number;  // 1=学员 2=教师 3=管理员
  roles: string[];
  permissions: string[];
}

interface AuthState {
  accessToken: string | null;
  userInfo: UserInfo | null;
  isLoggedIn: boolean;

  // Actions
  setToken: (accessToken: string) => void;
  setUserInfo: (info: UserInfo) => void;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  doRefreshToken: () => Promise<string>;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      userInfo: null,
      isLoggedIn: false,

      setToken: (accessToken: string) => {
        set({ accessToken, isLoggedIn: true });
      },

      setUserInfo: (info: UserInfo) => {
        set({ userInfo: info });
      },

      login: async (username: string, password: string) => {
        const res = await request.post('/auth/login', { username, password });
        const { accessToken, userInfo: info } = res;
        set({
          accessToken: accessToken.replace('Bearer ', ''),
          userInfo: info,
          isLoggedIn: true,
        });
      },

      logout: () => {
        request.post('/auth/logout').catch(() => {});
        set({
          accessToken: null,
          userInfo: null,
          isLoggedIn: false,
        });
        window.location.href = '/login';
      },

      doRefreshToken: async (): Promise<string> => {
        const res = await request.post('/auth/refresh-token');
        const newToken = res.accessToken.replace('Bearer ', '');
        set({ accessToken: newToken });
        return newToken;
      },
    }),
    {
      name: 'exam-auth-storage',
      partialize: (state) => ({
        accessToken: state.accessToken,
        userInfo: state.userInfo,
        isLoggedIn: state.isLoggedIn,
      }),
    },
  ),
);
