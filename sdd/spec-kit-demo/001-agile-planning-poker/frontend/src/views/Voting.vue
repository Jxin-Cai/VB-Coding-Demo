<template>
  <div class="voting-container">
    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div>
          <h2><el-icon><TrendCharts /></el-icon> 估点会话</h2>
          <p class="subtitle" v-if="session">{{ session.storyCard?.title }}</p>
        </div>
        <div class="header-actions">
          <el-button 
            v-if="currentScene === 'voting' && session?.status === 'IN_PROGRESS'"
            @click="switchToStoryReview"
            aria-label="返回故事卡讲解"
          >
            <el-icon><Back /></el-icon>
            返回讲解
          </el-button>
          <el-button 
            v-if="session?.status === 'IN_PROGRESS'"
            type="warning" 
            @click="cancelSession"
            :loading="cancelling"
            aria-label="取消估点会话"
          >
            <el-icon><Close /></el-icon>
            取消估点
          </el-button>
          <el-button @click="goBack" aria-label="返回需求池">
            <el-icon><Back /></el-icon>
            返回需求池
          </el-button>
        </div>
      </div>
    </el-card>

    <div v-loading="loading" class="voting-content">
      <!-- 场景切换 -->
      <Transition name="scene-flip" mode="out-in">
        <!-- 故事卡讲解场景 -->
        <StoryReviewScene
          v-if="currentScene === 'story-review' && session?.status === 'IN_PROGRESS'"
          :key="'story-review'"
          :storyCard="session?.storyCard || {}"
          :participants="participantsForReview"
          :onlineUsers="[]"
          @startVoting="switchToVoting"
        />

        <!-- 估点场景 -->
        <VotingScene
          v-else-if="currentScene === 'voting' && session?.status === 'IN_PROGRESS'"
          :key="'voting'"
          :storyCard="session?.storyCard || {}"
          :participants="allParticipants"
          :myVote="myVote"
          :selectedPoint="selectedPoint"
          :stats="statsSummary"
          :distribution="voteDistribution"
          :revealed="!!session?.revealedAt"
          :votedCount="session?.totalVotes || 0"
          :totalCount="session?.totalParticipants || 0"
          :justVotedUsers="justVotedUsers"
          :submitting="submitting"
          :sessionStatus="session?.status"
          :canComplete="canComplete"
          :finalPoint="finalPoint"
          :completing="completing"
          :storyPoints="storyPoints"
          @selectPoint="selectPoint"
          @submitVote="submitVote"
          @update:finalPoint="finalPoint = $event"
          @complete="completeSessionWithPoint"
        />

        <!-- 已完成/已取消状态 -->
        <div v-else :key="'status'" class="status-display">
          <el-card 
            v-if="session?.status === 'COMPLETED'"
            class="complete-section"
          >
            <el-result
              icon="success"
              title="估点已完成"
              sub-title="该会话已成功完成"
            >
              <template #extra>
                <el-button type="primary" @click="goBack">
                  返回需求池
                </el-button>
              </template>
            </el-result>
          </el-card>
          
          <el-card 
            v-if="session?.status === 'CANCELLED'"
            class="complete-section"
          >
            <el-result
              icon="warning"
              title="估点已取消"
              sub-title="该会话已被取消"
            >
              <template #extra>
                <el-button type="primary" @click="goBack">
                  返回需求池
                </el-button>
              </template>
            </el-result>
          </el-card>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { TrendCharts, Back, Close } from '@element-plus/icons-vue'
import api from '../api'
import { useUserStore } from '../stores/user'
import { useVotingScene } from '../composables/useVotingScene'
import { useRevealEffects } from '../composables/useRevealEffects'
import { usePerformance } from '../composables/usePerformance'
import { computeDistribution, isConsensus, computeStatsSummary } from '../utils/statisticsCalculator'
import StoryReviewScene from '../components/voting/StoryReviewScene.vue'
import VotingScene from '../components/voting/VotingScene.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 场景状态管理
const { currentScene, isTransitioning, switchToVoting, switchToStoryReview } = useVotingScene()

