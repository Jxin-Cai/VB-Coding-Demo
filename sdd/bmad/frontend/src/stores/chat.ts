/**
 * 对话管理 Store
 * 管理对话历史和消息状态
 */
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export interface Message {
  id: string
  role: 'user' | 'assistant'
  content: string
  type: 'text' | 'sql'  // 消息类型
  timestamp: string
  sql?: string  // 如果是 SQL 类型
  explanation?: string  // SQL 解释
  references?: any[]  // 引用来源
}

export const useChatStore = defineStore('chat', () => {
  // State
  const messages = ref<Message[]>([])
  const loading = ref(false)
  const currentInput = ref('')

  // Computed
  const messageCount = computed(() => messages.value.length)

  // Actions

  /**
   * 添加用户消息
   */
  function addUserMessage(content: string): Message {
    const message: Message = {
      id: `user-${Date.now()}`,
      role: 'user',
      content,
      type: 'text',
      timestamp: new Date().toISOString()
    }
    messages.value.push(message)
    return message
  }

  /**
   * 添加 AI 响应消息
   */
  function addAssistantMessage(data: {
    content: string
    type: 'text' | 'sql'
    sql?: string
    explanation?: string
    references?: any[]
  }): Message {
    const message: Message = {
      id: `assistant-${Date.now()}`,
      role: 'assistant',
      content: data.content,
      type: data.type,
      timestamp: new Date().toISOString(),
      sql: data.sql,
      explanation: data.explanation,
      references: data.references
    }
    messages.value.push(message)
    return message
  }

  /**
   * 清空对话历史
   */
  function clearMessages() {
    messages.value = []
  }

  /**
   * 设置加载状态
   */
  function setLoading(value: boolean) {
    loading.value = value
  }

  return {
    // State
    messages,
    loading,
    currentInput,
    
    // Computed
    messageCount,
    
    // Actions
    addUserMessage,
    addAssistantMessage,
    clearMessages,
    setLoading
  }
})
