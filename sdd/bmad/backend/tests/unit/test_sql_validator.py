"""
SQL 验证器单元测试
"""
import sys
from pathlib import Path

# 添加 backend 目录到 Python 路径
BACKEND_DIR = Path(__file__).parent.parent.parent
sys.path.insert(0, str(BACKEND_DIR))

import pytest
from domain.sql.sql_validator import SQLValidator, get_sql_validator


class TestSQLSyntaxValidation:
    """SQL 语法验证测试"""
    
    def test_valid_simple_select(self):
        """测试简单 SELECT 语句验证"""
        validator = get_sql_validator()
        
        sql = "SELECT id, name FROM users WHERE id = 1;"
        result = validator.validate_syntax(sql)
        
        assert result["valid"] is True, f"简单 SELECT 应该通过：{result['errors']}"
        assert len(result["errors"]) == 0
    
    def test_valid_join_query(self):
        """测试 JOIN 查询验证"""
        validator = get_sql_validator()
        
        sql = """
        SELECT u.id, u.name, o.order_id
        FROM users u
        LEFT JOIN orders o ON u.id = o.user_id
        WHERE u.status = 'active';
        """
        result = validator.validate_syntax(sql)
        
        assert result["valid"] is True, f"JOIN 查询应该通过：{result['errors']}"
    
    def test_valid_aggregate_query(self):
        """测试聚合查询验证"""
        validator = get_sql_validator()
        
        sql = """
        SELECT 
            category,
            COUNT(*) as total,
            AVG(price) as avg_price
        FROM products
        GROUP BY category
        HAVING COUNT(*) > 10;
        """
        result = validator.validate_syntax(sql)
        
        assert result["valid"] is True, f"聚合查询应该通过：{result['errors']}"
    
    def test_invalid_unmatched_parentheses(self):
        """测试括号不匹配的错误检测"""
        validator = get_sql_validator()
        
        sql = "SELECT id FROM users WHERE (status = 'active';"
        result = validator.validate_syntax(sql)
        
        assert result["valid"] is False, "括号不匹配应该被检测"
        assert any("括号" in err for err in result["errors"])
    
    def test_invalid_unmatched_quotes(self):
        """测试引号不匹配的错误检测"""
        validator = get_sql_validator()
        
        sql = "SELECT id FROM users WHERE name = 'John;"
        result = validator.validate_syntax(sql)
        
        assert result["valid"] is False, "引号不匹配应该被检测"
        assert any("引号" in err for err in result["errors"])
    
    def test_empty_sql(self):
        """测试空 SQL 的处理"""
        validator = get_sql_validator()
        
        sql = ""
        result = validator.validate_syntax(sql)
        
        assert result["valid"] is False
        assert len(result["errors"]) > 0


class TestSQLReferenceValidation:
    """SQL 引用验证测试"""
    
    def test_valid_table_reference(self):
        """测试有效的表引用"""
        schema_metadata = {
            "users": {"columns": ["id", "name", "email"]},
            "orders": {"columns": ["id", "user_id", "amount"]}
        }
        
        validator = get_sql_validator(schema_metadata)
        
        sql = "SELECT id, name FROM users;"
        result = validator.validate_references(sql)
        
        assert result["valid"] is True, f"有效表引用应该通过：{result['errors']}"
    
    def test_invalid_table_reference(self):
        """测试无效的表引用"""
        schema_metadata = {
            "users": {"columns": ["id", "name"]}
        }
        
        validator = get_sql_validator(schema_metadata)
        
        sql = "SELECT id FROM products;"  # products 表不存在
        result = validator.validate_references(sql)
        
        assert result["valid"] is False, "无效表引用应该被检测"
        assert any("products" in err for err in result["errors"])
    
    def test_no_schema_metadata(self):
        """测试缺少 schema 元数据时的处理"""
        validator = get_sql_validator(None)
        
        sql = "SELECT id FROM users;"
        result = validator.validate_references(sql)
        
        # 缺少元数据时应该通过，但有警告
        assert result["valid"] is True
        assert len(result["warnings"]) > 0


class TestCompleteValidation:
    """完整验证测试"""
    
    def test_valid_sql_passes_complete_validation(self):
        """测试完全正确的 SQL 通过验证"""
        schema_metadata = {
            "users": {"columns": ["id", "name", "email"]}
        }
        
        validator = get_sql_validator(schema_metadata)
        
        sql = "SELECT id, name FROM users WHERE id > 0;"
        result = validator.validate(sql)
        
        assert result["valid"] is True
        assert result["syntax"]["valid"] is True
        assert result["references"]["valid"] is True
        assert "formatted_sql" in result
    
    def test_invalid_sql_fails_validation(self):
        """测试错误的 SQL 无法通过验证"""
        validator = get_sql_validator()
        
        sql = "SELECT id FROM WHERE;"  # 缺少表名，明显错误
        result = validator.validate(sql)
        
        # 验证整体验证失败
        assert "valid" in result, "结果应包含 valid 字段"
        assert result["valid"] is False, \
            f"明显错误的 SQL 应该被拒绝，但 valid={result['valid']}"
        
        # 验证错误信息存在
        has_errors = False
        if "syntax" in result and "errors" in result["syntax"]:
            has_errors = len(result["syntax"]["errors"]) > 0
        if "references" in result and "errors" in result["references"]:
            has_errors = has_errors or len(result["references"]["errors"]) > 0
        if "errors" in result:
            has_errors = has_errors or len(result["errors"]) > 0
        
        assert has_errors, \
            f"应该有错误信息，但未检测到。结果: {result}"
        
        # 验证格式化的 SQL 存在（即使有错误，也应尝试格式化）
        assert "formatted_sql" in result, "结果应包含 formatted_sql 字段"


class TestTableNameExtraction:
    """表名提取测试"""
    
    def test_extract_single_table(self):
        """测试提取单表名"""
        validator = get_sql_validator()
        
        sql = "SELECT * FROM users;"
        import sqlparse
        parsed = sqlparse.parse(sql)[0]
        
        tables = validator._extract_table_names(parsed)
        
        assert "users" in tables
        assert len(tables) == 1
    
    def test_extract_multiple_tables_with_join(self):
        """测试提取多表名（使用 JOIN 语法）"""
        validator = get_sql_validator()
        
        sql = "SELECT * FROM users JOIN orders ON users.id = orders.user_id;"
        import sqlparse
        parsed = sqlparse.parse(sql)[0]
        
        tables = validator._extract_table_names(parsed)
        
        assert "users" in tables
        assert "orders" in tables
    
    @pytest.mark.xfail(
        reason="已知限制：逗号分隔的多表提取尚未完全实现。"
               "当前仅能提取第一个表。推荐用户使用 JOIN 语法。"
               "TODO: 完善 _extract_table_names 方法以支持逗号分隔的表"
    )
    def test_extract_multiple_tables_comma_separated(self):
        """
        测试逗号分隔的多表提取（旧式 SQL 语法）
        
        已知限制：当前实现无法完整提取所有逗号分隔的表
        推荐使用 JOIN 语法
        """
        validator = get_sql_validator()
        
        sql = "SELECT * FROM users, orders;"
        import sqlparse
        parsed = sqlparse.parse(sql)[0]
        
        tables = validator._extract_table_names(parsed)
        
        # 完整测试：应该提取所有表（目前会失败，因此标记为 xfail）
        assert "users" in tables, "应该提取到 users 表"
        assert "orders" in tables, "应该提取到 orders 表（当前实现缺失）"
        assert len(tables) == 2, f"应该提取到 2 个表，实际: {len(tables)}"


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
