import confetti from 'canvas-confetti'

/**
 * 结果揭示动效
 * 处理一致/不一致场景的庆祝和提示动效
 */
export function useRevealEffects() {
  
  /**
   * 播放一致场景庆祝动效
   * - 礼花粒子效果
   * - 光效扫过
   */
  const playConsensusEffect = () => {
    // 礼花效果 - 从中心向四周扩散
    confetti({
      particleCount: 100,
      spread: 70,
      origin: { y: 0.6 },
      colors: ['#00ff88', '#00d4ff', '#ff0088'],
      startVelocity: 45,
      gravity: 1.2,
      ticks: 200
    })

    // 0.3秒后再来一波
    setTimeout(() => {
      confetti({
        particleCount: 50,
        angle: 60,
        spread: 55,
        origin: { x: 0 },
        colors: ['#00ff88', '#00d4ff']
      })
    }, 300)

    setTimeout(() => {
      confetti({
        particleCount: 50,
        angle: 120,
        spread: 55,
        origin: { x: 1 },
        colors: ['#ff0088', '#00d4ff']
      })
    }, 300)
  }

  /**
   * 播放数字弹出动画
   * @param {HTMLElement} element - 目标元素
   */
  const playNumberPopEffect = (element) => {
    if (!element) return

    element.style.transition = 'transform 0.6s cubic-bezier(0.34, 1.56, 0.64, 1)'
    element.style.transform = 'scale(1.2)'
    
    setTimeout(() => {
      element.style.transform = 'scale(1)'
    }, 600)
  }

  /**
   * 触发光效扫过动画
   * @param {HTMLElement} element - 目标元素
   */
  const playGlowSweep = (element) => {
    if (!element) return

    element.classList.add('glow-sweep-active')
    
    setTimeout(() => {
      element.classList.remove('glow-sweep-active')
    }, 1000)
  }

  /**
   * 标记少数意见卡片
   * @param {Array} minorityCards - 少数意见卡片元素数组
   */
  const highlightMinorityCards = (minorityCards) => {
    if (!minorityCards || minorityCards.length === 0) return

    minorityCards.forEach((card, index) => {
      setTimeout(() => {
        card.classList.add('minority-highlight')
      }, index * 150)
    })
  }

  /**
   * 播放卡片震动动画
   * @param {Array} cardElements - 卡片元素数组
   */
  const playCardShake = (cardElements) => {
    if (!cardElements || cardElements.length === 0) return

    cardElements.forEach((card, index) => {
      setTimeout(() => {
        card.style.animation = 'shake 0.3s ease-in-out'
        setTimeout(() => {
          card.style.animation = ''
        }, 300)
      }, index * 50)
    })
  }

  /**
   * 完整的结果揭示动效序列
   * @param {Boolean} isConsensus - 是否一致
   * @param {Object} elements - 需要动画的元素集合
   */
  const playRevealSequence = (isConsensus, elements = {}) => {
    const { 
      statisticsPanel, 
      statValues, 
      minorityCards 
    } = elements

    if (isConsensus) {
      // 一致场景动效
      setTimeout(() => {
        playConsensusEffect()
      }, 100)

      if (statisticsPanel) {
        setTimeout(() => {
          playGlowSweep(statisticsPanel)
        }, 500)
      }

      if (statValues && statValues.length > 0) {
        setTimeout(() => {
          statValues.forEach((el, index) => {
            setTimeout(() => {
              playNumberPopEffect(el)
            }, index * 150)
          })
        }, 800)
      }
    } else {
      // 不一致场景动效
      if (minorityCards && minorityCards.length > 0) {
        setTimeout(() => {
          highlightMinorityCards(minorityCards)
        }, 500)
      }
    }
  }

  return {
    playConsensusEffect,
    playNumberPopEffect,
    playGlowSweep,
    highlightMinorityCards,
    playCardShake,
    playRevealSequence
  }
}
