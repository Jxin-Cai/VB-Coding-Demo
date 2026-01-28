<!--
  RAG Text-to-SQL 主布局
  Split-Panel SQL Workshop: 左右 50-50 分屏
  基于 UX Design Specification - Direction 3
-->
<template>
  <a-layout class="main-layout">
    <!-- 左侧面板：对话与历史（50%）-->
    <a-layout-content class="left-panel">
      <!-- 顶部主Tab栏：对话 / 文件管理 -->
      <div class="main-tabs-wrapper">
        <a-tabs
          v-model:activeKey="activeMainTab"
          type="card"
          size="default"
          :tab-bar-style="{ 
            background: tokens.backgrounds.container, 
            margin: 0,
            borderBottom: `1px solid ${tokens.border.primary}`
          }"
        >
          <!-- 对话Tab -->
          <a-tab-pane key="chat" tab="💬 对话">
            <!-- 会话历史子标签栏 -->
            <div class="session-tabs-wrapper">
              <a-tabs
                v-model:activeKey="activeSessionId"
                type="editable-card"
                size="small"
                hide-add
                :tab-bar-style="{ 
                  background: tokens.backgrounds.base, 
                  margin: 0,
                  padding: '8px 16px'
                }"
                @change="handleSessionChange"
                @edit="handleEditSession"
              >
                <a-tab-pane
                  v-for="session in sessions"
                  :key="session.id"
                  :tab="formatSessionTab(session)"
                />
                <template #rightExtra>
                  <a-button type="text" size="small" @click="handleNewSession">
                    <plus-outlined />
                    新对话
                  </a-button>
                </template>
              </a-tabs>
            </div>

            <!-- 对话区域 -->
            <div class="conversation-wrapper">
              <ConversationArea
                :messages="currentMessages"
                :loading="agentWorking"
                @send-example="handleSendExample"
                @regenerate="handleRegenerate"
                @copy-sql="handleCopySQL"
                @view-ddl="handleViewDDL"
              />
            </div>

            <!-- 底部输入框 -->
            <div class="input-wrapper">
              <InputBox
                v-model="userInput"
                :loading="agentWorking"
                :placeholder="inputPlaceholder"
                @send="handleSendMessage"
                @stop="handleStop"
              />
            </div>
          </a-tab-pane>

          <!-- 文件管理Tab -->
          <a-tab-pane key="files" tab="📁 文件管理">
            <div class="file-management-area">
              <FileList
                :files="uploadedFiles"
                :current-file-id="currentFile?.file_id"
                @select-file="handleSelectFile"
                @delete-file="handleDeleteFile"
                @view-ddl="handleViewFileDDL"
              />
              <div class="file-upload-section">
                <FileUpload @upload-success="handleFileUploadSuccess" />
              </div>
            </div>
          </a-tab-pane>

          <!-- 右上角设置按钮 -->
          <template #rightExtra>
            <a-button type="text" @click="showSettingsDrawer = true">
              <setting-outlined />
              设置
            </a-button>
          </template>
        </a-tabs>
      </div>
    </a-layout-content>

    <!-- 右侧面板：SQL 预览与引用源（50%）-->
    <a-layout-content class="right-panel">
      <!-- SQL 预览区（始终显示）-->
      <div class="sql-preview-area">
        <!-- 顶部标题栏 -->
        <div class="sql-header">
          <div class="sql-title">
            <span class="title-icon">📝</span>
            <h3>SQL 预览</h3>
            <a-tag v-if="currentVersion" color="orange">
              版本 {{ currentVersion }}
            </a-tag>
          </div>
          <div class="sql-actions-header">
            <a-tag v-if="currentFile" color="success">
              <check-circle-outlined />
              {{ currentFile.filename }}
            </a-tag>
            <a-tag v-else color="default">
              <info-circle-outlined />
              未选择文件
            </a-tag>
            <a-button type="text" size="small" @click="activeMainTab = 'files'">
              <folder-outlined />
              切换文件
            </a-button>
          </div>
        </div>

        <!-- SQL 代码块（Monaco Editor）-->
        <div class="sql-code-wrapper">
          <SQLCodeBlock
            v-if="currentSQL"
            :sql="currentSQL"
            :references="currentReferences"
            @copy="handleCopySQL"
            @regenerate="handleRegenerate"
            @view-ddl="handleViewDDL"
          />
          <div v-else class="no-sql-placeholder">
            <inbox-outlined :style="{ fontSize: '48px', color: tokens.text.tertiary }" />
            <p>在左侧输入框中描述你的查询需求...</p>
          </div>
        </div>

        <!-- 操作按钮区 -->
        <div v-if="currentSQL" class="action-buttons">
          <a-button type="primary" size="large" @click="handleCopySQL(currentSQL)">
            <copy-outlined />
            复制 SQL
          </a-button>
          <a-button v-if="sqlVersions.length > 1" @click="handleRevert">
            <rollback-outlined />
            回退到版本 {{ currentVersion - 1 }}
          </a-button>
          <a-button @click="handleRegenerate">
            <reload-outlined />
            重新生成
          </a-button>
        </div>

        <!-- 引用源展示区 -->
        <div v-if="currentReferences && currentReferences.length > 0" class="reference-section">
          <div class="reference-header">
            <span class="reference-icon">📋</span>
            <span class="reference-title">引用源</span>
          </div>
          <div class="reference-content">
            <a-tag
              v-for="table in currentReferences"
              :key="table"
              class="reference-tag"
              @click="handleViewDDL(table)"
            >
              {{ table }}
              <info-circle-outlined />
            </a-tag>
          </div>
          <div class="reference-hint">
            点击表名查看完整 DDL 结构
          </div>
        </div>
      </div>
    </a-layout-content>

    <!-- DDL 详情抽屉 -->
    <a-drawer
      v-model:open="showDDLDrawer"
      title="📄 DDL 详情"
      placement="right"
      :width="600"
      :body-style="{ padding: '0', backgroundColor: tokens.backgrounds.base }"
    >
      <DDLPreview
        :ddl-content="selectedDDL"
        :highlighted-fields="highlightedFields"
      />
    </a-drawer>

    <!-- 系统设置 Drawer -->
    <Settings v-model:visible="showSettingsDrawer" />
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  CheckCircleOutlined,
  FolderOutlined,
  InboxOutlined,
  CopyOutlined,
  RollbackOutlined,
  ReloadOutlined,
  InfoCircleOutlined,
  SettingOutlined,
} from '@ant-design/icons-vue'
import { useFileStore } from '../stores/file'
import { tokens } from '../theme/theme.config'

