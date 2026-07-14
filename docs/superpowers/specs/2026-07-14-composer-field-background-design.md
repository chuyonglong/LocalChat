# Composer 输入框背景设计

## 目标

使 `OutlinedTextField` 的内部背景与参考图一致，使用深紫灰色 `#211E27`。

## 方案

- 将 `Composer` 中文本框的聚焦、未聚焦、禁用与错误容器色统一为 `Color(0xFF211E27)`。
- 保留 `Surface` 的圆角、阴影和底部整体容器角色。
- 不修改文本框的形状、描边颜色、输入回调、图片选择或发送逻辑。

## 验证

- 编译 Debug Kotlin 源码。
- 在 Android Studio 刷新 `HomePreview`，确认输入框的内部为深紫灰实色并保持现有描边。
