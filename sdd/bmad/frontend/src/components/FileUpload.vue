<template>
  <div class="file-upload-container">
    <a-upload-dragger
      v-model:file-list="fileList"
      :before-upload="beforeUpload"
      :custom-request="handleUpload"
      :show-upload-list="false"
      accept=".sql"
      @drop="handleDrop"
    >
      <p class="ant-upload-drag-icon">
        <inbox-outlined :style="{ fontSize: '48px', color: tokens.colors.info }" />
      </p>
      <p class="ant-upload-text">拖拽 .sql 文件到此处，或点击选择文件</p>
      <p class="ant-upload-hint">
        支持 .sql 格式的 DDL 文件，最大 10MB
      </p>
    </a-upload-dragger>

    <!-- 上传进度显示 -->
    <div v-if="uploading" class="upload-progress">
      <a-spin />
      <span class="upload-status">正在上传 {{ currentFileName }}...</span>
      <a-progress :percent="uploadProgress" :show-info="true" />
    </div>

    <!-- 上传成功提示 -->
    <div v-if="uploadSuccess" class="upload-success">
      <check-circle-outlined :style="{ fontSize: '24px', color: '#52c41a' }" />
      <span>✓ 上传成功</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import { InboxOutlined, CheckCircleOutlined } from '@ant-design/icons-vue'
import type { UploadProps } from 'ant-design-vue'
import axios from 'axios'
import { tokens } from '../theme/theme.config'

// Props and Emits
const emit = defineEmits(['upload-success', 'upload-error'])

// State
const fileList = ref([])
const uploading = ref(false)
const uploadSuccess = ref(false)
const uploadProgress = ref(0)
const currentFileName = ref('')

/**
 * 上传前验证
 */
const beforeUpload: UploadProps['beforeUpload'] = (file) => {
  // 验证文件格式
  const isSql = file.name.toLowerCase().endsWith('.sql')
  if (!isSql) {
    message.error('仅支持 .sql 格式的 DDL 文件')
    return false
  }

  // 验证文件大小（< 10MB）
  const maxSize = 10 * 1024 * 1024 // 10MB
  if (file.size > maxSize) {
    message.error(`文件大小超过限制（最大 10MB）\n当前文件：${(file.size / 1024 / 1024).toFixed(2)} MB`)
    return false
  }

  return true
}

/**
 * 自定义上传处理
 */
const handleUpload: UploadProps['customRequest'] = async (options) => {
  const { file, onSuccess, onError, onProgress } = options as any

  uploading.value = true
  uploadSuccess.value = false
  uploadProgress.value = 0
  currentFileName.value = file.name

  try {
    const formData = new FormData()
    formData.append('file', file)

    // 模拟上传进度
    const progressInterval = setInterval(() => {
      if (uploadProgress.value < 90) {
        uploadProgress.value += 10
      }
    }, 100)

    // 发送上传请求
    const response = await axios.post('/api/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        const percent = progressEvent.total
          ? Math.round((progressEvent.loaded * 100) / progressEvent.total)
          : 0
        uploadProgress.value = percent
        onProgress?.({ percent })
      }
    })

    clearInterval(progressInterval)
    uploadProgress.value = 100

    // 显示成功状态
    uploadSuccess.value = true
    message.success('上传成功')

    // 触发成功事件
    emit('upload-success', response.data)
    onSuccess?.(response.data)

    // 2秒后隐藏成功提示
    setTimeout(() => {
      uploading.value = false
      uploadSuccess.value = false
      uploadProgress.value = 0
    }, 2000)

  } catch (error: any) {
    uploading.value = false
    uploadProgress.value = 0

    // 提取错误信息
    const errorMessage = error.response?.data?.detail || '上传失败，请重试'
    message.error(errorMessage)

    // 触发错误事件
    emit('upload-error', error)
    onError?.(error)
  }
}

/**
 * 拖拽处理
 */
const handleDrop = (e: DragEvent) => {
  console.log('File dropped:', e)
}
</script>

<style scoped>
.file-upload-container {
  margin: 20px 0;
}

.upload-progress {
  margin-top: 20px;
  padding: 16px;
  background: v-bind('tokens.backgrounds.elevated');
  border: 1px solid v-bind('tokens.border.primary');
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: center;
}

.upload-status {
  color: v-bind('tokens.colors.info');
  font-weight: 500;
}

.upload-success {
  margin-top: 16px;
  padding: 12px;
  background: rgba(82, 196, 26, 0.1);
  border: 1px solid v-bind('tokens.colors.success');
  border-radius: 4px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: v-bind('tokens.colors.success');
  font-weight: 500;
}

/* Ant Design Upload 组件样式覆盖 */
:deep(.ant-upload-drag) {
  background: v-bind('tokens.backgrounds.elevated') !important;
  border-color: v-bind('tokens.border.primary') !important;
  border-width: 2px !important;
  border-style: dashed !important;
  transition: all 0.3s;
}

:deep(.ant-upload-drag:hover) {
  border-color: v-bind('tokens.colors.primary') !important;
  background: v-bind('tokens.backgrounds.container') !important;
}

:deep(.ant-upload-drag-icon .anticon) {
  color: v-bind('tokens.colors.info') !important;
}

:deep(.ant-upload-text) {
  font-size: 16px;
  color: v-bind('tokens.text.primary') !important;
  margin: 16px 0 8px;
}

:deep(.ant-upload-hint) {
  color: v-bind('tokens.text.secondary') !important;
  font-size: 13px;
}

:deep(.ant-progress-text) {
  color: v-bind('tokens.text.primary');
}
</style>
