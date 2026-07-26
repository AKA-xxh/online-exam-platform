import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Table, Tag, Button, Typography } from 'antd';
import { ClockCircleOutlined, PlayCircleOutlined } from '@ant-design/icons';
import { getMyExams } from '@/services/api/exam.api';

const { Title } = Typography;

export default function StudentExamList() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [exams, setExams] = useState<any[]>([]);

  useEffect(() => { setLoading(true); getMyExams().then(setExams).finally(() => setLoading(false)); }, []);

  const statusMap: any = {
    0: { color: 'default', text: '未开始' },
    1: { color: 'processing', text: '考试中' },
    2: { color: 'warning', text: '已交卷' },
    3: { color: 'success', text: '已出分' },
  };

  return (
    <div>
      <Title level={4}>我的考试</Title>
      <Table dataSource={exams} rowKey="examId" loading={loading}
        columns={[
          { title: '考试名称', dataIndex: 'title', ellipsis: true },
          { title: '开始时间', dataIndex: 'startTime', width: 170 },
          { title: '截止时间', dataIndex: 'endTime', width: 170 },
          { title: '时长', dataIndex: 'duration', width: 80, render: (d: number) => `${d}分钟` },
          { title: '状态', dataIndex: 'myStatus', width: 100,
            render: (s: number) => <Tag color={statusMap[s]?.color}>{statusMap[s]?.text}</Tag> },
          { title: '成绩', dataIndex: 'totalScore', width: 80, render: (s: any) => s != null ? `${s}分` : '-' },
          { title: '操作', width: 120,
            render: (_: any, r: any) => {
              if (r.myStatus === 0 || r.myStatus === 1) return <Button  type="primary"  icon={<PlayCircleOutlined />} onClick={() => navigate(`/student/exams/${r.examId}`)}>进入考试</Button>;
              if (r.myStatus >= 2) return <Button onClick={() => navigate(`/student/exams/${r.studentExamId}/result`)}>查看成绩</Button>;
              return '-';
            },
          },
        ]}
      />
    </div>
  );
}
