"""
文件状态管理与显示集成测试
"""
import sys
import io
import time
import requests
from pathlib import Path

# 添加 backend 目录到 Python 路径
BACKEND_DIR = Path(__file__).parent.parent.parent
sys.path.insert(0, str(BACKEND_DIR))

BASE_URL = "http://localhost:8000"


class TestFileStatusAPI:
    """文件状态 API 测试"""
    
    def test_get_file_status_pending(self):
        """测试获取待解析文件状态"""
        # 上传文件但不等待解析
        ddl = "CREATE TABLE test (id INT);"
        files = {'file': ('test.sql', io.BytesIO(ddl.encode('utf-8')), 'text/plain')}
        
        upload_response = requests.post(f"{BASE_URL}/api/files/upload", files=files)
        file_id = upload_response.json()['file_id']
        
        # 立即获取状态
        response = requests.get(f"{BASE_URL}/api/files/{file_id}")
        assert response.status_code == 200
        
        data = response.json()
        assert data['file_id'] == file_id
        assert data['filename'] == 'test.sql'
        assert 'status' in data
        assert 'uploaded_at' in data
        assert 'file_size' in data
    
    def test_get_file_status_after_parsing(self):
        """测试获取解析完成后的文件状态（包含表信息）"""
        # 上传并等待解析
        ddl = """
        CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(100));
        CREATE TABLE orders (id INT PRIMARY KEY, user_id INT);
        """
        files = {'file': ('test.sql', io.BytesIO(ddl.encode('utf-8')), 'text/plain')}
        
        upload_response = requests.post(f"{BASE_URL}/api/files/upload", files=files)
        file_id = upload_response.json()['file_id']
        
        # 等待解析完成（最多 10 秒）
        for _ in range(20):
            time.sleep(0.5)
            response = requests.get(f"{BASE_URL}/api/files/{file_id}")
            data = response.json()
            
            if data['status'] in ['ready', 'error']:
                break
        
        # 验证解析结果
        assert data['status'] == 'ready', f"解析失败: {data.get('error_message')}"
        assert 'table_count' in data, "缺少 table_count 字段"
        assert 'column_count' in data, "缺少 column_count 字段"
        assert 'embedding_count' in data, "缺少 embedding_count 字段"
        assert 'tables' in data, "缺少 tables 字段"
        
        assert data['table_count'] == 2, f"表数量不正确: {data['table_count']}"
        assert data['column_count'] == 4, f"字段数量不正确: {data['column_count']}"
        assert len(data['tables']) == 2, f"tables 数组长度不正确: {len(data['tables'])}"


class TestStatusTransitions:
    """状态转换测试"""
    
    def test_status_transitions_complete_flow(self):
        """测试完整的状态转换流程：pending → parsing → ready"""
        ddl = "CREATE TABLE status_test (id INT PRIMARY KEY);"
        files = {'file': ('status_test.sql', io.BytesIO(ddl.encode('utf-8')), 'text/plain')}
        
        # 上传文件
        upload_response = requests.post(f"{BASE_URL}/api/files/upload", files=files)
        file_id = upload_response.json()['file_id']
        
        # 记录状态转换
        statuses = []
        max_attempts = 20
        
        for _ in range(max_attempts):
            response = requests.get(f"{BASE_URL}/api/files/{file_id}")
            data = response.json()
            current_status = data['status']
            
            if not statuses or statuses[-1] != current_status:
                statuses.append(current_status)
            
            if current_status in ['ready', 'error']:
                break
            
            time.sleep(0.5)
        
        # 验证状态转换序列
        print(f"Status transitions: {' → '.join(statuses)}")
        
        # 应该到达最终状态（ready 或 error）
        assert len(statuses) > 0, "未记录任何状态"
        assert statuses[-1] in ['ready', 'error'], f"最终状态不正确: {statuses[-1]}"
        
        # 如果解析很快，可能直接到达 ready（这是好事）
        if len(statuses) == 1 and statuses[0] == 'ready':
            print("✅ 解析速度极快，直接到达 ready 状态")
        else:
            # 如果有中间状态，验证转换合理
            assert 'pending' in statuses or 'parsing' in statuses, "缺少中间状态"


class TestPerformance:
    """性能测试"""
    
    def test_parsing_performance_under_5_seconds(self):
        """测试解析性能 < 5 秒"""
        ddl = """
        CREATE TABLE perf_test_1 (id INT, name VARCHAR(100));
        CREATE TABLE perf_test_2 (id INT, data TEXT);
        CREATE TABLE perf_test_3 (id INT, value DECIMAL(10,2));
        """
        files = {'file': ('perf.sql', io.BytesIO(ddl.encode('utf-8')), 'text/plain')}
        
        start = time.time()
        
        # 上传文件
        upload_response = requests.post(f"{BASE_URL}/api/files/upload", files=files)
        file_id = upload_response.json()['file_id']
        
        # 等待解析完成
        for _ in range(20):
            time.sleep(0.5)
            response = requests.get(f"{BASE_URL}/api/files/{file_id}")
            data = response.json()
            
            if data['status'] in ['ready', 'error']:
                break
        
        elapsed = time.time() - start
        
        # 验证性能要求
        assert data['status'] == 'ready', f"解析失败: {data.get('error_message')}"
        assert elapsed < 5, f"解析时间过长: {elapsed:.2f}s > 5s"
        
        print(f"✅ 解析性能: {elapsed:.2f}s < 5s")


if __name__ == "__main__":
    import pytest
    pytest.main([__file__, "-v"])
