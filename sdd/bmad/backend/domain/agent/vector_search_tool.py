"""
向量检索工具
为 Agent 提供向量检索能力
"""
from typing import List, Dict, Any
import json

from langchain_core.tools import BaseTool
from pydantic import Field

from infrastructure.vector.vector_service import get_vector_service
from infrastructure.logging.logger import get_logger

logger = get_logger("vector_search_tool")


class VectorSearchTool(BaseTool):
    """
    向量检索工具
    用于检索与用户查询相关的数据库表结构信息
    """
    
    name: str = "vector_search"
    description: str = (
        "搜索数据库结构信息。"
        "输入：用户查询的自然语言描述（如'用户表'、'订单信息'）。"
        "输出：相关的表和字段结构信息。"
        "使用场景：需要了解数据库中有哪些表、字段时调用。"
    )
    
    n_results: int = Field(default=5, description="返回结果数量")
    
    def _run(self, query: str) -> str:
        """
        执行向量检索
        
        Args:
            query: 查询文本
            
        Returns:
            str: 检索结果（JSON 格式字符串）
        """
        logger.info(f"VectorSearchTool called with query: {query}")
        
        try:
            # 获取向量服务
            vector_service = get_vector_service()
            
            # 执行检索
            results = vector_service.query_schema(query, n_results=self.n_results)
            
            if not results or not results.get('documents'):
                logger.warning("No results found from vector search")
                return json.dumps({"message": "未找到相关的表结构信息"}, ensure_ascii=False)
            
            # 提取结果
            documents = results['documents'][0] if results['documents'] else []
            metadatas = results['metadatas'][0] if results['metadatas'] else []
            distances = results['distances'][0] if results['distances'] else []
            
            # 构建结构化结果
            formatted_results = []
            
            for i, (doc, metadata, distance) in enumerate(zip(documents, metadatas, distances)):
                item_type = metadata.get('type', 'unknown')
                
                if item_type == 'table':
                    formatted_results.append({
                        "type": "表",
                        "name": metadata.get('table_name'),
                        "description": doc,
                        "column_count": metadata.get('column_count'),
                        "relevance": round(1 - distance, 2)  # 距离转相关性
                    })
                elif item_type == 'column':
                    formatted_results.append({
                        "type": "字段",
                        "table": metadata.get('table_name'),
                        "name": metadata.get('column_name'),
                        "data_type": metadata.get('data_type'),
                        "description": doc,
                        "relevance": round(1 - distance, 2)
                    })
            
            logger.info(f"Vector search returned {len(formatted_results)} results")
            
            return json.dumps(formatted_results, ensure_ascii=False, indent=2)
        
        except Exception as e:
            logger.error(f"Vector search failed: {e}", exc_info=True)
            return json.dumps({"error": str(e)}, ensure_ascii=False)
    
    async def _arun(self, query: str) -> str:
        """异步执行（调用同步方法）"""
        return self._run(query)


def get_vector_search_tool() -> VectorSearchTool:
    """
    获取向量检索工具实例
    
    Returns:
        VectorSearchTool: 工具实例
    """
    return VectorSearchTool()
