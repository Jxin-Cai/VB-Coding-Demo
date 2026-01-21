<template>
  <div class="voting-sidebar">
    <!-- 故事卡标题 -->
    <div class="story-card-summary">
      <div class="summary-header">
        <el-icon><Document /></el-icon>
        <span>故事卡</span>
      </div>
      <h3 class="story-title">{{ storyCard.title || '未命名' }}</h3>
    </div>

    <!-- 参与者列表 (精简版) -->
    <div class="participants-compact">
      <div class="compact-header">
        <el-icon><UserFilled /></el-icon>
        <span>参与者 ({{ participants.length }})</span>
      </div>
      <div class="compact-list">
        <div 
          v-for="participant in participants" 
          :key="participant.participantName"
          class="compact-item"
        >
          <div class="compact-avatar">
            {{ getAvatarText(participant.participantName) }}
          </div>
          <span class="compact-name">{{ participant.participantName }}</span>
          <el-tag 
            v-if="revealed && participant.storyPoint" 
            :type="getVoteTagType(participant)"
            size="small"
          >
            {{ participant.storyPoint }} 点
          </el-tag>
          <el-tag v-else-if="participant.hasVoted" type="success" size="small">
            ✓
          </el-tag>
          <el-tag v-else type="info" size="small">
            ...
          </el-tag>
        </div>
      </div>
    </div>

    <!-- 统计面板 -->
    <StatisticsPanel 
      :stats="stats"
      :distribution="distribution"
      :revealed="revealed"
      :votedCount="votedCount"
      :totalCount="totalCount"
    />

    <!-- 最终点数选择 (已揭示后显示) -->
    <div v-if="revealed && canComplete" class="final-point-section">
      <div class="section-header">
        <el-icon><Select /></el-icon>
        <span>最终点数</span>
      </div>
      <el-radio-group v-model="localFinalPoint" size="large" style="width: 100%">
        <el-radio-button 
          v-for="point in storyPoints" 
          :key="point"
          :label="point"
          style="flex: 1"
        >
          {{ point }}
        </el-radio-button>
      </el-radio-group>
      <el-button 
        type="success" 
        size="large"
        style="width: 100%; margin-top: 12px"
        @click="handleComplete"
        :disabled="!localFinalPoint"
        :loading="completing"
      >
        完成估点
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Document, UserFilled, Select } from '@element-plus/icons-vue'
import StatisticsPanel from './StatisticsPanel.vue'

const props = defineProps({
  storyCard: {
    type: Object,
    default: () => ({})
  },
  participants: {
    type: Array,
    default: () => []
  },
  stats: {
    type: Object,
    default: () => ({})
  },
  distribution: {
    type: Array,
    default: () => []
  },
  revealed: {
    type: Boolean,
    default: false
  },
  votedCount: {
    type: Number,
    default: 0
  },
  totalCount: {
    type: Number,
    default: 0
  },
  canComplete: {
    type: Boolean,
    default: false
  },
  finalPoint: {
    type: Number,
    default: null
  },
  completing: {
    type: Boolean,
    default: false
  },
  storyPoints: {
    type: Array,
    default: () => [1, 2, 3, 5, 8, 13]
  }
})

const emit = defineEmits(['update:finalPoint', 'complete'])

const localFinalPoint = ref(props.finalPoint)

watch(() => props.finalPoint, (newVal) => {
  localFinalPoint.value = newVal
})

watch(localFinalPoint, (newVal) => {
  emit('update:finalPoint', newVal)
})

const getAvatarText = (name) => {
  if (!name) return '?'
  return name.charAt(0).toUpperCase()
}

const getVoteTagType = (participant) => {
  if (!props.distribution || props.distribution.length === 0) return 'info'
  
  const group = props.distribution.find(g => g.storyPoint === participant.storyPoint)
  if (!group) return 'info'
  
  return group.isMinority ? 'danger' : 'success'
}

const handleComplete = () => {
  emit('complete', localFinalPoint.value)
}
</script>

<style scoped>
.voting-sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 故事卡摘要 */
.story-card-summary {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-primary);
  border-radius: var(--radius-lg);
  padding: 16px;
  animation: fadeInRight 0.5s var(--ease-out);
}

.summary-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-tertiary);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 8px;
}

.story-title {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 16px;
  font-weight: 600;
  line-height: 1.5;
}

/* 参与者列表 (精简版) */
.participants-compact {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-primary);
  border-radius: var(--radius-lg);
  padding: 16px;
  animation: fadeInRight 0.5s var(--ease-out) 0.1s backwards;
}

.compact-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-tertiary);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 12px;
}

.compact-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 300px;
  overflow-y: auto;
}

.compact-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-sm);
  transition: all var(--duration-fast) var(--ease-out);
}

.compact-item:hover {
  background: var(--color-bg-elevated);
  transform: translateX(2px);
}

.compact-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-accent-green), var(--color-accent-blue));
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.compact-name {
  flex: 1;
  color: var(--color-text-primary);
  font-size: 13px;
}

/* 最终点数选择 */
.final-point-section {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-primary);
  border-radius: var(--radius-lg);
  padding: 16px;
  animation: fadeInRight 0.5s var(--ease-out) 0.3s backwards;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-tertiary);
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 12px;
}

/* Radio按钮组样式 */
.el-radio-group {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

/* 滚动条样式 */
.compact-list::-webkit-scrollbar {
  width: 4px;
}

.compact-list::-webkit-scrollbar-track {
  background: var(--color-bg-tertiary);
  border-radius: 2px;
}

.compact-list::-webkit-scrollbar-thumb {
  background: var(--color-accent-green);
  border-radius: 2px;
}

/* 响应式 */
@media (max-width: 768px) {
  .voting-sidebar {
    gap: 16px;
  }

  .story-card-summary,
  .participants-compact,
  .final-point-section {
    padding: 12px;
  }

  .compact-list {
    max-height: 200px;
  }

  .el-radio-group {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
