# Test Design: Epic 2 - DDL 文件管理与向量化 📁

**Date:** 2026-01-25
**Author:** Jxin
**Status:** Draft

---

## Executive Summary

**Scope:** 完整测试设计 - Epic 2（DDL 文件管理与向量化）

**Risk Summary:**

- Total risks identified: 12
- High-priority risks (≥6): 4
- Critical categories: DATA, TECH, PERF

**Coverage Summary:**

- P0 scenarios: 8 (16 hours)
- P1 scenarios: 10 (10 hours)
- P2/P3 scenarios: 12 (5 hours)
- **Total effort**: 31 hours (~4 days)

---

## Risk Assessment

### High-Priority Risks (Score ≥6)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner | Timeline |
| ------- | -------- | ----------- | ----------- | ------ | ----- | ---------- | ----- | -------- |
| R-101 | DATA | DDL 解析错误导致表结构信息不完整或错误 | 3 | 3 | 9 | 多样化 DDL 测试集 + 解析验证 | Dev | Sprint 1 |
| R-102 | TECH | 向量化失败导致后续 SQL 生成无法工作 | 2 | 3 | 6 | 向量化集成测试 + 异常处理 | Dev | Sprint 1 |
| R-103 | PERF | DDL 向量化超过 5 秒导致用户等待焦虑 | 2 | 3 | 6 | 性能测试 + 优化向量化算法 | Dev | Sprint 1 |
| R-104 | DATA | 多文件切换时向量库上下文混淆 | 2 | 3 | 6 | 上下文隔离测试 + 状态管理 | Dev | Sprint 1 |

### Medium-Priority Risks (Score 3-4)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner |
| ------- | -------- | ----------- | ----------- | ------ | ----- | ---------- | ----- |
| R-105 | TECH | 文件大小超限（>10MB）未正确拦截 | 2 | 2 | 4 | 文件大小验证测试 | Dev |
| R-106 | TECH | 文件格式验证不准确（非 .sql 文件通过） | 2 | 2 | 4 | 文件类型验证测试 | Dev |
| R-107 | DATA | 文件删除后向量库未清理 | 1 | 3 | 3 | 清理验证测试 | Dev |
| R-108 | TECH | 同名文件覆盖逻辑错误 | 1 | 3 | 3 | 覆盖流程测试 | Dev |
| R-109 | TECH | 文件状态更新不及时（解析中→已解析） | 2 | 2 | 4 | 状态同步测试 | Dev |

### Low-Priority Risks (Score 1-2)

| Risk ID | Category | Description | Probability | Impact | Score | Action |
| ------- | -------- | ----------- | ----------- | ------ | ----- | ------ |
| R-110 | TECH | 文件上传进度条显示不准确 | 1 | 1 | 1 | Monitor |
| R-111 | TECH | 文件列表排序错误 | 1 | 1 | 1 | Monitor |
| R-112 | TECH | 解析结果统计（表数量、字段数量）不准确 | 1 | 2 | 2 | Monitor |

---

## Test Coverage Plan

### P0 (Critical) - Run on every commit

**Criteria**: 阻塞核心功能 + 高风险 (≥6) + 无替代方案

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
| ----------- | ---------- | --------- | ---------- | ----- | ----- |
| DDL 解析正确性（标准 DDL） | Integration | R-101 | 2 | Dev | CREATE TABLE + 字段 + 索引 |
| DDL 解析正确性（复杂 DDL） | Integration | R-101 | 2 | Dev | 外键、注释、多表 |
| 向量化成功存储到向量库 | Integration | R-102 | 1 | Dev | 验证向量可检索 |
| 向量化性能（< 5 秒） | Performance | R-103 | 1 | Dev | 23 张表向量化时间 |
| 多文件上下文隔离 | Integration | R-104 | 1 | Dev | 切换文件不混淆向量 |
| 文件切换后 SQL 使用正确表 | E2E | R-104 | 1 | QA | 端到端验证 |

