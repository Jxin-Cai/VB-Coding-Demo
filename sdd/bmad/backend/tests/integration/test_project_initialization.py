"""
项目初始化集成测试
验证前后端项目结构和配置的正确性
"""
import os
import json
from pathlib import Path
import pytest


# 项目根目录
PROJECT_ROOT = Path(__file__).parent.parent.parent.parent
FRONTEND_DIR = PROJECT_ROOT / "frontend"
BACKEND_DIR = PROJECT_ROOT / "backend"


class TestFrontendInitialization:
    """前端项目初始化测试"""
    
    def test_frontend_directory_exists(self):
        """测试前端目录是否存在"""
        assert FRONTEND_DIR.exists(), "前端目录不存在"
        assert FRONTEND_DIR.is_dir(), "frontend 不是目录"
    
    def test_package_json_exists(self):
        """测试 package.json 是否存在"""
        package_json = FRONTEND_DIR / "package.json"
        assert package_json.exists(), "package.json 不存在"
    
    def test_required_dependencies_installed(self):
        """测试必需的依赖是否在 package.json 中"""
        package_json = FRONTEND_DIR / "package.json"
        with open(package_json) as f:
            package_data = json.load(f)
        
        dependencies = package_data.get("dependencies", {})
        dev_dependencies = package_data.get("devDependencies", {})
        all_deps = {**dependencies, **dev_dependencies}
        
        required_deps = ["vue", "vue-router", "pinia", "ant-design-vue"]
        for dep in required_deps:
            assert dep in all_deps, f"缺少依赖: {dep}"
    
    def test_vite_config_exists(self):
        """测试 vite.config.ts 是否存在"""
        vite_config = FRONTEND_DIR / "vite.config.ts"
        assert vite_config.exists(), "vite.config.ts 不存在"
    
    def test_vite_proxy_configured(self):
        """测试 Vite 代理是否配置正确"""
        vite_config = FRONTEND_DIR / "vite.config.ts"
        with open(vite_config) as f:
            content = f.read()
        
        assert "server:" in content, "Vite server 配置缺失"
        assert "proxy:" in content, "代理配置缺失"
        assert "/api" in content, "/api 代理路径缺失"
        assert "http://localhost:8000" in content, "后端代理地址配置错误"
    
    def test_main_ts_ant_design_imported(self):
        """测试 main.ts 是否引入了 Ant Design Vue"""
        main_ts = FRONTEND_DIR / "src" / "main.ts"
        assert main_ts.exists(), "main.ts 不存在"
        
        with open(main_ts) as f:
            content = f.read()
        
        assert "ant-design-vue" in content, "未引入 Ant Design Vue"
        assert "app.use(Antd)" in content, "未注册 Ant Design Vue"


