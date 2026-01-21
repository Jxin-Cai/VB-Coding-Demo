<template>
  <div class="pool-list-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <h1>
          <el-icon><Menu /></el-icon>
          需求池管理
        </h1>
        <p class="subtitle">选择一个需求池查看故事卡</p>
      </div>
      <el-button type="primary" size="large" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        创建需求池
      </el-button>
    </div>

    <!-- 需求池卡片网格 -->
    <div v-loading="loading" class="pools-grid">
      <div
        v-for="pool in pools"
        :key="pool.id"
        class="pool-card"
        @click="enterPool(pool.id)"
      >
        <div class="pool-card-header">
          <h3>{{ pool.name }}</h3>
          <el-tag :type="getPoolTagType(pool.storyCardCount)">
            {{ pool.storyCardCount || 0 }} 张卡
          </el-tag>
        </div>
        
        <p class="pool-description">
          {{ pool.description || '暂无描述' }}
        </p>
        
        <div class="pool-card-footer">
          <div class="creator-info">
            <el-icon><User /></el-icon>
            <span>{{ pool.createdBy }}</span>
          </div>
          <div class="create-time">
            <el-icon><Clock /></el-icon>
            <span>{{ formatRelativeTime(pool.createdAt) }}</span>
          </div>
        </div>
      </div>
      
      <!-- 空状态 -->
      <div v-if="pools.length === 0 && !loading" class="empty-state">
        <el-empty description="还没有需求池，创建第一个吧！" />
      </div>
    </div>

    <!-- 创建需求池对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建需求池"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createFormRules"
        label-width="100px"
      >
        <el-form-item label="需求池名称" prop="name">
          <el-input
            v-model="createForm.name"
            placeholder="请输入需求池名称（1-100字符）"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="需求池描述" prop="description">
          <el-input
            v-model="createForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入需求池描述（可选，最多500字符）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreatePool" :loading="creating">
          确认创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { Menu, Plus, User, Clock } from '@element-plus/icons-vue';
import api from '../api';
import { useUserStore } from '../stores/user';

const router = useRouter();
const userStore = useUserStore();

// 状态
const loading = ref(false);
const pools = ref([]);
const createDialogVisible = ref(false);
const creating = ref(false);
const createFormRef = ref(null);

// 表单数据
const createForm = ref({
  name: '',
  description: ''
});

