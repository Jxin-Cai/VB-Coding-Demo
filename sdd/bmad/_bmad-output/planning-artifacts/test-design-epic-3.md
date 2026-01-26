# Test Design: Epic 3 - 智能对话与 SQL 生成（含可解释性） 🤖

**Date:** 2026-01-25
**Author:** Jxin
**Status:** Draft

---

## Executive Summary

**Scope:** 完整测试设计 - Epic 3（智能对话与 SQL 生成 - 核心价值）

**Risk Summary:**

- Total risks identified: 18
- High-priority risks (≥6): 7
- **CRITICAL risks (=9): 2** 🚨
- Critical categories: BUS, TECH, PERF, DATA

**Coverage Summary:**

- P0 scenarios: 15 (30 hours)
- P1 scenarios: 18 (18 hours)
- P2/P3 scenarios: 14 (6 hours)
- **Total effort**: 54 hours (~7 days)

**⚠️ 注意**: 这是系统的核心 Epic，风险最高，测试投入最大。

---

## Risk Assessment

### CRITICAL Risks (Score = 9) 🚨

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner | Timeline |
| ------- | -------- | ----------- | ----------- | ------ | ----- | ---------- | ----- | -------- |
| R-301 | BUS | **SQL 生成准确率 < 100% 导致用户不信任系统** | 3 | 3 | 9 | 多层验证 + 大规模测试集 + Few-shot 优化 | Dev + QA | Sprint 2-3 |
| R-302 | PERF | **SQL 生成超过 3 秒导致用户焦虑和放弃使用** | 3 | 3 | 9 | 性能优化 + 缓存策略 + 压力测试 | Dev | Sprint 2-3 |

### High-Priority Risks (Score ≥6)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner | Timeline |
| ------- | -------- | ----------- | ----------- | ------ | ----- | ---------- | ----- | -------- |
| R-303 | TECH | Agent 决策失误不调用向量检索导致 SQL 错误 | 2 | 3 | 6 | Agent 决策测试 + 日志追踪 | Dev | Sprint 2 |
| R-304 | DATA | 向量检索召回不相关的表导致 SQL 逻辑错误 | 2 | 3 | 6 | 召回相关性测试（≥85%） | Dev | Sprint 2 |
| R-305 | TECH | 意图识别错误导致模式切换不正确 | 2 | 3 | 6 | 意图分类测试（>95% 准确率） | Dev | Sprint 2 |
| R-306 | BUS | 可解释性信息缺失导致用户无法验证 SQL | 2 | 3 | 6 | 引用源展示测试（100% 覆盖） | Dev + QA | Sprint 2-3 |
| R-307 | TECH | 复杂查询（多表 JOIN）生成失败 | 2 | 3 | 6 | 复杂查询测试集 | Dev | Sprint 3 |

### Medium-Priority Risks (Score 3-4)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner |
| ------- | -------- | ----------- | ----------- | ------ | ----- | ---------- | ----- |
| R-308 | TECH | 普通对话响应不够友好 | 2 | 2 | 4 | 对话测试 | Dev |
| R-309 | TECH | SQL 复制功能失败 | 1 | 3 | 3 | 复制功能测试 | QA |
| R-310 | TECH | 对话历史显示异常 | 1 | 2 | 2 | UI 测试 | QA |
| R-311 | PERF | 向量检索性能 > 1 秒 | 2 | 2 | 4 | 检索性能测试 | Dev |
| R-312 | DATA | 引用信息格式化错误 | 1 | 2 | 2 | 格式化测试 | Dev |

### Low-Priority Risks (Score 1-2)

| Risk ID | Category | Description | Probability | Impact | Score | Action |
| ------- | -------- | ----------- | ----------- | ------ | ----- | ------ |
| R-313 | TECH | 加载状态动画不流畅 | 1 | 1 | 1 | Monitor |
| R-314 | TECH | 消息时间戳显示错误 | 1 | 1 | 1 | Monitor |
| R-315 | TECH | 输入框高度自动调整异常 | 1 | 1 | 1 | Monitor |
| R-316 | TECH | 清空对话确认弹窗缺失 | 1 | 1 | 1 | Monitor |

---

## Test Coverage Plan

### P0 (Critical) - Run on every commit