**Total P0**: 8 tests, 16 hours

### P1 (High) - Run on PR to main

**Criteria**: 重要功能 + 中风险 (3-4) + 常用流程

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
| ----------- | ---------- | --------- | ---------- | ----- | ----- |
| 文件上传（拖拽和选择） | E2E | - | 2 | QA | 两种上传方式 |
| 文件大小验证（<10MB） | Integration | R-105 | 1 | Dev | 前后端双重验证 |
| 文件格式验证（仅 .sql） | Integration | R-106 | 1 | Dev | Content-Type + 扩展名 |
| 文件状态实时更新 | E2E | R-109 | 1 | QA | 解析中→已解析 |
| 文件删除清理向量库 | Integration | R-107 | 1 | Dev | 验证向量已移除 |
| 同名文件覆盖逻辑 | E2E | R-108 | 2 | QA | 覆盖 + 保留两者 |
| 解析结果显示正确 | E2E | R-112 | 1 | QA | 表数量、字段数量 |
| 解析失败友好提示 | E2E | R-101 | 1 | QA | 错误原因和建议 |

**Total P1**: 10 tests, 10 hours

### P2 (Medium) - Run nightly/weekly

**Criteria**: 次要功能 + 低风险 (1-2) + 边界场景

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
| ----------- | ---------- | --------- | ---------- | ----- | ----- |
| 文件上传进度条显示 | E2E | R-110 | 1 | QA | 视觉验证 |
| 文件列表排序（时间倒序） | E2E | R-111 | 1 | QA | 最新在上 |
| 文件列表搜索功能 | E2E | - | 2 | QA | 按文件名搜索 |
| 文件列表筛选功能 | E2E | - | 2 | QA | 按状态筛选 |
| DDL 原始内容展示 | E2E | - | 1 | QA | 折叠/展开 |
| 表详情查看 | E2E | - | 2 | QA | 点击表名查看字段 |
| 边界场景：空 DDL 文件 | Integration | R-101 | 1 | Dev | 友好提示 |
| 边界场景：超大 DDL（接近 10MB） | Integration | R-103 | 2 | Dev | 性能和内存 |

**Total P2**: 12 tests, 6 hours

### P3 (Low) - Run on-demand

**Criteria**: 探索性测试

- 无（Epic 2 为核心数据流，优先保障质量）

**Total P3**: 0 tests, 0 hours

---

## Execution Order

### Smoke Tests (<5 min)

**Purpose**: 快速反馈，捕获数据流破坏性问题

- [ ] 上传标准 DDL 文件成功（1min）
- [ ] DDL 解析完成并显示结果（1.5min）
- [ ] 文件列表显示已解析状态（30s）

**Total**: 3 scenarios

### P0 Tests (<15 min)

**Purpose**: 验证核心数据质量和性能

- [ ] 标准 DDL 解析正确性（Integration, 2min）
- [ ] 复杂 DDL 解析正确性（外键、注释）（Integration, 3min）
- [ ] 向量化成功并可检索（Integration, 2min）
- [ ] 向量化性能 < 5 秒（Performance, 2min）
- [ ] 多文件上下文隔离（Integration, 2min）
- [ ] 文件切换后 SQL 使用正确表（E2E, 3min）

**Total**: 8 scenarios

### P1 Tests (<30 min)

**Purpose**: 重要功能和边界场景

- [ ] 拖拽上传文件（E2E, 1min）
- [ ] 选择文件上传（E2E, 1min）
- [ ] 文件大小验证（Integration, 1min）
- [ ] 文件格式验证（Integration, 1min）
- [ ] 状态实时更新（E2E, 1min）
- [ ] 删除文件清理向量（Integration, 1min）
- [ ] 同名文件覆盖（E2E, 2min）
- [ ] 解析结果显示（E2E, 1min）
- [ ] 解析失败友好提示（E2E, 1min）

**Total**: 10 scenarios

### P2/P3 Tests (<60 min)

**Purpose**: 完整回归和边界场景

