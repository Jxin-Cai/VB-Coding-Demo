<template>
  <div class="chat-interface">
    <!-- 消息显示区 -->
    <div ref="messagesContainer" class="messages-container">
      <div v-for="msg in messages" :key="msg.id" :class="['message', msg.role]">
        <div class="message-header">
          <span class="role-label">{{ msg.role === 'user' ? '你' : 'AI' }}</span>
          <span class="timestamp">{{ formatTime(msg.timestamp) }}</span>
        </div>
        
        <div class="message-content">
          <!-- 普通文本消息 -->
          <div v-if="msg.type === 'text'" class="text-message">
            {{ msg.content }}
          </div>
          
          <!-- SQL 消息 -->
          <div v-else-if="msg.type === 'sql'" class="sql-message">
            <div v-if="msg.explanation" class="explanation">
              {{ msg.explanation }}
            </div>
            <div class="sql-block">
              <pre><code>{{ msg.sql }}</code></pre>
              <a-button size="small" @click="copySql(msg.sql!)">
                复制 SQL
              </a-button>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 加载指示器 -->
      <div v-if="loading" class="loading-message">
        <a-spin size="small" />
        <span>AI 正在思考...</span>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="input-area">
      <a-textarea
        v-model:value="inputText"
        :rows="1"
        :auto-size="{ minRows: 1, maxRows: 5 }"
        placeholder="输入问题（Enter 发送，Shift+Enter 换行）..."
        @press-enter="handleEnter"
      />
      <a-button
        type="primary"
        :loading="loading"
        :disabled="!inputText.trim()"
        @click="handleSend"
      >
        发送
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, watch } from 'vue'
import { message } from 'ant-design-vue'
import { useChatStore } from '../stores/chat'
import axios from 'axios'

const chatStore = useChatStore()

// State
const inputText = ref('')
const messagesContainer = ref<HTMLElement>()

// Computed
const messages = computed(() => chatStore.messages)
const loading = computed(() => chatStore.loading)

// Methods
const handleEnter = (e: KeyboardEvent) => {
  if (!e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

const handleSend = async () => {
  const text = inputText.value.trim()
  if (!text || loading.value) return

  // 添加用户消息
  chatStore.addUserMessage(text)
  inputText.value = ''

  // 滚动到底部
  await scrollToBottom()

  // 发送到后端
  chatStore.setLoading(true)

  try {
    const response = await axios.post('/api/chat', {
      message: text,
      file_id: null  // TODO: 从 file store 获取当前文件 ID
    })

    const data = response.data

    // 添加 AI 响应
    chatStore.addAssistantMessage({
      content: data.content || data.response,
      type: data.type || 'text',
      sql: data.sql,
      explanation: data.explanation,
      references: data.references
    })

    // 滚动到底部
    await scrollToBottom()

  } catch (error: any) {
    console.error('Chat error:', error)
    message.error('发送失败，请重试')
    
    // 添加错误消息
    chatStore.addAssistantMessage({
      content: '抱歉，处理您的请求时出错了。请稍后重试。',
      type: 'text'
    })
  } finally {
    chatStore.setLoading(false)
  }
}

const copySql = (sql: string) => {
  navigator.clipboard.writeText(sql)
  message.success('SQL 已复制到剪贴板')
}

const formatTime = (timestamp: string) => {
  return new Date(timestamp).toLocaleTimeString('zh-CN')
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// Watch messages for auto-scroll
watch(() => chatStore.messages.length, () => {
  scrollToBottom()
})
</script>

<style scoped>
.chat-interface {
  display: flex;
  flex-direction: column;
  height: 600px;
  background: #fff;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.message.user {
  align-items: flex-end;
}

.message.assistant {
  align-items: flex-start;
}

.message-header {
  display: flex;
  gap: 8px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}

.role-label {
  font-weight: 500;
}

.message-content {
  max-width: 80%;
  padding: 12px;
  border-radius: 8px;
  word-wrap: break-word;
}

.message.user .message-content {
  background: #1890ff;
  color: #fff;
}

.message.assistant .message-content {
  background: #f5f5f5;
  color: rgba(0, 0, 0, 0.85);
}

.text-message {
  white-space: pre-wrap;
}

.sql-message {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.explanation {
  color: rgba(0, 0, 0, 0.65);
}

.sql-block {
  position: relative;
}

.sql-block pre {
  background: #262626;
  color: #f5f5f5;
  padding: 12px;
  border-radius: 4px;
  overflow-x: auto;
  margin: 0;
}

.sql-block button {
  position: absolute;
  top: 8px;
  right: 8px;
}

.loading-message {
  display: flex;
  align-items: center;
  gap: 8px;
  color: rgba(0, 0, 0, 0.45);
}

.input-area {
  padding: 16px;
  border-top: 1px solid #d9d9d9;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.input-area textarea {
  flex: 1;
}
</style>
