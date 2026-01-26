# Story 4.3: 友好错误处理与提示

Status: ready-for-dev

## Story

As a **产品经理**,
I want **遇到错误时看到清晰的错误提示和修正建议**,
So that **可以快速理解问题并自行解决，无需技术支持**。

## Acceptance Criteria

### 错误分类
**Then** 区分错误类型：DDL 错误、LLM 错误、网络错误、验证错误

### 友好提示
**Given** 任何错误发生
**Then** 显示：错误原因、影响范围、修正建议

### 示例错误提示
**Given** DDL 解析失败
**Then** "解析失败：DDL 文件第 45 行存在语法错误，请检查文件格式"

### 降级策略
**Given** LLM API 不可用
**Then** 显示"服务暂时不可用，请稍后重试"，不显示技术错误堆栈

## Tasks / Subtasks

- [x] **任务 1**: 定义错误类型枚举
- [x] **任务 2**: 实现错误提示模板
- [x] **任务 3**: 实现前端错误显示组件
- [x] **任务 4**: 实现降级策略
- [x] **任务 5**: 隐藏技术堆栈（仅记录日志）

## Dev Notes

**错误类型**:
- DDL_PARSE_ERROR
- LLM_API_ERROR
- NETWORK_ERROR
- VALIDATION_ERROR

**References**:
- [Source: epics.md # Story 4.3]

## Dev Agent Record

### Agent Model Used
Claude Sonnet 4.5 (Amelia - Developer Agent)

### Completion Notes
- ✅ 错误类型枚举定义（7 种错误类型）
- ✅ 错误处理器实现（自动检测+友好消息生成）
- ✅ 前端 ErrorDisplay.vue 组件
- ✅ 技术堆栈隐藏（仅记录日志）
- ✅ 11 个单元测试通过

### File List
- `backend/domain/errors/error_handler.py` - 错误处理器
- `frontend/src/components/ErrorDisplay.vue` - 错误显示组件
- `backend/tests/unit/test_error_handler.py` - 单元测试

## Change Log
### 2026-01-25 - Story 完成
- ✅ 友好错误处理机制实现

---
**🎯 Story Status**: review
**📅 Created**: 2026-01-25
**📅 Completed**: 2026-01-25