// 组件导入
import ConversationArea from '../components/ConversationArea.vue'
import InputBox from '../components/InputBox.vue'
import SQLCodeBlock from '../components/SQLCodeBlock.vue'
import EmptyStateGuide from '../components/EmptyStateGuide.vue'
import DDLPreview from '../components/DDLPreview.vue'
import FileUpload from '../components/FileUpload.vue'
import FileList from '../components/FileList.vue'
import Settings from '../components/Settings.vue'

// API Service
import { chatService, type ChatResponse } from '../services/api'

// Store
const fileStore = useFileStore()

// State
const activeMainTab = ref('chat') // 主Tab：chat / files
const showDDLDrawer = ref(false)
const showSettingsDrawer = ref(false)
const userInput = ref('')
const agentWorking = ref(false)

// 会话管理
interface Session {
  id: string
  title: string
  timestamp: Date
  messages: any[]
}

const sessions = ref<Session[]>([
  {
    id: 'session-1',
    title: '新会话',
    timestamp: new Date(),
    messages: [],
  }
])
const activeSessionId = ref('session-1')

// SQL 状态
const currentSQL = ref('')
const currentReferences = ref<string[]>([])
const currentVersion = ref(1)
const sqlVersions = ref<any[]>([])
const selectedDDL = ref('')
const highlightedFields = ref<string[]>([])

// Computed
const currentFile = computed(() => fileStore.currentFile)
const uploadedFiles = computed(() => fileStore.files)
const currentSession = computed(() => {
  return sessions.value.find(s => s.id === activeSessionId.value)
})
const currentMessages = computed(() => {
  return currentSession.value?.messages || []
})
const inputPlaceholder = computed(() => {
  if (currentMessages.value.length === 0) {
    if (!currentFile.value) {
      return '你好，我是SQL助手。可以问我任何问题，或上传DDL后生成SQL查询...'
    }
    return '描述你想查询的数据...'
  }
  return '继续对话...'
})

/**
 * 格式化会话标签
 */
const formatSessionTab = (session: Session) => {
  const timeAgo = formatTimeAgo(session.timestamp)
  return `${session.title} (${timeAgo})`
}

