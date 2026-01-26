"""
DDL 解析与向量化集成测试
"""
import sys
import io
import time
from datetime import datetime
import requests
from pathlib import Path

# 添加 backend 目录到 Python 路径
BACKEND_DIR = Path(__file__).parent.parent.parent
sys.path.insert(0, str(BACKEND_DIR))

BASE_URL = "http://localhost:8000"


class TestDDLParsing:
    """DDL 解析测试"""
    
    def test_parse_simple_create_table(self):
        """测试解析简单的 CREATE TABLE 语句"""
        from infrastructure.parser.ddl_parser import ddl_parser
        
        ddl = """
        CREATE TABLE users (
            id INT PRIMARY KEY,
            username VARCHAR(100) NOT NULL,
            email VARCHAR(255) UNIQUE,
            created_at TIMESTAMP
        );
        """
        
        tables = ddl_parser.parse(ddl)
        assert len(tables) == 1, "应该解析出 1 个表"
        
        table = tables[0]
        assert table.name == "users", f"表名不正确: {table.name}"
        assert len(table.columns) == 4, f"字段数量不正确: {len(table.columns)}"
        
        # 验证字段名
        col_names = [col.name for col in table.columns]
        assert "id" in col_names, "缺少 id 字段"
        assert "username" in col_names, "缺少 username 字段"
        assert "email" in col_names, "缺少 email 字段"
    
    def test_parse_multiple_tables(self):
        """测试解析多表 DDL"""
        from infrastructure.parser.ddl_parser import ddl_parser
        
        ddl = """
        CREATE TABLE users (id INT PRIMARY KEY);
        CREATE TABLE orders (id INT PRIMARY KEY, user_id INT);
        CREATE TABLE products (id INT PRIMARY KEY, name VARCHAR(100));
        """
        
        tables = ddl_parser.parse(ddl)
        assert len(tables) == 3, f"应该解析出 3 个表，实际: {len(tables)}"
        
        table_names = [t.name for t in tables]
        assert "users" in table_names, "缺少 users 表"
        assert "orders" in table_names, "缺少 orders 表"
        assert "products" in table_names, "缺少 products 表"
    
    def test_extract_primary_key(self):
        """测试提取主键"""
        from infrastructure.parser.ddl_parser import ddl_parser
        
        ddl = """
        CREATE TABLE users (
            id INT,
            username VARCHAR(100),
            PRIMARY KEY (id)
        );
        """
        
        tables = ddl_parser.parse(ddl)
        assert len(tables) == 1
        
        table = tables[0]
        assert len(table.primary_keys) > 0, "未提取到主键"
        assert "id" in table.primary_keys, f"主键不正确: {table.primary_keys}"


class TestVectorization:
    """向量化测试"""
    
    def test_vector_service_initialization(self):
        """测试向量服务初始化"""
        from infrastructure.vector.vector_service import get_vector_service
        
        service = get_vector_service()
        assert service is not None, "向量服务未初始化"
        assert service.collection is not None, "向量库集合未创建"
    
    def test_vectorize_single_table(self):
        """测试单表向量化"""
        from infrastructure.parser.ddl_parser import ddl_parser
        from infrastructure.vector.vector_service import get_vector_service
        
        ddl = """
        CREATE TABLE test_table (
            id INT PRIMARY KEY,
            name VARCHAR(100)
        );
        """
        
        tables = ddl_parser.parse(ddl)
        assert len(tables) == 1
        
        service = get_vector_service()
        embedding_count = service.vectorize_tables(tables, "test_file_id_vector")
        
        # 应该有 3 个向量：1 个表 + 2 个字段
        assert embedding_count == 3, f"向量数量不正确: {embedding_count}"
    
    def test_vector_query(self):
        """测试向量检索"""
        from infrastructure.vector.vector_service import get_vector_service
        
        service = get_vector_service()
        
        # 查询与"用户"相关的表结构
        results = service.query_schema("用户表", n_results=5)
        
        assert results is not None, "查询结果为空"
        assert 'documents' in results, "查询结果缺少 documents 字段"


class TestDDLServiceIntegration:
    """DDL 服务集成测试"""
    
    def test_process_ddl_file_success(self):
        """测试完整的 DDL 处理流程（解析 + 向量化）"""
        from application.ddl_service import get_ddl_service
        
        ddl = """
        CREATE TABLE users (
            id INT PRIMARY KEY,
            username VARCHAR(100) NOT NULL,
            email VARCHAR(255)
        );
        
        CREATE TABLE orders (
            id INT PRIMARY KEY,
            user_id INT,
            amount DECIMAL(10,2)
        );
        """
        
        # 模拟文件存储
        file_id = "test_integration_file"
        test_store = {
            file_id: {
                'filename': 'test.sql',
                'content': ddl,
                'status': 'pending',
                'uploaded_at': datetime.now(),
                'file_size': len(ddl)
            }
        }
        
        # 执行处理
        service = get_ddl_service()
        import asyncio
        result = asyncio.run(service.process_ddl_file(file_id, ddl, test_store))
        
        # 验证结果
        assert result['success'] is True, "处理失败"
        assert result['table_count'] == 2, f"表数量不正确: {result['table_count']}"
        assert result['column_count'] == 6, f"字段数量不正确: {result['column_count']}"
        assert result['embedding_count'] > 0, "未生成向量"
        assert result['elapsed_time'] < 10, f"处理时间过长: {result['elapsed_time']}s"
        
        # 验证状态更新
        assert test_store[file_id]['status'] == 'ready', \
            f"状态未更新: {test_store[file_id]['status']}"


class TestAsyncParsing:
    """异步解析测试"""
    
    def test_upload_triggers_background_parsing(self):
        """测试上传后触发后台解析"""
        ddl_content = """
        CREATE TABLE async_test (
            id INT PRIMARY KEY,
            name VARCHAR(100)
        );
        """
        
        # 上传文件
        files = {
            'file': ('async_test.sql', io.BytesIO(ddl_content.encode('utf-8')), 'text/plain')
        }
        response = requests.post(f"{BASE_URL}/api/files/upload", files=files)
        assert response.status_code == 201, f"上传失败: {response.status_code}"
        
        data = response.json()
        file_id = data['file_id']
        assert data['status'] == 'pending', "初始状态应为 pending"
        
        # 等待后台解析完成（最多 10 秒）
        max_wait = 10
        start = time.time()
        
        while time.time() - start < max_wait:
            time.sleep(0.5)
            
            # 查询文件状态
            status_response = requests.get(f"{BASE_URL}/api/files/{file_id}")
            if status_response.status_code == 200:
                status_data = status_response.json()
                current_status = status_data['status']
                
                if current_status in ['ready', 'error']:
                    # 解析完成
                    assert current_status == 'ready', \
                        f"解析失败: {status_data.get('error_message', 'unknown')}"
                    
                    elapsed = time.time() - start
                    assert elapsed < 5, f"解析时间过长: {elapsed:.2f}s > 5s"
                    
                    print(f"✅ Async parsing completed in {elapsed:.2f}s")
                    return
        
        assert False, "后台解析超时（> 10s）"


if __name__ == "__main__":
    import pytest
    pytest.main([__file__, "-v"])
