"""
意图识别模块
使用 LLM 智能识别用户消息是 SQL 生成类还是普通对话类
"""
from typing import Literal, Optional
from infrastructure.logging.logger import get_logger

logger = get_logger("intent_recognizer")


class IntentRecognizer:
    """意图识别器（基于LLM的智能判断）"""
    
    def __init__(self):
        """初始化意图识别器"""
        self._llm_service = None  # 延迟加载，避免循环依赖
    
    def _get_llm_service(self):
        """延迟加载LLM服务（避免循环依赖）"""
        if self._llm_service is None:
            from infrastructure.llm.llm_service import get_llm_service
            self._llm_service = get_llm_service()
        return self._llm_service
    
    def recognize(
        self,
        message: str,
        api_key: Optional[str] = None
    ) -> Literal['sql_generation', 'general_chat']:
        """
        使用LLM识别用户意图
        
        Args:
            message: 用户消息
            api_key: 用户的API Key（可选）
            
        Returns:
            str: 'sql_generation' 或 'general_chat'
        """
        logger.info(f"Recognizing intent for message: {message[:50]}...")
        
        try:
            llm_service = self._get_llm_service()
            
            # 如果LLM不可用且没有提供API Key，回退到规则匹配
            if not llm_service.is_available() and not api_key:
                logger.warning("LLM not available, using fallback rule-based recognition")
                return self._fallback_recognize(message)
            
            # 构建意图识别提示词
            system_prompt = """你是一个意图识别专家。你的任务是判断用户的消息是否是想要生成数据库查询SQL。

请根据以下规则判断：

**SQL查询意图的特征**：
- 明确要求查询、统计、分析数据
- 涉及数据库表、字段、数据
- 包含"查询"、"找出"、"统计"、"有多少"等关键词
- 例如："查询所有用户"、"统计订单数量"、"找出金额最高的订单"

**普通对话意图的特征**：
- 日常寒暄、问候
- 询问天气、时间等非数据库相关问题
- 闲聊、感谢等社交性对话
- 询问系统功能、使用帮助
- 例如："你好"、"今天天气怎么样"、"谢谢"、"你能做什么"

请只回答"sql"或"chat"，不要有任何其他内容。
- 如果用户意图是生成SQL查询，回答"sql"
- 如果用户意图是普通对话，回答"chat"
"""
            
            # 调用LLM判断
            response = llm_service.generate_response(
                user_message=f"用户消息：{message}\n\n这是SQL查询意图还是普通对话意图？",
                system_prompt=system_prompt,
                api_key=api_key
            )
            
            # 解析响应
            response_lower = response.strip().lower()
            
            if 'sql' in response_lower:
                intent = 'sql_generation'
            elif 'chat' in response_lower:
                intent = 'general_chat'
            else:
                # 如果LLM响应不明确，回退到规则匹配
                logger.warning(f"Unclear LLM response: {response}, using fallback")
                intent = self._fallback_recognize(message)
            
            logger.info(f"Intent recognized: {intent} (by LLM: {response.strip()})")
            return intent
            
        except Exception as e:
            logger.error(f"LLM intent recognition failed: {e}, using fallback")
            return self._fallback_recognize(message)
    
    def _fallback_recognize(self, message: str) -> Literal['sql_generation', 'general_chat']:
        """
        回退到基于规则的意图识别（当LLM不可用时）
        
        Args:
            message: 用户消息
            
        Returns:
            str: 'sql_generation' 或 'general_chat'
        """
        message_lower = message.lower()
        
        # SQL 生成关键词
        SQL_KEYWORDS = [
            '查询', '查找', '找出', '获取', '列出', '显示', '统计',
            'select', 'query', 'find', 'get', 'show', 'list',
            '我想看', '帮我查', '给我找', '有哪些', '多少',
            '分析', '汇总', '总结', '计算', '求和', 'sum', 'count', 'avg',
        ]
        
        # 普通对话关键词（强特征）
        CHAT_KEYWORDS = [
            '你好', 'hello', 'hi', '谢谢', '感谢', 'thank',
            '天气', 'weather', '时间', 'time', '日期', 'date',
            '你是谁', '你能做什么', '帮助', 'help',
        ]
        
        # 计算关键词匹配分数
        sql_score = sum(1 for keyword in SQL_KEYWORDS if keyword in message_lower)
        chat_score = sum(1 for keyword in CHAT_KEYWORDS if keyword in message_lower)
        
        # 判断逻辑
        if chat_score > 0:
            # 强对话特征，优先判定为普通对话
            intent = 'general_chat'
        elif sql_score > 0:
            # 包含SQL关键词，判定为SQL生成
            intent = 'sql_generation'
        else:
            # 模糊情况，默认为普通对话（更安全）
            intent = 'general_chat'
        
        logger.info(
            f"Fallback intent: {intent} | "
            f"SQL score: {sql_score}, Chat score: {chat_score}"
        )
        
        return intent


# 全局单例
_intent_recognizer = None


def get_intent_recognizer() -> IntentRecognizer:
    """
    获取意图识别器单例
    
    Returns:
        IntentRecognizer: 意图识别器实例
    """
    global _intent_recognizer
    if _intent_recognizer is None:
        _intent_recognizer = IntentRecognizer()
    return _intent_recognizer