/**
 * 格式化时间
 */
const formatTimeAgo = (date: Date) => {
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  return `${Math.floor(diff / 3600000)} 小时前`
}

/**
 * 发送消息（集成真实后端API）
 * 支持普通对话 + SQL生成混合模式
 */
const handleSendMessage = async (text: string) => {
  // 检查GLM API Key配置
  const apiKey = localStorage.getItem('glm_api_key')
  if (!apiKey || apiKey === 'test_api_key_placeholder') {
    message.warning('请先在设置中配置 GLM API Key')
    showSettingsDrawer.value = true
    return
  }

  // 添加用户消息到当前会话
  const session = currentSession.value
  if (!session) return

  session.messages.push({
    id: `user-${Date.now()}`,
    type: 'user',
    content: text,
    timestamp: new Date(),
  })

  // 调用后端 API（支持普通对话和SQL生成）
  agentWorking.value = true
  userInput.value = '' // 清空输入框
  
  try {
    const response: ChatResponse = await chatService.sendMessage({
      message: text,
      file_id: currentFile.value?.file_id, // 可选：没有文件时也允许对话
    })
    
    // 添加Assistant响应到会话
    session.messages.push({
      id: `assistant-${Date.now()}`,
      type: 'assistant',
      content: response.content || '收到回复', // 确保有内容
      sql: response.sql,
      explanation: response.explanation,
      references: response.references,
      intent: response.intent,
      timestamp: new Date(),
    })
    
    // 如果是SQL生成（后端意图识别为sql_generation），更新右侧预览区
    if (response.type === 'sql' && response.sql) {
      currentSQL.value = response.sql
      currentReferences.value = response.references?.map(ref => ref.table) || []
      currentVersion.value++
      sqlVersions.value.push({ 
        sql: response.sql, 
        version: currentVersion.value,
        references: response.references,
      })
      
      message.success('SQL 已生成')
    } else if (response.type === 'text') {
      // 普通对话：不更新右侧SQL区域，仅在对话区显示
      // 对话区的 ConversationArea 会自动显示 content
      console.log('Received text response:', response.content)
    }
  } catch (error: any) {
    console.error('Failed to send message:', error)
    
    // 添加错误消息到会话
    session.messages.push({
      id: `error-${Date.now()}`,
      type: 'assistant',
      content: `抱歉，请求失败：${error.message || '未知错误'}`,
      timestamp: new Date(),
    })
    
    // 如果是API Key问题，提示用户配置
    if (error.response?.status === 401) {
      message.error('API Key 验证失败，请在设置中更新')
      showSettingsDrawer.value = true
    }
  } finally {
    agentWorking.value = false
  }
}

/**
 * 发送示例查询
 */
const handleSendExample = (example: string) => {
  userInput.value = example
  handleSendMessage(example)
}

/**
 * 停止生成
 */
const handleStop = () => {
  agentWorking.value = false
}

/**
 * 重新生成
 */
const handleRegenerate = () => {
  message.info('重新生成 SQL...')
}

/**
 * 复制 SQL
 */
const handleCopySQL = (sql: string) => {
  navigator.clipboard.writeText(sql)
  message.success('✅ 已复制到剪贴板')
}

/**
 * 查看 DDL
 */
const handleViewDDL = (tableName: string) => {
  showDDLDrawer.value = true
  selectedDDL.value = `-- 表: ${tableName}\nCREATE TABLE ${tableName} (\n  id INT PRIMARY KEY,\n  name VARCHAR(255),\n  created_at TIMESTAMP\n);`
  highlightedFields.value = ['id', 'name', 'created_at']
}

/**
 * 回退版本
 */
const handleRevert = () => {
  if (currentVersion.value > 1) {
    currentVersion.value--
    const prevVersion = sqlVersions.value[currentVersion.value - 1]
    if (prevVersion) {
      currentSQL.value = prevVersion.sql
      message.success(`已回退到版本 ${currentVersion.value}`)
    }
  }
}

/**
 * 切换会话
 */
const handleSessionChange = (sessionId: string) => {
  activeSessionId.value = sessionId
}

/**
 * 编辑会话（处理Tab的关闭事件）
 */
const handleEditSession = (targetKey: string | MouseEvent, action: 'add' | 'remove') => {
  if (action === 'remove') {
    handleCloseSession(targetKey as string)
  }
}

