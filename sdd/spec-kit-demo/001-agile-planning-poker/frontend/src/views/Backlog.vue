<template>
  <div class="backlog-container">
    <!-- 需求池上下文 -->
    <div class="pool-context" v-if="poolInfo">
      <el-button @click="router.push('/pools')" class="back-button" size="large">
        <el-icon><ArrowLeft /></el-icon>
        返回需求池列表
      </el-button>
      <h2>
        <el-icon><Folder /></el-icon>
        {{ poolInfo.name }}
      </h2>
      <p v-if="poolInfo.description" class="pool-description">{{ poolInfo.description }}</p>
    </div>

    <el-card class="page-header" shadow="never">
      <div class="header-content">
        <div>
          <h2><el-icon><List /></el-icon> 故事卡列表</h2>
          <p class="subtitle">管理待估点的用户故事</p>
        </div>
        <el-button type="primary" size="large" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          创建故事卡
        </el-button>
      </div>
    </el-card>

    <!-- 故事卡列表 -->
    <el-card class="story-list" v-loading="loading">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <span class="total-count">共 {{ totalCount }} 张故事卡</span>
        </div>
        <div class="toolbar-right">
          <el-select 
            v-model="statusFilter" 
            placeholder="状态筛选" 
            style="width: 140px; margin-right: 10px"
            @change="handleFilterChange"
          >
            <el-option label="全部状态" value="all" />
            <el-option label="待估点" value="NOT_ESTIMATED" />
            <el-option label="估点中" value="ESTIMATING" />
            <el-option label="已估点" value="ESTIMATED" />
          </el-select>
          <el-select 
            v-model="sortBy" 
            placeholder="排序字段" 
            style="width: 140px; margin-right: 10px"
            @change="handleSortChange"
          >
            <el-option label="按ID排序" value="id" />
            <el-option label="按创建时间" value="createdAt" />
            <el-option label="按状态排序" value="status" />
          </el-select>
          <el-select 
            v-model="sortOrder" 
            placeholder="排序方向" 
            style="width: 120px"
            @change="handleSortChange"
          >
            <el-option label="降序" value="desc" />
            <el-option label="升序" value="asc" />
          </el-select>
        </div>
      </div>

      <el-table 
        :data="paginatedCards" 
        style="width: 100%"
        :row-class-name="getRowClassName"
      >
        <el-table-column prop="id" label="ID" width="80" sortable />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="description" label="描述" min-width="300" show-overflow-tooltip />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="故事点" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.storyPoint" type="success" effect="dark">
              {{ scope.row.storyPoint }} 点
            </el-tag>
            <span v-else style="color: #909399">-</span>
          </template>
        </el-table-column>
        <el-table-column label="主持人" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="isHost(scope.row)" type="success" effect="dark">
              <el-icon><User /></el-icon> {{ scope.row.hostName }}
            </el-tag>
            <span v-else style="color: #a0a0a0">{{ scope.row.hostName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <!-- 开始估点按钮：所有人在NOT_ESTIMATED状态时都可以开始 -->
            <el-button
              v-if="scope.row.status === 'NOT_ESTIMATED'"
              type="primary"
              size="small"
              @click="startVoting(scope.row)"
            >
              <el-icon><VideoPlay /></el-icon>
              开始估点
            </el-button>

            <!-- 加入估点按钮：所有人在ESTIMATING状态时显示 -->
            <el-button
              v-if="scope.row.status === 'ESTIMATING'"
              type="success"
              size="small"
              @click="joinVoting(scope.row)"
              :loading="loading && currentCard?.id === scope.row.id"
            >
              <el-icon><VideoPlay /></el-icon>
              加入估点
            </el-button>

            <!-- 编辑按钮：所有人可见 -->
            <el-button
              size="small"
              @click="editStoryCard(scope.row)"
            >
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>

            <!-- 删除按钮：所有人可见 -->
            <el-button
              type="danger"
              size="small"
              @click="deleteStoryCard(scope.row)"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-if="totalCount > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="totalCount"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />

      <div v-if="!loading && storyCards.length === 0" class="empty-state">
        <el-empty description="暂无故事卡">
          <el-button type="primary" @click="showCreateDialog">创建第一个故事卡</el-button>
        </el-empty>
      </div>
    </el-card>

    <!-- 创建/编辑/查看对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="getDialogTitle()"
      width="600px"
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="例如：用户登录功能"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="详细描述用户故事的内容和验收标准"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ isViewMode ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!isViewMode" type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onActivated } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { List, Plus, VideoPlay, Edit, Delete, User, ArrowLeft, Folder, View } from '@element-plus/icons-vue'
import api from '../api'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 从路由参数获取poolId
const poolId = computed(() => {
  return route.query.poolId ? Number(route.query.poolId) : null
})

const loading = ref(false)
const submitting = ref(false)
const storyCards = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const isView = ref(false) // 是否为查看模式
const currentEditId = ref(null)
const formRef = ref(null)
const poolInfo = ref(null)
const statusFilter = ref('all')

// 是否为查看模式
const isViewMode = computed(() => isView.value)

// 获取当前用户 - 从 store 而不是 localStorage
const currentUser = computed(() => {
  const userName = userStore.currentUser?.name || ''
  console.log('当前用户:', userName)
  return userName
})

// 分页和排序
const currentPage = ref(1)
const pageSize = ref(20)
const sortBy = ref('id')
const sortOrder = ref('desc')

const form = ref({
  title: '',
  description: ''
})

const formRules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度在 10 到 500 个字符', trigger: 'blur' }
  ]
}

