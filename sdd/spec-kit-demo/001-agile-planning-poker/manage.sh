#!/bin/bash

#############################################
# Planning Poker 项目管理脚本
# 版本: 1.0
# 日期: 2026-01-19
#############################################

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
FRONTEND_DIR="$PROJECT_ROOT/frontend"
STATIC_DIR="$PROJECT_ROOT/src/main/resources/static"

#############################################
# 工具函数
#############################################

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ $1${NC}"
}

#############################################
# 前端构建
#############################################

build_frontend() {
    print_header "重建前端"
    
    # 检查 Node.js
    if ! command -v node &> /dev/null; then
        print_error "Node.js 未安装"
        print_info "请先安装 Node.js 18+"
        exit 1
    fi
    
    print_info "Node.js 版本: $(node -v)"
    print_info "npm 版本: $(npm -v)"
    
    # 进入前端目录
    cd "$FRONTEND_DIR"
    
    # 检查 package.json
    if [ ! -f "package.json" ]; then
        print_error "未找到 package.json"
        exit 1
    fi
    
    # 安装依赖
    print_info "检查依赖..."
    if [ ! -d "node_modules" ]; then
        print_info "安装依赖..."
        npm install
    fi
    
    # 构建
    print_info "开始构建..."
    npm run build
    
    # 检查构建结果
    if [ ! -d "dist" ]; then
        print_error "构建失败：dist 目录不存在"
        exit 1
    fi
    
    # 清空 static 目录
    print_info "清空 static 目录..."
    rm -rf "$STATIC_DIR"/*
    
    # 复制构建结果
    print_info "复制构建结果到 static 目录..."
    cp -r dist/* "$STATIC_DIR/"
    
    # 返回项目根目录
    cd "$PROJECT_ROOT"
    
    print_success "前端构建完成！"
    print_info "输出目录: $STATIC_DIR"
    
    # 统计文件
    local file_count=$(find "$STATIC_DIR" -type f | wc -l | xargs)
    print_info "生成文件数: $file_count"
}

#############################################
# 后端操作
#############################################

start_backend() {
    print_header "启动后端"
    
    # 检查 Java
    if ! command -v java &> /dev/null; then
        print_error "Java 未安装"
        print_info "请先安装 Java 17+"
        exit 1
    fi
    
    print_info "Java 版本: $(java -version 2>&1 | head -n 1)"
    
    # 检查 Maven
    if ! command -v mvn &> /dev/null; then
        print_error "Maven 未安装"
        print_info "请先安装 Maven 3.6+"
        exit 1
    fi
    
    print_info "Maven 版本: $(mvn -version | head -n 1)"
    
    # 启动
    print_info "启动 Spring Boot..."
    print_warning "按 Ctrl+C 停止服务"
    mvn spring-boot:run
}

build_backend() {
    print_header "构建后端"
    
    print_info "清理旧构建..."
    mvn clean
    
    print_info "开始构建..."
    mvn package -DskipTests
    
    print_success "后端构建完成！"
    print_info "输出: target/planning-poker-1.0.0.jar"
}

#############################################
# 测试
#############################################

test_backend() {
    print_header "运行后端测试"
    
    mvn test
    
    print_success "后端测试完成"
}

test_apis() {
    print_header "测试所有API"
    
    local BASE_URL="http://localhost:8080"
    
    # 检查服务是否运行
    print_info "检查服务状态..."
    if ! curl -s "$BASE_URL/actuator/health" > /dev/null 2>&1; then
        print_error "后端服务未启动"
        print_info "请先运行: ./manage.sh start"
        exit 1
    fi
    
    print_success "服务正在运行"
    
    # 测试登录
    print_info "测试登录..."
    LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/participants/login" \
        -H "Content-Type: application/json" \
        -d '{"name":"测试用户"}')
    
    if echo "$LOGIN_RESPONSE" | grep -q "登录成功"; then
        print_success "登录测试通过"
        TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
        USER_NAME=$(echo "$LOGIN_RESPONSE" | grep -o '"userName":"[^"]*"' | cut -d'"' -f4)
        print_info "Token: ${TOKEN:0:20}..."
        print_info "用户名: $USER_NAME"
    else
        print_error "登录测试失败"
        echo "$LOGIN_RESPONSE"
        exit 1
    fi
    
    # 测试获取故事卡列表
    print_info "测试获取故事卡列表..."
    CARDS_RESPONSE=$(curl -s -X GET "$BASE_URL/api/v1/story-cards" \
        -H "X-User-Name: $USER_NAME")
    
    if echo "$CARDS_RESPONSE" | grep -q '"code":200'; then
        print_success "获取故事卡列表测试通过"
        CARD_COUNT=$(echo "$CARDS_RESPONSE" | grep -o '"id":[0-9]*' | wc -l | xargs)
        print_info "故事卡数量: $CARD_COUNT"
    else
        print_error "获取故事卡列表测试失败"
        echo "$CARDS_RESPONSE"
    fi
    
    # 测试创建故事卡
    print_info "测试创建故事卡..."
    CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/story-cards" \
        -H "Content-Type: application/json" \
        -H "X-User-Name: $USER_NAME" \
        -d '{"title":"测试卡片","description":"这是测试"}')
    
    if echo "$CREATE_RESPONSE" | grep -q '"code":200'; then
        print_success "创建故事卡测试通过"
        CARD_ID=$(echo "$CREATE_RESPONSE" | grep -o '"id":[0-9]*' | grep -o '[0-9]*')
        print_info "故事卡ID: $CARD_ID"
    else
        print_error "创建故事卡测试失败"
        echo "$CREATE_RESPONSE"
    fi
    
    # 测试开始估点
    if [ -n "$CARD_ID" ]; then
        print_info "测试开始估点..."
        SESSION_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/voting-sessions" \
            -H "Content-Type: application/json" \
            -H "X-User-Name: $USER_NAME" \
            -d "{\"storyCardId\":$CARD_ID}")
        
        if echo "$SESSION_RESPONSE" | grep -q '"code":200'; then
            print_success "开始估点测试通过"
            SESSION_ID=$(echo "$SESSION_RESPONSE" | grep -o '"id":[0-9]*' | head -1 | grep -o '[0-9]*')
            print_info "会话ID: $SESSION_ID"
            
            # 测试取消会话
            if [ -n "$SESSION_ID" ]; then
                print_info "测试取消估点..."
                CANCEL_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/voting-sessions/$SESSION_ID/cancel" \
                    -H "X-User-Name: $USER_NAME")
                
                if echo "$CANCEL_RESPONSE" | grep -q '"code":200'; then
                    print_success "取消估点测试通过"
                else
                    print_error "取消估点测试失败"
                    echo "$CANCEL_RESPONSE"
                fi
            fi
        else
            print_error "开始估点测试失败"
            echo "$SESSION_RESPONSE"
        fi
    fi
    
    print_success "API测试完成！"
    echo ""
    print_info "测试总结:"
    print_success "  ✓ 登录API"
    print_success "  ✓ 获取故事卡列表"
    print_success "  ✓ 创建故事卡"
    print_success "  ✓ 开始估点"
    print_success "  ✓ 取消估点"
}

#############################################
# 清理
#############################################

clean_all() {
    print_header "清理项目"
    
    print_info "清理 Maven 构建..."
    mvn clean
    
    print_info "清理前端构建..."
    rm -rf "$FRONTEND_DIR/dist"
    rm -rf "$FRONTEND_DIR/node_modules"
    
    print_info "清理 static 目录..."
    rm -rf "$STATIC_DIR"/*
    
    print_success "清理完成"
}

#############################################
# 帮助信息
#############################################

show_help() {
    cat << EOF
${BLUE}Planning Poker 项目管理脚本${NC}

${GREEN}用法:${NC}
    ./manage.sh <命令>

${GREEN}命令:${NC}
    ${YELLOW}build${NC}           重建前端（构建Vue项目并复制到static目录）
    ${YELLOW}start${NC}           启动后端服务（Spring Boot）
    ${YELLOW}test${NC}            运行所有测试（API测试）
    ${YELLOW}test-backend${NC}    运行后端单元测试
    ${YELLOW}build-backend${NC}   构建后端JAR包
    ${YELLOW}clean${NC}           清理所有构建文件
    ${YELLOW}help${NC}            显示帮助信息

${GREEN}示例:${NC}
    # 首次启动（或修改前端代码后）
    ./manage.sh build
    ./manage.sh start

    # 运行测试
    ./manage.sh test

    # 清理项目
    ./manage.sh clean

${GREEN}常见操作:${NC}
    1. 修改前端代码后：
       ./manage.sh build

    2. 修改后端代码后：
       重启 Spring Boot（Ctrl+C 后重新运行 ./manage.sh start）

    3. 验证功能：
       ./manage.sh test

${GREEN}端口信息:${NC}
    - 应用: http://localhost:8080
    - H2控制台: http://localhost:8080/h2-console

${GREEN}问题排查:${NC}
    - 前端问题：检查 frontend/dist 是否生成
    - 后端问题：检查 8080 端口是否被占用
    - API问题：运行 ./manage.sh test 查看详细错误

EOF
}

#############################################
# 主函数
#############################################

main() {
    # 检查参数
    if [ $# -eq 0 ]; then
        show_help
        exit 0
    fi
    
    # 切换到项目根目录
    cd "$PROJECT_ROOT"
    
    # 执行命令
    case "$1" in
        build)
            build_frontend
            echo ""
            print_success "🎉 前端重建完成！"
            print_warning "⚠️  请重启后端服务以加载新的前端代码"
            print_info "💡 提示: Ctrl+C 停止后端，然后运行 ./manage.sh start"
            ;;
        start)
            start_backend
            ;;
        test)
            test_apis
            ;;
        test-backend)
            test_backend
            ;;
        build-backend)
            build_backend
            ;;
        clean)
            clean_all
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "未知命令: $1"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# 运行主函数
main "$@"
