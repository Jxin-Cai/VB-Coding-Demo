/**
 * 文件管理 Store
 * 管理 DDL 文件列表和当前激活的文件
 */
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import axios from 'axios'

export interface FileItem {
  file_id: string
  filename: string
  status: string
  uploaded_at: string
  file_size: number
  table_count?: number
  column_count?: number
  embedding_count?: number
  error_message?: string
  tables?: any[]
}

export const useFileStore = defineStore('file', () => {
  // State
  const files = ref<FileItem[]>([])
  const currentFileId = ref<string | null>(null)
  const loading = ref(false)

  // Computed
  const currentFile = computed(() => {
    if (!currentFileId.value) return null
    return files.value.find(f => f.file_id === currentFileId.value) || null
  })

  const readyFiles = computed(() => {
    return files.value.filter(f => f.status === 'ready')
  })

  // Actions

  /**
   * 添加文件到列表
   */
  function addFile(file: FileItem) {
    files.value.unshift(file)
    
    // 如果是第一个文件，自动设为当前
    if (files.value.length === 1) {
      setCurrentFile(file.file_id)
    }
  }

  /**
   * 更新文件状态
   */
  function updateFile(fileId: string, updates: Partial<FileItem>) {
    const index = files.value.findIndex(f => f.file_id === fileId)
    if (index === -1) return
    
    const existingFile = files.value[index]!
    files.value[index] = { 
      ...existingFile, 
      ...updates,
      file_id: existingFile.file_id, // 确保 file_id 不变
      filename: existingFile.filename, // 确保 filename 不变
      status: updates.status || existingFile.status,
      uploaded_at: existingFile.uploaded_at,
      file_size: existingFile.file_size,
    }
  }

  /**
   * 设置当前激活的文件
   */
  function setCurrentFile(fileId: string) {
    currentFileId.value = fileId
    console.log(`Current file set to: ${fileId}`)
  }

  /**
   * 删除文件
   */
  async function deleteFile(fileId: string) {
    try {
      await axios.delete(`/api/files/${fileId}`)
      
      // 从列表中移除
      const index = files.value.findIndex(f => f.file_id === fileId)
      if (index !== -1) {
        files.value.splice(index, 1)
      }
      
      // 如果删除的是当前文件，切换到第一个可用文件
      if (currentFileId.value === fileId) {
        const firstReadyFile = readyFiles.value[0]
        if (firstReadyFile) {
          setCurrentFile(firstReadyFile.file_id)
        } else {
          currentFileId.value = null
        }
      }
      
      return true
    } catch (error) {
      console.error('Delete file error:', error)
      return false
    }
  }

  /**
   * 刷新文件状态
   */
  async function refreshFile(fileId: string) {
    try {
      const response = await axios.get(`/api/files/${fileId}`)
      updateFile(fileId, response.data)
    } catch (error) {
      console.error('Refresh file error:', error)
    }
  }

  /**
   * 刷新所有文件
   */
  async function refreshAllFiles() {
    loading.value = true
    
    try {
      const promises = files.value.map(file => 
        axios.get(`/api/files/${file.file_id}`)
      )
      
      const responses = await Promise.all(promises)
      files.value = responses.map(r => r.data)
    } catch (error) {
      console.error('Refresh all files error:', error)
    } finally {
      loading.value = false
    }
  }

  return {
    // State
    files,
    currentFileId,
    loading,
    
    // Computed
    currentFile,
    readyFiles,
    
    // Actions
    addFile,
    updateFile,
    setCurrentFile,
    deleteFile,
    refreshFile,
    refreshAllFiles
  }
})
