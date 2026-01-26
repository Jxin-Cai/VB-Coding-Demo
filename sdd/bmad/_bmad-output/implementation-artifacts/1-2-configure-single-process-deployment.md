# Story 1.2: 配置单进程部署架构

Status: review

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a **开发工程师**,
I want **配置单进程部署流程，前端构建产物由后端静态服务提供**,
So that **部署简化为单个 Python 进程启动，减少运维复杂度**。

## Acceptance Criteria

### 前端构建验收标准

**Given** 前端项目已完成开发

**When** 执行前端构建命令 `npm run build`

**Then** 前端编译产物生成在 `frontend/dist` 目录
- **And** 产物包含优化后的 HTML、CSS、JS 文件
- **And** 构建日志显示成功信息

### 部署脚本验收标准

**Given** 前端构建完成

**When** 配置部署脚本将 `frontend/dist` 复制到 `backend/static`

**Then** 部署脚本成功执行
- **And** `backend/static` 目录包含完整的前端文件
- **And** 文件权限正确，可被后端服务读取

### 静态文件服务验收标准

**Given** 后端已配置静态文件服务

**When** 在 FastAPI `main.py` 中挂载静态文件目录

**Then** 配置如下代码成功添加：
```python
from fastapi.staticfiles import StaticFiles

app.mount("/", StaticFiles(directory="static", html=True), name="static")
```
- **And** 访问根路径 `/` 返回前端 `index.html`
- **And** 静态资源（CSS、JS、图片）正确加载

### 单进程部署验收标准

**Given** 单进程部署已配置

**When** 仅启动后端 Python 进程 `python main.py`

**Then** 前端界面在 `http://localhost:8000` 可访问
- **And** 前端 API 调用正确路由到后端接口
- **And** 前后端功能完整，无需单独启动前端服务

### 部署文档验收标准

**Given** 部署架构已验证

**When** 创建部署文档 `DEPLOYMENT.md`

**Then** 文档包含以下内容：
- **And** 构建步骤（前端 build + 复制到 static）
- **And** 启动命令（单进程启动）
- **And** 环境变量配置说明
- **And** 端口和网络配置

## Tasks / Subtasks

### 任务 1: 配置前端构建输出 (AC: 前端构建验收标准)

- [x] **1.1 验证 Vite 构建配置**
  - 检查 `vite.config.ts` 中的 `build.outDir` 设置为 `dist`
  - 确认构建优化配置（minify、splitChunks）
  - 验证 base 路径配置（如需要）

- [x] **1.2 执行构建测试**
  - 运行 `npm run build`
  - 验证 `frontend/dist` 目录生成
  - 检查构建产物（index.html、assets/、favicon 等）
  - 确认构建日志无错误

### 任务 2: 创建部署脚本 (AC: 部署脚本验收标准)

- [x] **2.1 创建 deploy.sh 脚本**
  - 在项目根目录创建 `deploy.sh` 脚本
  - 脚本步骤：
    1. 进入前端目录，执行 `npm run build`
    2. 清空 `backend/static` 目录（如存在）
    3. 复制 `frontend/dist/*` 到 `backend/static/`
    4. 设置正确的文件权限
    5. 输出部署成功信息

- [x] **2.2 创建 deploy.bat 脚本（Windows 支持）**
  - 创建 Windows 批处理脚本
  - 实现与 deploy.sh 相同的功能

- [x] **2.3 测试部署脚本**
  - 执行 `./deploy.sh` 验证脚本正常运行
  - 检查 `backend/static` 目录内容
  - 验证文件权限

### 任务 3: 配置 FastAPI 静态文件服务 (AC: 静态文件服务验收标准)

- [x] **3.1 修改 main.py 添加静态文件挂载**
  - 导入 `StaticFiles`：`from fastapi.staticfiles import StaticFiles`
  - 在 API 路由注册之后挂载静态文件：
    ```python
    app.mount("/", StaticFiles(directory="static", html=True), name="static")
    ```
  - **注意**：静态文件挂载必须在所有 API 路由注册之后

- [x] **3.2 调整路由优先级**
  - 确保 API 路由（如 `/api/*`）在静态文件挂载之前注册
  - 验证 API 路由优先级高于静态文件路由

- [x] **3.3 测试静态文件服务**
  - 启动后端：`python main.py`
  - 访问 `http://localhost:8000/` 验证前端加载
  - 检查浏览器开发者工具，确认静态资源正确加载
  - 验证 API 调用正常工作

### 任务 4: 验证单进程部署 (AC: 单进程部署验收标准)

