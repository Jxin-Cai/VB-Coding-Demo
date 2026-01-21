/**
 * 统计计算工具
 * 用于计算投票结果的统计数据和分布
 */

/**
 * 计算点数分布统计
 * @param {Array} votes - 投票数据数组
 * @param {Number} totalParticipants - 总参与人数
 * @returns {Array} 点数分布数据,按人数降序排列
 */
export function computeDistribution(votes, totalParticipants = 0) {
  if (!votes || votes.length === 0) {
    return []
  }

  // 按storyPoint分组统计
  const groupMap = new Map()
  
  votes.forEach(vote => {
    const point = vote.storyPoint
    if (!groupMap.has(point)) {
      groupMap.set(point, {
        storyPoint: point,
        count: 0,
        participants: []
      })
    }
    
    const group = groupMap.get(point)
    group.count++
    group.participants.push(vote.participantName)
  })

  // 转换为数组并计算百分比
  const total = votes.length
  const distribution = Array.from(groupMap.values()).map(group => {
    const percentage = Math.round((group.count / total) * 100)
    
    // 识别少数意见(人数 ≤ 总人数的20%)
    const minorityThreshold = Math.ceil(total * 0.2)
    const isMinority = group.count <= minorityThreshold && group.count < (total / 2)
    
    return {
      ...group,
      percentage,
      isMinority
    }
  })

  // 按人数降序排列
  distribution.sort((a, b) => b.count - a.count)
  
  return distribution
}

/**
 * 检查投票结果是否一致(所有人投了相同点数)
 * @param {Array} distribution - 点数分布数据
 * @returns {Boolean}
 */
export function isConsensus(distribution) {
  if (!distribution || distribution.length === 0) {
    return false
  }
  
  return distribution.length === 1
}

/**
 * 获取少数意见者
 * @param {Array} distribution - 点数分布数据
 * @returns {Array} 少数意见的点数分组
 */
export function getMinorityOpinions(distribution) {
  if (!distribution) return []
  return distribution.filter(group => group.isMinority)
}

/**
 * 计算统计摘要
 * @param {Object} session - 会话数据
 * @returns {Object} 统计摘要
 */
export function computeStatsSummary(session) {
  if (!session || !session.votes || session.votes.length === 0) {
    return {
      average: 0,
      max: 0,
      min: 0,
      median: 0,
      votedCount: 0,
      totalCount: session?.totalParticipants || 0
    }
  }

  const points = session.votes.map(v => v.storyPoint).sort((a, b) => a - b)
  const sum = points.reduce((acc, p) => acc + p, 0)
  const average = sum / points.length
  const max = Math.max(...points)
  const min = Math.min(...points)
  
  // 计算中位数
  const mid = Math.floor(points.length / 2)
  const median = points.length % 2 === 0
    ? (points[mid - 1] + points[mid]) / 2
    : points[mid]

  return {
    average: parseFloat(average.toFixed(1)),
    max,
    min,
    median: parseFloat(median.toFixed(1)),
    votedCount: session.votes.length,
    totalCount: session.totalParticipants || session.participants?.length || 0
  }
}

/**
 * 获取投票进度百分比
 * @param {Number} votedCount - 已投票人数
 * @param {Number} totalCount - 总人数
 * @returns {Number} 百分比(0-100)
 */
export function getVotingProgress(votedCount, totalCount) {
  if (!totalCount || totalCount === 0) return 0
  return Math.round((votedCount / totalCount) * 100)
}
