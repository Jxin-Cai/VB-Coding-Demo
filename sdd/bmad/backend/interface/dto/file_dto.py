"""
文件相关 DTO（数据传输对象）
"""
from datetime import datetime
from typing import Optional
from pydantic import BaseModel, Field


class FileUploadResponse(BaseModel):
    """文件上传响应 DTO"""
    
    file_id: str = Field(..., description="文件唯一 ID")
    filename: str = Field(..., description="文件名")
    status: str = Field(..., description="文件状态：pending（待解析）、parsing（解析中）、ready（就绪）、error（错误）")
    uploaded_at: datetime = Field(..., description="上传时间")
    file_size: int = Field(..., description="文件大小（字节）")


class FileMetadata(BaseModel):
    """文件元数据 DTO"""
    
    file_id: str = Field(..., description="文件唯一 ID")
    filename: str = Field(..., description="文件名")
    status: str = Field(..., description="文件状态")
    uploaded_at: datetime = Field(..., description="上传时间")
    file_size: int = Field(..., description="文件大小（字节）")
    error_message: Optional[str] = Field(None, description="错误信息（如果存在）")