**Criteria**: 阻塞核心价值 + CRITICAL 风险 (=9) + 无替代方案

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
| ----------- | ---------- | --------- | ---------- | ----- | ----- |
| **SQL 生成准确率（单表查询）** | Integration | R-301 | 3 | Dev | WHERE、ORDER BY、LIMIT |
| **SQL 生成准确率（多表 JOIN）** | Integration | R-301 | 2 | Dev | INNER/LEFT JOIN |
| **SQL 生成准确率（聚合查询）** | Integration | R-301 | 2 | Dev | COUNT、SUM、AVG、GROUP BY |
| **SQL 生成准确率（子查询）** | Integration | R-301 | 1 | Dev | 简单子查询 |
| **SQL 语法验证正确** | Integration | R-301 | 1 | Dev | SQL Parser 验证通过 |
| **引用验证（表和字段存在性）** | Integration | R-301 | 1 | Dev | 防止幻觉生成 |
| **SQL 生成性能 < 3 秒** | Performance | R-302 | 2 | Dev | 简单查询 < 1.5s，复杂查询 < 3s |
| **Agent 自主调用向量检索** | Integration | R-303 | 1 | Dev | 验证 VectorSearchTool 被调用 |
| **向量检索召回相关性 ≥ 85%** | Integration | R-304 | 1 | Dev | Top-K 结果相关性测试 |
| **可解释性信息完整展示** | E2E | R-306 | 1 | QA | 表名 + 字段名 + 中文说明 |

**Total P0**: 15 tests, 30 hours

### P1 (High) - Run on PR to main

**Criteria**: 重要功能 + 中高风险 (4-6) + 常用流程

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
| ----------- | ---------- | --------- | ---------- | ----- | ----- |
| 意图识别准确性（SQL 生成类） | Integration | R-305 | 3 | Dev | 查询、统计、找出 |
| 意图识别准确性（普通对话类） | Integration | R-305 | 2 | Dev | 问候、帮助、功能咨询 |
| 普通对话模式响应友好 | E2E | R-308 | 2 | QA | 自然语言回复 |
| 对话界面布局和交互 | E2E | - | 2 | QA | 输入框、发送按钮、消息显示 |
| SQL 代码块语法高亮 | E2E | - | 1 | QA | 视觉验证 |
| SQL 复制功能正常 | E2E | R-309 | 2 | QA | 复制按钮 + 剪贴板 |
| 加载状态显示（AI 正在思考） | E2E | - | 1 | QA | 加载动画 + 禁用输入 |
| 对话历史显示正确 | E2E | R-310 | 2 | QA | 时间戳 + 排序 |
| 引用源格式化正确 | E2E | R-312 | 1 | QA | 表格布局 + 高亮 |
| DDL 片段展开/折叠 | E2E | - | 1 | QA | 交互验证 |
| 当前文件提示显示 | E2E | - | 1 | QA | 顶部或输入框上方 |

**Total P1**: 18 tests, 18 hours

### P2 (Medium) - Run nightly/weekly

**Criteria**: 次要功能 + 低风险 (1-2) + 边界场景

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
| ----------- | ---------- | --------- | ---------- | ----- | ----- |
| 输入框多行自动扩展 | E2E | R-315 | 1 | QA | 最多 5 行 + 滚动 |
| Shift+Enter 换行功能 | E2E | - | 1 | QA | 键盘交互 |
| 清空对话功能 | E2E | R-316 | 1 | QA | 确认弹窗 + 清空 |
| 消息滚动自动定位 | E2E | - | 1 | QA | 自动滚到最新 |
| 虚拟滚动（>50 条消息） | E2E | - | 1 | QA | 性能验证 |
| 请求失败错误提示 | E2E | - | 2 | QA | 网络错误、API 错误 |
| 请求超时处理 | E2E | R-302 | 1 | QA | 显示超时提示 |
| 取消请求功能 | E2E | - | 1 | QA | 可选功能 |
| DDL 未解析时生成 SQL 提示 | E2E | - | 1 | QA | 友好提示 |
| 边界场景：空输入处理 | E2E | - | 1 | QA | 禁用发送按钮 |
| 边界场景：超长输入处理 | E2E | - | 1 | QA | 输入限制提示 |
| 响应式设计（桌面/平板） | E2E | - | 2 | QA | 布局自适应 |

**Total P2**: 14 tests, 7 hours

### P3 (Low) - Run on-demand

**Criteria**: 探索性测试

- 无（Epic 3 为核心功能，所有测试都是 P0-P2）

**Total P3**: 0 tests, 0 hours

