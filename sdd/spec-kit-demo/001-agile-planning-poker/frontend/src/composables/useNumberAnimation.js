/**
 * 数字滚动动画 Composable
 * 
 * 用于统计数字从旧值到新值的平滑过渡动画
 */

import { ref, watch } from 'vue'

/**
 * 数字动画工具函数
 * @param {number} start - 起始值
 * @param {number} end - 结束值
 * @param {number} duration - 动画时长(ms)
 * @param {function} callback - 更新回调
 * @param {function} easing - 缓动函数
 */
function animateNumber(start, end, duration, callback, easing = easeOutCubic) {
  const startTime = Date.now()
  const change = end - start

  function update() {
    const currentTime = Date.now()
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easedProgress = easing(progress)
    const current = start + change * easedProgress

    callback(current)

    if (progress < 1) {
      requestAnimationFrame(update)
    } else {
      callback(end) // 确保最终值精确
    }
  }

  requestAnimationFrame(update)
}

/**
 * 缓动函数: easeOutCubic
 * @param {number} t - 进度 (0-1)
 * @returns {number} 缓动后的进度
 */
function easeOutCubic(t) {
  return 1 - Math.pow(1 - t, 3)
}

/**
 * 缓动函数: easeOutQuad
 * @param {number} t - 进度 (0-1)
 * @returns {number} 缓动后的进度
 */
function easeOutQuad(t) {
  return t * (2 - t)
}

/**
 * 使用数字动画
 * @param {Ref<number>} source - 源数值的ref
 * @param {Object} options - 配置选项
 * @returns {Object} 包含动画值和控制方法
 */
export function useNumberAnimation(source, options = {}) {
  const {
    duration = 800, // 默认动画时长
    decimals = 0,   // 小数位数
    easing = easeOutCubic // 缓动函数
  } = options

  const displayValue = ref(0)
  const isAnimating = ref(false)

  // 监听源数值变化
  watch(
    source,
    (newVal, oldVal) => {
      if (newVal === oldVal || newVal === null || newVal === undefined) {
        return
      }

      const start = oldVal || 0
      const end = newVal

      isAnimating.value = true

      animateNumber(
        start,
        end,
        duration,
        (current) => {
          displayValue.value = decimals > 0 
            ? current.toFixed(decimals)
            : Math.round(current)
        },
        easing
      )

      // 动画结束
      setTimeout(() => {
        isAnimating.value = false
      }, duration)
    },
    { immediate: true }
  )

  return {
    displayValue,
    isAnimating
  }
}

/**
 * 使用多个数字动画
 * @param {Object} sources - 源数值的ref对象
 * @param {Object} options - 配置选项
 * @returns {Object} 包含所有动画值
 */
export function useMultipleNumberAnimations(sources, options = {}) {
  const animations = {}

  Object.keys(sources).forEach(key => {
    animations[key] = useNumberAnimation(sources[key], options[key] || options)
  })

  return animations
}

export default {
  animateNumber,
  easeOutCubic,
  easeOutQuad,
  useNumberAnimation,
  useMultipleNumberAnimations
}
