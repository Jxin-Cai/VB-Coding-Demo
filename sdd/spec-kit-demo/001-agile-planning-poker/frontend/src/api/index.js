import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const api = axios.create({
  baseURL: '/api/v1',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('sessionToken')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
      config.headers['X-Session-Token'] = token
    }

    // 添加当前用户名到请求头
    const userStr = localStorage.getItem('user')
    if (userStr) {
      try {
        const user = JSON.parse(userStr)
        if (user && user.name) {
          config.headers['X-User-Name'] = user.name
          console.log('✓ 请求拦截器已添加 X-User-Name:', user.name)
        } else {
          console.warn('⚠ 用户对象存在但没有 name 字段:', user)
          console.warn('⚠ localStorage user:', userStr)
        }
      } catch (e) {
        console.error('✗ 解析用户信息失败:', e)
        console.error('✗ localStorage user string:', userStr)
      }
    } else {
      console.warn('⚠ localStorage 中没有 user 对象，无法添加 X-User-Name header')
      console.warn('⚠ 请检查是否已登录，或重新登录')
    }

    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
api.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    console.error('API响应错误:', error)

    // 不在这里显示错误消息，让组件自己处理
    // 这样可以避免重复显示错误，也让错误处理更灵活

    // 特殊处理：401未授权，需要清除登录状态并跳转
    if (error.response && error.response.status === 401) {
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('sessionToken')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }

    return Promise.reject(error)
  }
)

// API 方法
export default {
  // 用户登录
  login(name) {
    return api.post('/participants/login', { name })
  },

  // 故事卡管理
  getStoryCards(sortBy = 'id', sortOrder = 'desc') {
    return api.get('/story-cards', {
      params: { sortBy, sortOrder }
    })
  },

  createStoryCard(data) {
    return api.post('/story-cards', data)
  },

  updateStoryCard(id, data) {
    return api.put(`/story-cards/${id}`, data)
  },

  deleteStoryCard(id) {
    return api.delete(`/story-cards/${id}`)
  },

  // 估点会话
  startVotingSession(storyCardId) {
    return api.post('/voting-sessions/start', { storyCardId })
  },

  // 加入估点会话
  joinVotingSession(storyCardId) {
    return api.post(`/voting-sessions/join/${storyCardId}`)
  },

  getVotingSession(storyCardId) {
    return api.get(`/voting-sessions/${storyCardId}`)
  },

  submitVote(storyCardId, storyPoint) {
    return api.post(`/voting-sessions/${storyCardId}/votes`, { storyPoint })
  },

  cancelSession(storyCardId) {
    return api.post(`/voting-sessions/${storyCardId}/cancel`)
  },

  completeSession(storyCardId, finalStoryPoint) {
    return api.post(`/voting-sessions/${storyCardId}/complete`, { finalStoryPoint })
  },

  // 需求池管理
  getPools() {
    return api.get('/pools')
  },

  getPoolById(id) {
    return api.get(`/pools/${id}`)
  },

  createPool(data) {
    return api.post('/pools', data)
  },

  getPoolStoryCards(poolId) {
    return api.get(`/pools/${poolId}/story-cards`)
  }
}
