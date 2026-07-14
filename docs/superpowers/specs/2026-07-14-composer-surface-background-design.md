# Composer Surface 背景设计

## 目标

让底部输入区的外层 `Surface` 成为唯一的背景容器，避免 `OutlinedTextField` 绘制第二层不一致的容器背景。

## 方案

- 保留 `Composer` 当前的 `Surface`、圆角、间距和阴影。
- 给内部 `OutlinedTextField` 配置聚焦与非聚焦状态的容器色为透明。
- 不调整输入框边框、文本、占位符、发送逻辑或图片选择逻辑。

## 验证

- 执行 Debug Kotlin 编译，确认 Compose Material 3 API 调用有效。
- 在 Android Studio 刷新 `HomePreview`，确认输入框中央区域显示外层 `Surface` 的背景色，而边框和控件布局保持不变。
