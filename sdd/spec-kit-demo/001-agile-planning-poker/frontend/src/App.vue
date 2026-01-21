<template>
  <div id="app">
    <!-- 背景粒子效果 -->
    <ParticlesBackground 
      :particle-count="40"
      particle-color="rgba(0, 255, 136, 0.5)"
      :particle-speed="0.3"
    />
    
    <el-container class="layout-container">
      <!-- 顶部导航栏 -->
      <el-header class="app-header">
        <div class="header-content">
          <div class="logo">
            <el-icon :size="24" style="margin-right: 8px"><TrendCharts /></el-icon>
            <span class="logo-text">敏捷估点服务</span>
          </div>
          <div class="user-info" v-if="userStore.isLoggedIn">
            <el-tag type="success" effect="dark">
              <el-icon><User /></el-icon>
              {{ userStore.currentUser.name }}
            </el-tag>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleLogout"
              style="margin-left: 12px"
            >
              退出登录
            </el-button>
          </div>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>

      <!-- 底部 -->
      <el-footer class="app-footer">
        <div class="footer-content">
          <span>Agile Planning Poker Service © 2026</span>
          <span style="margin-left: 20px; color: #909399">
            基于 Spring Boot + Vue 3 + Element Plus
          </span>
        </div>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
import { useUserStore } from './stores/user'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import ParticlesBackground from './components/ParticlesBackground.vue'

const userStore = useUserStore()
const router = useRouter()

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('退出登录成功')
  router.push('/login')
}
</script>

<style scoped>
.app-wrapper {
  position: relative;
  min-height: 100vh;
  background: var(--color-bg-primary);
}

.layout-container {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  background: transparent;
}

.app-header {
  background: linear-gradient(135deg, #1a1a1a 0%, #0a0a0a 100%);
  color: var(--color-text-primary);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.5);
  height: 60px !important;
  padding: 0;
  border-bottom: 1px solid var(--color-border-primary);
}

.header-content {
  height: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.logo {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  color: var(--color-text-primary);
}

.logo-text {
  background: linear-gradient(to right, var(--color-accent-green), var(--color-accent-blue));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 20px rgba(0, 255, 136, 0.3);
}

.user-info {
  display: flex;
  align-items: center;
}

.app-main {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.app-footer {
  background: var(--color-bg-primary);
  border-top: 1px solid var(--color-border-primary);
  height: 50px !important;
  display: flex;
  align-items: center;
  justify-content: center;
}

.footer-content {
  color: var(--color-text-secondary);
  font-size: 14px;
}
</style>

<style>
/* 引入主题系统和动画库 */
@import './styles/theme.css';
@import './styles/animations.css';
@import './styles/components/button.css';
@import './styles/components/form.css';

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 
    'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  background: var(--color-bg-primary);
  color: var(--color-text-primary);
}

/* 覆盖Element Plus默认样式为黑色主题 */
.el-card {
  background: var(--color-bg-secondary);
  border-color: var(--color-border-primary);
  color: var(--color-text-primary);
}

/* 按钮增强交互效果 */
.el-button {
  transition: all var(--duration-normal) var(--ease-out);
  position: relative;
  overflow: hidden;
}

.el-button:hover:not(:disabled) {
  transform: translateY(-2px);
}

.el-button:active:not(:disabled) {
  transform: translateY(0);
}

.el-button--primary {
  background: linear-gradient(135deg, #409eff 0%, #0066cc 100%);
  border-color: #409eff;
}

.el-button--primary:hover:not(:disabled) {
  box-shadow: var(--shadow-glow-blue);
  background: linear-gradient(135deg, #66b1ff 0%, #409eff 100%);
}

.el-button--success {
  background: linear-gradient(135deg, var(--color-accent-green) 0%, #00d48f 100%);
  border-color: var(--color-accent-green);
}

.el-button--success:hover:not(:disabled) {
  box-shadow: var(--shadow-glow-green);
}

.el-button--warning {
  background: linear-gradient(135deg, var(--color-accent-orange) 0%, #ff7b00 100%);
  border-color: var(--color-accent-orange);
}

.el-button--warning:hover:not(:disabled) {
  box-shadow: var(--shadow-glow-pink);
}

.el-button--danger {
  background: linear-gradient(135deg, var(--color-accent-pink) 0%, #ff0066 100%);
  border-color: var(--color-accent-pink);
}

.el-button--danger:hover:not(:disabled) {
  box-shadow: var(--shadow-glow-pink);
}

/* 按钮点击涟漪效果 */
.el-button::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 0;
  height: 0;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.5);
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: width 0.6s, height 0.6s, opacity 0.6s;
}

.el-button:active::after {
  width: 200px;
  height: 200px;
  opacity: 0;
  transition: 0s;
}

/* 页面切换动画 */
.page-enter-active {
  animation: pageEnter 0.5s var(--ease-out);
}

.page-leave-active {
  animation: pageLeave 0.3s var(--ease-in);
}

@keyframes pageEnter {
  from {
    opacity: 0;
    filter: blur(10px);
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    filter: blur(0);
    transform: translateY(0);
  }
}

@keyframes pageLeave {
  from {
    opacity: 1;
  }
  to {
    opacity: 0;
  }
}
</style>
