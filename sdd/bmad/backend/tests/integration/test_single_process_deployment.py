"""
单进程部署集成测试
验证前端静态文件由后端提供，前后端在同一进程中运行
"""
import requests
from pathlib import Path


# 后端服务地址
BASE_URL = "http://localhost:8000"
BACKEND_DIR = Path(__file__).parent.parent.parent


class TestStaticFileServing:
    """静态文件服务测试"""
    
    def test_static_directory_exists(self):
        """测试 static 目录是否存在"""
        static_dir = BACKEND_DIR / "static"
        assert static_dir.exists(), "static 目录不存在，请先运行 ./deploy.sh"
        assert static_dir.is_dir(), "static 不是目录"
    
    def test_static_index_html_exists(self):
        """测试 index.html 是否存在"""
        index_html = BACKEND_DIR / "static" / "index.html"
        assert index_html.exists(), "index.html 不存在"
    
    def test_static_assets_exist(self):
        """测试 assets 目录和文件是否存在"""
        assets_dir = BACKEND_DIR / "static" / "assets"
        assert assets_dir.exists(), "assets 目录不存在"
        
        # 检查是否有 JS 和 CSS 文件
        js_files = list(assets_dir.glob("*.js"))
        css_files = list(assets_dir.glob("*.css"))
        
        assert len(js_files) > 0, "assets 目录中没有 JS 文件"
        assert len(css_files) > 0, "assets 目录中没有 CSS 文件"


class TestSingleProcessDeployment:
    """单进程部署功能测试"""
    
    def test_root_path_returns_frontend(self):
        """测试根路径返回前端页面"""
        response = requests.get(f"{BASE_URL}/")
        assert response.status_code == 200, f"根路径返回 {response.status_code}，期望 200"
        
        # 验证返回的是 HTML
        content_type = response.headers.get("content-type", "")
        assert "text/html" in content_type, f"Content-Type 不是 HTML: {content_type}"
        
        # 验证包含 Vue 应用的关键标识
        content = response.text
        assert "<div id=\"app\">" in content or "id=\"app\"" in content, \
            "HTML 中未找到 Vue 应用挂载点"
    
    def test_api_routes_work(self):
        """测试 API 路由正常工作"""
        # 测试 /api/health
        response = requests.get(f"{BASE_URL}/api/health")
        assert response.status_code == 200, f"/api/health 返回 {response.status_code}"
        
        # 验证返回 JSON
        data = response.json()
        assert "status" in data, "API 响应缺少 status 字段"
        
        # Story 1.3 升级健康检查，status 可能是 "healthy" 或 "degraded"
        assert data["status"] in ["healthy", "degraded"], \
            f"API 状态不正确: {data['status']}"
        
        # Story 1.3 健康检查返回 services 字段而不是 message
        assert "services" in data or "message" in data, \
            "API 响应缺少 services 或 message 字段"
    
    def test_health_endpoint_works(self):
        """测试 /health 端点正常工作"""
        response = requests.get(f"{BASE_URL}/health")
        assert response.status_code == 200, f"/health 返回 {response.status_code}"
        
        # 验证返回 JSON
        data = response.json()
        assert "status" in data, "健康检查响应缺少 status 字段"
        assert "services" in data, "健康检查响应缺少 services 字段"
    
    def test_api_priority_over_static(self):
        """测试 API 路由优先级高于静态文件"""
        # /api/health 应该返回 JSON，而不是静态文件
        response = requests.get(f"{BASE_URL}/api/health")
        assert response.status_code == 200
        
        # 验证是 JSON 而非 HTML
        content_type = response.headers.get("content-type", "")
        assert "application/json" in content_type, \
            f"API 路由返回的不是 JSON: {content_type}"
    
    def test_vue_router_history_mode_support(self):
        """测试 Vue Router History 模式支持（任意路径返回 index.html）"""
        # 访问一个不存在的前端路由（应该返回 index.html，由前端路由处理）
        response = requests.get(f"{BASE_URL}/about")
        assert response.status_code == 200, \
            f"Vue Router 路径 /about 返回 {response.status_code}，期望 200"
        
        # 验证返回的是 HTML（index.html）
        content_type = response.headers.get("content-type", "")
        assert "text/html" in content_type, \
            f"Vue Router 路径返回的不是 HTML: {content_type}"


class TestDeploymentScript:
    """部署脚本测试"""
    
    def test_deploy_script_exists(self):
        """测试部署脚本是否存在"""
        project_root = BACKEND_DIR.parent
        
        deploy_sh = project_root / "deploy.sh"
        deploy_bat = project_root / "deploy.bat"
        
        assert deploy_sh.exists(), "deploy.sh 不存在"
        assert deploy_bat.exists(), "deploy.bat 不存在"
    
    def test_deploy_sh_is_executable(self):
        """测试 deploy.sh 是否可执行"""
        project_root = BACKEND_DIR.parent
        deploy_sh = project_root / "deploy.sh"
        
        import os
        assert os.access(deploy_sh, os.X_OK), "deploy.sh 没有执行权限"


if __name__ == "__main__":
    import pytest
    pytest.main([__file__, "-v"])
