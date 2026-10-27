package com.sleepcat.app.data.preferences

enum class ThemePalette(val key: String, val label: String) {
    WARM("warm", "暖橘"), MINT("mint", "薄荷绿"), BLUE("blue", "雾蓝")
}

enum class ThemeMode(val key: String, val label: String) {
    SYSTEM("system", "跟随系统"), LIGHT("light", "浅色"), DARK("dark", "深色")
}

data class ThemePreference(
    val palette: ThemePalette = ThemePalette.WARM,
    val mode: ThemeMode = ThemeMode.SYSTEM,
)

internal fun decodeTheme(palette: String?, mode: String?): ThemePreference = ThemePreference(
    palette = ThemePalette.entries.firstOrNull { it.key == palette } ?: ThemePalette.WARM,
    mode = ThemeMode.entries.firstOrNull { it.key == mode } ?: ThemeMode.SYSTEM,
)

fun ThemePreference.isDark(systemDark: Boolean): Boolean = when (mode) {
    ThemeMode.SYSTEM -> systemDark
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
