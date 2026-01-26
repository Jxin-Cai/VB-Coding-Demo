"""
DDL Parser 模块
使用 sqlparse 解析 DDL 文件，提取表结构信息
"""
import re
from typing import List, Dict, Any, Optional
import sqlparse
from sqlparse.sql import Statement, Identifier, IdentifierList, Parenthesis, Function
from sqlparse.tokens import Keyword, Name, Punctuation

from infrastructure.logging.logger import get_logger

logger = get_logger("ddl_parser")


class TableColumn:
    """表字段信息"""
    
    def __init__(self, name: str, data_type: str, constraints: Optional[List[str]] = None, 
                 comment: Optional[str] = None):
        self.name = name
        self.data_type = data_type
        self.constraints = constraints or []
        self.comment = comment
    
    def to_dict(self) -> Dict[str, Any]:
        """转换为字典"""
        return {
            "name": self.name,
            "data_type": self.data_type,
            "constraints": self.constraints,
            "comment": self.comment
        }


class TableInfo:
    """表结构信息"""
    
    def __init__(self, name: str, columns: List[TableColumn], 
                 primary_keys: Optional[List[str]] = None,
                 foreign_keys: Optional[List[Dict[str, str]]] = None,
                 indexes: Optional[List[str]] = None,
                 comment: Optional[str] = None):
        self.name = name
        self.columns = columns
        self.primary_keys = primary_keys or []
        self.foreign_keys = foreign_keys or []
        self.indexes = indexes or []
        self.comment = comment
    
    def to_dict(self) -> Dict[str, Any]:
        """转换为字典"""
        return {
            "name": self.name,
            "columns": [col.to_dict() for col in self.columns],
            "primary_keys": self.primary_keys,
            "foreign_keys": self.foreign_keys,
            "indexes": self.indexes,
            "comment": self.comment,
            "column_count": len(self.columns)
        }


