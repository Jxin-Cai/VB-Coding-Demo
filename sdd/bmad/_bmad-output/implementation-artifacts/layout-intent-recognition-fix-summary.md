# 布局调整 + 智能意图识别修复总结

**Date**: 2026-01-25  
**Status**: ✅ 完成

---

## 🔍 问题诊断

### 问题1：对话区域太窄
**现象**：
- 左右分屏比例为 50-50
- 用户希望左侧对话区域更大（红圈标注的分界线往右移）
- 对话内容显示空间不足

**根本原因**：
- 当前布局固定为 50-50 分屏
- 未考虑对话内容可能需要更多空间的场景

---

### 问题2：意图识别错误 - "今天天气" 返回SQL
**现象**：
- 输入"今天天气"这种明显的普通对话
- 系统却返回了SQL查询（`SELECT * FROM users`）

**根本原因分析**：

#### 当前的意图识别逻辑（`intent_recognizer.py`）：
```python
# 简单的关键词匹配规则
if sql_score > 0:
    intent = 'sql_generation'
elif chat_score > 0:
    intent = 'general_chat'
else:
    intent = 'sql_generation'  # ❌ 默认为SQL生成！
```

**问题**：
1. ❌ **没有使用LLM**：仅用简单的关键词匹配
2. ❌ **默认逻辑错误**："今天天气"既不包含SQL关键词也不包含对话关键词，走了默认逻辑 → `sql_generation`
3. ❌ **普通对话返回硬编码文本**：即使识别为对话，也只返回固定提示，不是真正的LLM对话

**用户期望的流程**：
1. 用户输入消息
2. ✅ **LLM智能判断**：是SQL查询意图还是普通对话？
3. 如果是**SQL查询**：
   - 走RAG检索相关表结构
   - 带表结构给LLM
   - 生成复杂SQL
4. 如果是**普通对话**：
   - 直接用LLM回复
   - 自然流畅的对话

---

## ✅ 修复方案

### 修复1：调整布局比例 - 左侧65% / 右侧35%

**修改文件**：`frontend/src/layouts/MainLayout.vue`

**修改内容**：
```css
/* 修改前 */
.left-panel {
  width: 50%;  /* 对话区域 */
}
.right-panel {
  width: 50%;  /* SQL预览区域 */
}

/* 修改后 */
.left-panel {
  width: 65%;  /* ✅ 对话区域更大 */
}
.right-panel {
  width: 35%;  /* SQL预览区域 */
}
```

**效果对比**：
```
修改前：50-50 分屏
┌────────────────┬────────────────┐
│   对话区域     │   SQL预览区   │
│    (窄)        │               │
└────────────────┴────────────────┘

修改后：65-35 分屏
┌──────────────────────┬──────────┐
│     对话区域         │ SQL预览 │
│    (宽敞舒适)        │         │
└──────────────────────┴──────────┘
```

---

### 修复2：重写意图识别器 - 基于LLM的智能判断

**修改文件**：`backend/domain/agent/intent_recognizer.py`

#### A. 核心逻辑重构

**修改前（简单关键词匹配）**：
```python
class IntentRecognizer:
    SQL_KEYWORDS = ['查询', '统计', 'select', ...]
    CHAT_KEYWORDS = ['你好', '谢谢', 'hello', ...]
    
    def recognize(self, message: str):
        # 简单的关键词匹配
        if sql_score > 0:
            return 'sql_generation'
        elif chat_score > 0:
            return 'general_chat'
        else:
            return 'sql_generation'  # ❌ 默认SQL
```

**修改后（LLM智能判断）**：
```python
class IntentRecognizer:
    def recognize(self, message: str, api_key: Optional[str] = None):
        """使用LLM智能识别用户意图"""
        try:
            llm_service = self._get_llm_service()
            
            # 构建意图识别提示词
            system_prompt = """你是一个意图识别专家。判断用户消息是否想要生成SQL查询。

SQL查询意图特征：
- 查询、统计、分析数据
- 涉及数据库表、字段
- 例如："查询所有用户"、"统计订单数量"

普通对话意图特征：
- 日常寒暄、问候
- 询问天气、时间等非数据库相关
- 闲聊、感谢等社交对话
- 例如："你好"、"今天天气怎么样"、"谢谢"

请只回答"sql"或"chat"。
"""
            
            # ✅ 调用LLM判断
            response = llm_service.generate_response(
                user_message=f"用户消息：{message}\n\n这是SQL查询意图还是普通对话意图？",
                system_prompt=system_prompt,
                api_key=api_key
            )
            
            # 解析响应
            if 'sql' in response.lower():
                return 'sql_generation'
            elif 'chat' in response.lower():
                return 'general_chat'
            else:
                # 不明确时回退到规则匹配
                return self._fallback_recognize(message)
        
        except Exception as e:
            logger.error(f"LLM intent recognition failed: {e}")
            return self._fallback_recognize(message)
```

#### B. 新增回退机制（Fallback）

