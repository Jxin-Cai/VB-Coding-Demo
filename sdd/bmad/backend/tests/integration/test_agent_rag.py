"""
Agent 架构和 RAG 检索集成测试
"""
import sys
import io
import time
import requests
import pytest
from pathlib import Path

# 添加 backend 目录到 Python 路径
BACKEND_DIR = Path(__file__).parent.parent.parent
sys.path.insert(0, str(BACKEND_DIR))

BASE_URL = "http://localhost:8000"


class TestVectorSearchTool:
    """向量检索工具测试"""
    
    def test_vector_search_tool_creation(self):
        """测试向量检索工具创建"""
        from domain.agent.vector_search_tool import get_vector_search_tool
        
        tool = get_vector_search_tool()
        assert tool is not None, "工具未创建"
        assert tool.name == "vector_search", "工具名称不正确"
        assert tool.description, "工具描述缺失"
    
    def test_vector_search_tool_execution(self):
        """测试向量检索工具执行"""
        from domain.agent.vector_search_tool import get_vector_search_tool
        from infrastructure.parser.ddl_parser import ddl_parser
        from infrastructure.vector.vector_service import get_vector_service
        
        # 准备测试数据：解析并向量化一个表
        ddl = "CREATE TABLE test_users (id INT PRIMARY KEY, username VARCHAR(100));"
        tables = ddl_parser.parse(ddl)
        
        vector_service = get_vector_service()
        vector_service.vectorize_tables(tables, "test_tool_file")
        
        # 执行工具
        tool = get_vector_search_tool()
        result = tool._run("用户表")
        
        # 验证结果
        assert result is not None, "工具返回结果为空"
        assert "test_users" in result or "用户" in result, \
            f"结果应包含相关表信息: {result}"


class TestAgentService:
    """Agent 服务测试"""
    
    def test_agent_service_initialization(self):
        """测试 Agent 服务初始化"""
        from application.agent_service import get_agent_service
        
        service = get_agent_service()
        assert service is not None, "Agent 服务未初始化"
        assert service.llm_service is not None, "LLM 服务未初始化"
        assert len(service.tools) > 0, "工具列表为空"
    
    def test_agent_tools_registered(self):
        """测试工具已注册到 Agent"""
        from application.agent_service import get_agent_service
        
        service = get_agent_service()
        tool_names = [tool.name for tool in service.tools]
        
        assert "vector_search" in tool_names, "向量检索工具未注册"


class TestAgentIntegration:
    """Agent 集成测试（端到端）"""
    
    def test_chat_with_vector_context(self):
        """测试带向量上下文的对话（完整流程）"""
        # 前置检查：确保 LLM API 已配置
        from infrastructure.llm.llm_service import get_llm_service
        llm_service = get_llm_service()
        
        if not llm_service.is_available():
            pytest.skip("❌ LLM API 未配置，无法测试完整流程。请配置 GLM_API_KEY 环境变量。")
        
        # Step 1: 上传 DDL 文件并等待解析
        ddl = """
        CREATE TABLE customers (
            id INT PRIMARY KEY,
            name VARCHAR(100),
            email VARCHAR(255)
        );
        """
        files = {'file': ('customers.sql', io.BytesIO(ddl.encode('utf-8')), 'text/plain')}
        
        upload_response = requests.post(f"{BASE_URL}/api/files/upload", files=files)
        assert upload_response.status_code == 201, "文件上传失败"
        
        file_id = upload_response.json()['file_id']
        
        # 等待解析完成
        time.sleep(2)
        
        # Step 2: 发送对话请求
        chat_payload = {
            "message": "查询所有客户信息",
            "file_id": file_id
        }
        
        chat_response = requests.post(f"{BASE_URL}/api/chat", json=chat_payload)
        assert chat_response.status_code == 200, f"对话请求失败: {chat_response.status_code}"
        
        data = chat_response.json()
        
        # 严格验证：必须是 SQL 类型（不接受降级）
        assert 'type' in data, "响应缺少 type 字段"
        assert data['type'] == 'sql', \
            f"❌ 核心功能失败：期望返回 SQL，实际返回 {data['type']}。" \
            f"Content: {data.get('content', '')[:200]}"
        
        # 验证 SQL 字段存在且非空
        assert 'sql' in data, "SQL 响应缺少 sql 字段"
        assert isinstance(data['sql'], str), "SQL 字段类型错误"
        assert len(data['sql'].strip()) > 0, "生成的 SQL 为空"
        
        # 验证 SQL 内容合理性
        sql_lower = data['sql'].lower()
        assert 'select' in sql_lower, "生成的 SQL 不包含 SELECT 语句"
        assert 'customers' in sql_lower or 'customer' in sql_lower, \
            "生成的 SQL 未引用 customers 表"
        
        print(f"✅ SQL 生成成功: {data['sql'][:100]}...")


if __name__ == "__main__":
    import pytest
    pytest.main([__file__, "-v"])