- [ ] 进度条显示（E2E, 1min）
- [ ] 文件列表排序（E2E, 1min）
- [ ] 搜索功能（E2E, 2min）
- [ ] 筛选功能（E2E, 2min）
- [ ] DDL 内容展示（E2E, 1min）
- [ ] 表详情查看（E2E, 2min）
- [ ] 空 DDL 处理（Integration, 1min）
- [ ] 超大 DDL 性能（Integration, 2min）

**Total**: 12 scenarios

---

## Resource Estimates

### Test Development Effort

| Priority | Count | Hours/Test | Total Hours | Notes |
| -------- | ----- | ---------- | ----------- | ----- |
| P0 | 8 | 2.0 | 16 | 复杂数据验证、性能测试 |
| P1 | 10 | 1.0 | 10 | 标准功能验证 |
| P2 | 12 | 0.5 | 6 | 边界场景 |
| P3 | 0 | 0.25 | 0 | 无 |
| **Total** | **30** | **-** | **32** | **~4 days** |

### Prerequisites

**Test Data:**

- `test-ddl-simple.sql`：简单 DDL（3-5 张表）
- `test-ddl-complex.sql`：复杂 DDL（23 张表，外键、索引、注释）
- `test-ddl-invalid.sql`：语法错误的 DDL
- `test-ddl-large.sql`：接近 10MB 的 DDL（性能测试）
- `test-ddl-empty.sql`：空文件

**Tooling:**

- Playwright（E2E 文件上传测试）
- Pytest（Integration 解析和向量化测试）
- FAISS/Chroma 测试环境（内存）
- Performance monitoring（响应时间统计）

**Environment:**

- Python 3.9+ 环境
- 内存 ≥ 4GB（向量库）
- LangChain + FAISS/Chroma 已安装

---

## Quality Gate Criteria

### Pass/Fail Thresholds

- **P0 pass rate**: 100% (no exceptions)
- **P1 pass rate**: ≥95%
- **P2/P3 pass rate**: ≥90%
- **High-risk mitigations**: 100% complete (R-101 to R-104)

### Coverage Targets

- **DDL 解析正确性**: 100%（标准 + 复杂 DDL）
- **向量化成功率**: 100%（解析成功必须向量化成功）
- **性能目标**: 95% 的文件解析 < 5 秒
- **文件管理功能**: ≥85%

### Non-Negotiable Requirements

- [ ] DDL 解析正确性 100%（标准和复杂 DDL）
- [ ] 向量化成功且可检索
- [ ] 向量化性能 < 5 秒（23 张表）
- [ ] 多文件上下文隔离不混淆

---

## Mitigation Plans

### R-101: DDL 解析错误 (Score: 9) 🚨 CRITICAL

**Mitigation Strategy:**
1. **构建多样化 DDL 测试集**：
   - MySQL 标准语法（AUTO_INCREMENT、ENGINE=InnoDB）
   - PostgreSQL 语法（SERIAL、INDEX CONCURRENTLY）
   - Oracle 语法（NUMBER、VARCHAR2）
   - 复杂 DDL（外键、多列索引、CHECK 约束、触发器）
   - 边界场景（超长表名、特殊字符、中文注释）
2. **解析验证测试**：
   - 验证提取的表名、字段名、类型完全正确
   - 验证注释提取准确（用于可解释性）
   - 验证主键、外键、索引信息完整
3. **解析失败的友好处理**：
   - 捕获解析异常，返回错误位置（第 X 行）
   - 提供修正建议（如"缺少分号"、"不支持的语法"）
4. **持续测试**：每次支持新数据库类型时补充测试用例

**Owner:** Dev
**Timeline:** Sprint 1（解析模块开发同步）
**Status:** Planned
**Verification:** 
- 所有测试 DDL 100% 解析成功
- 解析失败返回清晰错误信息
- 覆盖 3 种数据库方言（MySQL、PostgreSQL、Oracle）

### R-102: 向量化失败 (Score: 6)

