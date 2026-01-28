<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { CheckCircleOutlined } from '@ant-design/icons-vue'
import axios from 'axios'
import { useFileStore } from '../stores/file'
import FileUpload from '../components/FileUpload.vue'
import FileList from '../components/FileList.vue'

// Store
const fileStore = useFileStore()

// Computed
const uploadedFiles = computed(() => fileStore.files)
const currentFile = computed(() => fileStore.currentFile)
const loading = computed(() => fileStore.loading)

/**
 * 上传成功处理
 */
const handleUploadSuccess = (data: any) => {
  console.log('Upload success:', data)
  // 添加到 store
  fileStore.addFile(data)
  
  // 开始轮询状态（等待解析完成）
  pollFileStatus(data.file_id)
}

/**
 * 上传错误处理
 */
const handleUploadError = (error: any) => {
  console.error('Upload error:', error)
}

/**
 * 轮询文件状态
 */
const pollFileStatus = async (fileId: string) => {
  const maxAttempts = 10 // 最多轮询 10 次
  let attempts = 0
  
  const interval = setInterval(async () => {
    attempts++
    
    try {
      await fileStore.refreshFile(fileId)
      const file = fileStore.files.find(f => f.file_id === fileId)
      
      // 如果状态为 ready 或 error，停止轮询
      if (file && (file.status === 'ready' || file.status === 'error')) {
        clearInterval(interval)
        
        if (file.status === 'ready') {
          message.success(`文件 ${file.filename} 解析完成`)
        } else {
          message.error(`文件 ${file.filename} 解析失败`)
        }
      }
      
      // 超过最大尝试次数，停止轮询
      if (attempts >= maxAttempts) {
        clearInterval(interval)
      }
    } catch (error) {
      console.error('Poll status error:', error)
      clearInterval(interval)
    }
  }, 1000) // 每秒轮询
}

/**
 * 设置当前文件
 */
const handleSetCurrent = (fileId: string) => {
  fileStore.setCurrentFile(fileId)
}

/**
 * 删除文件
 */
const handleDelete = async (fileId: string) => {
  const success = await fileStore.deleteFile(fileId)
  if (success) {
    message.success('文件已删除')
  } else {
    message.error('删除失败')
  }
}

/**
 * 刷新文件列表
 */
const refreshFiles = async () => {
  await fileStore.refreshAllFiles()
}

// Lifecycle
onMounted(() => {
  // 组件加载时可以加载已有文件列表
  // 当前为内存存储，刷新后数据会丢失
})
</script>

<template>
  <main class="home-container">
    <div class="header">
      <h1>RAG Text-to-SQL</h1>
      <p class="subtitle">上传 DDL 文件，自然语言查询数据库</p>
    </div>

    <!-- 当前上下文提示 -->
    <a-alert
      v-if="currentFile"
      :message="`当前使用：${currentFile.filename}（${currentFile.table_count} 张表）`"
      type="info"
      show-icon
      class="current-file-alert"
    >
      <template #icon>
        <check-circle-outlined />
      </template>
    </a-alert>

    <a-card title="📁 DDL 文件管理" :bordered="false" class="upload-card">
      <FileUpload 
        @upload-success="handleUploadSuccess"
        @upload-error="handleUploadError"
      />

      <!-- 文件列表（增强版）-->
      <FileList 
        v-if="uploadedFiles.length > 0"
        :files="uploadedFiles"
        :current-file-id="fileStore.currentFileId"
        @refresh="refreshFiles"
        @set-current="handleSetCurrent"
        @delete="handleDelete"
      />
    </a-card>
  </main>
</template>

<style scoped>
.home-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.header {
  text-align: center;
  margin-bottom: 32px;
}

.header h1 {
  font-size: 32px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 8px;
}

.subtitle {
  font-size: 16px;
  color: rgba(0, 0, 0, 0.45);
}

.upload-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.uploaded-files {
  margin-top: 24px;
}

.uploaded-files h3 {
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 500;
}

.current-file-alert {
  margin-bottom: 16px;
}
</style>

