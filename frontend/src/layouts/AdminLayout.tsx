import { useState } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import { Layout, Menu, Button, Avatar, Dropdown, theme } from 'antd';
import ProfileDrawer from '@/components/common/ProfileDrawer';
import {
  DashboardOutlined,
  UserOutlined,
  BookOutlined,
  DatabaseOutlined,
  FileTextOutlined,
  ScheduleOutlined,
  CheckOutlined,
  BarChartOutlined,
  SettingOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  LogoutOutlined,
} from '@ant-design/icons';
import { useAuthStore } from '@/stores/authStore';
import { useAppStore } from '@/stores/appStore';
import { useRouteGuard } from '@/router/guard';

const { Header, Sider, Content } = Layout;

/**
 * 管理端布局
 *
 * 结构：左侧可折叠菜单 + 顶部栏 + 内容区
 */
export default function AdminLayout() {
  const { isLoggedIn, userInfo } = useRouteGuard('admin');
  const { sidebarCollapsed, toggleSidebar } = useAppStore();
  const { logout } = useAuthStore();
  const navigate = useNavigate();
  const { token: themeToken } = theme.useToken();
  const [selectedKeys, setSelectedKeys] = useState<string[]>(['dashboard']);
  const [profileOpen, setProfileOpen] = useState(false);

  if (!isLoggedIn || !userInfo) return null;

  const menuItems = [
    { key: 'dashboard', icon: <DashboardOutlined />, label: '控制台' },
    { key: 'users',      icon: <UserOutlined />,      label: '用户管理' },
    { key: 'courses',    icon: <BookOutlined />,       label: '课程管理' },
    { key: 'questions',  icon: <DatabaseOutlined />,    label: '题库管理' },
    { key: 'papers',     icon: <FileTextOutlined />,    label: '试卷管理' },
    { key: 'exams',      icon: <ScheduleOutlined />,    label: '考试管理' },
    { key: 'grading',    icon: <CheckOutlined />,       label: '阅卷管理' },
    { key: 'scores',     icon: <BarChartOutlined />,    label: '成绩管理' },
    { key: 'system',     icon: <SettingOutlined />,     label: '系统设置' },
  ];

  const userMenuItems = [
    { key: 'profile', label: '个人中心' },
    { type: 'divider' as const },
    { key: 'logout', label: '退出登录', icon: <LogoutOutlined />, danger: true },
  ];

  const handleMenuClick = (key: string) => {
    setSelectedKeys([key]);
    navigate(`/admin/${key}`);
  };

  const handleUserMenuClick = (key: string) => {
    if (key === 'profile') { setProfileOpen(true); return; }
    if (key === 'logout') { logout(); }
  };

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        trigger={null}
        collapsible
        collapsed={sidebarCollapsed}
        style={{ background: themeToken.colorBgContainer }}
        width={220}
      >
        <div style={{
          height: 64,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          borderBottom: `1px solid ${themeToken.colorBorderSecondary}`,
        }}>
          <h2 style={{
            margin: 0,
            fontSize: sidebarCollapsed ? 16 : 18,
            color: themeToken.colorPrimary,
            whiteSpace: 'nowrap',
          }}>
            {sidebarCollapsed ? '考试' : '在线考试培训系统'}
          </h2>
        </div>
        <Menu
          mode="inline"
          selectedKeys={selectedKeys}
          items={menuItems}
          onClick={({ key }) => handleMenuClick(key)}
          style={{ border: 'none' }}
        />
      </Sider>

      <Layout>
        <Header style={{
          padding: '0 24px',
          background: themeToken.colorBgContainer,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          borderBottom: `1px solid ${themeToken.colorBorderSecondary}`,
        }}>
          <Button
            type="text"
            icon={sidebarCollapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={toggleSidebar}
            style={{ fontSize: 16, width: 64, height: 64 }}
          />

          <Dropdown
            menu={{
              items: userMenuItems,
              onClick: ({ key }) => handleUserMenuClick(key),
            }}
          >
            <div style={{ cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 8 }}>
              <Avatar icon={<UserOutlined />} />
              <span>{userInfo.realName}</span>
            </div>
          </Dropdown>
        </Header>

        <Content style={{ margin: 24, minHeight: 280 }}>
          <Outlet />
        </Content>
      </Layout>
      <ProfileDrawer open={profileOpen} onClose={() => setProfileOpen(false)} />
    </Layout>
  );
}
