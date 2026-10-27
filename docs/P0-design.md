# SleepCat P0 设计与执行清单

日期：2026-10-27。需求依据：用户提供的 v0.1 任务书；视觉参考：prototype/SleepCat_V5.13_交互原型.html。

## 设计决定

只实现 P0。原目录只有原型，没有已有代码或 Git 仓库。采用单 app 模块、Kotlin、Compose Material 3、Navigation Compose、Preferences DataStore、简单 AppContainer。minSdk 29，compile/targetSdk 35，JDK 17。暂不引入 Room、Hilt、网络依赖及任何权限。

相比 XML View 或多模块架构，Compose 单模块更符合任务书且维护成本低。相比每页保存主题，应用根节点统一观察 SettingsViewModel 的 StateFlow，避免不一致。ThemePalette 与 ThemeMode 保存独立稳定字符串，未知值分别回退暖橘/跟随系统。读取异常显示提示并允许重试，写入失败不假装保存成功。

三个主 Tab：首页/回顾/我的；清单是首页二级页面，隐藏底栏并提供返回。顶部目标时间暂用原型的 00:00，样例任务只读；准备按钮展示明确的 P0 占位说明。回顾采用带显著演示标识的周视图，不写历史记录。

语义颜色集中于 core/designsystem/theme，六组明暗色板；浅色强调色略加深以保证按钮文字对比度。卡片、按钮、页面容器复用，页面可滚动、限宽并处理系统栏留白。无需图片生成，采用可替换的本地猫咪矢量占位。

## 实施计划

- [x] 1. 工程与测试基础：Gradle wrapper/version catalog、Manifest、MainActivity；assembleDebug。
- [x] 2. 主题与持久化：ThemePreference、ThemePreferencesRepository、SettingsViewModel；测试未知值回退、独立更新、重新创建 DataStore 后恢复。
- [x] 3. 设计系统与四页：SleepCatTheme、共享组件、Navigation Compose；导航、返回、9 种主题选择、准备占位 UI 测试。
- [x] 4. 验证与交付：assembleDebug、testDebugUnitTest、lintDebug、模拟器检查、README 与实施报告。

## 检查重点

1. 两个主题维度互不覆盖，快速连续更新后最终值正确。
2. 非法旧配置不会崩溃且不会破坏另一维度。
3. 杀进程重启恢复，系统暗色改变即时响应。
4. 二级页返回与重复点主 Tab 不堆积返回栈。
5. 小屏、横屏、大字体可以滚动；深色系统栏和正文可读。

## 执行记录

- 用户明确要求检查后直接实施，设计/计划记录后直接执行，不增加审批环节。
- 本地存在 JDK 17；默认位置无 SDK、Gradle、adb。将工具隔离安装在忽略目录 .tools，local.properties 指向本机 SDK，不修改全局设置。
- 任务书允许 P0 手工验收；针对持久化和导航补充自动测试，避免给纯展示包装组件添加无价值测试。
- 已完成工具安装；Gradle 镜像包与官方 SHA-256 一致。Java 构建命令显式使用本机已有代理。工具不要求提交到版本库。
- 初始主题 stub 运行 8 项测试，7 项按预期失败；实现模型与读写后，模型 5 项通过。Windows 上 DataStore 文件替换失败，将 3 项真实文件测试移至 Android instrumentation，均通过。
- 初始首页占位应用运行导航测试，2 项因缺失导航入口按预期失败；接通导航后通过。
- 独立审查发现同值写入成功后旧错误提示可能残留；增加回归测试，确认超时失败后修复，最终 Android 测试共 6 项通过。
- 依赖采用已核验的稳定组合，不追逐最新版；保留 lint 版本升级提示供后续集中升级。
