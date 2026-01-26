"""
友好错误处理器
将技术错误转换为用户友好的提示
"""
from enum import Enum
from typing import Dict, Any, Optional
from infrastructure.logging.logger import get_logger

logger = get_logger("error_handler")


class ErrorType(Enum):
    """错误类型"""
    DDL_PARSE_ERROR = "ddl_parse_error"
    LLM_API_ERROR = "llm_api_error"
    NETWORK_ERROR = "network_error"
    VALIDATION_ERROR = "validation_error"
    RESOURCE_NOT_READY = "resource_not_ready"
    TIMEOUT_ERROR = "timeout_error"
    UNKNOWN_ERROR = "unknown_error"


class ErrorHandler:
    """错误处理器"""
    
    @staticmethod
    def handle_error(
        error: Exception,
        error_type: Optional[ErrorType] = None,
        context: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        """
        处理错误并生成友好提示
        
        Args:
            error: 原始异常
            error_type: 错误类型（如果已知）
            context: 错误上下文
            
        Returns:
            Dict: 友好的错误响应
                - error_type: str - 错误类型
                - message: str - 用户友好的错误消息
                - suggestion: str - 修正建议
                - technical_detail: str - 技术细节（仅记录日志）
        """
        # 记录完整的技术错误到日志
        logger.error(
            f"Error occurred: {error_type.value if error_type else 'unknown'}",
            exc_info=True,
            extra={"context": context}
        )
        
        # 自动识别错误类型（如果未指定）
        if error_type is None:
            error_type = ErrorHandler._detect_error_type(error)
        
        # 生成友好提示
        friendly_message = ErrorHandler._get_friendly_message(error_type, error, context)
        
        return {
            "error_type": error_type.value,
            "message": friendly_message["message"],
            "suggestion": friendly_message["suggestion"],
            "technical_detail": str(error)  # 仅用于日志，不返回给前端
        }
    
    @staticmethod
    def _detect_error_type(error: Exception) -> ErrorType:
        """
        自动检测错误类型
        
        Args:
            error: 异常对象
            
        Returns:
            ErrorType: 识别的错误类型
        """
        error_str = str(error).lower()
        error_class = error.__class__.__name__.lower()
        
        # DDL 解析错误
        if "parse" in error_str or "syntax" in error_str or "ddl" in error_str:
            return ErrorType.DDL_PARSE_ERROR
        
        # LLM API 错误
        if "api" in error_str or "openai" in error_str or "rate limit" in error_str:
            return ErrorType.LLM_API_ERROR
        
        # 网络错误
        if "network" in error_str or "connection" in error_str or "timeout" in error_str:
            return ErrorType.NETWORK_ERROR
        
        # 验证错误
        if "validation" in error_str or "invalid" in error_str:
            return ErrorType.VALIDATION_ERROR
        
        # 超时错误
        if "timeout" in error_class:
            return ErrorType.TIMEOUT_ERROR
        
        return ErrorType.UNKNOWN_ERROR
    
    @staticmethod
    def _get_friendly_message(
        error_type: ErrorType,
        error: Exception,
        context: Optional[Dict[str, Any]]
    ) -> Dict[str, str]:
        """
        生成友好的错误消息
        
        Args:
            error_type: 错误类型
            error: 原始异常
            context: 上下文
            
        Returns:
            Dict: 包含 message 和 suggestion
        """
        templates = {
            ErrorType.DDL_PARSE_ERROR: {
                "message": "DDL 文件解析失败，文件格式可能不正确",
                "suggestion": "请检查：\n1. 文件是否为标准 SQL DDL 格式\n2. 是否包含 CREATE TABLE 语句\n3. 语法是否符合 MySQL/PostgreSQL 标准"
            },
            ErrorType.LLM_API_ERROR: {
                "message": "AI 服务暂时不可用",
                "suggestion": "请稍后重试（1-2 分钟后）。如果问题持续，请联系管理员检查 API 配置。"
            },
            ErrorType.NETWORK_ERROR: {
                "message": "网络连接失败",
                "suggestion": "请检查：\n1. 网络连接是否正常\n2. 防火墙是否阻止了连接\n3. 稍后重试"
            },
            ErrorType.VALIDATION_ERROR: {
                "message": "生成的 SQL 验证未通过",
                "suggestion": "请尝试：\n1. 更清晰地描述需求\n2. 确认 DDL 文件包含所需的表和字段\n3. 简化查询需求"
            },
            ErrorType.RESOURCE_NOT_READY: {
                "message": "系统资源尚未就绪",
                "suggestion": "请等待：\n1. DDL 文件正在解析（约需 3-5 秒）\n2. 向量库正在初始化（约需 30 秒）\n3. 稍后重试"
            },
            ErrorType.TIMEOUT_ERROR: {
                "message": "操作超时，当前请求处理时间过长",
                "suggestion": "请尝试：\n1. 简化查询需求\n2. 稍后重试\n3. 检查系统负载"
            },
            ErrorType.UNKNOWN_ERROR: {
                "message": "系统遇到未知错误",
                "suggestion": "请稍后重试。如果问题持续，请联系技术支持。"
            }
        }
        
        template = templates.get(error_type, templates[ErrorType.UNKNOWN_ERROR])
        
        # 可以根据 context 动态调整消息
        message = template["message"]
        suggestion = template["suggestion"]
        
        # 特殊处理：DDL 解析错误，尝试提取行号
        if error_type == ErrorType.DDL_PARSE_ERROR and context:
            line_number = context.get("line_number")
            if line_number:
                message = f"DDL 文件解析失败：第 {line_number} 行存在语法错误"
        
        return {
            "message": message,
            "suggestion": suggestion
        }


def handle_error(
    error: Exception,
    error_type: Optional[ErrorType] = None,
    context: Optional[Dict[str, Any]] = None
) -> Dict[str, Any]:
    """
    全局错误处理函数
    
    Args:
        error: 异常对象
        error_type: 错误类型（可选）
        context: 错误上下文（可选）
        
    Returns:
        Dict: 友好的错误响应
    """
    return ErrorHandler.handle_error(error, error_type, context)
