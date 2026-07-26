import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Table, Button, Input, Select, Space, Tag, Popconfirm, message, Card } from 'antd';
import { PlusOutlined, SearchOutlined, EditOutlined, DeleteOutlined, ApartmentOutlined } from '@ant-design/icons';
import { getCourses, deleteCourse, updateCourseStatus, getCategories } from '@/services/api/course.api';
import CategoryManage from '@/components/common/CategoryManage';
import { createCategory, updateCategory, deleteCategory } from '@/services/api/course.api';

export default function AdminCourseList() {
  const [catOpen, setCatOpen] = useState(false);
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [categories, setCategories] = useState<any[]>([]);
  const [params, setParams] = useState({ page: 1, pageSize: 20, keyword: '', categoryId: undefined as any, status: undefined as any });

  const fetchData = async () => {
    setLoading(true);
    try {
      const res = await getCourses(params);
      setData(res.records || []);
      setTotal(res.total || 0);
    } finally { setLoading(false); }
  };

  const fetchCategories = async () => {
    const res = await getCategories();
    setCategories(flattenCategories(res || []));
  };

  const flattenCategories = (list: any[], prefix = ''): any[] => {
    let result: any[] = [];
    list.forEach((item: any) => {
      result.push({ ...item, label: prefix + item.name });
      if (item.children) result = result.concat(flattenCategories(item.children, prefix + '--'));
    });
    return result;
  };

  useEffect(() => { fetchCategories(); }, []);
  useEffect(() => { fetchData(); }, [params]);

  const handleDelete = async (id: number) => {
    await deleteCourse(id);
    message.success('删除成功');
    fetchData();
  };

  const handleStatusChange = async (id: number, status: number) => {
    await updateCourseStatus(id, status);
    message.success('状态已更新');
    fetchData();
  };

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '课程名称', dataIndex: 'title', ellipsis: true,
      render: (text: string, record: any) => <a onClick={() => navigate(`/admin/courses/${record.id}`)}>{text}</a> },
    { title: '分类', dataIndex: 'categoryName', width: 120 },
    { title: '讲师', dataIndex: 'teacherName', width: 100 },
    { title: '课时', dataIndex: 'lessonCount', width: 60 },
    { title: '学习人数', dataIndex: 'studentCount', width: 80 },
    { title: '状态', dataIndex: 'status', width: 100,
      render: (s: number) => ({ 0: <Tag>草稿</Tag>, 1: <Tag color="green">已发布</Tag>, 2: <Tag color="red">已下架</Tag> })[s] },
    { title: '操作', width: 200, fixed: 'right' as const,
      render: (_: any, record: any) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => navigate(`/admin/courses/${record.id}`)}>编辑</Button>
          {record.status === 0 && <Button size="small" type="primary" onClick={() => handleStatusChange(record.id, 1)}>发布</Button>}
          {record.status === 1 && <Button size="small" onClick={() => handleStatusChange(record.id, 2)}>下架</Button>}
          {record.status === 2 && <Button size="small" onClick={() => handleStatusChange(record.id, 1)}>上架</Button>}
          <Popconfirm title="确认删除？" onConfirm={() => handleDelete(record.id)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <>
    <Card>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Space>
          <Input placeholder="搜索课程" prefix={<SearchOutlined />} allowClear
            value={params.keyword} onChange={e => setParams({ ...params, keyword: e.target.value, page: 1 })} />
          <Select placeholder="选择分类" allowClear style={{ width: 150 }}
            value={params.categoryId} onChange={v => setParams({ ...params, categoryId: v, page: 1 })}
            options={categories.map(c => ({ value: c.id, label: c.label }))} />
          <Select placeholder="状态" allowClear style={{ width: 100 }}
            value={params.status} onChange={v => setParams({ ...params, status: v, page: 1 })}
            options={[{ value: 0, label: '草稿' }, { value: 1, label: '已发布' }, { value: 2, label: '已下架' }]} />
        </Space>
        <Space>
          <Button icon={<ApartmentOutlined />} onClick={() => setCatOpen(true)}>分类管理</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/admin/courses/new')}>新建课程</Button>
        </Space>
      </div>
      <Table columns={columns} dataSource={data} rowKey="id" loading={loading}
        pagination={{ current: params.page, pageSize: params.pageSize, total, onChange: (p, ps) => setParams({ ...params, page: p, pageSize: ps }) }} />
    </Card>
    <CategoryManage open={catOpen} title="课程分类管理" onClose={() => { setCatOpen(false); fetchData(); }}
      api={{ getTree: getCategories, create: (d: any) => createCategory(d), update: (id: number, d: any) => updateCategory(id, d), remove: (id: number) => deleteCategory(id) }} />
    </>
  );
}
