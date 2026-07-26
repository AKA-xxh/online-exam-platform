import { Card, Form, Input, Button, message, Switch } from 'antd';

export default function AdminSystemConfig() {
  const [form] = Form.useForm();

  const onFinish = (values: any) => {
    console.log('保存配置:', values);
    message.success('系统配置已保存');
  };

  return (
    <Card title="系统设置">
      <Form form={form} layout="vertical" style={{ maxWidth: 500 }} onFinish={onFinish}
        initialValues={{ siteName: '在线考试培训系统', registerEnabled: true, registerVerify: false }}>
        <Form.Item name="siteName" label="平台名称"><Input /></Form.Item>
        <Form.Item name="siteLogo" label="Logo URL"><Input placeholder="https://..." /></Form.Item>
        <Form.Item name="siteCopyright" label="版权信息"><Input /></Form.Item>
        <Form.Item name="registerEnabled" label="开放注册" valuePropName="checked"><Switch /></Form.Item>
        <Form.Item name="registerVerify" label="注册需验证码" valuePropName="checked"><Switch /></Form.Item>
        <Form.Item><Button type="primary" htmlType="submit">保存配置</Button></Form.Item>
      </Form>
    </Card>
  );
}
