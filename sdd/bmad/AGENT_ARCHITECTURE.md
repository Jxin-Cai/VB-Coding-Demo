# RAG Text-to-SQL Agent 架构说明

## 🎯 架构设计原则

**从手动编排 → Agent自主决策**

### 改进前（手动编排）❌
```python
# 硬编码的流程
Step 1: 调用向量检索
Step 2: 构建DDL上下文
Step 3: 构建提示词
Step 4: 调用LLM生成SQL
Step 5: 清理SQL
```

**问题**：
- 流程固定，无法灵活调整
- Agent没有自主思考能力
- 必须每次都调用向量检索（即使不需要）

### 改进后（Agent自主决策）✅
```python
# LangChain ReAct Agent
Agent自主决定：
1. 是否需要调用向量检索工具
2. 如何使用检索结果
3. 何时生成最终答案
```

**优势**：
- ✅ Agent根据提示词自主规划流程
- ✅ 根据用户问题灵活调用工具
- ✅ 符合RAG标准流程（图中左侧Native RAG）

---

## 🏗️ 新架构组件

### 1. **AgentService（Agent服务）**
- 文件：`backend/application/agent_service.py`
- 职责：创建和管理LangChain Agent

**核心方法：**
```python
def _get_or_create_agent(api_key) -> AgentExecutor:
    """创建 ReAct Agent，配置工具和提示词"""
    
def process_message(user_message, api_key) -> Dict:
    """
    主入口：
    1. 获取Agent
    2. 让Agent处理用户消息
    3. 解析Agent输出
    """
```

### 2. **VectorSearchTool（向量检索工具）**
- 文件：`backend/domain/agent/vector_search_tool.py`
- 职责：作为LangChain Tool提供给Agent

**特性：**
- 继承自`BaseTool`
- Agent可通过Action调用
- 返回JSON格式的表结构信息

### 3. **Agent提示词设计**

**关键指导：**
```
你是 RAG Text-to-SQL 智能助手

工作流程：
1. 识别用户意图
   - 如果是SQL查询 → 必须先调用 vector_search 工具
   
2. 调用工具格式：
   Thought: 我需要了解表结构
   Action: vector_search
   Action Input: 用户关键词
   Observation: （工具返回结果）
   
3. 基于工具返回的表结构生成SQL
   
Final Answer: SELECT * FROM users WHERE ...
```

**Agent自主决策点：**
- ✅ 是否调用向量检索
- ✅ 使用哪些检索结果
- ✅ 如何组合SQL语句

---

## 🔄 RAG工作流程（对照图）

### 左侧 Native RAG 流程（当前实现）

```
用户Query
  ↓
问题分类 → 槽位提取 → 关键词切分 → 查询改写
  ↓
向量检索（vector_search工具）
  ↓
知识库 → 分级混合检索
  ↓
后置filter → topk去重 → Rerank
  ↓
context组装
  ↓
LLM+answer
```

**对应实现：**
1. **问题分类**：`intent_recognizer.py` 识别是否为SQL查询
2. **向量检索**：Agent调用`vector_search`工具
3. **context组装**：Agent在提示词中组装DDL上下文
4. **LLM生成**：Agent使用LLM生成最终SQL

### 右侧 Advanced RAG（未来扩展）

可以添加更多工具：
- `qa_recall_tool` - qa召回
- `doc_tree_tool` - DocTree层级召回
- `graph_search_tool` - 图召回

---

## 🛠️ 如何添加新工具

### 步骤1：创建Tool类
```python
# backend/domain/agent/new_tool.py
from langchain_core.tools import BaseTool

class NewTool(BaseTool):
    name = "new_tool"
    description = "工具描述，Agent会根据描述决定何时调用"
    
    def _run(self, query: str) -> str:
        # 工具逻辑
        return result
```

### 步骤2：注册到Agent
```python
# backend/application/agent_service.py
self.tools = [
    get_vector_search_tool(),
    get_new_tool()  # 添加新工具
]
```

### 步骤3：更新Agent提示词
```python
agent_prompt = """
...

你有以下工具可用：
- vector_search: 检索表结构
- new_tool: 新工具功能  # 描述新工具

根据用户需求选择合适的工具调用
"""
```

**Agent会自动学会何时调用新工具！**

---

## 📊 执行流程示例

### 用户问题："查询所有活跃用户"

**Agent思考过程：**

```
Thought: 我需要先了解数据库中有哪些用户相关的表
Action: vector_search
Action Input: 用户表 活跃

Observation: [
  {
    "type": "表",
    "name": "users",
    "description": "用户表"
  },
  {
    "type": "字段",
    "table": "users",
    "name": "status",
    "data_type": "VARCHAR",
    "description": "用户状态"
  }
]

Thought: 我现在知道了表结构，可以生成SQL了
Final Answer: SELECT * FROM users WHERE status = 'active';
```

**返回给前端：**
```json
{
  "sql": "SELECT * FROM users WHERE status = 'active';",
  "explanation": "由 Agent 自主调用向量检索工具并生成",
  "references": ["users"]
}
```

---

## 🔍 调试和监控

### 启用详细日志
```python
agent_executor = AgentExecutor(
    agent=agent,
    tools=self.tools,
    verbose=True,  # ✅ 启用，可以看到Agent思考过程
    max_iterations=5
)
```

### 日志输出示例
```
[Agent] Thought: 我需要查询表结构
[Agent] Action: vector_search
[Agent] Action Input: 用户
[Tool] VectorSearchTool called with query: 用户
[Tool] Vector search returned 3 results
[Agent] Observation: [...]
[Agent] Thought: 基于检索结果生成SQL
[Agent] Final Answer: SELECT * FROM users;
```

---

## 🎯 与图中流程的对应关系

| 图中步骤 | 当前实现 | 位置 |
|---------|---------|------|
| 问题分类 | IntentRecognizer | `domain/agent/intent_recognizer.py` |
| 向量检索 | VectorSearchTool | `domain/agent/vector_search_tool.py` |
| 知识库 | ChromaDB | `infrastructure/vector/vector_service.py` |
| context组装 | Agent自动完成 | Agent提示词中 |
| LLM+answer | ChatOpenAI (GLM-4) | `infrastructure/llm/llm_service.py` |

---

## 🚀 未来扩展

### 1. 添加混合检索
```python
class HybridSearchTool(BaseTool):
    """混合检索：向量检索 + 全文检索"""
    pass
```

### 2. 添加Rerank
```python
class RerankTool(BaseTool):
    """重排序检索结果"""
    pass
```

### 3. 添加查询改写
```python
class QueryRewriteTool(BaseTool):
    """优化用户查询"""
    pass
```

**Agent会自动学会使用这些工具！**

---

## 📝 总结

### 核心改进
1. ✅ **从手动编排 → Agent自主决策**
2. ✅ **向量检索作为Tool，而非硬编码步骤**
3. ✅ **Agent根据提示词灵活调用工具**
4. ✅ **符合RAG标准流程**

### 架构优势
- 🎯 灵活：Agent自主决定流程
- 🔧 可扩展：轻松添加新工具
- 📊 可观测：detailed logging
- 🚀 符合业界标准：LangChain ReAct Agent

### 下一步
- [ ] 测试Agent在各种场景下的表现
- [ ] 添加更多RAG工具（混合检索、Rerank等）
- [ ] 优化Agent提示词
- [ ] 添加Agent记忆（对话历史）
