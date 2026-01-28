<template>
  <div class="file-list-container">
    <!-- 搜索和筛选 -->
    <div class="list-toolbar">
      <a-input-search
        v-model:value="searchText"
        placeholder="搜索文件名..."
        style="width: 300px"
        @search="handleSearch"
      />
      <a-select
        v-model:value="statusFilter"
        style="width: 150px; margin-left: 12px"
        @change="handleFilterChange"
      >
        <a-select-option value="all">全部状态</a-select-option>
        <a-select-option value="pending">待解析</a-select-option>
        <a-select-option value="parsing">解析中</a-select-option>
        <a-select-option value="ready">已解析</a-select-option>
        <a-select-option value="error">解析失败</a-select-option>
      </a-select>
    </div>

    <!-- 文件列表 -->
    <a-list
      :data-source="filteredFiles"
      :loading="loading"
      bordered
      class="file-list"
    >
      <template #renderItem="{ item, index }">
        <a-list-item>
          <template #actions>
            <a-button
              v-if="item.status === 'ready'"
              type="primary"
              size="small"
              :disabled="item.file_id === currentFileId"
              @click="setAsCurrent(item.file_id)"
            >
              {{ item.file_id === currentFileId ? '当前文件' : '设为当前' }}
            </a-button>
            <a-button
              danger
              size="small"
              @click="handleDelete(item.file_id, item.filename)"
            >
              删除
            </a-button>
          </template>
          <a-list-item-meta>
            <template #avatar>
              <a-badge :status="getStatusBadge(item.status)" />
            </template>
            <template #title>
              <a @click="toggleDetails(item.file_id)" class="file-name">
                {{ item.filename }}
                <loading-outlined v-if="item.status === 'parsing'" spin />
              </a>
            </template>
            <template #description>
              <div class="file-meta">
                <span>文件 ID: {{ item.file_id.substring(0, 8) }}...</span>
                <a-divider type="vertical" />
                <span>上传时间: {{ formatDate(item.uploaded_at) }}</span>
                <a-divider type="vertical" />
                <span>大小: {{ formatSize(item.file_size) }}</span>
                <a-divider type="vertical" />
                <span>
                  <a-tag :color="getStatusColor(item.status)">
                    {{ getStatusText(item.status) }}
                  </a-tag>
                </span>
                <template v-if="item.status === 'ready'">
                  <a-divider type="vertical" />
                  <span class="success-info">
                    ✓ {{ item.table_count }} 张表，{{ item.column_count }} 个字段
                  </span>
                </template>
                <template v-if="item.status === 'error'">
                  <a-divider type="vertical" />
                  <span class="error-info">
                    ✗ {{ item.error_message }}
                  </span>
                </template>
              </div>
            </template>
          </a-list-item-meta>

          <!-- 文件详情（展开/折叠）-->
          <div v-if="expandedFileId === item.file_id" class="file-details">
            <a-descriptions title="文件详情" bordered size="small" :column="2">
              <a-descriptions-item label="文件 ID">
                {{ item.file_id }}
              </a-descriptions-item>
              <a-descriptions-item label="文件名">
                {{ item.filename }}
              </a-descriptions-item>
              <a-descriptions-item label="上传时间">
                {{ new Date(item.uploaded_at).toLocaleString('zh-CN') }}
              </a-descriptions-item>
              <a-descriptions-item label="文件大小">
                {{ formatSize(item.file_size) }}
              </a-descriptions-item>
              <a-descriptions-item label="状态">
                <a-tag :color="getStatusColor(item.status)">
                  {{ getStatusText(item.status) }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item v-if="item.status === 'ready'" label="解析结果">
                {{ item.table_count }} 张表，{{ item.column_count }} 个字段，
                {{ item.embedding_count }} 个向量
              </a-descriptions-item>
            </a-descriptions>

            <!-- 表列表（如果已解析）-->
            <div v-if="item.status === 'ready' && item.tables" class="tables-section">
              <h4>表列表</h4>
              <a-table
                :columns="tableColumns"
                :data-source="item.tables"
                :pagination="false"
                size="small"
              >
                <template #bodyCell="{ column, record }">
                  <template v-if="column.key === 'name'">
                    <a-tag color="blue">{{ record.name }}</a-tag>
                  </template>
                  <template v-if="column.key === 'primary_keys'">
                    <a-tag v-for="pk in record.primary_keys" :key="pk" color="green">
                      {{ pk }}
                    </a-tag>
                  </template>
                </template>
              </a-table>
            </div>
          </div>
        </a-list-item>
      </template>

      <template #header>
        <div class="list-header">
          <span>已上传文件 ({{ filteredFiles.length }})</span>
          <a-button type="link" size="small" @click="refreshAll">
            <reload-outlined /> 刷新
          </a-button>
        </div>
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { LoadingOutlined, ReloadOutlined } from '@ant-design/icons-vue'
import { Modal, message } from 'ant-design-vue'
import axios from 'axios'
import { tokens } from '../theme/theme.config'

// Props
interface FileItem {
  file_id: string
  filename: string
  status: string
  uploaded_at: string
  file_size: number
  table_count?: number
  column_count?: number
  embedding_count?: number
  error_message?: string
  tables?: any[]
}

const props = defineProps<{
  files: FileItem[]
  currentFileId?: string | null
}>()

const emit = defineEmits(['refresh', 'set-current', 'delete'])

// State
const loading = ref(false)
const searchText = ref('')
const statusFilter = ref('all')
const expandedFileId = ref<string | null>(null)
const pollingInterval = ref<number | null>(null)

// 表列定义
const tableColumns = [
  { title: '表名', dataIndex: 'name', key: 'name' },
  { title: '字段数', dataIndex: 'column_count', key: 'column_count' },
  { title: '主键', dataIndex: 'primary_keys', key: 'primary_keys' },
]

// Computed
const filteredFiles = computed(() => {
  let files = props.files

  // 状态筛选
  if (statusFilter.value !== 'all') {
    files = files.filter(f => f.status === statusFilter.value)
  }

  // 搜索
  if (searchText.value) {
    const search = searchText.value.toLowerCase()
    files = files.filter(f => 
      f.filename.toLowerCase().includes(search) ||
      f.file_id.toLowerCase().includes(search)
    )
  }

  return files
})

// Methods
const getStatusBadge = (status: string) => {
  const badges: Record<string, string> = {
    'pending': 'default',
    'parsing': 'processing',
    'ready': 'success',
    'error': 'error'
  }
  return badges[status] || 'default'
}

const getStatusColor = (status: string) => {
  const colors: Record<string, string> = {
    'pending': 'orange',
    'parsing': 'blue',
    'ready': 'green',
    'error': 'red'
  }
  return colors[status] || 'default'
}

const getStatusText = (status: string) => {
  const texts: Record<string, string> = {
    'pending': '待解析',
    'parsing': '解析中',
    'ready': '已解析',
    'error': '解析失败'
  }
  return texts[status] || status
}

const formatDate = (dateString: string) => {
  return new Date(dateString).toLocaleString('zh-CN')
}

const formatSize = (bytes: number) => {
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(2)} KB`
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
}

const toggleDetails = (fileId: string) => {
  expandedFileId.value = expandedFileId.value === fileId ? null : fileId
}

const handleSearch = () => {
  // 搜索已通过 computed 实现
}

const handleFilterChange = () => {
  // 筛选已通过 computed 实现
}

const refreshAll = () => {
  emit('refresh')
}

const setAsCurrent = (fileId: string) => {
  emit('set-current', fileId)
  message.success('已切换当前文件')
}

const handleDelete = (fileId: string, filename: string) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除文件 "${filename}" 吗？此操作不可恢复。`,
    okText: '删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      emit('delete', fileId)
    }
  })
}

