<!--
  SQL 历史列表组件
  左侧侧边栏，类似 ChatGPT
-->
<template>
  <div class="sql-history-list">
    <!-- 空状态 -->
    <div v-if="history.length === 0" class="empty-history">
      <history-outlined :style="{ fontSize: '32px', color: tokens.text.tertiary }" />
      <p>暂无历史记录</p>
    </div>

    <!-- 历史列表 -->
    <div v-else class="history-items">
      <div
        v-for="item in history"
        :key="item.id"
        :class="['history-item', { active: item.id === currentId }]"
        @click="$emit('select', item.id)"
      >
        <div class="history-content">
          <div class="history-title">{{ item.query || 'SQL 查询' }}</div>
          <div class="history-meta">
            <a-tag
              v-if="item.status === 'success'"
              color="success"
              size="small"
            >
              <check-circle-outlined />
              成功
            </a-tag>
            <a-tag
              v-else-if="item.status === 'error'"
              color="error"
              size="small"
            >
              <close-circle-outlined />
              失败
            </a-tag>
            <a-tag v-else color="processing" size="small">
              <sync-outlined :spin="true" />
              进行中
            </a-tag>
            <span class="history-time">{{ formatTime(item.timestamp) }}</span>
          </div>
        </div>
        
        <!-- 删除按钮 -->
        <a-button
          type="text"
          size="small"
          class="delete-btn"
          @click.stop="$emit('delete', item.id)"
        >
          <delete-outlined />
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  HistoryOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  SyncOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue'
import { tokens } from '../theme/theme.config'

// Props
interface HistoryItem {
  id: string
  query: string
  status: 'success' | 'error' | 'pending'
  timestamp: Date
}

defineProps<{
  history: HistoryItem[]
  currentId?: string | null
}>()

// Emits
defineEmits(['select', 'delete'])

/**
 * 格式化时间
 */
const formatTime = (date: Date) => {
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  
  return date.toLocaleDateString('zh-CN', { month: 'short', day: 'numeric' })
}
</script>

<style scoped>
.sql-history-list {
  height: calc(100vh - 120px);
  overflow-y: auto;
  padding: 8px;
}

/* 空状态 */
.empty-history {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
  color: v-bind('tokens.text.tertiary');
}

.empty-history p {
  margin-top: 12px;
  font-size: 13px;
}

/* 历史项 */
.history-items {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.history-item {
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  background: transparent;
  border: 1px solid transparent;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  position: relative;
}

.history-item:hover {
  background: v-bind('tokens.backgrounds.elevated');
  border-color: v-bind('tokens.border.secondary');
}

.history-item.active {
  background: v-bind('tokens.backgrounds.elevated');
  border-color: v-bind('tokens.colors.primary');
}

.history-content {
  flex: 1;
  min-width: 0;
}

.history-title {
  font-size: 13px;
  color: v-bind('tokens.text.primary');
  font-weight: 500;
  margin-bottom: 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.history-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 11px;
}

.history-time {
  color: v-bind('tokens.text.tertiary');
}

/* 删除按钮 */
.delete-btn {
  opacity: 0;
  transition: opacity 0.2s;
  color: v-bind('tokens.text.tertiary');
  flex-shrink: 0;
}

.history-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  color: v-bind('tokens.colors.error');
}

/* 滚动条样式 */
.sql-history-list::-webkit-scrollbar {
  width: 6px;
}

.sql-history-list::-webkit-scrollbar-track {
  background: transparent;
}

.sql-history-list::-webkit-scrollbar-thumb {
  background: v-bind('tokens.border.secondary');
  border-radius: 3px;
}

.sql-history-list::-webkit-scrollbar-thumb:hover {
  background: v-bind('tokens.border.primary');
}
</style>
