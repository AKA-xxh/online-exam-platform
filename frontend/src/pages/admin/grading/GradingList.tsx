import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Table, Select, Space, Tag, Button, Card } from 'antd';
import { getGradingList, publishScore } from '@/services/api/grading.api';
import { getExams } from '@/services/api/exam.api';

export default function AdminGradingList() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [exams, setExams] = useState<any[]>([]);
  const [params, setParams] = useState({ page: 1, pageSize: 20, examId: undefined as any, status: 0 });

  useEffect(() => { getExams({ pageSize: 200 }).then((res: any) => setExams(res.records || [])); }, []);
  useEffect(() => { setLoading(true); getGradingList(params).then(res => { setData(res.records || []); setTotal(res.total || 0); }).finally(() => setLoading(false)); }, [params]);

  return (
    <Card title={params.status === 0 ? '待阅卷列表' : '已阅卷列表'}>
      <Space style={{ marginBottom: 16 }}>
        <Select placeholder="选择考试" allowClear style={{ width: 200 }} value={params.examId} onChange={v => setParams({ ...params, examId: v, page: 1 })}
          options={exams.map((e: any) => ({ value: e.id, label: e.title }))} />
        <Select value={params.status} style={{ width: 120 }} onChange={v => setParams({ ...params, status: v, page: 1 })}
          options={[{ value: 0, label: '待阅卷' }, { value: 1, label: '已阅卷' }]} />
      </Space>
      <Table columns={[
        { title: '考试', dataIndex: 'examTitle', ellipsis: true },
        { title: '考生ID', dataIndex: 'userId', width: 80 },
        { title: '交卷时间', dataIndex: 'submitTime', width: 170 },
        { title: '客观题分', dataIndex: 'objectiveScore', width: 90 },
        { title: '总分', dataIndex: 'totalScore', width: 80 },
        { title: '状态', dataIndex: 'gradingStatus', width: 100, render: (s: number) => s === 1 ? <Tag color="green">已阅</Tag> : <Tag color="orange">待阅</Tag> },
        { title: '操作', width: 100, render: (_: any, r: any) => <Button size="small" type="primary" onClick={() => navigate(`/admin/grading/${r.examId}/${r.id}`)}>阅卷</Button> },
      ]} dataSource={data} rowKey="id" loading={loading}
        pagination={{ current: params.page, pageSize: params.pageSize, total, onChange: (p, ps) => setParams({ ...params, page: p, pageSize: ps }) }} />
    </Card>
  );
}
