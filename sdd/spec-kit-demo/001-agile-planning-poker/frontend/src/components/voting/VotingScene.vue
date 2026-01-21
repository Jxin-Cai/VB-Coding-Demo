<template>
  <div class="voting-scene" role="tabpanel" aria-label="估点场景">
    <div class="scene-layout">
      <!-- 左侧: 投票中心区域 (65%) -->
      <div class="voting-center">
        <!-- 投票选择区 -->
        <el-card class="vote-selection-card">
          <template #header>
            <div class="card-header">
              <span>🎯 选择故事点数</span>
              <el-tag v-if="myVote" type="success">
                已投票: {{ myVote.storyPoint }} 点
              </el-tag>
              <el-tag v-else-if="selectedPoint" type="warning">
                已选择: {{ selectedPoint }} 点 (待确认)
              </el-tag>
            </div>
          </template>

          <div class="point-cards">
            <div 
              v-for="point in storyPoints" 
              :key="point"
              class="point-card"
              :class="{ 
                'selected': selectedPoint === point || (myVote && !selectedPoint && myVote.storyPoint === point),
                'disabled': sessionStatus === 'COMPLETED'
              }"
              @click="handleSelectPoint(point)"
            >
              <div class="point-value">{{ point }}</div>
              <div class="point-label">点</div>
            </div>
          </div>

          <el-button 
            v-if="selectedPoint && sessionStatus !== 'COMPLETED'"
            type="primary" 
            size="large"
            style="width: 100%; margin-top: 20px"
            @click="handleSubmitVote"
            :loading="submitting"
          >
            {{ submitting ? '提交中...' : (myVote ? '修改投票' : '确认投票') }}
          </el-button>
        </el-card>

        <!-- 投票池 -->
        <el-card class="vote-pool-card">
          <template #header>
            <div class="card-header">
              <span>🎴 投票池 (Vote Pool)</span>
              <el-tag v-if="!revealed" type="info" size="small">
                {{ votedCount }} / {{ totalCount }} 已投票
              </el-tag>
              <el-tag v-else type="success" size="small">
                已揭示
              </el-tag>
            </div>
          </template>
          
          <div class="pool-cards">
            <div 
              v-for="(participant, index) in participants" 
              :key="participant.participantName"
              class="vote-card" 
              :class="{ 
                'flipped': revealed && participant.hasVoted,
                'just-voted': isJustVoted(participant.participantName),
                'minority': isMinorityVote(participant.storyPoint)
              }"
              :style="{ 
                animationDelay: revealed ? `${index * 0.15}s` : '0s' 
              }"
            >
              <div class="card-inner">
                <!-- 卡片背面 -->
                <div class="card-back">
                  <div class="participant-name">{{ participant.participantName }}</div>
                  <div v-if="participant.hasVoted" class="vote-status voted">
                    <el-icon :size="32"><Check /></el-icon>
                    <span>已投票</span>
                  </div>
                  <div v-else class="vote-status waiting">
                    <el-icon :size="32"><Clock /></el-icon>
                    <span>等待中...</span>
                  </div>
                </div>
                
                <!-- 卡片正面 -->
                <div class="card-front">
                  <div class="participant-name">{{ participant.participantName }}</div>
                  <div class="vote-value">{{ participant.storyPoint || '?' }}</div>
                  <div class="vote-label">点</div>
                </div>
              </div>
            </div>
            
            <el-empty 
              v-if="participants.length === 0"
              description="暂无参与者"
              :image-size="80"
            />
          </div>
        </el-card>
      </div>

      <!-- 右侧: 侧边栏 (35%) -->
      <div class="sidebar-section">
        <VotingSidebar 
          :storyCard="storyCard"
          :participants="participants"
          :stats="stats"
          :distribution="distribution"
          :revealed="revealed"
          :votedCount="votedCount"
          :totalCount="totalCount"
          :canComplete="canComplete"
          :finalPoint="finalPoint"
          :completing="completing"
          :storyPoints="storyPoints"
          @update:finalPoint="$emit('update:finalPoint', $event)"
          @complete="$emit('complete', $event)"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { Check, Clock } from '@element-plus/icons-vue'
import VotingSidebar from './VotingSidebar.vue'

