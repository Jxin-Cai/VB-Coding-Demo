"""
向量表名去重测试
验证重复表名会被正确去重
"""
import sys
from pathlib import Path

BACKEND_DIR = Path(__file__).parent.parent.parent
sys.path.insert(0, str(BACKEND_DIR))

import pytest
from infrastructure.vector.vector_service import VectorService
from infrastructure.parser.ddl_parser import TableInfo, TableColumn


class TestVectorTableDeduplication:
    """测试表名去重功能"""
    
    @pytest.fixture
    def vector_service(self):
        """创建向量服务实例"""
        return VectorService()
    
    @pytest.fixture
    def duplicate_tables(self):
        """创建重复表名的表列表"""
        # 创建3个同名表 "users"
        tables = []
        for i in range(3):
            table = TableInfo(
                name="users",  # 相同的表名
                columns=[
                    TableColumn(
                        name=f"id_{i}",
                        data_type="BIGINT",
                        constraints=["PRIMARY KEY"],
                        comment=f"ID {i}"
                    ),
                    TableColumn(
                        name=f"name_{i}",
                        data_type="VARCHAR(100)",
                        constraints=[],
                        comment=f"Name {i}"
                    )
                ],
                primary_keys=[f"id_{i}"],
                comment=f"Users table version {i}"
            )
            tables.append(table)
        return tables
    
    def test_duplicate_table_names_deduplication(self, vector_service, duplicate_tables):
        """测试重复表名去重"""
        file_id = "test-dedup-123"
        
        # 上传3个同名表
        count = vector_service.vectorize_tables(duplicate_tables, file_id)
        
        # 应该只保留第一个表：1个表 + 2个字段 = 3个向量
        assert count == 3, f"Expected 3 embeddings (1 table + 2 columns), got {count}"
        
        # 验证向量库中只有一个"users"表
        stats = vector_service.get_stats()
        assert stats["total_embeddings"] == 3
    
    def test_mixed_tables_with_duplicates(self, vector_service):
        """测试混合表（有重复+无重复）"""
        file_id = "test-mixed-456"
        
        # 创建混合表列表
        tables = [
            TableInfo(
                name="users",
                columns=[
                    TableColumn("id", "BIGINT", ["PRIMARY KEY"]),
                    TableColumn("name", "VARCHAR(100)", [])
                ],
                primary_keys=["id"]
            ),
            TableInfo(
                name="orders",
                columns=[
                    TableColumn("order_id", "BIGINT", ["PRIMARY KEY"]),
                    TableColumn("amount", "DECIMAL(10,2)", [])
                ],
                primary_keys=["order_id"]
            ),
            TableInfo(
                name="users",  # 重复
                columns=[
                    TableColumn("user_id", "BIGINT", ["PRIMARY KEY"]),
                    TableColumn("email", "VARCHAR(255)", [])
                ],
                primary_keys=["user_id"]
            ),
            TableInfo(
                name="products",
                columns=[
                    TableColumn("product_id", "BIGINT", ["PRIMARY KEY"]),
                    TableColumn("price", "DECIMAL(10,2)", [])
                ],
                primary_keys=["product_id"]
            ),
            TableInfo(
                name="orders",  # 重复
                columns=[
                    TableColumn("id", "BIGINT", ["PRIMARY KEY"])
                ],
                primary_keys=["id"]
            )
        ]
        
        # 上传
        count = vector_service.vectorize_tables(tables, file_id)
        
        # 应该保留：users(1个表+2个字段), orders(1个表+2个字段), products(1个表+2个字段)
        # 总计：3个表 + 6个字段 = 9个向量
        assert count == 9, f"Expected 9 embeddings, got {count}"
    
    def test_no_duplicates_unchanged(self, vector_service):
        """测试无重复时不影响"""
        file_id = "test-no-dup-789"
        
        tables = [
            TableInfo(
                name="users",
                columns=[TableColumn("id", "BIGINT", ["PRIMARY KEY"])],
                primary_keys=["id"]
            ),
            TableInfo(
                name="orders",
                columns=[TableColumn("id", "BIGINT", ["PRIMARY KEY"])],
                primary_keys=["id"]
            ),
            TableInfo(
                name="products",
                columns=[TableColumn("id", "BIGINT", ["PRIMARY KEY"])],
                primary_keys=["id"]
            )
        ]
        
        count = vector_service.vectorize_tables(tables, file_id)
        
        # 3个表 + 3个字段 = 6个向量
        assert count == 6


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
