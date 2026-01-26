"""
多文件管理与上下文切换集成测试
"""
import sys
import io
import requests
from pathlib import Path

# 添加 backend 目录到 Python 路径
BACKEND_DIR = Path(__file__).parent.parent.parent
sys.path.insert(0, str(BACKEND_DIR))

BASE_URL = "http://localhost:8000"


class TestMultiFileManagement:
    """多文件管理测试"""
    
    def test_upload_multiple_files(self):
        """测试上传多个文件"""
        files_to_upload = [
            ('file1.sql', "CREATE TABLE users (id INT);"),
            ('file2.sql', "CREATE TABLE orders (id INT);"),
            ('file3.sql', "CREATE TABLE products (id INT);"),
        ]
        
        file_ids = []
        
        for filename, ddl in files_to_upload:
            files = {'file': (filename, io.BytesIO(ddl.encode('utf-8')), 'text/plain')}
            response = requests.post(f"{BASE_URL}/api/files/upload", files=files)
            
            assert response.status_code == 201, f"上传 {filename} 失败"
            
            data = response.json()
            file_ids.append(data['file_id'])
        
        assert len(file_ids) == 3, "应该上传 3 个文件"
        
        # 验证所有文件都可以访问
        for file_id in file_ids:
            response = requests.get(f"{BASE_URL}/api/files/{file_id}")
            assert response.status_code == 200, f"文件 {file_id} 不可访问"
    
    def test_delete_file(self):
        """测试删除文件"""
        # 上传文件
        ddl = "CREATE TABLE delete_test (id INT);"
        files = {'file': ('to_delete.sql', io.BytesIO(ddl.encode('utf-8')), 'text/plain')}
        upload_response = requests.post(f"{BASE_URL}/api/files/upload", files=files)
        
        assert upload_response.status_code == 201
        file_id = upload_response.json()['file_id']
        
        # 删除文件
        delete_response = requests.delete(f"{BASE_URL}/api/files/{file_id}")
        assert delete_response.status_code == 200, "删除失败"
        
        data = delete_response.json()
        assert data['success'] is True, "删除返回失败"
        assert data['file_id'] == file_id, "文件 ID 不匹配"
        
        # 验证文件已删除（再次访问应返回 404）
        get_response = requests.get(f"{BASE_URL}/api/files/{file_id}")
        assert get_response.status_code == 404, "文件应该已被删除"
    
    def test_delete_nonexistent_file(self):
        """测试删除不存在的文件"""
        response = requests.delete(f"{BASE_URL}/api/files/nonexistent-id")
        assert response.status_code == 404, "应该返回 404"


class TestFileContext:
    """文件上下文测试"""
    
    def test_multiple_files_independent_parsing(self):
        """测试多个文件独立解析"""
        import time
        
        # 上传两个不同的文件
        file1_ddl = "CREATE TABLE file1_table (id INT PRIMARY KEY);"
        file2_ddl = "CREATE TABLE file2_table (name VARCHAR(100));"
        
        files1 = {'file': ('file1.sql', io.BytesIO(file1_ddl.encode('utf-8')), 'text/plain')}
        files2 = {'file': ('file2.sql', io.BytesIO(file2_ddl.encode('utf-8')), 'text/plain')}
        
        response1 = requests.post(f"{BASE_URL}/api/files/upload", files=files1)
        response2 = requests.post(f"{BASE_URL}/api/files/upload", files=files2)
        
        file1_id = response1.json()['file_id']
        file2_id = response2.json()['file_id']
        
        # 等待解析完成
        time.sleep(2)
        
        # 获取两个文件的状态
        status1 = requests.get(f"{BASE_URL}/api/files/{file1_id}").json()
        status2 = requests.get(f"{BASE_URL}/api/files/{file2_id}").json()
        
        # 验证两个文件都解析成功
        assert status1['status'] == 'ready', f"文件1未解析成功: {status1.get('error_message')}"
        assert status2['status'] == 'ready', f"文件2未解析成功: {status2.get('error_message')}"
        
        # 验证解析结果独立（不同的表）
        assert status1['table_count'] == 1
        assert status2['table_count'] == 1
        
        tables1 = status1['tables']
        tables2 = status2['tables']
        
        assert tables1[0]['name'] == 'file1_table', "文件1表名不正确"
        assert tables2[0]['name'] == 'file2_table', "文件2表名不正确"


if __name__ == "__main__":
    import pytest
    pytest.main([__file__, "-v"])
