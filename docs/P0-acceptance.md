# P0 可执行验收步骤

在 API 29+ 的测试设备安装 Debug APK。若同时连接多个设备，给 adb 加 `-s <serial>`。

## 导航

1. 启动：`adb shell am start -W -n com.sleepcat.app/.MainActivity`。首页显示目标 00:00、清单入口和准备按钮，无权限弹窗。
2. 点击「查看睡前清单」：只读 3 项、预计用时，顶部返回按钮，无第四个底部 Tab。点返回/系统返回均回首页。
3. 「回顾」显示「演示数据 · 非真实记录」与七天演示差值；「我的」显示主题设置。反复点同一 Tab 不堆栈；回顾/我的按系统返回回首页。
4. 首页「开始准备」弹出「准备流程尚未开放」，关闭后仍留在首页，不启动任何系统功能。

## 主题与进程重启

1. 我的中依次选择三种配色，每种配色分别选择跟随系统/浅色/深色。当前状态文字与选择一致，改变一个维度不改变另一个。
2. 每个组合访问首页、清单、回顾、我的，检查背景/卡片/文字/按钮同步变化且可读。
3. 选择雾蓝 + 深色，等当前文字更新。执行：
   ```powershell
   adb shell am force-stop com.sleepcat.app
   adb shell am start -W -n com.sleepcat.app/.MainActivity
   ```
   启动后仍是雾蓝深色；打开我的，确认文字与选中项。
4. 切至跟随系统。执行 `adb shell cmd uimode night yes`，检查立即变暗；`adb shell cmd uimode night no` 后变浅。显式选择浅色时再改变系统模式，应仍为浅色。测试完恢复设备原模式。
5. 未知值回退、并发更新、关闭并重新创建真实 DataStore 由 `ThemePreferencesRepositoryTest` 验证，不手工修改二进制 protobuf 文件。

## 布局与权限

1. 默认手机尺寸、320dp 宽屏、横屏分别查看四页；内容可滚动，最后一项可触达，状态栏/导航栏不遮挡。
2. 系统字体调至 1.3 和 2.0，检查设置选项和按钮仍能操作，长内容可滚动。
3. `adb shell dumpsys package com.sleepcat.app` 检查权限：无 Internet、通知、悬浮窗、闹钟、使用情况权限。AndroidX 可能合并签名级内部广播保护权限，它不授权通知或后台业务能力。
4. `adb logcat -b crash -d` 检查测试期间本应用无异常退出。

自动化：`./gradlew :app:testDebugUnitTest :app:connectedDebugAndroidTest`。自动化覆盖主题回退/恢复/独立更新、核心导航、占位说明、九种选择组合与 Activity 重建；不能替代真实进程重启和视觉检查。实际执行结果以 P0-report.md 为准。
