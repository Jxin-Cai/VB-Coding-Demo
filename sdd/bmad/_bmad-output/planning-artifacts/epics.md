---
stepsCompleted: [step-01-validate-prerequisites, step-02-design-epics, step-03-create-stories]
inputDocuments:
  - prd.md
  - architecture.md
  - ux-design-specification.md
requirementsExtracted: true
epicsApproved: true
epicCount: 5
storiesCompleted: true
totalStories: 21
workflowComplete: true
---

# bmad - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for bmad, decomposing the requirements from the PRD, UX Design if it exists, and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

**文件管理能力（FR1-FR11）**：

- **FR1**: 用户可以上传 DDL 文件（.sql 格式）
- **FR2**: 用户可以通过拖拽或选择文件的方式上传 DDL
- **FR3**: 系统可以解析 DDL 文件,提取表结构、字段、索引信息
- **FR4**: 系统可以将 DDL 内容向量化并存储到内存向量库
- **FR5**: 用户可以查看已上传文件的列表（文件名、状态、上传时间）
- **FR6**: 用户可以查看文件的解析状态（已解析、解析中、解析失败、待支持格式）
- **FR7**: 用户可以删除已上传的文件
- **FR8**: 用户可以覆盖同名文件（系统提示确认）
- **FR9**: 用户可以选择当前使用的文件（切换上下文）
- **FR10**: 系统可以支持多个文件的独立管理
- **FR11**: 系统可以在文件解析完成后显示解析结果（表数量、字段数量）

**智能对话能力（FR12-FR17）**：

- **FR12**: 用户可以通过自然语言与系统对话
- **FR13**: 系统可以识别用户意图（普通对话 vs SQL 生成需求）
- **FR14**: 系统可以在普通对话模式下回应用户的一般性问题
- **FR15**: 系统可以在识别到 SQL 生成需求时自动切换到 SQL 生成模式
- **FR16**: 系统可以在对话界面显示历史对话（当前会话）
- **FR17**: 用户可以输入文本并发送消息

**SQL 生成能力（FR18-FR25）**：

- **FR18**: 系统可以基于用户的自然语言描述生成 SQL 语句
- **FR19**: 系统可以使用 Agent 架构自主决策是否需要调用向量检索工具
- **FR20**: 系统可以根据用户意图检索相关的 DDL 片段（表和字段）
- **FR21**: 系统可以将检索到的 DDL 片段作为上下文补充到 LLM Prompt 中
- **FR22**: 系统可以生成语法正确的 SQL 语句（符合目标数据库语法）
- **FR23**: 系统可以生成逻辑正确的 SQL 语句（完全符合用户意图）
- **FR24**: 用户可以一键复制生成的 SQL 语句
- **FR25**: 系统可以在 SQL 生成响应时间在 3 秒内完成

**可解释性能力（FR26-FR30）**：

- **FR26**: 系统可以展示生成 SQL 时引用的表名称
- **FR27**: 系统可以展示生成 SQL 时引用的字段名称
- **FR28**: 系统可以展示表和字段的中文说明（如果 DDL 中有注释）
- **FR29**: 用户可以通过引用信息验证 SQL 的准确性
- **FR30**: 系统可以清晰格式化引用信息（表名 + 字段名 + 说明）

**质量保障能力（FR31-FR36）**：

- **FR31**: 系统可以在生成 SQL 后进行语法验证（使用 SQL Parser）
- **FR32**: 系统可以验证引用的表和字段是否存在于当前 DDL 中
- **FR33**: 系统可以在 SQL 生成失败时提供友好的错误提示
- **FR34**: 系统可以在 DDL 解析失败时提供清晰的错误原因和修正建议
- **FR35**: 系统可以在向量检索失败时告知用户而非生成错误的 SQL
- **FR36**: 系统可以确保相同的自然语言输入生成一致的 SQL（输出一致性 ≥ 95%）

**用户引导能力（FR37-FR41）**：

- **FR37**: 系统可以为首次使用的用户提供简单的使用引导
- **FR38**: 系统可以在用户上传文件后提供清晰的成功反馈
- **FR39**: 系统可以在操作失败时提供明确的失败原因和下一步建议
- **FR40**: 系统可以在 DDL 解析过程中显示进度提示
- **FR41**: 系统可以在 SQL 生成过程中显示生成状态（如"AI 正在思考..."）

**系统稳定性能力（FR42-FR45）**：

- **FR42**: 系统可以在 LLM API 调用超时时提供重试机制
- **FR43**: 系统可以限制单个 DDL 文件的大小（< 10MB）
- **FR44**: 系统可以在内存资源不足时提供清晰的提示
- **FR45**: 系统可以在服务不可用时提供友好的降级提示

---

**总计：45 条功能需求，覆盖 7 个核心能力领域**

### NonFunctional Requirements

**性能要求（Performance）**：

| 指标 | 目标值 | 理由 |
|-----|-------|------|
| 首屏加载时间（FCP） | < 1.5秒 | 用户快速进入工作状态 |
| 完全可交互时间（TTI） | < 3秒 | 避免用户等待焦虑 |
| 首次输入延迟（FID） | < 100ms | 交互响应流畅 |
| 累积布局偏移（CLS） | < 0.1 | 页面稳定,不抖动 |
| DDL 文件上传 | < 1秒 | 快速反馈 |
| DDL 解析与向量化 | < 5秒 | 用户可接受的等待时间 |
| **SQL 生成响应** | **< 3秒** | **核心体验,必须快速** |
| 普通对话响应 | < 2秒 | 流畅的对话体验 |
| 向量检索 | < 1秒 | SQL 生成的关键环节 |

**可靠性要求（Reliability）**：

| 指标 | 目标值 | 测量方式 |
|-----|-------|---------|
| 系统可用性 | ≥ 99.5% | 月度统计（允许约 3.6 小时停机/月） |
| 错误率 | < 2% | API 请求失败率 |
| MTBF（平均无故障时间） | > 168小时 | 连续运行时间 |
| MTTR（平均修复时间） | < 1小时 | 故障恢复时间 |

**质量要求（Quality）**：

| 指标 | 目标值 | 验证方式 |
|-----|-------|---------|
| **SQL 生成准确率** | **100%** | **语法正确 + 逻辑正确 + 可直接执行** |
| 向量检索相关性 | ≥ 85% | 召回的 DDL 片段与意图高度相关 |
| 输出一致性 | ≥ 95% | 相同输入生成一致的 SQL |
| LLM 温度 | 0.1 | 低温度确保输出稳定 |
| 单元测试覆盖率 | ≥ 80% | 核心模块 |

**安全要求（Security）**：

- DDL 文件内容仅存储在内存,不持久化到磁盘
- 用户会话数据不跨用户共享
- LLM API 调用通过 HTTPS 加密传输
- 敏感配置（API Key）通过环境变量管理,不写入代码
- 防止生成危险的 SQL（DELETE、DROP、UPDATE 无 WHERE 等）
- SQL 生成前检查语句类型,仅允许 SELECT 查询
- 日志中不记录用户的 DDL 文件内容、LLM API Key、完整 SQL

**集成要求（Integration）**：

| 依赖 | 类型 | 可用性要求 | 降级策略 |
|-----|------|-----------|---------|
| LLM API（GLM） | 必需 | ≥ 99% | 超时重试,失败提示用户 |
| 向量库（FAISS/Chroma） | 必需 | 100%（内存） | 内存不足时提示用户 |
| SQL Parser（sqlparse） | 必需 | 100%（本地库） | 无降级,直接报错 |

- 所有外部 API 调用设置超时（5秒）
- 所有外部 API 调用有重试机制（最多 3 次,指数退避）
- 所有外部 API 调用记录调用时长和成功率

**可维护性要求（Maintainability）**：

- 模块化设计：每个模块职责单一
- 依赖注入：便于测试和替换
- 配置外部化：所有配置通过环境变量或配置文件
- 文档完整：关键模块有设计文档和接口说明
- 日志记录：关键操作和错误记录
- 性能监控：响应时间、错误率统计
- 资源监控：内存、CPU 使用率
- 告警机制：关键错误触发告警
- 容器化部署（Docker）
- 快速回滚机制（保留上一版本镜像）
- 健康检查接口（/health）

### Additional Requirements

**从 Architecture 提取的技术要求**：

#### 1. Starter Template & Project Initialization

**前端 Starter**：
- 使用 Vue.js 官方 Starter（`create-vue`）
- 技术栈：Vue 3 + Vite + TypeScript（推荐）
- 自动配置：ESLint、Prettier、Vue Router、Pinia

**后端从零构建**：
- 基于 DDD 分层架构（Interface、Application、Domain、Infrastructure）
- Python 3.9+
- LangChain + LangGraph + RAG 架构高度定制化
- 遵循 SOLID 原则和充血模型

**初始化步骤**（Epic 1 Story 1 重点）：
1. 前端：`npm create vue@latest rag-text-to-sql-frontend`
   - 启用 TypeScript、Vue Router、Pinia
   - 安装 Ant Design Vue / Element Plus
   - 配置代理到后端（`vite.config.ts`）

2. 后端：创建 DDD 分层结构
   ```
   backend/
   ├── interface/          # API 层
   ├── application/        # 应用服务层
   ├── domain/             # 领域层
   ├── infrastructure/     # 基础设施层
   ├── config.py           # Pydantic Settings
   └── main.py             # FastAPI 入口
   ```

3. 配置管理：
   - `.env` 文件（GLM_API_KEY）
   - `config.py`（环境变量加载）

#### 2. 部署架构要求

**单进程部署方式**：
- 前端：`npm run build` → 编译产物
- 部署：前端产物复制到后端 `static/` 目录
- 启动：Python 后端同时提供前后端服务

**环境要求**：
- Python 3.9+
- Node.js 16+（前端构建）
- 内存：≥ 4GB（支持向量库）

#### 3. 监控与日志要求

**日志记录**：
- 使用 Python `logging` 模块
- 日志级别：INFO（关键操作）、ERROR（错误追踪）
- 不记录敏感信息（DDL 完整内容、API Key、完整 SQL）

**性能监控**：
- 响应时间统计（DDL 解析、SQL 生成、对话响应）
- 错误率统计（API 失败率）
- 资源监控（内存、CPU 使用率）

**健康检查**：
- 提供 `/health` 接口
- 返回系统状态（服务运行、向量库状态、LLM API 可用性）

#### 4. 技术约束

