<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <el-icon :size="32" color="#667eea"><TrendCharts /></el-icon>
          <h2>敏捷估点服务</h2>
          <p class="subtitle">Planning Poker</p>
        </div>
      </template>

      <el-form 
        :model="loginForm" 
        :rules="rules" 
        ref="formRef"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="name">
          <el-input
            v-model="loginForm.name"
            placeholder="请输入您的姓名"
            size="large"
            clearable
            :prefix-icon="User"
          >
            <template #prepend>
              <el-icon><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            style="width: 100%"
          >
            <el-icon v-if="!loading"><Right /></el-icon>
            {{ loading ? '登录中...' : '进入估点' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="tips">
        <el-alert
          title="提示"
          type="info"
          :closable="false"
          show-icon
        >
          <template #default>
            <p>输入您的姓名即可进入系统</p>
            <p>首次使用会自动创建账号</p>
          </template>
        </el-alert>
      </div>
    </el-card>

    <div class="footer-info">
      <p>🎯 支持多人实时协作估点</p>
      <p>📊 自动统计分析投票结果</p>
      <p>🔄 WebSocket 实时同步</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Right } from '@element-plus/icons-vue'
import api from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const loginForm = ref({
  name: ''
})

const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await api.login(loginForm.value.name)

        console.log('登录响应:', response)
        console.log('sessionToken:', response.data.sessionToken)
        console.log('userName:', response.data.userName)

        // 保存用户信息和 token
        // 使用后端返回的userName，如果后端没有则使用前端输入的
        const userName = response.data.userName || loginForm.value.name
        const sessionToken = response.data.sessionToken

        if (!sessionToken) {
          console.error('后端未返回sessionToken!')
          ElMessage.error('登录失败：服务器未返回会话令牌')
          return
        }

        userStore.login(
          { name: userName },
          sessionToken
        )

        console.log('用户信息已保存到store:', userStore.currentUser)
        console.log('localStorage user:', localStorage.getItem('user'))

        ElMessage.success('登录成功！')
        router.push('/pools')
      } catch (error) {
        console.error('登录失败:', error)
        ElMessage.error('登录失败，请重试')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, var(--color-bg-primary) 0%, #1a1a1a 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 背景装饰 */
.login-container::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: radial-gradient(
    circle,
    rgba(0, 255, 136, 0.05) 0%,
    transparent 50%
  );
  animation: rotate 20s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.login-card {
  width: 100%;
  max-width: 450px;
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-xl);
  background: var(--glass-bg);
  backdrop-filter: var(--glass-blur);
  border: 2px solid var(--glass-border);
  position: relative;
  z-index: 1;
  animation: scaleIn 0.6s var(--ease-out);
}

/* 卡片霓虹边框效果 */
.login-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: var(--radius-xl);
  padding: 2px;
  background: linear-gradient(135deg, var(--color-accent-green), var(--color-accent-blue));
  -webkit-mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0.3;
  animation: neonPulse 3s ease-in-out infinite;
}

.card-header {
  text-align: center;
  padding: 20px 0;
}

.card-header :deep(.el-icon) {
  color: var(--color-accent-green);
  filter: drop-shadow(0 0 10px rgba(0, 255, 136, 0.5));
}

.card-header h2 {
  margin: 16px 0 8px 0;
  color: var(--color-text-primary);
  font-size: 28px;
  background: linear-gradient(to right, var(--color-accent-green), var(--color-accent-blue));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  color: var(--color-text-secondary);
  font-size: 16px;
  margin: 0;
  text-transform: uppercase;
  letter-spacing: 2px;
}

.tips {
  margin-top: 20px;
}

.tips :deep(.el-alert) {
  background: var(--color-bg-tertiary);
  border-color: var(--color-border-primary);
  color: var(--color-text-primary);
}

.tips :deep(.el-alert__title) {
  color: var(--color-text-primary);
}

.footer-info {
  margin-top: 40px;
  text-align: center;
  color: var(--color-text-primary);
  position: relative;
  z-index: 1;
}

.footer-info p {
  margin: 12px 0;
  font-size: 16px;
  opacity: 0.8;
  animation: fadeInUp 0.6s var(--ease-out) backwards;
}

.footer-info p:nth-child(1) {
  animation-delay: 0.3s;
}

.footer-info p:nth-child(2) {
  animation-delay: 0.4s;
}

.footer-info p:nth-child(3) {
  animation-delay: 0.5s;
}

/* 表单样式增强 */
:deep(.el-input__wrapper) {
  background: var(--color-bg-tertiary);
  border-color: var(--color-border-primary);
  box-shadow: none;
  transition: all var(--duration-normal) var(--ease-out);
}

:deep(.el-input__wrapper:hover) {
  border-color: var(--color-accent-blue);
  box-shadow: 0 0 10px rgba(0, 212, 255, 0.2);
}

:deep(.el-input__wrapper.is-focus) {
  border-color: var(--color-accent-green);
  box-shadow: 0 0 15px rgba(0, 255, 136, 0.3);
}

:deep(.el-input__inner) {
  color: var(--color-text-primary);
}

:deep(.el-input__inner::placeholder) {
  color: var(--color-text-tertiary);
}

/* 响应式 */
@media (max-width: 768px) {
  .login-card {
    max-width: 100%;
  }
  
  .footer-info p {
    font-size: 14px;
  }
}
</style>
