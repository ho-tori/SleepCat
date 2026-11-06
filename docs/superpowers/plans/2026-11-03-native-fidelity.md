# SleepCat Native Fidelity Implementation Plan

Goal: implement the supplied SleepCat.html using native Compose, preserving final CSS/JS behavior.
Architecture: existing app container, Navigation and theme repository remain. Add immutable business models, a dedicated DataStore demo repository, and custom Compose components. HTML is documentation only, never an app runtime dependency.
Stack: existing Kotlin 2.1.20 / Compose BOM 2025.04.01 / SDK 35 / Java 17.
Spec: E:/Term3.1/人机交互/docs/prototype/audit.md; source: E:/Term3.1/人机交互/SleepCat.html.
Execution: inline in the current session; user authorized the existing project.

Global constraints: preserve copy, original raster illustrations and SVG paths; no WebView; no full-screen screenshots as UI. Light prototype is the visual baseline; retain existing theme infrastructure separately.

Review focus: midnight rollover at 05:00; draft cancel must not mutate saved state; weekday ownership is exclusive; restored pages must not require missing drafts; completed task order and cancellation must remain stable.

- [ ] Domain: add `data/prototype/Models.kt`, `PrototypeRepository.kt`, `PrototypeViewModel.kt`; test `PrototypeRulesTest.kt` for night key, effective schedule, exclusive weekday ownership, completed order, draft isolation and duration bounds.
- [ ] Components: `core/designsystem/prototype/Primitives.kt`, `SleepDial.kt`; exact tokens, extracted PNGs and vector paths, custom buttons/dialogs/wheels; compile and screenshot.
- [ ] Onboarding and settings: welcome, schedule list/editor, daily task grid/editor, reminder modes/permissions/previews. Validate input and back/cancel behavior.
- [ ] Home: current task, mini/expanded paw paths, quick undo, batch completion, swipe and drag draft editor, inline ring and scope confirmation.
- [ ] Review: editable demo records, diverging weekly bars, details, morning reasons, weekly suggestion, routine and reversible goodnight.
- [ ] Navigation: extend existing host, restore tab/scroll/page state, lifecycle night rollover, save errors. Keep theme repository and tests.
- [ ] Verification: Gradle unit tests, debug APK, lint; emulator install, instrumentation interactions and persistence, calibrated screenshots for all routes and representative states; correct observed differences and report remaining ones.
