<template>
  <div class="vote-distribution-chart">
    <div v-if="distribution && distribution.length > 0" class="chart-container">
      <div 
        v-for="(group, index) in distribution" 
        :key="group.storyPoint"
        class="chart-item"
        :class="{ 'minority': group.isMinority }"
        :style="{ animationDelay: `${index * 100}ms` }"
      >
        <!-- 点数标签 -->
        <div class="point-label">
          {{ group.storyPoint }} 点
        </div>

        <!-- 柱子容器 -->
        <div class="bar-container">
          <div 
            class="bar" 
            :style="{ width: `${group.percentage}%` }"
          >
            <div class="bar-fill"></div>
          </div>
        </div>

        <!-- 人数和百分比 -->
        <div class="count-label">
          <span class="count">{{ group.count }} 人</span>
          <span class="percentage">({{ group.percentage }}%)</span>
          <el-icon v-if="group.isMinority" class="warning-icon">
            <WarningFilled />
          </el-icon>
        </div>
      </div>
    </div>

    <div v-else class="empty-state">
      <el-icon :size="32" style="color: var(--color-text-tertiary)">
        <DataLine />
      </el-icon>
      <p>暂无投票数据</p>
    </div>

    <!-- 少数意见提示 -->
    <div v-if="hasMinority" class="minority-hint">
      <el-icon><ChatDotRound /></el-icon>
      少数意见者请发表看法
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { WarningFilled, DataLine, ChatDotRound } from '@element-plus/icons-vue'

const props = defineProps({
  distribution: {
    type: Array,
    default: () => []
  }
})

// 是否存在少数意见
const hasMinority = computed(() => {
  return props.distribution.some(group => group.isMinority)
})
</script>

<style scoped>
.vote-distribution-chart {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

/* 图表容器 */
.chart-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

/* 图表项 */
.chart-item {
  display: grid;
  grid-template-columns: 80px 1fr 140px;
  gap: 12px;
  align-items: center;
  padding: 8px;
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  transition: all var(--duration-fast) var(--ease-out);
  animation: fadeInLeft 0.5s var(--ease-out) backwards;
}

.chart-item:hover {
  background: var(--color-bg-elevated);
  transform: translateX(4px);
}

/* 少数意见项 */
.chart-item.minority {
  border: 2px solid var(--color-accent-pink);
  background: rgba(255, 0, 136, 0.05);
  animation: fadeInLeft 0.5s var(--ease-out) backwards, breathe 2s ease-in-out infinite;
}

.chart-item.minority .bar-fill {
  background: linear-gradient(90deg, var(--color-accent-pink), rgba(255, 0, 136, 0.6));
  box-shadow: 0 0 20px rgba(255, 0, 136, 0.4);
}

/* 点数标签 */
.point-label {
  color: var(--color-text-primary);
  font-size: 14px;
  font-weight: 600;
  text-align: right;
}

/* 柱子容器 */
.bar-container {
  position: relative;
  height: 32px;
  background: var(--color-bg-secondary);
  border-radius: 16px;
  overflow: hidden;
}

.bar {
  position: relative;
  height: 100%;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

.bar-fill {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, var(--color-accent-green), var(--color-accent-blue));
  border-radius: 16px;
  box-shadow: 0 0 20px rgba(0, 255, 136, 0.3);
  animation: barGrow 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes barGrow {
  from {
    transform: scaleX(0);
    transform-origin: left;
  }
  to {
    transform: scaleX(1);
  }
}

/* 人数标签 */
.count-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text-primary);
  font-size: 14px;
}

.count {
  font-weight: 600;
}

.percentage {
  color: var(--color-text-tertiary);
  font-size: 13px;
}

.warning-icon {
  color: var(--color-accent-pink);
  font-size: 18px;
  animation: pulse 2s ease-in-out infinite;
}

/* 少数意见提示 */
.minority-hint {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(255, 0, 136, 0.1);
  border: 1px solid var(--color-accent-pink);
  border-radius: var(--radius-md);
  color: var(--color-accent-pink);
  font-size: 14px;
  font-weight: 500;
  animation: fadeInUp 0.5s var(--ease-out) 0.3s backwards;
}

.minority-hint .el-icon {
  font-size: 18px;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 40px 20px;
  color: var(--color-text-tertiary);
  text-align: center;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

/* 响应式 */
@media (max-width: 768px) {
  .chart-item {
    grid-template-columns: 60px 1fr 100px;
    gap: 8px;
    padding: 6px;
  }

  .point-label {
    font-size: 13px;
  }

  .bar-container {
    height: 24px;
  }

  .count-label {
    font-size: 13px;
  }

  .count,
  .percentage {
    font-size: 12px;
  }

  .warning-icon {
    font-size: 16px;
  }
}

/* 呼吸动画（用于少数意见） */
@keyframes breathe {
  0%, 100% {
    border-color: var(--color-accent-pink);
    box-shadow: 0 0 10px rgba(255, 0, 136, 0.3);
  }
  50% {
    border-color: rgba(255, 0, 136, 0.8);
    box-shadow: 0 0 20px rgba(255, 0, 136, 0.6);
  }
}

/* 禁用动画在低端设备 */
@media (prefers-reduced-motion: reduce) {
  .chart-item.minority {
    animation: fadeInLeft 0.5s var(--ease-out) backwards;
  }

  .bar-fill {
    animation: none;
  }
}

.reduced-animations .chart-item.minority {
  animation: fadeInLeft 0.5s var(--ease-out) backwards;
}

.reduced-animations .bar-fill {
  animation: none;
}
</style>
