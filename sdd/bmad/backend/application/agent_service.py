"""
Agent 应用服务
使用 LangChain Agent 框架，让 Agent 自主决定何时调用向量检索工具
"""
from typing import Dict, Any, Optional, List
import re

from langchain.agents import AgentExecutor, create_react_agent
from langchain_core.prompts import PromptTemplate
from langchain_core.messages import SystemMessage, HumanMessage

from infrastructure.llm.llm_service import get_llm_service
from domain.agent.vector_search_tool import get_vector_search_tool
from infrastructure.logging.logger import get_logger

logger = get_logger("agent_service")


class AgentService:
    """
    Agent 服务（基于 LangChain Agent 框架）
    
    核心设计：
    1. 使用 ReAct Agent 模式
    2. 向量检索作为 Tool 提供给 Agent
    3. Agent 根据提示词自主决定工作流程
    """
    
    def __init__(self):
        """初始化 Agent"""
        logger.info("Initializing LangChain Agent service...")
        
        # 获取 LLM 服务
        self.llm_service = get_llm_service()
        
        # 准备工具列表（Agent 可用的技能）
        self.tools = [
            get_vector_search_tool()
        ]
        
        # Agent Executor（延迟初始化，因为需要API Key）
        self.agent_executor = None
        
        logger.info(f"Agent initialized with {len(self.tools)} tools: {[t.name for t in self.tools]}")
    
    def _get_or_create_agent(self, api_key: Optional[str] = None) -> AgentExecutor:
        """
        获取或创建 Agent Executor
        
        Args:
            api_key: 用户提供的API Key
            
        Returns:
            AgentExecutor: Agent 执行器
        """
        # 获取 LLM（使用用户提供的API Key）
        llm = self.llm_service.get_chat_model(api_key=api_key)
        
        # 构建 ReAct Agent 提示词模板（LangChain 标准格式）
        # 必须包含所有必需变量: tools, tool_names, input, agent_scratchpad
        agent_prompt = PromptTemplate(
            input_variables=["tools", "tool_names", "input", "agent_scratchpad"],
            template="""你是一个专业的 RAG Text-to-SQL 智能助手。

你的核心任务是：根据用户的自然语言问题生成准确的 SQL 查询语句。

TOOLS:
------
你可以使用以下工具：

{tools}

工具名称: {tool_names}

使用工具的格式（严格遵循）:
```
Thought: 思考当前需要做什么
Action: 工具名称
Action Input: 工具的输入参数
Observation: 工具返回的结果
... (这个 Thought/Action/Action Input/Observation 可以重复多次)
Thought: 我现在知道最终答案了
Final Answer: 最终答案
```

**重要工作流程：**
1. 对于SQL查询问题，必须先调用 vector_search 工具获取表结构
2. 基于工具返回的表结构信息生成SQL
3. 只生成SELECT语句
4. Final Answer只包含SQL语句本身

**示例：**

Question: 查询所有活跃用户
Thought: 我需要先了解用户表的结构
Action: vector_search
Action Input: 用户表 活跃
Observation: [{{"type": "表", "name": "users", "description": "用户表"}}, {{"type": "字段", "table": "users", "name": "status", "data_type": "VARCHAR"}}]
Thought: 我现在知道了表结构，可以生成SQL了
Final Answer: SELECT * FROM users WHERE status = 'active';

开始！

Question: {input}
Thought: {agent_scratchpad}"""
        )
        
        # 创建 Agent
        agent = create_react_agent(
            llm=llm,
            tools=self.tools,
            prompt=agent_prompt
        )
        
        # 创建 Agent Executor
        agent_executor = AgentExecutor(
            agent=agent,
            tools=self.tools,
            verbose=True,  # 启用详细日志
            max_iterations=5,  # 最多5轮思考
            handle_parsing_errors=True  # 处理解析错误
        )
        
        return agent_executor
    
    async def process_message(
        self,
        user_message: str,
        file_id: Optional[str] = None,
        api_key: Optional[str] = None
    ) -> Dict[str, Any]:
        """
        处理用户消息（使用 LangChain Agent 框架）
        
        Args:
            user_message: 用户消息
            file_id: 当前文件 ID（可选）
            api_key: 用户提供的API Key（可选）
            
        Returns:
            Dict: 响应数据（SQL、解释、引用表等）
        """
        logger.info(f"Agent processing message: {user_message[:50]}...")
        if api_key:
            logger.info("Using user-provided API Key")
        
        try:
            # 获取 Agent Executor
            agent_executor = self._get_or_create_agent(api_key=api_key)
            
            # 执行 Agent（Agent 会自主决定是否调用工具）
            logger.info("Invoking Agent...")
            result = agent_executor.invoke({
                "input": user_message
            })
            
            # 提取结果
            agent_output = result.get('output', '')
            
            logger.info(f"Agent output: {agent_output[:200]}...")
            
            # 清理 SQL
            sql = self._clean_sql(agent_output)
            
            # 提取引用的表名（从 Agent 的中间步骤中提取）
            referenced_tables = self._extract_tables_from_agent_steps(result)
            
            return {
                "sql": sql,
                "explanation": "由 Agent 自主调用向量检索工具并生成",
                "references": referenced_tables
            }
        
        except Exception as e:
            logger.error(f"Agent processing failed: {e}", exc_info=True)
            raise
    
    def _extract_tables_from_agent_steps(self, agent_result: Dict[str, Any]) -> List[Dict[str, Any]]:
        """
        从 Agent 执行结果中提取引用的表名和字段信息
        
        Args:
            agent_result: Agent 执行结果
            
        Returns:
            List[Dict[str, Any]]: 表引用列表，格式为 [{"table": "table_name", "fields": ["field1", "field2"]}]
        """
        import json
        import re
        
        # 使用字典来存储表和对应的字段
        table_info = {}
        
        # 从 intermediate_steps 中提取
        intermediate_steps = agent_result.get('intermediate_steps', [])
        
        for step in intermediate_steps:
            # step 是 (AgentAction, observation) 的元组
            if len(step) >= 2:
                observation = step[1]
                
                # 解析 observation（JSON字符串）
                try:
                    if isinstance(observation, str):
                        results = json.loads(observation)
                        if isinstance(results, list):
                            for item in results:
                                if item.get('type') == '表':
                                    table_name = item.get('name')
                                    if table_name:
                                        if table_name not in table_info:
                                            table_info[table_name] = {"table": table_name, "fields": []}
                                elif item.get('type') == '字段':
                                    table_name = item.get('table')
                                    field_name = item.get('name')
                                    if table_name and field_name:
                                        if table_name not in table_info:
                                            table_info[table_name] = {"table": table_name, "fields": []}
                                        if field_name not in table_info[table_name]["fields"]:
                                            table_info[table_name]["fields"].append(field_name)
                except:
                    pass
        
        # 如果没有从步骤中提取到，尝试从输出的SQL中提取
        if not table_info:
            output = agent_result.get('output', '')
            # FROM table_name
            from_matches = re.findall(r'FROM\s+(\w+)', output, re.IGNORECASE)
            for table_name in from_matches:
                if table_name not in table_info:
                    table_info[table_name] = {"table": table_name}
            
            # JOIN table_name
            join_matches = re.findall(r'JOIN\s+(\w+)', output, re.IGNORECASE)
            for table_name in join_matches:
                if table_name not in table_info:
                    table_info[table_name] = {"table": table_name}
        
        # 转换为列表，如果没有字段信息则移除空的fields数组
        result = []
        for info in table_info.values():
            if not info.get("fields"):
                # 如果没有字段信息，移除fields键
                result.append({"table": info["table"]})
            else:
                result.append(info)
        
        return result
    
    def _clean_sql(self, sql_response: str) -> str:
        """
        清理 Agent/LLM 返回的 SQL 语句
        
        Args:
            sql_response: 原始响应
            
        Returns:
            str: 清理后的 SQL 语句
        """
        sql = sql_response.strip()
        
        # 移除 markdown 代码块标记
        if sql.startswith('```sql'):
            sql = sql[6:]
        elif sql.startswith('```'):
            sql = sql[3:]
        
        if sql.endswith('```'):
            sql = sql[:-3]
        
        sql = sql.strip()
        
        # 移除 "Final Answer:" 前缀（ReAct Agent 可能产生）
        if sql.startswith('Final Answer:'):
            sql = sql[len('Final Answer:'):].strip()
        
        # 如果不是以 SELECT 开头，尝试提取
        if not sql.upper().startswith('SELECT'):
            lines = sql.split('\n')
            for line in lines:
                line_clean = line.strip()
                if line_clean.upper().startswith('SELECT'):
                    sql = line_clean
                    break
        
        return sql


# 全局单例
_agent_service_instance = None


def get_agent_service() -> AgentService:
    """
    获取 Agent 服务单例
    
    Returns:
        AgentService: Agent 服务实例
    """
    global _agent_service_instance
    if _agent_service_instance is None:
        _agent_service_instance = AgentService()
    return _agent_service_instance