// 结果揭示动效
const { playRevealSequence } = useRevealEffects()

// 性能优化
const { isLowPerformance, reducedAnimations } = usePerformance()

const loading = ref(false)
const submitting = ref(false)
const completing = ref(false)
const cancelling = ref(false)
const session = ref(null)
const selectedPoint = ref(null)
const finalPoint = ref(null)
const justVotedUsers = ref(new Set())
const storyPoints = [1, 2, 3, 5, 8, 13]

let pollInterval = null
const revealTriggered = ref(false)

const storyCardId = computed(() => route.params.storyCardId)

const myVote = computed(() => {
  if (!session.value?.votes) return null
  return session.value.votes.find(
    v => v.participantName === userStore.currentUser?.name
  )
})

const canComplete = computed(() => {
  return session.value?.votes && session.value.votes.length > 0
})

// 所有参与者列表（包括未投票的）
const allParticipants = computed(() => {
  if (!session.value) return []
  
  const participants = session.value.participants || []
  const votes = session.value.votes || []
  
  return participants.map(name => {
    const vote = votes.find(v => v.participantName === name)
    return {
      participantName: name,
      storyPoint: vote?.storyPoint || null,
      hasVoted: !!vote
    }
  })
})

// 用于故事卡讲解场景的参与者列表
const participantsForReview = computed(() => {
  if (!session.value) return []
  return (session.value.participants || []).map(name => ({
    participantName: name,
    hasVoted: false,
    storyPoint: null
  }))
})

// 计算点数分布
const voteDistribution = computed(() => {
  if (!session.value?.votes || !session.value.revealedAt) return []
  return computeDistribution(session.value.votes, session.value.totalParticipants)
})

// 计算统计摘要
const statsSummary = computed(() => {
  if (!session.value) return {}
  return computeStatsSummary(session.value)
})

// 检查是否一致
const isConsensusResult = computed(() => {
  return isConsensus(voteDistribution.value)
})

const loadSession = async () => {
  try {
    const response = await api.getVotingSession(storyCardId.value)
    session.value = response.data
    
    // 如果投票已揭示且有统计结果，建议最终点数为平均值
    if (session.value.revealedAt && session.value.averageStoryPoint && !finalPoint.value) {
      const avg = session.value.averageStoryPoint
      const suggested = storyPoints.reduce((prev, curr) => {
        return Math.abs(curr - avg) < Math.abs(prev - avg) ? curr : prev
      })
      finalPoint.value = suggested
    }
  } catch (error) {
    console.error('加载会话失败:', error)
    ElMessage.error(error.response?.data?.message || '加载失败')
  }
}

const selectPoint = (point) => {
  if (session.value?.status === 'COMPLETED') return
  if (session.value?.status === 'CANCELLED') return
  selectedPoint.value = point
}

const submitVote = async () => {
  if (!selectedPoint.value) {
    ElMessage.warning('请先选择点数')
    return
  }

  submitting.value = true
  try {
    await api.submitVote(storyCardId.value, selectedPoint.value)
    const isUpdate = myVote.value !== null
    ElMessage.success(isUpdate ? '投票已修改' : '投票成功')
    
    const currentUser = userStore.currentUser?.name
    if (currentUser) {
      justVotedUsers.value.add(currentUser)
      setTimeout(() => {
        justVotedUsers.value.delete(currentUser)
      }, 2000)
    }
    
    selectedPoint.value = null
    await loadSession()
  } catch (error) {
    console.error('投票失败:', error)
    ElMessage.error(error.response?.data?.message || '投票失败')
  } finally {
    submitting.value = false
  }
}

