"""
文件上传控制器
处理 DDL 文件上传和验证
"""
from datetime import datetime
from uuid import uuid4
import asyncio
from fastapi import APIRouter, UploadFile, File, HTTPException, BackgroundTasks
from fastapi.responses import JSONResponse

from interface.dto.file_dto import FileUploadResponse
from application.ddl_service import get_ddl_service
from infrastructure.logging.logger import get_logger

logger = get_logger("file_controller")
router = APIRouter()

# 内存存储（临时）
# 注意：后续 Story 中会替换为持久化存储
file_store = {}

# DDL 服务
ddl_service = get_ddl_service()


async def process_ddl_background(file_id: str, content: str):
    """
    后台任务：处理 DDL 解析和向量化
    
    Args:
        file_id: 文件 ID
        content: DDL 内容
    """
    try:
        result = await ddl_service.process_ddl_file(file_id, content, file_store)
        logger.info(f"Background DDL processing completed: {result}")
    except Exception as e:
        logger.error(f"Background DDL processing failed: {e}", exc_info=True)


@router.post("/upload", response_model=FileUploadResponse, status_code=201)
async def upload_file(
    file: UploadFile = File(...),
    background_tasks: BackgroundTasks = None
):
    """
    上传 DDL 文件
    
    Args:
        file: 上传的文件对象
        background_tasks: FastAPI 后台任务
        
    Returns:
        FileUploadResponse: 文件上传响应（文件 ID、文件名、状态等）
        
    Raises:
        HTTPException: 验证失败时抛出 400 错误
    """
    logger.info(f"File upload started: {file.filename}")
    
    # 验证文件名（必须是 .sql 格式）
    if not file.filename or not file.filename.lower().endswith('.sql'):
        logger.warning(f"Invalid file format rejected: {file.filename}")
        raise HTTPException(
            status_code=400,
            detail="仅支持 .sql 格式的 DDL 文件"
        )
    
    # 读取文件内容
    try:
        content = await file.read()
    except Exception as e:
        logger.error(f"Failed to read file {file.filename}: {e}", exc_info=True)
        raise HTTPException(
            status_code=400,
            detail=f"文件读取失败: {str(e)}"
        )
    
    file_size = len(content)
    
    # 验证文件大小（< 10MB）
    max_size = 10 * 1024 * 1024  # 10MB in bytes
    if file_size > max_size:
        logger.warning(
            f"File too large rejected: {file.filename} "
            f"({file_size / 1024 / 1024:.2f} MB > 10 MB)"
        )
        raise HTTPException(
            status_code=400,
            detail=f"文件大小超过限制（最大 10MB），当前文件：{file_size / 1024 / 1024:.2f} MB"
        )
    
    # 解码内容
    try:
        content_str = content.decode('utf-8')
    except UnicodeDecodeError:
        logger.error(f"File encoding error: {file.filename}")
        raise HTTPException(
            status_code=400,
            detail="文件编码错误，请确保文件为 UTF-8 格式"
        )
    
    # 验证包含有效的 DDL 语句（至少包含 CREATE TABLE）
    if 'CREATE TABLE' not in content_str.upper():
        logger.warning(f"Invalid DDL content: {file.filename} - missing CREATE TABLE")
        raise HTTPException(
            status_code=400,
            detail="文件不包含有效的 DDL 语句（需要至少一个 CREATE TABLE 语句）"
        )
    
    # 生成唯一文件 ID
    file_id = str(uuid4())
    uploaded_at = datetime.now()
    
    # 保存到内存存储
    file_store[file_id] = {
        'filename': file.filename,
        'content': content_str,
        'status': 'pending',  # 待解析
        'uploaded_at': uploaded_at,
        'file_size': file_size,
        'error_message': None
    }
    
    logger.info(
        f"File upload successful: {file.filename} "
        f"(ID: {file_id}, Size: {file_size / 1024:.2f} KB)"
    )
    
    # 触发后台解析任务（Story 2.2）
    if background_tasks:
        background_tasks.add_task(process_ddl_background, file_id, content_str)
        logger.info(f"Background DDL processing task scheduled for: {file_id}")
    
    # 返回上传响应
    return FileUploadResponse(
        file_id=file_id,
        filename=file.filename,
        status='pending',
        uploaded_at=uploaded_at,
        file_size=file_size
    )


@router.get("/{file_id}")
async def get_file_metadata(file_id: str):
    """
    获取文件元数据（包含解析结果）
    
    Args:
        file_id: 文件 ID
        
    Returns:
        Dict: 文件元数据（包含表结构信息）
        
    Raises:
        HTTPException: 文件不存在时抛出 404 错误
    """
    if file_id not in file_store:
        raise HTTPException(status_code=404, detail="文件不存在")
    
    file_data = file_store[file_id]
    
    # 构建响应（包含所有可用信息）
    response = {
        "file_id": file_id,
        "filename": file_data['filename'],
        "status": file_data['status'],
        "uploaded_at": file_data['uploaded_at'].isoformat(),
        "file_size": file_data['file_size']
    }
    
    # 如果已解析，包含解析结果
    if file_data['status'] == 'ready':
        response.update({
            "table_count": file_data.get('table_count', 0),
            "column_count": file_data.get('column_count', 0),
            "embedding_count": file_data.get('embedding_count', 0),
            "tables": file_data.get('tables', [])
        })
    
    # 如果解析失败，包含错误信息
    if file_data['status'] == 'error':
        response["error_message"] = file_data.get('error_message', '未知错误')
    
    return response


@router.delete("/{file_id}")
async def delete_file(file_id: str):
    """
    删除文件
    
    Args:
        file_id: 文件 ID
        
    Returns:
        Dict: 删除结果
        
    Raises:
        HTTPException: 文件不存在时抛出 404 错误
    """
    if file_id not in file_store:
        raise HTTPException(status_code=404, detail="文件不存在")
    
    # 获取文件信息用于日志
    file_data = file_store[file_id]
    filename = file_data['filename']
    
    # 从存储中删除
    del file_store[file_id]
    
    logger.info(f"File deleted: {filename} (ID: {file_id})")
    
    # TODO: 在后续 Story 中同时清理向量库中的相关数据
    # vector_service.delete_file_vectors(file_id)
    
    return {
        "success": True,
        "message": f"文件 {filename} 已删除",
        "file_id": file_id
    }
