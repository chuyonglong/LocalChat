# Composer Field Background Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 LocalChat 输入框内部背景固定为参考图的深紫灰 `#211E27`。

**Architecture:** 保留 `OutlinedTextFieldDefaults.colors` 的四个状态参数，仅把其值从透明替换为 `Color(0xFF211E27)`。不改变 Surface、描边、状态或任何回调。

**Tech Stack:** Kotlin、Jetpack Compose Material 3、Gradle。

---

### Task 1: 应用参考背景色

**Files:**
- Modify: `app/src/main/java/com/localchat/app/LocalChatApp.kt:310-315`

- [ ] **Step 1: 将四个容器状态改为固定色**

替换 `OutlinedTextFieldDefaults.colors` 内容为：

```kotlin
colors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color(0xFF211E27),
    unfocusedContainerColor = Color(0xFF211E27),
    disabledContainerColor = Color(0xFF211E27),
    errorContainerColor = Color(0xFF211E27),
),
```

- [ ] **Step 2: 编译验证**

Run: `./gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`。

- [ ] **Step 3: 在 Android Studio 刷新预览**

打开 `app/src/main/java/com/localchat/app/LocalChatApp.kt`，选择 `Split` 或 `Design`，点击 Preview 面板的 `Build & Refresh`。

Expected: `HomePreview` 中输入框内部显示深紫灰实色，圆角描边与按钮布局不变。
