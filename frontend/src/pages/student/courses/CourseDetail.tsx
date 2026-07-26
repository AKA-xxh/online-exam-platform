import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Typography, Tag, Space, Button, Progress, Collapse, List, message } from 'antd';
import { PlayCircleOutlined, FileTextOutlined, ClockCircleOutlined, CheckCircleOutlined } from '@ant-design/icons';
import { getCourseDetail } from '@/services/api/course.api';

const { Title, Paragraph } = Typography;

export default function StudentCourseDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [course, setCourse] = useState<any>({});
  const [chapters, setChapters] = useState<any[]>([]);
  const [progress, setProgress] = useState(0);

  useEffect(() => {
    if (id) {
      getCourseDetail(Number(id)).then(res => {
        setCourse(res.course || {});
        setChapters(res.chapters || []);
        const total = (res.chapters || []).flatMap((c: any) => c.lessons || []).length;
        const finished = (res.chapters || []).flatMap((c: any) => c.lessons || []).filter((l: any) => l.isFinished).length;
        setProgress(total > 0 ? Math.round(finished * 100 / total) : 0);
      });
    }
  }, [id]);

  const iconMap: any = { 1: <PlayCircleOutlined style={{ color: '#1677ff' }} />, 2: <FileTextOutlined style={{ color: '#52c41a' }} /> };

  return (
    <div>
      <Card>
        <div style={{ display: 'flex', gap: 24 }}>
          <div style={{ width: 320, height: 180, background: '#f0f5ff', borderRadius: 8, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <PlayCircleOutlined style={{ fontSize: 64, color: '#1677ff' }} />
          </div>
          <div style={{ flex: 1 }}>
            <Title level={4}>{course.title}</Title>
            <Paragraph type="secondary">{course.description}</Paragraph>
            <Space size="large">
              <span>讲师：{course.teacherName || '暂无'}</span>
              <span><ClockCircleOutlined /> {Math.floor((course.totalDuration || 0) / 60)}分钟</span>
              <span>共{course.lessonCount}课时</span>
            </Space>
            <div style={{ marginTop: 16 }}>
              <span>学习进度：</span>
              <Progress percent={progress} style={{ width: 200 }} />
            </div>
          </div>
        </div>
      </Card>

      <Card title="课程目录" style={{ marginTop: 16 }}>
        {chapters.map((ch: any, ci: number) => (
          <div key={ch.id || ci} style={{ marginBottom: 16 }}>
            <Title level={5}>{`第${ci + 1}章 ${ch.title}`}</Title>
            <List dataSource={ch.lessons || []} renderItem={(ls: any) => (
              <List.Item extra={ls.isFinished ? <Tag color="success" icon={<CheckCircleOutlined />}>已完成</Tag> : null}
                style={{ cursor: 'pointer', padding: '12px 16px', borderRadius: 6, background: ls.isFinished ? '#f6ffed' : '#fafafa', marginBottom: 4 }}
                onClick={() => navigate(`/student/courses/${id}/lessons/${ls.id}`, { state: { courseTitle: course.title } })}
              >
                <Space>
                  {iconMap[ls.lessonType] || <FileTextOutlined />}
                  <span>{ls.title}</span>
                  {ls.isFree === 1 && <Tag color="orange">免费</Tag>}
                </Space>
              </List.Item>
            )} />
          </div>
        ))}
      </Card>
    </div>
  );
}
