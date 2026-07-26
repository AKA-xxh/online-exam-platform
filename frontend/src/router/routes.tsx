import { lazy } from 'react';
import { Navigate } from 'react-router-dom';
import type { RouteObject } from 'react-router-dom';

// 懒加载页面组件
const Login = lazy(() => import('@/pages/auth/Login'));
const AdminLayout = lazy(() => import('@/layouts/AdminLayout'));
const StudentLayout = lazy(() => import('@/layouts/StudentLayout'));

// 管理端页面
const Dashboard = lazy(() => import('@/pages/admin/Dashboard'));
const AdminUserList = lazy(() => import('@/pages/admin/users/UserList'));
const AdminCourseList = lazy(() => import('@/pages/admin/courses/CourseList'));
const AdminCourseEdit = lazy(() => import('@/pages/admin/courses/CourseEdit'));
const AdminQuestionList = lazy(() => import('@/pages/admin/questions/QuestionList'));
const AdminQuestionEdit = lazy(() => import('@/pages/admin/questions/QuestionEdit'));
const AdminPaperList = lazy(() => import('@/pages/admin/papers/PaperList'));
const AdminPaperEdit = lazy(() => import('@/pages/admin/papers/PaperEdit'));
const AdminExamList = lazy(() => import('@/pages/admin/exams/ExamList'));
const AdminExamPublish = lazy(() => import('@/pages/admin/exams/ExamPublish'));
const AdminExamMonitor = lazy(() => import('@/pages/admin/exams/ExamMonitor'));
const AdminGradingList = lazy(() => import('@/pages/admin/grading/GradingList'));
const AdminGradingDetail = lazy(() => import('@/pages/admin/grading/GradingDetail'));
const AdminScoreList = lazy(() => import('@/pages/admin/scores/ScoreList'));
const AdminSystemConfig = lazy(() => import('@/pages/admin/system/SystemConfig'));
const AdminRoleManage = lazy(() => import('@/pages/admin/system/RoleManage'));
const AdminOperateLog = lazy(() => import('@/pages/admin/system/OperateLog'));
const NotFound = lazy(() => import('@/pages/common/NotFound'));

// 学员端页面
const StudentCourseList = lazy(() => import('@/pages/student/courses/CourseList'));
const StudentCourseDetail = lazy(() => import('@/pages/student/courses/CourseDetail'));
const StudentCoursePlayer = lazy(() => import('@/pages/student/courses/CoursePlayer'));
const StudentDashboard = lazy(() => import('@/pages/student/Dashboard'));
const StudentExamList = lazy(() => import('@/pages/student/exams/ExamList'));
const StudentExamPaper = lazy(() => import('@/pages/student/exams/ExamPaper'));
const StudentExamResult = lazy(() => import('@/pages/student/exams/ExamResult'));
const StudentWrongList = lazy(() => import('@/pages/student/wrong/WrongList'));
const StudentProfile = lazy(() => import('@/pages/student/profile/Profile'));

export const routes: RouteObject[] = [
  { path: '/', element: <Navigate to="/admin" replace /> },
  { path: '/login', element: <Login /> },

  // 管理端
  {
    path: '/admin',
    element: <AdminLayout />,
    children: [
      { index: true, element: <Navigate to="/admin/dashboard" replace /> },
      { path: 'dashboard', element: <Dashboard /> },
      { path: 'users', element: <AdminUserList /> },
      { path: 'courses', element: <AdminCourseList /> },
      { path: 'courses/new', element: <AdminCourseEdit /> },
      { path: 'courses/:id', element: <AdminCourseEdit /> },
      { path: 'questions', element: <AdminQuestionList /> },
      { path: 'questions/new', element: <AdminQuestionEdit /> },
      { path: 'questions/:id', element: <AdminQuestionEdit /> },
      { path: 'papers', element: <AdminPaperList /> },
      { path: 'papers/new', element: <AdminPaperEdit /> },
      { path: 'papers/:id', element: <AdminPaperEdit /> },
      { path: 'exams', element: <AdminExamList /> },
      { path: 'exams/new', element: <AdminExamPublish /> },
      { path: 'exams/:id', element: <AdminExamPublish /> },
      { path: 'exams/:id/monitor', element: <AdminExamMonitor /> },
      { path: 'grading', element: <AdminGradingList /> },
      { path: 'grading/:examId/:id', element: <AdminGradingDetail /> },
      { path: 'scores', element: <AdminScoreList /> },
      { path: 'system', element: <AdminSystemConfig /> },
      { path: 'system/roles', element: <AdminRoleManage /> },
      { path: 'system/logs', element: <AdminOperateLog /> },
    ],
  },

  // 学员端
  {
    path: '/student',
    element: <StudentLayout />,
    children: [
      { index: true, element: <Navigate to="/student/dashboard" replace /> },
      { path: 'dashboard', element: <StudentDashboard /> },
      { path: 'courses', element: <StudentCourseList /> },
      { path: 'courses/:id', element: <StudentCourseDetail /> },
      { path: 'courses/:id/lessons/:lessonId', element: <StudentCoursePlayer /> },
      { path: 'exams', element: <StudentExamList /> },
      { path: 'exams/:id', element: <StudentExamPaper /> },
      { path: 'exams/:id/result', element: <StudentExamResult /> },
      { path: 'wrong', element: <StudentWrongList /> },
      { path: 'profile', element: <StudentProfile /> },
    ],
  },

  { path: '*', element: <NotFound /> },
];