const props = defineProps({
  storyCard: {
    type: Object,
    default: () => ({})
  },
  participants: {
    type: Array,
    default: () => []
  },
  myVote: {
    type: Object,
    default: null
  },
  selectedPoint: {
    type: Number,
    default: null
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
  justVotedUsers: {
    type: Set,
    default: () => new Set()
  },
  submitting: {
    type: Boolean,
    default: false
  },
  sessionStatus: {
    type: String,
    default: 'IN_PROGRESS'
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

const emit = defineEmits(['selectPoint', 'submitVote', 'update:finalPoint', 'complete'])

const handleSelectPoint = (point) => {
  emit('selectPoint', point)
}

const handleSubmitVote = () => {
  emit('submitVote')
}

const isJustVoted = (participantName) => {
  return props.justVotedUsers.has(participantName)
}

const isMinorityVote = (storyPoint) => {
  if (!props.revealed || !props.distribution) return false
  const group = props.distribution.find(g => g.storyPoint === storyPoint)
  return group?.isMinority || false
}
</script>

<style scoped>
.voting-scene {
  display: flex;
  flex-direction: column;
  padding: 24px;
  min-height: calc(100vh - 120px);
  animation: fadeIn 0.6s var(--ease-out);
}

/* 场景布局 */
.scene-layout {
  display: grid;
  grid-template-columns: 65% 1fr;
  gap: 24px;
  flex: 1;
}

/* 投票中心区域 */
.voting-center {
  display: flex;
  flex-direction: column;
  gap: 24px;
  animation: fadeInUp 0.6s var(--ease-out);
}

.vote-selection-card,
.vote-pool-card {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-primary);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  color: var(--color-text-primary);
}

/* 点数卡片 */
.point-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 20px;
  padding: 10px 0;
}

.point-card {
  aspect-ratio: 3/4;
  background: linear-gradient(135deg, var(--color-bg-tertiary) 0%, var(--color-bg-secondary) 100%);
  border: 2px solid var(--color-border-secondary);
  border-radius: var(--radius-lg);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-out);
  color: var(--color-text-primary);
  box-shadow: var(--shadow-md);
  position: relative;
  overflow: hidden;
}

.point-card:hover:not(.disabled) {
  transform: translateY(-10px) rotateX(10deg);
  box-shadow: var(--shadow-glow-blue);
  border-color: var(--color-accent-blue);
}

.point-card.selected {
  background: linear-gradient(135deg, var(--color-accent-green) 0%, var(--color-accent-blue) 100%);
  transform: scale(1.08);
  box-shadow: var(--shadow-glow-green-strong);
  border-color: var(--color-accent-green);
  animation: neonPulse 2s infinite;
}

.point-card.disabled {
  opacity: 0.5;
  cursor: not-allowed;
  filter: grayscale(0.5);
}

.point-value {
  font-size: 48px;
  font-weight: bold;
  font-family: 'Monaco', 'Menlo', monospace;
  transition: transform var(--duration-fast) var(--ease-out);
}

.point-card:hover:not(.disabled) .point-value {
  transform: scale(1.1);
}

.point-label {
  font-size: 14px;
  margin-top: 8px;
  text-transform: uppercase;
  letter-spacing: 1px;
}

/* 投票池卡片 */
.pool-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 24px;
  padding: 24px 12px;
  min-height: 240px;
}

.vote-card {
  perspective: 1500px;
  height: 240px;
  animation: fadeInUp var(--duration-slow) var(--ease-out) backwards;
}

.card-inner {
  position: relative;
  width: 100%;
  height: 100%;
  transition: transform 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  transform-style: preserve-3d;
}

.vote-card.flipped .card-inner {
  transform: rotateY(180deg);
}

.vote-card.just-voted .card-inner {
  animation: pulse var(--duration-slow) var(--ease-out);
}

/* 少数意见高亮 */
.vote-card.minority.flipped .card-inner {
  animation: pullUp 0.6s cubic-bezier(0.4, 0, 0.2, 1) forwards, breathe 2s ease-in-out 0.6s infinite;
}

.vote-card.minority .card-front {
  border: 3px solid var(--color-accent-pink);
  box-shadow: 
    0 0 30px rgba(255, 0, 136, 0.8),
    0 0 60px rgba(255, 0, 136, 0.5);
  animation: breathe 2s ease-in-out infinite;
}

