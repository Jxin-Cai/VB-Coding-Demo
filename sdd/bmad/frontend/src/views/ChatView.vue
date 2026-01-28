<template>
  <div class="chat-container">
    <div class="chat-header">
      <h1>💬 AI SQL 生成助手</h1>
      <div class="header-info">
        <span v-if="currentFile" class="current-file">
          📄 当前文件: {{ currentFile }}
        </span>
        <span v-else class="no-file-warning">
          ⚠️ 请先上传 DDL 文件
        </span>
      </div>
    </div>

    <div class="chat-messages" ref="messagesContainer">
      <div v-if="messages.length === 0" class="empty-state">
        <div class="welcome-message">
          <h2>👋 欢迎使用 AI SQL 生成助手</h2>
          <p>请用自然语言描述您的查询需求，例如：</p>
          <ul>
            <li>"查询用户表中状态为活跃的用户"</li>
            <li>"统计最近30天的订单数量"</li>
            <li>"找出购买次数最多的前10位用户"</li>
          </ul>
          <a-button type="link" @click="goToFiles">
            还没有上传 DDL？点击这里上传 →
          </a-button>
        </div>
      </div>

      <div
        v-for="(msg, index) in messages"
        :key="index"
        :class="['message', msg.role]"
      >
        <div class="message-content">
          <div v-if="msg.role === 'user'" class="user-message">
            {{ msg.content }}
          </div>

          <div v-else-if="msg.role === 'assistant'" class="assistant-message">
            <div v-if="msg.type === 'sql'" class="sql-response">
              <SQLCodeBlock
                :sql="msg.sql"
                :responseTime="msg.responseTime"
              />
              <References
                v-if="msg.references"
                :references="msg.references"
              />
            </div>
            <div v-else class="text-response">
              {{ msg.content }}
            </div>
          </div>

          <LoadingAnimation v-else-if="msg.role === 'loading'" />
        </div>
      </div>
    </div>

    <div class="chat-input-area">
      <ErrorDisplay
        v-if="error"
        :error="error"
        :show-retry="true"
        @close="error = null"
        @retry="handleRetry"
      />

      <div class="input-wrapper">
        <a-textarea
          v-model:value="userInput"
          :placeholder="currentFile ? '输入您的需求，如：查询用户表中的所有活跃用户' : '请先上传 DDL 文件'"
          :disabled="!currentFile || isLoading"
          :auto-size="{ minRows: 2, maxRows: 5 }"
          @keydown.enter.exact="handleSend"
          @keydown.enter.shift.exact.prevent="handleNewLine"
          class="chat-input"
        />
        <a-button
          type="primary"
          :disabled="!userInput.trim() || !currentFile || isLoading"
          :loading="isLoading"
          @click="handleSend"
          class="send-button"
        >
          发送
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import SQLCodeBlock from '../components/SQLCodeBlock.vue'
import References from '../components/References.vue'
import LoadingAnimation from '../components/LoadingAnimation.vue'
import ErrorDisplay from '../components/ErrorDisplay.vue'

const router = useRouter()

// State
const userInput = ref('')
const messages = ref<any[]>([])
const isLoading = ref(false)
const error = ref<any>(null)
const currentFile = ref<string | null>(null)
const messagesContainer = ref<HTMLElement>()

// Methods
const goToFiles = () => {
  router.push('/')
}

const handleSend = async (event?: KeyboardEvent) => {
  if (event) {
    event.preventDefault()
  }

  if (!userInput.value.trim() || !currentFile.value || isLoading.value) {
    return
  }

  const query = userInput.value.trim()
  
  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: query
  })

  // 清空输入
  userInput.value = ''

  // 添加加载状态
  messages.value.push({
    role: 'loading'
  })

  isLoading.value = true
  error.value = null

  // 滚动到底部
  await nextTick()
  scrollToBottom()

  try {
    // 调用 Chat API
    const response = await fetch('/api/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        message: query,
        file_id: 'current' // TODO: 获取当前 file_id
      })
    })

    if (!response.ok) {
      throw new Error(`API 错误: ${response.status}`)
    }

    const data = await response.json()

    // 移除加载状态
    messages.value = messages.value.filter(m => m.role !== 'loading')

    // 添加 AI 响应
    if (data.type === 'sql') {
      messages.value.push({
        role: 'assistant',
        type: 'sql',
        sql: data.sql || data.formatted_sql,
        references: data.references,
        responseTime: data.response_time
      })
    } else {
      messages.value.push({
        role: 'assistant',
        type: 'text',
        content: data.message || data.response || '收到回复'
      })
    }

    // 滚动到底部
    await nextTick()
    scrollToBottom()

  } catch (err: any) {
    console.error('Chat error:', err)
    
    // 移除加载状态
    messages.value = messages.value.filter(m => m.role !== 'loading')

    // 显示错误
    error.value = {
      error_type: 'network_error',
      message: '消息发送失败',
      suggestion: '请检查网络连接或稍后重试'
    }

    message.error('发送失败，请重试')
  } finally {
    isLoading.value = false
  }
}

const handleNewLine = () => {
  userInput.value += '\n'
}

const handleRetry = () => {
  error.value = null
  // 可以重新发送最后一条消息
}

const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// Lifecycle
onMounted(() => {
  // TODO: 从 API 或 Store 获取当前文件信息
  // 这里暂时模拟
  checkCurrentFile()
})

const checkCurrentFile = async () => {
  try {
    const response = await fetch('/api/files')
    if (response.ok) {
      const data = await response.json()
      const files = data.files || []
      
      // 找到第一个已解析的文件
      const readyFile = files.find((f: any) => f.status === 'ready')
      if (readyFile) {
        currentFile.value = readyFile.filename
      }
    }
  } catch (err) {
    console.error('Failed to check current file:', err)
  }
}
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.chat-header {
  padding: 20px 0;
  border-bottom: 1px solid #e0e0e0;
  margin-bottom: 20px;
}

.chat-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
}

.header-info {
  font-size: 14px;
  color: #666;
}

.current-file {
  color: #1890ff;
  font-weight: 500;
}

.no-file-warning {
  color: #faad14;
  font-weight: 500;
}

.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 0;
  margin-bottom: 20px;
}

.empty-state {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.welcome-message {
  text-align: center;
  max-width: 600px;
  padding: 40px;
  background: #f5f5f5;
  border-radius: 12px;
}

.welcome-message h2 {
  margin-bottom: 16px;
  color: #262626;
}

.welcome-message p {
  margin-bottom: 12px;
  color: #595959;
}

.welcome-message ul {
  text-align: left;
  margin: 20px 0;
  padding-left: 40px;
}

.welcome-message li {
  margin: 8px 0;
  color: #595959;
}

.message {
  margin-bottom: 20px;
}

.message.user {
  display: flex;
  justify-content: flex-end;
}

.message.assistant {
  display: flex;
  justify-content: flex-start;
}

.user-message {
  background: #e6f7ff;
  color: #262626;
  padding: 12px 16px;
  border-radius: 12px 12px 0 12px;
  max-width: 70%;
  word-wrap: break-word;
}

.assistant-message {
  background: #fafafa;
  padding: 16px;
  border-radius: 12px 12px 12px 0;
  max-width: 85%;
}

.text-response {
  color: #262626;
  line-height: 1.6;
}

.chat-input-area {
  border-top: 1px solid #e0e0e0;
  padding-top: 20px;
}

.input-wrapper {
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.chat-input {
  flex: 1;
}

.send-button {
  height: 40px;
  padding: 0 24px;
}
</style>
