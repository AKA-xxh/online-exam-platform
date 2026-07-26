import { useState } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import { Layout, Menu, Avatar, Dropdown, theme } from 'antd';
import {
  DashboardOutlined, BookOutlined, ScheduleOutlined, BugOutlined,
  UserOutlined, LogoutOutlined,
} from '@ant-design/icons';
import { useAuthStore } from '@/stores/authStore';
import { useRouteGuard } from '@/router/guard';
import ProfileDrawer from '@/components/common/ProfileDrawer';

const { Header, Content } = Layout;

export default function StudentLayout() {
  const { isLoggedIn, userInfo } = useRouteGuard('student');
  const { logout } = useAuthStore();
  const navigate = useNavigate();
  const { token: themeToken } = theme.useToken();
  const [selectedKeys, setSelectedKeys] = useState<string[]>(['dashboard']);
  const [profileOpen, setProfileOpen] = useState(false);

  if (!isLoggedIn || !userInfo) return null;

  const menuItems = [
    { key: 'dashboard', icon: <DashboardOutlined />, label: '学习首页' },
    { key: 'courses',   icon: <BookOutlined />,      label: '课程中心' },
    { key: 'exams',     icon: <ScheduleOutlined />,   label: '我的考试' },
    { key: 'wrong',     icon: <BugOutlined />,        label: '错题本' },
  ];

  const userMenuItems = [
    { key: 'profile', label: '个人中心' },
    { type: 'divider' as const },
    { key: 'logout', label: '退出登录', icon: <LogoutOutlined />, danger: true },
  ];

  const handleMenuClick = (key: string) => {
    setSelectedKeys([key]);
    navigate(`/student/${key}`);
  };

  const handleUserMenu = (key: string) => {
    if (key === 'profile') { setProfileOpen(true); return; }
    if (key === 'logout') logout();
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{
        padding: '0 24px', background: themeToken.colorBgContainer,
        display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        borderBottom: `1px solid ${themeToken.colorBorderSecondary}`,
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 24 }}>
          <h2 style={{ margin: 0, color: themeToken.colorPrimary, whiteSpace: 'nowrap' }}>
            在线考试培训系统
          </h2>
          <Menu mode="horizontal" selectedKeys={selectedKeys} items={menuItems}
            onClick={({ key }) => handleMenuClick(key)}
            style={{ border: 'none', flex: 1, minWidth: 400 }} />
        </div>
        <Dropdown menu={{ items: userMenuItems, onClick: ({ key }) => handleUserMenu(key) }}>
          <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8 }}>
            <Avatar icon={<UserOutlined />} />
            <span>{userInfo.realName}</span>
          </div>
        </Dropdown>
      </Header>
      <Content style={{ margin: 24, minHeight: 280 }}>
        <Outlet />
      </Content>
      <ProfileDrawer open={profileOpen} onClose={() => setProfileOpen(false)} />
    </Layout>
  );
}
