<template>
  <div class="not-found-container">
    <div class="error-content">
      <!-- 404数字 -->
      <div class="error-code">
        <span class="digit">4</span>
        <span class="digit">0</span>
        <span class="digit">4</span>
      </div>
      
      <!-- 错误信息 -->
      <h1 class="error-title">页面未找到</h1>
      <p class="error-description">
        抱歉,您访问的页面不存在或已被移除
      </p>
      
      <!-- 操作按钮 -->
      <div class="error-actions">
        <el-button 
          type="primary" 
          size="large"
          @click="goHome"
        >
          <el-icon><HomeFilled /></el-icon>
          返回首页
        </el-button>
        <el-button 
          size="large"
          @click="goBack"
        >
          <el-icon><Back /></el-icon>
          返回上一页
        </el-button>
      </div>
      
      <!-- 装饰元素 -->
      <div class="decoration-circles">
        <div class="circle circle-1"></div>
        <div class="circle circle-2"></div>
        <div class="circle circle-3"></div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { HomeFilled, Back } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/pools')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.not-found-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: var(--color-bg-primary);
  padding: 20px;
  position: relative;
  overflow: hidden;
}

.error-content {
  text-align: center;
  position: relative;
  z-index: 1;
  animation: fadeInUp 0.8s var(--ease-out);
}

/* 404数字 */
.error-code {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 40px;
}

.digit {
  font-size: 120px;
  font-weight: bold;
  font-family: 'Monaco', 'Menlo', monospace;
  background: linear-gradient(135deg, var(--color-accent-green), var(--color-accent-blue));
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  text-shadow: 0 0 30px rgba(0, 255, 136, 0.3);
  animation: digitFloat 3s ease-in-out infinite;
  position: relative;
}

.digit:nth-child(1) {
  animation-delay: 0s;
}

.digit:nth-child(2) {
  animation-delay: 0.2s;
}

.digit:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes digitFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-20px);
  }
}

/* 霓虹光晕效果 */
.digit::after {
  content: attr(data-text);
  position: absolute;
  left: 0;
  top: 0;
  z-index: -1;
  filter: blur(20px);
  opacity: 0.5;
}

/* 标题和描述 */
.error-title {
  font-size: 32px;
  color: var(--color-text-primary);
  margin-bottom: 16px;
  animation: fadeInUp 0.8s var(--ease-out) 0.2s backwards;
}

.error-description {
  font-size: 18px;
  color: var(--color-text-secondary);
  margin-bottom: 40px;
  animation: fadeInUp 0.8s var(--ease-out) 0.3s backwards;
}

/* 操作按钮 */
.error-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
  animation: fadeInUp 0.8s var(--ease-out) 0.4s backwards;
}

/* 装饰圆圈 */
.decoration-circles {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 0;
}

.circle {
  position: absolute;
  border-radius: 50%;
  border: 2px solid;
  opacity: 0.1;
  animation: circleFloat 20s linear infinite;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: 10%;
  left: 10%;
  border-color: var(--color-accent-green);
  animation-duration: 25s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  top: 60%;
  right: 15%;
  border-color: var(--color-accent-blue);
  animation-duration: 30s;
  animation-direction: reverse;
}

.circle-3 {
  width: 150px;
  height: 150px;
  bottom: 15%;
  left: 20%;
  border-color: var(--color-accent-pink);
  animation-duration: 20s;
}

@keyframes circleFloat {
  0% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -30px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
  100% {
    transform: translate(0, 0) scale(1);
  }
}

/* 背景网格效果 */
.not-found-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-image: 
    linear-gradient(rgba(0, 255, 136, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(0, 255, 136, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  opacity: 0.5;
}

/* 响应式 */
@media (max-width: 768px) {
  .error-code {
    gap: 10px;
  }
  
  .digit {
    font-size: 80px;
  }
  
  .error-title {
    font-size: 24px;
  }
  
  .error-description {
    font-size: 16px;
  }
  
  .error-actions {
    flex-direction: column;
    width: 100%;
    max-width: 300px;
    margin: 0 auto;
  }
  
  .error-actions .el-button {
    width: 100%;
  }
  
  .circle-1,
  .circle-2,
  .circle-3 {
    width: 150px;
    height: 150px;
  }
}

/* 鼠标悬停增强 */
.error-actions .el-button {
  transition: all var(--duration-normal) var(--ease-out);
}

.error-actions .el-button:hover {
  transform: translateY(-4px) scale(1.05);
}
</style>
