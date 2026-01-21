## ADDED Requirements

### Requirement: 需求池列表导航
系统 SHALL 在需求池详情页面提供清晰的导航控件，允许用户返回到需求池列表页面。

#### Scenario: 从需求池详情返回列表
- **WHEN** 用户在需求池详情页面
- **AND** 用户点击"返回需求池列表"按钮
- **THEN** 系统导航到需求池列表页面
- **AND** 保留用户的登录状态

#### Scenario: 返回按钮可见性
- **WHEN** 用户进入需求池详情页面
- **THEN** 页面顶部显示明显的"返回需求池列表"按钮
- **AND** 按钮包含左箭头图标
- **AND** 按钮使用主题色突出显示

### Requirement: 需求池API集成
前端 MUST 通过配置的API实例调用需求池管理接口，确保统一的错误处理、请求拦截和响应拦截。

#### Scenario: 获取需求池列表
- **WHEN** 用户访问需求池列表页面
- **THEN** 前端通过 `api.getPools()` 调用后端接口
- **AND** 后端返回所有需求池及其故事卡数量
- **AND** 每个需求池包含 id, name, description, createdBy, createdAt, storyCardCount 字段

#### Scenario: 创建需求池
- **WHEN** 用户填写需求池名称和描述并提交
- **THEN** 前端通过 `api.createPool(data)` 调用后端接口
- **AND** 后端创建需求池并返回完整的需求池信息
- **AND** 前端刷新需求池列表

#### Scenario: 获取需求池详情
- **WHEN** 用户访问特定需求池的故事卡列表
- **THEN** 前端通过 `api.getPoolById(poolId)` 调用后端接口
- **AND** 后端返回需求池的详细信息
- **AND** 前端在页面顶部显示需求池名称和描述

## MODIFIED Requirements

### Requirement: 需求池数据完整性
系统 MUST 确保需求池实体包含所有必需的字段，并且前端和后端的数据结构保持一致。

#### Scenario: 需求池包含ID字段
- **GIVEN** 系统初始化时创建默认需求池
- **WHEN** 插入默认需求池到数据库
- **THEN** 需求池包含自增的 id 字段
- **AND** id 字段在返回给前端时正确映射

#### Scenario: 需求池字段验证
- **WHEN** 前端接收到需求池数据
- **THEN** 每个需求池对象包含以下字段：
  - id: Long (需求池唯一标识)
  - name: String (需求池名称，1-100字符)
  - description: String (需求池描述，最多500字符)
  - createdBy: String (创建者用户名)
  - createdAt: LocalDateTime (创建时间)
  - storyCardCount: Integer (故事卡数量)
