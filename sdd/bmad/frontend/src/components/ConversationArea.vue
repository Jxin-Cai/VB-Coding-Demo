<!--
  对话区组件
  ChatGPT 风格的对话界面
-->
<template>
  <div class="conversation-area">
    <!-- 空状态 -->
    <div v-if="messages.length === 0" class="empty-state">
      <div class="empty-icon">💬</div>
      <h3>开始你的第一次查询</h3>
      <p>在下方输入框中描述你想查询的数据...</p>
      
      <!-- 示例查询 -->
      <div class="example-queries">
        <p class="example-title">试试这些示例：</p>
        <a-button
          v-for="example in exampleQueries"
          :key="example"
          type="dashed"
          @click="$emit('send-example', example)"
        >
          {{ example }}
        </a-button>
      </div>
    </div>

    <!-- 消息列表 -->
    <div v-else class="message-list">
      <div
        v-for="msg in messages"
        :key="msg.id"
        :class="['message-item', `message-${msg.type}`]"
      >
        <!-- 用户消息 -->
        <div v-if="msg.type === 'user'" class="user-message">
          <div class="message-avatar">
            <user-outlined />
          </div>
          <div class="message-content">
            <div class="message-text">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
          </div>
        </div>

        <!-- AI 消息 -->
        <div v-else-if="msg.type === 'assistant'" class="assistant-message">
          <div class="message-avatar ai-avatar">
            <robot-outlined />
          </div>
          <div class="message-content">
            <!-- 意图识别提示 -->
            <div v-if="msg.intent" class="intent-badge">
              <span v-if="msg.intent === 'new'">🆕 理解为：生成新的 SQL 查询</span>
              <span v-else>🔄 理解为：优化当前 SQL（版本 {{ msg.version }}）</span>
            </div>

            <!-- Agent 工作步骤 -->
            <div v-if="msg.steps" class="agent-steps">
              <div v-for="(step, idx) in msg.steps" :key="idx" class="step-item">
                <a-spin v-if="step.loading" size="small" />
                <check-circle-outlined v-else style="color: #52c41a" />
                <span>{{ step.text }}</span>
              </div>
            </div>

            <!-- SQL 代码块 -->
            <SQLCodeBlock
              v-if="msg.sql"
              :sql="msg.sql"
              :references="msg.references"
              @copy="$emit('copy-sql', msg.sql)"
              @view-ddl="$emit('view-ddl', $event)"
              @regenerate="$emit('regenerate', msg.id)"
            />

            <!-- 普通文本消息 -->
            <div v-else-if="msg.content" class="message-text">
              {{ msg.content }}
            </div>
            
            <!-- 空消息占位符（避免显示空白）-->
            <div v-else class="message-text message-empty">
              <span style="opacity: 0.5;">（收到回复）</span>
            </div>

            <div class="message-time">{{ formatTime(msg.timestamp) }}</div>
          </div>
        </div>
      </div>

      <!-- Loading 状态 -->
      <div v-if="loading" class="message-item message-assistant">
        <div class="message-avatar ai-avatar">
          <robot-outlined />
        </div>
        <div class="message-content">
          <div class="agent-working">
            <a-spin />
            <span>AI 正在思考...</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import {
  UserOutlined,
  RobotOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons-vue'
import SQLCodeBlock from './SQLCodeBlock.vue'
import { tokens } from '../theme/theme.config'

// Props
interface Message {
  id: number
  type: 'user' | 'assistant'
  content: string
  sql?: string
  references?: string[]
  intent?: 'new' | 'optimize'
  version?: number
  steps?: Array<{ text: string; loading: boolean }>
  timestamp: Date
}

defineProps<{
  messages: Message[]
  loading?: boolean
}>()

// Emits
defineEmits(['send-example', 'copy-sql', 'view-ddl', 'regenerate'])

// Data
const exampleQueries = ref([
  '查看昨天的订单量',
  '查看最近 30 天的新用户',
  '统计各地区的销售额',
])

/**
 * 格式化时间
 */
const formatTime = (date: Date) => {
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.conversation-area {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 空状态 */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  text-align: center;
  padding: 48px 24px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 24px;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 500;
  color: v-bind('tokens.text.primary');
  margin-bottom: 8px;
}

.empty-state p {
  font-size: 14px;
  color: v-bind('tokens.text.secondary');
  margin-bottom: 32px;
}

.example-queries {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-width: 400px;
  width: 100%;
}

.example-title {
  font-size: 12px;
  color: v-bind('tokens.text.tertiary');
  margin-bottom: 8px;
}

/* 消息列表 */
.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px 0;
}

.message-item {
  margin-bottom: 32px;
}

/* 用户消息 */
.user-message {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.user-message .message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: v-bind('tokens.colors.primary');
  color: v-bind('tokens.text.primary');
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
  order: 2;
}

.user-message .message-content {
  max-width: 70%;
  order: 1;
}

.user-message .message-text {
  background: v-bind('tokens.backgrounds.elevated');
  color: v-bind('tokens.text.primary');
  padding: 12px 16px;
  border-radius: 12px;
  border-bottom-right-radius: 4px;
  line-height: 1.6;
}

/* AI 消息 */
.assistant-message {
  display: flex;
  gap: 12px;
}

.ai-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.assistant-message .message-content {
  flex: 1;
  max-width: 80%;
}

.assistant-message .message-text {
  background: v-bind('tokens.backgrounds.container');
  color: v-bind('tokens.text.primary');
  padding: 12px 16px;
  border-radius: 12px;
  border-bottom-left-radius: 4px;
  line-height: 1.6;
  white-space: pre-wrap; /* 保留换行符 */
  word-wrap: break-word; /* 自动换行 */
}

.message-empty {
  font-style: italic;
  opacity: 0.6;
}

/* 意图识别提示 */
.intent-badge {
  display: inline-block;
  background: v-bind('tokens.colors.info') + '20';
  color: v-bind('tokens.colors.info');
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 12px;
  margin-bottom: 12px;
}

/* Agent 工作步骤 */
.agent-steps {
  background: v-bind('tokens.backgrounds.container');
  border: 1px solid v-bind('tokens.border.primary');
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 12px;
}

.step-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 0;
  color: v-bind('tokens.text.secondary');
  font-size: 13px;
}

/* Agent 工作中 */
.agent-working {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: v-bind('tokens.backgrounds.container');
  border-radius: 8px;
  color: v-bind('tokens.text.secondary');
}

/* 消息时间 */
.message-time {
  font-size: 11px;
  color: v-bind('tokens.text.tertiary');
  margin-top: 6px;
  text-align: right;
}

.assistant-message .message-time {
  text-align: left;
}
</style>
