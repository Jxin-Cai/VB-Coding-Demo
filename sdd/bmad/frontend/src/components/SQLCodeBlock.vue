<!--
  SQL 代码块组件
  Monaco Editor + 引用源展示 + 操作按钮
-->
<template>
  <div class="sql-code-block">
    <!-- SQL 代码编辑器 -->
    <div class="code-editor-wrapper">
      <div ref="editorContainer" class="monaco-editor-container"></div>
      
      <!-- 操作按钮 -->
      <div class="code-actions">
        <a-tooltip title="复制 SQL">
          <a-button type="text" size="small" @click="handleCopy">
            <copy-outlined />
          </a-button>
        </a-tooltip>
        <a-tooltip title="重新生成">
          <a-button type="text" size="small" @click="$emit('regenerate')">
            <reload-outlined />
          </a-button>
        </a-tooltip>
      </div>
    </div>

    <!-- 引用源展示（橙色高亮）-->
    <div v-if="references && references.length > 0" class="references-section">
      <div class="references-header">
        <span class="references-icon">📋</span>
        <span class="references-title">引用表：</span>
      </div>
      <div class="references-list">
        <a-tag
          v-for="table in references"
          :key="table"
          class="reference-tag"
          @click="$emit('view-ddl', table)"
        >
          {{ table }}
          <info-circle-outlined class="reference-info" />
        </a-tag>
      </div>
      <div class="references-hint">
        点击表名查看完整 DDL 结构
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  CopyOutlined,
  ReloadOutlined,
  InfoCircleOutlined,
} from '@ant-design/icons-vue'
import { tokens } from '../theme/theme.config'
import { createSQLEditor, highlightText } from '../utils/monaco'
import type * as monaco from 'monaco-editor'

// Props
const props = defineProps<{
  sql: string
  references?: string[]
}>()

// Emits
defineEmits(['copy', 'regenerate', 'view-ddl'])

// State
const editorContainer = ref<HTMLElement | null>(null)
let editor: monaco.editor.IStandaloneCodeEditor | null = null

/**
 * 初始化 Monaco Editor
 */
onMounted(() => {
  if (!editorContainer.value) return

  // 创建编辑器实例
  editor = createSQLEditor(editorContainer.value, props.sql, true)

  // 高亮引用的表名和字段名（橙色）
  highlightReferences()
})

/**
 * 高亮引用源
 */
const highlightReferences = () => {
  if (!editor || !props.references || props.references.length === 0) return
  highlightText(editor, props.references)
}

/**
 * 复制 SQL
 */
const handleCopy = () => {
  if (props.sql) {
    navigator.clipboard.writeText(props.sql)
    message.success('✅ 已复制到剪贴板')
  }
}

/**
 * 监听 SQL 变化
 */
watch(() => props.sql, (newSql) => {
  if (editor && newSql) {
    editor.setValue(newSql)
    highlightReferences()
  }
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
.sql-code-block {
  background: v-bind('tokens.backgrounds.container');
  border: 1px solid v-bind('tokens.border.primary');
  border-radius: 8px;
  overflow: hidden;
  margin: 12px 0;
}

/* 代码编辑器包装器 */
.code-editor-wrapper {
  position: relative;
}

.monaco-editor-container {
  height: 300px; /* 增加高度，确保可见 */
  width: 100%;
  min-height: 200px; /* 最小高度 */
  background: #1f1f1f; /* 确保有背景色，即使Monaco未加载 */
}

/* 操作按钮 */
.code-actions {
  position: absolute;
  top: 8px;
  right: 8px;
  display: flex;
  gap: 4px;
  background: v-bind('tokens.backgrounds.elevated');
  border-radius: 4px;
  padding: 4px;
  opacity: 0.8;
  transition: opacity 0.2s;
}

.code-editor-wrapper:hover .code-actions {
  opacity: 1;
}

.code-actions :deep(.ant-btn) {
  color: v-bind('tokens.text.secondary');
}

.code-actions :deep(.ant-btn:hover) {
  color: v-bind('tokens.text.primary');
}

/* 引用源展示 */
.references-section {
  border-top: 1px solid v-bind('tokens.border.secondary');
  padding: 12px 16px;
  background: v-bind('tokens.backgrounds.base');
}

.references-header {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 8px;
}

.references-icon {
  font-size: 14px;
}

.references-title {
  font-size: 13px;
  color: v-bind('tokens.text.secondary');
  font-weight: 500;
}

.references-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 6px;
}

.reference-tag {
  background: v-bind('tokens.highlight.bg');
  color: v-bind('tokens.colors.highlight');
  border: 1px solid v-bind('tokens.highlight.border');
  cursor: pointer;
  font-family: 'Monaco', 'Menlo', 'Courier New', monospace;
  font-size: 13px;
  padding: 4px 12px;
  border-radius: 4px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 4px;
}

.reference-tag:hover {
  background: v-bind('tokens.colors.highlight') + '30';
  border-color: v-bind('tokens.colors.highlight');
  transform: translateY(-1px);
}

.reference-info {
  font-size: 12px;
  opacity: 0.6;
}

.references-hint {
  font-size: 11px;
  color: v-bind('tokens.text.tertiary');
  font-style: italic;
}

/* Monaco Editor 高亮样式 */
:deep(.highlight-reference) {
  color: v-bind('tokens.colors.highlight') !important;
  font-weight: 500;
  background: v-bind('tokens.highlight.bg');
  padding: 2px 4px;
  border-radius: 2px;
}
</style>
