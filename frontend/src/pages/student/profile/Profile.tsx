import { Card, Form, Input, Button, message, Divider, Typography } from 'antd';
import { useAuthStore } from '@/stores/authStore';
import request from '@/services/request';

const { Title } = Typography;

export default function StudentProfile() {
  const userInfo = useAuthStore(s => s.userInfo);
  const [form] = Form.useForm();
  const [pwdForm] = Form.useForm();

  const onSaveProfile = async (values: any) => {
    await request.put('/users/me', values);
    message.success('个人信息已更新');
  };

  const onChangePassword = async (values: any) => {
    await request.put('/auth/password', null, { params: values });
    message.success('密码已修改，请重新登录');
  };

  return (
    <div style={{ maxWidth: 600 }}>
      <Title level={4}>个人中心</Title>
      <Card title="基本信息">
        <Form form={form} layout="vertical" initialValues={{ realName: userInfo?.realName, phone: userInfo?.phone, email: userInfo?.email }}
          onFinish={onSaveProfile}>
          <Form.Item name="realName" label="姓名"><Input /></Form.Item>
          <Form.Item name="phone" label="手机号"><Input /></Form.Item>
          <Form.Item name="email" label="邮箱"><Input /></Form.Item>
          <Form.Item><Button type="primary" htmlType="submit">保存</Button></Form.Item>
        </Form>
      </Card>
      <Card title="修改密码" style={{ marginTop: 16 }}>
        <Form form={pwdForm} layout="vertical" onFinish={onChangePassword}>
          <Form.Item name="oldPassword" label="原密码" rules={[{ required: true }]}><Input.Password /></Form.Item>
          <Form.Item name="newPassword" label="新密码" rules={[{ required: true, min: 6 }]}><Input.Password /></Form.Item>
          <Form.Item><Button type="primary" htmlType="submit">修改密码</Button></Form.Item>
        </Form>
      </Card>
    </div>
  );
}