- [x] **4.1 完整部署流程测试**
  - 停止所有前端开发服务器
  - 执行部署脚本：`./deploy.sh`
  - 启动后端：`python main.py`
  - 仅通过 `http://localhost:8000` 访问应用

- [x] **4.2 功能完整性验证**
  - 验证前端页面正确渲染
  - 测试前端路由（Vue Router）正常工作
  - 验证 API 调用成功（前端 → 后端 API）
  - 检查浏览器控制台无错误

### 任务 5: 创建部署文档 (AC: 部署文档验收标准)

- [x] **5.1 创建 DEPLOYMENT.md**
  - 在项目根目录创建文档
  - 包含以下章节：
    - 部署前准备（环境要求）
    - 构建步骤（详细命令）
    - 部署步骤（脚本使用）
    - 启动命令
    - 环境变量配置
    - 端口和网络配置
    - 常见问题排查

- [x] **5.2 添加快速部署指南**
  - 提供一键部署命令示例
  - 说明生产环境部署注意事项

## Dev Notes

### 核心架构决策

**单进程部署架构**：
- ✅ **前端编译产物 → 后端静态服务**
- **优势**：
  - 简化部署流程（单个进程）
  - 减少端口管理（统一端口）
  - 降低运维复杂度
  - 避免 CORS 问题（同域）

**技术实现**：
- 前端：Vite 构建优化产物
- 后端：FastAPI `StaticFiles` 中间件
- 部署：Shell 脚本自动化

### 前端构建配置

**Vite 配置 (vite.config.ts)**：
```typescript
export default defineConfig({
  plugins: [vue()],
  build: {
    outDir: 'dist',
    assetsDir: 'assets',
    minify: 'terser',
    sourcemap: false, // 生产环境建议关闭
    rollupOptions: {
      output: {
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'ui-vendor': ['ant-design-vue']
        }
      }
    }
  },
  // 开发环境代理配置保持不变
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8000',
        changeOrigin: true
      }
    }
  }
})
```

**关键配置说明**：
- `outDir`: 构建输出目录（默认 `dist`）
- `assetsDir`: 静态资源子目录
- `minify`: 代码压缩（推荐 `terser`）
- `manualChunks`: 代码分割策略（优化加载性能）

### 部署脚本实现

**deploy.sh 脚本示例**：
```bash
#!/bin/bash

set -e  # 遇到错误立即退出

echo "🚀 Starting deployment..."

# Step 1: Build frontend
echo "📦 Building frontend..."
cd frontend
npm run build
cd ..

# Step 2: Clean backend static directory
echo "🧹 Cleaning backend static directory..."
rm -rf backend/static
mkdir -p backend/static

# Step 3: Copy frontend build to backend static
echo "📋 Copying frontend build to backend..."
cp -r frontend/dist/* backend/static/

# Step 4: Set permissions
echo "🔒 Setting file permissions..."
chmod -R 755 backend/static

echo "✅ Deployment completed successfully!"
echo "💡 Start server: cd backend && python main.py"
```

**deploy.bat 脚本示例（Windows）**：
```batch
@echo off
echo Starting deployment...

echo Building frontend...
cd frontend
call npm run build
cd ..

echo Cleaning backend static directory...
if exist backend\static rmdir /s /q backend\static
mkdir backend\static

echo Copying frontend build to backend...
xcopy /E /I /Y frontend\dist backend\static

echo Deployment completed successfully!
echo Start server: cd backend && python main.py
```

### FastAPI 静态文件配置

**main.py 关键代码**：
```python
from fastapi import FastAPI
from fastapi.staticfiles import StaticFiles
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(title="RAG Text-to-SQL API")

# CORS configuration (开发环境)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173"],  # 开发时保留
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# API routes must be registered BEFORE static files
@app.get("/api/health")
async def health_check():
    return {"status": "healthy"}

# Include other API routers here...
# app.include_router(file_router, prefix="/api/files")
# app.include_router(chat_router, prefix="/api/chat")

# Static files MUST be mounted LAST
# This catches all remaining routes and serves frontend
app.mount("/", StaticFiles(directory="static", html=True), name="static")
```

**关键要点**：
- ⚠️ **API 路由必须在静态文件挂载之前注册**
- ⚠️ **静态文件挂载必须是最后一步**
- `html=True`: 启用 HTML 模式，根路径返回 `index.html`
- `directory="static"`: 相对于 `main.py` 的目录

### 路由优先级

**路由匹配顺序**：
1. **API 路由优先**：`/api/*` 先匹配，由 API Router 处理
2. **静态文件兜底**：其他路径由 StaticFiles 处理，返回前端资源或 `index.html`

