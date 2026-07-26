# 在线考试培训系统

面向企业/培训机构的在线学习 + 考试一体化平台。支持学员在线学习课程、参加考试、查看错题本；管理员管理题库、组卷、阅卷、统计分析。

---

## 技术栈

| 层级 | 版本 | 选型 |
|------|------|------|
| 前端框架 | React 18 + TypeScript | 企业级中后台首选 |
| UI 组件库 | Ant Design 5 | 阿里出品，中文生态最完善 |
| 构建工具 | Vite 5 | 秒级冷启动，HMR 极速热更新 |
| 状态管理 | Zustand 4 | 轻量，TS 推断极好 |
| 图表 | ECharts 5 | 百度开源，数据可视化标配 |
| 后端框架 | Spring Boot 3.2 | Java 企业级事实标准 |
| ORM | MyBatis-Plus 3.5 | 国人最爱，代码生成 + 分页 + 多租户 |
| 权限认证 | Sa-Token 1.37 | 比 Spring Security 更轻，中文文档友好 |
| 数据库 | MySQL 8.0 | InnoDB 行级锁 + 全文索引 |
| 缓存 | Redis 7 + Redisson | 分布式锁 + 会话管理 |
| 接口文档 | Knife4j | Swagger 增强，离线文档导出 |
| 建表管理 | SQL 脚本 + DataInitializer | 项目启动自动补齐初始数据 |

---

## 核心功能

### 管理端（教师 / 管理员）

| 模块 | 功能 |
|------|------|
| 控制台 | 核心指标卡片、学习趋势图、考试通过率统计、课程排行 |
| 用户管理 | 用户列表（搜索/筛选）、批量导入导出、角色分配、启用/禁用 |
| 课程管理 | 多级分类树、课程 CRUD、章节/课时拖拽排序、视频/图文/附件 |
| 题库管理 | 单选/多选/判断/简答四种题型、题目分类、难度标签、Excel 批量导入 |
| 试卷管理 | **手动组卷**（逐题勾选 + 分值设置）、**随机组卷**（规则配置自动抽题）、预览、题目快照 |
| 考试管理 | 发布考试（时段 + 考生范围 + 防作弊配置）、实时监控、强制收卷、延长考生时间 |
| 阅卷管理 | 主观题逐题批阅、参考答案对照、批量阅卷、成绩发布/撤回 |
| 成绩管理 | 分数分布图、各题正确率分析、成绩排名、Excel 导出 |
| 系统设置 | 平台配置、角色权限管理、操作日志审计 |

### 学员端

| 模块 | 功能 |
|------|------|
| 学习首页 | 学习统计、最近课程、待考提醒、错题数量 |
| 课程中心 | 分类筛选、关键词搜索、视频倍速 + 断点续播、图文阅读、学习进度自动记录 |
| 我的考试 | 待考/已考列表、倒计时提醒 |
| 在线答题 | **全屏霸屏模式**、倒计时 + 题目导航（已答/未答/标记）、30 秒自动保存、**切屏检测 + 防作弊**、交卷确认 |
| 考试成绩 | 总分/各题型得分/正确率、逐题作答详情、答案与解析 |
| 错题本 | 按题型/考试筛选、逐题查看（正确答案高亮 + 解析）、标记已掌握、薄弱点统计 |
| 个人中心 | 资料编辑、修改密码 |

---

## 快速开始

### 前置条件

- **JDK** 17+
- **Maven** 3.8+
- **Node.js** 18+
- **MySQL** 8.0+ (端口 3306，密码 `123456`)
- **Redis** 7+ (端口 6379)

如果你有 Docker，可以一键启动 MySQL + Redis：

```bash
docker-compose -f docker/docker-compose.yml up -d mysql redis
```

### 1. 初始化数据库

```bash
# 创建表结构
mysql -u root -p123456 < sql/schema.sql

# 导入初始数据（角色、权限、默认账号、课程分类）
mysql -u root -p123456 < sql/init-data.sql
```

### 2. 启动后端

```bash
cd backend
mvn install -DskipTests -q
cd exam-server
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

启动成功后访问：
- **API 在线文档**：http://localhost:8080/doc.html
- **健康检查**：GET http://localhost:8080/api/v1/auth/check

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

浏览器打开 http://localhost:5173

---

## 默认账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 超级管理员 | `admin` | `admin123` | 所有权限 |
| 教师 | `teacher` | `teacher123` | 课程/题库/试卷/考试/阅卷管理 |
| 学员 | `student` | `student123` | 学习课程、参加考试、查看错题本 |

> 首次启动时 `DataInitializer` 会自动用 BCrypt 加密密码，确保密码安全。

---

## 项目结构

```
online-exam-platform/
├── frontend/                          # 前端项目
│   └── src/
│       ├── pages/
│       │   ├── auth/Login.tsx         # 登录页
│       │   ├── admin/                 # 管理端页面
│       │   │   ├── Dashboard.tsx      # 数据看板
│       │   │   ├── courses/           # 课程管理
│       │   │   ├── questions/         # 题库管理
│       │   │   ├── papers/            # 试卷管理
│       │   │   ├── exams/             # 考试管理 + 监控
│       │   │   ├── grading/           # 阅卷管理
│       │   │   ├── scores/            # 成绩管理
│       │   │   └── system/            # 系统设置
│       │   └── student/               # 学员端页面
│       │       ├── Dashboard.tsx      # 学员首页
│       │       ├── courses/           # 课程学习
│       │       ├── exams/             # 考试 + 答题页
│       │       ├── wrong/             # 错题本
│       │       └── profile/           # 个人中心
│       ├── components/                # 公共组件
│       ├── services/                  # API 请求层
│       ├── stores/                    # Zustand 状态管理
│       ├── router/                    # 路由配置 + 守卫
│       └── hooks/                     # 自定义 Hooks
│
├── backend/                           # 后端项目 (Maven 多模块)
│   ├── exam-common/                   # 公共模块（Result/异常/枚举）
│   └── exam-server/                   # 主服务模块
│       └── src/main/java/com/exam/
│           ├── config/                # Spring 配置 + DataInitializer
│           ├── security/              # Sa-Token 认证 + 权限
│           ├── common/                # 统一返回 + 异常处理
│           └── module/                # 业务模块
│               ├── auth/              # 认证（登录/注册/Token）
│               ├── user/              # 用户管理
│               ├── course/            # 课程管理
│               ├── question/          # 题库管理
│               ├── paper/             # 试卷管理 + 随机组卷引擎
│               ├── exam/              # 考试管理 + 自动判分引擎
│               ├── score/             # 成绩 + 错题本
│               └── statistics/        # Dashboard + 统计分析
│
├── sql/
│   ├── schema.sql                     # 建表脚本（22 张表）
│   └── init-data.sql                  # 初始数据
├── docker/
│   └── docker-compose.yml             # 开发环境编排
└── README.md
```

---

## API 文档

启动后端后访问 Knife4j 在线文档：

```
http://localhost:8080/doc.html
```

所有接口按模块分组（认证/课程/题库/试卷/考试/阅卷/统计/系统），支持在线调试。

### 统一返回格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": { ... },
  "timestamp": 1690387200000
}
```

### 认证方式

请求头携带 `Authorization: Bearer <token>`，Token 有效期 2 小时。

---

## License

MIT
