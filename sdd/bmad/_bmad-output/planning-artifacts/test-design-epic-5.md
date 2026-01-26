# Test Design: Epic 5 - 前端用户体验优化 🎨

**Date:** 2026-01-25
**Author:** Jxin
**Status:** Draft

---

## Executive Summary

**Scope:** 完整测试设计 - Epic 5（前端用户体验优化）

**Risk Summary:**

- Total risks identified: 9
- High-priority risks (≥6): 1
- Critical categories: BUS, TECH

**Coverage Summary:**

- P0 scenarios: 5 (10 hours)
- P1 scenarios: 12 (12 hours)
- P2/P3 scenarios: 10 (4 hours)
- **Total effort**: 26 hours (~3.3 days)

---

## Risk Assessment

### High-Priority Risks (Score ≥6)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner | Timeline |
| ------- | -------- | ----------- | ----------- | ------ | ----- | ---------- | ----- | -------- |
| R-501 | BUS | 响应式布局断点切换异常导致核心功能不可用 | 2 | 3 | 6 | 响应式测试（桌面、平板、移动） | FE Dev | Sprint 3 |

### Medium-Priority Risks (Score 3-4)

| Risk ID | Category | Description | Probability | Impact | Score | Mitigation | Owner |
| ------- | -------- | ----------- | ----------- | ------ | ----- | ---------- | ----- |
| R-502 | TECH | 主题切换时样式错乱 | 2 | 2 | 4 | 主题切换测试 | FE Dev |
| R-503 | TECH | 颜色对比度不足影响可读性 | 1 | 3 | 3 | 对比度验证（WCAG） | FE Dev |
| R-504 | TECH | 加载状态动画卡顿 | 2 | 2 | 4 | 动画性能测试 | FE Dev |
| R-505 | TECH | 键盘导航焦点顺序混乱 | 1 | 2 | 2 | 键盘导航测试 | QA |
| R-506 | TECH | 语义化 HTML 缺失 | 1 | 2 | 2 | HTML 结构审查 | FE Dev |

### Low-Priority Risks (Score 1-2)

| Risk ID | Category | Description | Probability | Impact | Score | Action |
| ------- | -------- | ----------- | ----------- | ------ | ----- | ------ |
| R-507 | TECH | 快捷键冲突或失效 | 1 | 1 | 1 | Monitor |
| R-508 | TECH | ARIA 标签缺失 | 1 | 1 | 1 | Monitor |
| R-509 | TECH | 情感化设计动画过度 | 1 | 1 | 1 | Monitor |

---

## Test Coverage Plan

### P0 (Critical) - Run on every commit

**Criteria**: 阻塞核心功能可用性 + 高风险 (≥6)

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
| ----------- | ---------- | --------- | ---------- | ----- | ----- |
| **桌面端核心功能可用** | E2E | R-501 | 1 | QA | 三栏布局正常 |
| **平板端核心功能可用** | E2E | R-501 | 1 | QA | 两栏布局正常 |
| **响应式断点切换流畅** | E2E | R-501 | 1 | QA | 窗口调整无破坏 |
| **关键交互在所有设备可用** | E2E | R-501 | 2 | QA | 上传、生成 SQL |

**Total P0**: 5 tests, 10 hours

### P1 (High) - Run on PR to main

**Criteria**: 重要体验功能 + 中风险 (3-4) + 影响满意度

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
| ----------- | ---------- | --------- | ---------- | ----- | ----- |
| 浅色主题样式正确 | E2E | R-502 | 1 | QA | 颜色、对比度验证 |
| 深色主题样式正确 | E2E | R-502 | 1 | QA | 颜色、对比度验证 |
| 主题切换流畅 | E2E | R-502 | 1 | QA | 动画 200ms |
| 主题偏好持久化 | E2E | R-502 | 1 | QA | localStorage 保存 |
| SQL 代码块语法高亮 | E2E | - | 1 | QA | 关键字、字符串、数字 |
| 颜色对比度符合 WCAG | E2E | R-503 | 1 | QA | ≥ 4.5:1 |
| 加载动画流畅 | E2E | R-504 | 1 | QA | 60fps + 无卡顿 |
| 按钮交互反馈 | E2E | - | 1 | QA | Hover、Active、Disabled |
| 情感化设计反馈 | E2E | - | 2 | QA | 成功动画、错误提示 |
| 视觉一致性 | E2E | - | 1 | QA | 按钮、输入框样式统一 |
| 间距系统一致性 | E2E | - | 1 | QA | 4px 倍数规则 |

