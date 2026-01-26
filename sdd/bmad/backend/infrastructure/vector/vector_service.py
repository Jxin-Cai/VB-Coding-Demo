"""
向量化服务模块
负责表结构的向量化和向量库管理
"""
import time
from typing import List, Dict, Any
import chromadb
from chromadb.config import Settings as ChromaSettings

from infrastructure.parser.ddl_parser import TableInfo, TableColumn
from infrastructure.logging.logger import get_logger

logger = get_logger("vector_service")


class VectorService:
    """向量化服务"""
    
    def __init__(self):
        """初始化向量库（内存模式）"""
        logger.info("Initializing Chroma vector store (in-memory mode)...")
        
        # 创建内存向量库
        self.client = chromadb.Client(ChromaSettings(
            is_persistent=False,  # 内存模式
            anonymized_telemetry=False
        ))
        
        # 创建集合（用于存储表结构向量）
        # 使用默认的 embedding function（sentence-transformers）
        self.collection = self.client.get_or_create_collection(
            name="ddl_schema",
            metadata={"description": "Database schema embeddings"}
        )
        
        logger.info(f"Vector store initialized: collection=ddl_schema")
    
    def vectorize_tables(self, tables: List[TableInfo], file_id: str) -> int:
        """
        向量化表结构信息
        
        Args:
            tables: 表结构信息列表
            file_id: 文件 ID（用于关联）
            
        Returns:
            int: 向量化的条目数量
        """
        logger.info(f"Starting vectorization for {len(tables)} tables...")
        start_time = time.time()
        
        # 🔧 先删除该文件的旧向量（避免重复上传导致 ID 冲突）
        self._delete_file_vectors(file_id)
        
        # 🔍 检查并去重表名（保留第一个出现的表）
        seen_names = set()
        unique_tables = []
        for table in tables:
            if table.name not in seen_names:
                unique_tables.append(table)
                seen_names.add(table.name)
            else:
                logger.warning(f"⚠️ Skipping duplicate table: {table.name}")
        
        if len(unique_tables) < len(tables):
            logger.warning(f"Removed {len(tables) - len(unique_tables)} duplicate tables")
            tables = unique_tables  # 使用去重后的表列表
        
        documents = []  # 文本内容
        metadatas = []  # 元数据
        ids = []        # 唯一 ID
        
        for table in tables:
            # 为每个表生成向量
            table_doc = self._generate_table_document(table)
            table_id = f"{file_id}:table:{table.name}"
            
            documents.append(table_doc)
            metadatas.append({
                "type": "table",
                "file_id": file_id,
                "table_name": table.name,
                "column_count": len(table.columns)
            })
            ids.append(table_id)
            
            # 为每个字段生成向量
            for col in table.columns:
                col_doc = self._generate_column_document(table.name, col)
                col_id = f"{file_id}:column:{table.name}.{col.name}"
                
                documents.append(col_doc)
                metadatas.append({
                    "type": "column",
                    "file_id": file_id,
                    "table_name": table.name,
                    "column_name": col.name,
                    "data_type": col.data_type
                })
                ids.append(col_id)
        
        # 批量添加到向量库
        self.collection.add(
            documents=documents,
            metadatas=metadatas,
            ids=ids
        )
        
        elapsed = time.time() - start_time
        logger.info(
            f"Vectorization completed: {len(documents)} embeddings in {elapsed:.2f}s"
        )
        
        return len(documents)
    
    def _delete_file_vectors(self, file_id: str) -> None:
        """
        删除指定文件的所有向量（避免重复上传时 ID 冲突）
        
        Args:
            file_id: 文件 ID
        """
        try:
            # 查询该文件的所有向量 ID
            existing_results = self.collection.get(
                where={"file_id": file_id}
            )
            
            if existing_results and existing_results['ids']:
                existing_ids = existing_results['ids']
                logger.info(f"Deleting {len(existing_ids)} existing vectors for file_id={file_id}")
                
                # 批量删除
                self.collection.delete(ids=existing_ids)
                logger.info(f"Successfully deleted old vectors")
            else:
                logger.debug(f"No existing vectors found for file_id={file_id}")
                
        except Exception as e:
            logger.warning(f"Failed to delete old vectors: {e}")
            # 不阻塞主流程，继续添加新向量
    
    def _generate_table_document(self, table: TableInfo) -> str:
        """
        为表生成文本描述（用于向量化）
        
        Args:
            table: 表结构信息
            
        Returns:
            str: 表的文本描述
        """
        doc_parts = [
            f"表名: {table.name}",
            f"字段数量: {len(table.columns)}"
        ]
        
        # 添加字段列表
        col_names = [col.name for col in table.columns]
        doc_parts.append(f"字段: {', '.join(col_names)}")
        
        # 添加主键
        if table.primary_keys:
            doc_parts.append(f"主键: {', '.join(table.primary_keys)}")
        
        # 添加注释
        if table.comment:
            doc_parts.append(f"说明: {table.comment}")
        
        return " | ".join(doc_parts)
    
    def _generate_column_document(self, table_name: str, column: TableColumn) -> str:
        """
        为字段生成文本描述（用于向量化）
        
        Args:
            table_name: 表名
            column: 字段信息
            
        Returns:
            str: 字段的文本描述
        """
        doc_parts = [
            f"表: {table_name}",
            f"字段: {column.name}",
            f"类型: {column.data_type}"
        ]
        
        # 添加约束
        if column.constraints:
            doc_parts.append(f"约束: {', '.join(column.constraints)}")
        
        # 添加注释
        if column.comment:
            doc_parts.append(f"说明: {column.comment}")
        
        return " | ".join(doc_parts)
    
    def query_schema(self, query_text: str, n_results: int = 5) -> List[Dict[str, Any]]:
        """
        查询相关的表结构信息
        
        Args:
            query_text: 查询文本
            n_results: 返回结果数量
            
        Returns:
            List[Dict[str, Any]]: 相关的表结构信息
        """
        results = self.collection.query(
            query_texts=[query_text],
            n_results=n_results
        )
        
        return results
    
    def get_stats(self) -> Dict[str, Any]:
        """
        获取向量库统计信息
        
        Returns:
            Dict[str, Any]: 统计信息
        """
        count = self.collection.count()
        return {
            "total_embeddings": count,
            "collection_name": self.collection.name,
            "status": "initialized" if count > 0 else "empty"
        }


# 全局单例
_vector_service_instance = None


def get_vector_service() -> VectorService:
    """
    获取向量服务单例
    
    Returns:
        VectorService: 向量服务实例
    """
    global _vector_service_instance
    if _vector_service_instance is None:
        _vector_service_instance = VectorService()
    return _vector_service_instance