**Vue Router 兼容性**：
- Vue Router 使用 HTML5 History 模式
- `StaticFiles(html=True)` 自动处理：任何未匹配路由返回 `index.html`
- 前端路由在客户端生效

### 环境区分

**开发环境**：
- 前端：`npm run dev` 在 `http://localhost:5173`
- 后端：`python main.py` 在 `http://localhost:8000`
- 前端通过 Vite proxy 访问后端 API

**生产环境（单进程）**：
- 执行部署脚本：`./deploy.sh`
- 启动后端：`python main.py`
- 访问：`http://localhost:8000`（前后端统一）

### 部署检查清单

**构建前检查**：
- [ ] 前端代码无 ESLint 错误
- [ ] 前端代码无 TypeScript 错误
- [ ] 环境变量正确配置（`.env`）

**构建检查**：
- [ ] `npm run build` 成功
- [ ] `frontend/dist` 目录生成
- [ ] 构建产物大小合理（检查是否过大）

**部署检查**：
- [ ] `backend/static` 包含所有前端文件
- [ ] `index.html` 存在于 `backend/static`
- [ ] `assets/` 目录包含 JS 和 CSS 文件

**运行检查**：
- [ ] 后端启动无错误
- [ ] 访问 `http://localhost:8000/` 显示前端页面
- [ ] API 调用正常（检查浏览器 Network 面板）
- [ ] 前端路由切换正常

### 常见问题

**问题 1**: 访问根路径显示 404
- **原因**: 静态文件目录路径不正确
- **解决**: 检查 `StaticFiles(directory="static")` 路径，确保相对于 `main.py`

**问题 2**: 静态资源 404（CSS/JS 加载失败）
- **原因**: Vite 构建的 `base` 路径配置错误
- **解决**: 检查 `vite.config.ts` 中 `base` 配置，默认为 `/`

**问题 3**: API 调用被静态文件拦截
- **原因**: 静态文件挂载在 API 路由之前
- **解决**: 确保 `app.mount("/", ...)` 在所有 API 路由注册之后

**问题 4**: Vue Router 刷新页面 404
- **原因**: `StaticFiles` 未启用 HTML 模式
- **解决**: 使用 `StaticFiles(directory="static", html=True)`

**问题 5**: 部署脚本权限错误
- **原因**: `deploy.sh` 没有执行权限
- **解决**: `chmod +x deploy.sh`

### Project Structure Notes

**部署后目录结构**：
```
backend/
├── static/                    # 前端构建产物（由部署脚本生成）
│   ├── index.html             # 前端入口
│   ├── assets/                # 静态资源
│   │   ├── index-[hash].js    # 主 JS bundle
│   │   ├── index-[hash].css   # 主 CSS bundle
│   │   └── ...                # 其他资源
│   └── favicon.ico            # 网站图标
├── interface/
├── application/
├── domain/
├── infrastructure/
├── config.py
└── main.py                    # FastAPI 入口（静态文件服务）
```

### 生产环境注意事项

**性能优化**：
- 前端构建时启用代码分割
- 启用 gzip 压缩（FastAPI 中间件）
- 配置静态资源缓存策略

**安全建议**：
- 生产环境关闭 sourcemap
- 限制 CORS 允许的源
- 配置 HTTPS（反向代理）

**监控建议**：
- 添加静态文件访问日志
- 监控前端资源加载时间
- 监控 API 响应时间

### References

