# Project Context

## Purpose
敏捷估点服务 (Agile Planning Poker Service) - 为敏捷开发团队提供基于Web的Planning Poker估点工具。团队成员通过局域网访问服务，对需求池中的故事卡进行估点投票，系统确保所有参与者投票完成后才能查看彼此的估算点数，避免相互影响，获得更客观的估点结果。

## Tech Stack
- **Backend**: Java 17+, Spring Boot 3.x, Spring Data JPA, WebSocket
- **Database**: H2内存数据库 (H2 In-Memory Database)
- **Frontend**: Vue.js 3, Element Plus UI, Pinia状态管理, Vite构建工具
- **Real-time Communication**: STOMP over WebSocket (使用 @stomp/stompjs 和 sockjs-client)
- **Build Tool**: Maven (后端), npm/Vite (前端)
- **Deployment**: 单体JAR部署，前后端打包在一起

## Project Conventions

### Code Style
- **语言**: 所有文档、注释、日志和用户提示信息使用中文
- **Java规范**:
  - 遵循 Effective Java 原则
  - 使用静态工厂方法创建值对象 (如 `StoryPoint.of()`)
  - 使用构建器模式创建复杂对象
  - 值对象设计为不可变类 (immutable)
  - 使用 Optional 处理可能缺失的数据
  - 使用枚举表示状态 (SessionStatus, CardStatus)
  - 重写 equals, hashCode, toString (值对象和实体)
- **前端规范**:
  - 使用 Vue 3 Composition API
  - 组件命名使用 PascalCase
  - 文件命名使用 PascalCase (组件) 或 camelCase (工具类)
  - 使用 Pinia 进行状态管理
  - **样式规范**: 必须使用全局CSS变量，禁止硬编码颜色值
  - **API调用**: 必须使用 `frontend/src/api/index.js` 中配置的api实例

### Architecture Patterns
- **四层架构 (Four-Layer Architecture)**:
  1. **表现层 (Presentation Layer)**: REST API + WebSocket, DTOs
  2. **应用层 (Application Layer)**: 用例编排, Application Services, Commands/Queries
  3. **领域层 (Domain Layer)**: 领域模型, 业务逻辑, 仓储接口, 领域服务
  4. **基础设施层 (Infrastructure Layer)**: 数据持久化, 仓储实现, 外部服务集成
- **领域驱动设计 (DDD)**:
  - 聚合根 (Aggregate Roots): VotingSession, StoryCard, BacklogPool
  - 值对象 (Value Objects): StoryPoint, Vote
  - 领域事件 (Domain Events): VotingSessionStarted, VoteSubmitted, VotesRevealed, VotingSessionCompleted
  - 仓储 (Repositories): 在领域层定义接口，基础设施层实现

### UI Design System
- **全局CSS变量**: 定义在 `frontend/src/styles/theme.css`
  - 颜色系统: `--color-bg-*`, `--color-text-*`, `--color-accent-*`
  - 间距系统: `--spacing-xs` 到 `--spacing-3xl`
  - 阴影系统: `--shadow-*` (包括霓虹光晕效果)
  - 玻璃态效果: `--glass-bg`, `--glass-border`, `--glass-blur`
- **样式使用规则**:
  - 所有颜色必须使用CSS变量: `var(--color-*)`
  - 禁止在组件中硬编码颜色值
  - 使用预定义的工具类: `.glass-card`, `.text-neon-*`
- **可访问性**: 支持用户偏好减少动画 (`prefers-reduced-motion`)

### Testing Strategy
- **测试覆盖率目标**: ≥80%
- **分层测试要求**:
  - 领域层: ≥90% (单元测试)
  - 应用层: ≥85% (单元测试 + Mock)
  - 基础设施层: ≥70% (集成测试)
  - 表现层: ≥75% (API集成测试)
- **测试工具**: JUnit 5, Mockito, Spring Boot Test
- **测试数据**: 使用H2内存数据库和 data.sql 初始化测试数据

### Git Workflow
- **分支策略**:
  - `main`: 主分支，保持稳定可部署状态
  - `feature/*`: 功能分支，从 main 创建，完成后合并回 main
  - `fix/*`: 修复分支，用于bug修复
- **提交规范**:
  - 使用清晰描述性的提交信息
  - 建议格式: `type: brief description` (e.g., `feat: 添加投票可见性控制`)
  - type类型: feat(新功能), fix(修复), refactor(重构), docs(文档), test(测试)
- **合并前要求**:
  - 所有测试通过
  - 代码通过检查
  - 完成相关的OpenSpec变更归档

## Domain Context
- **核心领域模型**:
  - **估点会话 (Voting Session)**: 针对一张故事卡进行估点的完整过程
  - **故事卡 (Story Card)**: 代表一个用户故事或需求项，是估点的对象
  - **投票 (Vote)**: 参与者对故事卡工作量的估算，用故事点数表示
  - **故事点数 (Story Point)**: 表示工作量的相对值，采用斐波那契数列：1、2、3、5、8
  - **需求池 (Backlog)**: 待估点的故事卡集合
  - **参与者 (Participant)**: 登录并参与估点的用户
- **关键业务规则**:
  - 同一时间只能有一个活跃的估点会话
  - 只有所有参与者都投票后，才能揭示投票结果
  - 参与者可以修改自己的投票，直到结果揭示
  - 故事点数必须是斐波那契数列值之一 (1, 2, 3, 5, 8)
- **统一语言**: 使用中文术语进行领域建模和沟通

## Important Constraints
- **技术约束**:
  - 必须使用Java、Spring Boot、H2数据库
  - 必须遵循四层架构和DDD设计
  - 前端必须使用Vue.js 3
- **性能约束**:
  - 支持10个并发用户
  - 响应时间 < 2秒
  - 页面加载时间 < 1秒
- **数据约束**:
  - 支持至少100张故事卡
  - 支持至少1000条投票记录
  - 单个会话投票记录上限10条 (对应10个参与者)
- **业务约束**:
  - 故事卡标题长度: 1-100字符
  - 故事卡描述长度: 最多2000字符
  - 用户名长度: 2-20字符，支持中文、英文和数字
  - 故事点数: 只能是 1、2、3、5、8 之一

## External Dependencies
- **前端依赖** (通过npm):
  - `vue` (v3.4+): 核心框架
  - `element-plus` (v2.5+): UI组件库
  - `@element-plus/icons-vue`: 图标库
  - `pinia` (v2.1+): 状态管理
  - `axios` (v1.6+): HTTP客户端
  - `@stomp/stompjs` (v7.0+): STOMP WebSocket客户端
  - `sockjs-client` (v1.6+): WebSocket降级方案
  - `canvas-confetti` (v1.9+): 揭示结果的庆祝动画效果
  - `vue-router` (v4.2+): 路由管理
- **后端依赖** (通过Maven):
  - Spring Boot Starter Web
  - Spring Boot Starter Data JPA
  - Spring Boot Starter WebSocket
  - H2 Database (runtime scope)
  - SpringDoc OpenAPI (API文档生成)
- **无外部服务依赖**: 系统设计为在局域网内独立运行，无需互联网连接或外部API