// 表单验证规则
const createFormRules = {
  name: [
    { required: true, message: '请输入需求池名称', trigger: 'blur' },
    { min: 1, max: 100, message: '名称长度在1-100字符之间', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述长度不能超过500字符', trigger: 'blur' }
  ]
};

// 加载需求池列表
const loadPools = async () => {
  loading.value = true;
  try {
    const response = await api.getPools();
    if (response.code === 200) {
      pools.value = response.data;
    } else {
      console.error('加载需求池列表失败:', response.message);
      // 不显示错误消息，避免干扰用户操作
      pools.value = [];
    }
  } catch (error) {
    console.error('加载需求池列表失败:', error);
    // 只在非创建操作时显示错误提示（避免在创建成功后立即显示错误）
    // 如果是因为创建后刷新失败，用户已经看到成功提示，不需要再看到错误
    pools.value = [];
  } finally {
    loading.value = false;
  }
};

// 显示创建对话框
const showCreateDialog = () => {
  createForm.value = {
    name: '',
    description: ''
  };
  createDialogVisible.value = true;
};

// 处理创建需求池
const handleCreatePool = async () => {
  if (!createFormRef.value) return;

  await createFormRef.value.validate(async (valid) => {
    if (!valid) return;

    // 立即设置loading状态，防止重复提交
    creating.value = true;
    try {
      // 调试：检查当前用户
      console.log('创建需求池 - 当前用户:', userStore.currentUser)
      console.log('创建需求池 - 用户名:', userStore.currentUser?.name)

      // 获取当前用户名并添加到请求数据中 - 多重后备方案
      let userName = userStore.currentUser?.name || ''
      if (!userName) {
        // 后备方案：从localStorage获取
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

      console.log('创建需求池 - 最终使用的用户名:', userName)

      const requestData = {
        ...createForm.value,
        createdBy: userName
      };

      console.log('创建需求池 - 请求数据:', requestData)

      const response = await api.createPool(requestData);
      if (response.code === 200) {
        ElMessage.success('需求池创建成功');
        createDialogVisible.value = false;
        // 重置表单
        createForm.value = {
          name: '',
          description: ''
        };
        await loadPools(); // 刷新列表
      } else {
        // 后端返回非200状态码
        ElMessage.error(response.message || '创建失败');
        // 不继续执行，让对话框保持打开状态以便用户修正
      }
    } catch (error) {
      console.error('创建需求池失败:', error);
      // 网络错误或HTTP错误状态码（4xx, 5xx）
      const errorMsg = error.response?.data?.message || error.message || '创建需求池失败，请检查网络连接';
      ElMessage.error(errorMsg);
      // 不继续执行，让对话框保持打开状态
    } finally {
      creating.value = false;
    }
  });
};

// 进入需求池
const enterPool = (poolId) => {
  router.push({
    path: '/backlog',
    query: { poolId }
  });
};

// 获取需求池标签类型
const getPoolTagType = (count) => {
  if (!count || count === 0) return 'info';
  if (count < 5) return 'warning';
  return 'success';
};

// 格式化相对时间
const formatRelativeTime = (dateStr) => {
  if (!dateStr) return '';
  
  const date = new Date(dateStr);
  const now = new Date();
  const diff = now - date;
  
  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);
  const days = Math.floor(hours / 24);
  
  if (days > 0) return `${days}天前`;
  if (hours > 0) return `${hours}小时前`;
  if (minutes > 0) return `${minutes}分钟前`;
  return '刚刚';
};

// 组件挂载时加载数据
onMounted(() => {
  // 调试：检查用户状态
  console.log('PoolList onMounted - userStore:', userStore)
  console.log('PoolList onMounted - currentUser:', userStore.currentUser)
  console.log('PoolList onMounted - 当前用户名:', userStore.currentUser?.name)

  loadPools();
});
</script>

<style scoped>
.pool-list-container {
  min-height: 100vh;
  background: var(--gradient-dark);
  background-attachment: fixed;
  padding: 40px;
}

.page-header {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  margin-bottom: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: var(--shadow-lg);
}

.header-content h1 {
  color: var(--color-text-primary);
  font-size: var(--font-size-3xl);
  font-weight: var(--font-weight-bold);
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-content .subtitle {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  margin: 0;
}

/* 需求池卡片网格 */
.pools-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
  min-height: 400px;
}

/* 需求池卡片 */
.pool-card {
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  padding: var(--spacing-lg);
  cursor: pointer;
  transition: all var(--duration-normal) var(--ease-out);
  box-shadow: var(--shadow-lg);
  position: relative;
  overflow: hidden;
}

.pool-card:hover {
  transform: translateY(-5px);
  border-color: var(--color-accent-blue);
  box-shadow: var(--shadow-glow-blue),
              var(--shadow-lg);
}

.pool-card::before {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  background: var(--gradient-success);
  border-radius: var(--radius-lg);
  opacity: 0;
  transition: opacity var(--duration-normal) var(--ease-out);
  z-index: -1;
}

.pool-card:hover::before {
  opacity: 0.3;
}

.pool-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);
}

.pool-card-header h3 {
  color: var(--color-text-primary);
  font-size: var(--font-size-xl);
  font-weight: var(--font-weight-bold);
  margin: 0;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pool-description {
  color: var(--color-text-secondary);
  font-size: var(--font-size-sm);
  line-height: var(--line-height-normal);
  margin: 0 0 20px 0;
  min-height: 44px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pool-card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: var(--spacing-md);
  border-top: 1px solid var(--glass-border);
}

.creator-info,
.create-time {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--color-text-tertiary);
  font-size: 13px;
}

.creator-info {
  color: var(--color-accent-green);
}

/* 空状态 */
.empty-state {
  grid-column: 1 / -1;
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 300px;
}

/* 响应式布局 */
@media (max-width: 768px) {
  .pool-list-container {
    padding: 20px;
  }
  
  .pools-grid {
    grid-template-columns: 1fr;
  }
  
  .page-header {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
}
</style>