class TestBackendInitialization:
    """后端项目初始化测试"""
    
    def test_backend_directory_exists(self):
        """测试后端目录是否存在"""
        assert BACKEND_DIR.exists(), "后端目录不存在"
        assert BACKEND_DIR.is_dir(), "backend 不是目录"
    
    def test_ddd_directories_exist(self):
        """测试 DDD 分层架构目录是否存在"""
        required_dirs = [
            "interface/api",
            "interface/dto",
            "application",
            "domain/ddl",
            "domain/agent",
            "domain/sql",
            "infrastructure/llm",
            "infrastructure/vector",
            "infrastructure/parser",
            "infrastructure/repository",
            "infrastructure/logging",
            "tests/unit",
            "tests/integration",
            "tests/fixtures",
        ]
        
        for dir_path in required_dirs:
            full_path = BACKEND_DIR / dir_path
            assert full_path.exists(), f"目录不存在: {dir_path}"
            assert full_path.is_dir(), f"{dir_path} 不是目录"
    
    def test_init_files_exist(self):
        """测试所有包目录是否有 __init__.py"""
        package_dirs = [
            "interface",
            "interface/api",
            "interface/dto",
            "application",
            "domain",
            "domain/ddl",
            "domain/agent",
            "domain/sql",
            "infrastructure",
            "infrastructure/llm",
            "infrastructure/vector",
            "infrastructure/parser",
            "infrastructure/repository",
            "infrastructure/logging",
            "tests",
            "tests/unit",
            "tests/integration",
            "tests/fixtures",
        ]
        
        for dir_path in package_dirs:
            init_file = BACKEND_DIR / dir_path / "__init__.py"
            assert init_file.exists(), f"__init__.py 不存在: {dir_path}"
    
    def test_requirements_txt_exists(self):
        """测试 requirements.txt 是否存在"""
        requirements = BACKEND_DIR / "requirements.txt"
        assert requirements.exists(), "requirements.txt 不存在"
    
    def test_required_dependencies_listed(self):
        """测试必需的依赖是否在 requirements.txt 中"""
        requirements = BACKEND_DIR / "requirements.txt"
        with open(requirements) as f:
            content = f.read()
        
        required_deps = [
            "fastapi",
            "uvicorn",
            "langchain",
            "langchain-core",
            "chromadb",
            "sqlparse",
            "python-dotenv",
            "pydantic-settings"
        ]
        
        for dep in required_deps:
            assert dep in content, f"缺少依赖: {dep}"
    
    def test_env_example_exists(self):
        """测试 .env.example 是否存在"""
        env_example = BACKEND_DIR / ".env.example"
        assert env_example.exists(), ".env.example 不存在"
    
    def test_env_example_contains_required_vars(self):
        """测试 .env.example 是否包含必需的环境变量"""
        env_example = BACKEND_DIR / ".env.example"
        with open(env_example) as f:
            content = f.read()
        
        required_vars = ["GLM_API_KEY", "LOG_LEVEL", "HOST", "PORT"]
        for var in required_vars:
            assert var in content, f"缺少环境变量: {var}"
    
    def test_config_py_exists(self):
        """测试 config.py 是否存在"""
        config_py = BACKEND_DIR / "config.py"
        assert config_py.exists(), "config.py 不存在"
    
    def test_config_uses_pydantic_settings(self):
        """测试 config.py 是否使用 Pydantic Settings"""
        config_py = BACKEND_DIR / "config.py"
        with open(config_py) as f:
            content = f.read()
        
        assert "BaseSettings" in content, "未使用 Pydantic BaseSettings"
        assert "Settings" in content, "未定义 Settings 类"
        assert "glm_api_key" in content, "未定义 glm_api_key"
    
    def test_main_py_exists(self):
        """测试 main.py 是否存在"""
        main_py = BACKEND_DIR / "main.py"
        assert main_py.exists(), "main.py 不存在"
    
    def test_main_py_has_fastapi_app(self):
        """测试 main.py 是否创建了 FastAPI 应用"""
        main_py = BACKEND_DIR / "main.py"
        with open(main_py) as f:
            content = f.read()
        
        assert "FastAPI" in content, "未导入 FastAPI"
        assert "app = FastAPI" in content, "未创建 FastAPI 实例"
    
    def test_main_py_has_cors_configured(self):
        """测试 main.py 是否配置了 CORS"""
        main_py = BACKEND_DIR / "main.py"
        with open(main_py) as f:
            content = f.read()
        
        assert "CORSMiddleware" in content, "未导入 CORSMiddleware"
        assert "add_middleware" in content, "未添加 CORS 中间件"
        assert "http://localhost:5173" in content, "CORS 未配置前端地址"
    
    def test_main_py_has_health_endpoint(self):
        """测试 main.py 是否有健康检查端点"""
        main_py = BACKEND_DIR / "main.py"
        with open(main_py) as f:
            content = f.read()
        
        # Story 1.3 将健康检查重构为独立控制器
        # 检查是否导入了健康检查路由
        assert "health_router" in content or '@app.get("/health")' in content, \
            "缺少健康检查端点或路由"


class TestGitignore:
    """Git 忽略配置测试"""
    
    def test_gitignore_exists(self):
        """测试 .gitignore 是否存在"""
        gitignore = PROJECT_ROOT / ".gitignore"
        assert gitignore.exists(), ".gitignore 不存在"
    
    def test_gitignore_excludes_env(self):
        """测试 .gitignore 是否包含 .env"""
        gitignore = PROJECT_ROOT / ".gitignore"
        with open(gitignore) as f:
            content = f.read()
        
        assert ".env" in content, ".gitignore 未包含 .env"
    
    def test_gitignore_excludes_python_artifacts(self):
        """测试 .gitignore 是否包含 Python 构建产物"""
        gitignore = PROJECT_ROOT / ".gitignore"
        with open(gitignore) as f:
            content = f.read()
        
        # 检查 __pycache__
        assert "__pycache__" in content, ".gitignore 未包含 __pycache__"
        
        # 检查 Python 编译文件（*.pyc 或 *.py[cod] 都可以）
        assert ("*.pyc" in content or "*.py[cod]" in content), \
            ".gitignore 未包含 Python 编译文件模式"
        
        # 检查虚拟环境
        assert "venv/" in content, ".gitignore 未包含 venv/"
    
    def test_gitignore_excludes_node_modules(self):
        """测试 .gitignore 是否包含 node_modules"""
        gitignore = PROJECT_ROOT / ".gitignore"
        with open(gitignore) as f:
            content = f.read()
        
        assert "node_modules/" in content, ".gitignore 未包含 node_modules/"