**必须遵循的架构原则**：
- DDD 分层架构（Interface、Application、Domain、Infrastructure）
- SOLID 原则（单一职责、开闭原则、里氏替换、接口隔离、依赖倒置）
- 充血模型（领域对象包含行为，避免贫血模型）
- 面向对象设计（避免面向过程式编程）

**代码质量**：
- 遵循 DDD 分层架构
- 遵循 SOLID 原则
- 无明显代码坏味道
- 代码审查必须通过

---

**从 UX Design 提取的用户体验要求**：

#### 1. 响应式设计要求

**桌面端（主要场景）**：
- 最小宽度：1280px
- 推荐宽度：1920px
- 布局：三栏布局（文件管理、对话区、DDL 预览）

**平板端（次要支持）**：
- 宽度范围：768px - 1279px
- 布局：自适应单栏布局
- 功能：完整功能

**移动端（暂不支持）**：
- MVP 阶段不支持移动端（< 768px）
- 理由：产品经理和数据分析师主要在桌面端工作

**设计原则**：
- Desktop-first 设计（优先优化桌面体验）
- 关键功能（DDL上传、SQL生成）必须在所有支持的屏幕尺寸上可用

#### 2. 无障碍访问要求

**目标级别**：基本可用（WCAG 2.1 Level A 部分符合）

**基本要求**：
- 语义化 HTML 结构（使用正确的标签）
- 键盘导航支持（Tab 键切换、Enter 确认）
- 合理的颜色对比度（文本与背景）
- 表单字段有明确的 Label
- 按钮和链接有清晰的可点击状态

**不强制要求**：
- 完整的屏幕阅读器支持
- WCAG 2.1 Level AA/AAA 认证
- 详细的 ARIA 标签

#### 3. 浏览器支持要求

**支持的浏览器（现代浏览器）**：

| 浏览器 | 最低版本 | 说明 |
|-------|---------|------|
| Chrome | 90+ | 推荐,完整支持 |
| Edge | 90+ | 推荐,完整支持 |
| Firefox | 88+ | 完整支持 |
| Safari | 14+ | 完整支持 |

**不支持的浏览器**：
- Internet Explorer（任何版本）
- 旧版浏览器（2020年以前的版本）

#### 4. 交互模式要求

**实时反馈与进度可视化**：
- 显示 Agent 工作进度（"正在检索相关表..."、"找到 3 个相关表"、"正在生成 SQL..."）
- 使用动画展示 RAG 检索过程
- 提供预估时间（"大约还需 2 秒"）

**双模式切换的透明性**：
- 明确的模式切换视觉反馈（图标、颜色、布局变化）
- 在切换到 SQL 生成模式时提供友好提示
- 支持手动切换模式（可选）
- 用户始终清楚当前处于什么模式

**情感化设计**：
- SQL 生成成功时的庆祝微交互（如 ✓ 动画、成功提示音）
- "首次成功"的特别反馈（"恭喜！你刚刚自己生成了第一条 SQL 🎉"）
- 温暖的引导文案（"别担心，我会帮你一步步完成"）

### FR Coverage Map

**Epic 1: 系统基础设施与可观测性**（Infrastructure Epic）
- Starter Template 初始化（Vue.js 前端 + DDD 后端）
- 监控与日志系统（NFR - Maintainability）
- 健康检查接口（/health）
- 配置管理（.env, config.py）

**Epic 2: DDL 文件管理与向量化**
- FR1: 用户可以上传 DDL 文件（.sql 格式）
- FR2: 用户可以通过拖拽或选择文件的方式上传 DDL
- FR3: 系统可以解析 DDL 文件,提取表结构、字段、索引信息
- FR4: 系统可以将 DDL 内容向量化并存储到内存向量库
- FR5: 用户可以查看已上传文件的列表（文件名、状态、上传时间）
- FR6: 用户可以查看文件的解析状态（已解析、解析中、解析失败、待支持格式）
- FR7: 用户可以删除已上传的文件
- FR8: 用户可以覆盖同名文件（系统提示确认）
- FR9: 用户可以选择当前使用的文件（切换上下文）
- FR10: 系统可以支持多个文件的独立管理
- FR11: 系统可以在文件解析完成后显示解析结果（表数量、字段数量）

**Epic 3: 智能对话与 SQL 生成（含可解释性）**
- FR12: 用户可以通过自然语言与系统对话
- FR13: 系统可以识别用户意图（普通对话 vs SQL 生成需求）
- FR14: 系统可以在普通对话模式下回应用户的一般性问题
- FR15: 系统可以在识别到 SQL 生成需求时自动切换到 SQL 生成模式
- FR16: 系统可以在对话界面显示历史对话（当前会话）
- FR17: 用户可以输入文本并发送消息
- FR18: 系统可以基于用户的自然语言描述生成 SQL 语句
- FR19: 系统可以使用 Agent 架构自主决策是否需要调用向量检索工具
- FR20: 系统可以根据用户意图检索相关的 DDL 片段（表和字段）
- FR21: 系统可以将检索到的 DDL 片段作为上下文补充到 LLM Prompt 中
- FR22: 系统可以生成语法正确的 SQL 语句（符合目标数据库语法）
- FR23: 系统可以生成逻辑正确的 SQL 语句（完全符合用户意图）
- FR24: 用户可以一键复制生成的 SQL 语句
- FR25: 系统可以在 SQL 生成响应时间在 3 秒内完成
- FR26: 系统可以展示生成 SQL 时引用的表名称
- FR27: 系统可以展示生成 SQL 时引用的字段名称
- FR28: 系统可以展示表和字段的中文说明（如果 DDL 中有注释）
- FR29: 用户可以通过引用信息验证 SQL 的准确性
- FR30: 系统可以清晰格式化引用信息（表名 + 字段名 + 说明）

**Epic 4: 质量保障与用户引导**
- FR31: 系统可以在生成 SQL 后进行语法验证（使用 SQL Parser）
- FR32: 系统可以验证引用的表和字段是否存在于当前 DDL 中
- FR33: 系统可以在 SQL 生成失败时提供友好的错误提示
- FR34: 系统可以在 DDL 解析失败时提供清晰的错误原因和修正建议
- FR35: 系统可以在向量检索失败时告知用户而非生成错误的 SQL
- FR36: 系统可以确保相同的自然语言输入生成一致的 SQL（输出一致性 ≥ 95%）
- FR37: 系统可以为首次使用的用户提供简单的使用引导
- FR38: 系统可以在用户上传文件后提供清晰的成功反馈
- FR39: 系统可以在操作失败时提供明确的失败原因和下一步建议
- FR40: 系统可以在 DDL 解析过程中显示进度提示
- FR41: 系统可以在 SQL 生成过程中显示生成状态（如"AI 正在思考..."）

**Epic 5: 系统稳定性与运维保障**
- FR42: 系统可以在 LLM API 调用超时时提供重试机制
- FR43: 系统可以限制单个 DDL 文件的大小（< 10MB）
- FR44: 系统可以在内存资源不足时提供清晰的提示
- FR45: 系统可以在服务不可用时提供友好的降级提示
- NFR: 系统可用性 ≥ 99.5%
- NFR: 错误率 < 2%
- NFR: 性能监控和告警机制

## Epic List

### Epic 1: 系统基础设施与可观测性 🛠️

开发团队和运维工程师可以部署和监控系统，基础架构和开发环境就绪，具备完整的可观测性能力（监控、日志、健康检查）。

**用户成果**：
- 前端项目（Vue 3 + Vite + TypeScript）初始化完成
- 后端 DDD 分层架构搭建完成
- 单进程部署架构配置完成
- 日志系统和健康检查接口运行正常

**FRs 覆盖**：
- Starter Template 初始化（Architecture 要求）
- 监控与日志（NFR - Maintainability）
- 健康检查接口（/health）
- 配置管理（.env、config.py）

**技术要点**：
- 前端：使用 `create-vue` 官方 Starter
- 后端：从零构建 DDD 架构（Interface、Application、Domain、Infrastructure）
- 部署：前端 build → 复制到后端 static/ → Python 单进程启动

---

### Epic 2: DDL 文件管理与向量化 📁

产品经理可以上传和管理数据库 DDL 文件，系统自动解析并向量化，为 SQL 生成做好准备。

**用户成果**：
- 可以上传 DDL 文件（拖拽或选择）
- 系统快速解析 DDL（< 5 秒）并显示结果
- 可以管理多个文件（查看列表、删除、切换上下文）
- 文件状态清晰可见（已解析、解析中、失败）

**FRs 覆盖**：FR1-FR11（完整的文件管理能力）

**技术要点**：
- DDL 解析：提取表结构、字段、索引信息
- 向量化：使用 FAISS/Chroma 内存向量库
- 文件状态管理：前端 Pinia Store + 后端状态追踪

**依赖**：基于 Epic 1（项目基础）

---

### Epic 3: 智能对话与 SQL 生成（含可解释性）🤖

产品经理可以通过自然语言对话生成精准的 SQL 语句，并看到生成依据（引用的表和字段），建立对 AI 的信任。

**用户成果**：
- 可以用自然语言描述需求，系统自动生成 SQL
- 双模式智能切换（普通对话 ↔ SQL 生成）
- SQL 生成快速（< 3 秒）且准确（100%）
- 看到引用的表、字段和说明，可以验证 SQL 正确性
- 一键复制生成的 SQL

**FRs 覆盖**：FR12-FR30（智能对话 + SQL 生成 + 可解释性）

**技术要点**：
- Agent 架构：LangChain + LangGraph
- RAG 流程：意图识别 → 向量检索 → SQL 生成
- 可解释性：展示引用源（表名、字段名、DDL 片段）
- 质量核心：100% SQL 准确率（语法 + 逻辑）

**依赖**：基于 Epic 1（项目基础）+ Epic 2（DDL 向量库）

---

### Epic 4: 质量保障与用户引导 ✅

新用户可以快速上手（首次使用成功率 ≥ 90%），系统确保 SQL 质量，提供友好的错误提示和修正建议。

**用户成果**：
- 首次使用有清晰的引导流程
- 操作有实时反馈（DDL 解析中、SQL 生成中）
- SQL 生成后自动验证语法和引用
- 错误时提供明确的原因和修正建议
- 输出一致性高（相同输入 → 相同输出）

**FRs 覆盖**：FR31-FR41（质量保障 + 用户引导）

**技术要点**：
- 语法验证：使用 SQL Parser（sqlparse/sqlglot）
- 引用验证：确保表和字段存在于 DDL 中
- 用户引导：首次使用提示、实时进度、友好错误
- 输出一致性：LLM 温度 0.1

**依赖**：横切关注点，贯穿 Epic 2、3

---

