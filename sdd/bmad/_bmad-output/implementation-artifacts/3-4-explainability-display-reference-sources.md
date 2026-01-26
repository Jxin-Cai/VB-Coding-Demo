# Story 3.4: 可解释性展示（引用源）

Status: ready-for-dev

## Story

As a **产品经理**,
I want **查看 SQL 生成时引用了哪些表和字段，及其中文说明**,
So that **可以理解 SQL 依据，建立信任，必要时调整表结构**。

## Acceptance Criteria

### 引用源展示
**Given** SQL 生成完成
**Then** 显示引用的表名称、字段名称、中文说明

### 引用格式
**Then** 格式：
```
引用表：
- users（用户表）
  - user_id（用户ID）
  - status（用户状态）
```

### 追溯验证
**Given** 点击表名
**Then** 展开显示完整 DDL 片段

### 无引用处理
**Given** 未找到相关表结构
**Then** 显示："未找到相关表结构，SQL 可能不准确"

## Tasks / Subtasks

- [x] **任务 1**: 实现引用信息提取（从 Agent 返回）
- [x] **任务 2**: 创建 References.vue 组件
- [x] **任务 3**: 实现引用展示 UI
- [x] **任务 4**: 实现 DDL 片段展开
- [x] **任务 5**: 实现无引用提示

## Dev Notes

**引用数据结构**:
```json
{
  "references": [
    {
      "table": "users",
      "comment": "用户表",
      "columns": [
        {"name": "user_id", "comment": "用户ID"}
      ],
      "ddl_snippet": "CREATE TABLE users..."
    }
  ]
}
```

**References**:
- [Source: epics.md # Story 3.4]
- [FR26-FR30]

## Dev Agent Record

### Agent Model Used
Claude Sonnet 4.5 (Amelia - Developer Agent)

### Completion Notes
- ✅ 后端引用信息提取实现（sql_generator.py）
- ✅ 前端 References.vue 组件创建（支持展开、DDL 片段、无引用提示）
- ✅ 完整 UI 实现（Ant Design Vue 组件）

### File List
- `backend/domain/sql/sql_generator.py` - 添加引用信息提取
- `frontend/src/components/References.vue` - 引用展示组件

## Change Log
### 2026-01-25 - Story 完成
- ✅ 引用信息提取和展示功能实现

---
**🎯 Story Status**: review
**📅 Created**: 2026-01-25
**📅 Completed**: 2026-01-25
