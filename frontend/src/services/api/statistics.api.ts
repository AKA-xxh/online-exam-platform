import request from '@/services/request';

export function getDashboard() { return request.get('/admin/statistics/dashboard'); }
export function getExamStats(examId: number) { return request.get(`/admin/statistics/exam/${examId}`); }
export function getQuestionStats(examId: number) { return request.get(`/admin/statistics/questions/${examId}`); }
export function getScores(params: any) { return request.get('/admin/statistics/scores', { params }); }
