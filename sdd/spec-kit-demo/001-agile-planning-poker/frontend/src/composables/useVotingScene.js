import { ref } from 'vue'

/**
 * 投票场景状态管理
 * 管理故事卡讲解场景和估点场景的切换
 */
export function useVotingScene() {
  // 当前场景: 'story-review' | 'voting'
  const currentScene = ref('story-review')
  
  // 是否正在场景切换中
  const isTransitioning = ref(false)
  
  /**
   * 切换到估点场景
   */
  const switchToVoting = () => {
    if (isTransitioning.value || currentScene.value === 'voting') return
    
    isTransitioning.value = true
    
    // 播放过渡动画(0.8s)
    setTimeout(() => {
      currentScene.value = 'voting'
      isTransitioning.value = false
    }, 800)
  }
  
  /**
   * 切换到故事卡讲解场景
   */
  const switchToStoryReview = () => {
    if (isTransitioning.value || currentScene.value === 'story-review') return
    
    isTransitioning.value = true
    
    // 播放过渡动画(0.8s)
    setTimeout(() => {
      currentScene.value = 'story-review'
      isTransitioning.value = false
    }, 800)
  }
  
  return {
    currentScene,
    isTransitioning,
    switchToVoting,
    switchToStoryReview
  }
}
