import { useEffect, useState } from 'react';
import { Card, Table, Select, Tag } from 'antd';
import { getScores } from '@/services/api/statistics.api';
import { getExams } from '@/services/api/exam.api';

export default function AdminScoreList() {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [exams, setExams] = useState<any[]>([]);
  const [params, setParams] = useState({ page: 1, pageSize: 20, examId: undefined as any });

  useEffect(() => { getExams({ pageSize: 200 }).then((r: any) => setExams(r.records || [])); }, []);
  useEffect(() => { setLoading(true); getScores(params).then(r => { setData(r.records || []); setTotal(r.total || 0); }).finally(() => setLoading(false)); }, [params]);

  return (
    <Card title="成绩管理">
      <Select placeholder="选择考试" allowClear style={{ width: 240, marginBottom: 16 }} value={params.examId} onChange={v => setParams({ ...params, examId: v, page: 1 })}
        options={exams.map((e: any) => ({ value: e.id, label: e.title }))} />
      <Table columns={[
        { title: '考试', dataIndex: 'examTitle', ellipsis: true },
        { title: '考生ID', dataIndex: 'userId', width: 80 },
        { title: '总分', dataIndex: 'totalScore', width: 80, sorter: (a: any, b: any) => (a.totalScore || 0) - (b.totalScore || 0) },
        { title: '客观题', dataIndex: 'objectiveScore', width: 80 },
        { title: '主观题', dataIndex: 'subjectiveScore', width: 80 },
        { title: '是否及格', dataIndex: 'isPassed', width: 90, render: (v: number) => <Tag color={v ? 'success' : 'error'}>{v ? '及格' : '不及格'}</Tag> },
        { title: '用时', dataIndex: 'usedTime', width: 80, render: (t: number) => t ? `${Math.floor(t/60)}分${t%60}秒` : '-' },
        { title: '交卷时间', dataIndex: 'submitTime', width: 170 },
      ]} dataSource={data} rowKey="id" loading={loading}
        pagination={{ current: params.page, pageSize: params.pageSize, total, onChange: (p, ps) => setParams({ ...params, page: p, pageSize: ps }) }} />
    </Card>
  );
}
