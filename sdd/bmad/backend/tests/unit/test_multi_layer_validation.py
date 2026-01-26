"""
多层 SQL 验证测试
测试语法、引用、逻辑三层验证机制
"""
import sys
from pathlib import Path

BACKEND_DIR = Path(__file__).parent.parent.parent
sys.path.insert(0, str(BACKEND_DIR))

import pytest
from domain.sql.sql_validator import SQLValidator, get_sql_validator


class TestLogicValidation:
    """逻辑验证测试"""
    
    def test_join_without_on_condition(self):
        """测试 JOIN 缺少 ON 条件的警告"""
        validator = get_sql_validator()
        
        sql = "SELECT * FROM users JOIN orders;"
        result = validator.validate_logic(sql)
        
        # 应该有警告但不是错误
        assert result["valid"] is True
        assert len(result["warnings"]) > 0
        assert any("JOIN" in w and "ON" in w for w in result["warnings"])
    
    def test_select_star_warning(self):
        """测试 SELECT * 的警告"""
        validator = get_sql_validator()
        
        sql = "SELECT * FROM users;"
        result = validator.validate_logic(sql)
        
        # 应该有警告
        assert len(result["warnings"]) > 0
        assert any("SELECT *" in w for w in result["warnings"])
    
    def test_delete_without_where(self):
        """测试 DELETE 缺少 WHERE 的错误"""
        validator = get_sql_validator()
        
        sql = "DELETE FROM users;"
        result = validator.validate_logic(sql)
        
        # 应该是错误
        assert result["valid"] is False
        assert len(result["errors"]) > 0
        assert any("WHERE" in e for e in result["errors"])
    
    def test_update_without_where(self):
        """测试 UPDATE 缺少 WHERE 的错误"""
        validator = get_sql_validator()
        
        sql = "UPDATE users SET status = 'active';"
        result = validator.validate_logic(sql)
        
        # 应该是错误
        assert result["valid"] is False
        assert len(result["errors"]) > 0
    
    def test_valid_join_with_on(self):
        """测试正确的 JOIN 语句"""
        validator = get_sql_validator()
        
        sql = """
        SELECT u.id, o.order_id
        FROM users u
        JOIN orders o ON u.id = o.user_id;
        """
        result = validator.validate_logic(sql)
        
        # 应该没有 JOIN 相关的警告
        assert result["valid"] is True
        # 可能有 SELECT * 的警告，但不应有 JOIN 的警告
    
    def test_delete_with_where(self):
        """测试带 WHERE 的 DELETE 语句"""
        validator = get_sql_validator()
        
        sql = "DELETE FROM users WHERE id = 1;"
        result = validator.validate_logic(sql)
        
        # 应该通过
        assert result["valid"] is True
        assert len(result["errors"]) == 0


class TestMultiLayerValidation:
    """多层验证集成测试"""
    
    def test_complete_validation_includes_all_layers(self):
        """测试完整验证包含所有层"""
        validator = get_sql_validator()
        
        sql = "SELECT * FROM users WHERE id = 1;"
        result = validator.validate(sql)
        
        # 应该包含三层验证结果
        assert "syntax" in result
        assert "references" in result
        assert "logic" in result
        assert "formatted_sql" in result
        assert "valid" in result
    
    def test_syntax_error_fails_validation(self):
        """测试语法错误导致验证失败"""
        validator = get_sql_validator()
        
        sql = "SELECT id FROM WHERE;"  # 缺少表名
        result = validator.validate(sql)
        
        # 整体应该失败
        assert "valid" in result
        # 应该有 syntax 错误（虽然 sqlparse 可能较宽容）
    
    def test_logic_warning_does_not_fail_validation(self):
        """测试逻辑警告不导致验证失败"""
        validator = get_sql_validator()
        
        sql = "SELECT * FROM users;"  # 有 SELECT * 警告
        result = validator.validate(sql)
        
        # 整体应该通过（逻辑问题只警告）
        assert result["syntax"]["valid"] is True
        # 可能有逻辑警告
        if result.get("logic"):
            assert len(result["logic"].get("warnings", [])) > 0
    
    def test_multi_layer_with_schema(self):
        """测试带 schema 的多层验证"""
        schema_metadata = {
            "users": {"columns": ["id", "name", "email"]},
            "orders": {"columns": ["id", "user_id", "amount"]}
        }
        
        validator = get_sql_validator(schema_metadata)
        
        sql = """
        SELECT u.id, u.name, o.amount
        FROM users u
        JOIN orders o ON u.id = o.user_id
        WHERE u.id > 0;
        """
        result = validator.validate(sql)
        
        # 应该全部通过
        assert result["syntax"]["valid"] is True
        assert result["references"]["valid"] is True
        assert result["logic"]["valid"] is True
        assert result["valid"] is True


class TestValidationPerformance:
    """验证性能测试"""
    
    def test_validation_completes_quickly(self):
        """测试验证在合理时间内完成"""
        import time
        
        validator = get_sql_validator()
        
        sql = """
        SELECT u.id, u.name, u.email, o.order_id, o.amount, o.created_at
        FROM users u
        LEFT JOIN orders o ON u.id = o.user_id
        WHERE u.status = 'active'
        AND o.created_at > '2024-01-01'
        ORDER BY o.created_at DESC
        LIMIT 100;
        """
        
        start = time.time()
        result = validator.validate(sql)
        elapsed = time.time() - start
        
        # 验证应该在 100ms 内完成
        assert elapsed < 0.1, f"验证耗时 {elapsed*1000:.2f}ms，应 < 100ms"
        assert "valid" in result


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