### Epic 5: 系统稳定性与运维保障 🛡️

系统稳定可靠运行（可用性 ≥ 99.5%），性能监控到位，故障快速恢复。

**用户成果**：
- 系统稳定运行，错误率低（< 2%）
- LLM API 调用失败时自动重试
- 资源限制保护（文件大小、内存使用）
- 性能监控和告警机制
- 友好的降级提示

**FRs 覆盖**：FR42-FR45 + NFRs（系统稳定性）

**技术要点**：
- 超时机制：5 秒超时 + 3 次重试（指数退避）
- 资源限制：单文件 < 10MB，内存保护
- 监控：响应时间、错误率、资源使用
- 降级策略：LLM API 不可用、向量库异常

**依赖**：横切关注点，贯穿所有 Epic

---

## Epic 1: 系统基础设施与可观测性 🛠️

开发团队和运维工程师可以部署和监控系统，基础架构和开发环境就绪，具备完整的可观测性能力（监控、日志、健康检查）。

### Story 1.1: 初始化 Starter Template（前端 + 后端）

As a **开发工程师**,
I want **使用官方 Starter 初始化前端项目并创建后端 DDD 架构**,
So that **项目基础结构标准化，开发环境快速就绪，可以立即开始功能开发**。

**Acceptance Criteria:**

**Given** 开发环境已安装 Node.js 16+ 和 Python 3.9+
**When** 执行前端初始化命令 `npm create vue@latest rag-text-to-sql-frontend`
**Then** 前端项目创建成功，包含以下配置：
**And** Vue 3 + Vite + TypeScript 已启用
**And** Vue Router 和 Pinia 已集成
**And** ESLint 和 Prettier 已配置
**And** 项目目录结构符合 Vue 官方最佳实践

**Given** 前端项目已创建
**When** 安装 UI 组件库（Ant Design Vue 或 Element Plus）
**Then** UI 组件库成功安装并可以导入使用
**And** 全局样式配置完成

**Given** 后端目录已创建
**When** 创建 DDD 分层架构目录结构
**Then** 以下目录结构创建成功：
```
backend/
├── interface/          # API 层
├── application/        # 应用服务层
├── domain/             # 领域层
├── infrastructure/     # 基础设施层
├── config.py           # Pydantic Settings
├── main.py             # FastAPI 入口
└── requirements.txt    # Python 依赖
```
**And** 每个层级包含 `__init__.py` 文件

**Given** DDD 架构目录已创建
**When** 配置环境变量管理（创建 `.env.example` 和 `config.py`）
**Then** `.env.example` 包含必需的环境变量模板（GLM_API_KEY、LOG_LEVEL）
**And** `config.py` 使用 Pydantic Settings 加载环境变量
**And** 敏感信息不写入代码或版本控制

**Given** 前后端项目已初始化
**When** 执行项目验证命令（前端 `npm run dev`，后端 `python main.py`）
**Then** 前端开发服务器在 `http://localhost:5173` 成功启动
**And** 后端服务器在 `http://localhost:8000` 成功启动
**And** 前端可以通过代理访问后端 API（配置在 `vite.config.ts`）

---

### Story 1.2: 配置单进程部署架构

As a **开发工程师**,
I want **配置单进程部署流程，前端构建产物由后端静态服务提供**,
So that **部署简化为单个 Python 进程启动，减少运维复杂度**。

**Acceptance Criteria:**

**Given** 前端项目已完成开发
**When** 执行前端构建命令 `npm run build`
**Then** 前端编译产物生成在 `frontend/dist` 目录
**And** 产物包含优化后的 HTML、CSS、JS 文件
**And** 构建日志显示成功信息

**Given** 前端构建完成
**When** 配置部署脚本将 `frontend/dist` 复制到 `backend/static`
**Then** 部署脚本成功执行
**And** `backend/static` 目录包含完整的前端文件
**And** 文件权限正确，可被后端服务读取

**Given** 后端已配置静态文件服务
**When** 在 FastAPI `main.py` 中挂载静态文件目录
**Then** 配置如下代码成功添加：
```python
from fastapi.staticfiles import StaticFiles

app.mount("/", StaticFiles(directory="static", html=True), name="static")
```
**And** 访问根路径 `/` 返回前端 `index.html`
**And** 静态资源（CSS、JS、图片）正确加载

**Given** 单进程部署已配置
**When** 仅启动后端 Python 进程 `python main.py`
**Then** 前端界面在 `http://localhost:8000` 可访问
**And** 前端 API 调用正确路由到后端接口
**And** 前后端功能完整，无需单独启动前端服务

**Given** 部署架构已验证
**When** 创建部署文档 `DEPLOYMENT.md`
**Then** 文档包含以下内容：
**And** 构建步骤（前端 build + 复制到 static）
**And** 启动命令（单进程启动）
**And** 环境变量配置说明
**And** 端口和网络配置

---

### Story 1.3: 建立日志系统和健康检查

As a **开发工程师和运维工程师**,
I want **配置结构化日志系统和健康检查接口**,
So that **可以监控系统运行状态，快速定位问题，确保系统可观测性**。

**Acceptance Criteria:**

**Given** Python 项目已初始化
**When** 配置 Python `logging` 模块（在 `config.py` 或独立 `logger.py`）
**Then** 日志配置包含以下要素：
**And** 日志级别从环境变量读取（默认 INFO）
**And** 日志格式包含：时间戳、日志级别、模块名、消息
**And** 日志输出到控制台（开发环境）
**And** 敏感信息（API Key、完整 SQL、DDL 内容）不记录

**Given** 日志系统已配置
**When** 在关键操作点添加日志记录
**Then** 以下操作有日志记录：
**And** 应用启动和关闭（INFO 级别）
**And** API 请求和响应（INFO 级别，记录路径、方法、状态码、耗时）
**And** 错误和异常（ERROR 级别，包含堆栈追踪）
**And** 性能瓶颈点（WARNING 级别，如超时操作）

**Given** 日志系统已工作
**When** 触发一个错误场景（如调用不存在的 API）
**Then** 错误日志正确记录：
```
2026-01-24 10:30:45 ERROR [module_name] API error: 404 Not Found - /api/nonexistent
Traceback: ...
```
**And** 日志信息足够定位问题
**And** 日志格式易于解析和搜索

**Given** 后端服务已启动
**When** 创建健康检查接口 `GET /health`
**Then** 接口返回 200 OK 和系统状态 JSON：
```json
{
  "status": "healthy",
  "timestamp": "2026-01-24T10:30:45Z",
  "services": {
    "api": "running",
    "vector_store": "not_initialized",
    "llm_api": "not_configured"
  }
}
```
**And** 响应时间 < 100ms

**Given** 健康检查接口已创建
**When** 系统启动但 LLM API 未配置（GLM_API_KEY 缺失）
**Then** 健康检查返回部分可用状态：
```json
{
  "status": "degraded",
  "timestamp": "2026-01-24T10:30:45Z",
  "services": {
    "api": "running",
    "vector_store": "not_initialized",
    "llm_api": "error: API key not configured"
  }
}
```
**And** HTTP 状态码仍为 200（避免监控误报）

**Given** 日志和健康检查已配置
**When** 创建监控文档 `MONITORING.md`
**Then** 文档包含：
**And** 日志级别说明（INFO、WARNING、ERROR）
**And** 健康检查接口使用方法
**And** 常见日志模式和故障排查指南
**And** 性能监控指标说明（响应时间、错误率）

---

## Epic 2: DDL 文件管理与向量化 📁

产品经理可以上传和管理数据库 DDL 文件，系统自动解析并向量化，为 SQL 生成做好准备。

### Story 2.1: DDL 文件上传功能

As a **产品经理**,
I want **通过拖拽或选择文件的方式上传 DDL 文件**,
So that **可以快速将数据库表结构导入系统，无需手动输入表信息**。

**Acceptance Criteria:**

**Given** 用户打开文件管理界面
**When** 看到文件上传区域
**Then** 界面显示清晰的上传提示：
**And** "拖拽 .sql 文件到此处，或点击选择文件"
**And** 支持的文件格式说明：".sql 格式的 DDL 文件"
**And** 文件大小限制提示："最大 10MB"

**Given** 用户拖拽一个 .sql 文件到上传区域
**When** 文件悬停在上传区域上方
**Then** 上传区域高亮显示（视觉反馈）
**And** 显示"释放以上传"提示

**Given** 用户释放拖拽的 .sql 文件
**When** 文件开始上传
**Then** 显示上传进度条和百分比
**And** 显示"正在上传 [文件名]..." 状态
**And** 上传完成后显示"✓ 上传成功"提示
**And** 文件自动进入"解析中"状态

**Given** 用户点击"选择文件"按钮
**When** 文件选择对话框打开
**Then** 对话框过滤仅显示 .sql 文件
**And** 用户选择文件后自动开始上传
**And** 上传流程与拖拽方式一致

**Given** 用户上传非 .sql 格式文件（如 .txt、.xlsx）
**When** 文件类型验证失败
**Then** 显示友好错误提示："仅支持 .sql 格式的 DDL 文件"
**And** 提供修正建议："请确保文件扩展名为 .sql"
**And** 上传被阻止，不发送到后端

**Given** 用户上传超过 10MB 的文件
**When** 文件大小验证失败
**Then** 显示错误提示："文件大小超过限制（最大 10MB）"
**And** 提供建议："请优化 DDL 文件或分批上传"
**And** 上传被阻止

**Given** 后端接收到上传的文件
**When** 执行服务端验证
**Then** 验证文件格式（Content-Type: text/plain 或 application/sql）
**And** 验证文件大小（< 10MB）
**And** 验证文件内容包含有效的 SQL DDL 语句（CREATE TABLE 等）
**And** 验证失败返回 400 Bad Request 和错误信息

**Given** 文件上传成功
**When** 后端保存文件到临时存储（内存）
**Then** 文件保存成功
**And** 生成唯一文件 ID（UUID）
**And** 创建文件元数据记录（文件名、上传时间、状态：待解析）
**And** 返回 201 Created 和文件 ID
**And** 触发异步解析任务

---

### Story 2.2: DDL 解析与向量化

As a **产品经理**,
I want **系统自动解析上传的 DDL 文件并向量化存储**,
So that **可以快速准备好 SQL 生成所需的数据库结构知识，解析时间 < 5 秒**。

**Acceptance Criteria:**

**Given** DDL 文件上传成功，状态为"待解析"
**When** 异步解析任务启动
**Then** 文件状态更新为"解析中"
**And** 前端实时显示"解析中..."状态
**And** 显示预估时间："大约需要 3-5 秒"

