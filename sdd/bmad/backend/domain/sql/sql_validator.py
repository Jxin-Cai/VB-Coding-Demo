"""
SQL 验证器
验证 SQL 语法正确性和表/字段引用有效性
"""
from typing import Dict, Any, List, Optional
import sqlparse
from sqlparse.sql import IdentifierList, Identifier, Where, Token
from sqlparse.tokens import Keyword, DML

from infrastructure.logging.logger import get_logger

logger = get_logger("sql_validator")


class SQLValidator:
    """SQL 验证器"""
    
    def __init__(self, schema_metadata: Optional[Dict[str, Any]] = None):
        """
        初始化 SQL 验证器
        
        Args:
            schema_metadata: 数据库 schema 元数据（表和字段信息）
        """
        self.schema_metadata = schema_metadata or {}
        logger.debug(f"SQLValidator initialized with {len(self.schema_metadata)} tables")
    
    def validate_syntax(self, sql: str) -> Dict[str, Any]:
        """
        验证 SQL 语法
        
        Args:
            sql: SQL 语句
            
        Returns:
            Dict: 验证结果
                - valid: bool - 是否有效
                - errors: List[str] - 错误列表
                - warnings: List[str] - 警告列表
        """
        logger.info(f"Validating SQL syntax...")
        
        errors = []
        warnings = []
        
        try:
            # 使用 sqlparse 解析 SQL
            parsed = sqlparse.parse(sql)
            
            if not parsed:
                errors.append("SQL 为空或无法解析")
                return {"valid": False, "errors": errors, "warnings": warnings}
            
            # 基础语法检查
            for statement in parsed:
                # 检查是否是有效的 SQL 语句
                if not statement.get_type():
                    errors.append("无效的 SQL 语句类型")
                
                # 检查是否有明显的语法错误
                tokens = list(statement.flatten())
                
                # 检查是否有未闭合的括号
                paren_count = sum(1 if str(t) == '(' else -1 if str(t) == ')' else 0 for t in tokens)
                if paren_count != 0:
                    errors.append(f"括号不匹配（差值：{paren_count}）")
                
                # 检查是否有未闭合的引号
                quote_tokens = [str(t) for t in tokens if str(t) in ["'", '"', '`']]
                if len(quote_tokens) % 2 != 0:
                    errors.append("引号不匹配")
            
            # 格式化检查（美化后是否改变语义）
            formatted = sqlparse.format(sql, reindent=True, keyword_case='upper')
            if not formatted:
                warnings.append("SQL 格式化失败")
            
            is_valid = len(errors) == 0
            
            logger.info(f"Syntax validation result: {'PASS' if is_valid else 'FAIL'}")
            if errors:
                logger.warning(f"Syntax errors: {errors}")
            
            return {
                "valid": is_valid,
                "errors": errors,
                "warnings": warnings
            }
        
        except Exception as e:
            logger.error(f"Syntax validation error: {e}", exc_info=True)
            return {
                "valid": False,
                "errors": [f"验证过程出错：{str(e)}"],
                "warnings": []
            }
    
    def validate_references(self, sql: str) -> Dict[str, Any]:
        """
        验证 SQL 中的表和字段引用
        
        Args:
            sql: SQL 语句
            
        Returns:
            Dict: 验证结果
                - valid: bool - 是否有效
                - errors: List[str] - 错误列表
                - warnings: List[str] - 警告列表
        """
        logger.info("Validating SQL references...")
        
        if not self.schema_metadata:
            logger.warning("No schema metadata available, skipping reference validation")
            return {
                "valid": True,
                "errors": [],
                "warnings": ["缺少 schema 元数据，无法验证引用"]
            }
        
        errors = []
        warnings = []
        
        try:
            # 解析 SQL
            parsed = sqlparse.parse(sql)
            
            for statement in parsed:
                # 提取表名
                table_names = self._extract_table_names(statement)
                
                # 验证表名是否存在
                for table_name in table_names:
                    if table_name not in self.schema_metadata:
                        errors.append(f"表不存在：{table_name}")
                    else:
                        logger.debug(f"Table reference valid: {table_name}")
                
                # TODO: 提取字段名并验证（更复杂，需要解析 SELECT、WHERE 子句）
                # 当前版本仅验证表名
            
            is_valid = len(errors) == 0
            
            logger.info(f"Reference validation result: {'PASS' if is_valid else 'FAIL'}")
            if errors:
                logger.warning(f"Reference errors: {errors}")
            
            return {
                "valid": is_valid,
                "errors": errors,
                "warnings": warnings
            }
        
        except Exception as e:
            logger.error(f"Reference validation error: {e}", exc_info=True)
            return {
                "valid": False,
                "errors": [f"验证过程出错：{str(e)}"],
                "warnings": []
            }
    
    def validate_logic(self, sql: str) -> Dict[str, Any]:
        """
        验证 SQL 逻辑（JOIN 条件、类型匹配等）
        
        Args:
            sql: SQL 语句
            
        Returns:
            Dict: 验证结果
                - valid: bool - 是否有效
                - errors: List[str] - 错误列表
                - warnings: List[str] - 警告列表
        """
        logger.info("Validating SQL logic...")
        
        errors = []
        warnings = []
        
        try:
            parsed = sqlparse.parse(sql)
            
            for statement in parsed:
                sql_upper = str(statement).upper()
                
                # 检查 JOIN 但没有 ON 条件
                if 'JOIN' in sql_upper and 'ON' not in sql_upper:
                    warnings.append("JOIN 语句缺少 ON 条件，可能导致笛卡尔积")
                
                # 检查 SELECT * 的使用（不推荐）
                if 'SELECT *' in sql_upper or 'SELECT*' in sql_upper:
                    warnings.append("使用 SELECT * 可能影响性能，建议明确列出字段")
                
                # 检查缺少 WHERE 条件的 DELETE/UPDATE
                if ('DELETE' in sql_upper or 'UPDATE' in sql_upper) and 'WHERE' not in sql_upper:
                    errors.append("DELETE/UPDATE 语句缺少 WHERE 条件，可能影响所有数据")
            
            is_valid = len(errors) == 0
            
            logger.info(f"Logic validation result: {'PASS' if is_valid else 'FAIL'}")
            if warnings:
                logger.info(f"Logic warnings: {warnings}")
            
            return {
                "valid": is_valid,
                "errors": errors,
                "warnings": warnings
            }
        
        except Exception as e:
            logger.error(f"Logic validation error: {e}", exc_info=True)
            return {
                "valid": True,  # 验证失败不阻止，只警告
                "errors": [],
                "warnings": [f"逻辑验证过程出错：{str(e)}"]
            }
    
    def validate(self, sql: str) -> Dict[str, Any]:
        """
        完整验证 SQL（语法 + 引用 + 逻辑）
        
        Args:
            sql: SQL 语句
            
        Returns:
            Dict: 验证结果
                - valid: bool - 是否有效
                - syntax: Dict - 语法验证结果
                - references: Dict - 引用验证结果
                - logic: Dict - 逻辑验证结果
                - formatted_sql: str - 格式化后的 SQL
        """
        logger.info("Starting multi-layer SQL validation...")
        
        # 第一层：语法验证
        syntax_result = self.validate_syntax(sql)
        
        # 第二层：引用验证
        references_result = self.validate_references(sql)
        
        # 第三层：逻辑验证
        logic_result = self.validate_logic(sql)
        
        # 格式化 SQL
        formatted_sql = sqlparse.format(
            sql,
            reindent=True,
            keyword_case='upper',
            identifier_case='lower'
        )
        
        # 综合结果（只有语法和引用错误才判定为无效，逻辑问题只警告）
        is_valid = syntax_result["valid"] and references_result["valid"]
        
        result = {
            "valid": is_valid,
            "syntax": syntax_result,
            "references": references_result,
            "logic": logic_result,
            "formatted_sql": formatted_sql
        }
        
        logger.info(f"Multi-layer validation result: {'PASS' if is_valid else 'FAIL'}")
        
        return result
    
    def _extract_table_names(self, statement) -> List[str]:
        """
        从 SQL 语句中提取表名
        
        Args:
            statement: sqlparse 解析的语句对象
            
        Returns:
            List[str]: 表名列表
        """
        table_names = []
        
        # 将 SQL 转为字符串进行简单解析
        sql_str = str(statement).upper()
        
        # 简单方法：查找 FROM 和 JOIN 后的标识符
        import re
        
        # 匹配 FROM table_name 或 JOIN table_name
        patterns = [
            r'FROM\s+([a-zA-Z_][a-zA-Z0-9_]*)',  # FROM table
            r'JOIN\s+([a-zA-Z_][a-zA-Z0-9_]*)',  # JOIN table
        ]
        
        original_sql = str(statement)
        for pattern in patterns:
            matches = re.finditer(pattern, sql_str)
            for match in matches:
                table_name = original_sql[match.start(1):match.end(1)].strip().lower()
                if table_name and table_name not in table_names:
                    table_names.append(table_name)
        
        # 处理逗号分隔的多表（FROM table1, table2）
        # 匹配 FROM 之后到 WHERE/GROUP/ORDER/LIMIT/JOIN/分号之前的内容
        from_match = re.search(r'FROM\s+(.*?)(?:WHERE|GROUP|ORDER|LIMIT|JOIN|;|$)', sql_str, re.IGNORECASE | re.DOTALL)
        if from_match:
            from_clause_start = from_match.start(1)
            from_clause_end = from_match.end(1)
            from_clause = original_sql[from_clause_start:from_clause_end].strip()
            
            # 分割逗号，提取每个表名
            parts = [p.strip() for p in from_clause.split(',')]
            for part in parts:
                # 移除空白
                part = part.strip().lower()
                # 移除分号
                part = part.rstrip(';')
                # 提取表名（移除别名，如 "users u" -> "users"）
                name = part.split()[0] if ' ' in part else part
                name = name.strip()
                # 过滤无效名称
                if name and name not in table_names and len(name) > 0:
                    table_names.append(name)
        
        logger.debug(f"Extracted table names: {table_names}")
        return list(set(table_names))  # 去重


def get_sql_validator(schema_metadata: Optional[Dict[str, Any]] = None) -> SQLValidator:
    """
    获取 SQL 验证器实例
    
    Args:
        schema_metadata: 数据库 schema 元数据
        
    Returns:
        SQLValidator: 验证器实例
    """
    return SQLValidator(schema_metadata)
