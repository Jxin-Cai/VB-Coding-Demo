<!--
  空状态引导组件
  首次使用引导，降低使用门槛
-->
<template>
  <div class="empty-state-guide">
    <!-- 主标题 -->
    <div class="guide-header">
      <h1 class="guide-title">开始你的 SQL 生成之旅 🚀</h1>
      <p class="guide-subtitle">通过 3 个简单步骤，快速生成精准的 SQL 查询</p>
    </div>

    <!-- 步骤引导 -->
    <div class="guide-steps">
      <div class="step-item">
        <div class="step-number">①</div>
        <div class="step-content">
          <h3>上传 DDL 文件</h3>
          <p>上传你的数据库表结构文件</p>
        </div>
      </div>
      <div class="step-arrow">→</div>
      <div class="step-item">
        <div class="step-number">②</div>
        <div class="step-content">
          <h3>描述需求</h3>
          <p>用自然语言说明你想查询什么</p>
        </div>
      </div>
      <div class="step-arrow">→</div>
      <div class="step-item">
        <div class="step-number">③</div>
        <div class="step-content">
          <h3>获得 SQL</h3>
          <p>3 秒内生成精准可用的 SQL</p>
        </div>
      </div>
    </div>

    <!-- 上传区域 -->
    <div class="upload-area">
      <a-button type="primary" size="large" @click="$emit('upload-file')">
        <upload-outlined />
        上传 DDL 文件
      </a-button>
      <p class="upload-hint">支持 .sql 文件，最大 10MB</p>
    </div>

    <!-- 示例问题 -->
    <div class="example-section">
      <h3 class="example-title">或者试试这些示例：</h3>
      <div class="example-cards">
        <a-card
          v-for="(example, index) in examples"
          :key="index"
          hoverable
          class="example-card"
          @click="$emit('select-example', example.query)"
        >
          <div class="example-icon">{{ example.icon }}</div>
          <div class="example-text">{{ example.query }}</div>
        </a-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { UploadOutlined } from '@ant-design/icons-vue'
import { tokens } from '../theme/theme.config'

// Emits
defineEmits(['upload-file', 'select-example'])

// Data
const examples = [
  { icon: '📊', query: '查看昨天的订单量' },
  { icon: '👥', query: '查看最近 30 天的新用户' },
  { icon: '💰', query: '统计各地区的销售额' },
]
</script>

<style scoped>
.empty-state-guide {
  width: 100%;
  max-width: 600px;
  text-align: center;
}

/* 主标题 */
.guide-header {
  margin-bottom: 48px;
}

.guide-title {
  font-size: 32px;
  font-weight: 700;
  color: v-bind('tokens.text.primary');
  margin-bottom: 12px;
  line-height: 1.3;
}

.guide-subtitle {
  font-size: 16px;
  color: v-bind('tokens.text.secondary');
  line-height: 1.6;
}

/* 步骤引导 */
.guide-steps {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 48px;
  padding: 32px 24px;
  background: v-bind('tokens.backgrounds.container');
  border-radius: 12px;
  border: 1px solid v-bind('tokens.border.primary');
}

.step-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.step-number {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: v-bind('tokens.colors.highlight');
  color: #ffffff;
  font-size: 24px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}

.step-content h3 {
  font-size: 16px;
  font-weight: 600;
  color: v-bind('tokens.text.primary');
  margin: 0 0 4px 0;
}

.step-content p {
  font-size: 13px;
  color: v-bind('tokens.text.secondary');
  margin: 0;
  line-height: 1.4;
}

.step-arrow {
  font-size: 24px;
  color: v-bind('tokens.text.tertiary');
  flex-shrink: 0;
}

/* 上传区域 */
.upload-area {
  margin-bottom: 48px;
}

.upload-hint {
  margin-top: 12px;
  font-size: 13px;
  color: v-bind('tokens.text.tertiary');
}

/* 示例问题 */
.example-section {
  text-align: left;
}

.example-title {
  font-size: 14px;
  font-weight: 500;
  color: v-bind('tokens.text.secondary');
  margin-bottom: 16px;
  text-align: center;
}

.example-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.example-card {
  background: v-bind('tokens.backgrounds.container');
  border: 1px solid v-bind('tokens.border.primary');
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.example-card:hover {
  border-color: v-bind('tokens.colors.highlight');
  background: v-bind('tokens.backgrounds.elevated');
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.example-card :deep(.ant-card-body) {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.example-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.example-text {
  font-size: 15px;
  color: v-bind('tokens.text.primary');
  font-weight: 500;
}
</style>
