"""
向量重复上传修复测试
验证重复上传同一文件不会导致 ID 冲突
"""
import sys
from pathlib import Path

BACKEND_DIR = Path(__file__).parent.parent.parent
sys.path.insert(0, str(BACKEND_DIR))

import pytest
from infrastructure.vector.vector_service import VectorService
from infrastructure.parser.ddl_parser import TableInfo, TableColumn


class TestVectorDuplicateUploadFix:
    """测试重复上传修复"""
    
    @pytest.fixture
    def vector_service(self):
        """创建向量服务实例"""
        return VectorService()
    
    @pytest.fixture
    def sample_table(self):
        """示例表结构"""
        return TableInfo(
            name="users",
            columns=[
                TableColumn(
                    name="id",
                    data_type="BIGINT",
                    constraints=["PRIMARY KEY"],
                    comment="用户ID"
                ),
                TableColumn(
                    name="name",
                    data_type="VARCHAR(100)",
                    constraints=["NOT NULL"],
                    comment="用户名"
                ),
                TableColumn(
                    name="created_at",
                    data_type="TIMESTAMP",
                    constraints=[],
                    comment="创建时间"
                )
            ],
            primary_keys=["id"],
            comment="用户表"
        )
    
    def test_duplicate_upload_same_file_id(self, vector_service, sample_table):
        """测试相同 file_id 重复上传"""
        file_id = "test-file-123"
        
        # 第一次上传
        count1 = vector_service.vectorize_tables([sample_table], file_id)
        assert count1 == 4  # 1 表 + 3 字段
        
        # 第二次上传（相同 file_id）- 应该不会报错
        count2 = vector_service.vectorize_tables([sample_table], file_id)
        assert count2 == 4
        
        # 验证向量库中只有一份数据
        stats = vector_service.get_stats()
        assert stats["total_embeddings"] == 4  # 应该还是 4，不是 8
    
    def test_duplicate_upload_multiple_times(self, vector_service, sample_table):
        """测试多次重复上传"""
        file_id = "test-file-456"
        
        # 连续上传 5 次
        for i in range(5):
            count = vector_service.vectorize_tables([sample_table], file_id)
            assert count == 4
        
        # 验证向量库中只有一份数据
        stats = vector_service.get_stats()
        # 注意：这里可能有之前测试的数据，所以不能直接断言 == 4
        # 但可以验证不是 20 (5 * 4)
        assert stats["total_embeddings"] < 20
    
    def test_different_file_ids_no_conflict(self, vector_service, sample_table):
        """测试不同 file_id 不会冲突"""
        file_id_1 = "file-1"
        file_id_2 = "file-2"
        
        # 上传两个不同的文件
        count1 = vector_service.vectorize_tables([sample_table], file_id_1)
        count2 = vector_service.vectorize_tables([sample_table], file_id_2)
        
        assert count1 == 4
        assert count2 == 4
        
        # 验证向量库中有两份数据
        stats = vector_service.get_stats()
        # 应该至少有 8 个向量（可能还有之前测试的）
        assert stats["total_embeddings"] >= 8
    
    def test_delete_file_vectors_works(self, vector_service, sample_table):
        """测试删除文件向量功能"""
        file_id = "test-delete-file"
        
        # 先上传
        vector_service.vectorize_tables([sample_table], file_id)
        
        # 手动删除
        vector_service._delete_file_vectors(file_id)
        
        # 验证已删除（通过查询）
        results = vector_service.collection.get(where={"file_id": file_id})
        assert len(results['ids']) == 0


if __name__ == "__main__":
    pytest.main([__file__, "-v"])
