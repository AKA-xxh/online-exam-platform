import { useEffect, useState } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import { Card, Typography, Button, Space, Progress, message } from 'antd';
import { ArrowLeftOutlined, ArrowRightOutlined, CheckOutlined } from '@ant-design/icons';
import { getCourseDetail, reportProgress } from '@/services/api/course.api';

const { Title } = Typography;

export default function CoursePlayer() {
  const { id, lessonId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const courseTitle = (location.state as any)?.courseTitle || '课程';
  const [lesson, setLesson] = useState<any>(null);
  const [allLessons, setAllLessons] = useState<any[]>([]);
  const [currentIdx, setCurrentIdx] = useState(-1);
  const [viewedTime, setViewedTime] = useState(0);

  useEffect(() => {
    getCourseDetail(Number(id)).then(res => {
      const flat = (res.chapters || []).flatMap((ch: any) => (ch.lessons || []).map((l: any) => ({ ...l, chapterTitle: ch.title })));
      setAllLessons(flat);
      const idx = flat.findIndex((l: any) => l.id === Number(lessonId));
      setCurrentIdx(idx);
      if (idx >= 0) setLesson(flat[idx]);
    });
  }, [id, lessonId]);

  // 模拟学习时长计时器（每10秒上报一次进度）
  useEffect(() => {
    const timer = setInterval(() => {
      setViewedTime(t => {
        const nt = t + 10;
        if (lesson && id) {
          const progress = Math.min(100, Math.round(nt / ((lesson.videoDuration || 300) * 100) * 100));
          reportProgress({ lessonId: lesson.id, courseId: Number(id), progress, duration: 10 });
        }
        return nt;
      });
    }, 10000);
    return () => clearInterval(timer);
  }, [lesson, id]);

  const goNext = () => {
    if (currentIdx < allLessons.length - 1) {
      const next = allLessons[currentIdx + 1];
      navigate(`/student/courses/${id}/lessons/${next.id}`, { state: { courseTitle } });
    }
  };

  const goPrev = () => {
    if (currentIdx > 0) {
      const prev = allLessons[currentIdx - 1];
      navigate(`/student/courses/${id}/lessons/${prev.id}`, { state: { courseTitle } });
    }
  };

  const markComplete = async () => {
    if (lesson) {
      await reportProgress({ lessonId: lesson.id, courseId: Number(id), progress: 100, duration: 0 });
      message.success('已标记为完成');
      goNext();
    }
  };

  if (!lesson) return <Card loading />;

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button icon={<ArrowLeftOutlined />} onClick={() => navigate(`/student/courses/${id}`)}>返回课程</Button>
        <Title level={4} style={{ display: 'inline', marginLeft: 16 }}>{lesson.chapterTitle} - {lesson.title}</Title>
      </div>

      <Card>
        {lesson.lessonType === 1 ? (
          <div style={{ textAlign: 'center', padding: 40, background: '#000', borderRadius: 8, color: '#fff', minHeight: 400, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
            <div style={{ fontSize: 48, marginBottom: 16 }}>▶</div>
            <div>视频播放区域</div>
            <div style={{ fontSize: 12, color: '#999', marginTop: 8 }}>{lesson.videoUrl || '视频链接未配置'}</div>
          </div>
        ) : (
          <div style={{ padding: 24, minHeight: 400, background: '#fff' }}>
            <div dangerouslySetInnerHTML={{ __html: lesson.content || '<p>暂无内容</p>' }} />
          </div>
        )}

        <div style={{ marginTop: 24, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Button onClick={goPrev} disabled={currentIdx <= 0} icon={<ArrowLeftOutlined />}>上一节</Button>
          <Space>
            <Button type="primary" icon={<CheckOutlined />} onClick={markComplete}>标记完成</Button>
          </Space>
          <Button onClick={goNext} disabled={currentIdx >= allLessons.length - 1}>下一节 <ArrowRightOutlined /></Button>
        </div>
        <div style={{ marginTop: 16 }}>
          <Progress percent={Math.round(currentIdx / Math.max(allLessons.length - 1, 1) * 100)}
            format={() => `${currentIdx + 1} / ${allLessons.length}`} />
        </div>
      </Card>
    </div>
  );
}
