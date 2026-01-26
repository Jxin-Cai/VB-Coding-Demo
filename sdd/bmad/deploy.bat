@echo off
echo Starting deployment...
echo.

echo Building frontend...
cd frontend
call npm run build
if errorlevel 1 (
    echo Frontend build failed!
    exit /b 1
)
cd ..

echo Cleaning backend static directory...
if exist backend\static rmdir /s /q backend\static
mkdir backend\static

echo Copying frontend build to backend...
xcopy /E /I /Y frontend\dist backend\static
if errorlevel 1 (
    echo Copy failed!
    exit /b 1
)

echo.
echo Deployment completed successfully!
echo.
echo To start the server:
echo    cd backend
echo    python main.py
echo.
echo Access the application at: http://localhost:8000
