import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Table, Button, Input, Select, Space, Tag, Popconfirm, message, Card } from 'antd';
import { PlusOutlined, SearchOutlined } from '@ant-design/icons';
import { getPapers, deletePaper } from '@/services/api/paper.api';

export default function AdminPaperList() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [params, setParams] = useState({ page: 1, pageSize: 20, keyword: '', paperType: undefined as any });

  const fetchData = async () => {
    setLoading(true);
    try { const res = await getPapers(params); setData(res.records || []); setTotal(res.total || 0); } finally { setLoading(false); }
  };
  useEffect(() => { fetchData(); }, [params]);

  const columns = [
    { title: 'ID', dataIndex: 'id', width: 60 },
    { title: '试卷名称', dataIndex: 'title', ellipsis: true },
    { title: '总分', dataIndex: 'totalScore', width: 80 },
    { title: '及格分', dataIndex: 'passScore', width: 80 },
    { title: '时长(分钟)', dataIndex: 'duration', width: 90 },
    { title: '组卷方式', dataIndex: 'paperType', width: 90, render: (t: number) => <Tag>{t === 1 ? '手动组卷' : '随机组卷'}</Tag> },
    { title: '状态', dataIndex: 'status', width: 80, render: (s: number) => ({ 0: <Tag>草稿</Tag>, 1: <Tag color="green">已发布</Tag> })[s] },
    { title: '操作', width: 160, render: (_: any, r: any) => (
      <Space>
        <Button size="small" onClick={() => navigate(`/admin/papers/${r.id}`)}>编辑</Button>
        <Popconfirm title="确认删除？" onConfirm={async () => { await deletePaper(r.id); fetchData(); }}><Button size="small" danger>删除</Button></Popconfirm>
      </Space>
    )},
  ];

  return (
    <Card>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Space>
          <Input placeholder="搜索试卷" prefix={<SearchOutlined />} allowClear style={{ width: 200 }}
            value={params.keyword} onChange={e => setParams({ ...params, keyword: e.target.value, page: 1 })} />
          <Select placeholder="组卷方式" allowClear style={{ width: 120 }} value={params.paperType} onChange={v => setParams({ ...params, paperType: v, page: 1 })}
            options={[{ value: 1, label: '手动组卷' }, { value: 2, label: '随机组卷' }]} />
        </Space>
        <Space>
          <Button onClick={() => navigate('/admin/papers/new?type=manual')}>手动组卷</Button>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/admin/papers/new?type=random')}>随机组卷</Button>
        </Space>
      </div>
      <Table columns={columns} dataSource={data} rowKey="id" loading={loading}
        pagination={{ current: params.page, pageSize: params.pageSize, total, onChange: (p, ps) => setParams({ ...params, page: p, pageSize: ps }) }} />
    </Card>
  );
}
