---
name: smart-image-generator
description: 自动识别场景类型,协作设计专业提示词,使用 Gemini 生成图片。支持智能账号管理（换号/重新登录），与tech-article-writer技能集成。
---

# 智能图片生成器

一个智能图片生成系统，能够自动识别场景、协作设计提示词并生成高质量图片。

## 🎯 何时使用这个技能

**用户请求包含以下关键词时使用**：

- "生成图片" / "生成封面" / "生成海报"
- "流程图" / "思维导图" / "可视化"
- "图片设计" / "视觉设计"
- 提供文章/内容并要求配图

**典型场景**：

```bash
用户: "帮我为这篇文章生成一个封面图"
用户: "把这个流程可视化成流程图"
用户: "生成一张活动海报"
用户: "为我的笔记生成配图"
```

## ✨ 核心能力

- 🎯 **智能场景识别**：自动检测图片用途（cover/flowchart/poster/mindmap/note）
- 🌍 **语言自适应**：中文内容生成中文图片，英文内容生成英文图片
- 📝 **协作设计**：与用户迭代优化提示词
- 🎨 **多种风格**：notion/obsidian/blueprint/warm

---

## 📋 AI 助手执行步骤

### Step 1: 识别用户需求

**分析用户输入，确定**：

- 内容来源：本地文件路径 / 网站 URL / 直接输入的文本
- 场景类型：自动检测或用户指定
- 视觉风格：默认 obsidian，或用户指定

**示例判断**：

```typescript
// 输入分析
if (input.includes(".md") || input.includes("/")) {
  type = "本地文件";
  content = await readFile(input);
} else if (input.startsWith("http")) {
  type = "网站URL";
  content = await fetchWebsite(input);
} else {
  type = "直接内容";
  content = input;
}
```

### Step 2: 确定场景类型

**自动检测逻辑**（如用户未指定）：

- 包含"流程"/"步骤"/"工作流" → flowchart
- 包含"思维"/"结构"/"知识图" → mindmap
- 包含"活动"/"宣传"/"海报" → poster
- 包含"笔记"/"插图"/"说明" → note
- 默认 → cover

**向用户确认**：

```
我检测到这可能是一个【流程图】场景。
是否正确？如需调整，请告诉我具体场景类型。
```

### Step 3: 生成提示词

**调用脚本**：

```bash
cd skills/custom-skills/smart-image-generator

# 推荐：指定输入文件（输出在输入文件同目录）
bun scripts/generate-prompt.ts \
  --scene ${SCENE_TYPE} \
  --style ${STYLE} \
  --file ${INPUT_FILE_PATH} \
  --content "${CONTENT}"

# 或：不指定输入文件（输出在项目根目录）
bun scripts/generate-prompt.ts \
  --scene ${SCENE_TYPE} \
  --style ${STYLE} \
  --content "${CONTENT}"
```

**参数说明**：

- `--file`: **输入文件路径**（⚠️ 指要为其生成图片的文件，如 `docs/article.md`）
  - ✅ 有此参数：输出在输入文件同目录
  - 📂 无此参数：输出在项目根目录
- `--content`: 文件内容或描述文本
- `--scene`: 场景类型（cover/flowchart/poster/mindmap/note）
- `--style`: 视觉风格（可选，默认 obsidian）

**脚本会自动**：

1. 检测内容语言（中文/英文）
2. 提取标题、关键词、摘要
3. 生成对应语言的提示词（包含 nano banana pro 指令）
4. 保存到 `smart-image-generator-output/prompts/`

**输出位置（三级优先级）**：

1. **用户指定**（`--output`）：使用指定路径
2. **输入文件同目录**（`--file`）：`输入文件目录/smart-image-generator-output/prompts/`
3. **项目根目录**（无 `--file`）：`项目根目录/smart-image-generator-output/prompts/`

### Step 4: 展示提示词并征求意见