// 计算属性
const filteredCards = computed(() => {
  if (statusFilter.value === 'all') {
    return storyCards.value
  }
  return storyCards.value.filter(card => card.status === statusFilter.value)
})

const totalCount = computed(() => filteredCards.value.length)

const paginatedCards = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredCards.value.slice(start, end)
})

// 加载需求池信息
const loadPoolInfo = async () => {
  try {
    const response = await api.getPoolById(poolId.value)
    if (response.code === 200) {
      poolInfo.value = response.data
    }
  } catch (error) {
    console.error('加载需求池信息失败:', error)
    ElMessage.error(error.message || '加载需求池信息失败')
  }
}

const loadStoryCards = async () => {
  loading.value = true
  try {
    // 使用API实例获取需求池的故事卡
    const response = await api.getPoolStoryCards(poolId.value)
    console.log('故事卡列表响应:', response)
    if (response.code === 200) {
      storyCards.value = response.data || []
      console.log('加载到故事卡:', storyCards.value.length, '张')
      console.log('当前用户:', currentUser.value)

      // 输出每张卡的详细信息用于调试
      storyCards.value.forEach((card, index) => {
        console.log(`卡片${index + 1}: id=${card.id}, title="${card.title}", hostName="${card.hostName}", status=${card.status}`)
      })
    } else {
      storyCards.value = []
      ElMessage.error(response.message || '加载故事卡失败')
    }
    // 重置到第一页
    if (currentPage.value > Math.ceil(totalCount.value / pageSize.value)) {
      currentPage.value = 1
    }
  } catch (error) {
    console.error('加载故事卡失败:', error)
    ElMessage.error(error.message || '加载失败，请刷新重试')
    storyCards.value = []
  } finally {
    loading.value = false
  }
}

// 检查是否为主持人
const isHost = (card) => {
  if (!card || !card.hostName) {
    console.warn('故事卡缺少hostName:', card)
    return false
  }
  if (!currentUser.value) {
    console.warn('当前用户为空')
    return false
  }
  const result = card.hostName === currentUser.value
  console.log(`isHost检查: card.hostName="${card.hostName}", currentUser="${currentUser.value}", result=${result}`)
  return result
}

const handleSortChange = () => {
  currentPage.value = 1
  loadStoryCards()
}

const handleFilterChange = () => {
  currentPage.value = 1
  // 前端筛选，不重新加载
}

const handlePageChange = (page) => {
  currentPage.value = page
}

const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

const showCreateDialog = () => {
  isEdit.value = false
  isView.value = false
  currentEditId.value = null
  form.value = { title: '', description: '' }
  dialogVisible.value = true
}

