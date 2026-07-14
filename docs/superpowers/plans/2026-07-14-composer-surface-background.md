# Composer Surface Background Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 让底部输入框透明，以外层 Surface 作为唯一背景容器。

**Architecture:** `Composer` 的 Surface、圆角和阴影保持不变。仅在内部 Material 3 `OutlinedTextField` 添加透明容器色；输入、发送和图片选择的回调均不变。

**Tech Stack:** Kotlin、Jetpack Compose Material 3、Gradle。

---

## 文件结构

- 修改：`app/src/main/java/com/localchat/app/LocalChatApp.kt` — 引入颜色与 Material 3 文本框颜色 API，并配置输入框容器色。
- 新增测试：无。此改动仅改变 Preview/运行时绘制色；项目未配置 Compose 截图测试，使用 Kotlin 编译和 Android Studio Preview 渲染验证。

### Task 1: 将输入框容器背景设为透明

**Files:**
- Modify: `app/src/main/java/com/localchat/app/LocalChatApp.kt:35-58,295-302`

- [ ] **Step 1: 确认修改前可以编译**

Run: `./gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`。

- [ ] **Step 2: 导入颜色 API 并配置 OutlinedTextField**

添加以下 import：

```kotlin
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.graphics.Color
```

在 `OutlinedTextField` 的 `shape` 参数后添加下列参数：

```kotlin
colors = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    errorContainerColor = Color.Transparent,
),
```

- [ ] **Step 3: 编译验证 Material 3 API 调用**

Run: `./gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`。

- [ ] **Step 4: 在 Android Studio 检查预览**

打开 `app/src/main/java/com/localchat/app/LocalChatApp.kt`，选择 `Split` 或 `Design`，在 Preview 面板点击 `Build & Refresh`。

Expected: `HomePreview` 的底部输入区域由外层 Surface 填充，输入框内部没有独立的矩形背景；边框、占位文字、加号和发送图标仍然显示。

- [ ] **Step 5: 提交变更（跳过）**

该工作区没有 Git 仓库元数据，因此不能创建提交。
