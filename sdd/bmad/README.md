# RAG Text-to-SQL 

基于 RAG 的自然语言到 SQL 转换系统

## 🚀 快速开始

### 一键启动开发环境

**Linux / macOS**：
```bash
./start-dev.sh
```

**Windows**：
```cmd
start-dev.bat
```

启动后访问：
- **前端开发服务器**：http://localhost:5173
- **后端 API 服务**：http://localhost:8000
- **API 文档**：http://localhost:8000/docs

### 停止开发环境

**Linux / macOS**：
```bash
./stop-dev.sh
```

**Windows**：
```cmd
stop-dev.bat
```

---

## 📋 技术栈

### 前端
- **Vue.js 3** - 渐进式 JavaScript 框架
- **Vite** - 下一代前端构建工具
- **TypeScript** - JavaScript 超集
- **Ant Design Vue** - 企业级 UI 组件库
- **Pinia** - Vue 状态管理
- **Vue Router** - 路由管理

### 后端
- **FastAPI** - 现代化 Python Web 框架
- **LangChain** - LLM 应用开发框架
- **LangGraph** - Agent 状态机管理
- **ChromaDB** - 向量数据库
- **sqlparse** - SQL 解析器
- **Pydantic** - 数据验证

---

## 📂 项目结构

```
bmad/
├── frontend/              # 前端项目（Vue 3 + Vite）
│   ├── src/
│   │   ├── components/   # 组件
│   │   ├── views/        # 页面
│   │   ├── router/       # 路由
│   │   └── stores/       # 状态管理
│   └── vite.config.ts
│
├── backend/               # 后端项目（FastAPI + DDD）
│   ├── interface/         # API 层
│   │   ├── api/          # API 端点
│   │   └── dto/          # 数据传输对象
│   ├── application/       # 应用服务层
│   ├── domain/            # 领域层
│   │   ├── ddl/          # DDL 管理
│   │   ├── agent/        # Agent 编排
│   │   └── sql/          # SQL 生成
│   ├── infrastructure/    # 基础设施层
│   │   ├── llm/          # LLM 集成
│   │   ├── vector/       # 向量库
│   │   ├── parser/       # SQL Parser
│   │   └── logging/      # 日志系统
│   ├── tests/             # 测试
│   ├── config.py          # 配置管理
│   └── main.py            # 应用入口
│
├── start-dev.sh           # 启动开发环境（Linux/macOS）
├── stop-dev.sh            # 停止开发环境（Linux/macOS）
├── deploy.sh              # 生产部署脚本
├── DEPLOYMENT.md          # 部署文档
└── MONITORING.md          # 监控文档
```

---

## 🛠️ 开发指南

### 环境要求

- **Node.js**: 16+ 
- **Python**: 3.9+
- **npm**: 8+

### 首次安装

**1. 安装前端依赖**：
```bash
cd frontend
npm install
```

**2. 安装后端依赖**：
```bash
cd backend
pip install -r requirements.txt
```

**3. 配置环境变量**：
```bash
cd backend
cp .env.example .env
# 编辑 .env，配置 GLM_API_KEY
```

### 开发模式

**启动开发环境**：
```bash
./start-dev.sh
```

- 前端热更新：修改代码自动刷新
- 后端热重载：修改代码自动重启

**查看日志**：
```bash
# 后端日志
tail -f logs/backend.log

# 前端日志
tail -f logs/frontend.log
```

### 运行测试

**后端测试**：
```bash
cd backend
pytest tests/ -v
```

**前端测试**：
```bash
cd frontend
npm run test
```

---

## 📦 部署

### 生产部署（单进程）

**1. 执行部署脚本**：
```bash
./deploy.sh
```

**2. 启动应用**：
```bash
cd backend
python main.py
```

**3. 访问应用**：
- **应用地址**：http://localhost:8000

详见 [DEPLOYMENT.md](DEPLOYMENT.md)

---

## 📊 监控

### 健康检查

```bash
curl http://localhost:8000/api/health
```

返回系统健康状态：
```json
{
  "status": "healthy",
  "timestamp": "2026-01-25T10:30:45Z",
  "services": {
    "api": "running",
    "vector_store": "initialized",
    "llm_api": "connected"
  }
}
```

详见 [MONITORING.md](MONITORING.md)

---

## 🧪 测试

### 测试覆盖

- **单元测试**：业务逻辑和工具类
- **集成测试**：API 端点和服务集成
- **端到端测试**：完整用户流程

### 运行测试

```bash
# 所有测试
cd backend && pytest tests/ -v

# 单元测试
pytest tests/unit/ -v

# 集成测试
pytest tests/integration/ -v

# 测试覆盖率
pytest tests/ --cov=. --cov-report=html
```

---

## 📖 文档

- [DEPLOYMENT.md](DEPLOYMENT.md) - 部署指南
- [MONITORING.md](MONITORING.md) - 监控指南
- [Architecture](_bmad-output/planning-artifacts/architecture.md) - 架构设计
- [Epics](_bmad-output/planning-artifacts/epics.md) - 需求文档

---

## 🤝 贡献

本项目使用 **BMM (BMad Method)** 开发流程：

1. **需求分析** → 创建 Epic 和 Story
2. **架构设计** → 定义技术架构
3. **迭代开发** → 按 Story 实现功能
4. **代码审查** → 质量保障
5. **持续集成** → 自动化测试和部署

---

## 📝 License

[License Type] - 根据实际情况添加

---

## 👥 团队

由 BMad Method 驱动的开发团队

---

**📅 最后更新**: 2026-01-25  
**🔖 版本**: 0.1.0
