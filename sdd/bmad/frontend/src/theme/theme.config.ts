/**
 * RAG Text-to-SQL 系统主题配置
 * 黑色主题 + 橙色高亮
 * 基于 UX Design Specification
 */

import type { ThemeConfig } from 'ant-design-vue/es/config-provider/context'

/**
 * 设计 Token 定义
 */
export const tokens = {
  // 主题色
  colors: {
    primary: '#FF9500',        // 橙色（主要操作按钮 - UX 规范）
    success: '#52c41a',        // 绿色（SQL 生成成功）
    error: '#ff4d4f',          // 红色（DDL 解析失败）
    warning: '#faad14',        // 橙黄色（警告提示）
    info: '#1890ff',           // 蓝色（信息提示）
    highlight: '#FF9500',      // 橙色（引用源高亮）
  },
  
  // 背景色（深色模式）
  backgrounds: {
    base: '#141414',           // 最深背景
    container: '#1f1f1f',      // 卡片背景
    elevated: '#262626',       // 浮层背景
  },
  
  // 文本色
  text: {
    primary: '#ffffff',        // 主文本（白色）
    secondary: '#a0a0a0',      // 次要文本（灰色）
    tertiary: '#606060',       // 三级文本（深灰）
  },
  
  // 边框色
  border: {
    primary: '#404040',        // 主边框
    secondary: '#303030',      // 次要边框
  },
  
  // 间距
  spacing: {
    xs: 4,
    sm: 8,
    md: 12,
    lg: 16,
    xl: 24,
  },
  
  // 字体大小
  fontSizes: {
    sm: 12,
    base: 14,
    lg: 16,
    xl: 18,
  },
  
  // 圆角
  borderRadius: {
    sm: 2,
    base: 4,
    lg: 8,
  },

  // 橙色高亮系统
  highlight: {
    color: '#FF9500',                        // 主高亮色
    bg: 'rgba(255, 149, 0, 0.15)',          // 高亮背景色
    border: 'rgba(255, 149, 0, 0.4)',       // 高亮边框色
  },
}

/**
 * Ant Design Vue 主题配置
 */
export const blackTheme: ThemeConfig = {
  token: {
    // 主色调：橙色系
    colorPrimary: tokens.colors.primary,
    colorSuccess: tokens.colors.success,
    colorWarning: tokens.colors.warning,
    colorError: tokens.colors.error,
    colorInfo: tokens.colors.info,
    
    // 背景色：深色模式
    colorBgBase: tokens.backgrounds.base,
    colorBgContainer: tokens.backgrounds.container,
    colorBgElevated: tokens.backgrounds.elevated,
    
    // 文本色：高对比度
    colorText: tokens.text.primary,
    colorTextSecondary: tokens.text.secondary,
    colorTextTertiary: tokens.text.tertiary,
    
    // 边框色
    colorBorder: tokens.border.primary,
    colorBorderSecondary: tokens.border.secondary,
    
    // 字体
    fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif',
    fontSize: 14,
    
    // 圆角：适度保留（专业工具感）
    borderRadius: 4,
  },
  // ⚠️ 不使用 algorithm - 直接在 token 中定义深色主题
  // algorithm: theme.darkAlgorithm 需要从 ant-design-vue 导入
}

/**
 * SQL 语法高亮配色（VS Code Dark+ 主题）
 */
export const sqlSyntaxColors = {
  keyword: '#569cd6',      // SQL 关键字（SELECT, FROM, WHERE）
  string: '#ce9178',       // 字符串（'2024-01-01'）
  number: '#b5cea8',       // 数字（100, 30）
  function: '#dcdcaa',     // 函数名（COUNT, SUM）
  comment: '#6a9955',      // 注释（-- 注释内容）
  reference: '#FF9500',    // 表名和字段名（引用源，橙色高亮）
}

/**
 * Monaco Editor 主题配置
 */
export const monacoTheme = {
  base: 'vs-dark' as const,
  inherit: true,
  rules: [
    { token: 'keyword.sql', foreground: sqlSyntaxColors.keyword.substring(1) },
    { token: 'string.sql', foreground: sqlSyntaxColors.string.substring(1) },
    { token: 'number.sql', foreground: sqlSyntaxColors.number.substring(1) },
    { token: 'comment.sql', foreground: sqlSyntaxColors.comment.substring(1) },
    { token: 'identifier.sql', foreground: sqlSyntaxColors.reference.substring(1) }, // 橙色高亮
  ],
  colors: {
    'editor.background': tokens.backgrounds.container,
    'editor.foreground': tokens.text.primary,
    'editor.lineHighlightBackground': tokens.backgrounds.elevated,
    'editorLineNumber.foreground': tokens.text.tertiary,
  },
}

export default blackTheme
