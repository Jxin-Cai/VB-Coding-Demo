# RAG Text-to-SQL 系统 MVP 需求文档

## 📋 项目概述

**项目名称**：RAG Text-to-SQL 智能查询系统

**项目目标**：构建一个基于 RAG（检索增强生成）技术的智能 SQL 生成系统，用户可以上传数据库 DDL 文件，通过自然语言描述生成精确的 SQL 语句。

**核心价值**：降低 SQL 编写门槛，让非技术人员也能通过自然语言与数据库交互。

---

## 🎯 功能需求

### 1. DDL 文件管理模块

#### 1.1 DDL 上传

- **支持的数据库类型**：
  - MySQL
  - PostgreSQL
  - Oracle
- **上传方式**：文件上传（.sql 格式）
- **处理逻辑**：
  - 解析 DDL 文件，提取表结构、字段、索引、约束等元数据
  - 将 DDL 信息向量化并存储到**内存向量库**
  - 支持多个 DDL 文件上传（多数据库 schema）

#### 1.2 DDL 预览

- 上传 DDL 后，展示解析出的表结构
- 显示内容包括：
  - 表名
  - 字段名、类型、约束
  - 索引信息
  - 外键关系

### 2. 自然语言转 SQL 模块

#### 2.1 查询能力范围

- **支持的 SQL 操作**：
  - ✅ SELECT（查询）
  - ✅ INSERT（插入）
  - ✅ UPDATE（更新）
  - ✅ DELETE（删除）
- **支持的复杂查询**：
  - JOIN（多表关联）
  - 子查询（Subquery）
  - 聚合函数（COUNT、SUM、AVG、MAX、MIN）
  - GROUP BY / HAVING
  - ORDER BY / LIMIT

#### 2.2 查询流程

1. 用户输入自然语言描述
2. 系统基于 RAG 检索相关的 DDL 向量
3. 调用 GLM 模型生成 SQL 语句
4. **仅生成 SQL，不执行查询**
5. 将生成的 SQL 返回给前端展示

#### 2.3 功能限制

- ❌ 不需要查询历史记录功能
- ❌ 不执行 SQL 语句（仅生成）

### 3. GLM 模型集成

#### 3.1 配置方式

- 用户只需提供 **GLM API Key**
- 系统自动配置并连接 GLM 模型

---

## 🏗️ 技术架构

### 前端技术栈

- **框架**：Vue.js
- **构建工具**：npm run build

### 后端技术栈

- **语言**：python
- **框架**：
  - langchain 全家桶
- **向量存储**：内存向量库
- **AI 模型**：GLM（通过 API Key 集成）

### 部署架构

- **部署方式**：单进程启动
- **流程**：
  1. 前端代码执行 `npm run build`
  2. 编译产物复制到 `src/main/resources/static`
  3. 后端启动时同时提供前后端服务

---

## 📐 开发规范

### 后端架构规范

#### 1. DDD 分层架构

采用领域驱动设计（DDD），分层如下：

- **Presentation Layer（展示层）**：Controller、DTO
- **Application Layer（应用层）**：Service、Application Service
- **Domain Layer（领域层）**：
  - Entity（实体）
  - Value Object（值对象）
  - Domain Service（领域服务）
  - Repository Interface（仓储接口）
- **Infrastructure Layer（基础设施层）**：
  - Repository Implementation（仓储实现）
  - 外部服务调用（GLM API）
  - 向量存储实现

#### 2. DDD POJO 定义规范

- 使用 **DDD 战术模式** 定义 POJO：
  - Entity：具有唯一标识的对象
  - Value Object：不可变的值对象
  - Aggregate Root：聚合根
- 遵循 **充血模型**，而非贫血模型

#### 3. 面向对象编程原则

- 遵循 **SOLID 原则**
- 合理使用设计模式
- 避免面向过程式编程

#### 4. 代码质量规范

- **重构**：识别并规避《重构（Refactoring）》中描述的代码坏味道，例如：
  - 重复代码（Duplicated Code）
  - 过长方法（Long Method）
  - 过大类（Large Class）
  - 过长参数列表（Long Parameter List）
  - 发散式变化（Divergent Change）
  - 霰弹式修改（Shotgun Surgery）
- **Effective Java**：遵循《Effective Java》编码建议，例如：
  - 使用构建器（Builder）处理多参数构造
  - 优先使用枚举而非 int 常量
  - 最小化可变性
  - 合理使用 Optional
  - 优先使用标准异常

---
