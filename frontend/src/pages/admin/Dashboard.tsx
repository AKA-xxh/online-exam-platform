import { useEffect, useState } from 'react';
import { Card, Row, Col, Statistic, Typography } from 'antd';
import { UserOutlined, BookOutlined, ScheduleOutlined, TeamOutlined } from '@ant-design/icons';
import ReactECharts from 'echarts-for-react';
import { getDashboard, getExamStats, getQuestionStats } from '@/services/api/statistics.api';
import { getExams } from '@/services/api/exam.api';

const { Title } = Typography;

export default function Dashboard() {
  const [dash, setDash] = useState<any>({});
  const [examList, setExamList] = useState<any[]>([]);
  const [examData, setExamData] = useState<any>(null);
  const [questionData, setQuestionData] = useState<any[]>([]);

  useEffect(() => { getDashboard().then(setDash); getExams({ pageSize: 10, status: undefined }).then((r: any) => setExamList(r.records || [])); }, []);
  useEffect(() => {
    if (examList.length > 0) {
      const latestId = examList[0].id;
      getExamStats(latestId).then(setExamData);
      getQuestionStats(latestId).then(setQuestionData);
    }
  }, [examList]);

  const stats = [
    { title: '学员总数', value: dash.totalStudents || 0, icon: <UserOutlined style={{ fontSize: 32, color: '#1677ff' }} />, color: '#e6f4ff' },
    { title: '课程总数', value: dash.totalCourses || 0, icon: <BookOutlined style={{ fontSize: 32, color: '#52c41a' }} />, color: '#f6ffed' },
    { title: '考试总数', value: dash.totalExams || 0, icon: <ScheduleOutlined style={{ fontSize: 32, color: '#faad14' }} />, color: '#fffbe6' },
    { title: '进行中考试', value: dash.inProgressExams || 0, icon: <TeamOutlined style={{ fontSize: 32, color: '#722ed1' }} />, color: '#f9f0ff' },
  ];

  const distData = examData?.scoreDistribution;
  const distOption = {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: distData ? Object.keys(distData) : [] },
    yAxis: { type: 'value', name: '人数' },
    series: [{ name: '人数', type: 'bar', data: distData ? Object.values(distData) : [], itemStyle: { color: '#1677ff', borderRadius: [4, 4, 0, 0] } }],
  };

  const questionOption = {
    tooltip: { trigger: 'axis' },
    grid: { left: 100, right: 30 },
    yAxis: { type: 'category', data: (questionData || []).map((q: any) => q.title || '').reverse(), axisLabel: { width: 90, overflow: 'truncate' } },
    xAxis: { type: 'value', name: '正确率%', max: 100 },
    series: [{ name: '正确率', type: 'bar', data: (questionData || []).map((q: any) => +(q.correctRate || 0).toFixed(1)).reverse(),
      itemStyle: { color: (p: any) => p.value >= 60 ? '#52c41a' : '#ff4d4f' }, label: { show: true, position: 'right', formatter: '{c}%' } }],
  };

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>数据概览</Title>
      <Row gutter={[16, 16]}>
        {stats.map(item => (
          <Col xs={24} sm={12} lg={6} key={item.title}>
            <Card><div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
              <div style={{ width: 64, height: 64, borderRadius: 12, background: item.color, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>{item.icon}</div>
              <Statistic title={item.title} value={item.value} />
            </div></Card>
          </Col>
        ))}
      </Row>

      {examData && (
        <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
          <Col xs={24} lg={12}>
            <Card title={`分数分布 - ${examList[0]?.title || '最近考试'}`}>
              <div style={{ marginBottom: 8 }}>
                <span style={{ marginRight: 24 }}>平均分: <strong>{examData.avgScore?.toFixed(1)}</strong></span>
                <span style={{ marginRight: 24 }}>最高分: <strong>{examData.maxScore}</strong></span>
                <span style={{ marginRight: 24 }}>及格率: <strong>{examData.passRate?.toFixed(1)}%</strong></span>
              </div>
              <ReactECharts option={distOption} style={{ height: 280 }} />
            </Card>
          </Col>
          <Col xs={24} lg={12}>
            <Card title="各题正确率">
              <ReactECharts option={questionOption} style={{ height: 280 }} />
            </Card>
          </Col>
        </Row>
      )}
    </div>
  );
}
