# 监控文档 (MONITORING.md)

本文档说明如何监控 RAG Text-to-SQL 应用的运行状态和性能指标。

## 📋 目录

- [日志系统](#日志系统)
- [健康检查](#健康检查)
- [性能监控](#性能监控)
- [常见日志模式](#常见日志模式)
- [故障排查指南](#故障排查指南)

---

## 日志系统

### 日志级别说明

应用支持以下日志级别（按严重程度排序）：

| 级别 | 用途 | 示例 |
|------|------|------|
| **DEBUG** | 详细调试信息 | 函数调用参数、中间变量值 |
| **INFO** | 关键业务流程 | 应用启动、DDL上传、SQL生成完成 |
| **WARNING** | 警告信息 | 服务降级、API限流、配置缺失 |
| **ERROR** | 错误信息 | 异常捕获、API调用失败、解析错误 |

### 配置日志级别

通过环境变量配置（在 `backend/.env` 中）：

```env
LOG_LEVEL=INFO  # DEBUG | INFO | WARNING | ERROR
```

**建议**：
- **开发环境**：使用 `DEBUG` 或 `INFO`
- **生产环境**：使用 `INFO` 或 `WARNING`
- **问题排查**：临时调整为 `DEBUG`

### 日志格式

标准日志格式：
```
{timestamp} - {module_name} - {level} - {message}
```

**示例**：
```
2026-01-25 10:30:45 - bmad.api - INFO - Request: POST /api/chat | Status: 200 | Time: 250.50ms
2026-01-25 10:30:50 - bmad.ddl - INFO - DDL file uploaded: schema.sql (15.2 KB)
2026-01-25 10:31:00 - bmad.agent - WARNING - LLM API rate limit approached (90/100 requests)
2026-01-25 10:31:05 - bmad.sql - ERROR - SQL generation failed: Invalid table reference
```

### 敏感信息保护

日志系统自动过滤以下敏感信息：

**1. API Key**：
- 原始日志：`LLM API call with key: abc123xyz`
- 过滤后：`LLM API call with key: ***API_KEY***`

**2. 完整 SQL**：
- 原始日志：`Generated SQL: SELECT * FROM users WHERE id = 1 AND ...（500字符）`
- 过滤后：`Generated SQL: SELECT * FROM users WHERE id = 1 AND ... [SQL truncated]`（仅保留前100字符）

**3. DDL 内容**：
- 原始日志：`DDL content: CREATE TABLE users (id INT, ...（5000字符）)`
- 过滤后：`DDL content: CREATE TABLE users (id INT, ... [DDL truncated]`（仅保留前100字符）

---

## 健康检查

### 健康检查接口

**端点**：`GET /api/health`

**响应格式**：
```json
{
  "status": "healthy" | "degraded",
  "timestamp": "2026-01-25T10:30:45Z",
  "services": {
    "api": "running",
    "vector_store": "initialized" | "not_initialized",
    "llm_api": "connected" | "configured" | "error: ..."
  }
}
```

### 状态说明

**整体状态**：
- `healthy`: 所有服务正常运行
- `degraded`: 部分服务异常或未配置（但不影响基本功能）

**服务状态**：

| 服务 | 状态值 | 说明 |
|------|--------|------|
| `api` | `running` | API 服务正常运行（始终为此值） |
| `vector_store` | `initialized` | 向量库已初始化并可用 |
|  | `not_initialized` | 向量库未初始化（正常，首次启动） |
|  | `error: ...` | 向量库错误（详细错误信息） |
| `llm_api` | `connected` | LLM API 已配置且连接正常 |
|  | `configured` | LLM API 已配置（未测试连接） |
|  | `error: API key not configured` | LLM API Key 未配置 |
|  | `error: ...` | 其他 LLM 错误 |

### 使用方法

**1. 基础健康检查**：
```bash
curl http://localhost:8000/api/health
```

**2. 监控集成**：

**Prometheus 格式**（需要额外开发）：
```bash
curl http://localhost:8000/metrics
```

**脚本监控**：
```bash
#!/bin/bash
# health_monitor.sh

HEALTH_URL="http://localhost:8000/api/health"
STATUS=$(curl -s $HEALTH_URL | jq -r '.status')

if [ "$STATUS" != "healthy" ]; then
    echo "警告：系统状态异常 - $STATUS"
    # 发送告警通知
fi
```

**3. 定时检查（cron）**：
```bash
# 每5分钟检查一次健康状态
*/5 * * * * /path/to/health_monitor.sh
```

### 响应时间要求

健康检查接口响应时间应 **< 500ms**。

如果响应时间过长：
- 检查数据库连接
- 检查外部 API 调用
- 检查资源使用（CPU、内存）

---

## 性能监控

### 关键指标

**1. API 响应时间**：
- **监控方式**：请求日志中的 `Time: XXms` 字段
- **告警阈值**：
  - P95 > 1000ms（警告）
  - P95 > 3000ms（严重）

**2. 错误率**：
- **监控方式**：统计日志中 `ERROR` 级别的条目
- **告警阈值**：
  - 错误率 > 5%（警告）
  - 错误率 > 10%（严重）

**3. LLM API 调用成功率**：
- **监控方式**：统计 LLM 调用的成功/失败次数
- **告警阈值**：
  - 成功率 < 95%（警告）
  - 成功率 < 90%（严重）

**4. 向量库性能**：
- **监控方式**：记录向量检索时间
- **告警阈值**：
  - 检索时间 > 500ms（警告）
  - 检索时间 > 1000ms（严重）

### 日志分析脚本

**提取响应时间统计**：
```bash
# 统计最近 1000 条 API 请求的响应时间
grep "Request:" application.log | tail -1000 | \
  grep -oP "Time: \K[0-9.]+" | \
  awk '{sum+=$1; sumsq+=$1*$1; if($1>max)max=$1} END {
    avg=sum/NR;
    print "Avg:", avg "ms";
    print "Max:", max "ms";
    print "Requests:", NR
  }'
```

**统计错误率**：
```bash
# 统计最近 1000 条请求的错误率
TOTAL=$(grep "Request:" application.log | tail -1000 | wc -l)
ERRORS=$(grep "Request:" application.log | tail -1000 | grep "Status: [45]" | wc -l)
echo "Error Rate: $(echo "scale=2; $ERRORS * 100 / $TOTAL" | bc)%"
```

---

## 常见日志模式

### 正常运行日志

**应用启动**：
```
2026-01-25 10:00:00 - bmad - INFO - ============================================================
2026-01-25 10:00:00 - bmad - INFO - RAG Text-to-SQL Application Starting...
2026-01-25 10:00:00 - bmad - INFO - Environment: Development
2026-01-25 10:00:00 - bmad - INFO - Log Level: INFO
2026-01-25 10:00:00 - bmad - INFO - Server: 0.0.0.0:8000
2026-01-25 10:00:00 - bmad - INFO - ============================================================
2026-01-25 10:00:01 - bmad - INFO - ✅ Static files mounted from: /path/to/backend/static
```

**DDL 文件上传**：
```
2026-01-25 10:05:00 - bmad.ddl - INFO - DDL file upload started: schema.sql
2026-01-25 10:05:01 - bmad.ddl - INFO - DDL parsing successful: 25 tables, 150 columns
2026-01-25 10:05:02 - bmad.vector - INFO - Vector storage completed: 150 embeddings
2026-01-25 10:05:02 - bmad.ddl - INFO - DDL file upload completed: schema.sql
```

**SQL 生成请求**：
```
2026-01-25 10:10:00 - bmad.api - INFO - Request: POST /api/chat | Status: 200 | Time: 1250.50ms
2026-01-25 10:10:00 - bmad.agent - INFO - User query: 查询用户表中的所有记录
2026-01-25 10:10:01 - bmad.agent - INFO - LLM API call completed: 1200ms
2026-01-25 10:10:01 - bmad.sql - INFO - SQL generated: SELECT * FROM users ... [SQL truncated]
2026-01-25 10:10:01 - bmad.sql - INFO - SQL validation passed
```

### 异常日志模式

**配置缺失**：
```
2026-01-25 10:00:00 - bmad - WARNING - ⚠️  Static directory not found: /path/to/backend/static
2026-01-25 10:00:00 - bmad - WARNING -    Run './deploy.sh' to build and deploy frontend.
```

**API 调用失败**：
```
2026-01-25 10:15:00 - bmad.agent - ERROR - LLM API call failed: Connection timeout
2026-01-25 10:15:00 - bmad.api - INFO - Request: POST /api/chat | Status: 500 | Time: 5000.00ms
```

**解析错误**：
```
2026-01-25 10:20:00 - bmad.ddl - ERROR - DDL parsing failed: Invalid syntax at line 42
2026-01-25 10:20:00 - bmad.ddl - ERROR - File: schema.sql, Error: unexpected token 'CONSTRAINT'
```

---

## 故障排查指南

### 问题 1：应用无法启动

**症状**：
```
ModuleNotFoundError: No module named 'infrastructure'
```

**排查步骤**：
1. 检查 Python 路径配置
2. 验证所有 `__init__.py` 文件存在
3. 确认依赖已安装：`pip install -r requirements.txt`

---

### 问题 2：日志不输出

**症状**：控制台没有日志输出

**排查步骤**：
1. 检查 `LOG_LEVEL` 环境变量：
   ```bash
   echo $LOG_LEVEL
   ```
2. 验证 logger 初始化：
   ```python
   from infrastructure.logging.logger import logger
   logger.info("Test log")
   ```
3. 检查 `config.py` 是否正确读取 `.env` 文件

---

### 问题 3：健康检查返回 degraded

**症状**：
```json
{
  "status": "degraded",
  "services": {
    "llm_api": "error: API key not configured"
  }
}
```

**排查步骤**：
1. 检查 `.env` 文件是否存在：
   ```bash
   ls backend/.env
   ```
2. 验证 `GLM_API_KEY` 已配置：
   ```bash
   grep GLM_API_KEY backend/.env
   ```
3. 确保 API Key 不是占位符：
   - ❌ `GLM_API_KEY=your_api_key_here`
   - ✅ `GLM_API_KEY=actual_api_key_value`

---

### 问题 4：响应时间过长

**症状**：
```
Request: POST /api/chat | Status: 200 | Time: 5000.00ms
```

**排查步骤**：
1. 检查 LLM API 调用时间：
   ```bash
   grep "LLM API call" logs/* | grep -oP "Time: \K[0-9.]+"
   ```
2. 检查向量库检索时间：
   ```bash
   grep "Vector retrieval" logs/* | grep -oP "Time: \K[0-9.]+"
   ```
3. 检查网络连接（外部 API）
4. 检查系统资源使用：
   ```bash
   top
   free -h
   df -h
   ```

**优化建议**：
- 增加向量库索引
- 使用更快的 embedding 模型
- 启用结果缓存
- 优化数据库查询

---

### 问题 5：高错误率

**症状**：大量 ERROR 日志

**排查步骤**：
1. 统计错误类型：
   ```bash
   grep "ERROR" application.log | \
     awk -F"Error:" '{print $2}' | \
     sort | uniq -c | sort -rn
   ```
2. 查看详细错误堆栈：
   ```bash
   grep -A 10 "ERROR" application.log | less
   ```
3. 检查外部依赖状态：
   - LLM API 是否可用
   - 向量库是否正常
   - 网络连接是否稳定

**常见错误类型**：
- **LLM API 错误**：检查 API Key、配额、网络
- **解析错误**：检查 DDL 文件格式
- **向量库错误**：检查存储空间、权限

---

## 实时监控工具

### 使用 tail 实时查看日志

```bash
# 实时查看所有日志
tail -f application.log

# 仅查看 ERROR 日志
tail -f application.log | grep ERROR

# 仅查看 API 请求日志
tail -f application.log | grep "Request:"
```

### 使用 grep 过滤日志

```bash
# 查看特定时间段的日志
grep "2026-01-25 10:" application.log

# 查看特定模块的日志
grep "bmad.ddl" application.log

# 查看慢请求（响应时间 > 1000ms）
grep "Request:" application.log | awk -F"Time: " '{print $2}' | \
  awk -F"ms" '$1 > 1000 {print $0}'
```

### 监控脚本示例

**health_monitor.sh**：
```bash
#!/bin/bash

HEALTH_URL="http://localhost:8000/api/health"
LOG_FILE="/var/log/bmad/health_monitor.log"

while true; do
    TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
    RESPONSE=$(curl -s -w "\n%{http_code}" $HEALTH_URL)
    STATUS_CODE=$(echo "$RESPONSE" | tail -n 1)
    BODY=$(echo "$RESPONSE" | head -n -1)
    
    if [ "$STATUS_CODE" != "200" ]; then
        echo "[$TIMESTAMP] ERROR: Health check failed (HTTP $STATUS_CODE)" >> $LOG_FILE
    else
        HEALTH_STATUS=$(echo "$BODY" | jq -r '.status')
        if [ "$HEALTH_STATUS" != "healthy" ]; then
            echo "[$TIMESTAMP] WARNING: System degraded - $BODY" >> $LOG_FILE
        fi
    fi
    
    sleep 300  # 每 5 分钟检查一次
done
```

---

## 生产环境监控建议

### 1. 日志聚合

使用日志聚合工具集中管理日志：

**ELK Stack**：
- Elasticsearch（存储）
- Logstash（收集）
- Kibana（可视化）

**替代方案**：
- Grafana Loki
- Splunk
- CloudWatch Logs（AWS）

### 2. APM 工具

应用性能监控（APM）工具：
- **New Relic**：全栈监控
- **Datadog**：基础设施 + APM
- **Prometheus + Grafana**：开源方案

### 3. 告警配置

设置告警规则：

**响应时间告警**：
- P95 响应时间 > 2000ms
- P99 响应时间 > 5000ms

**错误率告警**：
- 5分钟内错误率 > 5%
- 连续 3 个健康检查失败

**资源使用告警**：
- CPU 使用率 > 80%
- 内存使用率 > 85%
- 磁盘使用率 > 90%

### 4. 仪表板设计

**关键指标仪表板**：
- 实时请求量（QPS）
- 平均响应时间（ms）
- 错误率（%）
- 健康状态（healthy/degraded）

**服务状态仪表板**：
- API 状态（运行/停止）
- 向量库状态（初始化/未初始化）
- LLM API 状态（连接/错误）

**业务指标仪表板**：
- DDL 文件上传量
- SQL 生成请求量
- 用户活跃度

---

## 附录

### 日志文件位置

**开发环境**：
- 日志输出到控制台（stdout）

**生产环境（建议配置）**：
- 应用日志：`/var/log/bmad/application.log`
- 错误日志：`/var/log/bmad/error.log`
- 访问日志：`/var/log/bmad/access.log`

### 相关文档

- [DEPLOYMENT.md](DEPLOYMENT.md) - 部署文档
- [README.md](README.md) - 项目介绍
- [Architecture.md](_bmad-output/planning-artifacts/architecture.md) - 架构设计

---

**📅 文档版本**: v1.0  
**📅 最后更新**: 2026-01-25  
**✍️ 维护者**: 开发团队
