import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Table, Tag, Button, Space, Typography, message } from 'antd';
import { getExamMonitor, forceSubmit } from '@/services/api/exam.api';

const { Title } = Typography;

export default function AdminExamMonitor() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  const fetchData = async () => {
    if (!id) return;
    setLoading(true);
    try { const res = await getExamMonitor(Number(id)); setData(res); } finally { setLoading(false); }
  };

  useEffect(() => { fetchData(); const timer = setInterval(fetchData, 10000); return () => clearInterval(timer); }, [id]);

  const handleForceSubmit = async (userId: number) => {
    await forceSubmit(Number(id), userId);
    message.success('已强制收卷');
    fetchData();
  };

  const statusMap: any = {
    0: <Tag>未开始</Tag>, 1: <Tag color="processing">考试中</Tag>,
    2: <Tag color="warning">已交卷</Tag>, 3: <Tag color="success">已出分</Tag>, 4: <Tag color="default">缺考</Tag>,
  };

  return (
    <Card title="考试实时监控" extra={<Space><Button onClick={fetchData}>刷新</Button><Button onClick={() => navigate('/admin/exams')}>返回</Button></Space>}>
      {data && (
        <div style={{ marginBottom: 16 }}>
          <Space size="large">
            <span>总考生: <strong>{data.total}</strong></span>
            <span>考试中: <strong style={{ color: '#1677ff' }}>{data.inProgress}</strong></span>
            <span>已交卷: <strong style={{ color: '#52c41a' }}>{data.submitted}</strong></span>
            <span style={{ fontSize: 12, color: '#999' }}>每10秒自动刷新</span>
          </Space>
        </div>
      )}
      <Table loading={loading} dataSource={data?.students || []} rowKey="userId"
        columns={[
          { title: '考生ID', dataIndex: 'userId', width: 80 },
          { title: '状态', dataIndex: 'status', width: 100, render: (s: number) => statusMap[s] },
          { title: '开始时间', dataIndex: 'startTime', width: 170 },
          { title: '已用时间', dataIndex: 'usedTime', width: 100, render: (t: number) => t ? `${Math.floor(t/60)}分${t%60}秒` : '-' },
          { title: '切屏次数', dataIndex: 'screenSwitches', width: 90, render: (v: number) => <Tag color={v >= 3 ? 'red' : 'default'}>{v}</Tag> },
          { title: '作弊标记', dataIndex: 'isCheated', width: 90, render: (v: number) => v === 1 ? <Tag color="red">疑似</Tag> : v === 2 ? <Tag color="red">确认</Tag> : <Tag>正常</Tag> },
          { title: '操作', width: 120, render: (_: any, r: any) => r.status === 1 ? <Button size="small" danger onClick={() => handleForceSubmit(r.userId)}>强制收卷</Button> : '-' },
        ]}
      />
    </Card>
  );
}
