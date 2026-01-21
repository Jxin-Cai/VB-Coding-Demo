import { ref, onMounted } from 'vue'

/**
 * 性能检测和优化
 * 检测设备性能并应用降级策略
 */
export function usePerformance() {
  const isLowPerformance = ref(false)
  const reducedAnimations = ref(false)

  /**
   * 检测设备性能
   */
  const detectPerformance = () => {
    // 检测CPU核心数
    const cores = navigator.hardwareConcurrency || 2
    
    // 检测是否为移动设备或低端设备
    const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)
    const isOldAndroid = /Android [4-6]/i.test(navigator.userAgent)
    
    // 判断是否为低端设备
    isLowPerformance.value = cores < 4 || isOldAndroid
    
    // 检测用户偏好
    const prefersReducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches
    
    reducedAnimations.value = isLowPerformance.value || prefersReducedMotion
    
    // 应用降级类
    if (reducedAnimations.value) {
      document.body.classList.add('reduced-animations')
    } else {
      document.body.classList.remove('reduced-animations')
    }
  }

  /**
   * 获取推荐的粒子数量
   */
  const getRecommendedParticleCount = () => {
    if (isLowPerformance.value) {
      return 30 // 低端设备减少粒子
    }
    return 80 // 正常设备
  }

  /**
   * 优化动画性能
   * @param {HTMLElement} element - 需要动画的元素
   */
  const optimizeAnimation = (element) => {
    if (!element) return

    // 添加will-change提示
    element.style.willChange = 'transform, opacity'
    
    // 触发硬件加速
    element.style.transform = 'translateZ(0)'
    element.style.backfaceVisibility = 'hidden'
  }

  /**
   * 清理动画优化
   * @param {HTMLElement} element
   */
  const cleanupAnimation = (element) => {
    if (!element) return

    element.style.willChange = 'auto'
  }

  /**
   * 监听页面可见性,暂停不可见时的动画
   */
  const setupVisibilityListener = (pauseCallback, resumeCallback) => {
    const handleVisibilityChange = () => {
      if (document.hidden) {
        pauseCallback?.()
      } else {
        resumeCallback?.()
      }
    }

    document.addEventListener('visibilitychange', handleVisibilityChange)

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange)
    }
  }

  onMounted(() => {
    detectPerformance()
  })

  return {
    isLowPerformance,
    reducedAnimations,
    detectPerformance,
    getRecommendedParticleCount,
    optimizeAnimation,
    cleanupAnimation,
    setupVisibilityListener
  }
}