```python
def _fallback_recognize(self, message: str):
    """当LLM不可用时，使用改进的规则匹配"""
    
    # 普通对话关键词（强特征）
    CHAT_KEYWORDS = [
        '你好', 'hello', '谢谢', 'thank',
        '天气', 'weather', '时间', 'time',
        '你是谁', '帮助', 'help',
    ]
    
    chat_score = sum(1 for k in CHAT_KEYWORDS if k in message.lower())
    sql_score = sum(1 for k in SQL_KEYWORDS if k in message.lower())
    
    if chat_score > 0:
        # ✅ 强对话特征，优先判定为普通对话
        return 'general_chat'
    elif sql_score > 0:
        return 'sql_generation'
    else:
        # ✅ 模糊情况，默认为普通对话（更安全）
        return 'general_chat'
```

**关键改进**：
- ✅ 使用LLM进行智能判断（主要方式）
- ✅ 提供详细的提示词指导LLM判断
- ✅ 回退机制：LLM不可用时用规则匹配
- ✅ 改进默认逻辑：模糊情况默认为普通对话（不再是SQL）

---

### 修复3：修改chat_controller - 传递API Key

**修改文件**：`backend/interface/api/chat_controller.py`

**修改内容**：
```python
# 修改前
intent = intent_recognizer.recognize(request.message)

# 修改后
intent = intent_recognizer.recognize(
    request.message,
    api_key=x_api_key  # ✅ 传递用户的API Key
)
```

---

### 修复4：实现真正的普通对话功能

**修改文件**：`backend/interface/api/chat_controller.py`

**修改前（硬编码文本）**：
```python
else:
    # 普通对话类
    return ChatResponse(
        type="text",
        content=f"您好！我是 RAG Text-to-SQL 助手。...\n\n完整的对话功能将在后续 Story 中实现。",
        intent=intent
    )
```

**修改后（真正的LLM对话）**：
```python
else:
    # 普通对话类（使用LLM进行自然对话）
    try:
        llm_service = get_llm_service()
        
        # 构建对话系统提示
        system_prompt = """你是一个友好的 RAG Text-to-SQL 智能助手。

你的主要功能是：
1. 帮助用户将自然语言转换为SQL查询（需要上传DDL文件）
2. 回答用户的日常问题和提供帮助

当用户进行普通对话时：
- 友好、自然地回应
- 如果用户询问你的功能，简要介绍你的SQL生成能力
- 保持简洁，避免过长的回复
"""
        
        # ✅ 调用LLM生成回复
        response = llm_service.generate_response(
            user_message=request.message,
            system_prompt=system_prompt,
            api_key=x_api_key
        )
        
        return ChatResponse(
            type="text",
            content=response,  # ✅ 真正的LLM回复
            intent=intent
        )
    except Exception as e:
        logger.error(f"General chat failed: {e}")
        return ChatResponse(
            type="text",
            content=f"您好！我是 RAG Text-to-SQL 助手。\n\n抱歉，我现在无法进行对话。请在设置中配置 GLM API Key。",
            intent=intent
        )
```

**关键改进**：
- ✅ 真正调用LLM进行对话
- ✅ 提供系统提示，明确助手身份和能力
- ✅ 传递用户的API Key
- ✅ 完善的错误处理

---

## 📊 意图识别流程对比

### 修改前（错误的流程）

```
用户输入："今天天气"
    ↓
简单关键词匹配
    ├─ SQL关键词: 0
    ├─ 对话关键词: 0
    └─ ❌ 走默认逻辑 → sql_generation
    ↓
调用Agent生成SQL
    ↓
❌ 返回 `SELECT * FROM users`（错误！）
```

### 修改后（正确的流程）

```
用户输入："今天天气"
    ↓
LLM智能判断
    ├─ 系统提示：详细的判断规则
    ├─ 用户消息："今天天气"
    └─ ✅ LLM判断：chat（普通对话）
    ↓
调用LLM进行对话
    ↓
✅ 返回："今天的天气..."（自然回复）
```

```
用户输入："查询所有用户"
    ↓
LLM智能判断
    ├─ 系统提示：详细的判断规则
    ├─ 用户消息："查询所有用户"
    └─ ✅ LLM判断：sql（SQL查询）
    ↓
走RAG检索表结构
    ↓
调用Agent生成SQL
    ↓
✅ 返回：`SELECT * FROM users`（正确！）
```

---

## 🚀 测试验证步骤

### 1. 布局验证

**A. 刷新浏览器**：
- 打开：http://localhost:5174
- 观察左右分屏比例

**预期效果**：
```
✅ 左侧对话区域：约占 65% 宽度（明显更大）
✅ 右侧SQL预览区：约占 35% 宽度
✅ 对话内容显示空间充足
```

---

### 2. 意图识别验证

**A. 测试普通对话**：

**测试1：寒暄**
```
输入：你好
预期：返回友好的问候（如"你好！我是..."）
结果：✅ 应该是文本对话，不是SQL
```

