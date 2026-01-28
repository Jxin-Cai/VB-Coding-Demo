/**
 * Monaco Editor 配置和初始化工具
 */
import * as monaco from 'monaco-editor'
import { monacoTheme, tokens } from '../theme/theme.config'

/**
 * 初始化 Monaco Editor 主题
 */
export function initMonacoTheme() {
  // 定义自定义 SQL Dark 主题
  monaco.editor.defineTheme('sql-dark', {
    base: 'vs-dark',
    inherit: true,
    rules: monacoTheme.rules,
    colors: monacoTheme.colors as any,
  })
}

/**
 * 创建 SQL 编辑器
 */
export function createSQLEditor(
  container: HTMLElement,
  value: string,
  readOnly = true
): monaco.editor.IStandaloneCodeEditor {
  // 确保主题已定义
  initMonacoTheme()

  // 创建编辑器
  const editor = monaco.editor.create(container, {
    value,
    language: 'sql',
    theme: 'sql-dark',
    readOnly,
    minimap: { enabled: false },
    scrollBeyondLastLine: false,
    fontSize: 14,
    lineNumbers: 'on',
    lineNumbersMinChars: 3,
    glyphMargin: false,
    folding: readOnly ? false : true,
    lineDecorationsWidth: 0,
    lineHeight: 22,
    renderLineHighlight: readOnly ? 'none' : 'line',
    automaticLayout: true,
    overviewRulerBorder: false,
    hideCursorInOverviewRuler: true,
    scrollbar: {
      vertical: 'auto',
      horizontal: 'auto',
      verticalScrollbarSize: 8,
      horizontalScrollbarSize: 8,
    },
  })

  return editor
}

/**
 * 高亮指定文本
 */
export function highlightText(
  editor: monaco.editor.IStandaloneCodeEditor,
  keywords: string[]
): void {
  const model = editor.getModel()
  if (!model || keywords.length === 0) return

  const decorations: monaco.editor.IModelDeltaDecoration[] = []

  keywords.forEach((keyword) => {
    const text = model.getValue()
    const regex = new RegExp(`\\b${escapeRegExp(keyword)}\\b`, 'gi')
    let match

    while ((match = regex.exec(text)) !== null) {
      const startPos = model.getPositionAt(match.index)
      const endPos = model.getPositionAt(match.index + match[0].length)

      decorations.push({
        range: new monaco.Range(
          startPos.lineNumber,
          startPos.column,
          endPos.lineNumber,
          endPos.column
        ),
        options: {
          inlineClassName: 'highlight-reference',
        },
      })
    }
  })

  // 应用装饰
  editor.deltaDecorations([], decorations)
}

/**
 * 转义正则表达式特殊字符
 */
function escapeRegExp(text: string): string {
  return text.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
}

/**
 * 格式化 SQL
 */
export function formatSQL(sql: string): string {
  // 简单的 SQL 格式化
  return sql
    .replace(/\bSELECT\b/gi, 'SELECT')
    .replace(/\bFROM\b/gi, '\nFROM')
    .replace(/\bWHERE\b/gi, '\nWHERE')
    .replace(/\bAND\b/gi, '\n  AND')
    .replace(/\bOR\b/gi, '\n  OR')
    .replace(/\bORDER BY\b/gi, '\nORDER BY')
    .replace(/\bGROUP BY\b/gi, '\nGROUP BY')
    .replace(/\bJOIN\b/gi, '\nJOIN')
    .replace(/\bLEFT JOIN\b/gi, '\nLEFT JOIN')
    .replace(/\bRIGHT JOIN\b/gi, '\nRIGHT JOIN')
    .replace(/\bINNER JOIN\b/gi, '\nINNER JOIN')
}
