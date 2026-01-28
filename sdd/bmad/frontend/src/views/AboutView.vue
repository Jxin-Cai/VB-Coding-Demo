<script setup lang="ts">
import { useFileStore } from '../stores/file'
import ChatInterface from '../components/ChatInterface.vue'

const fileStore = useFileStore()
</script>

<template>
  <div class="chat-view">
    <div class="chat-header">
      <h2>💬 智能对话</h2>
      <p class="subtitle">使用自然语言查询数据库，AI 自动生成 SQL</p>
    </div>

    <!-- 当前 DDL 文件提示 -->
    <a-alert
      v-if="fileStore.currentFile"
      :message="`当前数据库：${fileStore.currentFile.filename}（${fileStore.currentFile.table_count} 张表）`"
      type="success"
      show-icon
      class="current-context-alert"
    />
    <a-alert
      v-else
      message="请先上传 DDL 文件"
      description="在"文件管理"页面上传数据库结构文件后，即可开始对话"
      type="warning"
      show-icon
      class="current-context-alert"
    />

    <!-- 对话界面 -->
    <ChatInterface />
  </div>
</template>

<style scoped>
.chat-view {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.chat-header {
  margin-bottom: 16px;
}

.chat-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 4px;
}

.subtitle {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.45);
}

.current-context-alert {
  margin-bottom: 16px;
}
</style>
