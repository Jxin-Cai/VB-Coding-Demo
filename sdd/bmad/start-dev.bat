@echo off
echo Starting development environment...
echo.

REM 创建日志目录
if not exist logs mkdir logs

REM 启动后端
echo Starting backend server (http://localhost:8000)...
cd backend
start /B python main.py > ..\logs\backend.log 2>&1
cd ..
echo Backend started
echo.

REM 等待后端启动
timeout /t 2 /nobreak >nul

REM 启动前端
echo Starting frontend dev server (http://localhost:5173)...
cd frontend
start /B npm run dev > ..\logs\frontend.log 2>&1
cd ..
echo Frontend started
echo.

echo ============================================
echo Development environment started!
echo.
echo Access URLs:
echo    Frontend: http://localhost:5173
echo    Backend:  http://localhost:8000
echo    API Docs: http://localhost:8000/docs
echo.
echo Logs:
echo    Backend: logs\backend.log
echo    Frontend: logs\frontend.log
echo.
echo To stop:
echo    Run: stop-dev.bat
echo    Or press Ctrl+C to stop processes
echo ============================================
