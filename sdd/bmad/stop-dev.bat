@echo off
echo Stopping development environment...
echo.

REM 停止 Python 进程
echo Stopping backend server...
taskkill /F /IM python.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo Backend stopped
) else (
    echo No backend process found
)

echo.

REM 停止 Node.js 进程
echo Stopping frontend dev server...
taskkill /F /IM node.exe >nul 2>&1
if %errorlevel% equ 0 (
    echo Frontend stopped
) else (
    echo No frontend process found
)

echo.
echo Development environment stopped