// 获取对话框标题
const getDialogTitle = () => {
  if (isViewMode.value) return '查看故事卡详情'
  if (isEdit.value) return '编辑故事卡'
  return '创建故事卡'
}

const editStoryCard = (row) => {
  isEdit.value = true
  isView.value = false
  currentEditId.value = row.id
  form.value = {
    title: row.title,
    description: row.description
  }
  dialogVisible.value = true
}

// 查看故事卡详情（只读）
const viewStoryCardDetails = (row) => {
  isEdit.value = false
  isView.value = true
  currentEditId.value = null
  form.value = {
    title: row.title,
    description: row.description
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true
      try {
        if (isEdit.value) {
          await api.updateStoryCard(currentEditId.value, form.value)
          ElMessage.success('更新成功')
        } else {
          // 创建时包含poolId和createdBy
          console.log('创建故事卡 - userStore:', userStore)
          console.log('创建故事卡 - currentUser:', userStore.currentUser)
          console.log('创建故事卡 - 当前用户名:', currentUser.value)
          console.log('创建故事卡 - poolId:', poolId.value)

          // 确保获取用户名，多个后备方案
          let userName = currentUser.value
          if (!userName) {
            // 后备方案1：直接从store获取
            userName = userStore.currentUser?.name || ''
          }
          if (!userName) {
            // 后备方案2：从localStorage获取
            const userStr = localStorage.getItem('user')
            if (userStr) {
              try {
                const userObj = JSON.parse(userStr)
                userName = userObj.name || ''
              } catch (e) {
                console.error('解析用户信息失败:', e)
              }
            }
          }
          if (!userName) {
            userName = '匿名用户'
          }

          console.log('创建故事卡 - 最终使用的用户名:', userName)

          await api.createStoryCard({
            title: form.value.title,
            description: form.value.description,
            poolId: poolId.value,
            createdBy: userName // 使用多重后备方案获取的用户名
          })
          ElMessage.success('创建成功，正在刷新列表...')
        }
        dialogVisible.value = false
        // 等待一小段时间确保后端保存完成
        await new Promise(resolve => setTimeout(resolve, 100))
        await loadStoryCards()
      } catch (error) {
        console.error('操作失败:', error)
        ElMessage.error(error.message || '操作失败')
      } finally {
        submitting.value = false
      }
    }
  })
}

const deleteStoryCard = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除故事卡 "${row.title}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await api.deleteStoryCard(row.id)
    ElMessage.success('删除成功')
    loadStoryCards()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
    }
  }
}

// 当前操作的卡片（用于loading状态）
const currentCard = ref(null)

const startVoting = async (row) => {
  try {
    const response = await api.startVotingSession(row.id)
    console.log('创建会话响应:', response)

    // 响应拦截器返回 response.data，所以这里 response 就是 ResponseDTO
    // ResponseDTO.data 包含 VotingSessionDTO，其中 id 就是 storyCardId（1:1关系）
    const storyCardId = response.data?.id

    if (!storyCardId) {
      console.error('未获取到 storyCardId，完整响应:', response)
      ElMessage.error('创建会话失败：未获取到会话ID')
      return
    }

    ElMessage.success('估点会话已创建')

    // 传递poolId到voting页面，以便返回时保留上下文
    const poolId = route.query.poolId
    router.push({
      path: `/voting/${storyCardId}`,
      query: poolId ? { poolId } : undefined
    })
  } catch (error) {
    console.error('创建会话失败:', error)
    ElMessage.error(error.response?.data?.message || '创建会话失败')
  }
}

// 加入估点会话
const joinVoting = async (row) => {
  try {
    loading.value = true
    currentCard.value = row
    
    // 调用加入会话API
    const response = await api.joinVotingSession(row.id)
    console.log('加入会话响应:', response)
    
    if (response.code === 200 && response.data?.id) {
      const storyCardId = response.data.id
      ElMessage.success('成功加入估点会话！')

      // 跳转到投票页面，传递poolId以便返回时保留上下文
      const poolId = route.query.poolId
      router.push({
        path: `/voting/${storyCardId}`,
        query: poolId ? { poolId } : undefined
      })
    } else {
      ElMessage.error('加入估点失败')
    }
  } catch (error) {
    console.error('加入估点失败:', error)
    ElMessage.error(error.response?.data?.message || '加入估点失败')
  } finally {
    loading.value = false
    currentCard.value = null
  }
}

