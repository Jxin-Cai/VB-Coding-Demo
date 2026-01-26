# Story 1.1: 初始化 Starter Template（前端 + 后端）

Status: review

<!-- Note: Validation is optional. Run validate-create-story for quality check before dev-story. -->

## Story

As a **开发工程师**,
I want **使用官方 Starter 初始化前端项目并创建后端 DDD 架构**,
So that **项目基础结构标准化，开发环境快速就绪，可以立即开始功能开发**。

## Acceptance Criteria

### 前端初始化验收标准

**Given** 开发环境已安装 Node.js 16+ 和 Python 3.9+

**When** 执行前端初始化命令 `npm create vue@latest rag-text-to-sql-frontend`

**Then** 前端项目创建成功，包含以下配置：
- **And** Vue 3 + Vite + TypeScript 已启用
- **And** Vue Router 和 Pinia 已集成
- **And** ESLint 和 Prettier 已配置
- **And** 项目目录结构符合 Vue 官方最佳实践

**Given** 前端项目已创建

**When** 安装 UI 组件库（Ant Design Vue 或 Element Plus）

**Then** UI 组件库成功安装并可以导入使用
- **And** 全局样式配置完成

### 后端初始化验收标准

**Given** 后端目录已创建

**When** 创建 DDD 分层架构目录结构

**Then** 以下目录结构创建成功：
```
backend/
├── interface/          # API 层
├── application/        # 应用服务层
├── domain/             # 领域层
├── infrastructure/     # 基础设施层
├── config.py           # Pydantic Settings
├── main.py             # FastAPI 入口
└── requirements.txt    # Python 依赖
```
- **And** 每个层级包含 `__init__.py` 文件

### 环境配置验收标准

**Given** DDD 架构目录已创建

**When** 配置环境变量管理（创建 `.env.example` 和 `config.py`）

**Then** `.env.example` 包含必需的环境变量模板（GLM_API_KEY、LOG_LEVEL）
- **And** `config.py` 使用 Pydantic Settings 加载环境变量
- **And** 敏感信息不写入代码或版本控制

### 项目运行验收标准

**Given** 前后端项目已初始化

**When** 执行项目验证命令（前端 `npm run dev`，后端 `python main.py`）

**Then** 前端开发服务器在 `http://localhost:5173` 成功启动
- **And** 后端服务器在 `http://localhost:8000` 成功启动
- **And** 前端可以通过代理访问后端 API（配置在 `vite.config.ts`）

## Tasks / Subtasks

### 任务 1: 前端项目初始化 (AC: 前端初始化验收标准)

- [x] **1.1 创建 Vue.js 3 项目**
  - 执行 `npm create vue@latest rag-text-to-sql-frontend`
  - 选择启用：TypeScript ✅, Vue Router ✅, Pinia ✅, ESLint ✅, Prettier ✅
  - 验证项目结构符合 Vue 官方最佳实践

- [x] **1.2 安装 UI 组件库**
  - 选择并安装 Ant Design Vue（推荐）或 Element Plus
  - 配置全局引入（在 `main.ts` 中）
  - 验证组件库可正常使用

- [x] **1.3 配置开发代理**
  - 在 `vite.config.ts` 中配置 proxy
  - 代理 `/api/*` 请求到后端 `http://localhost:8000`
  - 验证代理配置正确

### 任务 2: 后端 DDD 架构创建 (AC: 后端初始化验收标准)

- [x] **2.1 创建 DDD 目录结构**
  - 在项目根目录创建 `backend/` 目录
  - 创建分层目录：
    ```bash
    cd backend
    mkdir -p interface/{api,dto}
    mkdir -p application
    mkdir -p domain/{ddl,agent,sql}
    mkdir -p infrastructure/{llm,vector,parser,repository,logging}
    mkdir -p tests/{unit,integration,fixtures}
    ```
  - 在每个目录创建 `__init__.py` 文件

- [x] **2.2 创建 requirements.txt**
  - 添加核心依赖：
    ```txt
    fastapi>=0.109.0
    uvicorn[standard]>=0.27.0
    langchain>=0.1.0
    langchain-core>=0.1.0
    chromadb>=0.4.24
    sqlparse>=0.4.4
    python-dotenv>=1.0.0
    pydantic-settings>=2.1.0
    ```
  - 验证版本兼容性

### 任务 3: 环境配置 (AC: 环境配置验收标准)

