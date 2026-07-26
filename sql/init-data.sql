-- ============================================================
-- 在线考试培训系统 - 初始化数据
-- ============================================================

USE exam_platform;

-- ==================== 默认角色 ====================

INSERT INTO sys_role (id, role_name, role_code, description, status, sort_order) VALUES
(1, '超级管理员', 'admin',     '平台最高权限，可管理所有功能', 1, 0),
(2, '教师',       'teacher',  '课程管理、题库管理、组卷阅卷', 1, 1),
(3, '学员',       'student',  '学习课程、参加考试',           1, 2)
ON DUPLICATE KEY UPDATE role_name = VALUES(role_name);

-- ==================== 默认权限 ====================

-- 控制台（9列，9值）
INSERT INTO sys_permission (id, parent_id, perm_name, perm_code, perm_type, path, component, icon, sort_order) VALUES
(1,  0, '控制台',   'dashboard',            1, '/dashboard',         'admin/Dashboard',      'DashboardOutlined', 0),
(2,  0, '用户管理', 'user:manage',          1, '/users',             'admin/users/UserList', 'UserOutlined',      1),
(3,  0, '课程管理', 'course:manage',        1, '/courses',           'admin/courses/CourseList', 'BookOutlined',  2),
(4,  0, '题库管理', 'question:manage',      1, '/questions',         'admin/questions/QuestionList', 'DatabaseOutlined', 3),
(5,  0, '试卷管理', 'paper:manage',         1, '/papers',            'admin/papers/PaperList',       'FileTextOutlined', 4),
(6,  0, '考试管理', 'exam:manage',          1, '/exams',             'admin/exams/ExamList',         'ScheduleOutlined', 5),
(7,  0, '阅卷管理', 'grading:manage',       1, '/grading',           'admin/grading/GradingList',    'CheckOutlined',    6),
(8,  0, '成绩管理', 'score:manage',         1, '/scores',            'admin/scores/ScoreList',       'BarChartOutlined', 7),
(9,  0, '系统设置', 'system:manage',        1, '/system',            'admin/system/SystemConfig',    'SettingOutlined',  8),

-- 用户管理子权限
(10, 2, '用户列表',   'user:list',           2, NULL, NULL, NULL, 0),
(11, 2, '新增用户',   'user:create',         2, NULL, NULL, NULL, 1),
(12, 2, '编辑用户',   'user:update',         2, NULL, NULL, NULL, 2),
(13, 2, '删除用户',   'user:delete',         2, NULL, NULL, NULL, 3),
(14, 2, '批量导入',   'user:import',         2, NULL, NULL, NULL, 4),

-- 课程管理子权限
(20, 3, '课程列表',   'course:list',         2, NULL, NULL, NULL, 0),
(21, 3, '创建课程',   'course:create',       2, NULL, NULL, NULL, 1),
(22, 3, '编辑课程',   'course:update',       2, NULL, NULL, NULL, 2),
(23, 3, '删除课程',   'course:delete',       2, NULL, NULL, NULL, 3),

-- 题库管理子权限
(30, 4, '题目列表',   'question:list',       2, NULL, NULL, NULL, 0),
(31, 4, '新增题目',   'question:create',     2, NULL, NULL, NULL, 1),
(32, 4, '编辑题目',   'question:update',     2, NULL, NULL, NULL, 2),
(33, 4, '删除题目',   'question:delete',     2, NULL, NULL, NULL, 3),
(34, 4, '批量导入',   'question:import',     2, NULL, NULL, NULL, 4),

-- 试卷管理子权限
(40, 5, '试卷列表',   'paper:list',          2, NULL, NULL, NULL, 0),
(41, 5, '创建试卷',   'paper:create',        2, NULL, NULL, NULL, 1),
(42, 5, '编辑试卷',   'paper:update',        2, NULL, NULL, NULL, 2),
(43, 5, '删除试卷',   'paper:delete',        2, NULL, NULL, NULL, 3),

