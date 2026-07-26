import { useEffect, useState } from 'react';
import { Table, Button, Input, Select, Space, Tag, Popconfirm, message, Card, Modal, Form } from 'antd';
import { PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined, LockOutlined } from '@ant-design/icons';
import { getUsers, createUser, updateUser, deleteUser, toggleUserStatus, resetPassword, getRoles } from '@/services/api/user.api';

export default function AdminUserList() {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [roles, setRoles] = useState<any[]>([]);
  const [editOpen, setEditOpen] = useState(false);
  const [editing, setEditing] = useState<any>(null);
  const [form] = Form.useForm();
  const [params, setParams] = useState({
    page: 1, pageSize: 20, keyword: '', userType: undefined as any, status: undefined as any,
  });

  useEffect(() => { getRoles().then(setRoles); }, []);
  useEffect(() => { fetchData(); }, [params]);

  const fetchData = () => {
    setLoading(true);
    getUsers(params).then(res => { setData(res.records || []); setTotal(res.total || 0); }).finally(() => setLoading(false));
  };

  const openCreate = () => { setEditing(null); form.resetFields(); form.setFieldsValue({ userType: 1, status: 1 }); setEditOpen(true); };
  const openEdit = (user: any) => { setEditing(user); form.setFieldsValue(user); setEditOpen(true); };

  const handleSave = async () => {
    const values = await form.validateFields();
    if (editing) await updateUser(editing.id, values);
    else await createUser(values);
    message.success(editing ? '保存成功' : '创建成功');
    setEditOpen(false);
    fetchData();
  };

  const handleDelete = async (id: number) => { await deleteUser(id); message.success('已删除'); fetchData(); };
  const handleToggle = async (id: number, status: number) => { await toggleUserStatus(id, status); message.success(status === 1 ? '已启用' : '已禁用'); fetchData(); };
  const handleResetPwd = async (id: number) => { await resetPassword(id); message.success('密码已重置为 123456'); };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 50 },
    { title: '用户名', dataIndex: 'username', width: 100 },
    { title: '姓名', dataIndex: 'realName', width: 80 },
    { title: '手机号', dataIndex: 'phone', width: 120 },
    { title: '角色', dataIndex: 'userTypeName', width: 70, render: (t: string, r: any) => (
      <Tag color={r.userType === 3 ? 'purple' : r.userType === 2 ? 'blue' : 'green'}>{t}</Tag>
    )},
    { title: '状态', dataIndex: 'status', width: 70, render: (s: number) => (
      <Tag color={s === 1 ? 'success' : 'error'}>{s === 1 ? '正常' : '禁用'}</Tag>
    )},
    { title: '最后登录', dataIndex: 'lastLoginTime', width: 160 },
    { title: '操作', width: 240, fixed: 'right' as const, render: (_: any, r: any) => (
      <Space>
        <Button size="small" icon={<EditOutlined />} onClick={() => openEdit(r)}>编辑</Button>
        {r.id !== 1 && (
          <>
            {r.status === 1
              ? <Button size="small" onClick={() => handleToggle(r.id, 0)}>禁用</Button>
              : <Button size="small" onClick={() => handleToggle(r.id, 1)}>启用</Button>
            }
            <Button size="small" icon={<LockOutlined />} onClick={() => handleResetPwd(r.id)}>重置密码</Button>
            <Popconfirm title="确认删除？" onConfirm={() => handleDelete(r.id)}>
              <Button size="small" danger icon={<DeleteOutlined />} />
            </Popconfirm>
          </>
        )}
      </Space>
    )},
  ];

  return (
    <Card>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Space>
          <Input placeholder="搜索用户名/姓名/手机/邮箱" prefix={<SearchOutlined />} allowClear style={{ width: 260 }}
            value={params.keyword} onChange={e => setParams({ ...params, keyword: e.target.value, page: 1 })} />
          <Select placeholder="角色" allowClear style={{ width: 100 }} value={params.userType}
            onChange={v => setParams({ ...params, userType: v, page: 1 })}
            options={[{ value: 1, label: '学员' }, { value: 2, label: '教师' }, { value: 3, label: '管理员' }]} />
          <Select placeholder="状态" allowClear style={{ width: 90 }} value={params.status}
            onChange={v => setParams({ ...params, status: v, page: 1 })}
            options={[{ value: 1, label: '正常' }, { value: 0, label: '禁用' }]} />
        </Space>
        <Button type="primary" icon={<PlusOutlined />} onClick={openCreate}>新增用户</Button>
      </div>

      <Table columns={columns} dataSource={data} rowKey="id" loading={loading}
        pagination={{ current: params.page, pageSize: params.pageSize, total, showSizeChanger: true,
          onChange: (p, ps) => setParams({ ...params, page: p, pageSize: ps }) }} />

      <Modal title={editing ? '编辑用户' : '新增用户'} open={editOpen} onOk={handleSave} onCancel={() => setEditOpen(false)} width={480}>
        <Form form={form} layout="vertical">
          <Form.Item name="username" label="用户名" rules={[{ required: true, message: '请输入用户名' }]}>
            <Input disabled={!!editing} placeholder={editing ? '' : '登录用，创建后不可修改'} />
          </Form.Item>
          {!editing && <Form.Item name="password" label="密码"><Input.Password placeholder="默认 123456" /></Form.Item>}
          <Form.Item name="realName" label="姓名" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="phone" label="手机号"><Input /></Form.Item>
          <Form.Item name="email" label="邮箱"><Input /></Form.Item>
          <Form.Item name="userType" label="角色" rules={[{ required: true }]}>
            <Select options={roles.map((r: any) => ({ value: r.id === 3 ? 3 : r.id === 2 ? 2 : 1, label: r.roleName }))} />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select options={[{ value: 1, label: '正常' }, { value: 0, label: '禁用' }]} />
          </Form.Item>
        </Form>
      </Modal>
    </Card>
  );
}
