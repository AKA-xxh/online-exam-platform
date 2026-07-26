import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Row, Col, Input, Select, Tag, Typography, Image, Rate } from 'antd';
import { SearchOutlined, PlayCircleOutlined, ClockCircleOutlined, BookOutlined } from '@ant-design/icons';
import { getStudentCourses, getCategories } from '@/services/api/course.api';

const { Meta } = Card;
const { Title } = Typography;

export default function StudentCourseList() {
  const navigate = useNavigate();
  const [courses, setCourses] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [params, setParams] = useState({ page: 1, pageSize: 12, keyword: '', categoryId: undefined as any });

  useEffect(() => { getCategories().then(res => setCategories(res || [])); }, []);
  useEffect(() => { getStudentCourses(params).then(res => setCourses(res.records || [])); }, [params]);

  return (
    <div>
      <Title level={4}>课程广场</Title>
      <div style={{ marginBottom: 24, display: 'flex', gap: 12 }}>
        <Input.Search placeholder="搜索课程" allowClear style={{ maxWidth: 300 }}
          onSearch={v => setParams({ ...params, keyword: v, page: 1 })} />
        <Select placeholder="选择分类" allowClear style={{ width: 160 }}
          value={params.categoryId} onChange={v => setParams({ ...params, categoryId: v, page: 1 })}
          options={categories.map((c: any) => ({ value: c.id, label: c.name }))} />
      </div>
      <Row gutter={[16, 16]}>
        {courses.map((c: any) => (
          <Col key={c.id} xs={24} sm={12} md={8} lg={6}>
            <Card hoverable cover={
              <div style={{ height: 140, background: '#f0f5ff', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                <PlayCircleOutlined style={{ fontSize: 48, color: '#1677ff' }} />
              </div>}
              onClick={() => navigate(`/student/courses/${c.id}`)}
            >
              <Meta title={c.title}
                description={<div>
                  <div style={{ marginBottom: 8 }}>{c.teacherName || '暂无讲师'}</div>
                  <div style={{ display: 'flex', gap: 12, color: '#8c8c8c', fontSize: 12 }}>
                    <span><BookOutlined /> {c.lessonCount}课时</span>
                    <span><ClockCircleOutlined /> {Math.floor((c.totalDuration || 0) / 60)}分钟</span>
                  </div>
                  {c.progress > 0 && <div style={{ marginTop: 8, fontSize: 12, color: '#1677ff' }}>已学{c.progress}%</div>}
                </div>}
              />
            </Card>
          </Col>
        ))}
      </Row>
    </div>
  );
}
