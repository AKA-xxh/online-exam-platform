import { useEffect, useState, useRef, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Button, message, Modal, Progress, Space, Tag, Radio, Checkbox, Input, Card } from 'antd';
import { ClockCircleOutlined, FlagOutlined } from '@ant-design/icons';
import { startExam, saveAnswer, submitExam } from '@/services/api/exam.api';
import { useExamStore } from '@/stores/examStore';

export default function StudentExamPaper() {
  const { id: examId } = useParams();
  const navigate = useNavigate();
  const store = useExamStore();
  const [timeLeft, setTimeLeft] = useState(0);
  const [loading, setLoading] = useState(true);
  const [warning, setWarning] = useState(0);
  const submittingRef = useRef(false);
  const initializedRef = useRef(false); // 防止 StrictMode 双调
  const timerRef = useRef<any>(null);
  const saveTimerRef = useRef<any>(null);

  // 防作弊 handler（组件级别，方便 submit 时移除）
  const handleVisibility = useCallback(() => {
    if (submittingRef.current) return; // 正在交卷，忽略
    if (document.hidden || !document.fullscreenElement) {
      setWarning(w => {
        const nw = w + 1;
        if (nw >= 3) { message.error('切屏次数已达上限，即将交卷'); setTimeout(doSubmit, 1000); }
        else message.warning(`警告：已检测到 ${nw} 次切屏行为（超过3次将自动交卷）`);
        return nw;
      });
    }
  }, []);

  // 初始化考试（防 StrictMode 双调）
  useEffect(() => {
    if (!examId || initializedRef.current) return;
    initializedRef.current = true;
    startExam(Number(examId)).then(res => {
      store.init(res.examStudentId, res.duration, res.startTime, res.questions || []);
      setTimeLeft(res.duration * 60);
      setLoading(false);
      // 延迟到下一帧请求全屏，确保 DOM 已渲染
      requestAnimationFrame(() => enterFullscreen());
    }).catch((err: any) => {
      initializedRef.current = false;
      const msg = err?.response?.data?.message || err?.message || '进入考试失败';
      message.error(msg);
      navigate('/student/exams');
    });
    return () => { clearInterval(timerRef.current); clearInterval(saveTimerRef.current); };
  }, [examId]);

  // 倒计时
  useEffect(() => {
    if (timeLeft <= 0) return;
    timerRef.current = setInterval(() => {
      setTimeLeft(t => {
        if (t <= 1) { clearInterval(timerRef.current); doSubmit(); return 0; }
        return t - 1;
      });
    }, 1000);
    return () => clearInterval(timerRef.current);
  }, [timeLeft > 0]);

  // 定时保存 + 防作弊监听
  useEffect(() => {
    saveTimerRef.current = setInterval(autoSave, 30000);
    document.addEventListener('visibilitychange', handleVisibility);
    document.addEventListener('fullscreenchange', handleVisibility);
    document.addEventListener('contextmenu', e => e.preventDefault());
    document.addEventListener('copy', e => e.preventDefault());
    document.addEventListener('paste', e => e.preventDefault());
    return () => {
      clearInterval(saveTimerRef.current);
      document.removeEventListener('visibilitychange', handleVisibility);
      document.removeEventListener('fullscreenchange', handleVisibility);
      document.removeEventListener('contextmenu', e => e.preventDefault());
      document.removeEventListener('copy', e => e.preventDefault());
      document.removeEventListener('paste', e => e.preventDefault());
    };
  }, [handleVisibility]);

  const enterFullscreen = () => document.documentElement.requestFullscreen?.().catch(() => {});
  const exitFullscreen = () => { if (document.fullscreenElement) document.exitFullscreen?.().catch(() => {}); };

  const autoSave = useCallback(() => {
    const { answers, examStudentId } = useExamStore.getState();
    Object.entries(answers).forEach(([pqId, ans]) => {
      saveAnswer({ examStudentId, paperQuestionId: Number(pqId), answer: ans }).catch(() => {});
    });
  }, []);

  const doSubmit = async () => {
    submittingRef.current = true;
    clearInterval(timerRef.current);
    clearInterval(saveTimerRef.current);
    exitFullscreen();
    try {
      const res = await submitExam(store.examStudentId);
      message.success('交卷成功');
      navigate(`/student/exams/${examId}/result`, { state: { result: res } });
    } catch { message.error('交卷失败'); }
  };

  const handleAnswer = (paperQuestionId: number, answer: any) => {
    store.setAnswer(paperQuestionId, JSON.stringify(answer));
  };

  const renderQuestion = (q: any, idx: number) => {
    const rawAns = store.answers[q.paperQuestionId];
    const parsedAns = rawAns ? JSON.parse(rawAns) : null;

    return (
      <Card key={q.paperQuestionId} style={{ marginBottom: 16 }}
        title={<Space><Tag color="blue">{['', '单选', '多选', '判断', '简答'][q.questionType]}</Tag> 第{idx + 1}题 ({q.score}分)</Space>}
        extra={<Button type={store.marked.has(q.paperQuestionId) ? 'primary' : 'text'} icon={<FlagOutlined />} onClick={() => store.toggleMark(q.paperQuestionId)}>
          {store.marked.has(q.paperQuestionId) ? '已标记' : '标记'}
        </Button>}
      >
        <div style={{ marginBottom: 16, fontWeight: 500 }} dangerouslySetInnerHTML={{ __html: q.title }} />
        {q.questionType === 1 && (
          <Radio.Group value={parsedAns?.[0]} onChange={e => handleAnswer(q.paperQuestionId, [e.target.value])}>
            <Space direction="vertical">
              {(q.options || []).map((opt: any) => (
                <Radio key={opt.optionLabel} value={opt.optionLabel}><strong>{opt.optionLabel}.</strong> {opt.optionText || opt.text || ''}</Radio>
              ))}
            </Space>
          </Radio.Group>
        )}
        {q.questionType === 2 && (
          <Checkbox.Group value={parsedAns || []} onChange={vals => handleAnswer(q.paperQuestionId, vals)}>
            <Space direction="vertical">
              {(q.options || []).map((opt: any) => (
                <Checkbox key={opt.optionLabel} value={opt.optionLabel}><strong>{opt.optionLabel}.</strong> {opt.optionText || opt.text || ''}</Checkbox>
              ))}
            </Space>
          </Checkbox.Group>
        )}
        {q.questionType === 3 && (
          <Radio.Group value={parsedAns?.[0]} onChange={e => handleAnswer(q.paperQuestionId, [e.target.value])}>
            <Space><Radio value="A">正确</Radio><Radio value="B">错误</Radio></Space>
          </Radio.Group>
        )}
        {q.questionType === 4 && (
          <Input.TextArea rows={6} value={parsedAns?.[0] || ''} onChange={e => handleAnswer(q.paperQuestionId, [e.target.value])} placeholder="请输入答案..." />
        )}
      </Card>
    );
  };

  const formatTime = (s: number) => {
    const h = Math.floor(s / 3600), m = Math.floor((s % 3600) / 60), ss = s % 60;
    return `${h > 0 ? h + ':' : ''}${String(m).padStart(2, '0')}:${String(ss).padStart(2, '0')}`;
  };

  const answeredCount = Object.keys(store.answers).length;
  const progressPercent = Math.round(answeredCount / Math.max(store.questions.length, 1) * 100);

  if (loading) return <div style={{ padding: 40, textAlign: 'center' }}>加载试卷中...</div>;

  return (
    <div style={{ maxWidth: 900, margin: '0 auto', padding: '0 16px' }}>
      <Card size="small" style={{ position: 'sticky', top: 0, zIndex: 100, marginBottom: 16, background: '#fff' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Space>
            <ClockCircleOutlined style={{ color: timeLeft < 300 ? '#ff4d4f' : '#1677ff' }} />
            <span style={{ fontSize: 18, fontWeight: 'bold', color: timeLeft < 300 ? '#ff4d4f' : '#333' }}>{formatTime(timeLeft)}</span>
          </Space>
          <Progress percent={progressPercent} style={{ width: 200 }} format={() => `${answeredCount}/${store.questions.length}`} />
          <Space>
            {warning > 0 && <Tag color="orange">切屏警告: {warning}次</Tag>}
            <Button type="primary" danger onClick={() => {
              Modal.confirm({
                title: '确认交卷',
                content: `已答 ${answeredCount}/${store.questions.length} 题，${store.questions.length - answeredCount} 题未答。确定交卷吗？`,
                onOk: doSubmit,
                okText: '确认交卷',
                cancelText: '继续答题',
              });
            }}>交卷</Button>
          </Space>
        </div>
      </Card>
      <Card size="small" style={{ marginBottom: 16 }}>
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
          {store.questions.map((q: any, i: number) => (
            <Tag key={q.paperQuestionId}
              color={store.marked.has(q.paperQuestionId) ? 'orange' : store.answers[q.paperQuestionId] ? 'green' : 'default'}
              style={{ cursor: 'pointer', fontSize: 13, padding: '2px 10px' }}
              onClick={() => document.getElementById(`q-${q.paperQuestionId}`)?.scrollIntoView({ behavior: 'smooth' })}>
              {i + 1}{store.marked.has(q.paperQuestionId) ? ' ★' : ''}
            </Tag>
          ))}
        </div>
      </Card>
      {store.questions.map((q: any, i: number) => (
        <div key={q.paperQuestionId} id={`q-${q.paperQuestionId}`}>{renderQuestion(q, i)}</div>
      ))}
    </div>
  );
}