- [x] **3.1 创建 .env.example 模板**
  - 创建 `.env.example` 文件包含：
    ```
    # LLM API Configuration
    GLM_API_KEY=your_api_key_here
    
    # Logging Configuration
    LOG_LEVEL=INFO
    
    # Server Configuration
    HOST=0.0.0.0
    PORT=8000
    ```

- [x] **3.2 创建 config.py 配置管理**
  - 使用 Pydantic Settings 创建配置类
  - 从 `.env` 文件加载配置
  - 提供配置验证和默认值

- [x] **3.3 配置 .gitignore**
  - 添加 `.env` 到 `.gitignore`
  - 添加 Python 和 Node.js 标准忽略规则

### 任务 4: FastAPI 基础设置 (AC: 项目运行验收标准)

- [x] **4.1 创建 main.py 入口文件**
  - 初始化 FastAPI 应用
  - 配置 CORS（允许前端本地开发访问）
  - 创建基础健康检查端点 `/health`
  - 配置静态文件服务（为单进程部署做准备）

- [x] **4.2 验证项目运行**
  - 前端：运行 `npm run dev`，验证 `http://localhost:5173` 可访问
  - 后端：运行 `python main.py`，验证 `http://localhost:8000/health` 返回 200
  - 前端代理：验证前端可以通过代理访问后端 `/api/health`

## Dev Notes

### 核心架构决策

**前端 Starter 选择**：
- ✅ **使用 Vue.js 官方 Starter** (`create-vue`)
- **理由**：
  - 标准化的 Vue.js 3 + Vite 配置
  - 自动配置 TypeScript、ESLint、Prettier
  - 提供最佳实践的项目结构
  - Vue 团队官方维护，稳定可靠

**后端从零构建**：
- ✅ **基于 DDD 分层架构手动创建**
- **理由**：
  - LangChain + LangGraph + RAG 架构高度定制化
  - Agent 编排逻辑没有现成的 starter
  - DDD 分层架构需要按项目需求设计
  - 灵活适配单进程部署方式

### DDD 架构层次说明

根据 Architecture 文档，后端严格遵循 DDD 分层架构：

**Interface Layer（接口层）**：
- 职责：API 端点、DTO 转换、请求响应处理
- 目录：`interface/api/`, `interface/dto/`
- 技术：FastAPI Router, Pydantic Models

**Application Layer（应用服务层）**：
- 职责：用例编排、事务边界、跨领域协调
- 目录：`application/`
- 技术：Application Services

**Domain Layer（领域层）**：
- 职责：核心业务逻辑、领域模型、领域服务
- 目录：`domain/{ddl,agent,sql}/`
- 技术：充血模型（领域对象包含行为）

**Infrastructure Layer（基础设施层）**：
- 职责：外部依赖集成、数据持久化、技术工具
- 目录：`infrastructure/{llm,vector,parser,repository,logging}/`
- 技术：LLM 集成、向量库、SQL Parser

### 技术栈版本

**前端**：
- Vue.js: 3.x（使用 Composition API）
- Vite: 5.x
- TypeScript: 5.x
- Ant Design Vue: 4.x（推荐）
- Pinia: 2.x
- Vue Router: 4.x

**后端**：
- Python: 3.9+
- FastAPI: 0.109.0+
- LangChain: 0.1.0+
- LangGraph: （通过 LangChain 集成）
- Chroma: 0.4.24+
- sqlparse: 0.4.4+

### 单进程部署架构

**部署流程**（未来 Story 1.2）：
1. 前端：`npm run build` → 编译产物生成在 `frontend/dist`
2. 部署脚本：将 `frontend/dist` 复制到 `backend/static`
3. FastAPI 配置：挂载静态文件目录到根路径
4. 启动：Python 后端进程同时提供前后端服务

**当前 Story 准备**：
- 在 `main.py` 中预留静态文件服务配置（注释说明，暂不启用）
- 确保项目结构支持未来单进程部署

### 前端代理配置

在 `vite.config.ts` 中配置开发代理：

```typescript
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8000',
        changeOrigin: true,
        // rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  }
})
```

### Project Structure Notes

**前端结构**（Vue 官方 Starter 生成）：
```
frontend/
├── src/
│   ├── assets/          # 静态资源
│   ├── components/      # 组件
│   ├── router/          # 路由配置
│   ├── stores/          # Pinia 状态管理
│   ├── views/           # 页面视图
│   ├── App.vue          # 根组件
│   └── main.ts          # 入口文件
├── public/              # 公共资源
├── index.html           # HTML 模板
├── vite.config.ts       # Vite 配置
├── tsconfig.json        # TypeScript 配置
└── package.json         # 依赖管理
```

