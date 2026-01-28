<!--
  系统设置组件
  配置API Key、API Base URL等
-->
<template>
  <a-drawer
    v-model:open="visible"
    title="⚙️ 系统设置"
    width="450"
    placement="right"
    @close="handleClose"
  >
    <a-form layout="vertical" :model="formState">
      <!-- API Key配置 -->
      <a-form-item label="GLM API Key (智谱AI)" required>
        <a-input-password 
          v-model:value="formState.apiKey"
          placeholder="请输入您的 GLM API Key"
          :addon-before="keyIcon"
          allow-clear
        />
        <div class="hint-text">
          <info-circle-outlined />
          API Key 将存储在浏览器本地，不会上传到服务器
        </div>
      </a-form-item>

      <!-- API Base URL配置 -->
      <a-form-item label="API Base URL">
        <a-input 
          v-model:value="formState.apiBaseUrl"
          placeholder="http://localhost:8000/api"
          :addon-before="serverIcon"
          allow-clear
        />
        <div class="hint-text">
          <info-circle-outlined />
          后端API地址，默认为 localhost:8000
        </div>
      </a-form-item>

      <!-- 测试连接 -->
      <a-form-item>
        <a-space>
          <a-button 
            type="primary" 
            :loading="testing"
            @click="handleTestConnection"
          >
            <api-outlined v-if="!testing" />
            测试连接
          </a-button>
          
          <a-button @click="handleSave">
            <save-outlined />
            保存配置
          </a-button>
        </a-space>
        
        <a-alert
          v-if="testResult"
          :type="testResult.success ? 'success' : 'error'"
          :message="testResult.message"
          :show-icon="true"
          closable
          class="test-result"
        />
      </a-form-item>

      <a-divider />

      <!-- 使用提示 -->
      <a-alert
        type="info"
        message="使用提示"
        :description="usageGuide"
        :show-icon="true"
      />
    </a-form>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  InfoCircleOutlined,
  ApiOutlined,
  SaveOutlined,
} from '@ant-design/icons-vue'
import { settingsService } from '../services/api'

// Props
interface Props {
  visible?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  visible: false,
})

// Emits
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

// Icons
const keyIcon = '🔑'
const serverIcon = '🌐'

// State
const visible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value),
})

const formState = reactive({
  apiKey: '',
  apiBaseUrl: 'http://localhost:8000/api',
})

const testing = ref(false)
const testResult = ref<{ success: boolean; message: string } | null>(null)

// Usage guide
const usageGuide = `
1. 获取API Key：访问智谱AI开放平台（https://open.bigmodel.cn/）申请API Key
2. 配置后端：确保后端服务已启动（默认端口8000）
3. 测试连接：点击"测试连接"验证配置是否正确
4. 保存配置：配置成功后，API Key将保存到本地浏览器
`

/**
 * 从localStorage加载配置
 */
const loadConfig = () => {
  const savedApiKey = localStorage.getItem('glm_api_key')
  const savedBaseUrl = localStorage.getItem('api_base_url')
  
  if (savedApiKey) {
    formState.apiKey = savedApiKey
  }
  if (savedBaseUrl) {
    formState.apiBaseUrl = savedBaseUrl
  }
}

/**
 * 保存配置
 */
const handleSave = () => {
  if (!formState.apiKey.trim()) {
    message.warning('请输入API Key')
    return
  }

  localStorage.setItem('glm_api_key', formState.apiKey.trim())
  localStorage.setItem('api_base_url', formState.apiBaseUrl.trim())
  
  message.success('配置已保存到本地')
}

/**
 * 测试连接
 */
const handleTestConnection = async () => {
  if (!formState.apiKey.trim()) {
    message.warning('请先输入API Key')
    return
  }

  testing.value = true
  testResult.value = null

  try {
    // 临时保存到localStorage供API拦截器使用
    localStorage.setItem('glm_api_key', formState.apiKey.trim())
    localStorage.setItem('api_base_url', formState.apiBaseUrl.trim())

    const success = await settingsService.testConnection()
    
    if (success) {
      testResult.value = {
        success: true,
        message: '✅ 连接成功！后端服务正常运行',
      }
      message.success('连接测试成功')
    } else {
      testResult.value = {
        success: false,
        message: '❌ 连接失败！请检查后端服务是否启动',
      }
    }
  } catch (error: any) {
    testResult.value = {
      success: false,
      message: `❌ 连接失败：${error.message}`,
    }
  } finally {
    testing.value = false
  }
}

/**
 * 关闭Drawer
 */
const handleClose = () => {
  visible.value = false
}

// 初始化：加载已保存的配置
watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      loadConfig()
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.hint-text {
  margin-top: 8px;
  font-size: 12px;
  color: #8c8c8c;
  display: flex;
  align-items: center;
  gap: 6px;
}

.test-result {
  margin-top: 16px;
}

:deep(.ant-form-item-label) {
  font-weight: 500;
}
</style>
