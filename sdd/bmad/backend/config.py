"""
配置管理模块
使用 Pydantic Settings 从环境变量加载配置
"""
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """应用配置类"""
    
    # LLM Configuration
    glm_api_key: str
    
    # Server Configuration
    host: str = "0.0.0.0"
    port: int = 8000
    
    # Logging Configuration
    log_level: str = "INFO"
    
    class Config:
        """Pydantic Settings 配置"""
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = False


# 全局配置实例
settings = Settings()
