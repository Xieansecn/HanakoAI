**Full Changelog**: https://github.com/zyf2007/HanakoAI/compare/v0.0.16-alpha...v0.0.17-alpha

Full 版本带有本地 ML Kit OCR 模型，Lite 版本只能使用云端 OCR 模型。
ML Kit OCR 不支持 Latex 公式识别，因此无法用于数学题目解析的场景。如果没有特殊需求，建议使用 Lite 版本。

## Changelog

- 历史记录新增分组管理、未分组视图和独立分组页面，支持新建、重命名、删除分组以及一个题目加入多个分组。
- 重做历史记录卡片和长按操作，加入状态、自然日期时间、七色标签、多选及批量移动、标色、导出和删除。
- 新增题目卡片导出与统一图片预览，支持双击/双指缩放、长按保存、分享，以及批量保存结果提示。
- 可复制内容块支持 Markdown 与 LaTeX，按实际渲染结果自适应高度，复制时仍保留原始文本。
- 助手设置新增 AI 总结标题开关和提示词，生成结果通过内容版本校验，避免旧标题覆盖新答案。

## 下载说明

99% 的手机请下载 [app-lite-arm64-v8a-release.apk](https://github.com/zyf2007/HanakoAI/releases/download/v0.0.17-alpha/app-lite-arm64-v8a-release.apk) (3.03MB)
如果你明确需要本地 ML Kit OCR，再下载 [app-full-arm64-v8a-release.apk](https://github.com/zyf2007/HanakoAI/releases/download/v0.0.17-alpha/app-full-arm64-v8a-release.apk) (16.04MB)
32 位设备可以下载：
- [app-lite-armeabi-v7a-release.apk](https://github.com/zyf2007/HanakoAI/releases/download/v0.0.17-alpha/app-lite-armeabi-v7a-release.apk) (3.03MB)
- [app-full-armeabi-v7a-release.apk](https://github.com/zyf2007/HanakoAI/releases/download/v0.0.17-alpha/app-full-armeabi-v7a-release.apk) (11.95MB)

## 说明

0.0.9 版本中加入了 The Kirari Network 在线登录的模型获取方式，但目前仍建议优先使用自己的模型 API。Kirari 这边我有时候会换模型，用之前记得到「模型提供方 → The Kirari Network」一键同步。
