import { Suspense } from 'react';
import { useRoutes } from 'react-router-dom';
import { Spin } from 'antd';
import { routes } from './router/routes';

/**
 * 应用根组件
 * 使用 React Router 的 useRoutes 进行路由渲染
 */
function App() {
  const element = useRoutes(routes);

  return (
    <Suspense
      fallback={
        <div style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          height: '100vh',
        }}>
          <Spin size="large" tip="加载中..." />
        </div>
      }
    >
      {element}
    </Suspense>
  );
}

export default App;
