import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Table, Button, Input, Select, Space, Tag, message, Card } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { getExams, cancelExam } from '@/services/api/exam.api';

const statusOptions = [{ value: 0, label: '未开始' }, { value: 1, label: '进行中' }, { value: 2, label: '已结束' }, { value: 3, label: '已取消' }];

export default function AdminExamList() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [params, setParams] = useState({ page: 1, pageSize: 20, keyword: '', status: undefined as any });

  const fetchData = async () => {
    setLoading(true);
    try { const res = await getExams(params); setData(res.records || []); setTotal(res.total || 0); } finally { setLoading(false); }
  };
  useEffect(() => { fetchData(); }, [params]);

  const statusMap: any = { 0: <Tag>未开始</Tag>, 1: <Tag color="processing">进行中</Tag>, 2: <Tag color="default">已结束</Tag>, 3: <Tag color="red">已取消</Tag> };

  return (
    <Card>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 16 }}>
        <Space>
          <Input.Search placeholder="搜索考试" allowClear style={{ width: 200 }} onSearch={v => setParams({ ...params, keyword: v, page: 1 })} />
          <Select placeholder="状态" allowClear style={{ width: 120 }} value={params.status} onChange={v => setParams({ ...params, status: v, page: 1 })} options={statusOptions} />
        </Space>
        <Button type="primary" icon={<PlusOutlined />} onClick={() => navigate('/admin/exams/new')}>发布考试</Button>
      </div>
      <Table columns={[
        { title: 'ID', dataIndex: 'id', width: 60 },
        { title: '考试名称', dataIndex: 'title' },
        { title: '开始时间', dataIndex: 'startTime', width: 160 },
        { title: '截止时间', dataIndex: 'endTime', width: 160 },
        { title: '时长', dataIndex: 'duration', width: 80, render: (d: number) => `${d}分钟` },
        { title: '考生', dataIndex: 'studentCount', width: 60 },
        { title: '状态', dataIndex: 'status', width: 100, render: (s: number) => statusMap[s] },
        { title: '操作', width: 180, render: (_: any, r: any) => (
          <Space>
            {r.status === 1 && <Button size="small" onClick={() => navigate(`/admin/exams/${r.id}/monitor`)}>监控</Button>}
            {r.status <= 1 && <Button size="small" danger onClick={async () => { await cancelExam(r.id); fetchData(); message.success('已取消'); }}>取消</Button>}
          </Space>
        )},
      ]} dataSource={data} rowKey="id" loading={loading}
        pagination={{ current: params.page, pageSize: params.pageSize, total, onChange: (p, ps) => setParams({ ...params, page: p, pageSize: ps }) }} />
    </Card>
  );
}
