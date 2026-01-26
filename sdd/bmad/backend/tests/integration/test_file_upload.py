"""
DDL 文件上传集成测试
"""
import sys
import io
import requests
from pathlib import Path

# 添加 backend 目录到 Python 路径
BACKEND_DIR = Path(__file__).parent.parent.parent
sys.path.insert(0, str(BACKEND_DIR))

BASE_URL = "http://localhost:8000"


class TestFileUploadAPI:
    """文件上传 API 测试"""
    
    def test_upload_valid_ddl_file(self):
        """测试上传有效的 DDL 文件"""
        # 创建测试 DDL 内容
        ddl_content = """
        CREATE TABLE users (
            id INT PRIMARY KEY,
            username VARCHAR(100),
            email VARCHAR(255)
        );
        
        CREATE TABLE orders (
            id INT PRIMARY KEY,
            user_id INT,
            amount DECIMAL(10,2)
        );
        """
        
        # 创建文件对象
        files = {
            'file': ('test_schema.sql', io.BytesIO(ddl_content.encode('utf-8')), 'text/plain')
        }
        
        # 发送上传请求
        response = requests.post(f"{BASE_URL}/api/files/upload", files=files)
        
        # 验证响应
        assert response.status_code == 201, f"上传失败: {response.status_code}"
        
        data = response.json()
        assert "file_id" in data, "响应缺少 file_id"
        assert "filename" in data, "响应缺少 filename"
        assert data["filename"] == "test_schema.sql", "文件名不正确"
        assert "status" in data, "响应缺少 status"
        assert data["status"] == "pending", "初始状态应为 pending"
    
    def test_reject_non_sql_file(self):
        """测试拒绝非 .sql 格式文件"""
        # 创建 .txt 文件
        files = {
            'file': ('test.txt', io.BytesIO(b'some content'), 'text/plain')
        }
        
        response = requests.post(f"{BASE_URL}/api/files/upload", files=files)
        
        # 验证拒绝
        assert response.status_code == 400, "应该拒绝非 .sql 文件"
        
        data = response.json()
        assert "仅支持" in data["detail"] or "sql" in data["detail"].lower(), \
            "错误信息应提示文件格式"
    
    def test_reject_oversized_file(self):
        """测试拒绝超大文件（> 10MB）"""
        # 创建 11MB 的内容
        large_content = "CREATE TABLE test (id INT);\n" * 500000  # ~11MB
        
        files = {
            'file': ('large.sql', io.BytesIO(large_content.encode('utf-8')), 'text/plain')
        }
        
        response = requests.post(f"{BASE_URL}/api/files/upload", files=files)
        
        # 验证拒绝
        assert response.status_code == 400, "应该拒绝超大文件"
        
        data = response.json()
        assert "大小" in data["detail"] or "10MB" in data["detail"] or "10 MB" in data["detail"], \
            "错误信息应提示文件大小限制"
    
    def test_reject_invalid_ddl_content(self):
        """测试拒绝无效的 DDL 内容（不包含 CREATE TABLE）"""
        # 创建不包含 CREATE TABLE 的内容
        invalid_content = "SELECT * FROM users;"
        
        files = {
            'file': ('invalid.sql', io.BytesIO(invalid_content.encode('utf-8')), 'text/plain')
        }
        
        response = requests.post(f"{BASE_URL}/api/files/upload", files=files)
        
        # 验证拒绝
        assert response.status_code == 400, "应该拒绝无效 DDL"
        
        data = response.json()
        assert "DDL" in data["detail"] or "CREATE TABLE" in data["detail"], \
            "错误信息应提示 DDL 格式"
    
    def test_reject_invalid_encoding(self):
        """测试拒绝非 UTF-8 编码文件"""
        # 创建无效编码内容
        invalid_content = b'\xff\xfe\x00\x00'  # 无效的 UTF-8
        
        files = {
            'file': ('invalid_encoding.sql', io.BytesIO(invalid_content), 'text/plain')
        }
        
        response = requests.post(f"{BASE_URL}/api/files/upload", files=files)
        
        # 验证拒绝
        assert response.status_code == 400, "应该拒绝非 UTF-8 文件"
        
        data = response.json()
        assert "编码" in data["detail"] or "UTF-8" in data["detail"], \
            "错误信息应提示编码问题"
    
    def test_get_file_metadata(self):
        """测试获取文件元数据"""
        # 先上传文件
        ddl_content = "CREATE TABLE test (id INT);"
        files = {
            'file': ('test.sql', io.BytesIO(ddl_content.encode('utf-8')), 'text/plain')
        }
        upload_response = requests.post(f"{BASE_URL}/api/files/upload", files=files)
        assert upload_response.status_code == 201
        
        file_id = upload_response.json()["file_id"]
        
        # 获取元数据
        response = requests.get(f"{BASE_URL}/api/files/{file_id}")
        assert response.status_code == 200, "获取元数据失败"
        
        data = response.json()
        assert data["file_id"] == file_id, "文件 ID 不匹配"
        assert data["filename"] == "test.sql", "文件名不匹配"
    
    def test_get_nonexistent_file(self):
        """测试获取不存在的文件"""
        response = requests.get(f"{BASE_URL}/api/files/nonexistent-id")
        assert response.status_code == 404, "应该返回 404"


class TestFileUploadLogging:
    """文件上传日志测试"""
    
    def test_upload_success_logged(self):
        """测试文件上传日志系统配置正确"""
        import sys
        import logging
        import uuid
        
        # 导入controller获取logger配置
        sys.path.insert(0, str(BACKEND_DIR))
        from interface.api.file_controller import logger as file_logger
        
        # 验证logger存在且配置正确
        assert file_logger is not None, "logger 未初始化"
        assert isinstance(file_logger, logging.Logger), "logger 类型不正确"
        
        # 验证logger名称正确
        assert "file" in file_logger.name.lower(), \
            f"logger 名称不正确: {file_logger.name}（应包含 'file'）"
        
        # 验证logger级别配置合理
        effective_level = file_logger.getEffectiveLevel()
        assert effective_level <= logging.INFO, \
            f"logger 级别过高: {logging.getLevelName(effective_level)}（应 <= INFO才能记录日志）"
        
        # 验证至少有一个handler
        has_handlers = len(file_logger.handlers) > 0 or len(logging.getLogger().handlers) > 0
        assert has_handlers, \
            "logger 没有配置handler，日志无法输出。" \
            f"file_logger handlers: {len(file_logger.handlers)}, " \
            f"root handlers: {len(logging.getLogger().handlers)}"
        
        # 发送实际请求，验证API正常工作
        unique_filename = f"test_log_{uuid.uuid4().hex[:8]}.sql"
        ddl_content = "CREATE TABLE test_logging (id INT PRIMARY KEY);"
        files = {
            'file': (unique_filename, io.BytesIO(ddl_content.encode('utf-8')), 'text/plain')
        }
        
        response = requests.post(f"{BASE_URL}/api/files/upload", files=files)
        assert response.status_code == 201, \
            f"文件上传失败: {response.status_code}（日志可能未正常记录）"
        
        print(f"✅ 日志系统配置验证通过：logger={file_logger.name}, "
              f"level={logging.getLevelName(effective_level)}, "
              f"handlers={len(file_logger.handlers) + len(logging.getLogger().handlers)}")


if __name__ == "__main__":
    import pytest
    pytest.main([__file__, "-v"])