**Total P1**: 12 tests, 12 hours

### P2 (Medium) - Run nightly/weekly

**Criteria**: 次要体验 + 低风险 (1-2) + 无障碍支持

| Requirement | Test Level | Risk Link | Test Count | Owner | Notes |
| ----------- | ---------- | --------- | ---------- | ----- | ----- |
| 键盘导航支持 | E2E | R-505 | 2 | QA | Tab 切换 + Enter 确认 |
| 快捷键功能 | E2E | R-507 | 2 | QA | Ctrl+Enter、Ctrl+K |
| 焦点视觉指示 | E2E | R-505 | 1 | QA | 蓝色边框可见 |
| 语义化 HTML | E2E | R-506 | 1 | QA | header、main、aside、button |
| ARIA 标签基础支持 | E2E | R-508 | 2 | QA | aria-label、aria-live |
| 表单标签关联 | E2E | - | 1 | QA | label + input 关联 |
| 模态框焦点管理 | E2E | - | 1 | QA | 焦点移入/移出 |

**Total P2**: 10 tests, 5 hours

### P3 (Low) - Run on-demand

**Criteria**: 探索性测试

- 无（Epic 5 为 UX 优化，P0-P2 已覆盖关键场景）

**Total P3**: 0 tests, 0 hours

---

## Execution Order

### Smoke Tests (<5 min)

**Purpose**: 快速反馈，捕获 UI 破坏

- [ ] 页面在桌面端正常显示（1min）
- [ ] 主题切换工作正常（1min）
- [ ] 核心按钮可点击（1min）

**Total**: 3 scenarios

### P0 Tests (<15 min)

**Purpose**: 验证响应式设计和核心可用性

- [ ] 桌面端核心功能（E2E, 2min）
- [ ] 平板端核心功能（E2E, 2min）
- [ ] 断点切换流畅（E2E, 2min）
- [ ] 上传功能在所有设备（E2E, 2min）
- [ ] SQL 生成在所有设备（E2E, 2min）

**Total**: 5 scenarios

### P1 Tests (<30 min)

**Purpose**: 视觉设计和交互体验

- [ ] 浅色主题（E2E, 1min）
- [ ] 深色主题（E2E, 1min）
- [ ] 主题切换（E2E, 1min）
- [ ] 主题持久化（E2E, 1min）
- [ ] 语法高亮（E2E, 1min）
- [ ] 颜色对比度（E2E, 1min）
- [ ] 加载动画（E2E, 1min）
- [ ] 按钮反馈（E2E, 1min）
- [ ] 情感化设计（E2E, 2min）
- [ ] 视觉一致性（E2E, 1min）
- [ ] 间距一致性（E2E, 1min）

**Total**: 12 scenarios

### P2/P3 Tests (<20 min)

**Purpose**: 无障碍和键盘支持

- [ ] Tab 导航（E2E, 2min）
- [ ] 快捷键（E2E, 2min）
- [ ] 焦点指示（E2E, 1min）
- [ ] 语义化 HTML（E2E, 1min）
- [ ] ARIA 标签（E2E, 2min）
- [ ] 表单标签（E2E, 1min）
- [ ] 模态框焦点（E2E, 1min）

**Total**: 10 scenarios

---

## Resource Estimates

### Test Development Effort

| Priority | Count | Hours/Test | Total Hours | Notes |
| -------- | ----- | ---------- | ----------- | ----- |
| P0 | 5 | 2.0 | 10 | 响应式测试（多设备） |
| P1 | 12 | 1.0 | 12 | 视觉和交互验证 |
| P2 | 10 | 0.5 | 5 | 无障碍测试 |
| P3 | 0 | 0.25 | 0 | 无 |
| **Total** | **27** | **-** | **27** | **~3.5 days** |

### Prerequisites

**Test Data:**

- 无需特殊测试数据（UI 测试）

**Tooling:**

- **Playwright**（E2E 视觉和交互测试）
- **Playwright Emulation**（多设备和断点测试）
- **axe-core**（无障碍测试，颜色对比度验证）
- **Chrome DevTools Lighthouse**（性能和无障碍评分）
- **Percy 或 Chromatic**（可选，视觉回归测试）

**Environment:**

- 前端开发服务器（npm run dev）
- 或构建产物（npm run build）
- 多种屏幕尺寸（1920px、1280px、1024px、768px）

---

## Quality Gate Criteria

### Pass/Fail Thresholds

- **P0 pass rate**: 100% (no exceptions)
- **P1 pass rate**: ≥95%
- **P2/P3 pass rate**: ≥90%
- **High-risk mitigations**: 100% complete (R-501)

