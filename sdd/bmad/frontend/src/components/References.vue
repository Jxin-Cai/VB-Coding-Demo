<template>
  <div class="references-container">
    <h3 class="references-title">
      <span>📚 引用的表结构</span>
    </h3>
    
    <!-- 无引用提示 -->
    <a-alert
      v-if="!references || references.length === 0"
      message="未找到相关表结构"
      description="SQL 可能不准确，建议上传相关的 DDL 文件"
      type="warning"
      show-icon
    />
    
    <!-- 引用列表 -->
    <a-collapse v-else accordion class="references-list">
      <a-collapse-panel
        v-for="(ref, index) in references"
        :key="index"
        :header="formatTableHeader(ref)"
      >
        <!-- 表信息 -->
        <div class="table-info">
          <a-tag color="blue">表</a-tag>
          <span class="table-name">{{ ref.table }}</span>
          <span v-if="ref.comment" class="table-comment">（{{ ref.comment }}）</span>
          <span v-if="ref.column_count" class="column-count">
            - {{ ref.column_count }} 个字段
          </span>
        </div>
        
        <!-- 引用的字段列表 -->
        <div v-if="ref.columns && ref.columns.length > 0" class="columns-section">
          <h4>引用的字段：</h4>
          <a-list size="small" :data-source="ref.columns">
            <template #renderItem="{ item }">
              <a-list-item>
                <div class="column-item">
                  <a-tag color="green">{{ item.data_type }}</a-tag>
                  <span class="column-name">{{ item.name }}</span>
                  <span v-if="item.comment" class="column-comment">
                    - {{ item.comment }}
                  </span>
                </div>
              </a-list-item>
            </template>
          </a-list>
        </div>
        
        <!-- DDL 片段（如果有） -->
        <div v-if="ref.ddl_snippet" class="ddl-snippet">
          <h4>DDL 定义：</h4>
          <a-typography-paragraph :code="true" copyable>
            {{ ref.ddl_snippet }}
          </a-typography-paragraph>
        </div>
      </a-collapse-panel>
    </a-collapse>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

// Props
interface Reference {
  table: string
  comment?: string
  column_count?: number
  columns?: Array<{
    name: string
    data_type?: string
    comment?: string
  }>
  ddl_snippet?: string
}

interface Props {
  references?: Reference[]
}

const props = withDefaults(defineProps<Props>(), {
  references: () => []
})

// 格式化表头
const formatTableHeader = (ref: Reference) => {
  let header = `📊 ${ref.table}`
  if (ref.comment) {
    header += ` - ${ref.comment}`
  }
  if (ref.column_count) {
    header += ` (${ref.column_count} 个字段)`
  }
  return header
}
</script>

<style scoped>
.references-container {
  margin-top: 16px;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
}

.references-title {
  margin-bottom: 16px;
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
}

.references-list {
  background: white;
}

.table-info {
  margin-bottom: 12px;
  font-size: 14px;
}

.table-name {
  font-weight: 600;
  font-family: 'Courier New', monospace;
  margin: 0 8px;
}

.table-comment {
  color: #666;
}

.column-count {
  color: #999;
  font-size: 12px;
}

.columns-section {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e8e8e8;
}

.columns-section h4 {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #595959;
}

.column-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.column-name {
  font-weight: 500;
  font-family: 'Courier New', monospace;
}

.column-comment {
  color: #666;
  font-size: 13px;
}

.ddl-snippet {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e8e8e8;
}

.ddl-snippet h4 {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #595959;
}
</style>
