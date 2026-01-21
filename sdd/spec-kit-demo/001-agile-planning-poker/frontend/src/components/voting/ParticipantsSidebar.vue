<template>
  <div class="participants-sidebar">
    <div class="sidebar-header">
      <h3>👥 参与者</h3>
      <span class="participant-count">{{ participants.length }} 人</span>
    </div>

    <div class="participants-list">
      <div 
        v-for="(participant, index) in participants" 
        :key="participant.participantName"
        class="participant-item"
        :style="{ animationDelay: `${index * 100}ms` }"
      >
        <div class="participant-avatar">
          {{ getAvatarText(participant.participantName) }}
          <span 
            class="online-indicator" 
            :class="{ 'online': isOnline(participant.participantName) }"
          ></span>
        </div>
        <div class="participant-info">
          <span class="participant-name">{{ participant.participantName }}</span>
          <span class="participant-status">
            {{ isOnline(participant.participantName) ? '在线' : '离线' }}
          </span>
        </div>
      </div>

      <div v-if="participants.length === 0" class="empty-state">
        <el-icon :size="40" style="color: var(--color-text-tertiary)">
          <UserFilled />
        </el-icon>
        <p>暂无参与者</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { UserFilled } from '@element-plus/icons-vue'

const props = defineProps({
  participants: {
    type: Array,
    default: () => []
  },
  onlineUsers: {
    type: Array,
    default: () => []
  }
})

// 获取头像文字(首字母)
const getAvatarText = (name) => {
  if (!name) return '?'
  return name.charAt(0).toUpperCase()
}

// 判断用户是否在线(如果没有onlineUsers数据,默认全部在线)
const isOnline = (name) => {
  if (props.onlineUsers.length === 0) return true
  return props.onlineUsers.includes(name)
}
</script>

<style scoped>
.participants-sidebar {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-primary);
  border-radius: var(--radius-lg);
  padding: 20px;
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 头部 */
.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border-secondary);
}

.sidebar-header h3 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.participant-count {
  color: var(--color-accent-green);
  font-size: 14px;
  font-weight: 600;
  padding: 4px 12px;
  background: rgba(0, 255, 136, 0.1);
  border-radius: 12px;
}

/* 参与者列表 */
.participants-list {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
  padding-right: 4px;
}

/* 参与者项 */
.participant-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  transition: all var(--duration-fast) var(--ease-out);
  animation: fadeInLeft 0.5s var(--ease-out) backwards;
}

.participant-item:hover {
  background: var(--color-bg-elevated);
  transform: translateX(4px);
  box-shadow: 0 4px 12px rgba(0, 255, 136, 0.1);
}

/* 头像 */
.participant-avatar {
  position: relative;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-accent-green), var(--color-accent-blue));
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 600;
  flex-shrink: 0;
}

/* 在线指示器 */
.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--color-text-tertiary);
  border: 2px solid var(--color-bg-tertiary);
  transition: all var(--duration-fast) var(--ease-out);
}

.online-indicator.online {
  background: var(--color-accent-green);
  box-shadow: 0 0 10px rgba(0, 255, 136, 0.5);
  animation: pulse 2s ease-in-out infinite;
}

/* 参与者信息 */
.participant-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.participant-name {
  color: var(--color-text-primary);
  font-size: 15px;
  font-weight: 500;
}

.participant-status {
  color: var(--color-text-tertiary);
  font-size: 12px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 40px 20px;
  color: var(--color-text-tertiary);
  text-align: center;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

/* 滚动条样式 */
.participants-list::-webkit-scrollbar {
  width: 6px;
}

.participants-list::-webkit-scrollbar-track {
  background: var(--color-bg-tertiary);
  border-radius: 3px;
}

.participants-list::-webkit-scrollbar-thumb {
  background: var(--color-accent-green);
  border-radius: 3px;
}

.participants-list::-webkit-scrollbar-thumb:hover {
  background: var(--color-accent-blue);
}

/* 响应式 */
@media (max-width: 768px) {
  .participants-sidebar {
    padding: 16px;
  }

  .sidebar-header h3 {
    font-size: 16px;
  }

  .participant-avatar {
    width: 36px;
    height: 36px;
    font-size: 14px;
  }

  .participant-name {
    font-size: 14px;
  }
}
</style>
