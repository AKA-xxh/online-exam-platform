import { useEffect, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { Card, Form, Input, InputNumber, Select, Button, Space, message, Table, Popconfirm, Divider } from 'antd';
import { PlusOutlined, DeleteOutlined, ReloadOutlined } from '@ant-design/icons';
import { getPaperDetail, createPaper, updatePaper, regeneratePaper } from '@/services/api/paper.api';
import { getQuestions, getQuestionCategories } from '@/services/api/question.api';

export default function PaperEdit() {
  const { id } = useParams();
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const isNew = !id;
  const paperType = searchParams.get('type') === 'random' ? 2 : 1;
  const [form] = Form.useForm();
  const [pqList, setPqList] = useState<any[]>([]);
  const [rules, setRules] = useState<any[]>([]);
  const [showQuestionPicker, setShowQuestionPicker] = useState(false);
  const [availableQuestions, setAvailableQuestions] = useState<any[]>([]);
  const [categories, setCategories] = useState<any[]>([]);

  useEffect(() => { getQuestionCategories().then(res => setCategories(res || [])); }, []);
  useEffect(() => {
    if (!isNew) {
      getPaperDetail(Number(id)).then(res => {
        form.setFieldsValue(res.paper);
        if (res.paper?.paperType === 1) setPqList(res.questions || []);
        else if (res.paper?.genRules) setRules(JSON.parse(res.paper.genRules));
      });
    } else { form.setFieldsValue({ paperType, totalScore: 100, passScore: 60, duration: 60, showAnswer: 1, shuffleQuestion: 0, shuffleOption: 0, maxScreenSwitches: 3 }); }
  }, [id]);

  const loadQuestions = async () => {
    const res = await getQuestions({ page: 1, pageSize: 200 });
    setAvailableQuestions(res.records || []);
    setShowQuestionPicker(true);
  };

  const addQuestionToPaper = (q: any) => {
    setPqList([...pqList, { questionId: q.id, title: q.title, questionType: q.questionType, typeName: q.typeName, score: 2, sectionName: '' }]);
  };

  const removeFromPaper = (idx: number) => setPqList(pqList.filter((_, i) => i !== idx));
  const updateScore = (idx: number, score: number) => {
    const updated = [...pqList]; updated[idx].score = score; setPqList(updated);
  };

  const addRule = () => setRules([...rules, { questionType: 1, count: 5, scorePerQuestion: 2, difficulty: undefined as any, categoryId: undefined as any, sectionName: '' }]);
  const removeRule = (idx: number) => setRules(rules.filter((_, i) => i !== idx));
  const updateRule = (idx: number, field: string, value: any) => { const u = [...rules]; u[idx][field] = value; setRules(u); };

  const onSave = async () => {
    const values = await form.validateFields();
    const data: any = { ...values };
    if (paperType === 1) data.questions = pqList;
    else data.genRules = rules;
    if (!isNew) await updatePaper(Number(id), data);
    else await createPaper(data);
    message.success(isNew ? '创建成功' : '保存成功');
    navigate('/admin/papers');
  };

  const onRegenerate = async () => {
    const res = await regeneratePaper(Number(id));
    message.success('已重新生成');
    setPqList(res.questions || []);
  };

  const totalScore = pqList.reduce((sum: number, pq: any) => sum + (pq.score || 0), 0);

  return (
    <Card title={isNew ? (paperType === 1 ? '手动组卷' : '随机组卷') : '编辑试卷'}
      extra={<Space><Button onClick={() => navigate('/admin/papers')}>取消</Button><Button type="primary" onClick={onSave}>保存</Button></Space>}>
      <Form form={form} layout="vertical" style={{ maxWidth: 600 }}>
        <Form.Item name="title" label="试卷名称" rules={[{ required: true }]}><Input /></Form.Item>
        <Form.Item name="description" label="试卷说明"><Input.TextArea rows={2} /></Form.Item>
        <Space size="large">
          <Form.Item name="totalScore" label="总分"><InputNumber min={1} max={500} /></Form.Item>
          <Form.Item name="passScore" label="及格分"><InputNumber min={1} max={500} /></Form.Item>
          <Form.Item name="duration" label="考试时长(分钟)"><InputNumber min={1} max={480} /></Form.Item>
        </Space>
        <Space size="large">
          <Form.Item name="showAnswer" label="答案展示"><Select options={[{ value: 0, label: '不展示' }, { value: 1, label: '交卷后' }, { value: 2, label: '结束后' }]} /></Form.Item>
          <Form.Item name="shuffleQuestion" label="题目随机排序"><Select options={[{ value: 0, label: '否' }, { value: 1, label: '是' }]} /></Form.Item>
          <Form.Item name="shuffleOption" label="选项随机排序"><Select options={[{ value: 0, label: '否' }, { value: 1, label: '是' }]} /></Form.Item>
          <Form.Item name="maxScreenSwitches" label="切屏限制"><InputNumber min={0} max={10} /></Form.Item>
        </Space>
      </Form>

      <Divider />

      {paperType === 1 ? (
        <div>
          <Space style={{ marginBottom: 16 }}>
            <Button type="primary" icon={<PlusOutlined />} onClick={loadQuestions}>从题库选题</Button>
            <span>当前 {pqList.length} 题，总分 {totalScore}</span>
          </Space>
          <Table dataSource={pqList} rowKey={(_, i) => String(i)} pagination={false} size="small"
            columns={[
              { title: '#', render: (_, __, i) => i + 1, width: 40 },
              { title: '题干', dataIndex: 'title', ellipsis: true, render: (t: string, r: any) => r.titleSnapshot || t || '' },
              { title: '题型', dataIndex: 'typeName', width: 80 },
              { title: '分值', dataIndex: 'score', width: 100, render: (s: number, _, i: number) => <InputNumber min={1} value={s} onChange={v => updateScore(i, v || 0)} size="small" /> },
              { title: '操作', width: 60, render: (_, __, i) => <Button danger size="small" icon={<DeleteOutlined />} onClick={() => removeFromPaper(i)} /> },
            ]}
          />
        </div>
      ) : (
        <div>
          <Space style={{ marginBottom: 16 }}>
            <Button type="primary" icon={<PlusOutlined />} onClick={addRule}>添加出题规则</Button>
            {!isNew && <Button icon={<ReloadOutlined />} onClick={onRegenerate}>重新生成题目</Button>}
          </Space>
          {rules.map((rule, idx) => (
            <Card key={idx} size="small" style={{ marginBottom: 8 }}>
              <Space wrap>
                <Select value={rule.questionType} style={{ width: 100 }} onChange={v => updateRule(idx, 'questionType', v)}
                  options={[{ value: 1, label: '单选题' }, { value: 2, label: '多选题' }, { value: 3, label: '判断题' }, { value: 4, label: '简答题' }]} />
                <Select placeholder="分类" allowClear style={{ width: 120 }} value={rule.categoryId} onChange={v => updateRule(idx, 'categoryId', v)}
                  options={categories.map((c: any) => ({ value: c.id, label: c.name }))} />
                <Select placeholder="难度" allowClear style={{ width: 100 }} value={rule.difficulty} onChange={v => updateRule(idx, 'difficulty', v)}
                  options={[{ value: 1, label: '简单' }, { value: 2, label: '中等' }, { value: 3, label: '困难' }]} />
                <span>抽取 <InputNumber min={1} value={rule.count} onChange={v => updateRule(idx, 'count', v)} style={{ width: 60 }} /> 题</span>
                <span>每题 <InputNumber min={1} value={rule.scorePerQuestion} onChange={v => updateRule(idx, 'scorePerQuestion', v)} style={{ width: 60 }} /> 分</span>
                <Input placeholder="大题名称" value={rule.sectionName} onChange={e => updateRule(idx, 'sectionName', e.target.value)} style={{ width: 120 }} />
                <Button danger icon={<DeleteOutlined />} onClick={() => removeRule(idx)} />
              </Space>
            </Card>
          ))}
          {!isNew && pqList.length > 0 && (
            <Table dataSource={pqList} rowKey="id" pagination={false} size="small" style={{ marginTop: 16 }}
              columns={[
                { title: '#', render: (_, __, i) => i + 1, width: 40 },
                { title: '题干', dataIndex: 'titleSnapshot', ellipsis: true },
                { title: '题型', dataIndex: 'questionType', width: 80, render: (t: number) => ({ 1: '单选', 2: '多选', 3: '判断', 4: '简答' })[t] },
                { title: '分值', dataIndex: 'score', width: 60 },
              ]}
            />
          )}
        </div>
      )}

      {/* 选题弹窗 */}
      {showQuestionPicker && (
        <Card title="从题库选题" style={{ marginTop: 16 }}
          extra={<Button onClick={() => setShowQuestionPicker(false)}>关闭</Button>}>
          {availableQuestions.map((q: any) => (
            <div key={q.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '8px 0', borderBottom: '1px solid #f0f0f0' }}>
              <span>[{q.typeName}] {q.title}</span>
              <Button size="small" type="primary" onClick={() => { addQuestionToPaper(q); }}>选择</Button>
            </div>
          ))}
        </Card>
      )}
    </Card>
  );
}
