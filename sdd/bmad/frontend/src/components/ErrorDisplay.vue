<template>
  <a-alert
    v-if="error"
    :type="alertType"
    :message="error.message"
    :description="error.suggestion"
    closable
    show-icon
    @close="handleClose"
    class="error-display"
  >
    <template #icon>
      <component :is="iconComponent" />
    </template>
    
    <template v-if="showRetry" #action>
      <a-button size="small" type="primary" @click="handleRetry">
        重试
      </a-button>
    </template>
  </a-alert>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  CloseCircleOutlined,
  WarningOutlined,
  InfoCircleOutlined
} from '@ant-design/icons-vue'

// Props
interface ErrorInfo {
  error_type?: string
  message: string
  suggestion?: string
}

interface Props {
  error: ErrorInfo | null
  showRetry?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  error: null,
  showRetry: false
})

// Emits
const emit = defineEmits<{
  close: []
  retry: []
}>()

// Computed
const alertType = computed(() => {
  if (!props.error?.error_type) return 'error'
  
  const type = props.error.error_type
  
  if (type === 'timeout_error' || type === 'resource_not_ready') {
    return 'warning'
  }
  
  if (type === 'validation_error') {
    return 'info'
  }
  
  return 'error'
})

const iconComponent = computed(() => {
  const type = alertType.value
  
  switch (type) {
    case 'error':
      return CloseCircleOutlined
    case 'warning':
      return WarningOutlined
    case 'info':
      return InfoCircleOutlined
    default:
      return CloseCircleOutlined
  }
})

// Methods
const handleClose = () => {
  emit('close')
}

const handleRetry = () => {
  emit('retry')
}
</script>

<style scoped>
.error-display {
  margin: 16px 0;
  white-space: pre-line;
}
</style>
