-- ============================================================
-- 在线考试培训系统 - 数据库建表脚本
-- 版本: 1.0.0
-- 数据库: exam_platform
-- ============================================================

CREATE DATABASE IF NOT EXISTS exam_platform
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE exam_platform;

-- ==================== 用户与权限模块 ====================

-- 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id              BIGINT        PRIMARY KEY AUTO_INCREMENT  COMMENT '用户ID',
    username        VARCHAR(64)   NOT NULL UNIQUE             COMMENT '用户名',
    password        VARCHAR(256)  NOT NULL                    COMMENT '密码(BCrypt加密)',
    real_name       VARCHAR(32)                              COMMENT '真实姓名',
    nickname        VARCHAR(64)                              COMMENT '昵称',
    phone           VARCHAR(20)                              COMMENT '手机号',
    email           VARCHAR(128)                             COMMENT '邮箱',
    avatar          VARCHAR(512)                             COMMENT '头像URL',
    gender          TINYINT       DEFAULT 0                  COMMENT '性别:0未知1男2女',
    user_type       TINYINT       DEFAULT 1                  COMMENT '用户类型:1学员2教师3管理员',
    status          TINYINT       DEFAULT 1                  COMMENT '状态:0禁用1正常',
    last_login_time DATETIME                                 COMMENT '最后登录时间',
    last_login_ip   VARCHAR(64)                              COMMENT '最后登录IP',
    is_deleted      TINYINT       DEFAULT 0                  COMMENT '逻辑删除',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间',
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_phone (phone),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_user_type (user_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 系统角色表
CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT,
    role_name   VARCHAR(32)   NOT NULL UNIQUE COMMENT '角色名称',
    role_code   VARCHAR(32)   NOT NULL UNIQUE COMMENT '角色编码(admin/teacher/student)',
    description VARCHAR(256)  COMMENT '角色描述',
    status      TINYINT       DEFAULT 1 COMMENT '状态',
    sort_order  INT           DEFAULT 0 COMMENT '排序',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 系统权限表
CREATE TABLE IF NOT EXISTS sys_permission (
    id            BIGINT        PRIMARY KEY AUTO_INCREMENT,
    parent_id     BIGINT        DEFAULT 0                  COMMENT '父权限ID',
    perm_name     VARCHAR(64)   NOT NULL                    COMMENT '权限名称',
    perm_code     VARCHAR(128)  NOT NULL UNIQUE             COMMENT '权限编码',
    perm_type     TINYINT       NOT NULL                    COMMENT '类型:1菜单2按钮3接口',
    path          VARCHAR(256)                              COMMENT '路由路径',
    component     VARCHAR(256)                              COMMENT '前端组件路径',
    icon          VARCHAR(64)                               COMMENT '菜单图标',
    sort_order    INT           DEFAULT 0,
    is_visible    TINYINT       DEFAULT 1                  COMMENT '是否可见',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id      BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    UNIQUE KEY uk_user_role (user_id, role_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT,
    role_id       BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE KEY uk_role_perm (role_id, permission_id),
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- ==================== 课程模块 ====================

-- 课程分类表
CREATE TABLE IF NOT EXISTS course_category (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT,
    parent_id   BIGINT        DEFAULT 0     COMMENT '父分类ID(0为顶级)',
    name        VARCHAR(64)   NOT NULL      COMMENT '分类名称',
    icon        VARCHAR(512)                COMMENT '分类图标',
    sort_order  INT           DEFAULT 0,
    status      TINYINT       DEFAULT 1,
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程分类表';

-- 课程表
CREATE TABLE IF NOT EXISTS course (
    id                BIGINT        PRIMARY KEY AUTO_INCREMENT,
    category_id       BIGINT        NOT NULL      COMMENT '分类ID',
    title             VARCHAR(256)  NOT NULL      COMMENT '课程标题',
    subtitle          VARCHAR(256)                COMMENT '副标题',
    cover_url         VARCHAR(512)               COMMENT '封面图URL',
    description       TEXT                        COMMENT '课程简介',
    teacher_name      VARCHAR(64)                COMMENT '讲师姓名',
    teacher_intro     VARCHAR(512)               COMMENT '讲师简介',
    total_duration    INT           DEFAULT 0    COMMENT '总时长(秒)',
    lesson_count      INT           DEFAULT 0    COMMENT '课时数',
    student_count     INT           DEFAULT 0    COMMENT '学习人数(缓存)',
    rating            DECIMAL(2,1)  DEFAULT 5.0  COMMENT '评分(缓存)',
    status            TINYINT       DEFAULT 0    COMMENT '状态:0草稿1已发布2已下架',
    is_deleted        TINYINT       DEFAULT 0,
    create_by         BIGINT,
    create_time       DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_id (category_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 课程章节表
CREATE TABLE IF NOT EXISTS course_chapter (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT,
    course_id   BIGINT        NOT NULL,
    title       VARCHAR(256)  NOT NULL,
    sort_order  INT           DEFAULT 0,
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程章节表';

-- 课程课时表
CREATE TABLE IF NOT EXISTS course_lesson (
    id            BIGINT        PRIMARY KEY AUTO_INCREMENT,
    chapter_id    BIGINT        NOT NULL      COMMENT '所属章节ID',
    course_id     BIGINT        NOT NULL      COMMENT '所属课程ID(冗余)',
    title         VARCHAR(256)  NOT NULL      COMMENT '课时标题',
    lesson_type   TINYINT       NOT NULL      COMMENT '类型:1视频2图文3直播',
    video_url     VARCHAR(512)               COMMENT '视频URL',
    video_duration INT          DEFAULT 0    COMMENT '视频时长(秒)',
    content       LONGTEXT                   COMMENT '图文内容(富文本)',
    attachments   JSON                        COMMENT '附件列表JSON',
    is_free       TINYINT       DEFAULT 0    COMMENT '是否免费试看',
    sort_order    INT           DEFAULT 0,
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_chapter_id (chapter_id),
    INDEX idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程课时表';

-- 学习记录表
CREATE TABLE IF NOT EXISTS learn_record (
    id              BIGINT    PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT    NOT NULL,
    course_id       BIGINT    NOT NULL,
    lesson_id       BIGINT    NOT NULL,
    progress        INT       DEFAULT 0     COMMENT '观看进度(%)',
    duration        INT       DEFAULT 0     COMMENT '本次学习时长(秒)',
    is_finished     TINYINT   DEFAULT 0     COMMENT '是否完成',
    finished_time   DATETIME                COMMENT '完成时间',
    create_time     DATETIME  DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_lesson (user_id, lesson_id),
    INDEX idx_user_course (user_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习记录表';

-- 课程评价表
CREATE TABLE IF NOT EXISTS course_evaluation (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT,
    course_id   BIGINT        NOT NULL,
    user_id     BIGINT        NOT NULL,
    rating      TINYINT       NOT NULL      COMMENT '评分(1-5)',
    content     TEXT,
    status      TINYINT       DEFAULT 0    COMMENT '状态:0待审核1已通过2已拒绝',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_course_user (course_id, user_id),
    INDEX idx_course_id (course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程评价表';

-- ==================== 题库模块 ====================

-- 题目分类表
CREATE TABLE IF NOT EXISTS question_category (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT,
    parent_id   BIGINT        DEFAULT 0,
    name        VARCHAR(64)   NOT NULL      COMMENT '分类名称',
    sort_order  INT           DEFAULT 0,
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目分类表';

-- 题目表
CREATE TABLE IF NOT EXISTS question (
    id              BIGINT        PRIMARY KEY AUTO_INCREMENT,
    category_id     BIGINT        NOT NULL      COMMENT '题目分类ID',
    question_type   TINYINT       NOT NULL      COMMENT '题型:1单选2多选3判断4简答',
    title           TEXT          NOT NULL      COMMENT '题干(支持富文本+图片)',
    analysis        TEXT                        COMMENT '题目解析',
    difficulty      TINYINT       DEFAULT 1    COMMENT '难度:1简单2中等3困难',
    tags            VARCHAR(512)               COMMENT '标签(逗号分隔)',
    version         INT           DEFAULT 1    COMMENT '版本号',
    use_count       INT           DEFAULT 0    COMMENT '被引用次数(缓存)',
    correct_rate    DECIMAL(5,2)               COMMENT '正确率(缓存)',
    status          TINYINT       DEFAULT 1    COMMENT '状态:0禁用1正常',
    is_deleted      TINYINT       DEFAULT 0,
    create_by       BIGINT                      COMMENT '创建人ID',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category_id (category_id),
    INDEX idx_question_type (question_type),
    INDEX idx_difficulty (difficulty),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目表';

-- 题目选项表
CREATE TABLE IF NOT EXISTS question_option (
    id            BIGINT        PRIMARY KEY AUTO_INCREMENT,
    question_id   BIGINT        NOT NULL      COMMENT '题目ID',
    option_label  VARCHAR(4)    NOT NULL      COMMENT '选项标签(A/B/C/D...)',
    option_text   TEXT          NOT NULL      COMMENT '选项内容',
    is_correct    TINYINT       DEFAULT 0    COMMENT '是否正确答案',
    sort_order    INT           DEFAULT 0,
    INDEX idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='题目选项表';

-- ==================== 考试模块 ====================

-- 试卷表
CREATE TABLE IF NOT EXISTS exam_paper (
    id                  BIGINT        PRIMARY KEY AUTO_INCREMENT,
    title               VARCHAR(256)  NOT NULL      COMMENT '试卷标题',
    description         TEXT                        COMMENT '试卷描述/说明',
    total_score         INT           NOT NULL      COMMENT '试卷总分',
    pass_score          INT           NOT NULL      COMMENT '及格分数线',
    duration            INT           NOT NULL      COMMENT '考试时长(分钟)',
    paper_type          TINYINT       NOT NULL      COMMENT '组卷方式:1手动2随机',
    gen_rules           JSON                        COMMENT '随机组卷规则(JSON)',
    show_answer         TINYINT       DEFAULT 1    COMMENT '答案展示:0不展示1交卷后2结束后',
    shuffle_question    TINYINT       DEFAULT 0    COMMENT '题目随机排序',
    shuffle_option      TINYINT       DEFAULT 0    COMMENT '选项随机排序',
    max_screen_switches INT           DEFAULT 3    COMMENT '允许切屏次数',
    enable_face_check   TINYINT       DEFAULT 0    COMMENT '人脸识别开关',
    anti_cheat          JSON                        COMMENT '防作弊配置',
    status              TINYINT       DEFAULT 0    COMMENT '状态:0草稿1已发布',
    is_deleted          TINYINT       DEFAULT 0,
    create_by           BIGINT,
    create_time         DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_create_by (create_by),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷表';

-- 试卷题目关联表（题目快照）
CREATE TABLE IF NOT EXISTS paper_question (
    id                BIGINT    PRIMARY KEY AUTO_INCREMENT,
    paper_id          BIGINT    NOT NULL      COMMENT '试卷ID',
    question_id       BIGINT    NOT NULL      COMMENT '题目ID(快照来源)',
    section_name      VARCHAR(128)            COMMENT '大题名称',
    question_type     TINYINT   NOT NULL      COMMENT '题型',
    title_snapshot    TEXT      NOT NULL      COMMENT '题干快照',
    options_snapshot  JSON                    COMMENT '选项快照(JSON)',
    answer_snapshot   JSON      NOT NULL      COMMENT '正确答案快照(JSON)',
    analysis_snapshot TEXT                    COMMENT '解析快照',
    score             INT       NOT NULL      COMMENT '分值',
    sort_order        INT       DEFAULT 0    COMMENT '排序',
    INDEX idx_paper_id (paper_id),
    INDEX idx_question_id (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='试卷题目关联表(题目快照)';

-- 考试表（考试发布）
CREATE TABLE IF NOT EXISTS exam (
    id              BIGINT        PRIMARY KEY AUTO_INCREMENT,
    paper_id        BIGINT        NOT NULL      COMMENT '试卷ID',
    title           VARCHAR(256)  NOT NULL      COMMENT '考试标题',
    description     TEXT                        COMMENT '考试须知',
    start_time      DATETIME      NOT NULL      COMMENT '考试开始时间',
    end_time        DATETIME      NOT NULL      COMMENT '考试截止时间',
    duration        INT           NOT NULL      COMMENT '考试时长(分钟)',
    student_scope   TINYINT       DEFAULT 1    COMMENT '考生范围:1全部2指定3指定部门',
    student_ids     JSON                        COMMENT '指定考生ID列表',
    status          TINYINT       DEFAULT 0    COMMENT '状态:0未开始1进行中2已结束3已取消',
    pass_score      INT                         COMMENT '及格分(可覆盖试卷设置)',
    is_deleted      TINYINT       DEFAULT 0,
    create_by       BIGINT,
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_paper_id (paper_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试表';

-- 考试考生记录表
CREATE TABLE IF NOT EXISTS exam_student (
    id                BIGINT    PRIMARY KEY AUTO_INCREMENT,
    exam_id           BIGINT    NOT NULL,
    user_id           BIGINT    NOT NULL,
    status            TINYINT   DEFAULT 0    COMMENT '状态:0未开始1考试中2已交卷3已出分4缺考',
    start_time        DATETIME               COMMENT '实际开始时间',
    submit_time       DATETIME               COMMENT '交卷时间',
    used_time         INT       DEFAULT 0   COMMENT '答题用时(秒)',
    total_score       INT                    COMMENT '总分',
    objective_score   INT       DEFAULT 0   COMMENT '客观题得分',
    subjective_score  INT       DEFAULT 0   COMMENT '主观题得分',
    is_passed         TINYINT               COMMENT '是否及格',
    grading_status    TINYINT   DEFAULT 0   COMMENT '阅卷状态:0未阅1已阅',
    screen_switches   INT       DEFAULT 0   COMMENT '切屏次数',
    is_cheated        TINYINT   DEFAULT 0   COMMENT '是否标记作弊',
    cheat_reason      VARCHAR(512)          COMMENT '作弊原因',
    is_deleted        TINYINT   DEFAULT 0,
    UNIQUE KEY uk_exam_user (exam_id, user_id),
    INDEX idx_exam_id (exam_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考试考生记录表';

-- 答题记录表
CREATE TABLE IF NOT EXISTS answer_record (
    id                BIGINT    PRIMARY KEY AUTO_INCREMENT,
    exam_student_id   BIGINT    NOT NULL      COMMENT '考试考生记录ID',
    exam_id           BIGINT    NOT NULL      COMMENT '考试ID',
    paper_question_id BIGINT    NOT NULL      COMMENT '试卷题目ID',
    user_id           BIGINT    NOT NULL      COMMENT '用户ID',
    user_answer       JSON                    COMMENT '用户答案(JSON)',
    is_correct        TINYINT                COMMENT '是否正确:0错1对(NULL=主观题)',
    score             INT                    COMMENT '得分',
    grader_id         BIGINT                 COMMENT '阅卷人ID',
    grader_comment    VARCHAR(512)           COMMENT '阅卷评语',
    graded_time       DATETIME               COMMENT '阅卷时间',
    create_time       DATETIME  DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_exam_student_id (exam_student_id),
    INDEX idx_exam_user (exam_id, user_id),
    UNIQUE KEY uk_exam_student_question (exam_student_id, paper_question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='答题记录表';

-- ==================== 错题本与统计 ====================

-- 错题本表
CREATE TABLE IF NOT EXISTS wrong_question (
    id                BIGINT    PRIMARY KEY AUTO_INCREMENT,
    user_id           BIGINT    NOT NULL,
    question_id       BIGINT    NOT NULL      COMMENT '原题目ID',
    exam_id           BIGINT    NOT NULL,
    answer_record_id  BIGINT    NOT NULL,
    wrong_count       INT       DEFAULT 1    COMMENT '错误次数',
    is_removed        TINYINT   DEFAULT 0    COMMENT '是否移除(已掌握)',
    create_time       DATETIME  DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_question_exam (user_id, question_id, exam_id),
    INDEX idx_user_id (user_id),
    INDEX idx_user_removed (user_id, is_removed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='错题本表';

-- ==================== 系统管理 ====================

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_operate_log (
    id              BIGINT        PRIMARY KEY AUTO_INCREMENT,
    user_id         BIGINT,
    username        VARCHAR(64),
    module          VARCHAR(64)   COMMENT '操作模块',
    action          VARCHAR(128)  COMMENT '操作类型',
    description     VARCHAR(512)  COMMENT '操作描述',
    request_url     VARCHAR(256)  COMMENT '请求URL',
    request_method  VARCHAR(16)   COMMENT '请求方法',
    request_params  TEXT          COMMENT '请求参数(脱敏)',
    ip              VARCHAR(64),
    user_agent      VARCHAR(512),
    duration        INT           COMMENT '耗时(毫秒)',
    result          TINYINT       DEFAULT 1 COMMENT '结果:0失败1成功',
    error_msg       VARCHAR(1024) COMMENT '错误信息',
    create_time     DATETIME      DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_module (module)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    id            BIGINT        PRIMARY KEY AUTO_INCREMENT,
    config_key    VARCHAR(64)   NOT NULL UNIQUE COMMENT '配置键',
    config_value  TEXT          NOT NULL      COMMENT '配置值',
    config_type   TINYINT       DEFAULT 1    COMMENT '类型:1文本2JSON3图片',
    description   VARCHAR(256)               COMMENT '配置说明',
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 消息通知表
CREATE TABLE IF NOT EXISTS sys_message (
    id          BIGINT        PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT        NOT NULL,
    title       VARCHAR(256)  NOT NULL,
    content     TEXT,
    msg_type    TINYINT       DEFAULT 1    COMMENT '类型:1系统通知2考试提醒3课程通知',
    is_read     TINYINT       DEFAULT 0,
    read_time   DATETIME,
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 学习时长统计表
CREATE TABLE IF NOT EXISTS user_learn_duration (
    id          BIGINT    PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT    NOT NULL,
    course_id   BIGINT    NOT NULL,
    date        DATE      NOT NULL      COMMENT '日期',
    duration    INT       DEFAULT 0    COMMENT '当日学习时长(秒)',
    UNIQUE KEY uk_user_course_date (user_id, course_id, date),
    INDEX idx_user_date (user_id, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习时长统计表';
