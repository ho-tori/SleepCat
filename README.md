# SleepCat Android

P0 原生应用框架：Kotlin + Jetpack Compose + Material 3，单 Activity、单 `:app` 模块。配色和明暗模式是两个独立维度，使用 Preferences DataStore 保存。不包含通知、闹钟、后台服务、后端或网络权限。

## 环境与运行

在当前 Windows 环境直接演示：[模拟器启动、安装与演示指南](docs/emulator-demo.md)。

- JDK 17；Android SDK Platform 35、Build Tools 35.0.0、Platform Tools。
- Android 10 / API 29 及以上设备或模拟器。
- Gradle 8.11.1 Wrapper、AGP 8.9.2、Kotlin/Compose Compiler 2.1.20。
- Compose BOM 2025.04.01；Navigation 2.9.0、Lifecycle 2.9.0、Activity 1.10.1、DataStore 1.1.7。
- 将 `local.properties` 设置为本机 SDK 路径，例如 `sdk.dir=C:/Users/yourname/AppData/Local/Android/Sdk`。此文件不提交。

在 Android Studio 打开根目录，使用 JDK 17 同步，选择 app 运行；或在 PowerShell 执行：

```powershell
$env:JAVA_HOME='D:/Develop/java17' # 替换为你的 JDK
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.sleepcat.app/.MainActivity
# 连接模拟器/测试设备后：
.\gradlew.bat :app:connectedDebugAndroidTest
```

macOS/Linux 使用 `./gradlew`。Windows 本次验证的 SDK 位于 `.tools/android-sdk`，仅用于本机；`.tools` 被忽略，不是源代码依赖。首次构建需要下载依赖；Java 不一定自动使用系统代理，如有需要，在本机命令中传 `-Dhttps.proxyHost=127.0.0.1 -Dhttps.proxyPort=你的端口`，不要提交私人代理配置。

Debug APK：`app/build/outputs/apk/debug/app-debug.apk`。仅为开发签名，不是商店发布包。

## 目录与数据流

```text
app/src/main/java/com/sleepcat/app/
  MainActivity.kt              # 系统栏与 Compose 入口
  SleepCatApplication.kt       # 应用级 AppContainer / 单例 DataStore
  SleepCatApp.kt               # 根节点唯一一次主题 StateFlow 观察
  core/designsystem/theme/     # 六组颜色、字体、形状、间距
  core/designsystem/component/ # 卡片、按钮、可滚动限宽容器
  core/navigation/             # Scaffold、三个 Tab、清单二级路由
  data/preferences/            # 主题模型与 DataStore Repository
  feature/home/                # 目标卡、清单入口、准备占位说明
  feature/tasks/               # 只读样例清单
  feature/review/              # 有明确标识的演示周回顾
  feature/settings/           # 主题选择、ViewModel、UiState
```

设置事件 → SettingsViewModel → Repository 原子更新 DataStore → Flow → StateFlow → 根节点 `collectAsStateWithLifecycle` → `SleepCatTheme` → 所有页面。

存储文件 `theme.preferences_pb`；键 `theme_palette` 保存 `warm/mint/blue`，`theme_mode` 保存 `system/light/dark`。不保存 ordinal。未知值各自回退到 `warm/system`；文件损坏重建默认设置，普通 I/O 错误显示可重试提示。异步读取时使用暖橘/跟随系统默认值，设置页显示加载态。写入成功后才采用新主题，失败显示未保存提示。

## 如何继续修改

- 修改配色：`core/designsystem/theme/ColorSchemes.kt`；页面只使用 Material 语义颜色。
- 修改字体/圆角/间距：`SleepCatTheme.kt`。共享组件文件内包含暖橘浅色、薄荷绿深色预览。
- 新增配色：给 ThemePalette 添加稳定 key，并补充 Light/Dark 颜色；不要改已发布的 key。
- 清单和周回顾均为显式演示，不持久化业务数据。P1 引入 Room 和 TaskRepository 后替换清单数据入口，页面继续接收状态与事件，避免直接访问数据库。
- 当前文字为简体中文；有国际化需求时统一迁移到 string resources。猫咪是本地可替换 vector 占位，插画固有颜色不随主题变化。

## 验证与参考

- [P0 设计与执行记录](docs/P0-design.md)
- [手工验收步骤](docs/P0-acceptance.md)；模型测试在 `src/test`，真实 DataStore 与 UI/错误恢复测试在 `src/androidTest`，后者需要 Android 设备，避免以 Windows 文件语义代替 Android 行为。
- [实施报告](docs/P0-report.md)
- [AGP 官方兼容表](https://developer.android.com/build/releases/agp-8-9-0-release-notes)
- [Compose BOM 官方发布说明](https://android-developers.googleblog.com/2025/04/whats-new-in-jetpack-compose-april-25.html)
- [Navigation](https://developer.android.com/jetpack/androidx/releases/navigation)、[Lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle)、[DataStore](https://developer.android.com/jetpack/androidx/releases/datastore) 官方版本记录。

原型保留在 `prototype/`，只作为产品结构、交互和视觉参考，运行时不加载 HTML。
