import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Card, Button, Input, InputNumber, Space, message, Typography, Descriptions, Divider, Tag } from 'antd';
import { getGradingDetail, submitScore, publishScore } from '@/services/api/grading.api';

const { Title } = Typography;

export default function AdminGradingDetail() {
  const { examId, id } = useParams();
  const navigate = useNavigate();
  const [data, setData] = useState<any>(null);
  const [scores, setScores] = useState<Record<number, { score: number; comment: string }>>({});
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (id) getGradingDetail(Number(id)).then(res => {
      setData(res);
      const init: any = {};
      (res.answers || []).forEach((a: any) => {
        if (a.questionType === 4) init[a.paperQuestionId] = { score: a.gradedScore || 0, comment: a.graderComment || '' };
      });
      setScores(init);
    });
  }, [id]);

  const handleSubmit = async (publish: boolean) => {
    setLoading(true);
    try {
      const scoreList = Object.entries(scores).map(([pqId, v]) => ({ paperQuestionId: Number(pqId), score: v.score, comment: v.comment }));
      await submitScore(Number(id), { scores: scoreList, publish });
      message.success(publish ? '评分已提交并发布成绩' : '评分已保存');
      navigate('/admin/grading');
    } finally { setLoading(false); }
  };

  if (!data) return <Card loading />;

  const s = data.examStudent;
  const subQuestions = (data.answers || []).filter((a: any) => a.questionType === 4);

  return (
    <Card title={`阅卷 - ${data.examTitle}`} extra={<Button onClick={() => navigate('/admin/grading')}>返回列表</Button>}>
      <Descriptions bordered size="small" style={{ marginBottom: 16 }}>
        <Descriptions.Item label="考生ID">{s.userId}</Descriptions.Item>
        <Descriptions.Item label="客观题得分">{s.objectiveScore}分</Descriptions.Item>
        <Descriptions.Item label="当前总分">{s.totalScore}分</Descriptions.Item>
      </Descriptions>

      <Divider>主观题批阅</Divider>
      {(data.answers || []).map((a: any, i: number) => (
        <Card key={a.paperQuestionId} size="small" style={{ marginBottom: 16 }}>
          <div style={{ marginBottom: 8, fontWeight: 500 }}>第{i + 1}题 ({a.score}分) — {a.questionType === 4 ? '简答题' : '客观题'}</div>
          <div style={{ marginBottom: 8 }} dangerouslySetInnerHTML={{ __html: a.title }} />
          {a.questionType !== 4 ? (
            <div>
              <div>学生答案：{a.userAnswer || '未作答'}</div>
              <div style={{ color: '#52c41a' }}>正确答案：{a.answerSnapshot}</div>
              <Tag color={a.isCorrect === 1 ? 'success' : 'error'}>系统判定：{a.isCorrect === 1 ? '正确' : '错误'}（{a.gradedScore}分）</Tag>
            </div>
          ) : (
            <Space direction="vertical" style={{ width: '100%' }}>
              <div style={{ background: '#fafafa', padding: 12, borderRadius: 6 }}>
                <div style={{ fontWeight: 500, marginBottom: 4 }}>学生答案：</div>
                <div>{a.userAnswer || '未作答'}</div>
              </div>
              <div style={{ background: '#f6ffed', padding: 12, borderRadius: 6 }}>
                <div style={{ fontWeight: 500, marginBottom: 4 }}>参考答案：</div>
                <div>{a.answerSnapshot || '无参考答案'}</div>
              </div>
              <Space>
                <span>得分：</span>
                <InputNumber min={0} max={a.score} value={scores[a.paperQuestionId]?.score}
                  onChange={v => setScores(prev => ({ ...prev, [a.paperQuestionId]: { ...prev[a.paperQuestionId], score: v || 0 } }))} />
                <span>/ {a.score}分</span>
              </Space>
              <Input placeholder="评语（可选）" value={scores[a.paperQuestionId]?.comment}
                onChange={e => setScores(prev => ({ ...prev, [a.paperQuestionId]: { ...prev[a.paperQuestionId], comment: e.target.value } }))} />
            </Space>
          )}
        </Card>
      ))}

      <div style={{ textAlign: 'center', marginTop: 24 }}>
        <Space size="large">
          <Button onClick={() => handleSubmit(false)} loading={loading}>暂存评分</Button>
          <Button type="primary" onClick={() => handleSubmit(true)} loading={loading}>评分并发布成绩</Button>
        </Space>
      </div>
    </Card>
  );
}
