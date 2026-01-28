<script setup lang="ts">
import { RouterView } from 'vue-router'
import { ConfigProvider } from 'ant-design-vue'
import { blackTheme } from './theme/theme.config'
</script>

<template>
  <ConfigProvider :theme="blackTheme">
    <RouterView />
  </ConfigProvider>
</template>

<style>
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html,
body,
#app {
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #141414;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* Ant Design 深色主题全局覆盖 */
.ant-layout {
  background: #141414 !important;
}

.ant-layout-sider {
  background: #1f1f1f !important;
}

.ant-layout-header {
  background: #1f1f1f !important;
}

.ant-layout-footer {
  background: #1f1f1f !important;
}

/* 滚动条样式（全局）*/
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #141414;
}

::-webkit-scrollbar-thumb {
  background: #303030;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #404040;
}

/* Ant Design 组件深色主题调整 */
.ant-drawer .ant-drawer-body {
  background: #141414;
}

.ant-modal .ant-modal-content {
  background: #1f1f1f;
}

.ant-message-notice-content {
  background: #262626;
}
</style>
