import { Card, Table, Tag, Button, Space, Typography } from 'antd';

const { Title } = Typography;

const mockRoles = [
  { id: 1, name: '超级管理员', code: 'admin', description: '平台最高权限', status: 1 },
  { id: 2, name: '教师', code: 'teacher', description: '课程管理、题库管理、组卷阅卷', status: 1 },
  { id: 3, name: '学员', code: 'student', description: '学习课程、参加考试', status: 1 },
];

export default function AdminRoleManage() {
  return (
    <Card title="角色权限管理">
      <Table columns={[
        { title: 'ID', dataIndex: 'id', width: 60 },
        { title: '角色名称', dataIndex: 'name' },
        { title: '角色编码', dataIndex: 'code' },
        { title: '描述', dataIndex: 'description' },
        { title: '状态', dataIndex: 'status', render: (v: number) => <Tag color={v ? 'success' : 'error'}>{v ? '启用' : '禁用'}</Tag> },
        { title: '操作', render: () => <Button size="small">配置权限</Button> },
      ]} dataSource={mockRoles} rowKey="id" />
    </Card>
  );
}
