<template>
  <div class="statistics-panel">
    <!-- 头部标题 -->
    <div class="panel-header">
      <h3>📊 统计结果</h3>
      <el-tag v-if="revealed" type="success" size="small">已揭示</el-tag>
      <el-tag v-else type="info" size="small">
        {{ votedCount }} / {{ totalCount }} 已投票
      </el-tag>
    </div>

    <!-- 统计数值网格 -->
    <div v-if="revealed" class="stats-grid" role="region" aria-live="polite" aria-label="投票统计结果">
      <div class="stat-item stat-average">
        <div class="stat-value">{{ animatedAverage || 'N/A' }}</div>
        <div class="stat-label">平均值</div>
      </div>
      <div class="stat-item stat-max">
        <div class="stat-value">{{ animatedMax || 'N/A' }}</div>
        <div class="stat-label">最大值</div>
      </div>
      <div class="stat-item stat-min">
        <div class="stat-value">{{ animatedMin || 'N/A' }}</div>
        <div class="stat-label">最小值</div>
      </div>
    </div>

    <!-- 点数分布图 -->
    <div v-if="revealed && distribution && distribution.length > 0" class="distribution-section">
      <div class="section-title">点数分布</div>
      <VoteDistributionChart :distribution="distribution" />
    </div>

    <!-- 未揭示时显示进度 -->
    <div v-if="!revealed" class="voting-progress">
      <div class="progress-info">
        <span>投票进度</span>
        <span class="progress-percentage">{{ progressPercentage }}%</span>
      </div>
      <div class="progress-bar-container">
        <div 
          class="progress-bar" 
          :style="{ width: `${progressPercentage}%` }"
        ></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useNumberAnimation } from '../../composables/useNumberAnimation'
import VoteDistributionChart from './VoteDistributionChart.vue'

const props = defineProps({
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
  }
})

// 统计数字的响应式源
const averageValue = computed(() => props.stats.average || 0)
const maxValue = computed(() => props.stats.max || 0)
const minValue = computed(() => props.stats.min || 0)

// 数字滚动动画
const { displayValue: animatedAverage } = useNumberAnimation(averageValue, { duration: 800, decimals: 1 })
const { displayValue: animatedMax } = useNumberAnimation(maxValue, { duration: 600 })
const { displayValue: animatedMin } = useNumberAnimation(minValue, { duration: 600 })

// 投票进度百分比
const progressPercentage = computed(() => {
  if (!props.totalCount || props.totalCount === 0) return 0
  return Math.round((props.votedCount / props.totalCount) * 100)
})
</script>

<style scoped>
.statistics-panel {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-primary);
  border-radius: var(--radius-lg);
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 头部 */
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--color-border-secondary);
}

.panel-header h3 {
  margin: 0;
  color: var(--color-text-primary);
  font-size: 18px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 统计数值网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: var(--color-bg-tertiary);
  border-radius: var(--radius-md);
  transition: all var(--duration-normal) var(--ease-out);
  animation: scaleIn 0.6s var(--ease-out) backwards;
}

.stat-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2);
}

/* 统计值 */
.stat-value {
  font-size: 36px;
  font-weight: bold;
  margin-bottom: 8px;
  font-family: 'Monaco', 'Menlo', monospace;
  line-height: 1;
}

.stat-average .stat-value {
  color: var(--color-accent-green);
  text-shadow: 0 0 20px rgba(0, 255, 136, 0.3);
}

.stat-max .stat-value {
  color: var(--color-accent-pink);
  text-shadow: 0 0 20px rgba(255, 0, 136, 0.3);
}

.stat-min .stat-value {
  color: var(--color-accent-blue);
  text-shadow: 0 0 20px rgba(0, 212, 255, 0.3);
}

/* 统计标签 */
.stat-label {
  font-size: 12px;
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* 分布区域 */
.distribution-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.section-title {
  color: var(--color-text-secondary);
  font-size: 14px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

/* 投票进度 */
.voting-progress {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--color-text-primary);
  font-size: 14px;
}

.progress-percentage {
  font-weight: 600;
  color: var(--color-accent-blue);
}

.progress-bar-container {
  height: 12px;
  background: var(--color-bg-tertiary);
  border-radius: 6px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, var(--color-accent-green), var(--color-accent-blue));
  border-radius: 6px;
  transition: width 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 0 10px rgba(0, 255, 136, 0.3);
  animation: barGrow 0.6s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 响应式 */
@media (max-width: 768px) {
  .statistics-panel {
    padding: 16px;
  }

  .stats-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .stat-value {
    font-size: 32px;
  }

  .panel-header h3 {
    font-size: 16px;
  }
}

/* 一致结果时的光效扫过 */
.statistics-panel.consensus {
  animation: glowSweep 1s ease-in-out;
}
</style>