**Given** 解析任务运行
**When** 使用 SQL Parser（如 sqlparse）解析 DDL 内容
**Then** 成功提取以下信息：
**And** 所有 CREATE TABLE 语句
**And** 表名称（含 schema 如果有）
**And** 字段列表（字段名、数据类型、约束、注释）
**And** 主键和外键信息
**And** 索引定义
**And** 表注释（如果有）

**Given** DDL 解析完成
**When** 提取到的表结构信息
**Then** 每个表的信息包含：
```json
{
  "table_name": "users",
  "schema": "public",
  "comment": "用户表",
  "columns": [
    {
      "name": "user_id",
      "type": "BIGINT",
      "nullable": false,
      "comment": "用户ID",
      "is_primary_key": true
    },
    ...
  ],
  "indexes": [...],
  "foreign_keys": [...]
}
```

**Given** 表结构信息提取完成
**When** 执行向量化处理
**Then** 为每个表和字段生成向量嵌入（Embedding）
**And** 使用 Embedding 模型（如 text-embedding-ada-002 或本地模型）
**And** 向量维度符合向量库要求（如 1536 维）
**And** 向量存储到 FAISS 或 Chroma 内存向量库
**And** 向量与原始 DDL 文本关联（便于检索后展示）

**Given** 向量化完成
**When** 向量库初始化成功
**Then** 向量库包含所有表和字段的向量
**And** 可以执行相似度检索测试
**And** 测试查询："查询用户表" 返回 users 表相关向量

**Given** 解析和向量化成功
**When** 更新文件状态为"已解析"
**Then** 文件元数据更新：
**And** 状态：已解析
**And** 解析时间戳
**And** 表数量统计
**And** 字段总数统计
**And** 前端显示"✓ 解析完成：23 张表，156 个字段"

**Given** 解析过程中发生错误（语法错误、不支持的 DDL）
**When** 捕获解析异常
**Then** 文件状态更新为"解析失败"
**And** 记录错误原因（如"第 45 行：语法错误"）
**And** 前端显示友好错误提示："解析失败：DDL 文件第 45 行存在语法错误"
**And** 提供修正建议："请检查 DDL 文件格式是否正确"
**And** 错误日志记录详细堆栈（供开发排查）

**Given** 解析任务性能要求
**When** 测量解析和向量化总时间
**Then** 单文件解析时间 < 5 秒（包含向量化）
**And** 23 张表、156 个字段的文件在 3-4 秒内完成
**And** 如超时返回部分结果或提示用户稍后重试

---

### Story 2.3: 文件状态管理与显示

As a **产品经理**,
I want **查看已上传文件的列表和状态**,
So that **可以清楚了解哪些文件已就绪，哪些正在处理，快速定位问题文件**。

**Acceptance Criteria:**

**Given** 用户已上传一个或多个文件
**When** 打开文件管理界面
**Then** 显示文件列表，每个文件包含：
**And** 文件名（如"电商主站_DDL_v2.3.sql"）
**And** 上传时间（如"2026-01-24 10:30"）
**And** 文件大小（如"2.3 MB"）
**And** 状态标识（已解析 ✅、解析中 ⏳、解析失败 ❌、待支持 ⚠️）
**And** 表数量（解析成功后显示，如"23 张表"）
**And** 字段数量（如"156 个字段"）

**Given** 文件状态为"解析中"
**When** 实时更新状态
**Then** 状态图标动态显示（旋转的加载图标 ⏳）
**And** 显示进度提示："正在解析，大约还需 2 秒"
**And** 每 1 秒轮询后端状态更新（或使用 WebSocket）

**Given** 文件状态为"已解析"
**When** 显示解析结果
**Then** 状态图标为绿色对勾 ✅
**And** 显示解析结果摘要："✓ 解析完成：23 张表，156 个字段"
**And** 显示解析耗时："解析用时 3.2 秒"

**Given** 文件状态为"解析失败"
**When** 显示错误信息
**Then** 状态图标为红色叉号 ❌
**And** 显示错误原因："解析失败：DDL 文件第 45 行存在语法错误"
**And** 提供操作建议："请检查文件格式或联系技术支持"
**And** 提供"重新上传"按钮

**Given** 文件为不支持的格式（如 .xlsx）
**When** 显示待支持状态
**Then** 状态图标为黄色警告 ⚠️
**And** 显示提示："当前仅支持 .sql 格式，Excel 数据字典支持将在未来版本提供"

**Given** 文件列表较长（> 10 个文件）
**When** 显示文件列表
**Then** 实现虚拟滚动或分页
**And** 按上传时间倒序排列（最新的在上）
**And** 提供搜索功能（按文件名搜索）
**And** 提供筛选功能（按状态筛选：全部、已解析、失败）

**Given** 用户点击文件名
**When** 展开文件详情
**Then** 显示完整的解析结果：
**And** 表列表（表名、字段数量、注释）
**And** 可以点击表名查看字段详情
**And** DDL 原始内容（可选，折叠显示）

---

### Story 2.4: 多文件管理与上下文切换

As a **产品经理**,
I want **管理多个 DDL 文件并切换当前使用的文件**,
So that **可以为不同项目准备不同的数据库结构，避免混淆，确保生成正确的 SQL**。

**Acceptance Criteria:**

**Given** 用户已上传多个 DDL 文件（如"电商主站"、"小程序"、"供应链"）
**When** 查看文件列表
**Then** 每个文件独立显示
**And** 当前使用的文件有明确标识（如高亮、"当前使用"标签）
**And** 其他文件为普通状态

**Given** 用户点击某个文件的"使用此文件"按钮
**When** 切换当前上下文
**Then** 系统提示："确认切换到 [文件名]？切换后将使用该文件的表结构生成 SQL。"
**And** 用户确认后，当前文件切换
**And** 向量库上下文切换到该文件的向量
**And** 界面更新当前文件标识
**And** 显示切换成功提示："✓ 已切换到 [文件名]"

**Given** 当前正在使用某个文件生成 SQL
**When** 切换到另一个文件
**Then** 对话历史保留（不清空）
**And** 后续 SQL 生成使用新文件的表结构
**And** 系统在对话界面提示："上下文已切换到 [新文件名]"

**Given** 用户想删除某个文件
**When** 点击文件的"删除"按钮
**Then** 系统提示确认："确认删除 [文件名]？删除后无法恢复。"
**And** 用户确认后，文件从列表移除
**And** 向量库中该文件的向量数据清除
**And** 如果删除的是当前使用文件，提示用户选择新的当前文件

**Given** 用户上传同名文件（如"电商主站_DDL.sql"）
**When** 检测到同名文件
**Then** 系统提示："检测到同名文件 [文件名]，是否覆盖现有文件？"
**And** 提供选项："覆盖" 或 "保留两者（自动重命名为 [文件名]_v2）"
**And** 用户选择"覆盖"后，旧文件删除，新文件替代
**And** 用户选择"保留两者"后，新文件重命名保存

**Given** 用户管理多个项目的文件
**When** 文件列表包含多个项目
**Then** 可以添加"项目标签"功能（可选，未来增强）
**And** 当前 MVP 阶段通过文件名区分项目（如"电商主站_"、"小程序_"前缀）

**Given** 多文件管理功能已实现
**When** 验证独立性
**Then** 不同文件的向量库互不干扰
**And** 切换文件后 SQL 生成仅引用当前文件的表
**And** 删除文件不影响其他文件的功能

---

## Epic 3: 智能对话与 SQL 生成（含可解释性） 🤖

产品经理可以通过自然语言获得精准的 SQL 语句，并清楚了解生成依据，SQL 生成准确率达 100%。

### Story 3.1: 智能对话界面与意图识别

As a **产品经理**,
I want **在对话界面输入自然语言问题，系统智能识别是普通对话还是 SQL 生成需求**,
So that **可以自然交互，既能获得 SQL 也能获得帮助，无需手动切换模式**。

**Acceptance Criteria:**

**Given** 用户打开对话界面
**When** 查看界面布局
**Then** 对话区域包含：
**And** 消息显示区（顶部）：显示历史对话
**And** 输入框（底部）：单行或多行输入，自动调整高度
**And** 发送按钮：位于输入框右侧
**And** 清空按钮：清空对话历史（可选）
**And** 当前使用的 DDL 文件提示：显示在顶部或输入框上方（如"当前：电商主站_DDL.sql"）

**Given** 用户在输入框输入自然语言
**When** 输入框支持多行文本
**Then** 输入框随内容自动扩展（最多 5 行，超出则滚动）
**And** 支持 Shift + Enter 换行
**And** Enter 键发送消息
**And** 输入框有 placeholder 提示："输入您的需求，如：查询用户表中的所有活跃用户"

**Given** 用户输入消息并点击发送
**When** 消息发送到后端
**Then** 输入框清空
**And** 消息立即添加到对话区（用户消息，右对齐）
**And** 显示"AI 正在思考..."加载状态（左对齐，灰色背景）
**And** 后端执行意图识别

**Given** 后端收到用户消息
**When** 执行意图识别（使用 LLM 或规则）
**Then** 判断消息类型：
**And** **SQL 生成类**：包含查询意图的自然语言（如"查询..."、"统计..."、"找出..."、"我想看..."）
**And** **普通对话类**：问候、帮助请求、功能咨询等（如"你好"、"如何使用"、"支持什么功能"）
**And** 识别准确率 > 95%（基于测试用例验证）

**Given** 意图识别结果为 **SQL 生成类**
**When** 系统判定需要生成 SQL
**Then** 调用 SQL 生成流程（Story 3.2-3.4）
**And** 返回包含 SQL 和可解释性信息的响应
**And** 响应结构：
```json
{
  "type": "sql_generation",
  "sql": "SELECT * FROM users WHERE status = 'active'",
  "explanation": "基于用户表（users）生成查询语句",
  "references": [
    {"table": "users", "columns": ["status"], "ddl_snippet": "..."}
  ]
}
```

**Given** 意图识别结果为 **普通对话类**
**When** 系统判定为普通对话
**Then** 调用通用对话流程（基础 LLM 响应）
**And** 返回友好的自然语言回复
**And** 响应结构：
```json
{
  "type": "chat",
  "message": "您好！我是 SQL 生成助手，您可以用自然语言描述查询需求，我会为您生成精准的 SQL 语句。"
}
```

**Given** AI 响应返回
**When** 前端接收到响应
**Then** 移除"AI 正在思考..."加载状态
**And** 根据响应类型渲染消息：
**And** **SQL 生成类**：显示 SQL 代码块、复制按钮、可解释性信息（Story 3.4）
**And** **普通对话类**：显示普通文本消息
**And** 消息左对齐，浅灰色背景

