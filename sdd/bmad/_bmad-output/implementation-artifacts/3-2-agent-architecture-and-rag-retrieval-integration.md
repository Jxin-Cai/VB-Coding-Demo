# Story 3.2: Agent 架构与 RAG 检索集成

Status: ready-for-dev

## Story

As a **开发工程师**,
I want **搭建 LangChain + LangGraph 的 Agent 架构，集成向量检索工具**,
So that **Agent 可以自主决策何时检索向量库，为 SQL 生成提供精准的表结构上下文**。

## Acceptance Criteria

### Agent 架构初始化
**Then** 创建：Agent Executor、LLM 配置、Tools、Memory

### 向量检索工具定义
**Given** 定义 VectorSearchTool
**Then** 输入：查询文本，执行：向量检索，输出：Top-K 表结构

### Agent 执行流程
**Then** Step 1-6：理解需求 → 决策检索 → 调用工具 → 生成 SQL → 验证 → 返回结果

### 检索性能
**When** 调用 VectorSearchTool
**Then** 检索速度 < 500ms，返回完整表结构

### 复杂查询支持
**Given** 多表关联查询
**Then** Agent 可迭代调用工具，整合所有检索结果

### 可观测性
**Then** 记录详细日志：每步决策、工具调用、返回结果

## Tasks / Subtasks

- [ ] **任务 1**: 安装并配置 LangChain + LangGraph
- [ ] **任务 2**: 创建 Agent Executor 和状态机
- [ ] **任务 3**: 实现 VectorSearchTool
- [ ] **任务 4**: 注册工具到 Agent
- [ ] **任务 5**: 实现 Agent 执行流程
- [ ] **任务 6**: 添加日志和可观测性

## Dev Notes

**核心技术**:
- LangChain: Agent 框架
- LangGraph: 状态机管理
- LLM: DeepSeek/OpenAI
- Vector DB: Chroma/FAISS

**References**:
- [Source: epics.md # Story 3.2]
- [Source: architecture.md # Agent Architecture]

## Dev Agent Record
_待开发时填写_

---
**🎯 Story Status**: ready-for-dev
**📅 Created**: 2026-01-25
