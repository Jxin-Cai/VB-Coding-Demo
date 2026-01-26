"""
对话和意图识别集成测试
"""
import sys
import requests
from pathlib import Path

# 添加 backend 目录到 Python 路径
BACKEND_DIR = Path(__file__).parent.parent.parent
sys.path.insert(0, str(BACKEND_DIR))

BASE_URL = "http://localhost:8000"


class TestIntentRecognition:
    """意图识别测试"""
    
    def test_recognize_sql_generation_intent(self):
        """测试识别 SQL 生成意图"""
        from domain.agent.intent_recognizer import get_intent_recognizer
        
        recognizer = get_intent_recognizer()
        
        # SQL 生成类消息
        sql_messages = [
            "查询所有用户",
            "找出订单金额大于1000的记录",
            "显示用户表中的所有数据",
            "统计订单数量",
            "我想看产品列表",
        ]
        
        for msg in sql_messages:
            intent = recognizer.recognize(msg)
            assert intent == 'sql_generation', \
                f"消息 '{msg}' 应识别为 sql_generation，实际: {intent}"
    
    def test_recognize_general_chat_intent(self):
        """测试识别普通对话意图"""
        from domain.agent.intent_recognizer import get_intent_recognizer
        
        recognizer = get_intent_recognizer()
        
        # 普通对话类消息
        chat_messages = [
            "什么是 RAG？",
            "如何使用这个系统？",
            "你好",
            "谢谢",
            "帮助",
        ]
        
        for msg in chat_messages:
            intent = recognizer.recognize(msg)
            assert intent == 'general_chat', \
                f"消息 '{msg}' 应识别为 general_chat，实际: {intent}"


class TestChatAPI:
    """对话 API 测试"""
    
    def test_chat_endpoint_accessible(self):
        """测试对话端点可访问"""
        payload = {
            "message": "你好",
            "file_id": None
        }
        
        response = requests.post(f"{BASE_URL}/api/chat", json=payload)
        assert response.status_code == 200, f"对话端点返回 {response.status_code}"
    
    def test_chat_response_format(self):
        """测试对话响应格式"""
        payload = {
            "message": "查询用户表",
            "file_id": None
        }
        
        response = requests.post(f"{BASE_URL}/api/chat", json=payload)
        data = response.json()
        
        # 验证必需字段
        assert "type" in data, "响应缺少 type 字段"
        assert "content" in data, "响应缺少 content 字段"
        assert "intent" in data, "响应缺少 intent 字段"
        
        # 验证 type 值
        assert data["type"] in ["text", "sql"], f"type 值不正确: {data['type']}"
    
    def test_sql_generation_request(self):
        """测试 SQL 生成类请求"""
        payload = {
            "message": "查询所有用户",
            "file_id": None
        }
        
        response = requests.post(f"{BASE_URL}/api/chat", json=payload)
        data = response.json()
        
        # 应该识别为 SQL 生成
        assert data["intent"] == "sql_generation", \
            f"意图识别错误: {data['intent']}"
        
        # 响应类型应该是 sql
        assert data["type"] == "sql", f"响应类型错误: {data['type']}"
    
    def test_general_chat_request(self):
        """测试普通对话类请求"""
        payload = {
            "message": "你好，请介绍一下自己",
            "file_id": None
        }
        
        response = requests.post(f"{BASE_URL}/api/chat", json=payload)
        data = response.json()
        
        # 应该识别为普通对话
        assert data["intent"] == "general_chat", \
            f"意图识别错误: {data['intent']}"
        
        # 响应类型应该是 text
        assert data["type"] == "text", f"响应类型错误: {data['type']}"
    
    def test_chat_logging(self):
        """测试对话日志系统配置正确"""
        import sys
        import logging
        
        # 导入controller获取logger配置
        sys.path.insert(0, str(BACKEND_DIR))
        from interface.api.chat_controller import logger as chat_logger
        
        # 验证logger存在且配置正确
        assert chat_logger is not None, "logger 未初始化"
        assert isinstance(chat_logger, logging.Logger), "logger 类型不正确"
        
        # 验证logger名称正确
        assert "chat" in chat_logger.name.lower(), \
            f"logger 名称不正确: {chat_logger.name}（应包含 'chat'）"
        
        # 验证logger级别配置合理
        effective_level = chat_logger.getEffectiveLevel()
        assert effective_level <= logging.INFO, \
            f"logger 级别过高: {logging.getLevelName(effective_level)}（应 <= INFO才能记录日志）"
        
        # 验证至少有一个handler（否则日志无处输出）
        has_handlers = len(chat_logger.handlers) > 0 or len(logging.getLogger().handlers) > 0
        assert has_handlers, \
            "logger 没有配置handler，日志无法输出。" \
            f"chat_logger handlers: {len(chat_logger.handlers)}, " \
            f"root handlers: {len(logging.getLogger().handlers)}"
        
        # 发送实际请求，验证API正常工作
        payload = {
            "message": "测试日志功能",
            "file_id": None
        }
        
        response = requests.post(f"{BASE_URL}/api/chat", json=payload)
        assert response.status_code == 200, \
            f"对话请求失败: {response.status_code}（日志可能未正常记录）"
        
        print(f"✅ 日志系统配置验证通过：logger={chat_logger.name}, "
              f"level={logging.getLevelName(effective_level)}, "
              f"handlers={len(chat_logger.handlers) + len(logging.getLogger().handlers)}")


if __name__ == "__main__":
    import pytest
    pytest.main([__file__, "-v"])