**Given** 对话界面有历史消息
**When** 滚动查看历史
**Then** 消息按时间顺序排列（最新在下）
**And** 自动滚动到最新消息
**And** 每条消息显示时间戳（如"10:35"）

---

### Story 3.2: Agent 架构与 RAG 检索集成

As a **开发工程师**,
I want **搭建 LangChain + LangGraph 的 Agent 架构，集成向量检索工具**,
So that **Agent 可以自主决策何时检索向量库，为 SQL 生成提供精准的表结构上下文**。

**Acceptance Criteria:**

**Given** 项目已安装 LangChain 和 LangGraph
**When** 初始化 Agent 架构
**Then** 创建以下核心组件：
**And** **Agent Executor**：LangGraph 状态机，管理 Agent 执行流程
**And** **LLM**：配置 DeepSeek 或 OpenAI API（可配置切换）
**And** **Tools**：定义 Agent 可用的工具集
**And** **Memory**：可选的对话历史管理（基于内存）

**Given** Agent 需要查询表结构
**When** 定义向量检索工具（VectorSearchTool）
**Then** 工具包含以下能力：
**And** **输入参数**：查询文本（如"用户表"、"订单状态字段"）
**And** **执行逻辑**：
  - 将查询文本向量化（Embedding）
  - 在 FAISS/Chroma 向量库中执行相似度检索
  - 返回 Top-K 最相关的表和字段（K=5）
**And** **输出格式**：
```python
{
  "results": [
    {
      "table_name": "users",
      "schema": "public",
      "comment": "用户表",
      "columns": [
        {"name": "user_id", "type": "BIGINT", "comment": "用户ID"},
        {"name": "status", "type": "VARCHAR(20)", "comment": "用户状态"}
      ],
      "ddl_snippet": "CREATE TABLE users ..."
    }
  ]
}
```

**Given** Agent 工具集已定义
**When** 注册工具到 Agent
**Then** Agent 包含以下工具：
**And** **VectorSearchTool**：检索表结构（上述定义）
**And** **SQLValidatorTool**：验证 SQL 语法（Story 3.3）
**And** 未来可扩展其他工具（如 DatabaseQueryTool）

**Given** 用户输入 SQL 生成需求（如"查询所有活跃用户"）
**When** Agent 开始执行
**Then** Agent 执行流程：
**And** **Step 1: 理解需求**：解析用户意图，识别关键实体（如"用户"、"活跃"）
**And** **Step 2: 决策是否检索**：判断是否需要查询表结构
**And** **Step 3: 调用 VectorSearchTool**：如需要，自动调用向量检索
**And** **Step 4: 生成 SQL**：基于检索上下文生成 SQL
**And** **Step 5: 验证 SQL**：调用 SQLValidatorTool 验证语法
**And** **Step 6: 返回结果**：返回 SQL 和可解释性信息

**Given** Agent 执行向量检索
**When** 调用 VectorSearchTool
**Then** 检索结果包含相关表和字段
**And** 检索速度 < 500ms
**And** 返回的表结构完整（包含字段类型、注释）
**And** 如无匹配结果，返回空列表并提示 Agent

**Given** Agent 需要处理复杂查询（如多表关联）
**When** Agent 执行多次检索
**Then** Agent 可以迭代调用 VectorSearchTool
**And** 第一次检索"用户表"
**And** 第二次检索"订单表"
**And** 整合所有检索结果生成 SQL

**Given** Agent 架构已完成
**When** 测试 Agent 端到端流程
**Then** 给定输入："查询用户表中状态为活跃的用户"
**And** Agent 自动调用 VectorSearchTool 检索 users 表
**And** Agent 生成 SQL：`SELECT * FROM users WHERE status = 'active'`
**And** Agent 调用 SQLValidatorTool 验证语法通过
**And** Agent 返回完整响应（SQL + 引用信息）

**Given** Agent 架构的可观测性
**When** Agent 执行过程
**Then** 记录详细日志：
**And** Agent 每步决策（Thought）
**And** 工具调用及参数（Action、Action Input）
**And** 工具返回结果（Observation）
**And** 最终输出（Final Answer）
**And** 日志级别：DEBUG（用于开发调试）

---

### Story 3.3: SQL 生成与语法验证

As a **产品经理**,
I want **基于表结构上下文生成精准的 SQL，并自动验证语法正确性**,
So that **可以确保生成的 SQL 100% 可执行，不会因为语法错误导致失败**。

**Acceptance Criteria:**

**Given** Agent 已获取表结构上下文（通过 RAG 检索）
**When** Agent 执行 SQL 生成
**Then** 使用 LLM（DeepSeek 或 OpenAI）生成 SQL
**And** Prompt 包含以下要素：
**And** 用户原始需求（自然语言）
**And** 检索到的表结构（DDL 片段或结构化信息）
**And** 生成规则（如"使用标准 SQL"、"不使用 SELECT *"等）
**And** 输出格式要求（纯 SQL，无额外解释）

**Given** SQL 生成 Prompt 示例
**When** 构建 Prompt
**Then** Prompt 结构如下：
```
你是一个 SQL 生成专家。基于以下数据库表结构，将用户需求转换为精准的 SQL 语句。

**用户需求**：
查询用户表中状态为活跃的用户

**表结构**：
CREATE TABLE users (
  user_id BIGINT PRIMARY KEY COMMENT '用户ID',
  username VARCHAR(50) NOT NULL COMMENT '用户名',
  status VARCHAR(20) NOT NULL COMMENT '用户状态：active, inactive, banned',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
);

**生成规则**：
1. 使用标准 SQL 语法（兼容 MySQL/PostgreSQL）
2. 字段名和表名使用反引号或双引号
3. 字符串使用单引号
4. 不使用 SELECT *（明确指定字段）
5. 添加必要的注释说明

**输出格式**：
仅输出 SQL 语句，不包含其他解释。

**SQL 输出**：
```

**Given** LLM 返回生成的 SQL
**When** 接收 SQL 文本
**Then** SQL 文本格式化：
**And** 去除多余空格和换行
**And** 保留必要的缩进（如 WHERE、JOIN 子句）
**And** 确保 SQL 以分号结尾

**Given** SQL 生成完成
**When** 执行语法验证（使用 sqlparse 或 sqlglot）
**Then** 验证以下方面：
**And** **语法正确性**：SQL 可以被解析，无语法错误
**And** **引用正确性**：表名和字段名存在于当前 DDL 文件中
**And** **逻辑合理性**：WHERE 条件、JOIN 关系符合逻辑
**And** 验证通过返回 `{"valid": true}`
**And** 验证失败返回 `{"valid": false, "errors": ["第 3 行：字段 'xxx' 不存在"]}`

**Given** 语法验证失败
**When** 检测到错误
**Then** 系统自动重试生成（最多 2 次）
**And** 第一次重试：调整 Prompt，强调错误点
**And** 第二次重试：使用更简单的 SQL 模板
**And** 如仍失败，返回错误提示给用户："无法生成有效 SQL，请尝试调整需求描述"

**Given** SQL 生成成功且验证通过
**When** 返回 SQL
**Then** 响应包含：
**And** 生成的 SQL 文本
**And** 引用的表和字段列表（用于可解释性，Story 3.4）
**And** 生成时间戳

**Given** SQL 生成性能要求
**When** 测量生成时间
**Then** 单次 SQL 生成 < 3 秒
**And** 简单查询（单表、无 JOIN）< 1.5 秒
**And** 复杂查询（多表 JOIN、子查询）< 3 秒
**And** 如超时返回错误："生成超时，请稍后重试"

**Given** 生成 SQL 的准确性要求
**When** 基于测试用例验证
**Then** 准确率 = 100%（所有测试用例通过）
**And** 测试用例覆盖：
  - 单表查询（WHERE 条件）
  - 多表关联（JOIN）
  - 聚合函数（COUNT、SUM、AVG）
  - 排序和分页（ORDER BY、LIMIT）
  - 子查询
  - 复杂 WHERE 条件（AND、OR、IN）

---

### Story 3.4: 可解释性展示（引用源）

As a **产品经理**,
I want **在 SQL 生成后看到引用的表和字段，以及对应的 DDL 片段**,
So that **可以理解 SQL 是如何生成的，验证生成逻辑的正确性，建立对系统的信任**。

**Acceptance Criteria:**

**Given** SQL 生成成功
**When** 返回响应给前端
**Then** 响应包含 `references` 字段：
```json
{
  "type": "sql_generation",
  "sql": "SELECT user_id, username, status FROM users WHERE status = 'active'",
  "explanation": "查询用户表（users）中状态为活跃的用户信息",
  "references": [
    {
      "table_name": "users",
      "schema": "public",
      "comment": "用户表",
      "columns_used": [
        {"name": "user_id", "type": "BIGINT", "comment": "用户ID"},
        {"name": "username", "type": "VARCHAR(50)", "comment": "用户名"},
        {"name": "status", "type": "VARCHAR(20)", "comment": "用户状态"}
      ],
      "ddl_snippet": "CREATE TABLE users (\n  user_id BIGINT PRIMARY KEY COMMENT '用户ID',\n  username VARCHAR(50) NOT NULL COMMENT '用户名',\n  status VARCHAR(20) NOT NULL COMMENT '用户状态：active, inactive, banned'\n);"
    }
  ]
}
```

**Given** 前端接收到响应
**When** 渲染可解释性信息
**Then** 在 SQL 代码块下方显示"引用信息"区域：
**And** 标题："📚 引用的表和字段"
**And** 对每个引用的表：
  - 表名（加粗）+ schema（如 `public.users`）
  - 表注释（如"用户表"）
  - 使用的字段列表（字段名、类型、注释）
  - "查看完整 DDL"展开按钮

**Given** 用户点击"查看完整 DDL"
**When** 展开 DDL 片段
**Then** 显示格式化的 DDL 代码块
**And** 使用 SQL 语法高亮
**And** 高亮引用的字段（如加粗或背景色）
**And** DDL 可折叠（再次点击隐藏）

**Given** SQL 引用多个表（如 JOIN 查询）
**When** 渲染引用信息
**Then** 按表分组显示
**And** 每个表独立展示
**And** 表之间有分隔线

**Given** 可解释性信息的格式化
**When** 显示字段列表
**Then** 使用表格布局：
| 字段名     | 类型          | 说明     |
|-----------|--------------|---------|
| user_id   | BIGINT       | 用户ID   |
| username  | VARCHAR(50)  | 用户名   |
| status    | VARCHAR(20)  | 用户状态 |

