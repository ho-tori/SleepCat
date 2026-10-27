# 在当前环境使用模拟器演示 SleepCat

适用环境：Windows + PowerShell，项目目录 `E:\Term3.1\SleepCat`。

本机已准备好 Android SDK、Android 15 虚拟设备 `SleepCat_API35` 和 Debug APK，无需另外安装 Android Studio。以下路径与虚拟设备名称适用于当前电脑；换电脑时需要重新安装 SDK 并创建虚拟设备。

## 1. 打开模拟器窗口

在 IDE 的 PowerShell 终端执行：

```powershell
cd E:\Term3.1\SleepCat
$env:ANDROID_HOME="$PWD\.tools\android-sdk"

Start-Process -FilePath "$env:ANDROID_HOME\emulator\emulator.exe" `
  -ArgumentList '-avd','SleepCat_API35','-gpu','swiftshader','-memory','1536','-cores','2'
```

等待模拟器出现 Android 桌面。首次启动可能较慢。此命令会打开可见窗口，方便鼠标操作和现场演示；不要重复启动同一个虚拟设备。

## 2. 安装并打开应用

在同一个终端执行：

```powershell
$adb="$env:ANDROID_HOME\platform-tools\adb.exe"

& $adb wait-for-device
& $adb install -r .\app\build\outputs\apk\debug\app-debug.apk
& $adb shell am start -n com.sleepcat.app/.MainActivity
```

`wait-for-device` 只等待 adb 连接，不代表 Android 已完全启动。如果安装时报设备未就绪，等待桌面出现后重试安装和启动命令。

`install -r` 会覆盖安装并保留原有应用数据，包括主题选择。APK 为开发签名版本。

## 3. 建议演示顺序

1. 首页 →「查看睡前清单」→ 返回首页。
2. 点击「开始准备」，展示尚未开放的说明。
3. 切到「回顾」，展示明确标注的演示周数据。
4. 切到「我的」，切换暖橘、薄荷绿、雾蓝，以及浅色、深色、跟随系统。
5. 返回首页、清单和回顾，展示全局主题变化。
6. 选择一种非默认主题，按下节命令重启，展示选择仍被保留。

当前仅实现 P0 应用框架，目标时间、清单和回顾为演示内容，不包含真实提醒或任务编辑能力。

## 4. 演示主题持久化

在设置页选择例如「雾蓝 + 深色」，等待当前选择文字更新，然后执行：

```powershell
& $adb shell am force-stop com.sleepcat.app
& $adb shell am start -n com.sleepcat.app/.MainActivity
```

重新打开后应保持雾蓝深色；进入「我的」可检查选中项。

如需展示「跟随系统」，先在应用中选择该模式，再执行：

```powershell
# 切换模拟器为深色
& $adb shell cmd uimode night yes

# 切换模拟器为浅色
& $adb shell cmd uimode night no
```

这些命令会改变模拟器的系统明暗设置。显式选择应用浅色或深色时，应用应保持自己的选择。

## 5. 后续再次演示

打开模拟器后，直接点击应用列表中的 **SleepCat** 图标即可，不必每次安装。

修改代码后，需要重新构建并覆盖安装：

```powershell
$env:JAVA_HOME='D:/Develop/java17'
.\gradlew.bat :app:assembleDebug

& $adb install -r .\app\build\outputs\apk\debug\app-debug.apk
& $adb shell am force-stop com.sleepcat.app
& $adb shell am start -n com.sleepcat.app/.MainActivity
```

如果构建需要通过本机已有代理下载依赖，可以使用本次开发环境的代理参数：

```powershell
.\gradlew.bat '-Dhttps.proxyHost=127.0.0.1' '-Dhttps.proxyPort=7897' `
  '-Dhttp.proxyHost=127.0.0.1' '-Dhttp.proxyPort=7897' :app:assembleDebug
```

仅在本机代理正在监听 `7897` 端口时使用；代理端口变化后需相应修改。

## 6. 常用检查与关闭

```powershell
# 列出已有虚拟设备
& "$env:ANDROID_HOME\emulator\emulator.exe" -list-avds

# 查看 adb 连接状态
& $adb devices

# 检查系统是否启动完成：输出 1 表示完成
& $adb shell getprop sys.boot_completed

# 关闭模拟器，也可以直接关闭窗口
& $adb emu kill
```

如果同时连接了多个设备，在 adb 参数前加 `-s` 指定目标，例如：

```powershell
& $adb -s emulator-5554 shell am start -n com.sleepcat.app/.MainActivity
```

设备编号以 `adb devices` 的实际输出为准。新开终端后需重新执行第 1 节的目录、环境变量设置，以及第 2 节的 `$adb` 赋值。

更完整的功能验收见 [P0 手工验收步骤](P0-acceptance.md)。