**后端结构**（DDD 架构手动创建）：
```
backend/
├── interface/           # API 层
│   ├── api/            # API 端点
│   └── dto/            # 数据传输对象
├── application/         # 应用服务层
├── domain/              # 领域层
│   ├── ddl/            # DDL 管理领域
│   ├── agent/          # Agent 编排领域
│   └── sql/            # SQL 生成领域
├── infrastructure/      # 基础设施层
│   ├── llm/            # LLM 集成
│   ├── vector/         # 向量库集成
│   ├── parser/         # SQL Parser
│   ├── repository/     # 数据仓储
│   └── logging/        # 日志工具
├── tests/               # 测试
│   ├── unit/           # 单元测试
│   ├── integration/    # 集成测试
│   └── fixtures/       # 测试固件
├── config.py            # 配置管理（Pydantic Settings）
├── main.py              # FastAPI 应用入口
└── requirements.txt     # Python 依赖
```

### 依赖安装顺序

**前端依赖安装**：
```bash
cd frontend
npm install
npm install ant-design-vue  # 或 element-plus
```

**后端依赖安装**：
```bash
cd backend
pip install -r requirements.txt
```

### 环境变量管理

**配置文件结构**：
- `.env.example` - 环境变量模板（提交到版本控制）
- `.env` - 实际环境变量（不提交，需手动创建）
- `config.py` - Pydantic Settings 配置类

**config.py 示例结构**：
```python
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    # LLM Configuration
    glm_api_key: str
    
    # Server Configuration
    host: str = "0.0.0.0"
    port: int = 8000
    
    # Logging Configuration
    log_level: str = "INFO"
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"

settings = Settings()
```

### CORS 配置

在 `main.py` 中配置 CORS：
```python
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173"],  # 前端开发服务器
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)
```

### 健康检查端点

创建基础健康检查端点（Story 1.3 将完善）：
```python
@app.get("/health")
async def health_check():
    return {
        "status": "healthy",
        "timestamp": datetime.now().isoformat(),
        "services": {
            "api": "running",
            "vector_store": "not_initialized",
            "llm_api": "not_configured"
        }
    }
```

### 验证清单

**前端验证**：
- [ ] `npm run dev` 启动成功
- [ ] TypeScript 编译无错误
- [ ] ESLint 无警告
- [ ] Ant Design Vue 组件可以正常导入
- [ ] Vue Router 配置正确
- [ ] Pinia Store 初始化正确

**后端验证**：
- [ ] 所有 DDD 目录创建成功
- [ ] `__init__.py` 文件存在于所有包中
- [ ] `requirements.txt` 依赖正确
- [ ] `config.py` 可以加载环境变量
- [ ] `python main.py` 启动成功
- [ ] `/health` 端点返回 200 OK

**集成验证**：
- [ ] 前端可以通过代理访问后端 `/api/health`
- [ ] 前后端均无报错

### 常见问题

**问题 1**: `npm create vue@latest` 提示权限错误
- **解决**: 使用 `sudo npm create vue@latest` 或配置 npm 全局路径

**问题 2**: Python 依赖安装冲突
- **解决**: 使用虚拟环境 `python -m venv venv` 然后 `source venv/bin/activate`

**问题 3**: 前端代理不生效
- **解决**: 检查 `vite.config.ts` 配置，确保后端已启动

**问题 4**: CORS 错误
- **解决**: 检查 `main.py` 中的 CORS 配置，确保 `allow_origins` 包含前端地址

### References