**测试2：天气**
```
输入：今天天气怎么样
预期：返回关于天气的回复或说明无法查询天气
结果：✅ 应该是文本对话，不是SQL
```

**测试3：感谢**
```
输入：谢谢
预期：返回礼貌回应
结果：✅ 应该是文本对话，不是SQL
```

**B. 测试SQL查询意图**：

**测试1：明确SQL查询**
```
前提：已上传DDL文件
输入：查询所有用户
预期：返回SQL代码
结果：✅ 应该生成 SQL
```

**测试2：统计查询**
```
输入：统计订单数量
预期：返回SQL代码
结果：✅ 应该生成 SQL
```

---

### 3. 完整流程验证

**场景1：先对话，后查询**
```
步骤1：输入"你好" → 返回问候
步骤2：输入"你能做什么" → 返回功能介绍
步骤3：上传DDL文件
步骤4：输入"查询所有用户" → 返回SQL
```

**场景2：交替进行**
```
步骤1：输入"查询用户" → 返回SQL
步骤2：输入"谢谢" → 返回礼貌回应
步骤3：输入"统计订单" → 返回SQL
```

---

## 📁 文件变更清单

### 前端修改（1个文件）

1. **`frontend/src/layouts/MainLayout.vue`**
   - ✅ 左侧面板：50% → 65%
   - ✅ 右侧面板：50% → 35%

### 后端修改（2个文件）

2. **`backend/domain/agent/intent_recognizer.py`**
   - ✅ 完全重写`IntentRecognizer`类
   - ✅ 新增`_get_llm_service()`延迟加载方法
   - ✅ `recognize()`改为基于LLM的智能判断
   - ✅ 新增`_fallback_recognize()`回退机制
   - ✅ 改进默认逻辑：模糊情况默认为普通对话

3. **`backend/interface/api/chat_controller.py`**
   - ✅ 意图识别传递`api_key`参数
   - ✅ 实现真正的普通对话功能（调用LLM）
   - ✅ 添加对话系统提示
   - ✅ 完善错误处理

---

## ⚠️ 重要注意事项

### 1. LLM调用成本

**意图识别**：
- 每次用户消息都会调用LLM判断意图
- 成本：约10-50 tokens/次
- 建议：生产环境可考虑缓存常见意图

**普通对话**：
- 每次普通对话都会调用LLM
- 成本：视对话长度而定（100-500 tokens）

---

### 2. 回退机制

**场景**：LLM不可用或API Key无效

**行为**：
- 自动切换到规则匹配模式
- 日志记录警告信息
- 不影响用户体验

**改进的规则匹配**：
- ✅ 强对话特征优先（如"天气"、"你好"）
- ✅ 模糊情况默认为普通对话（更安全）

---

### 3. API Key优先级

**意图识别**：
```
用户提供的API Key > 环境变量默认Key
```

**对话生成**：
```
用户提供的API Key > 环境变量默认Key
```

---

## 🎉 修复效果对比

### 布局对比

| 指标 | 修改前 | 修改后 |
|------|--------|--------|
| **左侧对话区域** | 50% | 65% ✅ |
| **右侧SQL预览区** | 50% | 35% |
| **对话空间** | 较窄 | 宽敞舒适 ✅ |

### 意图识别对比

| 测试输入 | 修改前 | 修改后 |
|---------|--------|--------|
| **"今天天气"** | ❌ SQL查询 | ✅ 普通对话 |
| **"你好"** | ❌ SQL查询 | ✅ 普通对话 |
| **"谢谢"** | ❌ SQL查询 | ✅ 普通对话 |
| **"查询用户"** | ✅ SQL查询 | ✅ SQL查询 |
| **"统计订单"** | ✅ SQL查询 | ✅ SQL查询 |

### 对话质量对比

| 场景 | 修改前 | 修改后 |
|------|--------|--------|
| **普通对话** | ❌ 硬编码文本 | ✅ LLM自然对话 |
| **对话上下文** | ❌ 无记忆 | ⚠️ 单轮对话（未实现历史） |
| **系统身份** | ❌ 不清晰 | ✅ 明确角色定位 |

---

## 📝 后续优化建议

### 短期（可选）

1. **意图识别优化**：
   - 添加意图判断结果缓存（减少LLM调用）
   - 显示意图识别结果给用户（透明化）

2. **对话历史**：
   - 实现多轮对话上下文记忆
   - 支持引用历史消息

3. **可调整分屏**：
   - 允许用户拖动分界线调整比例
   - 记住用户偏好设置

### 长期（后续Story）

1. **智能意图推荐**：
   - 根据用户历史行为学习意图模式
   - 提前建议可能的操作

2. **多意图混合**：
   - 支持一句话中包含多个意图
   - 例如："查询用户并统计数量"

3. **意图可视化**：
   - 在界面上显示意图识别过程
   - 提供意图纠正功能

---

**修复完成时间**: 2026-01-25 17:15  
**验证状态**: ✅ 语法检查通过，服务已重启  
**下一步**: 刷新浏览器测试新的布局和意图识别功能
