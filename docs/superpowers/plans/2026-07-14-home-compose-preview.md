# 首页 Compose Preview Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在 Android Studio 中可预览与真机一致的 LocalChat 空聊天首页，且不改变真机行为。

**Architecture:** `ChatScreen` 保留 ViewModel 状态收集与 Activity Result 图片选择逻辑。其布局转交给纯 Compose 内容函数；无参数 Preview 在相同深色主题下以静态空状态调用该内容函数。

**Tech Stack:** Kotlin、Jetpack Compose Material 3、Compose UI Tooling、Gradle。

---

## 文件结构

- 修改：`app/src/main/java/com/localchat/app/LocalChatApp.kt` — 分离聊天页面的可预览布局，并新增静态首页预览入口。
- 修改：`docs/superpowers/specs/2026-07-14-home-compose-preview-design.md` — 不需要修改；作为已确认设计依据。
- 新增测试：无。该变更只加入 Android Studio 设计时入口，项目当前没有 Compose UI 测试运行器；以 Debug 编译验证类型、Preview 注解和依赖完整性。

### Task 1: 提供独立的聊天首页布局

**Files:**
- Modify: `app/src/main/java/com/localchat/app/LocalChatApp.kt:92-113`

- [ ] **Step 1: 确认现有编译状态**

Run: `./gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`。

- [ ] **Step 2: 提取纯 UI 内容函数**

移除 `DrawerContent` 上现有的 `@Preview`（它带有必填参数，不能预览）。将 `ChatScreen` 的 `Scaffold` 布局移入下列只接收显示状态和回调的函数；`ChatScreen` 继续收集 `vm.messages`、`vm.isSending`、`vm.error`，并传入图片选择与发送回调：

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatScreenContent(
    messages: List<MessageEntity>,
    sending: Boolean,
    error: String?,
    draft: String,
    hasImage: Boolean,
    onDraft: (String) -> Unit,
    onImage: () -> Unit,
    onSend: () -> Unit,
    onOpenDrawer: () -> Unit,
    onNewConversation: () -> Unit,
) {
    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets.safeDrawing,
        topBar = {
            CenterAlignedTopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onOpenDrawer) {
                        Icon(Icons.Default.Menu, "打开聊天记录")
                    }
                },
                actions = {
                    if (messages.isNotEmpty()) {
                        IconButton(onNewConversation) {
                            Icon(Icons.Default.Edit, "新建聊天")
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (messages.isEmpty()) {
                EmptyChat(Modifier.fillMaxSize())
            } else {
                MessageList(
                    messages,
                    Modifier.fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 88.dp),
                )
            }
            if (hasImage) {
                Text(
                    "已附加图片",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 82.dp),
                )
            }
            error?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 82.dp),
                )
            }
            Composer(
                draft = draft,
                hasImage = hasImage,
                sending = sending,
                modifier = Modifier.align(Alignment.BottomCenter),
                onDraft = onDraft,
                onImage = onImage,
                onSend = onSend,
            )
        }
    }
}
```

`ChatScreen` 内保留 `rememberLauncherForActivityResult`，并将 `image != null` 作为 `hasImage` 传入；发送成功后仍清空 `draft` 和 `image`。

- [ ] **Step 3: 编译验证布局重构未改变运行时接口**

Run: `./gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`。

- [ ] **Step 4: 提交变更（跳过）**

该目录不是 Git 仓库，不能创建提交。保留工作区改动供用户检查。

### Task 2: 添加静态首页 Preview

**Files:**
- Modify: `app/src/main/java/com/localchat/app/LocalChatApp.kt:after ChatScreenContent`

- [ ] **Step 1: 添加无参数 Preview 入口**

在 `LocalChatApp.kt` 添加下列 Preview，使用与 `MainActivity` 相同的主题及空聊天状态：

```kotlin
@Preview(showBackground = true, device = "spec:width=412dp,height=892dp,dpi=420")
@Composable
private fun HomePreview() {
    MaterialTheme(colorScheme = darkColorScheme()) {
        ChatScreenContent(
            messages = emptyList(),
            sending = false,
            error = null,
            draft = "",
            hasImage = false,
            onDraft = {},
            onImage = {},
            onSend = {},
            onOpenDrawer = {},
            onNewConversation = {},
        )
    }
}
```

- [ ] **Step 2: 验证 Preview 入口可编译**

Run: `./gradlew.bat :app:compileDebugKotlin`

Expected: `BUILD SUCCESSFUL`。

- [ ] **Step 3: 在 Android Studio 渲染确认**

打开 `LocalChatApp.kt`，在编辑器右上角选择 `Split` 或 `Design`。在 Preview 面板选中 `HomePreview` 并点击 `Build & Refresh`。

Expected: 显示深色空聊天首页，含顶部菜单/新建图标、欢迎语和底部输入框；不触发图片选择器或 ViewModel 初始化。

- [ ] **Step 4: 提交变更（跳过）**

该目录不是 Git 仓库，不能创建提交。保留工作区改动供用户检查。
