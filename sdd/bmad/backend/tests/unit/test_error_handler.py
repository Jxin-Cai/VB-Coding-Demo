"""
错误处理器测试
"""
import sys
from pathlib import Path

BACKEND_DIR = Path(__file__).parent.parent.parent
sys.path.insert(0, str(BACKEND_DIR))

import pytest
from domain.errors.error_handler import ErrorType, ErrorHandler, handle_error


class TestErrorTypeDetection:
    """错误类型检测测试"""
    
    def test_detect_ddl_parse_error(self):
        """测试识别 DDL 解析错误"""
        error = Exception("DDL parse failed at line 45")
        error_type = ErrorHandler._detect_error_type(error)
        assert error_type == ErrorType.DDL_PARSE_ERROR
    
    def test_detect_llm_api_error(self):
        """测试识别 LLM API 错误"""
        error = Exception("OpenAI API rate limit exceeded")
        error_type = ErrorHandler._detect_error_type(error)
        assert error_type == ErrorType.LLM_API_ERROR
    
    def test_detect_network_error(self):
        """测试识别网络错误"""
        error = Exception("Connection timeout")
        error_type = ErrorHandler._detect_error_type(error)
        assert error_type == ErrorType.NETWORK_ERROR
    
    def test_detect_validation_error(self):
        """测试识别验证错误"""
        error = Exception("SQL validation failed: invalid table reference")
        error_type = ErrorHandler._detect_error_type(error)
        assert error_type == ErrorType.VALIDATION_ERROR


class TestFriendlyMessageGeneration:
    """友好消息生成测试"""
    
    def test_ddl_parse_error_message(self):
        """测试 DDL 解析错误消息"""
        error = Exception("Syntax error")
        result = handle_error(error, ErrorType.DDL_PARSE_ERROR)
        
        assert "error_type" in result
        assert result["error_type"] == "ddl_parse_error"
        assert "message" in result
        assert "DDL" in result["message"]
        assert "suggestion" in result
        assert "检查" in result["suggestion"]
    
    def test_llm_api_error_message(self):
        """测试 LLM API 错误消息"""
        error = Exception("API call failed")
        result = handle_error(error, ErrorType.LLM_API_ERROR)
        
        assert result["error_type"] == "llm_api_error"
        assert "AI 服务" in result["message"] or "服务" in result["message"]
        assert "重试" in result["suggestion"]
    
    def test_network_error_message(self):
        """测试网络错误消息"""
        error = Exception("Connection refused")
        result = handle_error(error, ErrorType.NETWORK_ERROR)
        
        assert result["error_type"] == "network_error"
        assert "网络" in result["message"]
        assert "检查" in result["suggestion"]
    
    def test_validation_error_message(self):
        """测试验证错误消息"""
        error = Exception("Table not found")
        result = handle_error(error, ErrorType.VALIDATION_ERROR)
        
        assert result["error_type"] == "validation_error"
        assert "验证" in result["message"]
        assert "suggestion" in result
    
    def test_context_enhancement(self):
        """测试上下文增强错误消息"""
        error = Exception("Parse error")
        context = {"line_number": 45}
        result = handle_error(error, ErrorType.DDL_PARSE_ERROR, context)
        
        # 应该包含行号信息
        assert "45" in result["message"]
    
    def test_technical_detail_not_exposed(self):
        """测试技术细节不暴露给前端"""
        error = Exception("Detailed technical error with stack trace")
        result = handle_error(error)
        
        # technical_detail 字段存在但不应返回给前端
        assert "technical_detail" in result
        # 验证消息是友好的，不包含技术堆栈
        assert "stack trace" not in result["message"].lower()


class TestErrorHandlingWorkflow:
    """错误处理工作流测试"""
    
    def test_error_logged_but_user_sees_friendly_message(self):
        """测试错误被记录但用户看到友好消息"""
        error = Exception("Internal server error: database connection pool exhausted")
        result = handle_error(error)
        
        # 用户消息应该是友好的
        assert "Internal server error" not in result["message"]
        assert "database connection pool" not in result["message"]
        
        # 但技术细节应该在 technical_detail 中（供日志使用）
        assert "technical_detail" in result
        assert "Internal server error" in result["technical_detail"]


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
