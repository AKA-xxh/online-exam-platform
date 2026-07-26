import { useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuthStore } from '@/stores/authStore';

/**
 * 路由守卫 Hook
 *
 * 在布局组件中调用，检查：
 * 1. 是否已登录 → 未登录跳转登录页
 * 2. 是否有权限访问当前路径 → 无权限跳转 403
 */
export function useRouteGuard(requiredRole?: 'admin' | 'student') {
  const navigate = useNavigate();
  const location = useLocation();
  const { isLoggedIn, userInfo } = useAuthStore();

  useEffect(() => {
    // 登录页不需要守卫
    if (location.pathname === '/login') return;

    // 未登录 → 跳转登录页
    if (!isLoggedIn) {
      navigate('/login', { replace: true, state: { from: location.pathname } });
      return;
    }

    // 角色检查
    if (requiredRole && userInfo) {
      const userType = userInfo.userType;
      if (requiredRole === 'admin' && userType === 1) {
        // 学员访问管理端 → 跳转学员端
        navigate('/student', { replace: true });
      }
      if (requiredRole === 'student' && (userType === 2 || userType === 3)) {
        // 教师/管理员访问学员端 → 跳转管理端
        navigate('/admin', { replace: true });
      }
    }
  }, [isLoggedIn, userInfo, navigate, location.pathname, requiredRole]);

  return { isLoggedIn, userInfo };
}