**展示生成的提示词给用户**：

```
✅ 已为您生成提示词！

【提示词预览】
Create a obsidian-style flowchart (16:9, Chinese text):
**Title**: BMAD 框架工作流程
**Visual**:
- Main focus: 专业现代的构图
- Key elements: 工作流, 监控, 切换
...

这个提示词是否满意？
- 满意：我将立即生成图片
- 需要调整：请告诉我如何调整
```

### Step 5: 生成图片

**用户确认后，调用生成脚本**：

```bash
bun scripts/generate-image.ts \
  --prompt ${PROMPT_FILE_PATH}
```

**首次使用**（自动处理）：

1. 检测到无登录状态
2. 自动打开 Chrome 浏览器
3. 跳转到 gemini.google.com
4. 等待用户在浏览器中登录（最多 5 分钟）
5. 登录成功后浏览器自动关闭
6. 保存登录状态，后续自动使用

**注意**：
- ✅ 只会弹出1个浏览器窗口（已修复之前的双窗口bug）
- ✅ 给足5分钟时间，即使需要2FA验证也足够
- ✅ 登录成功后会自动保存，下次无需重新登录

**后续使用**：

- 自动使用已保存的登录状态
- 无需再次登录
- 如过期会自动打开浏览器

**换号/重新登录**：

当用户需要切换账号时，AI应该：

1. 识别用户意图（关键词：换号、重新登录、切换账号、登出、logout）
2. 智能搜索smart-image-generator技能位置
3. 执行清除登录脚本：
   ```bash
   cd ${技能目录}
   bun scripts/logout.ts
   ```
4. 告知用户：下次使用时会自动弹出浏览器重新登录

**示例对话**：

```
用户: 我想换个账号

AI: 检测到您想要换号重新登录。

    🔍 找到技能: ~/.cursor/skills/smart-image-generator
    🔐 正在清除登录状态...

    ✅ 登录状态已清除
    💡 下次生成图片时会自动弹出浏览器，请登录新账号。
```

**告知用户**：

```
⏳ 正在生成图片，请稍候...

首次使用提示：
如果浏览器自动打开，请在浏览器中登录您的 Google 账号。
登录成功后浏览器会自动关闭，图片生成会自动继续。
```

### Step 6: 返回结果

**成功后展示**：

```
✅ 图片生成成功！

📁 提示词：${PROMPT_PATH}
📁 图片：  ${IMAGE_PATH}

您可以：
- 查看生成的图片
- 修改提示词文件后重新生成
- 继续生成其他图片
```

---

## 📂 输出目录规则（三级优先级）

技能使用三级优先级确定输出位置：

### 优先级 1：用户明确指定路径

**触发条件**：用户使用 `--output` 参数

**示例**：

```bash
bun scripts/generate-prompt.ts \
  --scene cover \
  --content "..." \
  --output /custom/path/prompt.md
```

**输出位置**：用户指定的完整路径

### 优先级 2：输入文件同目录

**触发条件**：

- 用户提供了本地文件路径
- 未使用 `--output` 参数

**示例**：

```bash
# 用户提供: docs/article.md
→ 输出到: docs/smart-image-generator-output/
```

**输出结构**：

```
docs/
├── article.md  (输入文件)
└── smart-image-generator-output/
    ├── prompts/
    │   └── cover-notion-2026-01-29.md
    └── images/
        └── cover-notion-2026-01-29.png
```

### 优先级 3：项目根目录

**触发条件**：

- 直接输入内容（无文件路径）
- 或无法确定输入文件位置

**输出位置**：

```
项目根目录/smart-image-generator-output/
├── prompts/
└── images/
```

**如何确定项目根目录**：

- 向上查找 `package.json`
- 找到即为项目根目录
- 未找到则使用当前工作目录

### 统一的目录结构

**无论哪个优先级，都使用相同结构**：