**Given** 用户需要验证生成逻辑
**When** 查看引用信息
**Then** 可以快速确认：
**And** SQL 中使用的表是否正确
**And** 字段名是否与 DDL 一致
**And** 字段类型是否合理
**And** 是否遗漏必要字段

**Given** 可解释性展示的性能
**When** 渲染引用信息
**Then** 信息加载无延迟（<100ms）
**And** DDL 片段折叠/展开流畅（<50ms）

---

### Story 3.5: SQL 复制与交互优化

As a **产品经理**,
I want **快速复制生成的 SQL，并拥有流畅的对话体验**,
So that **可以高效使用生成的 SQL，减少手动操作，提升工作效率**。

**Acceptance Criteria:**

**Given** SQL 生成成功并显示
**When** 查看 SQL 代码块
**Then** 代码块右上角有"复制"按钮（📋 图标）
**And** 按钮位置明显，易于点击
**And** 代码使用语法高亮（SQL 语法）

**Given** 用户点击"复制"按钮
**When** 执行复制操作
**Then** SQL 文本复制到剪贴板
**And** 按钮图标变为"✓ 已复制"（1.5 秒后恢复）
**And** 显示轻提示："SQL 已复制到剪贴板"
**And** 复制的文本格式化（保留换行和缩进）

**Given** 用户需要多次生成 SQL
**When** 连续发送多条消息
**Then** 每条消息的 SQL 独立显示
**And** 历史消息保留在对话区
**And** 可以滚动查看历史 SQL
**And** 每条历史 SQL 都可以独立复制

**Given** 对话历史较长
**When** 查看历史消息
**Then** 实现虚拟滚动（> 50 条消息时）
**And** 滚动流畅无卡顿
**And** 自动滚动到最新消息
**And** 可以手动滚动查看历史

**Given** 用户想清空对话历史
**When** 点击"清空对话"按钮（顶部工具栏）
**Then** 系统提示确认："确认清空对话历史？此操作无法撤销。"
**And** 用户确认后，所有消息清空
**And** 输入框聚焦，提示用户输入新需求

**Given** 系统正在处理请求
**When** 显示加载状态
**Then** 显示"AI 正在思考..."（带动画点点点）
**And** 禁用输入框和发送按钮（防止重复提交）
**And** 用户可以点击"取消"按钮终止请求（可选）

**Given** 请求失败（如网络错误、API 错误）
**When** 捕获错误
**Then** 显示友好的错误提示：
**And** "生成失败：网络错误，请检查网络连接"
**And** "生成失败：API 调用超时，请稍后重试"
**And** "生成失败：当前 DDL 文件未解析完成，请等待解析"
**And** 提供"重试"按钮

**Given** 用户体验优化
**When** 使用对话界面
**Then** 输入框自动聚焦
**And** 发送按钮支持快捷键（Enter）
**And** 历史消息按时间排序
**And** 消息显示时间戳（如"10:35"）
**And** 支持键盘导航（Tab 切换焦点）

**Given** 响应式设计
**When** 在不同屏幕尺寸使用
**Then** 桌面端（> 1024px）：三列布局（文件管理、对话、DDL 预览）
**And** 平板端（768-1024px）：两列布局（对话 + 文件管理）
**And** 移动端（< 768px）：单列布局（对话优先，文件管理折叠）
**And** 所有按钮和交互元素易于点击（最小 44x44px）

---

## Epic 4: 多维度质量保障 🛡️

开发团队可以确保生成的 SQL 质量可靠，输出一致，错误可控，为用户提供稳定的服务。

### Story 4.1: 多层 SQL 验证机制

As a **开发工程师**,
I want **实现多层 SQL 验证（语法、引用、逻辑），并对验证失败进行分级处理**,
So that **可以确保生成的 SQL 100% 可执行，拦截所有不合法的 SQL，保障系统质量**。

**Acceptance Criteria:**

**Given** SQL 生成后进入验证流程
**When** 执行第一层验证：语法验证
**Then** 使用 SQL Parser（sqlparse 或 sqlglot）解析 SQL
**And** 验证 SQL 可以成功解析（无语法错误）
**And** 验证 SQL 关键字拼写正确（SELECT、FROM、WHERE 等）
**And** 验证括号、引号配对正确
**And** 验证分号位置正确
**And** 如验证失败，记录错误："语法错误：第 X 行，XXXXX"

**Given** 语法验证通过
**When** 执行第二层验证：引用验证
**Then** 提取 SQL 中的所有表名和字段名
**And** 对比当前使用的 DDL 文件中的表和字段
**And** 验证所有表名存在于 DDL 中
**And** 验证所有字段名存在于对应表中
**And** 验证字段类型与 SQL 操作匹配（如 WHERE 条件中的字段类型）
**And** 如验证失败，记录错误："引用错误：表 'xxx' 不存在" 或 "字段 'yyy' 不存在于表 'xxx' 中"

**Given** 引用验证通过
**When** 执行第三层验证：逻辑验证
**Then** 验证 JOIN 条件合理性（关联字段类型兼容）
**And** 验证 WHERE 条件的字段与表的关联正确
**And** 验证聚合函数（COUNT、SUM、AVG）的使用合理
**And** 验证 GROUP BY 与 SELECT 字段的一致性
**And** 验证 ORDER BY 字段存在于 SELECT 或表中
**And** 如验证失败，记录警告："逻辑警告：XXXXX"

**Given** 验证失败（任一层）
**When** 进行分级处理
**Then** 根据错误级别决策：
**And** **严重错误（Critical）**：语法错误、表/字段不存在 → 拒绝 SQL，返回错误给用户
**And** **警告（Warning）**：逻辑不合理但不影响执行 → 返回 SQL 但附带警告提示
**And** **信息（Info）**：性能建议等 → 返回 SQL 和优化建议

**Given** 验证失败触发重试
**When** 错误级别为 Critical
**Then** 自动触发 SQL 重新生成（最多 2 次）
**And** 第一次重试：调整 Prompt，强调错误点（如"表名必须来自：users, orders, products"）
**And** 第二次重试：使用更严格的生成规则（如"仅使用 SELECT、WHERE，禁止 JOIN"）
**And** 如仍失败，返回错误："无法生成有效 SQL，请尝试调整需求或检查 DDL 文件"

**Given** 验证流程的性能要求
**When** 测量验证时间
**Then** 三层验证总时间 < 500ms
**And** 语法验证 < 100ms
**And** 引用验证 < 200ms
**And** 逻辑验证 < 200ms

**Given** 验证结果记录
**When** 验证完成
**Then** 记录验证日志：
**And** SQL 文本
**And** 验证结果（通过/失败）
**And** 错误/警告列表
**And** 验证耗时
**And** 日志级别：INFO（通过）、WARN（有警告）、ERROR（失败）

---

### Story 4.2: 输出一致性保障

As a **产品经理和开发工程师**,
I want **相同的输入生成相同的 SQL，确保输出的确定性和稳定性**,
So that **可以信任系统的输出，复现问题时有稳定的基线，用户体验一致**。

**Acceptance Criteria:**

**Given** 用户输入相同的自然语言需求
**When** 多次执行 SQL 生成
**Then** 生成的 SQL 完全一致（字符级别相同）
**And** 测试用例：输入"查询用户表中的活跃用户" 3 次
**And** 3 次生成的 SQL 完全相同：`SELECT user_id, username, status FROM users WHERE status = 'active'`

**Given** LLM 的非确定性
**When** 配置 LLM 参数
**Then** 设置 `temperature=0`（确定性输出）
**And** 设置 `top_p=1.0`
**And** 禁用 `random_seed`（或固定 seed 用于测试）
**And** 配置在代码或环境变量中可调整

**Given** 向量检索的稳定性
**When** 执行向量相似度检索
**Then** 相同查询文本返回相同的 Top-K 结果
**And** 向量库不变时，检索结果顺序稳定
**And** 如向量库更新（如新增 DDL 文件），检索结果可能变化（这是合理的）

**Given** Agent 执行流程的确定性
**When** Agent 处理相同输入
**Then** Agent 执行路径一致：
**And** 相同的工具调用顺序
**And** 相同的检索次数
**And** 相同的生成 Prompt
**And** 相同的验证流程

**Given** 时间戳和随机因素的处理
**When** 生成 SQL 中包含时间相关逻辑
**Then** 不在 SQL 中硬编码时间戳（如 `WHERE created_at > '2026-01-24'`）
**And** 使用相对时间（如 `WHERE created_at > DATE_SUB(NOW(), INTERVAL 7 DAY)`）
**And** 或使用参数化（如 `WHERE created_at > ?`）

**Given** 输出一致性的测试验证
**When** 执行一致性测试
**Then** 测试用例集：10 个不同的自然语言需求
**And** 每个需求执行 5 次生成
**And** 计算一致性比例：相同输出次数 / 总次数
**And** 一致性比例 ≥ 100%（完全一致）

**Given** 不一致的边界情况
**When** 出现合理的不一致
**Then** 允许以下情况（不视为错误）：
**And** 字段顺序不同但语义相同（如 `SELECT a, b` vs `SELECT b, a`）
**And** 语法等价的不同写法（如 `!=` vs `<>`）
**And** 这些情况在测试中标记为"等价输出"

**Given** 一致性问题的调试
**When** 检测到不一致
**Then** 记录详细日志：
**And** 输入文本（用户需求）
**And** 两次生成的 SQL 差异（diff）
**And** LLM 配置参数
**And** Agent 执行路径差异
**And** 便于开发者快速定位问题

---

### Story 4.3: 友好错误处理与提示

As a **产品经理**,
I want **在系统出错时获得清晰、友好、可操作的错误提示**,
So that **可以快速理解问题原因，知道如何修正，减少挫败感，提升用户体验**。

**Acceptance Criteria:**

**Given** 系统可能发生的错误类型
**When** 定义错误分类
**Then** 错误分为以下类别：
**And** **用户输入错误**：需求描述不清、DDL 文件格式错误
**And** **系统内部错误**：SQL 生成失败、向量检索失败、API 调用失败
**And** **资源不可用**：DDL 文件未解析、向量库未初始化、网络错误
**And** **性能超时**：生成超时、解析超时

**Given** 用户输入错误：需求描述不清
**When** LLM 无法理解用户需求
**Then** 返回友好提示："抱歉，我无法理解您的需求。请尝试更具体地描述，例如：'查询用户表中注册时间在最近 7 天的用户'"
**And** 提供示例需求帮助用户改进

