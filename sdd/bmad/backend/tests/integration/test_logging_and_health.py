"""
日志系统和健康检查集成测试
"""
import logging
import sys
import requests
from pathlib import Path

# 添加 backend 目录到 Python 路径
BACKEND_DIR = Path(__file__).parent.parent.parent
sys.path.insert(0, str(BACKEND_DIR))

BASE_URL = "http://localhost:8000"


class TestLoggingConfiguration:
    """日志配置测试"""
    
    def test_logger_module_exists(self):
        """测试 logger.py 模块是否存在"""
        logger_file = BACKEND_DIR / "infrastructure" / "logging" / "logger.py"
        assert logger_file.exists(), "logger.py 不存在"
    
    def test_logger_imports_successfully(self):
        """测试 logger 模块可以成功导入"""
        try:
            from infrastructure.logging.logger import logger, get_logger
            assert logger is not None, "logger 为 None"
            assert get_logger is not None, "get_logger 为 None"
        except ImportError as e:
            assert False, f"导入 logger 失败: {e}"
    
    def test_sensitive_data_filter_exists(self):
        """测试敏感信息过滤器是否存在"""
        logger_file = BACKEND_DIR / "infrastructure" / "logging" / "logger.py"
        with open(logger_file) as f:
            content = f.read()
        
        assert "SensitiveDataFilter" in content, "未定义 SensitiveDataFilter"
        assert "api_key" in content.lower(), "未实现 API key 过滤"
    
    def test_logger_level_configurable(self):
        """测试日志级别可配置"""
        from infrastructure.logging.logger import logger
        
        # logger 应该已初始化
        assert logger is not None
        assert hasattr(logger, 'level')


class TestHealthCheckInterface:
    """健康检查接口测试"""
    
    def test_health_controller_exists(self):
        """测试健康检查控制器文件是否存在"""
        health_controller = BACKEND_DIR / "interface" / "api" / "health_controller.py"
        assert health_controller.exists(), "health_controller.py 不存在"
    
    def test_api_health_endpoint_accessible(self):
        """测试 /api/health 端点可访问"""
        response = requests.get(f"{BASE_URL}/api/health")
        assert response.status_code == 200, f"/api/health 返回 {response.status_code}"
    
    def test_health_response_format(self):
        """测试健康检查响应格式"""
        response = requests.get(f"{BASE_URL}/api/health")
        data = response.json()
        
        # 验证必需字段
        assert "status" in data, "响应缺少 status 字段"
        assert "timestamp" in data, "响应缺少 timestamp 字段"
        assert "services" in data, "响应缺少 services 字段"
        
        # 验证 status 值
        assert data["status"] in ["healthy", "degraded"], \
            f"status 值不正确: {data['status']}"
        
        # 验证 services 结构
        services = data["services"]
        assert "api" in services, "services 缺少 api 字段"
        assert "vector_store" in services, "services 缺少 vector_store 字段"
        assert "llm_api" in services, "services 缺少 llm_api 字段"
    
    def test_health_timestamp_format(self):
        """测试时间戳格式为 ISO 8601"""
        response = requests.get(f"{BASE_URL}/api/health")
        data = response.json()
        
        timestamp = data["timestamp"]
        # 验证格式包含 T 和 Z（ISO 8601）
        assert "T" in timestamp, "时间戳不是 ISO 8601 格式"
        assert timestamp.endswith("Z"), "时间戳未使用 UTC 时区"
    
    def test_api_service_always_running(self):
        """测试 API 服务状态始终为 running"""
        response = requests.get(f"{BASE_URL}/api/health")
        data = response.json()
        
        assert data["services"]["api"] == "running", \
            "API 服务状态不是 running"
    
    def test_health_check_response_time(self):
        """测试健康检查响应时间 < 500ms"""
        import time
        
        start = time.time()
        response = requests.get(f"{BASE_URL}/api/health")
        elapsed = (time.time() - start) * 1000  # 转换为毫秒
        
        assert response.status_code == 200
        assert elapsed < 500, f"响应时间 {elapsed:.2f}ms 超过 500ms"
    
    def test_degraded_status_when_llm_not_configured(self):
        """测试 LLM 未配置时返回 degraded 状态"""
        response = requests.get(f"{BASE_URL}/api/health")
        data = response.json()
        
        # 当前环境使用测试 API key，应该显示未正确配置
        llm_status = data["services"]["llm_api"]
        
        # 如果 LLM 未配置或使用测试 key，整体状态应为 degraded
        if llm_status.startswith("error") or llm_status == "not_configured":
            assert data["status"] == "degraded", \
                "LLM 未配置时整体状态应为 degraded"
    
    def test_root_health_endpoint_backward_compatible(self):
        """测试根路径 /health 端点向后兼容"""
        response = requests.get(f"{BASE_URL}/health")
        assert response.status_code == 200, "/health 端点不可访问"
        
        data = response.json()
        # 验证格式与 /api/health 一致
        assert "status" in data
        assert "services" in data


class TestRequestLogging:
    """请求日志测试"""
    
    def test_main_py_has_request_logging_middleware(self):
        """测试 main.py 是否有请求日志中间件"""
        main_py = BACKEND_DIR / "main.py"
        with open(main_py) as f:
            content = f.read()
        
        assert "@app.middleware" in content, "缺少中间件装饰器"
        assert "log_requests" in content, "缺少请求日志函数"
        assert "start_time" in content, "未记录请求开始时间"
        assert "process_time" in content, "未计算响应时间"


if __name__ == "__main__":
    import pytest
    pytest.main([__file__, "-v"])
