import { useEffect, useState } from 'react';
import { Card, Row, Col, Statistic, Typography, List } from 'antd';
import { BookOutlined, ScheduleOutlined, BugOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { useAuthStore } from '@/stores/authStore';
import { getStudentCourses } from '@/services/api/course.api';
import { getMyExams } from '@/services/api/exam.api';
import { getWrongStats } from '@/services/api/wrong.api';
import { useNavigate } from 'react-router-dom';

const { Title } = Typography;

/**
 * 学员端首页
 *
 * 只调学员端接口，不触碰管理端 API
 */
export default function StudentDashboard() {
  const userInfo = useAuthStore(s => s.userInfo);
  const navigate = useNavigate();
  const [courses, setCourses] = useState<any[]>([]);
  const [exams, setExams] = useState<any[]>([]);
  const [wrongCount, setWrongCount] = useState(0);

  useEffect(() => {
    getStudentCourses({ pageSize: 4 }).then(res => setCourses(res?.records || []));
    getMyExams().then(res => setExams(res || []));
    getWrongStats().then((res: any) => setWrongCount(res?.total || 0));
  }, []);

  // 统计
  const pendingExams = exams.filter((e: any) => e.myStatus === 0 || e.myStatus === 1).length;
  const finishedExams = exams.filter((e: any) => e.myStatus >= 2).length;

  return (
    <div>
      <Title level={4} style={{ marginBottom: 24 }}>
        欢迎回来，{userInfo?.realName || '同学'}
      </Title>

      <Row gutter={[16, 16]}>
        {[
          { title: '学习中课程', value: courses.length, icon: <BookOutlined style={{ fontSize: 32, color: '#1677ff' }} />, color: '#e6f4ff' },
          { title: '待考试', value: pendingExams, icon: <ScheduleOutlined style={{ fontSize: 32, color: '#faad14' }} />, color: '#fffbe6' },
          { title: '已完成考试', value: finishedExams, icon: <CheckCircleOutlined style={{ fontSize: 32, color: '#52c41a' }} />, color: '#f6ffed' },
          { title: '错题数', value: wrongCount, icon: <BugOutlined style={{ fontSize: 32, color: '#722ed1' }} />, color: '#f9f0ff' },
        ].map(item => (
          <Col xs={24} sm={12} lg={6} key={item.title}>
            <Card hoverable onClick={() => {
              if (item.title === '学习中课程') navigate('/student/courses');
              else if (item.title === '待考试') navigate('/student/exams');
              else if (item.title === '错题数') navigate('/student/wrong');
            }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                <div style={{ width: 64, height: 64, borderRadius: 12, background: item.color, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  {item.icon}
                </div>
                <Statistic title={item.title} value={item.value} />
              </div>
            </Card>
          </Col>
        ))}
      </Row>

      <Row gutter={[16, 16]} style={{ marginTop: 16 }}>
        <Col xs={24} lg={12}>
          <Card title="最近课程" extra={<a onClick={() => navigate('/student/courses')}>查看全部</a>}>
            {courses.length === 0 ? (
              <div style={{ color: '#999', padding: 20, textAlign: 'center' }}>暂无课程，去课程中心看看吧</div>
            ) : (
              <List dataSource={courses.slice(0, 4)} renderItem={(c: any) => (
                <List.Item extra={c.progress ? `${c.progress}%` : '未开始'}
                  style={{ cursor: 'pointer' }}
                  onClick={() => navigate(`/student/courses/${c.id}`)}>
                  {c.title}
                </List.Item>
              )} />
            )}
          </Card>
        </Col>
        <Col xs={24} lg={12}>
          <Card title="最近考试" extra={<a onClick={() => navigate('/student/exams')}>查看全部</a>}>
            {exams.length === 0 ? (
              <div style={{ color: '#999', padding: 20, textAlign: 'center' }}>暂无考试</div>
            ) : (
              <List dataSource={exams.slice(0, 4)} renderItem={(e: any) => (
                <List.Item extra={e.totalScore != null ? `${e.totalScore}分` : '待考'}
                  style={{ cursor: 'pointer' }}
                  onClick={() => navigate(`/student/exams/${e.examId}`)}>
                  {e.title}
                </List.Item>
              )} />
            )}
          </Card>
        </Col>
      </Row>
    </div>
  );
}