// 实时状态更新（轮询）
const startPolling = () => {
  pollingInterval.value = window.setInterval(async () => {
    // 检查是否有正在解析的文件
    const parsingFiles = props.files.filter(f => f.status === 'parsing')
    
    if (parsingFiles.length > 0) {
      // 刷新这些文件的状态
      emit('refresh')
    }
  }, 1000) // 每秒轮询
}

const stopPolling = () => {
  if (pollingInterval.value) {
    clearInterval(pollingInterval.value)
    pollingInterval.value = null
  }
}

// Lifecycle
onMounted(() => {
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.file-list-container {
  margin-top: 24px;
}

.list-toolbar {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  color: v-bind('tokens.text.primary');
}

.file-name {
  color: v-bind('tokens.colors.info');
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-name:hover {
  text-decoration: underline;
}

.file-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  font-size: 13px;
  color: v-bind('tokens.text.secondary');
}

.success-info {
  color: v-bind('tokens.colors.success');
  font-weight: 500;
}

.error-info {
  color: v-bind('tokens.colors.error');
}

.file-details {
  width: 100%;
  margin-top: 16px;
  padding: 16px;
  background: v-bind('tokens.backgrounds.elevated');
  border: 1px solid v-bind('tokens.border.secondary');
  border-radius: 4px;
}

.tables-section {
  margin-top: 16px;
}

.tables-section h4 {
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 500;
  color: v-bind('tokens.text.primary');
}

/* Ant Design 组件样式覆盖 */
:deep(.ant-list) {
  background: transparent;
  border-color: v-bind('tokens.border.primary');
}

:deep(.ant-list-item) {
  background: v-bind('tokens.backgrounds.container');
  border-bottom-color: v-bind('tokens.border.secondary');
  color: v-bind('tokens.text.primary');
}

:deep(.ant-list-item:hover) {
  background: v-bind('tokens.backgrounds.elevated');
}

:deep(.ant-input-search) {
  background: v-bind('tokens.backgrounds.elevated');
  border-color: v-bind('tokens.border.primary');
}

:deep(.ant-input) {
  background: v-bind('tokens.backgrounds.elevated');
  border-color: v-bind('tokens.border.primary');
  color: v-bind('tokens.text.primary');
}

:deep(.ant-select) {
  background: v-bind('tokens.backgrounds.elevated');
}

:deep(.ant-select-selector) {
  background: v-bind('tokens.backgrounds.elevated') !important;
  border-color: v-bind('tokens.border.primary') !important;
  color: v-bind('tokens.text.primary') !important;
}

:deep(.ant-descriptions) {
  background: transparent;
}

:deep(.ant-descriptions-item-label) {
  background: v-bind('tokens.backgrounds.base');
  color: v-bind('tokens.text.secondary');
}

:deep(.ant-descriptions-item-content) {
  background: v-bind('tokens.backgrounds.container');
  color: v-bind('tokens.text.primary');
}

:deep(.ant-table) {
  background: transparent;
  color: v-bind('tokens.text.primary');
}

:deep(.ant-table-thead > tr > th) {
  background: v-bind('tokens.backgrounds.base');
  color: v-bind('tokens.text.primary');
  border-bottom-color: v-bind('tokens.border.primary');
}

:deep(.ant-table-tbody > tr > td) {
  background: v-bind('tokens.backgrounds.container');
  color: v-bind('tokens.text.primary');
  border-bottom-color: v-bind('tokens.border.secondary');
}

:deep(.ant-table-tbody > tr:hover > td) {
  background: v-bind('tokens.backgrounds.elevated');
}
</style>