**Given** 用户输入错误：DDL 文件格式错误
**When** 上传的 DDL 文件解析失败
**Then** 返回友好提示："DDL 文件解析失败：第 45 行存在语法错误。请检查文件格式是否正确，或尝试使用标准的 CREATE TABLE 语句。"
**And** 指出具体错误位置
**And** 提供修正建议

**Given** 系统内部错误：SQL 生成失败
**When** 多次重试仍无法生成有效 SQL
**Then** 返回友好提示："暂时无法生成 SQL，可能的原因：当前需求较复杂，或 DDL 文件信息不足。建议：1) 简化需求描述；2) 检查 DDL 文件是否包含所需表和字段。"
**And** 提供具体的改进建议

**Given** 系统内部错误：向量检索失败
**When** 向量库查询异常
**Then** 返回友好提示："知识检索失败，可能是系统繁忙。请稍后重试。"
**And** 记录错误日志（供开发排查）
**And** 不暴露技术细节给用户

**Given** 系统内部错误：API 调用失败
**When** LLM API 返回错误（如 429 Rate Limit、500 Internal Error）
**Then** 根据错误类型返回友好提示：
**And** 429：请求过于频繁，请稍后再试（1 分钟后重试）
**And** 500：服务暂时不可用，请稍后重试
**And** 401：API 密钥配置错误，请联系管理员
**And** 网络超时：网络连接超时，请检查网络或稍后重试

**Given** 资源不可用：DDL 文件未解析
**When** 用户在 DDL 解析完成前尝试生成 SQL
**Then** 返回友好提示："当前 DDL 文件正在解析中（预计还需 2 秒），请稍后再试。"
**And** 显示解析进度（如果可用）
**And** 建议用户等待解析完成

**Given** 资源不可用：向量库未初始化
**When** 系统启动后向量库尚未加载
**Then** 返回友好提示："系统正在初始化，请稍后再试（预计 30 秒）。"
**And** 显示健康检查状态（`/health` endpoint）

**Given** 性能超时：生成超时
**When** SQL 生成超过 5 秒
**Then** 返回友好提示："生成超时，当前需求较复杂。建议：1) 简化需求；2) 稍后重试。"
**And** 后台继续完成生成（如果可能）
**And** 记录超时日志

**Given** 错误提示的降级策略
**When** 无法生成 SQL
**Then** 提供备选方案：
**And** 返回普通对话响应："我理解您想查询用户表中的活跃用户。我暂时无法生成 SQL，但您可以尝试手动编写：`SELECT * FROM users WHERE status = 'active'`"
**And** 或提供 SQL 模板供用户参考

**Given** 错误处理的可观测性
**When** 发生错误
**Then** 记录详细错误日志：
**And** 错误类型和级别
**And** 错误堆栈和上下文
**And** 用户输入和系统状态
**And** 时间戳和会话 ID
**And** 日志级别：ERROR（需立即关注）

---

### Story 4.4: 单元测试与集成测试

As a **开发工程师**,
I want **建立完善的单元测试和集成测试体系**,
So that **可以确保代码质量，快速发现回归问题，支持持续迭代和重构**。

**Acceptance Criteria:**

**Given** 核心功能需要单元测试
**When** 定义测试覆盖范围
**Then** 单元测试覆盖以下模块：
**And** **DDL 解析模块**：测试各种 DDL 语法的解析正确性
**And** **向量化模块**：测试 Embedding 生成和向量库存储
**And** **向量检索模块**：测试相似度检索的准确性
**And** **SQL 验证模块**：测试三层验证逻辑
**And** **Agent 工具模块**：测试每个工具的独立功能
**And** **错误处理模块**：测试各类错误的捕获和提示

**Given** 单元测试的覆盖率要求
**When** 运行测试覆盖率工具（如 pytest-cov）
**Then** 代码覆盖率 ≥ 80%
**And** 核心模块（DDL 解析、SQL 生成、验证）覆盖率 ≥ 90%
**And** 未覆盖的代码有明确注释说明原因

**Given** 单元测试的测试用例设计
**When** 编写测试用例
**Then** 每个模块包含：
**And** **正常场景**：标准输入，验证正确输出
**And** **边界场景**：空输入、最大输入、极端值
**And** **异常场景**：非法输入、错误格式、系统异常
**And** 测试用例命名清晰（如 `test_parse_ddl_with_foreign_keys`）

**Given** DDL 解析模块的单元测试
**When** 测试 DDL 解析功能
**Then** 测试用例包含：
**And** 简单表结构（单表、无外键）
**And** 复杂表结构（多字段、主键、外键、索引、注释）
**And** 多表 DDL（多个 CREATE TABLE）
**And** 边界情况（空文件、仅注释、无分号）
**And** 异常情况（语法错误、不支持的 DDL）

**Given** SQL 验证模块的单元测试
**When** 测试验证功能
**Then** 测试用例包含：
**And** 语法验证：正确 SQL、语法错误 SQL
**And** 引用验证：表存在、表不存在、字段存在、字段不存在
**And** 逻辑验证：JOIN 正确、GROUP BY 一致、ORDER BY 合法
**And** 验证每层的错误消息准确性

**Given** 向量检索模块的单元测试
**When** 测试检索功能
**Then** 测试用例包含：
**And** 单表检索：查询"用户表"返回 users
**And** 字段检索：查询"用户状态字段"返回 users.status
**And** 相似度检索：查询"订单信息"返回 orders 和相关表
**And** Top-K 结果准确性：验证返回数量和排序

**Given** 集成测试的端到端场景
**When** 定义集成测试用例
**Then** 测试完整流程：
**And** **Test E2E-1**：上传 DDL → 解析成功 → 向量化完成 → 生成 SQL → SQL 验证通过
**And** **Test E2E-2**：上传多个 DDL → 切换文件 → 生成 SQL（使用正确文件的表）
**And** **Test E2E-3**：上传错误 DDL → 解析失败 → 显示友好错误提示
**And** **Test E2E-4**：生成复杂 SQL（多表 JOIN）→ 验证通过 → 可解释性正确

**Given** 集成测试的性能验证
**When** 执行性能测试
**Then** 验证以下性能指标：
**And** DDL 解析时间 < 5 秒（23 张表）
**And** SQL 生成时间 < 3 秒
**And** 向量检索时间 < 500ms
**And** SQL 验证时间 < 500ms
**And** 端到端流程时间 < 10 秒

**Given** 测试自动化
**When** 配置 CI/CD 流程
**Then** 每次代码提交触发自动测试
**And** 运行所有单元测试和集成测试
**And** 生成测试报告（通过/失败、覆盖率）
**And** 测试失败阻止代码合并

**Given** 测试文档和维护
**When** 编写测试代码
**Then** 每个测试用例有清晰注释：
**And** 测试目的（What）
**And** 测试场景（Given-When-Then）
**And** 预期结果
**And** 测试数据来源
**And** 便于其他开发者理解和维护

---

## Epic 5: 前端用户体验优化 🎨

产品经理享受流畅、美观、易用的前端界面，提升工作效率和满意度。

### Story 5.1: 响应式布局与设备适配

As a **产品经理**,
I want **在不同设备上都能流畅使用系统**,
So that **无论使用台式机、笔记本、平板还是手机，都能高效完成工作，不受设备限制**。

**Acceptance Criteria:**

**Given** 桌面端（屏幕宽度 > 1024px）
**When** 打开应用
**Then** 显示三列布局：
**And** **左侧列**：文件管理区（宽度 300px，固定）
  - 文件列表
  - 上传按钮
  - 文件搜索
**And** **中间列**：对话区（宽度自适应，占主要空间）
  - 消息显示区
  - 输入框
  - 当前文件提示
**And** **右侧列**：DDL 预览区（宽度 400px，可调整）
  - 当前文件的 DDL 内容
  - 表结构树形展示
  - 可折叠/展开

**Given** 平板端（屏幕宽度 768-1024px）
**When** 打开应用
**Then** 显示两列布局：
**And** **左侧列**：对话区（宽度 60%）
**And** **右侧列**：文件管理区（宽度 40%，可折叠）
**And** DDL 预览区隐藏，通过点击文件名弹出查看

**Given** 移动端（屏幕宽度 < 768px）
**When** 打开应用
**Then** 显示单列布局：
**And** 默认显示对话区（全屏）
**And** 左上角菜单按钮打开文件管理抽屉
**And** 点击文件名弹出 DDL 预览底部抽屉
**And** 所有交互针对触摸优化（按钮最小 44x44px）

**Given** 响应式布局的动态调整
**When** 用户调整浏览器窗口大小
**Then** 布局自动切换（无刷新）
**And** 断点切换流畅（CSS transition）
**And** 内容重排不影响当前操作（如输入框内容保留）

**Given** 文件列表在不同设备的展示
**When** 查看文件列表
**Then** 桌面端：显示完整信息（文件名、时间、大小、状态、表数量）
**And** 平板端：显示核心信息（文件名、状态、表数量）
**And** 移动端：显示精简信息（文件名、状态图标）

**Given** 对话界面在不同设备的交互
**When** 使用对话功能
**Then** 桌面端：Enter 发送，Shift+Enter 换行
**And** 平板端：Enter 发送，Shift+Enter 换行
**And** 移动端：显示"发送"按钮，支持触摸发送

**Given** DDL 预览区的响应式行为
**When** 在不同设备查看 DDL
**Then** 桌面端：固定右侧列，始终可见
**And** 平板端：点击文件名弹出模态框查看
**And** 移动端：底部抽屉滑出，支持滑动关闭

**Given** 响应式设计的性能要求
**When** 测试不同设备
**Then** 布局切换无卡顿（< 300ms）
**And** 动画流畅（60fps）
**And** 移动端流量消耗优化（懒加载、图片优化）

---

### Story 5.2: 视觉设计与主题系统

As a **产品经理**,
I want **使用美观的界面和可切换的深色/浅色主题**,
So that **可以根据环境和个人偏好选择舒适的视觉风格，长时间使用不疲劳**。

**Acceptance Criteria:**

**Given** 设计系统的定义
**When** 建立设计规范
**Then** 定义以下设计 token：
**And** **颜色系统**：
  - 主色（Primary）：蓝色系（#1890ff）
  - 成功色（Success）：绿色系（#52c41a）
  - 警告色（Warning）：橙色系（#faad14）
  - 错误色（Error）：红色系（#f5222d）
  - 中性色（Neutral）：灰色阶梯（#000 - #fff，8 级）
