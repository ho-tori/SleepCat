# SleepCat 原生还原交付记录

日期：2026-11-06。在现有工程上修改，使用 Kotlin + Jetpack Compose；没有 WebView 或整页截图 UI。保留 Application、Gradle、主题 DataStore 等基础设施。

原型见 [reference.html](reference.html)，开发前页面映射、设计参数和交互清单见 [audit.md](audit.md)。本记录明确区分已实现、已验证和剩余差异，不宣称逐像素一致或全部 P1 验收通过。

## 页面实现与视觉对照

浏览器实际渲染：393×852 CSS px、DPR 1；Android 15 模拟器：393×852 px、density 160、fontScale 1。按相同逻辑宽度对照。真实系统状态栏和手势条没有伪装为 HTML 的 9:41。

参考截图在 screenshots/，运行截图在 android-screenshots/。已查看全部 15 页并按差异修正，关键编辑态和弹层另有截图。

| 页面 | 原生实现及修正 |
|---|---|
| welcome | 原猫图、标题、设置/直接体验；修正行高和插图位置 |
| goal | 设置进度、定时空态、虚线边框、新增和下一步 |
| schedules | 全部定时、空态、新增；修正顶部间距 |
| scheduleEditor | 名称、日期归属、双端作息环、保存/取消/删除；修正底栏遮挡保存按钮 |
| tasks | 准备时间、事项选择、自定义、用时校验、排序 |
| mode | 三种模式、预览、间隔；重建混合提醒缩略图 |
| permissions | 模拟权限、缺权限提示、降级/开启；合并为原型单张卡片 |
| home | 连续桃色背景、猫爪摘要/展开/编辑、完成/撤销、内联作息环 |
| tasksEdit | 日常清单编辑，返回来源页 |
| routine | 勾选、未完成确认、晚安倒计时返回；通过调试入口验证 |
| outside | 模拟动态、轻提醒、全屏提醒、延后升级、今晚跳过 |
| review | 七日上下柱图、记录详情、原因、建议；修正图例和单位 |
| morning | 原图、滚轮、多选原因、保存；修正选项换行和字号 |
| reminderSettings | 模式、只读预览、间隔和模拟权限 |
| settings | 三个设置入口、猫图、重置确认 |

## 交互与状态验证

- 设备测试：Tab 导航、日常清单入口、晨间反馈修改、自定义事项校验及恢复、定时保存/删除、预览禁用及返回。
- 草稿回归：定时和今晚清单草稿在 Activity 重建后保留；取消不写入已保存数据。
- DataStore：配置、临时事项、完成时间和临时作息读写；中断首次设置恢复到 goal，已设置用户恢复到 schedules。
- 实际 adb 触摸：环起点由 00:00 拖至 05:45、起床仍为 08:00；仅今晚保存并重启；延后两次升级全屏；跳过确认；未完成清单确认及倒计时返回。见 android-screenshots/interaction-checks.json。
- 单元测试：05:00 日期边界、跨午夜、星期归属、清单排序和滚轮分钟取整。
- 已修复返回栈堆积、冷启动来源错误、分钟向下取整、前台跨日刷新和过期草稿回写。

## 构建及安装

Java 17、SDK 35、Android 15 模拟器。

```text
gradlew.bat assembleDebug testDebugUnitTest connectedDebugAndroidTest lintDebug --offline --console=plain
```

11 项单元测试、13 项设备测试已通过；已实际安装 APK 并运行。APK 位于 app/build/outputs/apk/debug/app-debug.apk，构建产物未纳入 Git。

Lint 无错误，保留 Compose Modifier 参数约定、offset/滚轮读取优化及 Bitmap KTX 建议等警告。WebP 扩展名已修正，未使用的 ring_cat_img 已移除。

## 主要文件

- data/prototype/：演示模型、日期规则、独立 DataStore、ViewModel。
- core/designsystem/prototype/：设计参数、自定义组件、原始 SVG 路径、图片缩放、作息环。
- feature/prototype/：15 个页面及编辑事务、弹层。
- core/navigation/SleepCatNavHost.kt、SleepCatApp.kt：导航、返回、生命周期、浅色系统栏。
- src/test/、src/androidTest/：规则、持久化与界面回归。
- docs/prototype/：原型、审查、截图与复现脚本。

## 尚存差异与未覆盖项

1. Windows Chromium 使用 Microsoft YaHei/系统字体，Android 使用设备字体；中文字形、数字字重和部分文本高度有差异。原型没有附可分发字体，未复制系统商业字体。
2. 保留原型使用的 Emoji 原字符，Android 图案与 Windows 不同；导航、猫爪和作息环使用路径绘制。
3. 小图锯齿已采用分级采样修正；部分阴影、渐变中心、遮罩和全屏提醒系统栏尚未逐像素对齐。
4. 排序采用 680ms 位移，尚未完整复现斜向 FLIP、720ms 路径点亮及所有过渡；未做逐帧动画验收。
5. 未覆盖全部拖动/左滑边界、多点触控、日期冲突组合、窄屏/大字体/多密度矩阵。05:00 规则已测，未进行真实跨越 05:00 的长时间运行测试。
6. 原型没有深色设计，原型页面保持浅色；保留既有主题存储及测试，没有新增主题入口。不能声称已验收深浅两套视觉。
7. 回顾底部标注“本地演示数据 · 非真实睡眠记录”，用于明确模拟数据性质，是有记录的文案差异。

## 原型歧义及边界

- 最终脚本移除了首页外部提醒入口，routine/晚安不再从首页直达；没有擅自加回按钮。debug 可用 prototype_page intent extra 验证这些页面，release 不启用调试入口。
- 权限、通知、覆盖其他 App 都是模拟；没有真实悬浮权限或后台闹钟服务。真实能力需要另行确认触发规则。
- 今晚跳过按 05:00 重置，采用与今晚清单一致的最小实现。
- 周记录为固定演示数据，不生成未经采集的真实睡眠记录。
- 冷启动失去预览上下文时返回提醒设置/模式页，避免把只读预览变为可操作提醒。
