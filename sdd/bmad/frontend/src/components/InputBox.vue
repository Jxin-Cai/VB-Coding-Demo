<!--
  输入框组件
  底部固定输入框，支持多行输入
-->
<template>
  <div class="input-box">
    <a-textarea
      v-model:value="inputValue"
      :placeholder="placeholder"
      :disabled="loading"
      :auto-size="{ minRows: 1, maxRows: 4 }"
      class="input-textarea"
      @keydown.enter="handleKeyDown"
    />
    
    <div class="input-actions">
      <!-- 停止按钮（生成中）-->
      <a-button
        v-if="loading"
        type="primary"
        danger
        @click="$emit('stop')"
      >
        <stop-outlined />
        停止生成
      </a-button>
      
      <!-- 发送按钮 -->
      <a-button
        v-else
        type="primary"
        :disabled="!inputValue.trim()"
        @click="handleSend"
      >
        <send-outlined />
        发送
      </a-button>
    </div>

    <!-- 输入提示 -->
    <div class="input-hint">
      <span class="hint-text">Enter 发送 · Shift+Enter 换行</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { SendOutlined, StopOutlined } from '@ant-design/icons-vue'
import { tokens } from '../theme/theme.config'

// Props
const props = defineProps<{
  modelValue: string
  placeholder?: string
  loading?: boolean
}>()

// Emits
const emit = defineEmits(['update:modelValue', 'send', 'stop'])

// State
const inputValue = ref(props.modelValue)

/**
 * 监听 v-model 变化
 */
watch(() => props.modelValue, (newVal) => {
  inputValue.value = newVal
})

watch(inputValue, (newVal) => {
  emit('update:modelValue', newVal)
})

/**
 * 键盘事件处理
 */
const handleKeyDown = (e: KeyboardEvent) => {
  // Enter 发送，Shift+Enter 换行
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

/**
 * 发送消息
 */
const handleSend = () => {
  const text = inputValue.value.trim()
  if (!text || props.loading) return

  emit('send', text)
  inputValue.value = '' // 清空输入框
}
</script>

<style scoped>
.input-box {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  position: relative;
}

.input-textarea {
  flex: 1;
  background: v-bind('tokens.backgrounds.elevated');
  border-color: v-bind('tokens.border.primary');
  color: v-bind('tokens.text.primary');
  font-size: 14px;
  resize: none;
  transition: all 0.3s;
}

.input-textarea:focus {
  border-color: v-bind('tokens.colors.primary');
  box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1);
}

.input-textarea:disabled {
  background: v-bind('tokens.backgrounds.container');
  color: v-bind('tokens.text.tertiary');
  cursor: not-allowed;
}

.input-actions {
  display: flex;
  gap: 8px;
}

/* 输入提示 */
.input-hint {
  position: absolute;
  bottom: -20px;
  left: 0;
  font-size: 11px;
  color: v-bind('tokens.text.tertiary');
}

/* 按钮样式覆盖 */
:deep(.ant-btn-primary) {
  background: v-bind('tokens.colors.primary');
  border-color: v-bind('tokens.colors.primary');
}

:deep(.ant-btn-primary:hover) {
  background: #333333;
  border-color: #333333;
}

:deep(.ant-btn-primary[disabled]) {
  background: v-bind('tokens.backgrounds.elevated');
  border-color: v-bind('tokens.border.primary');
  color: v-bind('tokens.text.tertiary');
}

:deep(.ant-textarea) {
  background: v-bind('tokens.backgrounds.elevated');
  border-color: v-bind('tokens.border.primary');
  color: v-bind('tokens.text.primary');
}

:deep(.ant-textarea::placeholder) {
  color: v-bind('tokens.text.tertiary');
}

:deep(.ant-textarea:hover) {
  border-color: v-bind('tokens.border.primary');
}

:deep(.ant-textarea:focus) {
  border-color: v-bind('tokens.colors.primary');
  box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1);
}
</style>