```
smart-image-generator-output/
├── prompts/                          # 所有提示词
│   ├── cover-notion-2026-01-29.md
│   ├── flowchart-obsidian-2026-01-29.md
│   └── poster-minimal-2026-01-29.md
└── images/                           # 所有生成的图片
    ├── cover-notion-2026-01-29.png
    ├── flowchart-obsidian-2026-01-29.png
    └── poster-minimal-2026-01-29.png
```

**文件命名规范**：`{场景}-{风格}-{日期}.{扩展名}`

---

## 🎨 场景类型

| 场景        | 描述       | 适用内容                   |
| ----------- | ---------- | -------------------------- |
| `cover`     | 文章封面图 | 博客文章、文档、报告       |
| `flowchart` | 流程图     | 工作流、步骤说明、流程描述 |
| `poster`    | 宣传海报   | 活动、产品、公告           |
| `mindmap`   | 思维导图   | 知识结构、概念关系         |
| `note`      | 笔记插图   | 学习笔记、说明图           |

## 🎨 视觉风格

| 风格        | 描述                 | 适用场景           |
| ----------- | -------------------- | ------------------ |
| `notion`    | 现代 SaaS 风格       | 产品介绍、专业文档 |
| `obsidian`  | 手绘知识风格（默认） | 笔记、知识管理     |
| `blueprint` | 蓝图技术风格         | 技术文档、架构图   |
| `warm`      | 温暖渐变风格         | 生活、人文内容     |

---

## 🔧 脚本使用说明

### generate-prompt.ts

**生成提示词文件**

```bash
bun scripts/generate-prompt.ts \
  --scene <type> \
  --style <name> \
  --content "<text>" \
  [--output <path>]
```

**参数**：

- `--scene`：场景类型（必需）
- `--style`：视觉风格（默认 obsidian）
- `--content`：内容文本（必需）
- `--output`：输出路径（可选，默认自动确定）

**自动功能**：

- ✅ 检测内容语言（中文/英文）
- ✅ 中文内容 → 中文描述 + `(Chinese text)` 标记
- ✅ 英文内容 → 英文描述（无标记）
- ✅ 提取标题、关键词、摘要
- ✅ 智能确定输出路径

### generate-image.ts

**调用 Gemini 生成图片**

```bash
bun scripts/generate-image.ts \
  --prompt <file> \
  [--output <path>]
```

**参数**：

- `--prompt`：提示词文件路径（必需）
- `--output`：图片输出路径（可选，默认自动匹配 images/）

**自动功能**：

- ✅ 首次使用自动打开浏览器登录
- ✅ 后续使用自动复用登录状态
- ✅ 登录过期自动重新登录
- ✅ 自动保存到 `images/` 目录

---

## ⚠️ 常见问题处理

### 问题 1：登录失败

**症状**：浏览器打开后超时或登录不成功

**处理步骤**：

1. 确认用户已安装 Google Chrome
2. 确认网络可访问 gemini.google.com
3. 告知用户：系统会最多重试 20 次，每次间隔 5 秒
4. 如仍失败，建议手动删除 cookies 重试

```bash
rm -rf ~/.cache/puppeteer/gemini-cookies.json
```

### 问题 2：场景识别错误

**处理**：使用 `--scene` 参数手动指定

```bash
bun scripts/generate-prompt.ts \
  --scene flowchart \
  --content "..."
```

### 问题 3：中文显示乱码

**已彻底修复** (v2.2.0)：使用 Imagen 3 模型

**根本原因**：

- ❌ Gemini 默认图片生成能力对中文渲染支持有限
- ✅ Imagen 3 ("nano banana pro") 是专业图片生成模型，原生支持中文

**核心修复**：

- ✅ 添加 "nano banana pro" 指令触发 Imagen 3（v2.2.0 - 关键！）
- ✅ 添加结构化的 Language Requirements 章节（v2.1.0 - 辅助）
- ✅ 使用 CRITICAL 标记和强制性语言（v2.1.0 - 辅助）
- ✅ 明确指定标点符号样式（v2.1.0 - 辅助）

