## ADDED Requirements

### Requirement: 全局CSS变量系统
系统 MUST 使用全局CSS变量定义设计系统的颜色、间距、动画等属性，确保整个应用的视觉一致性。

#### Scenario: 颜色变量定义
- **WHEN** 前端应用启动时
- **THEN** 系统定义以下CSS变量：
  - `--color-bg-primary`: 主背景色 (#0a0a0a)
  - `--color-bg-secondary`: 次要背景色 (rgba(26, 26, 26, 0.6))
  - `--color-bg-tertiary`: 第三级背景色
  - `--color-text-primary`: 主文本色 (#e0e0e0)
  - `--color-text-secondary`: 次要文本色 (#a0a0a0)
  - `--color-border-primary`: 主边框色 (rgba(255, 255, 255, 0.1))
  - `--color-accent-blue`: 蓝色强调色 (#00d4ff)
  - `--color-accent-green`: 绿色强调色 (#00ff88)

#### Scenario: 组件使用CSS变量
- **WHEN** 开发者创建或修改组件样式
- **THEN** 组件必须使用预定义的CSS变量
- **AND** 禁止在组件中硬编码颜色值
- **AND** 所有颜色引用应使用 var(--color-*) 格式

### Requirement: 样式一致性检查
系统 SHALL 确保所有对话框、卡片和表单组件使用一致的设计语言和视觉样式。

#### Scenario: 对话框样式统一
- **WHEN** 任何对话框组件被渲染
- **THEN** 对话框背景使用 `--color-bg-secondary`
- **AND** 对话框边框使用 `--color-border-primary`
- **AND** 对话框阴影使用预定义的阴影变量
- **AND** 对话框文本使用 `--color-text-primary`

#### Scenario: 表单控件样式统一
- **WHEN** 任何表单控件（输入框、下拉框等）被渲染
- **THEN** 控件背景使用 `--color-bg-tertiary`
- **AND** 控件边框使用 `--color-border-primary`
- **AND** 控件文本使用 `--color-text-primary`
- **AND** 控件焦点状态使用 `--color-accent-blue`

### Requirement: 设计文档维护
系统 MUST 维护设计系统的文档，确保开发者了解如何正确使用CSS变量和样式规范。

#### Scenario: 样式指南文档
- **GIVEN** 项目包含全局CSS变量系统
- **WHEN** 新开发者加入项目
- **THEN** 他们可以在 `openspec/project.md` 中找到样式规范说明
- **AND** 文档列出所有可用的CSS变量及其用途
- **AND** 文档提供使用示例和最佳实践

#### Scenario: 样式审查流程
- **WHEN** 开发者提交包含样式变更的代码
- **THEN** 审查者检查是否使用了CSS变量
- **AND** 如果发现硬编码颜色值，要求开发者修复
- **AND** 确保新样式符合全局设计系统