**Architecture 文档关键章节**：
- [Source: architecture.md # Deployment Architecture] - 单进程部署架构详细说明
- [Source: architecture.md # Frontend Build Configuration] - 前端构建配置
- [Source: architecture.md # Static File Serving] - FastAPI 静态文件服务
- [Source: architecture.md # Routing Priority] - 路由优先级说明

**Epic 文档**：
- [Source: epics.md # Epic 1: 系统基础设施与可观测性] - Epic 上下文
- [Source: epics.md # Story 1.2] - Story 完整需求和验收标准

**前置依赖**：
- [Story 1.1: Initialize Starter Template] - 前后端项目已初始化

**技术文档**：
- Vite Build Configuration: https://vitejs.dev/config/build-options.html
- FastAPI Static Files: https://fastapi.tiangolo.com/tutorial/static-files/
- Vue Router History Mode: https://router.vuejs.org/guide/essentials/history-mode.html

## Dev Agent Record

### Agent Model Used

Claude Sonnet 4.5 (Amelia - Developer Agent)

### Debug Log References

**问题 1**：初次构建时 Vite 提示缺少 terser 依赖
- **解决**：修改 `vite.config.ts`，使用 `minify: 'esbuild'`（更快、内置）
- **影响**：无，esbuild 压缩效果与 terser 相当

### Implementation Plan

**任务 1**：配置前端构建
- 在 `vite.config.ts` 添加 build 配置（outDir、minify、代码分割）
- 执行 `npm run build` 生成构建产物

**任务 2**：创建部署脚本
- 创建 `deploy.sh`（Linux/macOS）和 `deploy.bat`（Windows）
- 脚本自动化：前端构建 → 清空 static → 复制产物 → 设置权限

**任务 3**：配置 FastAPI 静态文件服务
- 修改 `main.py`，启用 StaticFiles 导入
- 确保 API 路由在静态文件挂载之前注册
- 创建自定义 `SPAStaticFiles` 类支持 Vue Router History 模式

**任务 4**：验证单进程部署
- 执行部署脚本
- 启动后端，验证前后端在同一进程中运行
- 测试前端页面、API 调用、路由切换

**任务 5**：创建部署文档
- 编写 `DEPLOYMENT.md`，包含快速部署、详细步骤、环境配置、故障排查、生产建议

**测试策略**：
- 创建 10 个集成测试验证单进程部署功能
- 验证静态文件服务、API 路由优先级、Vue Router 支持
- 运行完整测试套件（33 个测试）确保无回归

### Completion Notes List

**✅ 前端构建配置完成**：
- 在 `vite.config.ts` 添加完整 build 配置
- 配置代码分割（vue-vendor、ui-vendor）
- 使用 esbuild 压缩（minify: 'esbuild'）
- 构建产物生成在 `frontend/dist`（验证通过）

**✅ 部署脚本创建完成**：
- `deploy.sh` 创建并设置执行权限（chmod +x）
- `deploy.bat` 创建（Windows 支持）
- 脚本自动化流程验证通过：构建 → 清空 → 复制 → 设置权限
- `backend/static` 目录包含完整前端文件

**✅ FastAPI 静态文件服务配置完成**：
- 启用 `StaticFiles` 导入
- API 路由在前（/health、/api/health）
- 静态文件挂载在最后（app.mount）
- 创建自定义 `SPAStaticFiles` 类，完美支持 Vue Router History 模式
- 静态目录存在性检查（提示未部署时运行脚本）

**✅ 单进程部署验证成功**：
- 根路径 `/` 返回前端页面（HTTP 200）
- API 路由 `/api/health` 正常工作（返回 JSON）
- `/health` 端点正常工作
- Vue Router 路径（如 `/about`）正确返回 index.html
- 前端由后端静态服务提供，无需单独启动

**✅ 部署文档完成**：
- `DEPLOYMENT.md` 创建，包含 8 个主要章节
- 快速部署指南（一键脚本）
- 详细部署步骤（分步说明）
- 环境变量配置（完整说明）
- 常见问题排查（6 个常见问题）
- 生产环境建议（性能、安全、监控）

**✅ 测试覆盖完整**：
- 10 个集成测试全部通过
- 完整测试套件 33/33 通过（无回归）
- 测试覆盖：静态文件、API 路由、Vue Router、部署脚本

### File List

**配置文件**（修改）：
- `frontend/vite.config.ts` - 添加 build 配置

**部署脚本**（新增）：
- `deploy.sh` - Linux/macOS 部署脚本
- `deploy.bat` - Windows 部署脚本

**后端文件**（修改）：
- `backend/main.py` - 添加静态文件服务和 SPAStaticFiles 类

**部署产物**（生成）：
- `backend/static/index.html`
- `backend/static/favicon.ico`
- `backend/static/assets/*.js` - JavaScript bundles
- `backend/static/assets/*.css` - CSS bundles

**测试文件**（新增）：
- `backend/tests/integration/test_single_process_deployment.py` - 单进程部署测试

**文档**（新增）：
- `DEPLOYMENT.md` - 部署文档（详细指南）

## Change Log

### 2026-01-25 - Story 完成
- ✅ 前端构建配置完成（Vite build 配置，代码分割优化）
- ✅ 部署脚本创建完成（deploy.sh + deploy.bat）
- ✅ FastAPI 静态文件服务配置完成（自定义 SPAStaticFiles 类）
- ✅ 单进程部署验证通过（前后端同一进程）
- ✅ 部署文档创建完成（DEPLOYMENT.md）
- ✅ 集成测试通过（10/10，总计 33/33）
- ✅ Vue Router History 模式支持验证通过

---

**🎯 Story Status**: review

**📅 Created**: 2026-01-25
**📅 Completed**: 2026-01-25

**✅ Story Implementation Complete**
- All tasks and subtasks completed
- All acceptance criteria satisfied
- 10 integration tests passing (33 total with Story 1.1)
- Single-process deployment verified and working
- Ready for code review