/**
 * 关闭会话
 */
const handleCloseSession = (sessionId: string) => {
  const index = sessions.value.findIndex(s => s.id === sessionId)
  if (index !== -1) {
    sessions.value.splice(index, 1)
    // 切换到另一个会话
    const firstSession = sessions.value[0]
    if (firstSession) {
      activeSessionId.value = firstSession.id
    }
  }
}

/**
 * 新建会话
 */
const handleNewSession = () => {
  const newSession: Session = {
    id: `session-${Date.now()}`,
    title: '新会话',
    timestamp: new Date(),
    messages: [],
  }
  sessions.value.push(newSession)
  activeSessionId.value = newSession.id
}

/**
 * 上传成功
 */
const handleUploadSuccess = (data: any) => {
  fileStore.addFile(data)
  message.success('文件上传成功')
}

/**
 * 文件上传成功（别名）
 */
const handleFileUploadSuccess = handleUploadSuccess

/**
 * 上传错误
 */
const handleUploadError = (error: any) => {
  console.error('Upload error:', error)
}

/**
 * 选择/切换当前文件
 */
const handleSelectFile = (fileId: string) => {
  fileStore.setCurrentFile(fileId)
  message.success('已切换到文件')
  // 切换到对话Tab
  activeMainTab.value = 'chat'
}

/**
 * 设置当前文件（别名）
 */
const handleSetCurrentFile = handleSelectFile

/**
 * 查看文件DDL详情
 */
const handleViewFileDDL = async (fileId: string) => {
  try {
    // 从后端获取DDL内容
    const { fileService } = await import('../services/api')
    const data = await fileService.getFileDDL(fileId)
    selectedDDL.value = data.ddl || data.content || ''
    highlightedFields.value = []
    showDDLDrawer.value = true
  } catch (error) {
    message.error('获取DDL详情失败')
    console.error('Failed to get DDL:', error)
  }
}

/**
 * 删除文件
 */
const handleDeleteFile = async (fileId: string) => {
  const success = await fileStore.deleteFile(fileId)
  if (success) {
    message.success('文件已删除')
  }
}

// Lifecycle
onMounted(() => {
  // 初始化
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
  width: 100vw; /* 确保占满整个视窗 */
  overflow: hidden;
  background: v-bind('tokens.backgrounds.base');
  display: flex;
  flex-direction: row;
  min-width: 1280px; /* PC端最小宽度 */
}

/* 左侧面板（60%）- 对话和文件管理 */
.left-panel {
  width: 60%;
  flex: 0 0 60%; /* 固定60%，不伸缩 */
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: v-bind('tokens.backgrounds.base');
  border-right: 1px solid v-bind('tokens.border.primary');
}

/* 主Tab栏 */
.main-tabs-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.main-tabs-wrapper :deep(.ant-tabs) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.main-tabs-wrapper :deep(.ant-tabs-content-holder) {
  flex: 1;
  overflow: hidden;
}

.main-tabs-wrapper :deep(.ant-tabs-content) {
  height: 100%;
}

.main-tabs-wrapper :deep(.ant-tabs-tabpane) {
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 会话标签栏 */
.session-tabs-wrapper {
  flex-shrink: 0;
  background: v-bind('tokens.backgrounds.base');
  max-height: 48px; /* 限制会话Tab高度 */
}

.session-tabs-wrapper :deep(.ant-tabs-nav) {
  margin-bottom: 0;
}

/* 对话区域 */
.conversation-wrapper {
  flex: 1 1 0; /* 强制占满剩余空间 */
  min-height: 0; /* 允许flex子元素缩小 */
  overflow-y: auto;
  padding: 24px;
  background: v-bind('tokens.backgrounds.base');
}

/* 输入框区域 */
.input-wrapper {
  flex-shrink: 0;
  padding: 16px 24px;
  background: v-bind('tokens.backgrounds.container');
  border-top: 1px solid v-bind('tokens.border.primary');
}

/* 文件管理区域 */
.file-management-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 24px;
  gap: 24px;
  background: v-bind('tokens.backgrounds.base');
}

.file-upload-section {
  flex-shrink: 0;
}

/* 右侧面板（40%）- SQL预览和引用 */
.right-panel {
  width: 40%;
  flex: 0 0 40%; /* 固定40%，不伸缩 */
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: v-bind('tokens.backgrounds.base');
  overflow: hidden;
}

/* 无文件状态 */
.no-file-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
}