const getStatusType = (status) => {
  const typeMap = {
    'NOT_ESTIMATED': 'info',
    'ESTIMATING': 'warning',
    'ESTIMATED': 'success'
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = {
    'NOT_ESTIMATED': '待估点',
    'ESTIMATING': '估点中',
    'ESTIMATED': '已估点'
  }
  return textMap[status] || status
}

const getRowClassName = ({ row }) => {
  if (row.status === 'ESTIMATED') return 'row-estimated'
  if (row.status === 'ESTIMATING') return 'row-voting'
  return ''
}

onMounted(async () => {
  // 调试：检查用户状态
  console.log('Backlog onMounted - userStore:', userStore)
  console.log('Backlog onMounted - currentUser:', userStore.currentUser)
  console.log('Backlog onMounted - 当前用户名:', currentUser.value)

  await loadPoolInfo()
  await loadStoryCards()
})

// 当路由激活时刷新（从其他页面返回时）
onActivated(() => {
  loadStoryCards()
})
</script>

<style scoped>
.pool-context {
  margin-bottom: 20px;
  padding: 16px 20px;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-primary);
  border-radius: 8px;
  display: flex;
  align-items: center;
  gap: 20px;
}

.back-button {
  background: var(--color-accent-blue) !important;
  color: white !important;
  border: none !important;
  font-weight: 600;
  padding: 12px 20px;
  transition: all 0.3s ease;
}

.back-button:hover {
  background: var(--color-accent-green) !important;
  transform: translateX(-3px);
}

.pool-context h2 {
  margin: 0;
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-primary);
  font-size: 20px;
}

.pool-description {
  margin: 0;
  color: var(--color-text-secondary);
  font-size: 14px;
  flex: 2;
}

.backlog-container {
  max-width: 1400px;
  margin: 0 auto;
  animation: fadeInUp var(--duration-slow) var(--ease-out);
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

.story-list {
  min-height: 400px;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-primary);
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 10px 0;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.total-count {
  font-size: 14px;
  color: var(--color-text-primary);
  font-weight: 500;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

/* 表格行高亮 - 黑色主题版本 */
:deep(.row-estimated) {
  background-color: rgba(0, 255, 136, 0.05);
}

:deep(.row-voting) {
  background-color: rgba(255, 152, 0, 0.05);
}

/* 表格黑色主题 */
:deep(.el-table) {
  background-color: transparent;
  color: var(--color-text-primary);
}

:deep(.el-table tr) {
  background-color: transparent;
}

:deep(.el-table th.el-table__cell) {
  background-color: var(--color-bg-tertiary);
  color: var(--color-text-primary);
  border-color: var(--color-border-primary);
}

:deep(.el-table td.el-table__cell) {
  border-color: var(--color-border-primary);
  color: var(--color-text-primary);
}

:deep(.el-table__body tr:hover > td) {
  background-color: var(--color-bg-tertiary) !important;
}

:deep(.el-table__empty-text) {
  color: var(--color-text-secondary);
}

/* 分页器黑色主题 */
:deep(.el-pagination) {
  display: flex;
  justify-content: flex-end;
}

:deep(.el-pagination button),
:deep(.el-pager li) {
  background-color: var(--color-bg-tertiary);
  color: var(--color-text-primary);
}

:deep(.el-pager li.is-active) {
  background-color: var(--color-accent-blue);
  color: white;
}

/* Select 下拉框黑色主题 */
:deep(.el-select .el-input__wrapper) {
  background-color: var(--color-bg-tertiary);
  border-color: var(--color-border-primary);
}

:deep(.el-select .el-input__inner) {
  color: var(--color-text-primary);
}
</style>