### Coverage Targets

- **响应式设计**: 100%（桌面、平板核心功能可用）
- **视觉一致性**: ≥90%（组件样式统一）
- **颜色对比度**: 100%（WCAG 2.1 Level A）
- **键盘导航**: ≥80%（关键元素可访问）

### Non-Negotiable Requirements

- [ ] 桌面端和平板端核心功能 100% 可用
- [ ] 断点切换流畅无破坏
- [ ] 主题切换工作正常
- [ ] 颜色对比度符合 WCAG 2.1 Level A

---

## Mitigation Plans

### R-501: 响应式布局断点切换异常 (Score: 6)

**Mitigation Strategy:**
1. **响应式测试覆盖**：
   - **桌面端（>1024px）**：
     - 验证三栏布局显示（文件管理、对话、DDL 预览）
     - 验证所有功能可用（上传、生成、复制）
   - **平板端（768-1024px）**：
     - 验证两栏布局（对话 + 文件管理）
     - 验证 DDL 预览折叠，点击弹出
   - **移动端（<768px）**：
     - 验证单栏布局（对话优先）
     - 验证文件管理抽屉弹出
     
2. **断点切换测试**：
   - 从桌面 → 平板：布局自动调整
   - 从平板 → 移动：布局自动调整
   - 切换时内容不丢失（输入框内容保留）
   - 切换动画流畅（CSS transition）
   
3. **交互元素触摸优化**：
   - 移动端按钮最小 44×44px
   - 拖拽上传在触摸设备工作
   - 滑动关闭抽屉支持
   
4. **性能验证**：
   - 布局切换 < 300ms
   - 动画 60fps 流畅

**Owner:** FE Dev
**Timeline:** Sprint 3
**Status:** Planned
**Verification:** 
- Playwright 模拟多设备测试
- 真实设备验证（桌面、iPad、iPhone）
- 所有核心功能在所有支持设备可用

---

### R-502: 主题切换样式错乱 (Score: 4)

**Mitigation Strategy:**
1. 主题切换测试：
   - 浅色主题：验证所有组件样式正确
   - 深色主题：验证所有组件样式正确
   - 切换动画：验证 transition 流畅
   - 持久化：验证 localStorage 保存
2. 颜色 Token 管理：
   - 使用 CSS 变量统一管理颜色
   - 主题切换时更新 CSS 变量
   - 避免硬编码颜色
3. 视觉回归测试（可选）：
   - 使用 Percy/Chromatic 截图对比
   - 捕获样式意外变化

**Owner:** FE Dev
**Timeline:** Sprint 3
**Status:** Planned
**Verification:** E2E 测试 + 视觉审查

---

### R-503: 颜色对比度不足 (Score: 3)

**Mitigation Strategy:**
1. 使用 axe-core 自动检测对比度
2. 目标：文本与背景对比度 ≥ 4.5:1（WCAG 2.1 Level A）
3. 手动验证工具：WebAIM Contrast Checker
4. 修复对比度不足的组件

**Owner:** FE Dev
**Timeline:** Sprint 3
**Status:** Planned
**Verification:** axe-core 测试通过 + Lighthouse 无障碍评分 ≥ 80

---

## Assumptions and Dependencies

### Assumptions

1. 桌面端为主要使用场景（产品经理、数据分析师）
2. 移动端暂不支持（MVP 阶段）
3. 用户使用现代浏览器（Chrome 90+、Edge 90+、Firefox 88+、Safari 14+）

### Dependencies

1. Epic 1-4 完成（核心功能就绪）- Sprint 3 开始前
2. UI 组件库（Ant Design Vue）已集成 - Sprint 3 开始前
3. 响应式设计规范已定义（断点、布局）- Sprint 3 开始前

### Risks to Plan

- **Risk**: 真实设备测试资源不足
  - **Impact**: 响应式问题未发现
  - **Contingency**: 优先使用 Playwright 模拟，关键路径手动验证

---

## Follow-on Workflows

- Run `*atdd` to generate failing P0 tests (响应式布局).
- Run `*automate` for broader coverage (主题、键盘、无障碍).

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
- Epic: `_bmad-output/planning-artifacts/epics.md#epic-5`
- Architecture: `_bmad-output/planning-artifacts/architecture.md`

---

**Generated by**: BMad TEA Agent - Test Architect Module
**Workflow**: `_bmad/bmm/testarch/test-design`
**Version**: 4.0 (BMad v6)
