"""
健康检查控制器
提供系统健康状态检查接口
"""
from datetime import datetime
from typing import Dict, Any
from fastapi import APIRouter

from config import settings
from infrastructure.logging.logger import get_logger

logger = get_logger("health")
router = APIRouter()


def check_vector_store() -> str:
    """
    检查向量库状态
    
    Returns:
        str: 向量库状态描述
    """
    # TODO: 在 Story 2.2 中实现实际检查
    # 当前暂时返回未初始化状态
    return "not_initialized"


def check_llm_api() -> str:
    """
    检查 LLM API 状态
    
    Returns:
        str: LLM API 状态描述
    """
    try:
        # 检查 API Key 是否配置
        if not settings.glm_api_key or settings.glm_api_key == "your_api_key_here" or \
           settings.glm_api_key == "test_api_key_placeholder":
            return "error: API key not configured"
        
        # TODO: 在后续 Story 中添加实际 API 连接测试
        # 当前仅检查配置存在
        logger.debug("LLM API key is configured")
        return "configured"
    except Exception as e:
        logger.error(f"Error checking LLM API: {e}")
        return f"error: {str(e)}"


@router.get("/health")
async def health_check() -> Dict[str, Any]:
    """
    健康检查端点
    返回系统整体健康状态和各服务状态
    
    Returns:
        Dict[str, Any]: 健康状态信息
            - status: "healthy" | "degraded"
            - timestamp: ISO 8601 格式时间戳
            - services: 各服务的状态
    """
    # 检查各服务状态
    services = {
        "api": "running",
        "vector_store": check_vector_store(),
        "llm_api": check_llm_api()
    }
    
    # 聚合整体状态
    # 如果有任何服务报错，整体状态为 degraded
    has_errors = any(
        status.startswith("error") or status == "not_initialized"
        for status in services.values()
    )
    overall_status = "degraded" if has_errors else "healthy"
    
    # 记录健康检查（仅在状态异常时记录）
    if overall_status == "degraded":
        logger.warning(f"Health check: {overall_status} - Services: {services}")
    
    return {
        "status": overall_status,
        "timestamp": datetime.utcnow().isoformat() + "Z",
        "services": services
    }