---

## Execution Order

### Smoke Tests (<5 min)

**Purpose**: 快速反馈，捕获核心功能破坏

- [ ] 用户输入自然语言，系统返回 SQL（2min）
- [ ] SQL 可以复制到剪贴板（30s）
- [ ] 引用信息正确显示（1min）

**Total**: 3 scenarios

### P0 Tests (<20 min)

**Purpose**: 验证 100% SQL 准确率和核心性能

- [ ] 单表查询生成准确（Integration, 3×2min）
- [ ] 多表 JOIN 生成准确（Integration, 2×3min）
- [ ] 聚合查询生成准确（Integration, 2×2min）
- [ ] 子查询生成准确（Integration, 1×2min）
- [ ] SQL 语法验证（Integration, 1×1min）
- [ ] 引用验证通过（Integration, 1×1min）
- [ ] 性能 < 3 秒（Performance, 2×2min）
- [ ] Agent 调用向量检索（Integration, 1×2min）
- [ ] 向量召回相关性（Integration, 1×2min）
- [ ] 可解释性展示（E2E, 1×2min）

**Total**: 15 scenarios

### P1 Tests (<40 min)

**Purpose**: 重要功能和用户体验

- [ ] 意图识别（SQL 类）（Integration, 3×1min）
- [ ] 意图识别（对话类）（Integration, 2×1min）
- [ ] 普通对话响应（E2E, 2×1min）
- [ ] 对话界面交互（E2E, 2×1min）
- [ ] SQL 语法高亮（E2E, 1×1min）
- [ ] SQL 复制功能（E2E, 2×1min）
- [ ] 加载状态显示（E2E, 1×1min）
- [ ] 对话历史显示（E2E, 2×1min）
- [ ] 引用源格式化（E2E, 1×1min）
- [ ] DDL 片段展开（E2E, 1×1min）
- [ ] 当前文件提示（E2E, 1×1min）

**Total**: 18 scenarios

### P2/P3 Tests (<60 min)

**Purpose**: 边界场景和体验细节

- [ ] 输入框自动扩展（E2E, 1×1min）
- [ ] Shift+Enter 换行（E2E, 1×1min）
- [ ] 清空对话（E2E, 1×1min）
- [ ] 自动滚动（E2E, 1×1min）
- [ ] 虚拟滚动（E2E, 1×1min）
- [ ] 错误提示（E2E, 2×1min）
- [ ] 超时处理（E2E, 1×1min）
- [ ] 取消请求（E2E, 1×1min）
- [ ] DDL 未解析提示（E2E, 1×1min）
- [ ] 空输入处理（E2E, 1×1min）
- [ ] 超长输入处理（E2E, 1×1min）
- [ ] 响应式设计（E2E, 2×1min）

**Total**: 14 scenarios

---

## Resource Estimates

### Test Development Effort

| Priority | Count | Hours/Test | Total Hours | Notes |
| -------- | ----- | ---------- | ----------- | ----- |
| P0 | 15 | 2.0 | 30 | 准确性验证、性能测试、复杂设置 |
| P1 | 18 | 1.0 | 18 | 标准功能验证 |
| P2 | 14 | 0.5 | 7 | 边界场景 |
| P3 | 0 | 0.25 | 0 | 无 |
| **Total** | **47** | **-** | **55** | **~7 days** |

### Prerequisites

**Test Data:**

- **SQL 生成测试集**（必需，高优先级）：
  - `test-queries-simple.json`：20 个单表查询（WHERE、ORDER BY、LIMIT）
  - `test-queries-join.json`：10 个多表 JOIN 查询
  - `test-queries-aggregate.json`：10 个聚合查询（COUNT、SUM、GROUP BY）
  - `test-queries-subquery.json`：5 个子查询
  - `test-queries-complex.json`：5 个复杂查询（多表 + 聚合 + 子查询）
  - **每个查询包含**：自然语言输入 + 预期 SQL + 预期引用表/字段

- **意图识别测试集**：
  - `test-intents-sql.json`：30 个 SQL 生成意图（查询、统计、找出、我想看）
  - `test-intents-chat.json`：20 个普通对话意图（问候、帮助、功能咨询）

**Tooling:**

- Playwright（E2E 对话交互测试）
- Pytest（Integration 层 Agent、RAG、SQL 生成测试）
- LangChain Testing Utils（Agent 执行追踪）
- SQL Parser（sqlparse/sqlglot，语法验证）
- Performance monitoring（响应时间统计）

