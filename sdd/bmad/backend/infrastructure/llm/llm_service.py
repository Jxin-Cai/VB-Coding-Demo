"""
LLM 服务模块
封装大语言模型调用（支持 GLM/DeepSeek/OpenAI 等）
"""
from typing import Dict, Any, Optional, List
import json

from fastapi import HTTPException
from langchain_core.language_models.chat_models import BaseChatModel
from langchain_core.messages import HumanMessage, SystemMessage, AIMessage
from langchain_openai import ChatOpenAI

from config import settings
from infrastructure.logging.logger import get_logger

logger = get_logger("llm_service")


class LLMService:
    """LLM 服务"""
    
    def __init__(self):
        """初始化 LLM 服务（默认使用环境变量中的API Key）"""
        logger.info("Initializing LLM service...")
        
        # 默认 LLM（使用环境变量配置）
        # 注意：用户可以在运行时通过 generate_response 的 api_key 参数覆盖
        try:
            self.default_llm = self._create_llm(settings.glm_api_key)
            logger.info("Default LLM service initialized successfully")
        except Exception as e:
            logger.error(f"Failed to initialize default LLM: {e}", exc_info=True)
            self.default_llm = None
    
    def _create_llm(self, api_key: str) -> ChatOpenAI:
        """
        创建 LLM 实例
        
        Args:
            api_key: GLM API Key
            
        Returns:
            ChatOpenAI: LLM 实例
        """
        return ChatOpenAI(
            model="glm-4",
            api_key=api_key,
            base_url="https://open.bigmodel.cn/api/paas/v4",  # ✅ GLM API
            temperature=0,  # 完全确定性输出
            top_p=1.0,
        )
    
    def generate_response(
        self,
        user_message: str,
        system_prompt: Optional[str] = None,
        context: Optional[Dict[str, Any]] = None,
        api_key: Optional[str] = None
    ) -> str:
        """
        生成 LLM 响应
        
        Args:
            user_message: 用户消息
            system_prompt: 系统提示（可选）
            context: 上下文信息（可选）
            api_key: 用户提供的API Key（可选，优先级高于默认）
            
        Returns:
            str: LLM 生成的响应
        """
        # 选择 LLM 实例：用户提供的API Key优先
        if api_key:
            logger.info("Creating LLM instance with user-provided API Key")
            try:
                llm = self._create_llm(api_key)
            except Exception as e:
                logger.error(f"Failed to create LLM with user API Key: {e}")
                raise HTTPException(
                    status_code=401,
                    detail="API Key验证失败，请检查您的GLM API Key是否正确"
                )
        else:
            logger.info("Using default LLM instance")
            llm = self.default_llm
            
        if llm is None:
            logger.warning("LLM not initialized, returning mock response")
            return "LLM 服务未初始化。请在设置中配置 GLM API Key。"
        
        try:
            messages = []
            
            # 添加系统提示
            if system_prompt:
                messages.append(SystemMessage(content=system_prompt))
            
            # 添加上下文（如果有）
            if context:
                context_str = json.dumps(context, ensure_ascii=False, indent=2)
                messages.append(SystemMessage(content=f"上下文信息：\n{context_str}"))
            
            # 添加用户消息
            messages.append(HumanMessage(content=user_message))
            
            # 调用 LLM
            logger.debug(f"Calling LLM with {len(messages)} messages")
            response = llm.invoke(messages)
            
            result = response.content
            logger.info(f"LLM response generated: {len(result)} characters")
            
            return result
        
        except Exception as e:
            logger.error(f"LLM generation failed: {e}", exc_info=True)
            raise
    
    def is_available(self) -> bool:
        """
        检查 LLM 服务是否可用（检查默认LLM实例）
        
        Returns:
            bool: True 如果可用
        """
        return self.default_llm is not None
    
    def get_chat_model(self, api_key: Optional[str] = None) -> BaseChatModel:
        """
        获取 ChatModel 实例（供 Agent 使用）
        
        Args:
            api_key: 用户提供的API Key（可选）
            
        Returns:
            BaseChatModel: Chat Model 实例
        """
        if api_key:
            logger.info("Creating ChatModel with user-provided API Key")
            return self._create_llm(api_key)
        else:
            if self.default_llm is None:
                raise ValueError("LLM not initialized. Please configure GLM API Key.")
            logger.info("Using default ChatModel")
            return self.default_llm


# 全局单例
_llm_service_instance = None


def get_llm_service() -> LLMService:
    """
    获取 LLM 服务单例
    
    Returns:
        LLMService: LLM 服务实例
    """
    global _llm_service_instance
    if _llm_service_instance is None:
        _llm_service_instance = LLMService()
    return _llm_service_instance
