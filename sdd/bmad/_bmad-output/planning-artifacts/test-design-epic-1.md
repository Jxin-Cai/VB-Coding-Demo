# Test Design: Epic 1 - 项目基础与开发环境 🛠️

**Date:** 2026-01-25
**Author:** Jxin
**Status:** Draft

---

## Executive Summary

**Scope:** 完整测试设计 - Epic 1（项目基础与开发环境）

**Risk Summary:**

- Total risks identified: 8
- High-priority risks (≥6): 2
- Critical categories: TECH, OPS

**Coverage Summary:**

- P0 scenarios: 4 (8 hours)
- P1 scenarios: 6 (6 hours)
- P2/P3 scenarios: 8 (3 hours)
- **Total effort**: 17 hours (~2.2 days)

---

## Risk Assessment

### High-Priority Risks (Score ≥6)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner | Timeline |
| ------- | -------- | ----------- | ----------- | ------ | ----- | ---------- | ----- | -------- |
| R-001 | TECH | DDD 分层架构违背导致代码耦合严重 | 2 | 3 | 6 | 架构评审 + 分层边界测试 | Tech Lead | Sprint 0 |
| R-002 | OPS | 单进程部署配置错误导致前后端无法正常通信 | 2 | 3 | 6 | 部署集成测试 + 健康检查验证 | DevOps | Sprint 0 |

### Medium-Priority Risks (Score 3-4)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner |
| ------- | -------- | ----------- | ----------- | ------ | ----- | ---------- | ----- |
| R-003 | TECH | 日志系统配置不当导致关键信息缺失 | 2 | 2 | 4 | 日志验证测试 | Dev |
| R-004 | OPS | 健康检查接口未正确反映系统状态 | 2 | 2 | 4 | 健康检查集成测试 | DevOps |
| R-005 | TECH | 环境变量管理混乱导致配置泄露 | 1 | 3 | 3 | 配置安全测试 | Dev |

### Low-Priority Risks (Score 1-2)

| Risk ID | Category | Description | Probability | Impact | Score | Action |
| ------- | -------- | ----------- | ----------- | ------ | ----- | ------ |
| R-006 | TECH | Starter Template 初始化失败 | 1 | 2 | 2 | Monitor |
| R-007 | OPS | 前端构建失败或产物不完整 | 1 | 2 | 2 | Monitor |
| R-008 | TECH | 依赖版本冲突 | 1 | 1 | 1 | Monitor |

### Risk Category Legend

- **TECH**: Technical/Architecture (flaws, integration, scalability)
- **SEC**: Security (access controls, auth, data exposure)
- **PERF**: Performance (SLA violations, degradation, resource limits)
- **DATA**: Data Integrity (loss, corruption, inconsistency)
- **BUS**: Business Impact (UX harm, logic errors, revenue)
- **OPS**: Operations (deployment, config, monitoring)

---

## Test Coverage Plan

### P0 (Critical) - Run on every commit

**Criteria**: 阻塞后续开发 + 高风险 (≥6) + 无替代方案

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
| ----------- | ---------- | --------- | ---------- | ----- | ----- |
| DDD 分层架构正确实现 | Integration | R-001 | 1 | Dev | 验证依赖方向正确 |
| 单进程部署前后端通信 | E2E | R-002 | 1 | DevOps | 启动后验证 API 可达 |
| 健康检查接口工作正常 | API | R-004 | 1 | DevOps | 验证状态反映准确 |
| 日志系统记录关键操作 | Integration | R-003 | 1 | Dev | 验证日志格式和内容 |

**Total P0**: 4 tests, 8 hours

### P1 (High) - Run on PR to main

**Criteria**: 重要基础设施 + 中风险 (3-4) + 影响开发效率

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
| ----------- | ---------- | --------- | ---------- | ----- | ----- |
| 前端 Starter 初始化成功 | Integration | R-006 | 1 | Dev | 验证 Vue 3 + Vite 配置 |
| 后端 DDD 目录结构创建 | Integration | R-001 | 1 | Dev | 验证 4 层目录存在 |
| 环境变量配置加载正确 | Unit | R-005 | 2 | Dev | config.py 加载测试 |
| 前端构建产物完整 | Integration | R-007 | 1 | Dev | npm run build 验证 |
| 静态文件服务工作正常 | E2E | R-002 | 1 | DevOps | 访问 / 返回前端页面 |

**Total P1**: 6 tests, 6 hours

### P2 (Medium) - Run nightly/weekly

**Criteria**: 次要功能 + 低风险 (1-2) + 边界场景

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
| ----------- | ---------- | --------- | ---------- | ----- | ----- |
| 日志级别动态调整 | Unit | R-003 | 2 | Dev | INFO/DEBUG/ERROR 切换 |
| 日志格式正确性 | Unit | R-003 | 2 | Dev | 时间戳、级别、模块名 |
| 健康检查边界场景 | API | R-004 | 2 | DevOps | 服务未就绪、部分降级 |
| 依赖版本兼容性 | Integration | R-008 | 1 | Dev | requirements.txt 安装 |
| 前端开发服务器启动 | Integration | R-006 | 1 | Dev | npm run dev 验证 |

**Total P2**: 8 tests, 4 hours

### P3 (Low) - Run on-demand

**Criteria**: Nice-to-have + 探索性测试

- 无（Epic 1 为基础设施，无 P3 测试）

**Total P3**: 0 tests, 0 hours

---

## Execution Order

### Smoke Tests (<5 min)

**Purpose**: 快速反馈，捕获构建破坏性问题

- [ ] 后端服务启动成功（30s）
- [ ] 健康检查接口返回 200 OK（15s）
- [ ] 前端页面可访问（30s）

**Total**: 3 scenarios

### P0 Tests (<10 min)