**Environment:**

- 向量库已初始化（包含测试 DDL 向量）
- LLM API 配置就绪（GLM API Key）
- 测试 DDL 文件已上传和解析

---

## Quality Gate Criteria

### Pass/Fail Thresholds

- **P0 pass rate**: 100% (no exceptions) 🚨
- **P1 pass rate**: ≥95%
- **P2/P3 pass rate**: ≥90%
- **CRITICAL risks (R-301, R-302)**: 100% mitigated

### Coverage Targets

- **SQL 生成准确率**: 100%（所有测试用例）
- **意图识别准确率**: >95%
- **向量召回相关性**: ≥85%
- **可解释性展示**: 100%（每次生成都有引用信息）
- **性能目标**: 95% 的 SQL 生成 < 3 秒

### Non-Negotiable Requirements

- [ ] **SQL 生成准确率 100%**（语法 + 逻辑）
- [ ] **性能 < 3 秒**（95% 的请求）
- [ ] **可解释性 100% 覆盖**（引用信息完整）
- [ ] **Agent 决策正确**（自主调用向量检索）
- [ ] **向量召回相关性 ≥ 85%**

---

## Mitigation Plans

### R-301: SQL 生成准确率 < 100% (Score: 9) 🚨 CRITICAL BLOCKER

**Mitigation Strategy:**

**Phase 1: 多层验证机制（必需）**
1. **语法验证**（第 1 层）：
   - 使用 SQL Parser（sqlparse）解析生成的 SQL
   - 验证 SQL 可以成功解析，无语法错误
   - 验证关键字拼写、括号配对、引号匹配
   
2. **引用验证**（第 2 层）：
   - 提取 SQL 中的所有表名和字段名
   - 对比当前 DDL 文件中的表和字段
   - 验证所有引用都存在（防止幻觉生成）
   
3. **逻辑验证**（第 3 层）：
   - Agent 自我验证（ReAct 推理模式）
   - 验证 SQL 是否符合用户意图
   - 验证 JOIN 条件、WHERE 条件合理性

**Phase 2: 生成策略优化（必需）**
1. **Prompt 工程**：
   - 数据分析专家 Persona
   - Few-shot 示例（3-5 个高质量示例）
   - 明确输出格式要求（纯 SQL，无额外解释）
   - 强制规则（不使用 SELECT *、添加注释、使用标准 SQL）
   
2. **RAG 优化**：
   - 向量检索相关性阈值 ≥ 85%
   - Top-K 结果优化（K=5）
   - Chunk 策略：每个表单独作为 Document
   
3. **LLM 配置优化**：
   - Temperature = 0.1（低温度确保输出稳定）
   - Top-p = 1.0
   - Max tokens 限制（避免过长输出）

**Phase 3: 大规模测试集验证（必需）**
1. **构建测试集**（50+ 查询）：
   - 单表查询：20 个（WHERE、ORDER BY、LIMIT、OFFSET）
   - 多表 JOIN：10 个（INNER、LEFT、RIGHT JOIN）
   - 聚合查询：10 个（COUNT、SUM、AVG、GROUP BY、HAVING）
   - 子查询：5 个（WHERE IN、FROM 子查询）
   - 复杂查询：5 个（组合多种特性）
   
2. **自动化测试流程**：
   - 对每个测试查询执行生成
   - 自动验证语法正确性
   - 自动验证引用存在性
   - 人工审查逻辑正确性（前期）
   - 统计准确率（目标 100%）
   
3. **持续改进**：
   - 收集用户修正的 SQL
   - 补充到测试集
   - 优化 Prompt 和检索策略

**Phase 4: 不达标降级方案（备选）**

如果 Sprint 2-3 无法达到 100% 准确率：
- **降级为 95% + 用户审核机制**
- 明确标注"AI 生成，请确认后使用"
- 提供 SQL 编辑功能（Phase 2 增强）
- 收集用户修正数据用于模型微调

**Owner:** Dev（Prompt 工程 + 验证机制）+ QA（测试集构建）
**Timeline:** Sprint 2-3（持续优化）
**Status:** Planned
**Verification:** 
- 所有测试用例 100% 通过
- 用户验收测试准确率 100%
- 生产环境监控准确率 ≥ 99%

---

### R-302: SQL 生成性能超过 3 秒 (Score: 9) 🚨 CRITICAL BLOCKER

