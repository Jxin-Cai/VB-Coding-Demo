<template>
  <div class="story-review-scene" role="tabpanel" aria-label="故事卡讲解场景">
    <div class="scene-layout">
      <!-- 左侧: 故事卡展示 (60%) -->
      <div class="story-card-section">
        <StoryCardDisplay :storyCard="storyCard" />
      </div>

      <!-- 右侧: 参与者列表 (40%) -->
      <div class="participants-section">
        <ParticipantsSidebar 
          :participants="participants" 
          :onlineUsers="onlineUsers"
        />
      </div>
    </div>

    <!-- 底部: 开始估点按钮 -->
    <div class="action-section">
      <el-button 
        class="start-voting-button" 
        type="primary" 
        size="large"
        @click="handleStartVoting"
        aria-label="开始估点会话"
      >
        <el-icon><TrendCharts /></el-icon>
        开始估点
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { TrendCharts } from '@element-plus/icons-vue'
import StoryCardDisplay from './StoryCardDisplay.vue'
import ParticipantsSidebar from './ParticipantsSidebar.vue'

const props = defineProps({
  storyCard: {
    type: Object,
    required: true,
    default: () => ({})
  },
  participants: {
    type: Array,
    default: () => []
  },
  onlineUsers: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['startVoting'])

const handleStartVoting = () => {
  emit('startVoting')
}
</script>

<style scoped>
.story-review-scene {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 24px;
  min-height: calc(100vh - 120px);
  animation: fadeIn 0.6s var(--ease-out);
}

/* 场景布局 */
.scene-layout {
  display: grid;
  grid-template-columns: 60% 1fr;
  gap: 24px;
  flex: 1;
}

/* 故事卡区域 */
.story-card-section {
  animation: fadeInUp 0.6s var(--ease-out);
}

/* 参与者区域 */
.participants-section {
  animation: fadeInRight 0.6s var(--ease-out) 0.2s backwards;
}

/* 操作区域 */
.action-section {
  display: flex;
  justify-content: center;
  padding: 20px;
  animation: fadeInUp 0.6s var(--ease-out) 0.4s backwards;
}

/* 开始估点按钮 */
.start-voting-button {
  min-width: 240px;
  height: 56px;
  font-size: 18px;
  font-weight: 600;
  background: linear-gradient(135deg, var(--color-accent-green), var(--color-accent-blue));
  border: none;
  border-radius: 28px;
  box-shadow: 
    0 4px 16px rgba(0, 255, 136, 0.3),
    0 0 20px rgba(0, 255, 136, 0.2);
  transition: all var(--duration-normal) var(--ease-out);
  animation: breathe 2s ease-in-out infinite;
}

.start-voting-button:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 
    0 8px 24px rgba(0, 255, 136, 0.5),
    0 0 40px rgba(0, 255, 136, 0.4);
}

.start-voting-button:active {
  transform: translateY(-2px) scale(1.02);
}

.start-voting-button :deep(.el-icon) {
  font-size: 20px;
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .scene-layout {
    grid-template-columns: 1fr;
  }

  .story-card-section,
  .participants-section {
    animation-delay: 0s !important;
  }
}

@media (max-width: 768px) {
  .story-review-scene {
    padding: 16px;
    gap: 16px;
  }

  .scene-layout {
    gap: 16px;
  }

  .start-voting-button {
    min-width: 100%;
    height: 48px;
    font-size: 16px;
  }
}

/* 禁用呼吸动画在低端设备 */
@media (prefers-reduced-motion: reduce) {
  .start-voting-button {
    animation: none;
  }
}

.reduced-animations .start-voting-button {
  animation: none;
}
</style>
