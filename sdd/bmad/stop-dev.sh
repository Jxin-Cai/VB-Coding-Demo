#!/bin/bash

echo "🛑 停止开发环境..."
echo ""

# 读取 PID 文件
BACKEND_PID_FILE=".dev-pids/backend.pid"
FRONTEND_PID_FILE=".dev-pids/frontend.pid"

if [ -f "$BACKEND_PID_FILE" ]; then
    BACKEND_PID=$(cat $BACKEND_PID_FILE)
    if kill -0 $BACKEND_PID 2>/dev/null; then
        echo "🔴 停止后端服务器 (PID: $BACKEND_PID)..."
        kill $BACKEND_PID
        echo "✅ 后端已停止"
    else
        echo "⚠️  后端进程不存在 (PID: $BACKEND_PID)"
    fi
    rm $BACKEND_PID_FILE
else
    echo "⚠️  未找到后端 PID 文件"
fi

echo ""

if [ -f "$FRONTEND_PID_FILE" ]; then
    FRONTEND_PID=$(cat $FRONTEND_PID_FILE)
    if kill -0 $FRONTEND_PID 2>/dev/null; then
        echo "🔴 停止前端开发服务器 (PID: $FRONTEND_PID)..."
        kill $FRONTEND_PID
        echo "✅ 前端已停止"
    else
        echo "⚠️  前端进程不存在 (PID: $FRONTEND_PID)"
    fi
    rm $FRONTEND_PID_FILE
else
    echo "⚠️  未找到前端 PID 文件"
fi

echo ""

# 清理可能的僵尸进程
echo "🧹 清理可能的僵尸进程..."
pkill -f "python3 main.py" 2>/dev/null && echo "✅ 清理后端僵尸进程" || echo "   无后端僵尸进程"
pkill -f "vite" 2>/dev/null && echo "✅ 清理前端僵尸进程" || echo "   无前端僵尸进程"

echo ""
echo "✅ 开发环境已完全停止"
