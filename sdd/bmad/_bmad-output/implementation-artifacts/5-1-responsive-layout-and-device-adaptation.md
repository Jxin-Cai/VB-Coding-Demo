# Story 5.1: 响应式布局与设备适配

Status: ready-for-dev

## Story

As a **产品经理**,
I want **在不同设备（桌面、平板）上都能流畅使用系统**,
So that **无论使用什么设备，都能完整访问所有功能**。

## Acceptance Criteria

### 桌面端布局（≥ 1280px）
**Then** 三栏布局：文件管理（左）、对话区（中）、SQL 展示（右）

### 平板端布局（768px - 1279px）
**Then** 自适应单栏或两栏布局，功能完整

### 移动端（< 768px）
**Then** MVP 阶段暂不支持，显示提示："请在桌面端或平板端使用"

### 响应式测试
**Then** 在 Chrome DevTools 测试各尺寸，布局正常

## Tasks / Subtasks

- [ ] **任务 1**: 实现桌面端三栏布局
- [ ] **任务 2**: 实现平板端自适应布局
- [ ] **任务 3**: 实现移动端提示页面
- [ ] **任务 4**: 使用 CSS Grid/Flexbox
- [ ] **任务 5**: 测试响应式效果

## Dev Notes

**断点**:
- Desktop: ≥ 1280px
- Tablet: 768px - 1279px
- Mobile: < 768px（不支持）

**References**:
- [Source: epics.md # Story 5.1]

## Dev Agent Record
_待开发时填写_

---
**🎯 Story Status**: ready-for-dev
**📅 Created**: 2026-01-25
