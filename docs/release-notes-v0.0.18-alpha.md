**Full Changelog**: https://github.com/zyf2007/HanakoAI/compare/v0.0.17-alpha...v0.0.18-alpha

Full 版本带有本地 ML Kit OCR 模型，Lite 版本只能使用云端 OCR 模型。
ML Kit OCR 不支持 Latex 公式识别，因此无法用于数学题目解析的场景。如果没有特殊需求，建议使用 Lite 版本。

## Changelog

- 历史记录支持长按或点击已引用内容块，快速定位并查看对应题目与回答。
- 新增 Markdown 内容块引用流程，可在追问中携带多个引用片段，并显示引用关系。
- 引用菜单新增复制、引用和关闭操作，适配数学公式内容的尺寸与预览定位。
- 统一历史记录与模型提供方的数据存储权威，补充答案版本 ID 迁移和架构回归测试。

## 下载说明

99% 的手机请下载 [app-lite-arm64-v8a-release.apk](https://github.com/zyf2007/HanakoAI/releases/download/v0.0.18-alpha/app-lite-arm64-v8a-release.apk) (3.05MB)
如果你明确需要本地 ML Kit OCR，再下载 [app-full-arm64-v8a-release.apk](https://github.com/zyf2007/HanakoAI/releases/download/v0.0.18-alpha/app-full-arm64-v8a-release.apk) (16.06MB)
32 位设备可以下载：
- [app-lite-armeabi-v7a-release.apk](https://github.com/zyf2007/HanakoAI/releases/download/v0.0.18-alpha/app-lite-armeabi-v7a-release.apk) (3.05MB)
- [app-full-armeabi-v7a-release.apk](https://github.com/zyf2007/HanakoAI/releases/download/v0.0.18-alpha/app-full-armeabi-v7a-release.apk) (11.97MB)

## 说明

0.0.9 版本中加入了 The Kirari Network 在线登录的模型获取方式，但目前仍建议优先使用自己的模型 API。Kirari 这边我有时候会换模型，用之前记得到「模型提供方 → The Kirari Network」一键同步。
