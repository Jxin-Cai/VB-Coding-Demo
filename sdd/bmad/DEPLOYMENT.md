# 部署文档 (DEPLOYMENT.md)

本文档说明如何部署 RAG Text-to-SQL 应用（单进程部署模式）。

## 📋 目录

- [部署前准备](#部署前准备)
- [快速部署](#快速部署)
- [详细部署步骤](#详细部署步骤)
- [环境变量配置](#环境变量配置)
- [启动应用](#启动应用)
- [验证部署](#验证部署)
- [常见问题排查](#常见问题排查)
- [生产环境建议](#生产环境建议)

---

## 部署前准备

### 环境要求

**必需**：
- Node.js 16+ （前端构建）
- Python 3.9+ （后端运行）
- npm 或 yarn （包管理）

**可选**：
- Git （版本控制）

### 依赖安装

**前端依赖**：
```bash
cd frontend
npm install
```

**后端依赖**：
```bash
cd backend
pip install -r requirements.txt
```

---

## 快速部署

### 一键部署（推荐）

**Linux / macOS**：
```bash
# 在项目根目录执行
./deploy.sh
```

**Windows**：
```cmd
REM 在项目根目录执行
deploy.bat
```

### 启动应用

```bash
cd backend
python main.py
```

访问应用：**http://localhost:8000**

---

## 详细部署步骤

### 步骤 1：前端构建

进入前端目录并执行构建：

```bash
cd frontend
npm run build
```

**预期输出**：
- 构建产物生成在 `frontend/dist` 目录
- 构建日志显示成功信息
- 产物包含：`index.html`、`assets/` 目录、`favicon.ico`

**构建产物示例**：
```
frontend/dist/
├── index.html
├── favicon.ico
└── assets/
    ├── index-[hash].js
    ├── index-[hash].css
    ├── vue-vendor-[hash].js
    └── ui-vendor-[hash].js
```

### 步骤 2：部署到后端

将前端构建产物复制到后端 `static` 目录：

**Linux / macOS**：
```bash
# 清空旧的 static 目录
rm -rf backend/static
mkdir -p backend/static

# 复制前端构建产物
cp -r frontend/dist/* backend/static/

# 设置文件权限
chmod -R 755 backend/static
```

**Windows**：
```cmd
REM 清空旧的 static 目录
rmdir /s /q backend\static
mkdir backend\static

REM 复制前端构建产物
xcopy /E /I /Y frontend\dist backend\static
```

**验证**：
```bash
ls backend/static
# 应该看到：index.html, assets/, favicon.ico
```

### 步骤 3：环境变量配置

创建 `.env` 文件（基于 `.env.example`）：

```bash
cd backend
cp .env.example .env
```

编辑 `.env` 文件，配置必需的环境变量（见下文）。

### 步骤 4：启动应用

```bash
cd backend
python main.py
```

**预期输出**：
```
INFO:     Started server process [12345]
INFO:     Waiting for application startup.
✅ Static files mounted from: /path/to/backend/static
INFO:     Application startup complete.
INFO:     Uvicorn running on http://0.0.0.0:8000 (Press CTRL+C to quit)
```

---

## 环境变量配置

### 必需的环境变量

在 `backend/.env` 文件中配置以下变量：

```env
# LLM API Configuration
GLM_API_KEY=your_api_key_here

# Logging Configuration
LOG_LEVEL=INFO

# Server Configuration
HOST=0.0.0.0
PORT=8000
```

### 配置说明

| 变量名 | 说明 | 默认值 | 必需 |
|--------|------|--------|------|
| `GLM_API_KEY` | 智谱 AI API 密钥 | - | ✅ 是 |
| `LOG_LEVEL` | 日志级别（DEBUG/INFO/WARNING/ERROR） | INFO | ❌ 否 |
| `HOST` | 服务器监听地址 | 0.0.0.0 | ❌ 否 |
| `PORT` | 服务器监听端口 | 8000 | ❌ 否 |

**⚠️ 安全提示**：
- 不要将 `.env` 文件提交到版本控制
- 生产环境使用专用 API 密钥
- 限制 API 密钥的访问权限

---

## 启动应用

### 开发环境

```bash
cd backend
python main.py
```

应用启动后访问：
- **前端界面**：http://localhost:8000
- **API 文档**：http://localhost:8000/docs
- **健康检查**：http://localhost:8000/health

### 生产环境

使用 Uvicorn 以生产模式启动：

```bash
cd backend
uvicorn main:app --host 0.0.0.0 --port 8000 --workers 4
```

**参数说明**：
- `--host 0.0.0.0`: 监听所有网络接口
- `--port 8000`: 监听端口
- `--workers 4`: 工作进程数（建议：CPU 核心数 × 2 + 1）

**使用 systemd（推荐）**：

创建 `/etc/systemd/system/rag-text-to-sql.service`：

```ini
[Unit]
Description=RAG Text-to-SQL Service
After=network.target

[Service]
Type=simple
User=www-data
WorkingDirectory=/path/to/backend
Environment="PATH=/path/to/venv/bin"
ExecStart=/path/to/venv/bin/uvicorn main:app --host 0.0.0.0 --port 8000 --workers 4
Restart=always

[Install]
WantedBy=multi-user.target
```

启动服务：
```bash
sudo systemctl enable rag-text-to-sql
sudo systemctl start rag-text-to-sql
sudo systemctl status rag-text-to-sql
```

---

## 验证部署

### 自动化测试

运行集成测试验证部署：

```bash
cd backend
pytest tests/integration/test_single_process_deployment.py -v
```

**预期结果**：所有测试通过（10/10）

### 手动验证

**1. 前端页面加载**：
```bash
curl -I http://localhost:8000/
# 预期：HTTP/1.1 200 OK
```

**2. API 健康检查**：
```bash
curl http://localhost:8000/api/health
# 预期：{"status":"healthy",...}
```

**3. 前端路由测试**：

访问以下地址，验证前端路由正常工作：
- http://localhost:8000/ （首页）
- http://localhost:8000/about （关于页面）
- http://localhost:8000/health （API 端点，返回 JSON）

**4. 浏览器测试**：

打开浏览器访问 http://localhost:8000，检查：
- ✅ 页面正常加载
- ✅ 浏览器控制台无错误
- ✅ 网络面板显示静态资源正确加载
- ✅ 前端路由切换正常

---

## 常见问题排查

### 问题 1：访问根路径 404

**症状**：访问 `http://localhost:8000/` 返回 404 Not Found

**原因**：`backend/static` 目录不存在或为空

**解决**：
```bash
# 重新执行部署脚本
./deploy.sh

# 验证 static 目录存在
ls backend/static
```

### 问题 2：静态资源 404（CSS/JS 加载失败）

**症状**：前端页面加载，但样式缺失或 JS 报错

**原因**：
1. Vite 构建配置问题
2. 文件路径不正确

**解决**：
1. 检查 `vite.config.ts` 中 `base` 配置：
   ```typescript
   export default defineConfig({
     base: '/', // 确保为根路径
   })
   ```
2. 重新构建并部署：
   ```bash
   ./deploy.sh
   ```

### 问题 3：API 调用被静态文件拦截

**症状**：API 调用返回 HTML 而不是 JSON

**原因**：静态文件挂载在 API 路由之前

**解决**：
确保 `main.py` 中 API 路由在静态文件挂载之前注册（已在 Story 1.2 中正确配置）

### 问题 4：Vue Router 刷新页面 404

**症状**：前端路由（如 `/about`）刷新后显示 404

**原因**：`StaticFiles` 未正确配置 SPA 支持

**解决**：
已在 Story 1.2 中使用自定义 `SPAStaticFiles` 类解决（见 `main.py`）

### 问题 5：部署脚本权限错误

**症状**：执行 `./deploy.sh` 提示权限拒绝

**原因**：脚本没有执行权限

**解决**：
```bash
chmod +x deploy.sh
./deploy.sh
```

### 问题 6：端口被占用

**症状**：启动时提示端口 8000 已被使用

**原因**：其他进程占用端口

**解决**：
```bash
# 查找占用端口的进程
lsof -i :8000

# 杀死进程
kill -9 <PID>

# 或者使用不同端口
PORT=8001 python main.py
```

---

## 生产环境建议

### 性能优化

**1. 启用 Gzip 压缩**：

在 `main.py` 中添加：
```python
from fastapi.middleware.gzip import GZipMiddleware

app.add_middleware(GZipMiddleware, minimum_size=1000)
```

**2. 配置静态资源缓存**：

使用反向代理（如 Nginx）配置缓存策略：
```nginx
location /assets/ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

**3. 使用 CDN**：

将静态资源部署到 CDN，提升全球访问速度。

### 安全配置

**1. 关闭 Sourcemap**：

确保 `vite.config.ts` 中：
```typescript
build: {
  sourcemap: false, // 生产环境必须关闭
}
```

**2. 限制 CORS**：

生产环境移除开发服务器地址：
```python
app.add_middleware(
    CORSMiddleware,
    allow_origins=["https://yourdomain.com"],  # 仅允许生产域名
)
```

**3. 配置 HTTPS**：

使用反向代理（Nginx）配置 SSL：
```nginx
server {
    listen 443 ssl;
    server_name yourdomain.com;
    
    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;
    
    location / {
        proxy_pass http://localhost:8000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

**4. 环境变量安全**：

- 使用密钥管理服务（AWS Secrets Manager、HashiCorp Vault）
- 定期轮换 API 密钥
- 限制最小权限原则

### 监控与日志

**1. 日志配置**：

配置结构化日志输出：
```python
import logging
logging.basicConfig(
    level=settings.log_level,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
```

**2. 健康检查**：

定期检查 `/health` 端点：
```bash
# 使用 cron 定时检查
*/5 * * * * curl -f http://localhost:8000/health || systemctl restart rag-text-to-sql
```

**3. 性能监控**：

- 使用 Prometheus + Grafana 监控指标
- 配置 APM 工具（如 New Relic、Datadog）
- 监控 API 响应时间和错误率

### 备份策略

**1. 代码备份**：
- 使用 Git 版本控制
- 定期推送到远程仓库

**2. 配置备份**：
- 备份 `.env` 文件（加密存储）
- 备份数据库（如使用）

---

## 附录

### 项目结构（部署后）

```
project/
├── frontend/
│   ├── dist/                 # 前端构建产物（部署前）
│   └── ...
├── backend/
│   ├── static/               # 前端静态文件（部署后）
│   │   ├── index.html
│   │   ├── assets/
│   │   └── favicon.ico
│   ├── interface/
│   ├── application/
│   ├── domain/
│   ├── infrastructure/
│   ├── config.py
│   ├── main.py
│   └── .env
├── deploy.sh                 # 部署脚本（Linux/macOS）
├── deploy.bat                # 部署脚本（Windows）
└── DEPLOYMENT.md             # 本文档
```

### 相关文档

- [README.md](README.md) - 项目介绍
- [Architecture.md](_bmad-output/planning-artifacts/architecture.md) - 架构设计
- [Epic 1](_bmad-output/planning-artifacts/epics.md) - 系统基础设施

---

**📅 文档版本**: v1.0  
**📅 最后更新**: 2026-01-25  
**✍️ 维护者**: 开发团队
