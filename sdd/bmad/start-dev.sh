#!/bin/bash

echo "🚀 启动开发环境..."
echo ""

# 检查 PID 文件目录
mkdir -p .dev-pids

# 启动后端
echo "📦 启动后端服务器 (http://localhost:8000)..."
cd backend
python3 main.py > ../logs/backend.log 2>&1 &
BACKEND_PID=$!
echo $BACKEND_PID > ../.dev-pids/backend.pid
cd ..
echo "✅ 后端进程 PID: $BACKEND_PID"

# 等待后端启动
sleep 2

# 启动前端
echo ""
echo "📦 启动前端开发服务器 (http://localhost:5173)..."
cd frontend
npm run dev > ../logs/frontend.log 2>&1 &
FRONTEND_PID=$!
echo $FRONTEND_PID > ../.dev-pids/frontend.pid
cd ..
echo "✅ 前端进程 PID: $FRONTEND_PID"

echo ""
echo "============================================"
echo "✅ 开发环境启动完成！"
echo ""
echo "📍 访问地址："
echo "   前端开发服务器: http://localhost:5173"
echo "   后端 API 服务:  http://localhost:8000"
echo "   API 文档:       http://localhost:8000/docs"
echo ""
echo "📋 进程信息："
echo "   后端 PID: $BACKEND_PID"
echo "   前端 PID: $FRONTEND_PID"
echo ""
echo "📝 日志文件："
echo "   后端日志: logs/backend.log"
echo "   前端日志: logs/frontend.log"
echo ""
echo "🛑 停止服务："
echo "   运行: ./stop-dev.sh"
echo "   或: kill $BACKEND_PID $FRONTEND_PID"
echo "============================================"