/* SQL 预览区 */
.sql-preview-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* SQL 头部 */
.sql-header {
  flex-shrink: 0;
  padding: 16px 24px;
  background: v-bind('tokens.backgrounds.container');
  border-bottom: 1px solid v-bind('tokens.border.primary');
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sql-title {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  font-size: 20px;
}

.sql-title h3 {
  font-size: 16px;
  font-weight: 600;
  color: v-bind('tokens.text.primary');
  margin: 0;
}

.sql-actions-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* SQL 代码区 */
.sql-code-wrapper {
  flex: 1;
  overflow: hidden;
  padding: 16px 24px;
  background: v-bind('tokens.backgrounds.base');
  min-height: 300px; /* 确保有足够高度 */
}

/* 无 SQL 占位符 */
.no-sql-placeholder {
  height: 100%;
  min-height: 300px; /* 确保占位符可见 */
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: v-bind('tokens.text.tertiary');
  background: v-bind('tokens.backgrounds.container'); /* 添加背景色，避免黑色 */
  border-radius: 8px;
}

.no-sql-placeholder p {
  margin-top: 16px;
  font-size: 14px;
  color: v-bind('tokens.text.secondary');
}

/* 操作按钮区 */
.action-buttons {
  flex-shrink: 0;
  padding: 16px 24px;
  background: v-bind('tokens.backgrounds.container');
  border-top: 1px solid v-bind('tokens.border.secondary');
  border-bottom: 1px solid v-bind('tokens.border.secondary');
  display: flex;
  gap: 12px;
}

/* 引用源展示区 */
.reference-section {
  flex-shrink: 0;
  padding: 16px 24px;
  background: rgba(82, 196, 26, 0.1);
  border-left: 3px solid v-bind('tokens.colors.success');
}

.reference-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.reference-icon {
  font-size: 16px;
}

.reference-title {
  font-size: 14px;
  font-weight: 500;
  color: v-bind('tokens.colors.success');
}

.reference-content {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.reference-tag {
  background: v-bind('tokens.highlight.bg');
  color: v-bind('tokens.colors.highlight');
  border: 1px solid v-bind('tokens.highlight.border');
  cursor: pointer;
  font-family: 'Monaco', 'Menlo', 'Courier New', monospace;
  font-size: 13px;
  padding: 6px 12px;
  border-radius: 4px;
  transition: all 0.2s;
}

.reference-tag:hover {
  background: rgba(255, 149, 0, 0.3);
  border-color: v-bind('tokens.colors.highlight');
  transform: translateY(-1px);
}

.reference-hint {
  font-size: 12px;
  color: v-bind('tokens.text.tertiary');
  font-style: italic;
}

/* Tabs 样式覆盖 */
:deep(.ant-tabs-nav) {
  margin: 0 !important;
  background: v-bind('tokens.backgrounds.container');
}

:deep(.ant-tabs-tab) {
  background: transparent !important;
  border-color: v-bind('tokens.border.primary') !important;
  color: v-bind('tokens.text.secondary') !important;
}

:deep(.ant-tabs-tab:hover) {
  color: v-bind('tokens.text.primary') !important;
}

:deep(.ant-tabs-tab-active) {
  background: v-bind('tokens.colors.highlight') + '20' !important;
  color: v-bind('tokens.colors.highlight') !important;
  border-bottom-color: v-bind('tokens.colors.highlight') !important;
}

:deep(.ant-tabs-tab-btn) {
  color: inherit !important;
}

/* 主按钮样式（橙色）*/
:deep(.ant-btn-primary) {
  background: v-bind('tokens.colors.highlight') !important;
  border-color: v-bind('tokens.colors.highlight') !important;
  color: #ffffff !important;
}

:deep(.ant-btn-primary:hover) {
  background: #E68600 !important;
  border-color: #E68600 !important;
}

/* 滚动条 */
.conversation-wrapper::-webkit-scrollbar {
  width: 8px;
}

.conversation-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

.conversation-wrapper::-webkit-scrollbar-thumb {
  background: v-bind('tokens.border.secondary');
  border-radius: 4px;
}

.conversation-wrapper::-webkit-scrollbar-thumb:hover {
  background: v-bind('tokens.border.primary');
}
</style>
