import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Table, Button, Input, Select, Space, Tag, Popconfirm, message, Card, Row, Col, Statistic } from 'antd';
import { PlusOutlined, SearchOutlined, DeleteOutlined, EditOutlined, ApartmentOutlined } from '@ant-design/icons';
import { getQuestions, deleteQuestion, batchDeleteQuestions, batchUpdateStatus, getQuestionCategories, getQuestionStats, createQuestionCategory, updateQuestionCategory, deleteQuestionCategory } from '@/services/api/question.api';
import CategoryManage from '@/components/common/CategoryManage';

const typeOptions = [{ value: 1, label: '单选题' }, { value: 2, label: '多选题' }, { value: 3, label: '判断题' }, { value: 4, label: '简答题' }];
const diffOptions = [{ value: 1, label: '简单' }, { value: 2, label: '中等' }, { value: 3, label: '困难' }];

export default function AdminQuestionList() {
  const navigate = useNavigate();
  const [catOpen, setCatOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [selected, setSelected] = useState<number[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [stats, setStats] = useState<any>({});
  const [params, setParams] = useState({ page: 1, pageSize: 20, keyword: '', type: undefined as any, categoryId: undefined as any, difficulty: undefined as any });

  useEffect(() => { getQuestionCategories().then(res => setCategories(res || [])); getQuestionStats().then(setStats); }, []);
  useEffect(() => { setLoading(true); getQuestions(params).then(res => { setData(res.records || []); setTotal(res.total || 0); }).finally(() => setLoading(false)); }, [params]);

  const handleDelete = async (id: number) => { await deleteQuestion(id); message.success('已删除'); fetchData(); };
  const fetchData = () => getQuestions(params).then(res => { setData(res.records || []); setTotal(res.total || 0); });

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '题干', dataIndex: 'title', ellipsis: true },
    { title: '题型', dataIndex: 'typeName', width: 80, render: (t: string) => <Tag>{t}</Tag> },
    { title: '分类', dataIndex: 'categoryName', width: 100 },
    { title: '难度', dataIndex: 'difficultyName', width: 80, render: (d: string) => <Tag color={d==='简单'?'green':d==='中等'?'orange':'red'}>{d}</Tag> },
    { title: '使用次数', dataIndex: 'useCount', width: 80 },
    { title: '操作', width: 120, render: (_:any, r:any) => (
      <Space>
        <Button size="small" icon={<EditOutlined />} onClick={() => navigate(`/admin/questions/${r.id}`)} />
        <Popconfirm title="确认删除？" onConfirm={() => handleDelete(r.id)}><Button size="small" danger icon={<DeleteOutlined />} /></Popconfirm>
      </Space>
    )},
  ];

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        {[{ title: '总题数', value: stats.total || 0 }, { title: '单选题', value: stats.singleChoice || 0 }, { title: '多选题', value: stats.multiChoice || 0 }, { title: '判断题', value: stats.trueFalse || 0 }, { title: '简答题', value: stats.essay || 0 }].map(s => (
          <Col span={4} key={s.title}><Card size="small"><Statistic title={s.title} value={s.value} /></Card></Col>
        ))}
      </Row>
      <Card>
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
          <Space>
            <Input placeholder="搜索题干" prefix={<SearchOutlined />} allowClear style={{ width: 200 }}
              value={params.keyword} onChange={e => setParams({ ...params, keyword: e.target.value, page: 1 })} />
            <Select placeholder="题型" allowClear style={{ width: 100 }} value={params.type} onChange={v => setParams({ ...params, type: v, page: 1 })} options={typeOptions} />
            <Select placeholder="分类" allowClear style={{ width: 140 }} value={params.categoryId} onChange={v => setParams({ ...params, categoryId: v, page: 1 })} options={categories.map((c:any)=>({value:c.id,label:c.name}))} />
            <Select placeholder="难度" allowClear style={{ width: 100 }} value={params.difficulty} onChange={v => setParams({ ...params, difficulty: v, page: 1 })} options={diffOptions} />
          </Space>
          <Space>
            {selected.length > 0 && <>
              <Popconfirm title="确认批量删除？" onConfirm={async () => { await batchDeleteQuestions(selected); setSelected([]); fetchData(); }}><Button danger>删除选中({selected.length})</Button></Popconfirm>
              <Button onClick={async () => { await batchUpdateStatus(selected, 0); setSelected([]); fetchData(); }}>批量禁用</Button>
            </>}
            <Button icon={<ApartmentOutlined />} onClick={() => setCatOpen(true)}>分类管理</Button>
            <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/admin/questions/new')}>新增题目</Button>
          </Space>
        </div>
        <Table columns={columns} dataSource={data} rowKey="id" loading={loading}
          rowSelection={{ selectedRowKeys: selected, onChange: k => setSelected(k as number[]) }}
          pagination={{ current: params.page, pageSize: params.pageSize, total, showSizeChanger: true, onChange: (p, ps) => setParams({ ...params, page: p, pageSize: ps }) }} />
      </Card>
      <CategoryManage open={catOpen} title="题目分类管理" onClose={() => { setCatOpen(false); fetchData(); }}
        api={{ getTree: getQuestionCategories, create: (d: any) => createQuestionCategory(d), update: (id: number, d: any) => updateQuestionCategory(id, d), remove: (id: number) => deleteQuestionCategory(id) }} />
    </div>
  );
}