**Mitigation Strategy:**

**Phase 1: 性能测试和基线建立（必需）**
1. 测量当前性能：
   - 简单查询（单表）耗时
   - 复杂查询（多表 JOIN）耗时
   - 分解耗时：向量检索 + LLM 调用 + 验证
2. 建立性能基线：
   - 简单查询目标：< 1.5 秒
   - 复杂查询目标：< 3 秒
3. 识别性能瓶颈：
   - 向量检索慢？（目标 < 500ms）
   - LLM 调用慢？（目标 < 2 秒）
   - 网络延迟？

**Phase 2: 性能优化（必需）**
1. **向量检索优化**：
   - 使用 FAISS 索引优化（IVF、HNSW）
   - 调整 Top-K（减少不必要的检索结果）
   - 相关性阈值过滤（>85%）
   
2. **LLM 调用优化**：
   - Prompt 长度控制（< 2000 tokens）
   - 仅传递相关 DDL 片段（不传完整 DDL）
   - 使用流式输出（可选，改善感知性能）
   
3. **缓存策略**：
   - 相同输入缓存 SQL 结果（5 分钟 TTL）
   - 向量检索结果缓存
   
4. **异步处理**：
   - 向量检索和 LLM 调用并发（如果可能）
   - 前端显示进度提示（"正在检索..."、"正在生成..."）

**Phase 3: 性能监控（必需）**
1. 记录每次 SQL 生成的耗时
2. 分解耗时（检索、LLM、验证）
3. 统计 P50、P95、P99 响应时间
4. 告警：如果 P95 > 3 秒触发告警

**Phase 4: 不达标降级方案（备选）**

如果无法达到 < 3 秒：
- **调整目标为 < 5 秒**（降低用户期望）
- 优化感知性能：
  - 显示详细进度（"正在检索表结构..."、"找到 3 个相关表"、"正在生成 SQL..."）
  - 提供预估时间（"大约还需 2 秒"）
  - 使用流式输出（SQL 逐字显示）

**Owner:** Dev
**Timeline:** Sprint 2-3（持续优化）
**Status:** Planned
**Verification:** 
- 95% 的 SQL 生成 < 3 秒
- 性能测试通过
- 用户反馈无明显等待焦虑

---

### R-303: Agent 决策失误 (Score: 6)

**Mitigation Strategy:**
1. Agent 决策测试：
   - 输入明确需要检索的查询（如"查询用户表"）
   - 验证 Agent 调用 VectorSearchTool
   - 记录 Agent 决策日志（Thought、Action）
2. Agent 提示词优化：
   - 明确工具使用场景（"当需要查询表结构时，使用 VectorSearchTool"）
   - Few-shot 示例（正确的工具调用示例）
3. 决策可观测性：
   - 日志记录 Agent 每步决策
   - DEBUG 模式展示 Agent 推理过程

**Owner:** Dev
**Timeline:** Sprint 2
**Status:** Planned
**Verification:** Integration 测试验证 Agent 100% 正确调用工具

---

### R-304: 向量检索召回不相关 (Score: 6)

**Mitigation Strategy:**
1. 召回相关性测试：
   - 构建测试查询集（20 个查询 + 预期召回表）
   - 执行向量检索，验证 Top-K 结果
   - 计算相关性分数（目标 ≥ 85%）
2. Embedding 模型优化：
   - 尝试不同 Embedding 模型（text-embedding-ada-002、本地模型）
   - 选择召回效果最好的模型
3. Chunk 策略优化：
   - 每个表单独作为 Document
   - 表注释 + 字段注释一起向量化
   - 提升语义相关性
4. 检索后过滤：
   - 相关性分数阈值过滤（< 85% 的结果丢弃）

**Owner:** Dev
**Timeline:** Sprint 2
**Status:** Planned
**Verification:** Integration 测试验证召回相关性 ≥ 85%

---

### R-305: 意图识别错误 (Score: 6)

**Mitigation Strategy:**
1. 意图分类测试：
   - 构建测试集（50 个输入 + 预期意图类别）
   - SQL 生成类：查询、统计、找出、我想看、给我...
   - 普通对话类：你好、如何使用、支持什么、帮助...
   - 模糊边界：我想了解用户表结构（可能是对话也可能是 SQL）
2. 分类准确率验证：
   - 自动化测试执行意图识别
   - 统计准确率（目标 >95%）
   - 分析错误分类案例
