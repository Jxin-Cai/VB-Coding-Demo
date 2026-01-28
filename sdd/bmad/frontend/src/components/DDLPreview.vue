<!--
  DDL 预览组件
  右侧侧边栏，展示表结构
-->
<template>
  <div class="ddl-preview">
    <!-- 空状态 -->
    <div v-if="!ddlContent" class="empty-ddl">
      <file-text-outlined :style="{ fontSize: '32px', color: tokens.text.tertiary }" />
      <p>点击引用表名查看 DDL</p>
    </div>

    <!-- DDL 内容 -->
    <div v-else class="ddl-content">
      <div ref="editorContainer" class="monaco-editor-container"></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { FileTextOutlined } from '@ant-design/icons-vue'
import { tokens } from '../theme/theme.config'
import { createSQLEditor, highlightText } from '../utils/monaco'
import type * as monaco from 'monaco-editor'

// Props
const props = defineProps<{
  ddlContent: string
  highlightedFields?: string[]
}>()

// State
const editorContainer = ref<HTMLElement | null>(null)
let editor: monaco.editor.IStandaloneCodeEditor | null = null

/**
 * 初始化 Monaco Editor
 */
onMounted(() => {
  if (!editorContainer.value) return

  // 创建编辑器实例
  editor = createSQLEditor(editorContainer.value, props.ddlContent || '', true)

  // 高亮使用的字段
  highlightFields()
})

/**
 * 高亮字段
 */
const highlightFields = () => {
  if (!editor || !props.highlightedFields || props.highlightedFields.length === 0) return
  highlightText(editor, props.highlightedFields)
}

/**
 * 监听 DDL 内容变化
 */
watch(() => props.ddlContent, (newContent) => {
  if (editor && newContent) {
    editor.setValue(newContent)
    highlightFields()
  }
})

/**
 * 监听高亮字段变化
 */
watch(() => props.highlightedFields, () => {
  highlightFields()
})

/**
 * 清理
 */
onUnmounted(() => {
  if (editor) {
    editor.dispose()
    editor = null
  }
})
</script>

<style scoped>
.ddl-preview {
  height: calc(100vh - 120px);
  overflow: hidden;
}

/* 空状态 */
.empty-ddl {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 48px 24px;
  text-align: center;
  color: v-bind('tokens.text.tertiary');
}

.empty-ddl p {
  margin-top: 12px;
  font-size: 13px;
}

/* DDL 内容 */
.ddl-content {
  height: 100%;
  padding: 8px;
}

.monaco-editor-container {
  height: 100%;
  width: 100%;
}

/* 高亮样式 */
:deep(.highlight-field) {
  color: v-bind('tokens.colors.highlight') !important;
  font-weight: 600;
  background: v-bind('tokens.highlight.bg');
  padding: 2px 4px;
  border-radius: 2px;
}

:deep(.highlight-glyph) {
  background: v-bind('tokens.colors.highlight');
  width: 4px !important;
  margin-left: 3px;
  border-radius: 2px;
}
</style>
