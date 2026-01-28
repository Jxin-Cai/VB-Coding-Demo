<template>
  <div class="files-container">
    <div class="files-header">
      <h1>📁 DDL 文件管理</h1>
      <a-button type="primary" @click="showUploadModal">
        ⬆️ 上传 DDL 文件
      </a-button>
    </div>

    <div class="files-content">
      <div v-if="files.length === 0" class="empty-files">
        <div class="empty-state">
          <h2>📂 还没有上传 DDL 文件</h2>
          <p>上传数据库 DDL 文件，开始生成 SQL</p>
          <a-button type="primary" size="large" @click="showUploadModal">
            ⬆️ 立即上传
          </a-button>
        </div>
      </div>

      <a-list v-else :data-source="files" class="files-list">
        <template #renderItem="{ item }">
          <a-list-item>
            <a-list-item-meta>
              <template #title>
                <div class="file-title">
                  <span class="filename">{{ item.filename }}</span>
                  <a-tag :color="getStatusColor(item.status)">
                    {{ getStatusText(item.status) }}
                  </a-tag>
                </div>
              </template>
              <template #description>
                <div class="file-meta">
                  <span>📊 {{ item.table_count || 0 }} 张表</span>
                  <span>📝 {{ item.column_count || 0 }} 个字段</span>
                  <span>📅 {{ formatDate(item.uploaded_at) }}</span>
                  <span>💾 {{ formatSize(item.size) }}</span>
                </div>
                <div v-if="item.error_message" class="error-message">
                  ❌ {{ item.error_message }}
                </div>
              </template>
            </a-list-item-meta>
            <template #actions>
              <a-button
                v-if="item.status === 'ready'"
                type="link"
                @click="goToChat(item.file_id)"
              >
                💬 开始对话
              </a-button>
              <a-button type="link" danger @click="deleteFile(item.file_id)">
                🗑️ 删除
              </a-button>
            </template>
          </a-list-item>
        </template>
      </a-list>
    </div>

    <!-- 上传模态框 -->
    <a-modal
      v-model:open="uploadModalVisible"
      title="上传 DDL 文件"
      :footer="null"
      width="600px"
    >
      <a-upload-dragger
        name="file"
        :multiple="false"
        :before-upload="beforeUpload"
        :custom-request="handleUpload"
        accept=".sql"
      >
        <p class="ant-upload-drag-icon">
          📤
        </p>
        <p class="ant-upload-text">点击或拖拽文件到此区域上传</p>
        <p class="ant-upload-hint">
          支持 .sql 格式的 DDL 文件，最大 10MB
        </p>
      </a-upload-dragger>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import SQLCodeBlock from '../components/SQLCodeBlock.vue'
import References from '../components/References.vue'
import LoadingAnimation from '../components/LoadingAnimation.vue'
import ErrorDisplay from '../components/ErrorDisplay.vue'

const router = useRouter()

// State
const files = ref<any[]>([])
const uploadModalVisible = ref(false)

// Methods
const showUploadModal = () => {
  uploadModalVisible.value = true
}

const beforeUpload = (file: File) => {
  const isSql = file.name.endsWith('.sql')
  if (!isSql) {
    message.error('仅支持 .sql 格式的 DDL 文件')
    return false
  }

  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    message.error('文件大小不能超过 10MB')
    return false
  }

  return true
}

const handleUpload = async (options: any) => {
  const { file, onSuccess, onError } = options
  
  const formData = new FormData()
  formData.append('file', file)

  try {
    const response = await fetch('/api/files/upload', {
      method: 'POST',
      body: formData
    })

    if (!response.ok) {
      throw new Error(`上传失败: ${response.status}`)
    }

    const data = await response.json()
    
    message.success('✓ 文件上传成功，正在解析...')
    onSuccess(data)
    
    uploadModalVisible.value = false
    
    // 刷新文件列表
    await loadFiles()
    
    // 轮询解析状态
    if (data.file_id) {
      pollFileStatus(data.file_id)
    }
    
  } catch (err: any) {
    console.error('Upload error:', err)
    message.error('上传失败，请重试')
    onError(err)
  }
}

const loadFiles = async () => {
  try {
    const response = await fetch('/api/files')
    if (response.ok) {
      const data = await response.json()
      files.value = data.files || []
    }
  } catch (err) {
    console.error('Failed to load files:', err)
    message.error('加载文件列表失败')
  }
}

const pollFileStatus = async (fileId: string) => {
  let attempts = 0
  const maxAttempts = 20 // 最多轮询 20 次（约 20 秒）

  const poll = setInterval(async () => {
    attempts++

    try {
      const response = await fetch(`/api/files/${fileId}`)
      if (response.ok) {
        const data = await response.json()
        
        // 更新文件列表中的对应文件
        const index = files.value.findIndex(f => f.file_id === fileId)
        if (index !== -1) {
          files.value[index] = data
        }

        // 如果解析完成或失败，停止轮询
        if (data.status === 'ready' || data.status === 'error') {
          clearInterval(poll)
          
          if (data.status === 'ready') {
            message.success(`✓ 解析完成：${data.table_count} 张表，${data.column_count} 个字段`)
          } else {
            message.error('解析失败，请检查文件格式')
          }
        }
      }
    } catch (err) {
      console.error('Polling error:', err)
    }

    // 超时停止
    if (attempts >= maxAttempts) {
      clearInterval(poll)
      message.warning('解析时间较长，请稍后刷新查看结果')
    }
  }, 1000) // 每秒轮询一次
}

const deleteFile = async (fileId: string) => {
  try {
    const response = await fetch(`/api/files/${fileId}`, {
      method: 'DELETE'
    })

    if (response.ok) {
      message.success('✓ 文件已删除')
      await loadFiles()
    } else {
      message.error('删除失败')
    }
  } catch (err) {
    console.error('Delete error:', err)
    message.error('删除失败，请重试')
  }
}

const goToChat = (fileId: string) => {
  router.push('/chat')
}

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    'ready': 'success',
    'parsing': 'processing',
    'error': 'error',
    'pending': 'default'
  }
  return colors[status] || 'default'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'ready': '✅ 已解析',
    'parsing': '⏳ 解析中',
    'error': '❌ 失败',
    'pending': '⏸️ 待解析'
  }
  return texts[status] || status
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatSize = (bytes: number) => {
  if (!bytes) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return `${(bytes / Math.pow(k, i)).toFixed(2)} ${sizes[i]}`
}

// Lifecycle
onMounted(() => {
  loadFiles()
})
</script>

<style scoped>
.files-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.files-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 2px solid #e0e0e0;
}

.files-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
}

.empty-files {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 400px;
}

.empty-state {
  text-align: center;
  padding: 60px;
  background: #fafafa;
  border-radius: 12px;
  border: 2px dashed #d9d9d9;
}

.empty-state h2 {
  margin-bottom: 16px;
  color: #262626;
}

.empty-state p {
  margin-bottom: 24px;
  color: #595959;
  font-size: 16px;
}

.files-list {
  background: white;
  border-radius: 8px;
  padding: 16px;
}

.file-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.filename {
  font-weight: 500;
  font-size: 16px;
}

.file-meta {
  display: flex;
  gap: 16px;
  margin-top: 8px;
  font-size: 14px;
  color: #8c8c8c;
}

.error-message {
  margin-top: 8px;
  color: #f5222d;
  font-size: 14px;
}
</style>