**And** **字体系统**：
  - 主字体：-apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif
  - 代码字体：'Fira Code', 'Consolas', monospace
  - 字号阶梯：12px, 14px, 16px, 18px, 20px, 24px
**And** **间距系统**：4px 倍数（4, 8, 12, 16, 24, 32, 48, 64）
**And** **圆角系统**：2px, 4px, 8px, 12px
**And** **阴影系统**：小、中、大三级

**Given** 浅色主题（默认）
**When** 应用浅色主题
**Then** 颜色配置：
**And** 背景色：#ffffff（主背景）、#f5f5f5（次级背景）
**And** 文本色：#262626（主文本）、#595959（次级文本）、#8c8c8c（辅助文本）
**And** 边框色：#d9d9d9
**And** 用户消息背景：#e6f7ff（浅蓝）
**And** AI 消息背景：#fafafa（浅灰）
**And** 代码块背景：#f5f5f5（浅灰）

**Given** 深色主题
**When** 应用深色主题
**Then** 颜色配置：
**And** 背景色：#141414（主背景）、#1f1f1f（次级背景）
**And** 文本色：#e8e8e8（主文本）、#a6a6a6（次级文本）、#737373（辅助文本）
**And** 边框色：#434343
**And** 用户消息背景：#111a2c（深蓝）
**And** AI 消息背景：#1f1f1f（深灰）
**And** 代码块背景：#1f1f1f（深灰）

**Given** 主题切换功能
**When** 用户点击主题切换按钮（顶部工具栏）
**Then** 主题在浅色/深色之间切换
**And** 切换动画流畅（CSS transition 200ms）
**And** 主题偏好保存到 localStorage
**And** 下次打开应用自动应用上次选择的主题
**And** 支持跟随系统主题（可选，未来增强）

**Given** SQL 代码块的语法高亮
**When** 显示 SQL 代码
**Then** 使用 Highlight.js 或 Prism.js
**And** 关键字高亮（SELECT、FROM、WHERE）：主色
**And** 字符串高亮（'active'）：成功色
**And** 数字高亮（123）：警告色
**And** 注释高亮（-- comment）：辅助文本色
**And** 浅色/深色主题自动切换语法高亮配色

**Given** 视觉设计的一致性
**When** 审查界面元素
**Then** 所有按钮遵循统一样式（圆角、阴影、hover 效果）
**And** 所有输入框样式一致（高度 40px、边框 1px、圆角 4px）
**And** 所有图标使用统一图标库（如 Ant Design Icons）
**And** 所有间距遵循 4px 倍数规则

**Given** 情感化设计
**When** 用户完成操作
**Then** 提供视觉反馈：
**And** 上传成功：绿色对勾 + "✓ 上传成功"
**And** 解析完成：蓝色进度条 + "✓ 解析完成"
**And** SQL 复制：灰色图标变为蓝色 + "✓ 已复制"
**And** 错误提示：红色叹号 + 友好错误信息

**Given** 视觉设计的可访问性
**When** 验证颜色对比度
**Then** 文本与背景对比度 ≥ 4.5:1（WCAG AA 标准）
**And** 重要文本（标题）对比度 ≥ 7:1（WCAG AAA 标准）
**And** 使用工具验证（如 WebAIM Contrast Checker）

---

### Story 5.3: 交互反馈与加载状态

As a **产品经理**,
I want **在操作过程中获得清晰的实时反馈和加载状态提示**,
So that **可以了解系统正在做什么，预期需要等待多久，避免焦虑和重复操作**。

**Acceptance Criteria:**

**Given** 文件上传的加载状态
**When** 用户上传文件
**Then** 显示上传进度：
**And** 进度条（0-100%）
**And** 百分比数字（如"75%"）
**And** 状态文本（"正在上传 example.sql..."）
**And** 预估剩余时间（如"大约还需 2 秒"）
**And** 上传完成后显示"✓ 上传成功"（1.5 秒后自动消失）

**Given** DDL 解析的加载状态
**When** 文件开始解析
**Then** 显示解析进度：
**And** 旋转加载图标（animated spinner）
**And** 状态文本："正在解析，大约需要 3-5 秒"
**And** 实时更新状态（如"正在解析表结构..."、"正在向量化..."）
**And** 解析完成后显示"✓ 解析完成：23 张表，156 个字段"

**Given** SQL 生成的加载状态
**When** 用户发送消息等待 SQL 生成
**Then** 显示思考动画：
**And** "AI 正在思考..."文本
**And** 动态点点点动画（... 循环）
**And** 旋转加载图标（左侧）
**And** 预估时间："通常需要 2-3 秒"
**And** 禁用输入框和发送按钮（防止重复提交）

**Given** SQL 生成完成
**When** 响应返回
**Then** 移除加载状态
**And** 流畅展示 SQL 代码块（淡入动画）
**And** 展示可解释性信息（依次展开）
**And** 自动滚动到最新消息

**Given** 按钮的交互反馈
**When** 用户点击按钮
**Then** 提供视觉反馈：
**And** Hover 状态：背景色变深 10%
**And** Active 状态：背景色变深 20% + 轻微缩放（scale 0.98）
**And** Disabled 状态：灰色 + 鼠标指针 not-allowed
**And** Loading 状态：显示加载图标，禁用点击

**Given** 复制操作的反馈
**When** 用户点击"复制"按钮
**Then** 提供即时反馈：
**And** 按钮图标从 📋 变为 ✓
**And** 按钮文本变为"已复制"
**And** 显示轻提示（Toast）："SQL 已复制到剪贴板"
**And** 1.5 秒后按钮恢复原状

**Given** 错误操作的反馈
**When** 操作失败（如上传失败、生成失败）
**Then** 显示错误提示：
**And** 红色错误图标（❌）
**And** 友好错误信息（Story 4.3 定义）
**And** "重试"按钮（如果可重试）
**And** 错误提示可手动关闭（× 按钮）

**Given** 成功操作的反馈
**When** 操作成功（如切换文件、删除文件）
**Then** 显示成功提示：
**And** 绿色对勾图标（✓）
**And** 简短成功信息（如"✓ 文件已删除"）
**And** Toast 提示 2 秒后自动消失
**And** 页面状态自动更新（如文件列表刷新）

**Given** 长时间操作的预期管理
**When** 操作耗时较长（> 5 秒）
**Then** 提供额外反馈：
**And** 更新进度信息（如"正在处理第 15/23 张表"）
**And** 提供"取消"按钮（如果支持取消）
**And** 避免用户焦虑和重复操作

**Given** 网络请求的状态管理
**When** 发起 API 请求
**Then** 统一的请求状态管理：
**And** Loading 状态：显示加载动画
**And** Success 状态：更新 UI，显示成功反馈
**And** Error 状态：显示错误提示，保留上次数据
**And** 防抖处理：避免重复请求（500ms 延迟）

---

### Story 5.4: 键盘快捷键与无障碍

As a **产品经理**,
I want **使用键盘快捷键快速完成常用操作，并确保基础无障碍支持**,
So that **可以提升工作效率，同时让更多用户（包括键盘用户、屏幕阅读器用户）能顺利使用系统**。

**Acceptance Criteria:**

**Given** 键盘快捷键的定义
**When** 用户按下快捷键
**Then** 执行对应操作：
**And** **Ctrl/Cmd + Enter**：发送消息
**And** **Ctrl/Cmd + K**：聚焦输入框
**And** **Ctrl/Cmd + U**：打开文件上传对话框
**And** **Ctrl/Cmd + L**：清空对话历史（需确认）
**And** **Ctrl/Cmd + /**：显示快捷键帮助
**And** **Esc**：关闭模态框或抽屉

**Given** 快捷键的提示
**When** 用户首次使用系统
**Then** 显示快捷键帮助提示（可关闭）
**And** 右下角显示"？"按钮，点击查看完整快捷键列表
**And** 快捷键列表以模态框展示，分类清晰

**Given** 键盘导航支持
**When** 用户使用 Tab 键导航
**Then** 焦点按逻辑顺序移动：
**And** 文件管理区 → 对话输入框 → 发送按钮 → SQL 复制按钮 → ...
**And** 焦点有明显视觉指示（蓝色边框 2px）
**And** Shift + Tab 反向导航
**And** 所有可交互元素可通过键盘访问（无"键盘陷阱"）

**Given** Enter 键的行为
**When** 用户在输入框按下 Enter
**Then** 默认行为：发送消息
**And** Shift + Enter：换行
**And** 在模态框中 Enter：确认操作
**And** 在文件列表中 Enter：使用该文件

**Given** 无障碍标签（ARIA）
**When** 屏幕阅读器访问页面
**Then** 关键元素包含 ARIA 标签：
**And** 文件上传按钮：`aria-label="上传 DDL 文件"`
**And** 输入框：`aria-label="输入您的需求"`
**And** 发送按钮：`aria-label="发送消息"`
**And** 加载状态：`aria-live="polite" aria-label="正在生成 SQL"`
**And** 错误提示：`role="alert" aria-live="assertive"`

**Given** 语义化 HTML
**When** 审查页面结构
**Then** 使用语义化标签：
**And** `<header>` 用于顶部导航
**And** `<main>` 用于主内容区
**And** `<aside>` 用于侧边栏
**And** `<button>` 用于所有按钮（非 `<div>` 模拟）
**And** `<form>` 用于输入表单
**And** 标题层级正确（`<h1>` → `<h2>` → `<h3>`）

**Given** 颜色对比度（无障碍要求）
**When** 验证颜色对比
**Then** 所有文本与背景对比度 ≥ 4.5:1（WCAG 2.1 Level AA）
**And** 大文本（18px+）对比度 ≥ 3:1
**And** 按钮和交互元素的焦点指示清晰可见

**Given** 表单标签和说明
**When** 显示表单元素
**Then** 每个输入框有关联的 `<label>`
**And** 必填字段标记 `*` 或 `aria-required="true"`
**And** 错误提示与输入框关联（`aria-describedby`）
**And** 占位符文本（placeholder）仅用于示例，非必要说明

**Given** 焦点管理
**When** 打开模态框或抽屉
**Then** 焦点自动移动到模态框内第一个可交互元素
**And** 关闭模态框后，焦点返回触发元素
**And** 模态框打开时，背景内容不可通过键盘访问（`inert` 或焦点陷阱）

**Given** 屏幕阅读器测试
**When** 使用屏幕阅读器（如 NVDA、JAWS、VoiceOver）测试
**Then** 所有关键功能可通过屏幕阅读器访问
**And** 页面结构和导航清晰
**And** 动态内容更新（如 SQL 生成完成）会被朗读
**And** 无冗余或困惑的提示
