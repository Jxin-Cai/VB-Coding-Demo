"""
SQL 生成器
基于用户查询和表结构上下文生成 SQL
"""
from typing import Dict, Any, Optional, List
import json

from infrastructure.llm.llm_service import get_llm_service
from domain.sql.sql_validator import get_sql_validator
from infrastructure.logging.logger import get_logger

logger = get_logger("sql_generator")


class SQLGenerator:
    """SQL 生成器"""
    
    def __init__(self):
        """初始化 SQL 生成器"""
        self.llm_service = get_llm_service()
        logger.debug("SQLGenerator initialized")
    
    def generate(
        self,
        user_query: str,
        table_context: List[Dict[str, Any]],
        schema_metadata: Optional[Dict[str, Any]] = None
    ) -> Dict[str, Any]:
        """
        生成 SQL
        
        Args:
            user_query: 用户查询
            table_context: 表结构上下文（从向量检索获取）
            schema_metadata: 完整 schema 元数据（用于验证）
            
        Returns:
            Dict: 生成结果
                - sql: str - 生成的 SQL
                - formatted_sql: str - 格式化的 SQL
                - valid: bool - 是否通过验证
                - validation: Dict - 验证详情
                - explanation: str - SQL 解释
        """
        logger.info(f"Generating SQL for query: {user_query}")
        logger.debug(f"Table context: {len(table_context)} items")
        
        try:
            # 构建 System Prompt
            system_prompt = self._build_system_prompt(table_context)
            
            # 构建用户消息
            user_message = self._build_user_message(user_query)
            
            # 调用 LLM 生成 SQL
            logger.debug("Calling LLM for SQL generation...")
            llm_response = self.llm_service.generate_response(
                user_message=user_message,
                system_prompt=system_prompt
            )
            
            # 提取 SQL（LLM 可能返回带解释的文本）
            sql, explanation = self._extract_sql_from_response(llm_response)
            
            if not sql:
                logger.error("Failed to extract SQL from LLM response")
                return {
                    "sql": "",
                    "formatted_sql": "",
                    "valid": False,
                    "validation": {"errors": ["无法从 LLM 响应中提取 SQL"]},
                    "explanation": llm_response
                }
            
            logger.info(f"SQL generated: {sql[:100]}...")
            
            # 验证 SQL
            validator = get_sql_validator(schema_metadata)
            validation_result = validator.validate(sql)
            
            # 提取引用信息（从 table_context 中）
            references = self._extract_references(sql, table_context)
            
            result = {
                "sql": sql,
                "formatted_sql": validation_result.get("formatted_sql", sql),
                "valid": validation_result["valid"],
                "validation": {
                    "syntax": validation_result["syntax"],
                    "references": validation_result["references"]
                },
                "explanation": explanation,
                "references": references  # 新增：引用信息
            }
            
            if validation_result["valid"]:
                logger.info("SQL validation PASSED")
            else:
                logger.warning(f"SQL validation FAILED: {validation_result}")
            
            return result
        
        except Exception as e:
            logger.error(f"SQL generation failed: {e}", exc_info=True)
            return {
                "sql": "",
                "formatted_sql": "",
                "valid": False,
                "validation": {"errors": [f"生成过程出错：{str(e)}"]},
                "explanation": ""
            }
    
    def _build_system_prompt(self, table_context: List[Dict[str, Any]]) -> str:
        """
        构建 System Prompt
        
        Args:
            table_context: 表结构上下文
            
        Returns:
            str: System Prompt
        """
        # 格式化表结构信息
        context_str = "## 数据库结构信息\n\n"
        
        for item in table_context:
            if item.get("type") == "表":
                context_str += f"### 表：{item.get('name')}\n"
                context_str += f"- 描述：{item.get('description', '无')}\n"
                context_str += f"- 字段数：{item.get('column_count', '未知')}\n"
                context_str += f"- 相关性：{item.get('relevance', 0)}\n\n"
            elif item.get("type") == "字段":
                context_str += f"### 字段：{item.get('table')}.{item.get('name')}\n"
                context_str += f"- 类型：{item.get('data_type', '未知')}\n"
                context_str += f"- 描述：{item.get('description', '无')}\n"
                context_str += f"- 相关性：{item.get('relevance', 0)}\n\n"
        
        # 构建完整 Prompt
        prompt = f"""你是一个专业的 SQL 生成助手。

任务：根据用户的自然语言查询和提供的数据库结构信息，生成准确的 SQL 语句。

{context_str}

## 生成规则

1. **严格使用上下文中的表和字段**：只使用上述数据库结构信息中提到的表和字段
2. **语法正确**：生成的 SQL 必须语法正确，可以直接执行
3. **符合需求**：SQL 必须完整满足用户的查询需求
4. **最佳实践**：
   - 使用合适的 JOIN 类型（INNER/LEFT/RIGHT）
   - 添加必要的 WHERE 条件
   - 使用适当的聚合函数（SUM、COUNT、AVG 等）
   - 对于复杂查询，使用子查询或 CTE
5. **输出格式**：
   - 返回纯 SQL 语句（不要包含 markdown 代码块标记）
   - 可以在 SQL 后添加简短解释（用 -- 注释）

## 输出示例

```
SELECT column1, column2
FROM table_name
WHERE condition = 'value';

-- 解释：查询满足条件的记录
```

现在请根据用户查询生成 SQL。
"""
        
        return prompt
    
    def _build_user_message(self, user_query: str) -> str:
        """
        构建用户消息
        
        Args:
            user_query: 用户查询
            
        Returns:
            str: 用户消息
        """
        return f"用户查询：{user_query}\n\n请生成对应的 SQL 语句。"
    
    def _extract_sql_from_response(self, response: str) -> tuple[str, str]:
        """
        从 LLM 响应中提取 SQL 和解释
        
        Args:
            response: LLM 响应
            
        Returns:
            tuple: (sql, explanation)
        """
        logger.debug("Extracting SQL from LLM response")
        
        # 尝试提取 SQL（处理多种格式）
        sql = ""
        explanation = ""
        
        # 方法 1：提取 SQL 代码块（```sql ... ```）
        if "```sql" in response:
            start = response.find("```sql") + 6
            end = response.find("```", start)
            if end != -1:
                sql = response[start:end].strip()
                # 提取解释（代码块后的内容）
                explanation = response[end + 3:].strip()
        
        # 方法 2：提取普通代码块（``` ... ```）
        elif "```" in response and not sql:
            start = response.find("```") + 3
            end = response.find("```", start)
            if end != -1:
                sql = response[start:end].strip()
                explanation = response[end + 3:].strip()
        
        # 方法 3：直接使用响应（如果是纯 SQL）
        elif not sql:
            # 尝试按行分割，提取 SQL 和注释
            lines = response.strip().split('\n')
            sql_lines = []
            explanation_lines = []
            
            for line in lines:
                stripped = line.strip()
                # 如果是 SQL 注释（-- 开头），算作解释
                if stripped.startswith('--'):
                    explanation_lines.append(stripped[2:].strip())
                # 如果包含 SQL 关键字，算作 SQL
                elif any(kw in stripped.upper() for kw in ['SELECT', 'INSERT', 'UPDATE', 'DELETE', 'FROM', 'WHERE']):
                    sql_lines.append(line)
                # 其他内容算作解释
                elif stripped and not sql_lines:
                    explanation_lines.append(stripped)
            
            sql = '\n'.join(sql_lines).strip()
            explanation = '\n'.join(explanation_lines).strip()
        
        logger.debug(f"Extracted SQL: {len(sql)} characters")
        logger.debug(f"Extracted explanation: {len(explanation)} characters")
        
        return sql, explanation
    
    def _extract_references(
        self,
        sql: str,
        table_context: List[Dict[str, Any]]
    ) -> List[Dict[str, Any]]:
        """
        从 SQL 中提取引用的表和字段信息
        
        Args:
            sql: 生成的 SQL
            table_context: 表结构上下文
            
        Returns:
            List[Dict]: 引用信息列表
        """
        logger.debug("Extracting references from SQL")
        
        references = []
        sql_upper = sql.upper()
        
        # 从 table_context 中提取引用的表
        for item in table_context:
            if item.get("type") == "表":
                table_name = item.get("name", "").upper()
                if table_name in sql_upper:
                    # 找到该表引用的字段
                    columns_referenced = []
                    
                    # 从 table_context 中查找该表的字段
                    for col_item in table_context:
                        if col_item.get("type") == "字段" and \
                           col_item.get("table", "").upper() == table_name:
                            col_name = col_item.get("name", "").upper()
                            if col_name in sql_upper:
                                columns_referenced.append({
                                    "name": col_item.get("name"),
                                    "data_type": col_item.get("data_type"),
                                    "comment": col_item.get("description", "")
                                })
                    
                    references.append({
                        "table": item.get("name"),
                        "comment": item.get("description", ""),
                        "column_count": item.get("column_count", 0),
                        "columns": columns_referenced,
                        "ddl_snippet": None  # TODO: 从 DDL 存储中获取
                    })
        
        logger.info(f"Extracted {len(references)} table references")
        return references


def get_sql_generator() -> SQLGenerator:
    """
    获取 SQL 生成器实例
    
    Returns:
        SQLGenerator: SQL 生成器实例
    """
    return SQLGenerator()
