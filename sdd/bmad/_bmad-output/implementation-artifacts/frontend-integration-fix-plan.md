# 前端集成修复计划

**Date**: 2026-01-25  
**Issue**: 布局不符UX、使用Mock数据、缺少API Key配置

---

## 🎯 问题分析

### 1. 布局问题
**现状**：
- 左右50-50分屏已实现
- 历史标签栏使用 `a-tabs`
- 但可能在**视觉细节**上与UX不符（间距、字体、颜色）

**UX要求**：
- 顶部历史标签栏：**横向滚动**的标签组
- 紧凑间距（8-12px）
- 橙色高亮激活标签（#FF9500）
- 对话消息间距：12px
- 最小宽度：1280px

### 2. Mock数据问题  
**现状**：
- `handleSendMessage` 使用 `setTimeout` 模拟
- 返回写死的SQL：`SELECT * FROM users WHERE created_at > '2024-01-01'`
- 没有真实调用后端API

**需要集成**：
- 后端API：`POST /api/chat`
- 请求：`ChatRequest { message, file_id }`
- 响应：`ChatResponse { type, content, sql, explanation, references, intent }`
- **注意**：当前后端不支持SSE流式响应（MVP阶段）

### 3. API Key配置缺失
**现状**：
- 后端 `.env` 有 `GLM_API_KEY=test_api_key_placeholder`
- 前端没有配置界面
- 用户无法更新API Key

**解决方案**：
- 添加"设置"页面或Drawer
- 提供输入框配置API Key
- 使用localStorage持久化（或后端API更新）

---

## ✅ 修复步骤

### Step 1: 修复布局细节（高优先级）

**1.1 检查最小宽度限制**
- 在 `App.vue` 添加宽度检查
- 如果 < 1280px，显示提示信息

**1.2 优化历史标签栏样式**
- 确认标签横向滚动效果
- 激活标签橙色高亮
- 紧凑间距（gap: 8px）

**1.3 优化对话区域样式**
- 消息间距：12px
- 用户消息：右对齐，深灰背景（#262626）
- Assistant消息：左对齐，卡片背景（#1f1f1f）

**1.4 优化输入框**
- 占位符根据上下文动态变化
- "描述你想查询的数据..." / "继续优化..."

### Step 2: 集成真实后端API（高优先级）

**2.1 创建API Service**
```typescript
// src/services/api.ts
import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8000/api'

export interface ChatRequest {
  message: string
  file_id?: string
}

export interface ChatResponse {
  type: 'text' | 'sql'
  content: string
  sql?: string
  explanation?: string
  references?: any[]
  intent?: string
}

export const chatService = {
  async sendMessage(request: ChatRequest): Promise<ChatResponse> {
    const response = await axios.post(`${API_BASE_URL}/chat`, request)
    return response.data
  },
  
  async getChatHistory() {
    const response = await axios.get(`${API_BASE_URL}/chat/history`)
    return response.data
  }
}
```

**2.2 更新 MainLayout.vue**
- 移除 `setTimeout` mock逻辑
- 调用 `chatService.sendMessage()`
- 处理loading状态和错误

**2.3 处理响应数据**
- 根据 `response.type` 渲染不同UI
- type="sql"：显示SQL代码块 + 引用源
- type="text"：显示普通文本消息

### Step 3: 添加API Key配置（中优先级）

**3.1 创建Settings组件**
```vue
<!-- src/components/Settings.vue -->
<template>
  <a-drawer
    v-model:open="visible"
    title="⚙️ 系统设置"
    width="400"
    placement="right"
  >
    <a-form layout="vertical">
      <a-form-item label="DeepSeek API Key">
        <a-input-password 
          v-model:value="apiKey"
          placeholder="请输入您的 API Key"
          @change="handleSaveApiKey"
        />
        <div class="hint">
          <info-circle-outlined />
          API Key 将存储在本地，不会上传到服务器
        </div>
      </a-form-item>
      
      <a-form-item label="API Base URL">
        <a-input 
          v-model:value="apiBaseUrl"
          placeholder="http://localhost:8000/api"
          @change="handleSaveConfig"
        />
      </a-form-item>
      
      <a-button type="primary" @click="handleTestConnection">
        测试连接
      </a-button>
    </a-form>
  </a-drawer>
</template>
```

**3.2 集成到MainLayout**
- 右上角添加"设置"图标按钮
- 点击打开Settings Drawer
- 使用localStorage持久化API Key

**3.3 后端支持（可选）**
- 如果需要后端管理API Key
- 添加 `/api/settings` 端点
- POST更新、GET获取

### Step 4: 测试与验证（必需）

**4.1 集成测试**
- 上传DDL文件 ✓
- 发送查询 → 调用后端API ✓
- 显示生成的SQL ✓
- 引用源高亮 ✓
- 复制SQL功能 ✓
- 多轮对话 ✓

**4.2 错误处理**
- API调用失败提示
- 网络超时处理
- API Key未配置提示

---

## 📁 文件变更清单

### 新增文件
- `src/services/api.ts` - API服务封装
- `src/components/Settings.vue` - 设置页面

### 修改文件
- `src/layouts/MainLayout.vue` - 移除mock数据，集成真实API
- `src/App.vue` - 添加最小宽度检查（可选）
- `.env.development` - 添加 `VITE_API_BASE_URL`

---

## 🚀 执行顺序

1. ✅ **Step 1**：修复布局细节（20分钟）
2. ✅ **Step 2**：集成真实API（30分钟）
3. ✅ **Step 3**：添加Settings（20分钟）
4. ✅ **Step 4**：端到端测试（15分钟）

**总计**：约1.5小时

---

## ⚠️ 注意事项

1. **SSE流式响应**：
   - 当前后端不支持SSE（MVP阶段）
   - 未来可升级为流式响应（逐字显示SQL）

2. **CORS配置**：
   - 确保后端已配置CORS允许前端调用
   - 检查 `backend/main.py` 的CORS设置

3. **API Key安全**：
   - localStorage存储（客户端）
   - 仅在需要时传递给后端
   - 不要在前端代码中硬编码

---

## ✅ 完成标准

- [ ] 布局完全符合UX设计（间距、颜色、字体）
- [ ] 移除所有mock数据
- [ ] 真实API调用成功
- [ ] SQL正确显示和高亮
- [ ] 引用源正确展示
- [ ] 用户可配置API Key
- [ ] 错误处理完善
- [ ] 端到端测试通过
