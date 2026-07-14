# Repository Guidelines

## 项目结构与模块组织

LocalChat 是单模块 Android 应用。业务代码位于 `app/src/main/java/com/localchat/app/`：`LocalChatApp.kt` 负责 Compose 界面，`ChatViewModel.kt` 协调页面状态。持久化、设置和 HTTP 客户端放在 `data/`；校验逻辑与不依赖 Android 的模型放在 `domain/`；路由类型放在 `ui/`。资源及启动图标位于 `app/src/main/res/`。JVM 单元测试位于 `app/src/test/`，设备和 Compose 集成测试位于 `app/src/androidTest/`；设计与计划文档位于 `docs/superpowers/`。

## 构建、测试与本地开发

- `./gradlew.bat assembleDebug`：构建 debug APK。
- `./gradlew.bat testDebugUnitTest`：运行本地 JVM 单元测试。
- `./gradlew.bat connectedDebugAndroidTest`：在已连接设备或模拟器上运行仪器测试。
- `./gradlew.bat lintDebug`：对 debug 变体执行 Android Lint。

使用 Android Studio 打开仓库根目录，并选择 debug 变体运行。`local.properties` 仅保存本机 Android SDK 配置，不能写入凭据或私有服务地址。

## 编码风格与命名

遵循 `kotlin.code.style=official`：使用 4 个空格缩进，不使用制表符，保持惯用 Kotlin 写法。类、Composable 和枚举使用 `PascalCase`；函数、属性和包使用 `camelCase`。测试名称应描述可观察行为，例如 ``fun `parse rejects an empty API key`()``。领域逻辑不得依赖 Android 或 Compose；网络与数据库访问放在 `data/`。优先延续现有 Material 3 和 Compose 模式，避免无关重构。

## 测试要求

每项新增校验规则、解析分支或路由行为都应有单元测试。涉及 Room、Compose 交互或 Android 框架行为时，新增 `androidTest`。覆盖成功路径、边界输入与失败结果。开发中运行受影响的测试；交付前运行 `testDebugUnitTest`。UI 或持久化变更还应在已连接设备上验证。

## 提交与拉取请求

仓库目前没有可继承的提交历史。使用简短、命令式的 Conventional Commit，例如 `feat: add model picker`、`fix: mask API endpoint`。每个提交只包含一个可验证的目标。PR 应说明目的、关联 issue、测试命令和结果；界面变更附截图。禁止提交 `app/build/`、`.gradle/`、IDE 缓存、APK 或密钥。

## 安全与视觉资源

API Key 必须经应用的安全设置流程保存，不得出现在日志、截图、测试夹具或测试数据中。新增应用图标或界面图标时，优先使用 [Iconfont](https://www.iconfont.cn/) 或 [Material Symbols Rounded](https://fonts.google.com/icons?icon.query=music&icon.style=Rounded)，并确认许可与视觉风格一致。
