import { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Form, Input, Button, Card, message, Typography } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useAuthStore } from '@/stores/authStore';

const { Title, Text } = Typography;

/**
 * 登录页面
 */
export default function Login() {
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const login = useAuthStore((s) => s.login);
  const isLoggedIn = useAuthStore((s) => s.isLoggedIn);
  const userInfo = useAuthStore((s) => s.userInfo);

  // 已登录 → 根据角色跳转
  if (isLoggedIn && userInfo) {
    const target = (userInfo.userType === 1) ? '/student' : '/admin';
    navigate(target, { replace: true });
    return null;
  }

  const onFinish = async (values: { username: string; password: string }) => {
    setLoading(true);
    try {
      await login(values.username, values.password);
      message.success('登录成功');

      // 登录后根据角色跳转
      const updatedInfo = useAuthStore.getState().userInfo;
      const from = (location.state as any)?.from;
      if (from) {
        navigate(from, { replace: true });
      } else if (updatedInfo?.userType === 1) {
        navigate('/student', { replace: true });
      } else {
        navigate('/admin', { replace: true });
      }
    } catch {
      // 错误已在 request 拦截器中处理
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '100vh',
      background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
      padding: 24,
    }}>
      <Card
        style={{ width: 420, boxShadow: '0 8px 32px rgba(0,0,0,0.1)' }}
        styles={{ body: { padding: '40px 32px' } }}
      >
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <Title level={3} style={{ marginBottom: 8 }}>在线考试培训系统</Title>
          <Text type="secondary">企业级在线学习与考试平台</Text>
        </div>

        <Form
          name="login"
          size="large"
          onFinish={onFinish}
          autoComplete="off"
        >
          <Form.Item
            name="username"
            rules={[{ required: true, message: '请输入用户名' }]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="用户名"
              autoFocus
            />
          </Form.Item>

          <Form.Item
            name="password"
            rules={[{ required: true, message: '请输入密码' }]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
            />
          </Form.Item>

          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              loading={loading}
              block
              style={{ height: 44, fontSize: 16 }}
            >
              登录
            </Button>
          </Form.Item>
        </Form>

        {/*<div style={{ textAlign: 'center' }}>*/}
        {/*  <Text type="secondary" style={{ fontSize: 12 }}>*/}
        {/*    演示账号：admin / admin123（管理员）| student / student123（学员）*/}
        {/*  </Text>*/}
        {/*</div>*/}
      </Card>
    </div>
  );
}