**Mitigation Strategy:**
1. 向量化集成测试：
   - 解析成功后立即向量化
   - 验证向量维度正确（如 1536 维）
   - 验证向量存储到 FAISS/Chroma
   - 验证可以执行相似度检索
2. 异常处理：
   - 捕获 Embedding 模型调用失败
   - 捕获向量库写入失败
   - 回滚机制：向量化失败不影响文件列表
3. 重试机制：
   - 向量化失败自动重试（最多 2 次）

**Owner:** Dev
**Timeline:** Sprint 1
**Status:** Planned
**Verification:** Integration 测试验证向量化成功率 100%

### R-103: 向量化性能超时 (Score: 6)

**Mitigation Strategy:**
1. 性能测试：
   - 测试不同规模 DDL（5 张表、23 张表、50 张表）
   - 测量解析时间 + 向量化时间
   - 目标：23 张表 < 5 秒
2. 性能优化：
   - 使用批量 Embedding（一次向量化多个文本）
   - 优化 Chunk 策略（每个表单独作为 Document）
   - 使用本地 Embedding 模型（避免 API 调用延迟）
3. 进度提示：
   - 显示"正在解析第 15/23 张表..."
   - 缓解用户等待焦虑

**Owner:** Dev
**Timeline:** Sprint 1
**Status:** Planned
**Verification:** Performance 测试验证 95% 的文件 < 5 秒

### R-104: 多文件上下文混淆 (Score: 6)

**Mitigation Strategy:**
1. 上下文隔离测试：
   - 上传 2 个不同项目的 DDL（电商、小程序）
   - 切换到"电商"文件，生成 SQL
   - 验证 SQL 仅引用电商的表
   - 切换到"小程序"文件，生成 SQL
   - 验证 SQL 仅引用小程序的表
2. 状态管理设计：
   - 后端：每个文件独立的向量库实例（或命名空间）
   - 前端：Pinia Store 管理当前文件 ID
   - Agent：从当前文件的向量库检索
3. 清晰的视觉反馈：
   - 当前文件高亮显示
   - 切换时提示"上下文已切换到 [文件名]"

**Owner:** Dev
**Timeline:** Sprint 1
**Status:** Planned
**Verification:** E2E 测试验证多文件切换不混淆

---

## Assumptions and Dependencies

### Assumptions

1. DDL 文件遵循标准 SQL 语法（MySQL、PostgreSQL、Oracle）
2. DDL 文件包含必要的表结构信息（表名、字段、类型）
3. 用户上传的 DDL 文件大小 < 10MB

### Dependencies

1. SQL Parser 库（sqlparse 或 sqlglot）- Sprint 1 开始前
2. 向量库（FAISS 或 Chroma）- Sprint 1 开始前
3. Embedding 模型（本地或 API）- Sprint 1 开始前
4. Epic 1 完成（项目基础就绪）- Sprint 1 开始前

### Risks to Plan

- **Risk**: 不同数据库的 DDL 语法差异大，解析器无法统一处理
  - **Impact**: 解析准确率下降，用户体验差
  - **Contingency**: 优先支持 MySQL 和 PostgreSQL（最常用），其他数据库 Phase 2

---

## Follow-on Workflows

- Run `*atdd` to generate failing P0 tests (DDL 解析、向量化、多文件隔离).
- Run `*automate` for broader coverage once implementation exists.

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
- `probability-impact.md` - Risk scoring methodology
- `test-levels-framework.md` - Test level selection
- `test-priorities-matrix.md` - P0-P3 prioritization

### Related Documents

- PRD: `_bmad-output/planning-artifacts/prd.md`
- Epic: `_bmad-output/planning-artifacts/epics.md#epic-2`
- Architecture: `_bmad-output/planning-artifacts/architecture.md`

---

**Generated by**: BMad TEA Agent - Test Architect Module
**Workflow**: `_bmad/bmm/testarch/test-design`
**Version**: 4.0 (BMad v6)
