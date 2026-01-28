/**
 * 类型定义
 * RAG Text-to-SQL 系统
 */

/**
 * 文件信息
 */
export interface FileItem {
  file_id: string
  filename: string
  status: 'pending' | 'parsing' | 'ready' | 'error'
  uploaded_at: string
  file_size: number
  table_count?: number
  column_count?: number
  embedding_count?: number
  error_message?: string
  tables?: TableInfo[]
}

/**
 * 表信息
 */
export interface TableInfo {
  name: string
  column_count: number
  primary_keys: string[]
  columns?: ColumnInfo[]
}

/**
 * 字段信息
 */
export interface ColumnInfo {
  name: string
  type: string
  nullable: boolean
  default_value?: string
  comment?: string
}

/**
 * 消息类型
 */
export interface Message {
  id: number
  type: 'user' | 'assistant' | 'system'
  content: string
  sql?: string
  references?: string[]
  intent?: 'new' | 'optimize'
  version?: number
  steps?: AgentStep[]
  timestamp: Date
}

/**
 * Agent 工作步骤
 */
export interface AgentStep {
  text: string
  loading: boolean
  completed: boolean
}

/**
 * SQL 历史项
 */
export interface SQLHistoryItem {
  id: string
  query: string
  sql: string
  status: 'success' | 'error' | 'pending'
  timestamp: Date
  references?: string[]
}

/**
 * 文件上传响应
 */
export interface FileUploadResponse {
  file_id: string
  filename: string
  status: string
}
