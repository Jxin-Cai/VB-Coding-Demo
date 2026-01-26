"""
对话控制器
处理用户消息、意图识别、SQL 生成或普通对话
"""
from fastapi import APIRouter, HTTPException, Header
from typing import Optional

from interface.dto.chat_dto import ChatRequest, ChatResponse
from domain.agent.intent_recognizer import get_intent_recognizer
from application.agent_service import get_agent_service
from infrastructure.logging.logger import get_logger

logger = get_logger("chat_controller")
router = APIRouter()

# 意图识别器
intent_recognizer = get_intent_recognizer()

# Agent 服务
agent_service = get_agent_service()


@router.post("", response_model=ChatResponse)
async def chat(
    request: ChatRequest,
    x_api_key: Optional[str] = Header(None, alias="X-API-Key")
) -> ChatResponse:
    """
    处理用户对话请求
    
    Args:
        request: 对话请求（消息、文件 ID）
        
    Returns:
        ChatResponse: AI 响应（类型、内容、SQL 等）
    """
    logger.info(f"Chat request received: {request.message[:50]}...")
    logger.info(f"API Key received: {'Yes' if x_api_key else 'No (using default)'}")
    
    # Step 1: 意图识别（使用LLM智能判断）
    intent = intent_recognizer.recognize(request.message, api_key=x_api_key)
    logger.info(f"Intent: {intent}")
    
    # Step 2: 根据意图处理
    if intent == 'sql_generation':
        # SQL 生成类（Story 3.2-3.3）
        try:
            result = await agent_service.process_message(
                user_message=request.message,
                file_id=request.file_id,
                api_key=x_api_key  # 传递用户的API Key
            )
            
            return ChatResponse(
                type="sql",
                content=result.get('sql', ''),
                sql=result.get('sql'),
                explanation=result.get('explanation'),
                references=result.get('references'),
                intent=intent
            )
        except Exception as e:
            logger.error(f"SQL generation failed: {e}", exc_info=True)
            return ChatResponse(
                type="text",
                content=f"抱歉，SQL 生成失败：{str(e)}。请检查 API 配置或重试。",
                intent=intent
            )
    else:
        # 普通对话类（使用LLM进行自然对话）
        try:
            from infrastructure.llm.llm_service import get_llm_service
            llm_service = get_llm_service()
            
            # 检查 LLM 是否可用
            if not llm_service.is_available() and not x_api_key:
                logger.warning("LLM service not available for general chat")
                return ChatResponse(
                    type="text",
                    content=f"您好！我是 RAG Text-to-SQL 助手。😊\n\n您问：「{request.message}」\n\n抱歉，我现在无法回答普通问题。请在右上角设置中配置 GLM API Key，这样我就可以和您聊天了！\n\n💡 我的主要功能是帮您生成 SQL 查询，上传 DDL 文件后，您可以用自然语言描述查询需求，我会为您生成对应的 SQL。",
                    intent=intent
                )
            
            # 构建对话系统提示
            system_prompt = """你是一个友好的 RAG Text-to-SQL 智能助手。

你的主要功能是：
1. 帮助用户将自然语言转换为SQL查询（需要用户上传DDL文件）
2. 回答用户的日常问题和提供帮助

当用户进行普通对话时：
- 友好、自然地回应
- 如果用户询问你的功能，简要介绍你的SQL生成能力
- 保持简洁，避免过长的回复
- 如果用户问天气、时间等，友好地回答或说明你没有实时数据
"""
            
            # 调用LLM生成回复
            response = llm_service.generate_response(
                user_message=request.message,
                system_prompt=system_prompt,
                api_key=x_api_key
            )
            
            return ChatResponse(
                type="text",
                content=response,
                intent=intent
            )
        except Exception as e:
            logger.error(f"General chat failed: {e}", exc_info=True)
            return ChatResponse(
                type="text",
                content=f"您好！我是 RAG Text-to-SQL 助手。😊\n\n您问：「{request.message}」\n\n抱歉，我现在无法回答。请检查：\n1. 右上角设置中是否已配置 GLM API Key\n2. API Key 是否有效\n3. 网络连接是否正常\n\n💡 配置完成后，我就可以和您聊天，也可以帮您生成 SQL 查询了！",
                intent=intent
            )


@router.get("/history")
async def get_chat_history():
    """
    获取对话历史
    
    Returns:
        Dict: 对话历史记录
    """
    # TODO: 在后续 Story 中实现持久化对话历史
    return {
        "messages": [],
        "count": 0
    }
