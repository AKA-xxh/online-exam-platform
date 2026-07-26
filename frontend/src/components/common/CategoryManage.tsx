import { useEffect, useState } from 'react';
import { Drawer, Tree, Button, Form, Input, InputNumber, Popconfirm, Space, message, Empty } from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, FolderOutlined } from '@ant-design/icons';

interface Props {
  open: boolean;
  onClose: () => void;
  title?: string;
  api: {
    getTree: () => Promise<any>;
    create: (data: any) => Promise<any>;
    update: (id: number, data: any) => Promise<any>;
    remove: (id: number) => Promise<any>;
  };
}

export default function CategoryManage({ open, onClose, title = '分类管理', api }: Props) {
  const [tree, setTree] = useState<any[]>([]);
  const [selected, setSelected] = useState<any>(null);
  const [editing, setEditing] = useState<any>(null);
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);

  const fetchTree = async () => {
    const res = await api.getTree();
    setTree(buildTreeNodes(res || []));
  };

  const buildTreeNodes = (list: any[]): any[] =>
    list.map(item => ({
      key: item.id, title: item.name, icon: <FolderOutlined />,
      children: item.children ? buildTreeNodes(item.children) : undefined,
      raw: item,
    }));

  useEffect(() => { if (open) fetchTree(); }, [open]);

  const handleSelect = (_keys: any[], info: any) => {
    const raw = info.node.raw;
    setSelected(raw);
    form.setFieldsValue(raw);
    setEditing(null);
  };

  const startNew = (parentId: number = 0) => {
    setSelected(null);
    setEditing({ parentId, category: null });
    form.resetFields();
    form.setFieldsValue({ parentId, sortOrder: 0 });
  };

  const startEdit = () => {
    if (!selected) return;
    setEditing({ parentId: selected.parentId || 0, category: selected });
    form.setFieldsValue(selected);
  };

  const handleSave = async () => {
    const values = await form.validateFields();
    setLoading(true);
    try {
      if (editing?.category) {
        await api.update(editing.category.id, values);
        message.success('已更新');
      } else {
        await api.create({ ...values, parentId: editing?.parentId || 0 });
        message.success('已创建');
      }
      setEditing(null); setSelected(null); form.resetFields();
      fetchTree();
    } finally { setLoading(false); }
  };

  const handleDelete = async (id: number) => {
    try {
      await api.remove(id);
      message.success('已删除');
      setSelected(null); form.resetFields();
      fetchTree();
    } catch {
      message.error('删除失败（可能存在子分类或关联数据）');
    }
  };

  return (
    <Drawer title={title} open={open} onClose={onClose} width={560}>
      <div style={{ display: 'flex', gap: 16, minHeight: 400 }}>
        <div style={{ flex: 1, borderRight: '1px solid #f0f0f0', paddingRight: 12 }}>
          <div style={{ marginBottom: 12 }}>
            <Space>
              <Button size="small" type="primary" icon={<PlusOutlined />} onClick={() => startNew(0)}>新增顶级</Button>
              {selected && <Button size="small" icon={<PlusOutlined />} onClick={() => startNew(selected.id)}>新增子级</Button>}
            </Space>
          </div>
          {tree.length === 0
            ? <Empty description="暂无分类" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            : <Tree showIcon defaultExpandAll treeData={tree} selectedKeys={selected ? [selected.id] : []}
                onSelect={handleSelect as any} style={{ background: 'transparent' }} />}
        </div>
        <div style={{ width: 240 }}>
          {editing ? (
            <Form form={form} layout="vertical" size="small">
              <Form.Item name="parentId" hidden><Input /></Form.Item>
              <Form.Item name="name" label="分类名称" rules={[{ required: true }]}>
                <Input placeholder="如：Java基础" autoFocus /></Form.Item>
              <Form.Item name="sortOrder" label="排序"><InputNumber min={0} style={{ width: '100%' }} /></Form.Item>
              <Form.Item><Space>
                <Button type="primary" loading={loading} onClick={handleSave}>保存</Button>
                <Button onClick={() => { setEditing(null); form.resetFields(); }}>取消</Button>
              </Space></Form.Item>
            </Form>
          ) : selected ? (
            <div>
              <div style={{ fontWeight: 600, marginBottom: 8 }}>{selected.name}</div>
              <div style={{ color: '#8c8c8c', fontSize: 12, marginBottom: 16 }}>ID: {selected.id} | 排序: {selected.sortOrder}</div>
              <Space direction="vertical">
                <Button block size="small" icon={<EditOutlined />} onClick={startEdit}>编辑</Button>
                <Button block size="small" icon={<PlusOutlined />} onClick={() => startNew(selected.id)}>添加子分类</Button>
                <Popconfirm title="删除该分类？" onConfirm={() => handleDelete(selected.id)}>
                  <Button block size="small" danger icon={<DeleteOutlined />}>删除</Button>
                </Popconfirm>
              </Space>
            </div>
          ) : (
            <div style={{ color: '#8c8c8c', textAlign: 'center', paddingTop: 40 }}>选择左侧分类<br />或点击上方按钮新建</div>
          )}
        </div>
      </div>
    </Drawer>
  );
}
