# 企业工单管理系统 (qiyegongdansys)

一套功能完善的企业工单管理系统，覆盖工单的**创建、分配、处理、跟踪、关闭**全流程。采用前后端分离架构，后端提供 RESTful API 并实现数据持久化，内置 RBAC 权限控制、JWT 认证与操作日志审计。

## 功能模块

| 模块 | 功能点 |
|------|--------|
| 用户管理 | 用户 CRUD、角色管理（ADMIN/MANAGER/AGENT/CUSTOMER）、启停用、按角色查询 |
| 工单创建 | 多种工单类型、自定义字段、优先级、自动/手动分配 |
| 工单分配 | 自动分配（最小工作量策略）、手动分配（指定处理人） |
| 工单处理 | 状态流转（含合法性校验）、评论/回复、附件上传/下载 |
| 查询统计 | 多维筛选（关键字/状态/优先级/类型/创建人/处理人/时间）、可视化报表数据接口 |
| 安全审计 | 用户认证（JWT）、接口级权限（RBAC）、操作日志记录（AOP 切面） |

## 技术栈

- **后端**：Java 17、Spring Boot 4.1.1、Spring MVC、Spring Security、Spring Data JPA、AOP（spring-boot-starter-aspectj）、JJWT
- **前端**：原生 HTML/CSS/JavaScript 单页应用（无构建工具依赖），内置响应式商务风格界面
- **数据库**：默认 MySQL 8（账号 `root`，密码 `123456`，库名 `qiyegongdansys`，自动建库建表），内置 H2 内存库作无 MySQL 环境的备用方案
- **构建**：Gradle 9.7.1
- **工具**：Lombok、Jackson 3

## 前端界面

由 Spring Boot 托管，访问根地址即可进入系统：

```
打开浏览器: http://localhost:8080/api/
```

功能覆盖（按角色显示）：

| 页面 | 说明 | 可见角色 |
|------|------|----------|
| 登录页 | JWT 认证 | 公开 |
| 数据看板 | 统计卡片、近 7 日趋势、类型/处理人/分配方式分布 | ADMIN / MANAGER |
| 工单管理 | 多维筛选、分页列表、状态/优先级徽标 | 登录用户 |
| 工单详情 | 状态流转、评论回复、附件上传/下载、自动/手动分配 | 按角色 |
| 工单类型 | 类型 CRUD、自定义字段 JSON 配置 | ADMIN / MANAGER |
| 用户管理 | 用户 CRUD、角色、启用/禁用 | ADMIN |
| 操作日志 | 审计日志查询 | ADMIN / MANAGER |

> 界面采用响应式设计，窄屏自动收起侧栏；自定义字段根据所选类型动态渲染。

## 快速开始

```bash
# 1. 前置：本机已运行 MySQL 8，账号 root / 密码 123456（可在 application.properties 修改）

# 2. 启动应用（默认端口 8080，上下文 /api，首次启动自动建库建表并初始化种子数据）
./gradlew bootRun

# 3. 访问 REST API: http://localhost:8080/api/*
```

> 说明：配置 `ddl-auto=update`，重启不会清空 MySQL 数据；种子数据仅在角色表为空时写入一次。
> 如本机无 MySQL，可将 `application.properties` 中注释的 H2 配置放开以临时运行。

### 默认种子账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 系统管理员 ADMIN | admin | admin123 |
| 业务主管 MANAGER | manager | manager123 |
| 处理人员 AGENT | agent1 / agent2 / agent3 | agent123 |
| 客户 CUSTOMER | customer | customer123 |

> MySQL 数据持久存在于磁盘，重启后数据保留。

## 接口约定

- 统一响应结构：`{ "code": 200, "message": "...", "data": {...} }`
- 认证方式：请求头 `Authorization: Bearer <token>`
- 登录接口：`POST /auth/login`（body: `{"username":"admin","password":"admin123"}`）

### 主要 REST API

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/auth/login` | 登录获取 token | 公开 |
| GET | `/auth/me` | 当前用户信息 | 登录 |
| GET | `/users` | 用户分页列表 | ADMIN |
| POST | `/users` | 创建用户 | ADMIN |
| PUT | `/users/{id}` | 更新用户 | ADMIN |
| DELETE | `/users/{id}` | 删除用户 | ADMIN |
| GET | `/users/role/{code}` | 按角色查用户 | ADMIN/MANAGER |
| GET | `/types` | 工单类型列表 | 登录 |
| POST | `/types` | 创建类型 | ADMIN/MANAGER |
| POST | `/workorders` | 创建工单（支持 assignMode=AUTO/MANUAL） | 登录 |
| GET | `/workorders` | 多维查询工单 | 登录 |
| GET | `/workorders/{id}` | 工单详情 | 登录 |
| POST | `/workorders/{id}/assign` | 分配/重新分配工单 | ADMIN/MANAGER |
| POST | `/workorders/{id}/status` | 状态流转 | ADMIN/MANAGER/AGENT |
| GET/POST | `/workorders/{id}/comments` | 评论列表/新增评论 | 登录 |
| GET/POST | `/workorders/{id}/attachments` | 附件列表/上传 | 登录 |
| GET | `/workorders/{id}/attachments/{aid}/download` | 附件下载 | 登录 |
| GET | `/statistics/overview` | 总体统计 | ADMIN/MANAGER |
| GET | `/statistics/by-type` `/by-agent` `/by-day` `/assign-mode` | 各维度报表 | ADMIN/MANAGER |
| GET | `/logs` | 操作日志查询 | ADMIN/MANAGER |

### 工单状态流转规则

```
PENDING ──开始处理──▶ PROCESSING ──标记解决──▶ RESOLVED ──确认关闭──▶ CLOSED
   │                     │                       │
   └──取消──▶ CANCELLED   └──取消──▶ CANCELLED     └──重新打开──▶ PROCESSING
```

## 项目结构

```
com.example.qiyegongdansys
├── aspect/         # 操作日志 AOP 切面
├── common/         # 统一响应、分页、异常、注解、角色常量
├── config/         # 安全配置、数据种子初始化
├── controller/     # REST 控制器
├── dto/            # 请求/响应对象
├── entity/         # JPA 实体
├── repository/     # 数据访问层
├── security/       # JWT、认证过滤器、安全工具
└── service/        # 业务逻辑（用户/工单/分配/统计/附件/日志等）
```

## 扩展性说明

- **自定义字段**：工单类型的 `customFields` 字段以 JSON 存储字段定义，工单通过 `customData` 存 JSON 值，业务结构变化无需改表。
- **权限点**：`RoleConstants` 中维护"角色 → 权限点"映射，前端可据此实现按钮级权限。
- **自动分配策略**：集中在 `AssignmentService.autoAssign()`，可替换为轮询、技能匹配等算法。
- **数据库切换**：数据库已默认使用 MySQL；如需回退 H2，放开 `application.properties` 中注释的 H2 配置并注释掉 MySQL 相关配置即可。