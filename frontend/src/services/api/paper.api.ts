import request from '@/services/request';

export function getPapers(params: any) { return request.get('/admin/papers', { params }); }
export function getPaperDetail(id: number) { return request.get(`/admin/papers/${id}`); }
export function createPaper(data: any) { return request.post('/admin/papers', data); }
export function updatePaper(id: number, data: any) { return request.put(`/admin/papers/${id}`, data); }
export function deletePaper(id: number) { return request.delete(`/admin/papers/${id}`); }
export function regeneratePaper(id: number) { return request.post(`/admin/papers/${id}/regenerate`); }
