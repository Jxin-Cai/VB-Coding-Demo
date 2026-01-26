"""
日志配置模块
提供结构化日志功能和敏感信息过滤
"""
import logging
import sys
import re
from typing import Optional

from config import settings


class SensitiveDataFilter(logging.Filter):
    """
    敏感信息过滤器
    过滤日志中的 API Key、完整 SQL、DDL 内容等敏感信息
    """
    
    def __init__(self):
        super().__init__()
        # 构建需要过滤的模式
        self.api_key_pattern = re.compile(r'(api[_-]?key["\']?\s*[:=]\s*["\']?)[^\s"\']+', re.IGNORECASE)
        self.sql_truncate_length = 100
        self.ddl_truncate_length = 100
    
    def filter(self, record: logging.LogRecord) -> bool:
        """
        过滤敏感信息
        
        Args:
            record: 日志记录对象
            
        Returns:
            bool: 是否保留此日志记录（始终返回 True，只修改内容）
        """
        message = str(record.msg)
        
        # 过滤 API Key
        if hasattr(settings, 'glm_api_key') and settings.glm_api_key:
            message = message.replace(settings.glm_api_key, '***API_KEY***')
        
        # 使用正则过滤 API Key 模式
        message = self.api_key_pattern.sub(r'\1***API_KEY***', message)
        
        # 过滤完整 SQL（保留前 100 字符）
        if 'SELECT' in message.upper() or 'INSERT' in message.upper() or \
           'UPDATE' in message.upper() or 'DELETE' in message.upper():
            if len(message) > self.sql_truncate_length:
                message = message[:self.sql_truncate_length] + '... [SQL truncated]'
        
        # 过滤 DDL 内容（保留前 100 字符）
        if 'CREATE TABLE' in message.upper() or 'ALTER TABLE' in message.upper() or \
           'DROP TABLE' in message.upper():
            if len(message) > self.ddl_truncate_length:
                message = message[:self.ddl_truncate_length] + '... [DDL truncated]'
        
        # 更新记录的消息
        record.msg = message
        return True


def setup_logging() -> logging.Logger:
    """
    设置日志系统
    
    Returns:
        logging.Logger: 配置好的 logger 实例
    """
    # 获取日志级别
    log_level = getattr(logging, settings.log_level.upper(), logging.INFO)
    
    # 配置日志格式
    log_format = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
    date_format = '%Y-%m-%d %H:%M:%S'
    
    # 配置根日志
    logging.basicConfig(
        level=log_level,
        format=log_format,
        datefmt=date_format,
        handlers=[
            logging.StreamHandler(sys.stdout)
        ]
    )
    
    # 为所有 handler 添加敏感信息过滤器
    for handler in logging.root.handlers:
        handler.addFilter(SensitiveDataFilter())
    
    # 创建应用专用 logger
    logger = logging.getLogger("bmad")
    logger.setLevel(log_level)
    
    return logger


def get_logger(name: Optional[str] = None) -> logging.Logger:
    """
    获取 logger 实例
    
    Args:
        name: logger 名称（可选）
        
    Returns:
        logging.Logger: logger 实例
    """
    if name:
        return logging.getLogger(f"bmad.{name}")
    return logging.getLogger("bmad")


# 初始化全局 logger
logger = setup_logging()