const cancelSession = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要取消当前估点会话吗？',
      '确认取消',
      {
        confirmButtonText: '确定取消',
        cancelButtonText: '继续估点',
        type: 'warning'
      }
    )
    
    cancelling.value = true
    await api.cancelSession(storyCardId.value)
    ElMessage.success('已取消估点会话')
    setTimeout(() => {
      goBack()
    }, 1000)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消会话失败:', error)
      ElMessage.error(error.response?.data?.message || '取消会话失败')
    }
  } finally {
    cancelling.value = false
  }
}

const completeSessionWithPoint = async (point) => {
  if (!point) {
    ElMessage.warning('请选择最终点数')
    return
  }

  completing.value = true
  try {
    await api.completeSession(storyCardId.value, point)
    ElMessage.success('估点会话已完成')
    setTimeout(() => {
      goBack()
    }, 1500)
  } catch (error) {
    console.error('完成会话失败:', error)
    ElMessage.error(error.response?.data?.message || '完成会话失败')
  } finally {
    completing.value = false
  }
}

const goBack = () => {
  // 获取poolId并返回到特定需求池
  const poolId = route.query.poolId
  if (poolId) {
    router.push({
      path: '/backlog',
      query: { poolId }
    })
  } else {
    // 如果没有poolId，返回通用backlog页面
    router.push('/backlog')
  }
}

// 监听结果揭示,触发动效
watch(() => session.value?.revealedAt, async (newVal, oldVal) => {
  if (newVal && !oldVal && !revealTriggered.value) {
    revealTriggered.value = true
    
    await nextTick()
    
    setTimeout(() => {
      const statisticsPanel = document.querySelector('.statistics-panel')
      const statValues = document.querySelectorAll('.stat-value')
      const minorityCards = document.querySelectorAll('.vote-card.minority')
      
      playRevealSequence(isConsensusResult.value, {
        statisticsPanel,
        statValues: Array.from(statValues),
        minorityCards: Array.from(minorityCards)
      })
    }, 1200)
  }
}, { immediate: true })

// 键盘导航支持
const handleKeydown = (e) => {
  // V键切换到估点模式
  if (e.key === 'v' || e.key === 'V') {
    if (currentScene.value === 'story-review' && session.value?.status === 'IN_PROGRESS') {
      switchToVoting()
    }
  }
  
  // S键切换到故事卡模式
  if (e.key === 's' || e.key === 'S') {
    if (currentScene.value === 'voting' && session.value?.status === 'IN_PROGRESS') {
      switchToStoryReview()
    }
  }
  
  // 在估点场景且未完成时,数字键选择点数
  if (currentScene.value === 'voting' && session.value?.status === 'IN_PROGRESS') {
    const keyToPoint = {
      '1': 1,
      '2': 2,
      '3': 3,
      '5': 5,
      '8': 8,
      '6': 13
    }
    
    if (keyToPoint[e.key]) {
      selectPoint(keyToPoint[e.key])
    }
    
    // Enter键确认投票
    if (e.key === 'Enter' && selectedPoint.value) {
      submitVote()
    }
  }
}

onMounted(async () => {
  loading.value = true
  await loadSession()
  loading.value = false
  
  pollInterval = setInterval(loadSession, 3000)
  document.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  if (pollInterval) {
    clearInterval(pollInterval)
  }
  document.removeEventListener('keydown', handleKeydown)
})
</script>

<style scoped>
.voting-container {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-primary);
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-content h2 {
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-primary);
}

.subtitle {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

/* ========== 内容区域 ========== */

.voting-content {
  min-height: calc(100vh - 140px);
}

/* ========== 场景切换动画 ========== */

.scene-flip-enter-active {
  animation: sceneFlipEnter 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

.scene-flip-leave-active {
  animation: sceneFlipLeave 0.8s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 状态显示区域 */
.status-display {
  animation: fadeIn 0.6s var(--ease-out);
  padding: 40px 20px;
}

.complete-section {
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-primary);
  max-width: 600px;
  margin: 0 auto;
}

/* 响应式 */
@media (max-width: 768px) {
  .header-content {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }

  .status-display {
    padding: 20px 0;
  }
}
</style>