-- 考试管理子权限
(50, 6, '考试列表',   'exam:list',           2, NULL, NULL, NULL, 0),
(51, 6, '发布考试',   'exam:create',         2, NULL, NULL, NULL, 1),
(52, 6, '编辑考试',   'exam:update',         2, NULL, NULL, NULL, 2),
(53, 6, '删除考试',   'exam:delete',         2, NULL, NULL, NULL, 3),
(54, 6, '考试监控',   'exam:monitor',        2, NULL, NULL, NULL, 4),
(55, 6, '强制收卷',   'exam:force-submit',   2, NULL, NULL, NULL, 5),

-- 系统设置子权限
(90, 9, '角色管理',   'role:manage',         2, NULL, NULL, NULL, 0),
(91, 9, '系统配置',   'config:manage',       2, NULL, NULL, NULL, 1),
(92, 9, '操作日志',   'log:view',            2, NULL, NULL, NULL, 2)
ON DUPLICATE KEY UPDATE perm_name = VALUES(perm_name);

-- ==================== 角色-权限关联 ====================

-- 超级管理员拥有所有权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 教师拥有课程、题库、试卷、考试、阅卷、成绩权限
INSERT INTO sys_role_permission (role_id, permission_id) VALUES
(2, 1),  -- 控制台
(2, 3), (2, 20), (2, 21), (2, 22), (2, 23),  -- 课程
(2, 4), (2, 30), (2, 31), (2, 32), (2, 33), (2, 34), -- 题库
(2, 5), (2, 40), (2, 41), (2, 42), (2, 43), -- 试卷
(2, 6), (2, 50), (2, 51), (2, 52), (2, 53), (2, 54), -- 考试
(2, 7),  -- 阅卷
(2, 8)   -- 成绩
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- ==================== 默认管理员账号 ====================
-- 密码: admin123 (BCrypt 加密结果)
INSERT INTO sys_user (id, username, password, real_name, nickname, user_type, status) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '系统管理员', 'Admin', 3, 1)
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- 管理员-角色关联
INSERT INTO sys_user_role (user_id, role_id) VALUES (1, 1)
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- ==================== 默认教师账号 ====================
INSERT INTO sys_user (id, username, password, real_name, nickname, user_type, status) VALUES
(2, 'teacher', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '张老师', 'Teacher Zhang', 2, 1)
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT INTO sys_user_role (user_id, role_id) VALUES (2, 2)
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- ==================== 默认学员账号 ====================
INSERT INTO sys_user (id, username, password, real_name, nickname, user_type, status) VALUES
(3, 'student', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5Eh', '李同学', 'Student Li', 1, 1)
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT INTO sys_user_role (user_id, role_id) VALUES (3, 3)
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- ==================== 默认系统配置 ====================
INSERT INTO sys_config (config_key, config_value, config_type, description) VALUES
('site_name',      '在线考试培训系统', 1, '平台名称'),
('site_logo',      '',                 3, '平台 Logo URL'),
('site_copyright', '© 2024 在线考试培训系统', 1, '版权信息'),
('register_enabled', 'true',           1, '是否开放注册'),
('register_verify',  'false',          1, '注册是否需要验证码')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value);

-- ==================== 默认课程分类 ====================
INSERT INTO course_category (id, parent_id, name, sort_order) VALUES
(1, 0, '计算机技术',   0),
(2, 0, '外语学习',     1),
(3, 0, '职业技能',     2),
(4, 0, '考试认证',     3),
(5, 1, 'Java开发',     0),
(6, 1, 'Python开发',   1),
(7, 1, '前端开发',     2),
(8, 2, '英语',         0),
(9, 2, '日语',         1),
(10,3, '项目管理',     0),
(11,4, '认证考试',     0)
ON DUPLICATE KEY UPDATE name = VALUES(name);
