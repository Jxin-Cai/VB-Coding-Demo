<template>
  <!-- 全屏背景蒙层 -->
  <div 
    v-if="isFullscreen" 
    class="story-card-backdrop"
    @click="exitFullscreen"
  ></div>

  <!-- 故事卡 -->
  <div 
    class="story-card-display"
    :class="{ 'fullscreen': isFullscreen }"
    @dblclick="toggleFullscreen"
  >
    <div class="story-card-content">
      <div class="story-card-header">
        <h2>📝 {{ storyCard.title || '未命名故事卡' }}</h2>
        <div v-if="isFullscreen" class="fullscreen-hint">
          按 ESC 退出全屏
        </div>
      </div>

      <div class="story-card-body">
        <div class="story-section">
          <h3 class="section-title">描述</h3>
          <p class="section-content">{{ storyCard.description || '暂无描述' }}</p>
        </div>

        <div v-if="storyCard.acceptanceCriteria" class="story-section">
          <h3 class="section-title">验收标准</h3>
          <ul class="acceptance-criteria-list">
            <li 
              v-for="(criterion, index) in acceptanceCriteria" 
              :key="index"
            >
              {{ criterion }}
            </li>
          </ul>
        </div>
      </div>

      <div v-if="!isFullscreen" class="story-card-footer">
        <span class="hint-text">💡 双击可全屏查看</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  storyCard: {
    type: Object,
    required: true,
    default: () => ({})
  }
})

const isFullscreen = ref(false)

// 解析验收标准(假设是字符串,按换行分割)
const acceptanceCriteria = computed(() => {
  if (!props.storyCard.acceptanceCriteria) return []
  if (Array.isArray(props.storyCard.acceptanceCriteria)) {
    return props.storyCard.acceptanceCriteria
  }
  return props.storyCard.acceptanceCriteria.split('\n').filter(line => line.trim())
})

const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

const exitFullscreen = () => {
  isFullscreen.value = false
}

// ESC键退出全屏
const handleKeydown = (e) => {
  if (e.key === 'Escape' && isFullscreen.value) {
    exitFullscreen()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
/* 背景蒙层 */
.story-card-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.8);
  backdrop-filter: blur(10px);
  z-index: 999;
  animation: fadeIn 0.3s var(--ease-out);
}

/* 故事卡容器 */
.story-card-display {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-primary);
  border-radius: var(--radius-lg);
  padding: 24px;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  animation: fadeInUp 0.6s var(--ease-out);
}

/* 悬浮效果 */
.story-card-display:not(.fullscreen):hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 
    0 8px 24px rgba(0, 0, 0, 0.4),
    0 0 20px rgba(0, 255, 136, 0.2);
  border-color: var(--color-accent-green);
}

/* 全屏模式 */
.story-card-display.fullscreen {
  position: fixed;
  top: 5%;
  left: 5%;
  width: 90vw;
  height: 90vh;
  z-index: 1000;
  transform: scale(1.05);
  box-shadow: 
    0 20px 60px rgba(0, 0, 0, 0.6),
    0 0 40px rgba(0, 255, 136, 0.4);
  border-color: var(--color-accent-green);
  overflow-y: auto;
  cursor: default;
  animation: scaleIn 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 内容区域 */
.story-card-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  height: 100%;
}

/* 头部 */
.story-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border-secondary);
}

.story-card-header h2 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 20px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

.fullscreen .story-card-header h2 {
  font-size: 28px;
}

.fullscreen-hint {
  color: var(--color-text-tertiary);
  font-size: 14px;
  animation: breathe 2s ease-in-out infinite;
}

/* 主体 */
.story-card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 24px;
  overflow-y: auto;
}

.story-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-title {
  margin: 0;
  color: var(--color-accent-green);
  font-size: 16px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.fullscreen .section-title {
  font-size: 18px;
}

.section-content {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 15px;
  line-height: 1.8;
  white-space: pre-wrap;
}

.fullscreen .section-content {
  font-size: 17px;
  line-height: 2;
}

/* 验收标准列表 */
.acceptance-criteria-list {
  margin: 0;
  padding-left: 24px;
  color: var(--color-text-primary);
  font-size: 15px;
  line-height: 1.8;
}

.fullscreen .acceptance-criteria-list {
  font-size: 17px;
  line-height: 2;
}

.acceptance-criteria-list li {
  margin-bottom: 8px;
}

.acceptance-criteria-list li::marker {
  color: var(--color-accent-green);
}

/* 底部 */
.story-card-footer {
  padding-top: 16px;
  border-top: 1px solid var(--color-border-secondary);
  text-align: center;
}

.hint-text {
  color: var(--color-text-tertiary);
  font-size: 13px;
  transition: color 0.3s var(--ease-out);
}

.story-card-display:hover .hint-text {
  color: var(--color-accent-blue);
}

/* 响应式 */
@media (max-width: 768px) {
  .story-card-display.fullscreen {
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    border-radius: 0;
  }

  .story-card-display {
    padding: 16px;
  }

  .fullscreen .story-card-header h2 {
    font-size: 24px;
  }

  .fullscreen .section-content {
    font-size: 16px;
  }
}

/* 滚动条样式 */
.story-card-body::-webkit-scrollbar {
  width: 6px;
}

.story-card-body::-webkit-scrollbar-track {
  background: var(--color-bg-tertiary);
  border-radius: 3px;
}

.story-card-body::-webkit-scrollbar-thumb {
  background: var(--color-accent-green);
  border-radius: 3px;
}

.story-card-body::-webkit-scrollbar-thumb:hover {
  background: var(--color-accent-blue);
}
</style>
