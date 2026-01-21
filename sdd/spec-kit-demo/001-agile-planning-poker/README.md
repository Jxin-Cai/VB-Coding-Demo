# 敏捷估点服务 (Agile Planning Poker)

一个基于 Web 的 Planning Poker 工具，支持多人实时协作估点，使用 Fibonacci 数列和投票池机制。

## 功能特性

### 核心功能
- **需求池管理** - 创建和管理多个需求池
- **故事卡管理** - 添加、编辑、删除故事卡，支持按需排序
- **实时估点会话** - 支持多人同时参与的估点会话
- **Fibonacci 点数系统** - 使用标准 Fibonacci 数列（0, 1, 2, 3, 5, 8, 13, 21, 34）
- **投票池机制** - 团队成员投票，投票完成后展示结果
- **WebSocket 实时同步** - 投票状态实时更新
- **统计分析** - 自动计算平均点数、最大/最小值
- **权限控制** - 只有会话主持人可以完成会话并确定最终点数

### 技术特性
- **前后端分离** - Vue 3 + Spring Boot
- **实时通信** - WebSocket 支持
- **RESTful API** - 标准化 API 设计
- **响应式设计** - 支持桌面和移动设备
- **暗色主题** - 现代化 UI 设计

## 快速启动

### 环境要求
- Java 17+
- Node.js 18+
- Maven 3.6+

### 一键启动

```bash
# 1. 构建前端
./manage.sh build

# 2. 启动后端服务
./manage.sh start
```

服务启动后访问：http://localhost:8080

### 管理命令

```bash
# 构建前端（修改前端代码后需要重新构建）
./manage.sh build

# 启动后端服务
./manage.sh start

# 运行测试
./manage.sh test

# 清理构建文件
./manage.sh clean
```

## 服务端口

- **应用服务**: http://localhost:8080
- **H2 数据库控制台**: http://localhost:8080/h2-console

## 使用说明

### 1. 登录
- 访问首页，输入姓名即可登录
- 首次使用会自动创建账号

### 2. 创建需求池
- 登录后点击"创建需求池"
- 输入需求池名称和描述

### 3. 添加故事卡
- 进入需求池详情页
- 点击"添加故事卡"
- 填写标题和描述

### 4. 开始估点
- 在需求池列表中选择故事卡
- 点击"开始估点"
- 等待其他成员加入（可选）

### 5. 投票
- 点击"开始投票"启动倒计时
- 选择 Fibonacci 点数
- 等待所有成员完成投票

### 6. 揭示结果
- 投票完成后自动揭示结果
- 查看统计信息（平均值、最大/最小值）
- 主持人确定最终点数

### 7. 完成会话
- 主持人点击"完成估点"
- 输入最终确定的点数
- 故事卡状态更新为"已估算"

## 项目结构

```
001-agile-planning-poker/
├── frontend/              # Vue 3 前端项目
│   ├── src/              # 源代码
│   ├── dist/             # 构建输出（自动生成）
│   └── package.json      # 前端依赖
├── src/main/             # Spring Boot 后端
│   ├── java/             # Java 源代码
│   └── resources/        # 配置文件
│       └── static/       # 前端静态资源（自动生成）
├── manage.sh             # 项目管理脚本
└── README.md             # 本文件
```

## 开发说明

### 修改前端代码
```bash
# 修改 frontend/src/ 下的文件后
./manage.sh build
# 重启后端服务
```

### 修改后端代码
```bash
# 修改 src/main/java/ 下的文件后
# 重启 Spring Boot 服务即可
```

### 前端开发模式（可选）
```bash
cd frontend
npm run dev
# 前端运行在 http://localhost:5173
# 需要单独启动后端
```

## 技术栈

### 前端
- Vue 3 - 渐进式框架
- Element Plus - UI 组件库
- Pinia - 状态管理
- Vue Router - 路由管理
- Axios - HTTP 客户端
- @stomp/stompjs - WebSocket 客户端

### 后端
- Spring Boot 3.2 - 应用框架
- Spring WebSocket - WebSocket 支持
- MyBatis - ORM 框架
- H2 Database - 内存数据库
- Lombok - 简化 Java 代码

## API 文档

完整的 API 文档启动后可访问：
- Swagger UI: http://localhost:8080/swagger-ui.html

## 常见问题

### Q: 修改前端代码后没有生效？
A: 需要运行 `./manage.sh build` 重新构建前端，然后重启后端服务。

### Q: 如何重置数据库？
A: 停止服务，删除 `data/planning-poker.mv.db` 文件，重新启动服务。

### Q: 如何查看日志？
A: 后端日志会输出到控制台，包含详细的请求和错误信息。

## 许可证

本项目仅用于演示和学习目的。
