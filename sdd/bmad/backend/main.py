"""
FastAPI 应用入口文件
提供 API 服务和健康检查端点
支持单进程部署（前端静态文件由后端提供）
"""
from datetime import datetime
import os
import time
from pathlib import Path
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles

from config import settings
from infrastructure.logging.logger import logger, get_logger


# 创建 FastAPI 应用实例
app = FastAPI(
    title="RAG Text-to-SQL API",
    description="基于 RAG 的自然语言到 SQL 转换系统",
    version="0.1.0"
)

# 记录应用启动
logger.info("="*60)
logger.info("RAG Text-to-SQL Application Starting...")
logger.info(f"Environment: {'Production' if not settings.host.startswith('0.0.0.0') else 'Development'}")
logger.info(f"Log Level: {settings.log_level}")
logger.info(f"Server: {settings.host}:{settings.port}")
logger.info("="*60)

# 请求日志中间件
@app.middleware("http")
async def log_requests(request: Request, call_next):
    """
    记录所有 HTTP 请求
    包含：方法、路径、响应码、响应时间
    """
    # 记录请求开始
    start_time = time.time()
    
    # 处理请求
    try:
        response = await call_next(request)
        
        # 计算响应时间
        process_time = (time.time() - start_time) * 1000  # 转换为毫秒
        
        # 记录请求信息（仅记录 API 请求，忽略静态文件）
        if request.url.path.startswith("/api") or request.url.path == "/health":
            logger.info(
                f"Request: {request.method} {request.url.path} | "
                f"Status: {response.status_code} | "
                f"Time: {process_time:.2f}ms"
            )
        
        return response
    except Exception as e:
        # 记录异常请求
        process_time = (time.time() - start_time) * 1000
        logger.error(
            f"Request Failed: {request.method} {request.url.path} | "
            f"Error: {str(e)} | "
            f"Time: {process_time:.2f}ms",
            exc_info=True
        )
        raise


# 配置 CORS（允许前端开发服务器访问）
# 注意：单进程部署时，前端由同一服务器提供，可以移除此配置
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:5173",  # 开发环境：前端开发服务器
        "http://localhost:8000",   # 生产环境：单进程部署
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


# ==================== API 路由（必须在静态文件挂载之前注册）====================

# 导入健康检查路由
from interface.api.health_controller import router as health_router
# 导入文件上传路由
from interface.api.file_controller import router as file_router
# 导入对话路由
from interface.api.chat_controller import router as chat_router

# 注册健康检查路由
app.include_router(health_router, prefix="/api", tags=["health"])

# 注册文件上传路由
app.include_router(file_router, prefix="/api/files", tags=["files"])

# 注册对话路由
app.include_router(chat_router, prefix="/api/chat", tags=["chat"])

# 保留根路径健康检查端点（兼容性）
@app.get("/health")
async def root_health_check():
    """
    根路径健康检查端点（向后兼容）
    """
    # 委托给健康检查控制器
    from interface.api.health_controller import health_check
    return await health_check()


# 其他 API 路由将在此处注册
# app.include_router(chat_router, prefix="/api/chat")


# ==================== 静态文件服务（必须最后挂载）====================

# 检查 static 目录是否存在
static_dir = Path(__file__).parent / "static"
if static_dir.exists():
    from fastapi.responses import FileResponse
    
    # 创建一个 SPAStaticFiles 类来处理 Vue Router History 模式
    class SPAStaticFiles(StaticFiles):
        async def get_response(self, path: str, scope):
            try:
                return await super().get_response(path, scope)
            except Exception:
                # 如果文件不存在，返回 index.html（支持 Vue Router）
                index_path = os.path.join(self.directory, "index.html")
                if os.path.exists(index_path):
                    return FileResponse(index_path)
                raise
    
    # 挂载静态文件服务（使用自定义类支持 SPA）
    app.mount("/", SPAStaticFiles(directory="static", html=True), name="static")
    print(f"✅ Static files mounted from: {static_dir}")
else:
    print(f"⚠️  Static directory not found: {static_dir}")
    print("   Run './deploy.sh' to build and deploy frontend.")


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=settings.host,
        port=settings.port,
        reload=True,
        log_level=settings.log_level.lower()
    )
