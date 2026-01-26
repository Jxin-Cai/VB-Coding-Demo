"""
对话相关 DTO（数据传输对象）
"""
from typing import Optional, List, Dict, Any
from pydantic import BaseModel, Field


class ChatRequest(BaseModel):
    """对话请求 DTO"""
    
    message: str = Field(..., description="用户消息")
    file_id: Optional[str] = Field(None, description="当前使用的文件 ID（可选）")


class ChatResponse(BaseModel):
    """对话响应 DTO"""
    
    type: str = Field(..., description="响应类型：text（普通对话）或 sql（SQL 生成）")
    content: str = Field(..., description="响应内容")
    sql: Optional[str] = Field(None, description="生成的 SQL（如果type=sql）")
    explanation: Optional[str] = Field(None, description="SQL 解释（如果type=sql）")
    references: Optional[List[Dict[str, Any]]] = Field(None, description="引用来源（如果type=sql）")
    intent: Optional[str] = Field(None, description="识别的意图类型")
