---
name: /openspec-archive
id: openspec-archive
category: OpenSpec
description: 归档已部署的 OpenSpec 变更并更新规格。
---
<!-- OPENSPEC:START -->
**护栏**
- 优先采用直接、最小化的实现，仅在明确请求或明显需要时才增加复杂性。
- 将变更严格限定在请求的结果范围内。
- 如需额外的 OpenSpec 约定或澄清，请参考 `openspec/AGENTS.md`（位于 `openspec/` 目录内——如果看不到，运行 `ls openspec` 或 `openspec update`）。

**步骤**
1. 确定要归档的 change ID：
   - 如果此提示已包含特定的 change ID（例如，在由斜杠命令参数填充的 `<ChangeId>` 块内），则在修剪空格后使用该值。
   - 如果对话宽松地引用了变更（例如，通过标题或摘要），运行 `openspec list` 以显示可能的 ID，共享相关候选项，并确认用户打算使用哪一个。
   - 否则，审查对话，运行 `openspec list`，并询问用户要归档哪个变更；在继续之前等待确认的 change ID。
   - 如果仍然无法识别单个 change ID，停止并告诉用户您还不能归档任何内容。
2. 通过运行 `openspec list`（或 `openspec show <id>`）验证 change ID，如果变更缺失、已归档或尚未准备好归档，则停止。
3. 运行 `openspec archive <id> --yes`，以便 CLI 移动变更并在不提示的情况下应用规格更新（仅对仅工具工作使用 `--skip-specs`）。
4. 审查命令输出以确认目标规格已更新并且变更落在 `changes/archive/` 中。
5. 使用 `openspec validate --strict --no-interactive` 进行验证，如果有任何看起来不对的地方，使用 `openspec show <id>` 检查。

**参考**
- 在归档之前使用 `openspec list` 确认 change ID。
- 使用 `openspec list --specs` 检查刷新的规格，并在交接之前解决任何验证问题。
<!-- OPENSPEC:END -->
