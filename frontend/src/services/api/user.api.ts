import request from '@/services/request';

export function getUsers(params: any) { return request.get('/admin/users', { params }); }
export function getUserDetail(id: number) { return request.get(`/admin/users/${id}`); }
export function getRoles() { return request.get('/admin/users/roles'); }
export function createUser(data: any) { return request.post('/admin/users', data); }
export function updateUser(id: number, data: any) { return request.put(`/admin/users/${id}`, data); }
export function deleteUser(id: number) { return request.delete(`/admin/users/${id}`); }
export function toggleUserStatus(id: number, status: number) { return request.patch(`/admin/users/${id}/status`, null, { params: { status } }); }
export function resetPassword(id: number) { return request.patch(`/admin/users/${id}/reset-password`); }