3. 意图识别优化：
   - 使用 LLM 分类（Prompt 明确两种模式）
   - 规则辅助（关键词匹配）
   - 边界情况：提示用户明确意图

**Owner:** Dev
**Timeline:** Sprint 2
**Status:** Planned
**Verification:** Integration 测试准确率 >95%

---

### R-306: 可解释性信息缺失 (Score: 6)

**Mitigation Strategy:**
1. 强制要求：
   - 每次 SQL 生成必须返回 `references` 字段
   - 包含引用的表名、字段名、DDL 片段
2. 引用源提取：
   - 从 Agent 执行记录中提取 VectorSearchTool 返回结果
   - 映射到生成的 SQL（哪些表和字段被使用）
3. 前端展示验证：
   - E2E 测试验证引用信息可见
   - 验证格式正确（表名、字段名、中文说明）
4. 降级处理：
   - 如果引用信息缺失，显示"引用信息暂不可用"
   - 记录错误日志

**Owner:** Dev + QA
**Timeline:** Sprint 2-3
**Status:** Planned
**Verification:** E2E 测试验证 100% 的 SQL 生成都有引用信息

---

### R-307: 复杂查询生成失败 (Score: 6)

**Mitigation Strategy:**
1. 复杂查询测试集：
   - 多表 JOIN（3 张表以上）
   - 多重子查询（嵌套 2 层）
   - 复杂聚合（窗口函数、CASE WHEN）
2. 逐步支持策略：
   - MVP 阶段：支持单表查询 + 简单 JOIN（2 张表）
   - Phase 2：支持复杂 JOIN（3+ 张表）+ 子查询
   - Phase 3：支持窗口函数、CTE
3. 用户引导：
   - 如果查询过于复杂，Agent 提示用户简化
   - 提供分步生成建议（如"先生成表关联，我再帮你添加条件"）

**Owner:** Dev
**Timeline:** Sprint 3（复杂查询增强）
**Status:** Planned
**Verification:** Integration 测试验证复杂查询准确率 ≥ 90%

---

## Assumptions and Dependencies

### Assumptions

1. LLM API（GLM）稳定可用，响应时间 < 2 秒
2. 向量检索相关性可以达到 ≥ 85%（通过优化 Embedding 模型和 Chunk 策略）
3. 用户上传的 DDL 文件质量较好（无严重语法错误）
4. 用户的自然语言描述相对清晰（不过于模糊）

### Dependencies

1. Epic 1 完成（项目基础就绪）- Sprint 1 开始前
2. Epic 2 完成（DDL 文件已上传和向量化）- Sprint 2 开始前
3. LLM API 配置就绪（GLM API Key）- Sprint 2 开始前
4. 测试数据准备（50+ 查询测试集）- Sprint 2 开始前

### Risks to Plan

- **Risk**: LLM API 不稳定导致测试失败率高
  - **Impact**: 无法准确评估准确率
  - **Contingency**: 使用 Mock LLM 进行单元测试，使用真实 LLM 进行集成测试

- **Risk**: 测试集规模不足（< 50 个查询）
  - **Impact**: 无法充分验证准确率
  - **Contingency**: 优先构建核心测试集（单表、JOIN、聚合），逐步扩展

---

## Follow-on Workflows

- Run `*atdd` to generate failing P0 tests (SQL 生成准确性、性能、Agent 决策).
- Run `*automate` for broader coverage (意图识别、对话交互、可解释性).

---

## Approval

**Test Design Approved By:**

- [ ] Product Manager: ___ Date: ___
- [ ] Tech Lead: ___ Date: ___
- [ ] QA Lead: ___ Date: ___

**Comments:**

---

## Appendix

### Knowledge Base References

- `risk-governance.md` - Risk classification framework
- `probability-impact.md` - Risk scoring methodology (9 = CRITICAL BLOCKER)
- `test-levels-framework.md` - Test level selection
- `test-priorities-matrix.md` - P0-P3 prioritization

### Related Documents

- PRD: `_bmad-output/planning-artifacts/prd.md`
- Epic: `_bmad-output/planning-artifacts/epics.md#epic-3`
- Architecture: `_bmad-output/planning-artifacts/architecture.md`

---

**Generated by**: BMad TEA Agent - Test Architect Module
**Workflow**: `_bmad/bmm/testarch/test-design`
**Version**: 4.0 (BMad v6)
