import request from '@/services/request';

export function getWrongQuestions(params: any) { return request.get('/student/wrong-questions', { params }); }
export function getWrongDetail(id: number) { return request.get(`/student/wrong-questions/${id}`); }
export function removeWrong(id: number) { return request.delete(`/student/wrong-questions/${id}`); }
export function getWrongStats() { return request.get('/student/wrong-questions/stats'); }