class DDLParser:
    """DDL 解析器"""
    
    def parse(self, ddl_content: str) -> List[TableInfo]:
        """
        解析 DDL 内容，提取所有表结构信息
        
        Args:
            ddl_content: DDL 文件内容
            
        Returns:
            List[TableInfo]: 表结构信息列表
        """
        logger.info("Starting DDL parsing...")
        
        # 使用 sqlparse 解析 SQL
        statements = sqlparse.parse(ddl_content)
        tables = []
        
        for statement in statements:
            # 仅处理 CREATE TABLE 语句
            if self._is_create_table(statement):
                try:
                    table_info = self._parse_create_table(statement)
                    if table_info:
                        tables.append(table_info)
                        logger.debug(f"Parsed table: {table_info.name} with {len(table_info.columns)} columns")
                except Exception as e:
                    logger.error(f"Failed to parse CREATE TABLE statement: {e}", exc_info=True)
                    # 继续解析其他表
                    continue
        
        logger.info(f"DDL parsing completed: {len(tables)} tables extracted")
        return tables
    
    def _is_create_table(self, statement: Statement) -> bool:
        """判断是否是 CREATE TABLE 语句"""
        # 使用 normalized 并去除前导空格
        normalized = statement.normalized.upper().strip()
        return 'CREATE TABLE' in normalized
    
    def _parse_create_table(self, statement: Statement) -> Optional[TableInfo]:
        """
        解析 CREATE TABLE 语句
        
        Args:
            statement: sqlparse Statement 对象
            
        Returns:
            TableInfo: 表结构信息，解析失败返回 None
        """
        # 提取表名
        table_name = self._extract_table_name(statement)
        if not table_name:
            logger.warning("Failed to extract table name from CREATE TABLE")
            return None
        
        # 提取字段定义
        columns = self._extract_columns(statement)
        if not columns:
            logger.warning(f"No columns found for table: {table_name}")
            return None
        
        # 提取主键
        primary_keys = self._extract_primary_keys(statement, columns)
        
        # 提取外键（简化实现）
        foreign_keys = []
        
        # 提取索引（简化实现）
        indexes = []
        
        # 提取表注释
        comment = self._extract_table_comment(statement)
        
        return TableInfo(
            name=table_name,
            columns=columns,
            primary_keys=primary_keys,
            foreign_keys=foreign_keys,
            indexes=indexes,
            comment=comment
        )
    
    def _extract_table_name(self, statement: Statement) -> Optional[str]:
        """提取表名"""
        tokens = list(statement.flatten())
        
        # 查找 TABLE 关键字后的标识符
        found_table = False
        for token in tokens:
            if found_table and token.ttype in (Name, None) and token.value.strip():
                # 移除引号和反引号，保持原始大小写
                name = token.value.strip('`"\' ')
                # 转换为小写以保持一致性
                return name.lower()
            if token.ttype is Keyword and token.value.upper() == 'TABLE':
                found_table = True
        
        return None
    
    def _extract_columns(self, statement: Statement) -> List[TableColumn]:
        """提取字段列表"""
        columns = []
        
        # 查找括号中的字段定义
        for token in statement.tokens:
            if isinstance(token, Parenthesis):
                col_definitions = self._parse_column_definitions(token.value)
                columns.extend(col_definitions)
                break
        
        return columns
    
    def _parse_column_definitions(self, parenthesis_content: str) -> List[TableColumn]:
        """
        解析字段定义（括号内容）
        使用改进的逻辑处理各种 DDL 格式
        """
        columns = []
        
        # 移除外层括号
        content = parenthesis_content.strip()
        if content.startswith('('):
            content = content[1:]
        if content.endswith(')'):
            content = content[:-1]
        
        # 处理多行格式：先规范化为单行，然后按逗号分割
        # 保护括号内的逗号（如 VARCHAR(100)）
        lines = self._split_by_comma(content)
        
        for line in lines:
            line = line.strip()
            if not line:
                continue
            
            # 跳过表级约束定义
            line_upper = line.upper()
            if any(keyword in line_upper for keyword in 
                   ['PRIMARY KEY (', 'FOREIGN KEY (', 'CONSTRAINT ', 'INDEX ', 'KEY (']):
                continue
            
            # 解析字段定义：字段名 数据类型 [约束...]
            parts = line.split()
            if len(parts) >= 2:
                col_name = parts[0].strip('`"\' ').lower()  # 转换为小写
                
                # 提取数据类型（可能包含括号，如 VARCHAR(100)）
                col_type = parts[1]
                # 如果有括号，可能跨越多个 parts
                if '(' in col_type and ')' not in col_type:
                    for i in range(2, len(parts)):
                        col_type += ' ' + parts[i]
                        if ')' in parts[i]:
                            break
                
                # 提取约束
                constraints = []
                if 'NOT NULL' in line_upper:
                    constraints.append('NOT NULL')
                if 'UNIQUE' in line_upper:
                    constraints.append('UNIQUE')
                if 'PRIMARY KEY' in line_upper:
                    constraints.append('PRIMARY KEY')
                if 'AUTO_INCREMENT' in line_upper or 'AUTOINCREMENT' in line_upper:
                    constraints.append('AUTO_INCREMENT')
                
                # 提取注释
                comment = None
                comment_match = re.search(r"COMMENT\s+'([^']+)'", line, re.IGNORECASE)
                if comment_match:
                    comment = comment_match.group(1)
                
                columns.append(TableColumn(col_name, col_type, constraints, comment))
        
        return columns
    
    def _split_by_comma(self, content: str) -> List[str]:
        """
        按逗号分割，但保护括号内的逗号
        例如：DECIMAL(10,2) 中的逗号不应分割
        """
        result = []
        current = []
        paren_depth = 0
        
        for char in content:
            if char == '(':
                paren_depth += 1
                current.append(char)
            elif char == ')':
                paren_depth -= 1
                current.append(char)
            elif char == ',' and paren_depth == 0:
                # 遇到逗号且不在括号内，分割
                result.append(''.join(current))
                current = []
            else:
                current.append(char)
        
        # 添加最后一段
        if current:
            result.append(''.join(current))
        
        return result
    
    def _extract_primary_keys(self, statement: Statement, columns: List[TableColumn]) -> List[str]:
        """提取主键字段"""
        primary_keys = []
        
        # 方法 1：从字段约束中查找 PRIMARY KEY
        for col in columns:
            if 'PRIMARY KEY' in ' '.join(col.constraints).upper():
                primary_keys.append(col.name)
        
        # 方法 2：查找 PRIMARY KEY (...) 语句
        content = statement.value.upper()
        pk_match = re.search(r'PRIMARY\s+KEY\s*\(([^)]+)\)', content)
        if pk_match:
            pk_fields = pk_match.group(1)
            # 分割多个字段，转换为小写
            pk_list = [f.strip('` "\'').lower() for f in pk_fields.split(',')]
            primary_keys.extend(pk_list)
        
        # 去重并保持小写
        return list(set([pk.lower() for pk in primary_keys]))
    
    def _extract_table_comment(self, statement: Statement) -> Optional[str]:
        """提取表注释"""
        comment_match = re.search(r"COMMENT\s*=?\s*'([^']+)'", statement.value, re.IGNORECASE)
        if comment_match:
            return comment_match.group(1)
        return None


# 单例实例
ddl_parser = DDLParser()
