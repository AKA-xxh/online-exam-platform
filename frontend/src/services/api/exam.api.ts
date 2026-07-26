import request from '@/services/request';

// 管理端
export function getExams(params: any) { return request.get('/admin/exams', { params }); }
export function publishExam(data: any) { return request.post('/admin/exams', data); }
export function cancelExam(id: number) { return request.patch(`/admin/exams/${id}/cancel`); }
export function getExamMonitor(id: number) { return request.get(`/admin/exams/${id}/monitor`); }
export function forceSubmit(examId: number, userId: number) { return request.post(`/admin/exams/${examId}/force-submit/${userId}`); }

// 学员端
export function getMyExams() { return request.get('/student/exams'); }
export function startExam(examId: number) { return request.post(`/student/exams/${examId}/start`); }
export function saveAnswer(data: { examStudentId: number; paperQuestionId: number; answer: string }) { return request.post('/student/exams/answers', data); }
export function submitExam(examStudentId: number) { return request.post(`/student/exams/${examStudentId}/submit`); }
export function getExamResult(examStudentId: number) { return request.get(`/student/exams/${examStudentId}/result`); }
