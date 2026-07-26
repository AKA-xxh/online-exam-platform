import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Form, Input, Select, Button, Tabs, Space, Modal, message, Table, Popconfirm } from 'antd';
import { PlusOutlined, DeleteOutlined, EditOutlined, ArrowUpOutlined, ArrowDownOutlined } from '@ant-design/icons';
import { getCourseDetail, createCourse, updateCourse, getCategories, createChapter, updateChapter, deleteChapter, createLesson, updateLesson, deleteLesson } from '@/services/api/course.api';

export default function CourseEdit() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isNew = !id;
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [chapters, setChapters] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);
  const [chapterModal, setChapterModal] = useState({ open: false, chapter: null as any });
  const [lessonModal, setLessonModal] = useState({ open: false, lesson: null as any, chapterId: 0 });
  const [chapterForm] = Form.useForm();
  const [lessonForm] = Form.useForm();

  useEffect(() => {
    getCategories().then(res => setCategories(flatten(res || [])));
    if (!isNew) {
      getCourseDetail(Number(id)).then(res => {
        form.setFieldsValue(res.course);
        setChapters(res.chapters || []);
      });
    }
  }, [id]);

  const flatten = (list: any[], p = ''): any[] => {
    let r: any[] = [];
    list.forEach((i: any) => { r.push({ ...i, label: p + i.name }); if (i.children) r = r.concat(flatten(i.children, p + '--')); });
    return r;
  };

  const onSave = async () => {
    const values = await form.validateFields();
    setLoading(true);
    try {
      if (isNew) {
        const res = await createCourse(values);
        message.success('创建成功');
        navigate(`/admin/courses/${res.id}`, { replace: true });
      } else {
        await updateCourse(Number(id), values);
        message.success('保存成功');
      }
    } finally { setLoading(false); }
  };

  // 章节操作
  const openChapterModal = (ch: any = null) => { setChapterModal({ open: true, chapter: ch }); chapterForm.resetFields(); if (ch) chapterForm.setFieldsValue(ch); };
  const saveChapter = async () => {
    const v = await chapterForm.validateFields();
    if (chapterModal.chapter) await updateChapter(chapterModal.chapter.id, v);
    else await createChapter(Number(id), v);
    setChapterModal({ open: false, chapter: null });
    refreshChapters();
  };
  const removeChapter = async (chId: number) => { await deleteChapter(chId); refreshChapters(); };

  // 课时操作
  const openLessonModal = (chapterId: number, ls: any = null) => { setLessonModal({ open: true, lesson: ls, chapterId }); lessonForm.resetFields(); if (ls) lessonForm.setFieldsValue(ls); };
  const saveLesson = async () => {
    const v = await lessonForm.validateFields();
    if (lessonModal.lesson) await updateLesson(lessonModal.lesson.id, v);
    else await createLesson(lessonModal.chapterId, Number(id), v);
    setLessonModal({ open: false, lesson: null, chapterId: 0 });
    refreshChapters();
  };
  const removeLesson = async (lsId: number) => { await deleteLesson(lsId); refreshChapters(); };

  const refreshChapters = async () => {
    const res = await getCourseDetail(Number(id));
    setChapters(res.chapters || []);
  };

  const chapterColumns = [
    { title: '课时标题', dataIndex: 'title' },
    { title: '类型', dataIndex: 'lessonType', width: 80, render: (t: number) => ({ 1: '视频', 2: '图文' })[t] || '未知' },
    { title: '免费', dataIndex: 'isFree', width: 60, render: (v: number) => v === 1 ? '是' : '否' },
    { title: '操作', width: 120,
      render: (_: any, r: any, idx: number) => (
        <Space>
          <Button size="small" icon={<EditOutlined />} onClick={() => openLessonModal(r.chapterId || 0, r)} />
          <Popconfirm title="确认删除？" onConfirm={() => removeLesson(r.id)}>
            <Button size="small" danger icon={<DeleteOutlined />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  return (
    <Card title={isNew ? '新建课程' : '编辑课程'} extra={<Space>
      <Button onClick={() => navigate('/admin/courses')}>取消</Button>
      <Button type="primary" loading={loading} onClick={onSave}>保存</Button>
    </Space>}>
      <Tabs items={[
        { key: 'info', label: '基本信息', children: (
          <Form form={form} layout="vertical" style={{ maxWidth: 600 }}>
            <Form.Item name="title" label="课程标题" rules={[{ required: true }]}><Input /></Form.Item>
            <Form.Item name="subtitle" label="副标题"><Input /></Form.Item>
            <Form.Item name="categoryId" label="课程分类" rules={[{ required: true }]}>
              <Select options={categories.map(c => ({ value: c.id, label: c.label }))} /></Form.Item>
            <Form.Item name="teacherName" label="讲师"><Input /></Form.Item>
            <Form.Item name="teacherIntro" label="讲师介绍"><Input.TextArea rows={2} /></Form.Item>
            <Form.Item name="coverUrl" label="封面图URL"><Input placeholder="https://..." /></Form.Item>
            <Form.Item name="description" label="课程简介"><Input.TextArea rows={4} /></Form.Item>
          </Form>
        )},
        { key: 'content', label: '章节课时', disabled: isNew, children: (
          <div>
            {chapters.map((ch: any, ci: number) => (
              <Card key={ch.id || ci} size="small" style={{ marginBottom: 16 }}
                title={<Space><span>{`第${ci + 1}章: ${ch.title}`}</span>
                  <Button size="small" icon={<EditOutlined />} onClick={() => openChapterModal(ch)} />
                  <Popconfirm title="删除章节及所有课时？" onConfirm={() => removeChapter(ch.id)}>
                    <Button size="small" danger icon={<DeleteOutlined />} />
                  </Popconfirm>
                </Space>}
                extra={<Button size="small" type="primary" icon={<PlusOutlined />} onClick={() => openLessonModal(ch.id)}>添加课时</Button>}
              >
                <Table columns={chapterColumns} dataSource={(ch.lessons || []).map((l: any) => ({ ...l, chapterId: ch.id }))} rowKey="id" pagination={false} size="small" />
              </Card>
            ))}
            <Button type="dashed" block icon={<PlusOutlined />} onClick={() => openChapterModal()}>添加章节</Button>
          </div>
        )},
      ]} />

      {/* 章节弹窗 */}
      <Modal title={chapterModal.chapter ? '编辑章节' : '新增章节'} open={chapterModal.open}
        onOk={saveChapter} onCancel={() => setChapterModal({ open: false, chapter: null })}>
        <Form form={chapterForm} layout="vertical">
          <Form.Item name="title" label="章节标题" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="sortOrder" label="排序"><Input type="number" /></Form.Item>
        </Form>
      </Modal>

      {/* 课时弹窗 */}
      <Modal title={lessonModal.lesson ? '编辑课时' : '新增课时'} open={lessonModal.open} width={600}
        onOk={saveLesson} onCancel={() => setLessonModal({ open: false, lesson: null, chapterId: 0 })}>
        <Form form={lessonForm} layout="vertical">
          <Form.Item name="title" label="课时标题" rules={[{ required: true }]}><Input /></Form.Item>
          <Form.Item name="lessonType" label="类型" rules={[{ required: true }]}>
            <Select options={[{ value: 1, label: '视频' }, { value: 2, label: '图文' }]} /></Form.Item>
          <Form.Item name="videoUrl" label="视频URL"><Input placeholder="https://..." /></Form.Item>
          <Form.Item name="videoDuration" label="视频时长(秒)"><Input type="number" /></Form.Item>
          <Form.Item name="content" label="图文内容"><Input.TextArea rows={6} /></Form.Item>
          <Form.Item name="isFree" label="是否免费试看"><Select options={[{ value: 0, label: '否' }, { value: 1, label: '是' }]} /></Form.Item>
          <Form.Item name="sortOrder" label="排序"><Input type="number" /></Form.Item>
        </Form>
      </Modal>
    </Card>
  );
}
