import { useEffect, useState } from 'react';
import { Drawer, Descriptions, Button, Form, Input, message, Divider, Tag } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { useAuthStore } from '@/stores/authStore';
import request from '@/services/request';

interface Props {
  open: boolean;
  onClose: () => void;
}

export default function ProfileDrawer({ open, onClose }: Props) {
  const userInfo = useAuthStore(s => s.userInfo);
  const [loading, setLoading] = useState(false);
  const [pwdLoading, setPwdLoading] = useState(false);
  const [form] = Form.useForm();
  const [pwdForm] = Form.useForm();
  const [info, setInfo] = useState<any>(null);

  useEffect(() => {
    if (open) {
      request.get('/users/me').then(setInfo);
      form.setFieldsValue({ realName: userInfo?.realName, phone: userInfo?.phone, email: userInfo?.email });
    }
  }, [open]);

  const saveProfile = async () => {
    const values = await form.validateFields();
    setLoading(true);
    try { await request.put('/users/me', values); message.success('保存成功'); } finally { setLoading(false); }
  };

  const changePassword = async () => {
    const values = await pwdForm.validateFields();
    if (values.newPassword !== values.confirmPassword) { message.error('两次密码不一致'); return; }
    setPwdLoading(true);
    try {
      await request.put('/users/me/password', { oldPassword: values.oldPassword, newPassword: values.newPassword });
      message.success('密码已修改，请重新登录');
      pwdForm.resetFields();
      setTimeout(() => useAuthStore.getState().logout(), 1500);
    } catch { } finally { setPwdLoading(false); }
  };

  const typeMap: any = { 1: '学员', 2: '教师', 3: '管理员' };

  return (
    <Drawer title="个人中心" open={open} onClose={onClose} width={420}>
      <Descriptions column={1} bordered size="small" style={{ marginBottom: 24 }}>
        <Descriptions.Item label="用户名">{info?.username || userInfo?.username}</Descriptions.Item>
        <Descriptions.Item label="姓名">{info?.realName || userInfo?.realName}</Descriptions.Item>
        <Descriptions.Item label="角色">
          <Tag color={userInfo?.userType === 3 ? 'purple' : userInfo?.userType === 2 ? 'blue' : 'green'}>
            {typeMap[userInfo?.userType || 1] || '未知'}
          </Tag>
        </Descriptions.Item>
        <Descriptions.Item label="手机号">{info?.phone || userInfo?.phone || '-'}</Descriptions.Item>
        <Descriptions.Item label="邮箱">{info?.email || userInfo?.email || '-'}</Descriptions.Item>
      </Descriptions>

      <Divider>编辑资料</Divider>
      <Form form={form} layout="vertical">
        <Form.Item name="realName" label="姓名" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="phone" label="手机号"><Input /></Form.Item>
        <Form.Item name="email" label="邮箱"><Input /></Form.Item>
        <Form.Item><Button type="primary" loading={loading} onClick={saveProfile} block>保存</Button></Form.Item>
      </Form>

      <Divider>修改密码</Divider>
      <Form form={pwdForm} layout="vertical">
        <Form.Item name="oldPassword" label="原密码" rules={[{ required: true }]}>
          <Input.Password prefix={<LockOutlined />} /></Form.Item>
        <Form.Item name="newPassword" label="新密码" rules={[{ required: true, min: 6 }]}>
          <Input.Password prefix={<LockOutlined />} /></Form.Item>
        <Form.Item name="confirmPassword" label="确认密码" rules={[{ required: true }]}>
          <Input.Password prefix={<LockOutlined />} /></Form.Item>
        <Form.Item><Button danger loading={pwdLoading} onClick={changePassword} block>修改密码</Button></Form.Item>
      </Form>
    </Drawer>
  );
}
