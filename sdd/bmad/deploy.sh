#!/bin/bash

set -e  # 遇到错误立即退出

echo "🚀 Starting deployment..."

# Step 1: Build frontend
echo "📦 Building frontend..."
cd frontend
npm run build
cd ..

# Step 2: Clean backend static directory
echo "🧹 Cleaning backend static directory..."
rm -rf backend/static
mkdir -p backend/static

# Step 3: Copy frontend build to backend static
echo "📋 Copying frontend build to backend..."
cp -r frontend/dist/* backend/static/

# Step 4: Set permissions
echo "🔒 Setting file permissions..."
chmod -R 755 backend/static

echo ""
echo "✅ Deployment completed successfully!"
echo ""
echo "💡 To start the server:"
echo "   cd backend && python3 main.py"
echo ""
echo "📍 Access the application at: http://localhost:8000"