**验证方法**：

```bash
# 查看生成的提示词文件末尾
tail -10 smart-image-generator-output/prompts/xxx.md

# 应该包含：
Please use nano banana pro to generate this [场景] ensuring all text is in Chinese...
```

**关键洞察**：详见 [KEY-INSIGHT.md](./KEY-INSIGHT.md)

### 问题 4：找不到 Bun

**处理**：

1. 确认环境已安装 Bun
2. 提示用户安装：`curl -fsSL https://bun.sh/install | bash`
3. 验证：`bun --version`

---

## 📖 参考文件

### 场景参考（内部使用）

- `references/scene-types/cover.md`
- `references/scene-types/flowchart.md`
- `references/scene-types/poster.md`
- `references/scene-types/mindmap.md`
- `references/scene-types/note.md`

### 风格参考（内部使用）

- `references/styles/notion.md`
- `references/styles/obsidian.md`
- `references/styles/blueprint.md`
- `references/styles/warm.md`

---

## 🎯 AI 助手注意事项

1. **必须先征求用户意见**：生成图片前必须展示提示词并征求用户同意
2. **自动处理登录**：不要让用户手动配置 cookies，系统会自动处理
3. **输出路径透明**：明确告知用户文件保存位置
4. **支持迭代**：用户可以修改提示词后重新生成
5. **语言识别**：系统自动识别语言，无需用户指定
6. **错误友好**：遇到错误给出明确的解决建议

---

## 📝 使用示例

### 示例 1：从文件生成封面

```
用户: 帮我为 docs/article.md 生成一个封面图

AI 步骤:
1. 读取文件内容
2. 检测为 cover 场景
3. 生成提示词（自动检测语言）
4. 展示给用户确认
5. 调用 generate-image.ts 生成
6. 输出到: docs/smart-image-generator-output/
```

### 示例 2：直接内容生成流程图

```
用户: 把这个流程生成流程图：
      1. 用户输入
      2. 场景识别
      3. 生成提示词
      4. 生成图片

AI 步骤:
1. 识别场景为 flowchart
2. 检测内容为中文
3. 生成中文描述的提示词
4. 展示并征求意见
5. 生成图片
6. 输出到: 项目根目录/smart-image-generator-output/
```

### 示例 3：指定风格和路径

```
用户: 用 notion 风格为这个内容生成海报，保存到 posters/ 目录

AI 步骤:
1. 场景: poster
2. 风格: notion
3. 输出: posters/smart-image-generator-output/
4. 生成并保存
```

---

## 🚀 版本信息

**当前版本**: v2.2.0  
**最后更新**: 2026-01-30

**v2.2.0 新特性**（🎯 真正的修复）：

- ✅ **找到并修复中文乱码的根本原因**：使用 **Imagen 3** ("nano banana pro")
- ✅ 添加模型指定指令，触发专业图片生成模型
- ✅ 完美解决中文文字渲染问题
- 📖 详见 [REAL-FIX.md](./REAL-FIX.md) 了解完整分析

**v2.1.0 特性**（辅助改进）：

- ✅ 添加独立的 "Language Requirements" 章节
- ✅ 使用 CRITICAL 标记强调语言设置
- ✅ 明确指定标点符号样式
- ✅ 在多个位置重复强调语言要求

**v2.0.0 特性**：

- ✅ 智能语言识别（中英文自适应）
- ✅ 统一输出目录管理（三级优先级）
- ✅ 简化用户操作（自动处理大部分配置）

---

## 📚 更多信息

- [README.md](./README.md) - 详细安装和使用指南
- [技术文档](./docs/) - 内部实现细节
- [故障排除](./README.md#-故障排除) - 常见问题解决

**MIT License** © VB Coding Demo
