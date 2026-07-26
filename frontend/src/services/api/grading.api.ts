import request from '@/services/request';

export function getGradingList(params: any) { return request.get('/admin/grading/list', { params }); }
export function getGradingDetail(examStudentId: number) { return request.get(`/admin/grading/${examStudentId}`); }
export function submitScore(examStudentId: number, data: any) { return request.post(`/admin/grading/${examStudentId}/score`, data); }
export function publishScore(examId: number) { return request.post(`/admin/grading/${examId}/publish`); }
