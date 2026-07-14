# 首页 Compose Preview 设计

## 目标

在 Android Studio 中预览 LocalChat 的空聊天首页，外观与真机首页一致；不改变真机运行逻辑。

## 方案

- 将当前聊天页面的视觉结构提取为仅接收状态和回调的 Compose 内容函数。
- 运行时页面继续由 `ChatViewModel` 收集状态，并保留图片选择和发送行为。
- 新增无参数 `@Preview` 函数：使用与 `MainActivity` 一致的深色 Material 主题，传入空消息、空草稿和无操作回调。
- Preview 指定手机纵向尺寸并显示系统背景，便于在 Android Studio 的 Design/Split 面板中查看。

## 边界与验证

- Preview 不创建 `ChatViewModel`，不启动图片选择器，不进行网络、数据库或发送操作。
- 真机的聊天、侧边栏、设置及发送逻辑不改。
- 通过 Gradle 编译和 Android Studio Preview 渲染验证；此变更只增加设计时入口，无新的运行时行为，因此不新增业务单元测试。
