import { useEffect, useState } from 'react';
import { useParams, useLocation } from 'react-router-dom';
import { Card, Typography, Descriptions, Tag, Divider, Space } from 'antd';
import { CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons';
import { getExamResult } from '@/services/api/exam.api';

const { Title } = Typography;

export default function StudentExamResult() {
  const { id } = useParams();
  const location = useLocation();
  const [data, setData] = useState<any>(null);
  const preResult = (location.state as any)?.result;

  useEffect(() => {
    if (preResult) setData(preResult);
    else if (id) getExamResult(Number(id)).then(setData);
  }, [id]);

  if (!data) return <Card loading />;

  const s = data.examStudent || data;

  return (
    <div style={{ maxWidth: 800, margin: '0 auto' }}>
      <Card>
        <Title level={4} style={{ textAlign: 'center' }}>考试成绩</Title>
        <Descriptions bordered column={2} style={{ marginTop: 24 }}>
          <Descriptions.Item label="总分">{s.totalScore} 分</Descriptions.Item>
          <Descriptions.Item label="是否及格">
            <Tag color={s.isPassed ? 'success' : 'error'}>{s.isPassed ? '及格' : '不及格'}</Tag>
          </Descriptions.Item>
          <Descriptions.Item label="客观题得分">{s.objectiveScore} 分</Descriptions.Item>
          <Descriptions.Item label="主观题得分">{data.hasSubjective ? `${s.subjectiveScore || '阅卷中'} 分` : '无主观题'}</Descriptions.Item>
          <Descriptions.Item label="用时">{Math.floor((s.usedTime || 0) / 60)}分{s.usedTime % 60}秒</Descriptions.Item>
          <Descriptions.Item label="切屏次数">{s.screenSwitches || 0} 次</Descriptions.Item>
        </Descriptions>
      </Card>
    </div>
  );
}
