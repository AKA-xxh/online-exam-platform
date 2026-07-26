import request from '@/services/request';

export function getQuestions(params: any) { return request.get('/admin/questions', { params }); }
export function getQuestionDetail(id: number) { return request.get(`/admin/questions/${id}`); }
export function createQuestion(data: any) { return request.post('/admin/questions', data); }
export function updateQuestion(id: number, data: any) { return request.put(`/admin/questions/${id}`, data); }
export function deleteQuestion(id: number) { return request.delete(`/admin/questions/${id}`); }
export function batchDeleteQuestions(ids: number[]) { return request.delete('/admin/questions/batch', { data: ids }); }
export function batchUpdateStatus(ids: number[], status: number) { return request.patch('/admin/questions/batch/status', { ids, status }); }
export function batchMoveCategory(ids: number[], categoryId: number) { return request.patch('/admin/questions/batch/move', { ids, categoryId }); }
export function getQuestionStats() { return request.get('/admin/questions/stats'); }

export function getQuestionCategories() { return request.get('/admin/questions/categories'); }
export function createQuestionCategory(data: any) { return request.post('/admin/questions/categories', data); }
export function updateQuestionCategory(id: number, data: any) { return request.put(`/admin/questions/categories/${id}`, data); }
export function deleteQuestionCategory(id: number) { return request.delete(`/admin/questions/categories/${id}`); }
