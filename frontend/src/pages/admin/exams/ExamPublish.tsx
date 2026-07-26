import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Card, Form, Input, InputNumber, Select, DatePicker, Button, message } from 'antd';
import { publishExam } from '@/services/api/exam.api';
import { getPapers } from '@/services/api/paper.api';
import { getUsers } from '@/services/api/user.api';

export default function AdminExamPublish() {
  const navigate = useNavigate();
  const [form] = Form.useForm();
  const [loading, setLoading] = useState(false);
  const [papers, setPapers] = useState<any[]>([]);
  const [students, setStudents] = useState<any[]>([]);
  const scope = Form.useWatch('studentScope', form);

  useEffect(() => {
    getUsers({ userType: 1, pageSize: 500 }).then((res: any) =>
      setStudents((res.records || []).map((u: any) => ({ value: u.id, label: `${u.realName} (${u.username})` }))));
  }, []);

  const loadPapers = async (keyword?: string) => {
    const res = await getPapers({ keyword, pageSize: 50 });
    setPapers((res.records || []).map((p: any) => ({ value: p.id, label: `${p.title} (满分${p.totalScore}分, ${p.duration}分钟)` })));
  };

  const onFinish = async (values: any) => {
    setLoading(true);
    try {
      const data = {
        ...values,
        startTime: values.timeRange?.[0]?.format('YYYY-MM-DD HH:mm:ss'),
        endTime: values.timeRange?.[1]?.format('YYYY-MM-DD HH:mm:ss'),
        // 指定学员时传学员 ID 列表
        studentIds: values.studentScope === 2 ? values.studentIds : undefined,
      };
      delete data.timeRange;
      await publishExam(data);
      message.success('考试已发布');
      navigate('/admin/exams');
    } finally { setLoading(false); }
  };

  return (
    <Card title="发布考试" extra={<Button onClick={() => navigate('/admin/exams')}>返回</Button>}>
      <Form form={form} layout="vertical" style={{ maxWidth: 600 }} onFinish={onFinish}
        initialValues={{ studentScope: 1, passScore: 60 }}>
        <Form.Item name="paperId" label="选择试卷" rules={[{ required: true }]}>
          <Select placeholder="搜索并选择试卷" showSearch onSearch={loadPapers} onFocus={() => loadPapers()} filterOption={false} options={papers} />
        </Form.Item>
        <Form.Item name="title" label="考试名称" rules={[{ required: true }]}><Input placeholder="如：2024年春季期末考试" /></Form.Item>
        <Form.Item name="description" label="考试须知"><Input.TextArea rows={3} placeholder="考试注意事项..." /></Form.Item>
        <Form.Item name="timeRange" label="考试时间范围" rules={[{ required: true }]}>
          <DatePicker.RangePicker showTime format="YYYY-MM-DD HH:mm" style={{ width: '100%' }} />
        </Form.Item>
        <Form.Item name="duration" label="考试时长（分钟）" rules={[{ required: true }]}>
          <InputNumber min={1} max={480} style={{ width: '100%' }} placeholder="从进入考试开始计时" />
        </Form.Item>
        <Form.Item name="passScore" label="及格分数线"><InputNumber min={0} max={500} style={{ width: '100%' }} /></Form.Item>
        <Form.Item name="studentScope" label="考生范围">
          <Select options={[{ value: 1, label: '全部学员' }, { value: 2, label: '指定学员' }]} />
        </Form.Item>
        {scope === 2 && (
          <Form.Item name="studentIds" label="选择学员" rules={[{ required: true, message: '请选择至少一名学员' }]}>
            <Select mode="multiple" placeholder="搜索并选择学员" showSearch filterOption={(input, option) =>
              (option?.label as string || '').includes(input)} options={students}
              style={{ width: '100%' }} />
          </Form.Item>
        )}
        <Form.Item>
          <Button type="primary" htmlType="submit" loading={loading}>发布考试</Button>
        </Form.Item>
      </Form>
    </Card>
  );
}
