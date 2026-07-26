import { useEffect, useState } from 'react';
import { Table, Select, Tag, Button, message, Card, Row, Col, Statistic } from 'antd';
import { getWrongQuestions, getWrongDetail, removeWrong, getWrongStats } from '@/services/api/wrong.api';

export default function StudentWrongList() {
  const [loading, setLoading] = useState(false);
  const [data, setData] = useState<any[]>([]);
  const [total, setTotal] = useState(0);
  const [stats, setStats] = useState<any>({});
  const [detail, setDetail] = useState<any>(null);
  const [params, setParams] = useState({ page: 1, pageSize: 20, questionType: undefined as any });

  useEffect(() => { getWrongStats().then(setStats); }, []);
  useEffect(() => { fetchData(); }, [params]);

  const fetchData = () => {
    setLoading(true);
    getWrongQuestions(params).then(res => { setData(res.records || []); setTotal(res.total || 0); }).finally(() => setLoading(false));
  };

  const viewDetail = async (id: number) => {
    const res = await getWrongDetail(id);
    setDetail(res);
  };

  const handleRemove = async (id: number) => { await removeWrong(id); message.success('已标记为掌握'); fetchData(); setDetail(null); };

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 16 }}>
        {[{ title: '错题总数', value: stats.total || 0 }, { title: '单选题', value: stats.singleChoice || 0 }, { title: '多选题', value: stats.multiChoice || 0 }, { title: '判断题', value: stats.trueFalse || 0 }, { title: '简答题', value: stats.essay || 0 }].map(s => (
          <Col span={4} key={s.title}><Card size="small"><Statistic title={s.title} value={s.value} /></Card></Col>
        ))}
      </Row>

      <Card>
        <Select placeholder="题型筛选" allowClear style={{ width: 120, marginBottom: 16 }} value={params.questionType} onChange={v => setParams({ ...params, questionType: v, page: 1 })}
          options={[{ value: 1, label: '单选题' }, { value: 2, label: '多选题' }, { value: 3, label: '判断题' }, { value: 4, label: '简答题' }]} />
        <Table columns={[
          { title: '题干', dataIndex: 'title', ellipsis: true, render: (t: string) => <div dangerouslySetInnerHTML={{ __html: t?.length > 100 ? t.slice(0, 100) + '...' : t }} /> },
          { title: '题型', dataIndex: 'questionType', width: 80, render: (t: number) => ({ 1: '单选', 2: '多选', 3: '判断', 4: '简答' })[t] },
          { title: '错误次数', dataIndex: 'wrongCount', width: 80 },
          { title: '操作', width: 150, render: (_: any, r: any) => (
            <div style={{ display: 'flex', gap: 8 }}>
              <Button size="small" onClick={() => viewDetail(r.id)}>查看</Button>
              <Button size="small" type="primary" onClick={() => handleRemove(r.id)}>已掌握</Button>
            </div>
          )},
        ]} dataSource={data} rowKey="id" loading={loading}
          pagination={{ current: params.page, pageSize: params.pageSize, total, onChange: (p, ps) => setParams({ ...params, page: p, pageSize: ps }) }} />
      </Card>

      {detail && (
        <Card title="错题详情" style={{ marginTop: 16 }} extra={<Button onClick={() => setDetail(null)}>关闭</Button>}>
          <div style={{ fontWeight: 500, marginBottom: 12 }} dangerouslySetInnerHTML={{ __html: detail.question?.title || '' }} />
          <div style={{ color: '#ff4d4f', marginBottom: 8 }}>错误次数：{detail.wrongQuestion?.wrongCount}</div>
          {(detail.options || []).map((opt: any) => (
            <div key={opt.optionLabel} style={{ padding: '4px 0', color: opt.isCorrect === 1 ? '#52c41a' : '#333' }}>
              <strong>{opt.optionLabel}.</strong> {opt.optionText} {opt.isCorrect === 1 && ' ✓'}
            </div>
          ))}
          {detail.question?.analysis && <div style={{ marginTop: 12, padding: 12, background: '#f6ffed', borderRadius: 6 }}><strong>解析：</strong>{detail.question.analysis}</div>}
        </Card>
      )}
    </div>
  );
}
