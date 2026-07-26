import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Card, Form, Input, Select, Button, Space, message, InputNumber } from 'antd';
import { PlusOutlined, DeleteOutlined } from '@ant-design/icons';
import { getQuestionDetail, createQuestion, updateQuestion, getQuestionCategories } from '@/services/api/question.api';

export default function QuestionEdit() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isNew = !id;
  const [form] = Form.useForm();
  const [categories, setCategories] = useState<any[]>([]);
  const [questionType, setQuestionType] = useState(1);
  const [options, setOptions] = useState<any[]>([]);

  useEffect(() => { getQuestionCategories().then(res => setCategories(res || [])); }, []);
  useEffect(() => {
    if (!isNew) {
      getQuestionDetail(Number(id)).then(res => {
        const q = res.question;
        form.setFieldsValue(q);
        setQuestionType(q.questionType);
        setOptions(res.options || []);
      });
    }
  }, [id]);

  const onSave = async () => {
    const values = await form.validateFields();
    const data: any = { ...values, options: options.map(o => ({ text: o.optionText || o.text || '', isCorrect: o.isCorrect === 1 || o.isCorrect === true })) };
    if (!isNew) await updateQuestion(Number(id), data);
    else await createQuestion(data);
    message.success(isNew ? '创建成功' : '保存成功');
    navigate('/admin/questions');
  };

  const addOption = () => setOptions([...options, { optionLabel: String.fromCharCode(65 + options.length), optionText: '', isCorrect: 0 }]);
  const removeOption = (idx: number) => setOptions(options.filter((_, i) => i !== idx));
  const updateOption = (idx: number, field: string, value: any) => {
    const updated = [...options];
    updated[idx] = { ...updated[idx], [field]: value };
    setOptions(updated);
  };

  const toggleCorrect = (idx: number) => {
    if (questionType === 1) { // 单选：只有一个正确答案
      setOptions(options.map((o, i) => ({ ...o, isCorrect: i === idx ? 1 : 0 })));
    } else if (questionType === 2) { // 多选：切换
      const updated = [...options];
      updated[idx] = { ...updated[idx], isCorrect: updated[idx].isCorrect === 1 ? 0 : 1 };
      setOptions(updated);
    }
  };

  return (
    <Card title={isNew ? '新增题目' : '编辑题目'} extra={<Space>
      <Button onClick={() => navigate('/admin/questions')}>取消</Button>
      <Button type="primary" onClick={onSave}>保存</Button>
    </Space>}>
      <Form form={form} layout="vertical" style={{ maxWidth: 700 }}>
        <Form.Item name="questionType" label="题型" rules={[{ required: true }]}>
          <Select options={[{ value: 1, label: '单选题' }, { value: 2, label: '多选题' }, { value: 3, label: '判断题' }, { value: 4, label: '简答题' }]}
            onChange={v => { setQuestionType(v); if (v === 3) setOptions([{ optionLabel: 'A', optionText: '正确', isCorrect: 1 }, { optionLabel: 'B', optionText: '错误', isCorrect: 0 }]); else setOptions([]); }} />
        </Form.Item>
        <Form.Item name="categoryId" label="分类" rules={[{ required: true }]}>
          <Select options={categories.map((c: any) => ({ value: c.id, label: c.name }))} /></Form.Item>
        <Form.Item name="difficulty" label="难度" rules={[{ required: true }]}>
          <Select options={[{ value: 1, label: '简单' }, { value: 2, label: '中等' }, { value: 3, label: '困难' }]} /></Form.Item>
        <Form.Item name="title" label="题干" rules={[{ required: true, message: '请输入题干' }]}>
          <Input.TextArea rows={4} placeholder="输入题目内容..." /></Form.Item>

        {/* 选择题选项编辑 */}
        {[1, 2].includes(questionType) && (
          <div style={{ marginBottom: 24 }}>
            <div style={{ fontWeight: 500, marginBottom: 8 }}>选项设置（点击选项可标记为正确答案）</div>
            {options.map((opt, idx) => (
              <div key={idx} style={{ display: 'flex', gap: 8, marginBottom: 8, alignItems: 'center' }}>
                <Button type={opt.isCorrect === 1 ? 'primary' : 'default'}
                  style={{ minWidth: 40 }} onClick={() => toggleCorrect(idx)}>
                  {String.fromCharCode(65 + idx)}
                </Button>
                <Input value={opt.optionText || opt.text || ''} placeholder={`选项${String.fromCharCode(65 + idx)}内容`}
                  onChange={e => updateOption(idx, 'optionText', e.target.value)} style={{ flex: 1 }} />
                <Button danger icon={<DeleteOutlined />} onClick={() => removeOption(idx)} disabled={questionType === 3} />
              </div>
            ))}
            {questionType !== 3 && <Button type="dashed" icon={<PlusOutlined />} onClick={addOption}>添加选项</Button>}
          </div>
        )}

        {/* 判断题 */}
        {questionType === 3 && (
          <Form.Item name="correctAnswer" label="正确答案">
            <Select options={[{ value: 1, label: '正确 (A)' }, { value: 0, label: '错误 (B)' }]} />
          </Form.Item>
        )}

        {/* 简答题 */}
        {questionType === 4 && (
          <Form.Item name="referenceAnswer" label="参考答案">
            <Input.TextArea rows={4} placeholder="输入参考答案和评分要点..." />
          </Form.Item>
        )}

        <Form.Item name="analysis" label="题目解析"><Input.TextArea rows={3} placeholder="输入答案解析..." /></Form.Item>
        <Form.Item name="tags" label="标签"><Input placeholder="逗号分隔多个标签" /></Form.Item>
      </Form>
    </Card>
  );
}
