/**
 * API 服务封装
 * 统一管理后端API调用
 */
import axios, { type AxiosInstance } from 'axios'
import { message } from 'ant-design-vue'

// API Base URL（从环境变量读取，默认localhost:8000）
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8000/api'

// Chat相关类型定义
export interface ChatRequest {
  message: string
  file_id?: string
}

export interface ChatResponse {
  type: 'text' | 'sql'
  content: string
  sql?: string
  explanation?: string
  references?: Array<{
    table: string
    fields?: string[]
  }>
  intent?: string
}

// 创建Axios实例
const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000, // 60秒超时
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器（添加API Key等）
apiClient.interceptors.request.use(
  (config) => {
    // 从localStorage读取GLM API Key
    const apiKey = localStorage.getItem('glm_api_key')
    if (apiKey) {
      // 后端期望通过X-API-Key头部接收
      config.headers['X-API-Key'] = apiKey
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器（统一错误处理）
apiClient.interceptors.response.use(
  (response) => {
    return response
  },
  (error) => {
    console.error('API Error:', error)
    
    if (error.response) {
      // 服务器返回错误
      const status = error.response.status
      const data = error.response.data
      
      if (status === 401) {
        message.error('API Key 验证失败，请检查配置')
      } else if (status === 500) {
        message.error(`服务器错误：${data.detail || '未知错误'}`)
      } else {
        message.error(`请求失败：${data.detail || error.message}`)
      }
    } else if (error.request) {
      // 请求发出但没有响应
      message.error('无法连接到后端服务，请检查服务是否运行')
    } else {
      // 其他错误
      message.error(`请求配置错误：${error.message}`)
    }
    
    return Promise.reject(error)
  }
)

/**
 * Chat API Service
 */
export const chatService = {
  /**
   * 发送消息（SQL生成或普通对话）
   */
  async sendMessage(request: ChatRequest): Promise<ChatResponse> {
    const response = await apiClient.post<ChatResponse>('/chat', request)
    return response.data
  },

  /**
   * 获取对话历史
   */
  async getChatHistory() {
    const response = await apiClient.get('/chat/history')
    return response.data
  },
}

/**
 * File API Service（已有）
 */
export const fileService = {
  /**
   * 上传DDL文件
   */
  async uploadFile(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await apiClient.post('/files/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    })
    return response.data
  },

  /**
   * 获取文件列表
   */
  async getFiles() {
    const response = await apiClient.get('/files')
    return response.data
  },

  /**
   * 删除文件
   */
  async deleteFile(fileId: string) {
    await apiClient.delete(`/files/${fileId}`)
  },

  /**
   * 获取文件DDL详情
   */
  async getFileDDL(fileId: string) {
    const response = await apiClient.get(`/files/${fileId}/ddl`)
    return response.data
  },
}

/**
 * Settings API Service（可选，如果后端支持）
 */
export const settingsService = {
  /**
   * 测试API连接
   */
  async testConnection(): Promise<boolean> {
    try {
      await apiClient.get('/health')
      return true
    } catch {
      return false
    }
  },
}

export default apiClient
