# SleepCat Android P0 实施报告

日期：2026-10-27。工程：`E:/Term3.1/SleepCat`。范围：仅 v0.1 任务书 P0。

## 1. 本次新增/修改

原目录仅有 HTML 原型；新增可独立构建的 Kotlin/Compose 原生 Android 工程，未改动原型。

- 单 `:app` module、Gradle Wrapper、Version Catalog；minSdk 29 / targetSdk 35 / JDK 17。
- `MainActivity.kt`、`SleepCatApplication.kt`、`SleepCatApp.kt`：单 Activity、手动 AppContainer、单例 DataStore、应用根节点生命周期感知状态收集及系统栏明暗。
- `core/navigation/SleepCatNavHost.kt`：首页/回顾/我的三 Tab，首页进入清单二级页，二级页隐藏底栏，顶部及系统返回可用。
- `core/designsystem/theme/`：统一颜色、字体、形状、间距；六套明暗 ColorScheme；共享卡片、按钮、页面容器及两套组件 Preview。
- `feature/`：四个基础页面；设置为真实功能，其余业务内容明确标为样例/演示。准备按钮打开未开放说明。
- `data/preferences/`、`SettingsViewModel.kt`：Repository → StateFlow<SettingsUiState> → UI；Loading、I/O Error、重试；稳定字符串持久化与未知值回退。
- `README.md`、设计记录、验收步骤、截图与本报告。

APK：`app/build/outputs/apk/debug/app-debug.apk`，约 28 MiB，开发签名。可安装到 API 29+ 设备。本次未生成发布签名包。

## 2. 构建与测试

本机初始仅有 JDK 17。已安装 SDK 35、Build Tools 35.0.0、adb、模拟器及 Android 15 x86_64 镜像到 `.tools/android-sdk`；本机路径在被忽略的 `local.properties` 中。Gradle 下载遇到官方跳转连接问题，镜像包经官方 SHA-256 校验一致后使用。

最终执行的完整命令（代理仅用于本机，不写入工程配置）：

```powershell
$env:JAVA_HOME='D:/Develop/java17'
.\gradlew.bat '-Dhttps.proxyHost=127.0.0.1' '-Dhttps.proxyPort=7897' `
  '-Dhttp.proxyHost=127.0.0.1' '-Dhttp.proxyPort=7897' `
  :app:assembleDebug :app:testDebugUnitTest `
  :app:connectedDebugAndroidTest :app:lintDebug --console=plain --max-workers=2
```

| 检查 | 结果 |
|---|---|
| assembleDebug | 通过，生成可安装 APK |
| testDebugUnitTest | 5 项通过：默认值、未知值独立回退、稳定 key、三模式解析 |
| connectedDebugAndroidTest | Android 15 模拟器 6 项通过：2 项 UI/导航、3 项真实 DataStore、1 项写入失败恢复 |
| lintDebug | 通过，0 errors / 27 warnings，全部为可选依赖版本升级提示，不关闭检查 |
| 安装与冷启动 | adb install / am start -W 通过，无启动崩溃 |
| 杀进程重启 | 雾蓝 + 深色在 am force-stop 后重新启动仍保持 |
| 跟随系统 | cmd uimode night yes/no，界面及系统栏随系统切换 |
| 小屏大字体 | 880×1760 / 440dpi（320dp 宽），font_scale=2.0；四页可滚动，最后任务、周日和深色选项可触达 |
| 横屏 | 2340×1080；内容限宽居中、可滚动，底栏与按钮可用 |
| APK 权限 | aapt dump permissions 仅有 AndroidX 内部签名权限，无网络/通知/闹钟/悬浮窗权限 |
| 崩溃日志 | adb logcat -b crash -d 无输出 |

测试过程曾真实观察到主题 stub 的 7 项失败及缺失导航的 2 项失败，然后实现并通过。持久化测试最初在 Windows JVM 遇到文件替换 IOException；迁移到实际 Android 文件系统测试后全部通过，没有屏蔽失败。独立代码审查发现同值写入成功可能保留旧错误提示，新增测试确认失败后修复，并通过最终全套验证。

原始构建和自动测试报告在 `app/build/reports/`；可留存证据在 `docs/verification/`，截图在 `docs/screenshots/`。完整手工复现步骤见 `P0-acceptance.md`。

## 3. 主题机制

- Palette：warm / mint / blue；Mode：system / light / dark；默认 warm + system。
- DataStore 文件 `theme.preferences_pb`，两个独立字符串字段 `theme_palette`、`theme_mode`，单字段原子更新，不使用 enum ordinal。
- 非法字段独立回退；损坏文件恢复默认值；普通读写错误显示提示，不假装保存成功。
- 设置事件经 ViewModel/Repository 写入，应用根节点只收集一份 StateFlow，再通过 SleepCatTheme 的 MaterialTheme 影响全部页面。明确选择的配色不会被动态取色覆盖。
- 页面无品牌色硬编码；猫咪 vector 为固定色插画占位。暖橘浅色的按钮色较原型加深以改善可读性。

## 4. 与 HTML 原型的差别

不使用 WebView，不移植 JS/localStorage。首次启动直接进入首页；保留三个 Tab 及清单二级页；目标时间暂为只读 00:00，准备时间 23:00。清单只读、回顾数据显著标为演示，不生成用户历史；猫咪使用本地简洁矢量占位。

未实现完整首次设置、时间滚轮、睡前准备流程、次晨反馈、动画、清单编辑、账户、后端和所有系统提醒能力；未添加假权限或提醒开关。P1 未自动实施。

## 5. 已知问题与待确认

**技术覆盖边界**：本轮实际运行验证仅覆盖 Android 15 模拟器，未实测 Android 10/API 29、其他厂商真机、TalkBack 和商店发布流程。使用已核验稳定依赖组合，未追逐最新版，升级提示留待统一评估。启动异步读取期间允许短暂使用默认主题。当前文案仅简体中文。

**待产品确认**：目标时间默认值、准备流程的正式交互、任务完成状态的每日归属/跨日规则、薄荷绿/雾蓝及暗色映射、正式猫咪素材和最终 applicationId（当前 com.sleepcat.app）。这些决定不阻塞当前框架。

## 6. 下一步建议（未实施）

1. P1：引入 Room，完成本地任务新增/编辑/删除/排序，并让首页共享同一 TaskRepository 数据源。
2. 明确每日完成与跨日归属，再设计任务记录及周回顾真实数据模型。
3. 补 API 29 与一台实际设备的兼容性验收，确认正式配色和素材。