/* 少数意见放大显示 */
.vote-card.minority.flipped {
  transform: translateY(-20px) scale(1.2);
  z-index: 10;
}

/* 发言提示气泡（仅在揭示后显示） */
.vote-card.minority.flipped::before {
  content: "💬 请发表意见";
  position: absolute;
  top: -50px;
  left: 50%;
  transform: translateX(-50%) scale(0);
  background: linear-gradient(135deg, #ff0088, #ff4466);
  color: white;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: bold;
  white-space: nowrap;
  box-shadow: 0 4px 12px rgba(255, 0, 136, 0.4);
  animation: bubble-appear 0.6s ease-out 0.8s forwards, shake-bubble 0.5s ease-in-out 1.4s infinite;
  z-index: 20;
}

/* 动画：呼吸（透明度变化） */
@keyframes breathe {
  0%, 100% { opacity: 0.8; box-shadow: 0 0 20px rgba(255, 0, 136, 0.5); }
  50% { opacity: 1.0; box-shadow: 0 0 40px rgba(255, 0, 136, 0.8); }
}

/* 动画：向上拉起 */
@keyframes pullUp {
  from {
    transform: translateY(0) scale(1);
  }
  to {
    transform: translateY(-20px) scale(1.2);
  }
}

/* 动画：气泡出现 */
@keyframes bubble-appear {
  from {
    transform: translateX(-50%) scale(0);
    opacity: 0;
  }
  to {
    transform: translateX(-50%) scale(1);
    opacity: 1;
  }
}

/* 动画：气泡震动 */
@keyframes shake-bubble {
  0%, 100% { transform: translateX(-50%) rotate(0deg); }
  25% { transform: translateX(-50%) rotate(-2deg); }
  75% { transform: translateX(-50%) rotate(2deg); }
}

/* 卡片正反面 */
.card-front,
.card-back {
  position: absolute;
  width: 100%;
  height: 100%;
  backface-visibility: hidden;
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-md);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
  border: 2px solid transparent;
}

.card-back {
  background: linear-gradient(135deg, var(--color-bg-tertiary) 0%, var(--color-bg-secondary) 100%);
  color: var(--color-text-primary);
  border-color: var(--color-border-secondary);
}

.card-back .participant-name {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 20px;
  text-align: center;
  word-break: break-word;
  color: var(--color-text-primary);
}

.vote-status {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.vote-status.voted {
  color: var(--color-accent-green);
}

.vote-status.waiting {
  color: var(--color-accent-orange);
  opacity: 0.9;
}

.vote-card:not(.flipped) .card-back .vote-status.waiting {
  animation: breathe 2s ease-in-out infinite;
}

.vote-card:not(.flipped) .card-back .vote-status.voted {
  animation: neonPulse 2s ease-in-out;
}

.card-front {
  background: linear-gradient(135deg, var(--color-accent-pink) 0%, var(--color-accent-blue) 100%);
  color: white;
  transform: rotateY(180deg);
  box-shadow: var(--shadow-glow-pink-strong);
}

.card-front .participant-name {
  font-size: 14px;
  margin-bottom: 10px;
  opacity: 0.95;
  text-align: center;
}

.card-front .vote-value {
  font-size: 72px;
  font-weight: bold;
  margin: 15px 0;
  line-height: 1;
  text-shadow: 0 0 20px rgba(255, 255, 255, 0.5);
  font-family: 'Monaco', 'Menlo', monospace;
}

.card-front .vote-label {
  font-size: 16px;
  opacity: 0.9;
}

/* 侧边栏区域 */
.sidebar-section {
  animation: fadeInRight 0.6s var(--ease-out) 0.2s backwards;
}

/* 响应式 */
@media (max-width: 1200px) {
  .scene-layout {
    grid-template-columns: 1fr;
  }

  .voting-center,
  .sidebar-section {
    animation-delay: 0s !important;
  }
}

@media (max-width: 768px) {
  .voting-scene {
    padding: 16px;
  }

  .scene-layout {
    gap: 16px;
  }

  .pool-cards {
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
    padding: 16px 8px;
  }

  .vote-card {
    height: 180px;
  }

  .card-front .vote-value {
    font-size: 56px;
  }

  .point-cards {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }
}
</style>