**Purpose**: 验证关键基础设施

- [ ] DDD 分层架构依赖方向正确（Integration, 2min）
- [ ] 单进程部署前后端 API 通信正常（E2E, 3min）
- [ ] 健康检查准确反映系统状态（API, 2min）
- [ ] 日志系统记录关键操作和错误（Integration, 2min）

**Total**: 4 scenarios

### P1 Tests (<30 min)

**Purpose**: 验证开发环境和配置

- [ ] Vue 3 Starter 初始化成功（Integration, 1min）
- [ ] DDD 四层目录创建（Integration, 1min）
- [ ] 环境变量正确加载（Unit, 2min）
- [ ] 前端构建产物完整（Integration, 2min）
- [ ] 静态文件服务正常（E2E, 2min）

**Total**: 6 scenarios

### P2/P3 Tests (<60 min)

**Purpose**: 完整回归覆盖

- [ ] 日志级别动态调整（Unit, 1min）
- [ ] 日志格式验证（Unit, 1min）
- [ ] 健康检查边界场景（API, 2min）
- [ ] 依赖兼容性验证（Integration, 2min）

**Total**: 8 scenarios

---

## Resource Estimates

### Test Development Effort

| Priority | Count | Hours/Test | Total Hours | Notes |
| -------- | ----- | ---------- | ----------- | ----- |
| P0 | 4 | 2.0 | 8 | 架构验证、集成测试 |
| P1 | 6 | 1.0 | 6 | 标准配置验证 |
| P2 | 8 | 0.5 | 4 | 简单场景 |
| P3 | 0 | 0.25 | 0 | 无 |
| **Total** | **18** | **-** | **18** | **~2.3 days** |

### Prerequisites

**Test Data:**

- 无需复杂测试数据（基础设施测试）

**Tooling:**

- Pytest（Python 单元和集成测试）
- Playwright（E2E 测试，验证前端可访问性）
- requests 库（API 健康检查测试）

**Environment:**

- Python 3.9+ 运行环境
- Node.js 16+ 构建环境
- Docker（可选，用于隔离测试）

---

## Quality Gate Criteria

### Pass/Fail Thresholds

- **P0 pass rate**: 100% (no exceptions)
- **P1 pass rate**: ≥95% (waivers required for failures)
- **P2/P3 pass rate**: ≥90% (informational)
- **High-risk mitigations**: 100% complete or approved waivers

### Coverage Targets

- **架构分层边界**: 100%（验证依赖方向）
- **部署配置**: 100%（前后端通信、静态文件服务）
- **日志关键操作**: ≥80%
- **健康检查**: 100%

### Non-Negotiable Requirements

- [ ] DDD 分层架构验证通过
- [ ] 单进程部署验证通过（前端产物 + 后端启动）
- [ ] 健康检查接口正确工作
- [ ] 日志系统记录关键操作

---

## Mitigation Plans

### R-001: DDD 分层架构违背 (Score: 6)

**Mitigation Strategy:**
1. 在架构设计阶段明确依赖方向规则（Interface → Application → Domain → Infrastructure）
2. 使用依赖分析工具（如 import-linter）自动检测依赖违规
3. 编写集成测试验证分层边界：
   - Domain 层不依赖 Infrastructure 层
   - Application 层不直接依赖 Infrastructure 细节
   - Interface 层仅依赖 Application 层接口
4. 代码审查检查点：强制验证分层规则

**Owner:** Tech Lead
**Timeline:** Sprint 0（框架搭建阶段）
**Status:** Planned
**Verification:** 运行 import-linter + 集成测试通过

### R-002: 单进程部署配置错误 (Score: 6)

**Mitigation Strategy:**
1. 编写部署集成测试：
   - 前端 `npm run build` 成功
   - 产物复制到 `backend/static/` 成功
   - 后端启动时挂载静态文件服务
   - 访问 `/` 返回前端 index.html
   - API 路径 `/api/*` 正确路由到后端
2. 添加健康检查验证：`/health` 接口返回完整状态
3. 部署文档（DEPLOYMENT.md）包含完整步骤
4. 自动化部署脚本（deploy.sh）

**Owner:** DevOps
**Timeline:** Sprint 0
**Status:** Planned
**Verification:** E2E 测试验证前后端通信 + 手动部署验证

---

## Assumptions and Dependencies

### Assumptions

1. Python 3.9+ 和 Node.js 16+ 已安装在开发和部署环境
2. 开发团队熟悉 DDD 分层架构和 SOLID 原则
3. 有足够内存支持向量库运行（≥ 4GB）

### Dependencies

1. Vue.js 官方 Starter（create-vue）- Sprint 0 开始前
2. Python 依赖库（LangChain、FastAPI、sqlparse）- Sprint 0 开始前
3. 部署环境准备（Docker 或服务器）- Sprint 0 结束前

### Risks to Plan

- **Risk**: 开发团队对 DDD 架构不熟悉
  - **Impact**: 架构实现偏离设计，分层混乱
  - **Contingency**: 提供 DDD 架构培训和代码示例，架构评审强制执行

---

## Follow-on Workflows

- Run `*atdd` to generate failing P0 tests (separate workflow; not auto-run).
- Run `*framework` to scaffold test framework structure.

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
- `test-levels-framework.md` - Test level selection
- `test-priorities-matrix.md` - P0-P3 prioritization

### Related Documents

- PRD: `_bmad-output/planning-artifacts/prd.md`
- Epic: `_bmad-output/planning-artifacts/epics.md#epic-1`
- Architecture: `_bmad-output/planning-artifacts/architecture.md`

---

**Generated by**: BMad TEA Agent - Test Architect Module
**Workflow**: `_bmad/bmm/testarch/test-design`
**Version**: 4.0 (BMad v6)