**Architecture 文档关键章节**：
- [Source: architecture.md # Starter Template Evaluation] - Starter 选择理由
- [Source: architecture.md # Selected Starter: Vue.js Official Starter] - Vue.js Starter 详情
- [Source: architecture.md # DDD Architecture] - DDD 分层架构说明
- [Source: architecture.md # First Implementation Priority] - 初始化优先级和步骤
- [Source: architecture.md # Deployment Architecture] - 单进程部署架构

**Epic 文档**：
- [Source: epics.md # Epic 1: 系统基础设施与可观测性] - Epic 上下文
- [Source: epics.md # Story 1.1] - Story 完整需求和验收标准

**关键技术文档**：
- Vue.js 官方文档: https://vuejs.org/
- Vite 官方文档: https://vitejs.dev/
- FastAPI 官方文档: https://fastapi.tiangolo.com/
- Pydantic Settings: https://docs.pydantic.dev/latest/concepts/pydantic_settings/

## Dev Agent Record

### Agent Model Used

Claude Sonnet 4.5 (Amelia - Developer Agent)

### Debug Log References

无调试问题需要记录。项目初始化顺利完成。

### Implementation Plan

**前端实现**：
1. 使用 Vue.js 官方 Starter 创建项目（TypeScript + Router + Pinia + ESLint + Prettier）
2. 安装并全局配置 Ant Design Vue UI 组件库
3. 配置 Vite 开发代理，将 `/api/*` 代理到后端 `http://localhost:8000`

**后端实现**：
1. 创建 DDD 分层架构目录（Interface、Application、Domain、Infrastructure 四层）
2. 在所有包目录创建 `__init__.py`
3. 配置 Pydantic Settings 环境变量管理
4. 创建 FastAPI 应用，配置 CORS 和健康检查端点

**测试策略**：
- 编写 23 个集成测试验证项目结构、配置、依赖
- 实际启动前后端服务器验证运行正确性
- 验证前端代理到后端的连通性

### Completion Notes List

**✅ 前端初始化完成**：
- Vue 3 + Vite + TypeScript 项目创建成功
- Ant Design Vue 全局配置完成（在 `main.ts` 中）
- Vite 代理配置完成（`vite.config.ts`）
- 开发服务器在 `http://localhost:5173` 正常运行

**✅ 后端初始化完成**：
- DDD 分层架构（4 层 + 测试目录）创建成功
- 所有包含 `__init__.py` 文件
- requirements.txt 包含 8 个核心依赖
- config.py 使用 Pydantic Settings 管理配置
- main.py 创建 FastAPI 应用，配置 CORS 和健康检查
- 后端服务器在 `http://localhost:8000` 正常运行

**✅ 环境配置完成**：
- `.env.example` 模板创建（GLM_API_KEY、LOG_LEVEL、HOST、PORT）
- `.gitignore` 配置完成（排除 .env、Python 和 Node.js 构建产物）

**✅ 集成验证完成**：
- 23/23 集成测试全部通过
- 前端通过代理成功访问后端 `/api/health`
- 前后端通信链路正常

### File List

**前端文件**：
- `frontend/src/main.ts` - 添加 Ant Design Vue 全局引入
- `frontend/vite.config.ts` - 配置开发代理
- `frontend/package.json` - 自动生成（包含依赖）

**后端文件**（新增）：
- `backend/interface/__init__.py`
- `backend/interface/api/__init__.py`
- `backend/interface/dto/__init__.py`
- `backend/application/__init__.py`
- `backend/domain/__init__.py`
- `backend/domain/ddl/__init__.py`
- `backend/domain/agent/__init__.py`
- `backend/domain/sql/__init__.py`
- `backend/infrastructure/__init__.py`
- `backend/infrastructure/llm/__init__.py`
- `backend/infrastructure/vector/__init__.py`
- `backend/infrastructure/parser/__init__.py`
- `backend/infrastructure/repository/__init__.py`
- `backend/infrastructure/logging/__init__.py`
- `backend/tests/__init__.py`
- `backend/tests/unit/__init__.py`
- `backend/tests/integration/__init__.py`
- `backend/tests/fixtures/__init__.py`
- `backend/main.py` - FastAPI 应用入口
- `backend/config.py` - Pydantic Settings 配置管理
- `backend/requirements.txt` - Python 依赖清单
- `backend/.env.example` - 环境变量模板
- `backend/.env` - 实际环境变量（不提交到版本控制）
- `backend/tests/integration/test_project_initialization.py` - 项目初始化集成测试

**根目录文件**：
- `.gitignore` - Git 忽略规则配置

## Change Log

### 2026-01-25 - Story 完成
- ✅ 前端项目初始化完成（Vue 3 + Vite + TypeScript + Ant Design Vue）
- ✅ 后端 DDD 架构创建完成（4 层架构 + 测试目录）
- ✅ 环境配置完成（Pydantic Settings + .env 管理）
- ✅ FastAPI 基础应用创建（CORS + 健康检查）
- ✅ 集成测试通过（23/23）
- ✅ 前后端服务器验证运行正常
- ✅ 前端代理到后端验证成功

---

**🎯 Story Status**: review

**📅 Created**: 2026-01-25
**📅 Completed**: 2026-01-25

**✅ Story Implementation Complete**
- All tasks and subtasks completed
- All acceptance criteria satisfied
- 23 integration tests passing
- Frontend and backend servers running successfully
- Ready for code review
