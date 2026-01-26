"""
DDL 应用服务
编排 DDL 解析、向量化和状态管理流程
"""
import time
from typing import Dict, Any
import asyncio

from infrastructure.parser.ddl_parser import ddl_parser, TableInfo
from infrastructure.vector.vector_service import get_vector_service
from infrastructure.logging.logger import get_logger

logger = get_logger("ddl_service")


class DDLService:
    """DDL 应用服务"""
    
    def __init__(self):
        """初始化服务"""
        self.vector_service = get_vector_service()
    
    async def process_ddl_file(self, file_id: str, content: str, file_store: Dict) -> Dict[str, Any]:
        """
        处理 DDL 文件：解析 + 向量化
        
        Args:
            file_id: 文件 ID
            content: DDL 文件内容
            file_store: 文件存储（引用）
            
        Returns:
            Dict[str, Any]: 处理结果
        """
        logger.info(f"Starting DDL processing for file: {file_id}")
        start_time = time.time()
        
        try:
            # 更新状态为"解析中"
            file_store[file_id]['status'] = 'parsing'
            logger.info(f"File status updated: {file_id} -> parsing")
            
            # Step 1: 解析 DDL
            logger.info("Step 1: Parsing DDL...")
            tables = ddl_parser.parse(content)
            
            if not tables:
                raise ValueError("未能解析出任何表结构，请检查 DDL 格式")
            
            table_count = len(tables)
            column_count = sum(len(table.columns) for table in tables)
            
            logger.info(f"DDL parsed: {table_count} tables, {column_count} columns")
            
            # Step 2: 向量化
            logger.info("Step 2: Vectorizing schema...")
            embedding_count = self.vector_service.vectorize_tables(tables, file_id)
            
            logger.info(f"Vectorization completed: {embedding_count} embeddings")
            
            # Step 3: 保存结构信息
            file_store[file_id]['tables'] = [table.to_dict() for table in tables]
            file_store[file_id]['table_count'] = table_count
            file_store[file_id]['column_count'] = column_count
            file_store[file_id]['embedding_count'] = embedding_count
            
            # Step 4: 更新状态为"就绪"
            file_store[file_id]['status'] = 'ready'
            file_store[file_id]['error_message'] = None
            
            elapsed = time.time() - start_time
            logger.info(
                f"DDL processing completed for {file_id}: "
                f"{table_count} tables, {column_count} columns in {elapsed:.2f}s"
            )
            
            return {
                "success": True,
                "file_id": file_id,
                "table_count": table_count,
                "column_count": column_count,
                "embedding_count": embedding_count,
                "elapsed_time": round(elapsed, 2)
            }
        
        except Exception as e:
            # 处理错误
            elapsed = time.time() - start_time
            error_message = str(e)
            
            logger.error(
                f"DDL processing failed for {file_id}: {error_message}",
                exc_info=True
            )
            
            # 更新状态为"错误"
            file_store[file_id]['status'] = 'error'
            file_store[file_id]['error_message'] = error_message
            
            return {
                "success": False,
                "file_id": file_id,
                "error": error_message,
                "elapsed_time": round(elapsed, 2)
            }


# 全局单例
_ddl_service_instance = None


def get_ddl_service() -> DDLService:
    """
    获取 DDL 服务单例
    
    Returns:
        DDLService: DDL 服务实例
    """
    global _ddl_service_instance
    if _ddl_service_instance is None:
        _ddl_service_instance = DDLService()
    return _ddl_service_instance
