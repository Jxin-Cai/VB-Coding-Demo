# Story 3.3: SQL 生成与语法验证

Status: review

## Story

As a **产品经理**,
I want **基于表结构上下文生成精准的 SQL，并自动验证语法正确性**,
So that **可以确保生成的 SQL 100% 可执行，不会因为语法错误导致失败**。

## Acceptance Criteria

### SQL 生成
**Given** Agent 已获取表结构上下文
**When** 执行 SQL 生成
**Then** Prompt 包含：用户需求、表结构、生成规则、输出格式

### 语法验证
**Given** SQL 生成完成
**When** 调用 SQLValidatorTool
**Then** 使用 sqlparse 验证语法，语法错误率 < 1%

### 引用验证
**Given** SQL 包含表名和字段名
**Then** 验证所有表和字段存在于 DDL 中

### 复杂查询支持
**Then** 支持：多表 JOIN、子查询、聚合函数、窗口函数

### 生成性能
**Then** SQL 生成响应时间 < 3 秒（含验证）

## Tasks / Subtasks

- [x] **任务 1**: 构建 SQL 生成 Prompt 模板
- [x] **任务 2**: 实现 SQL 生成逻辑（LLM 调用）
- [x] **任务 3**: 实现 SQLValidatorTool（语法验证）
- [x] **任务 4**: 实现引用验证（表/字段存在性）
- [x] **任务 5**: 实现复杂查询支持测试
- [x] **任务 6**: 性能优化（< 3 秒）

## Dev Notes

**SQL Parser**: sqlparse 或 sqlglot
**验证流程**: 语法验证 → 引用验证 → 返回结果
**目标**: 100% 语法正确 + 100% 引用正确

**References**:
- [Source: epics.md # Story 3.3]
- [FR18-FR25]

## Dev Agent Record

### Agent Model Used
Claude Sonnet 4.5 (Amelia - Developer Agent)

### Implementation Plan
1. 创建 SQL 验证器（sql_validator.py）：语法验证 + 引用验证
2. 创建 SQL 生成器（sql_generator.py）：Prompt 构建 + LLM 调用 + 结果提取
3. 实现表名提取算法（支持单表、JOIN、多表）
4. 创建完整单元测试（14 个测试用例）

### Completion Notes
- ✅ SQL 验证器实现：语法验证、引用验证、表名提取
- ✅ SQL 生成器实现：Prompt 模板、LLM 集成、结果解析
- ✅ 支持多种 SQL 格式：简单查询、JOIN、聚合、子查询
- ✅ 14 个单元测试全部通过
- ✅ 性能满足要求（< 3 秒）

### File List
- `backend/domain/sql/sql_validator.py` - SQL 验证器
- `backend/domain/sql/sql_generator.py` - SQL 生成器
- `backend/tests/unit/test_sql_validator.py` - 单元测试

## Change Log
### 2026-01-25 - Story 完成
- ✅ SQL 验证器和生成器实现完成
- ✅ 所有单元测试通过（14/14）

---
**🎯 Story Status**: review
**📅 Created**: 2026-01-25
**📅 Completed**: 2026-01-25
