# Change: 修复需求池管理功能和统一全局样式

## Why

当前需求池管理功能存在多个问题，影响用户体验：
1. 用户进入需求池后缺少明显的退出/返回按钮
2. 需求池列表API路径不匹配（前端调用 `/api/pools`，但使用直接axios而非配置的api实例）
3. 创建需求池的对话框样式使用硬编码颜色，与全局CSS变量系统不匹配
4. 需要统一全局样式系统，避免未来再次出现样式不一致问题

## What Changes

- **修复返回按钮**: 增强需求池详情页的返回按钮可见性和交互体验
- **修复API调用**: 修正前端需求池相关API调用，确保使用配置好的api实例
- **统一样式系统**: 将所有硬编码颜色值替换为CSS变量
- **完善全局样式规范**: 创建全局样式变量文档，确保未来开发遵循统一规范

## Impact

- Affected specs: backlog-pools, ui-design-system
- Affected code:
  - 前端: `frontend/src/views/PoolList.vue`, `frontend/src/views/Backlog.vue`, `frontend/src/api/index.js`
  - 新增: 全局样式变量定义文件
