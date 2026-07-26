import request from '@/services/request';

// 分类
export function getCategories() { return request.get('/courses/categories'); }
export function createCategory(data: any) { return request.post('/admin/courses/categories', data); }
export function updateCategory(id: number, data: any) { return request.put(`/admin/courses/categories/${id}`, data); }
export function deleteCategory(id: number) { return request.delete(`/admin/courses/categories/${id}`); }

// 课程
export function getCourses(params: any) { return request.get('/admin/courses', { params }); }
export function getStudentCourses(params: any) { return request.get('/courses', { params }); }
export function getCourseDetail(id: number) { return request.get(`/courses/${id}`); }
export function createCourse(data: any) { return request.post('/admin/courses', data); }
export function updateCourse(id: number, data: any) { return request.put(`/admin/courses/${id}`, data); }
export function updateCourseStatus(id: number, status: number) { return request.patch(`/admin/courses/${id}/status`, null, { params: { status } }); }
export function deleteCourse(id: number) { return request.delete(`/admin/courses/${id}`); }

// 章节
export function createChapter(courseId: number, data: any) { return request.post(`/admin/courses/${courseId}/chapters`, data); }
export function updateChapter(id: number, data: any) { return request.put(`/admin/courses/chapters/${id}`, data); }
export function deleteChapter(id: number) { return request.delete(`/admin/courses/chapters/${id}`); }
export function sortChapters(data: any) { return request.post('/admin/courses/chapters/sort', data); }

// 课时
export function createLesson(chapterId: number, courseId: number, data: any) { return request.post(`/admin/courses/chapters/${chapterId}/lessons`, data, { params: { courseId } }); }
export function updateLesson(id: number, data: any) { return request.put(`/admin/courses/lessons/${id}`, data); }
export function deleteLesson(id: number) { return request.delete(`/admin/courses/lessons/${id}`); }

// 学习进度
export function getMyCourses() { return request.get('/courses/my'); }
export function reportProgress(data: { lessonId: number; courseId: number; progress: number; duration: number }) { return request.post('/courses/progress', data); }
