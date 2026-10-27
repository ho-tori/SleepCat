package com.sleepcat.app.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.sleepcat.app.data.preferences.ThemePalette

// Each palette owns its complete surface family so Material defaults cannot leak purple.
private fun light(primary: Long, container: Long, background: Long, surface: Long, ink: Long, muted: Long, outline: Long) = lightColorScheme(
    primary = Color(primary), onPrimary = Color.White,
    primaryContainer = Color(container), onPrimaryContainer = Color(ink),
    secondary = Color(primary), onSecondary = Color.White,
    secondaryContainer = Color(container), onSecondaryContainer = Color(ink),
    tertiary = Color(primary), onTertiary = Color.White,
    tertiaryContainer = Color(container), onTertiaryContainer = Color(ink),
    background = Color(background), onBackground = Color(ink),
    surface = Color(surface), onSurface = Color(ink),
    surfaceVariant = Color(container), onSurfaceVariant = Color(muted),
    surfaceDim = Color(background), surfaceBright = Color(surface),
    surfaceContainerLowest = Color(surface), surfaceContainerLow = Color(surface),
    surfaceContainer = Color(background), surfaceContainerHigh = Color(container),
    surfaceContainerHighest = Color(container),
    outline = Color(outline), outlineVariant = Color(container),
    inverseSurface = Color(ink), inverseOnSurface = Color(surface), inversePrimary = Color(container),
    surfaceTint = Color(primary),
)

private fun dark(primary: Long, container: Long, background: Long, surface: Long, ink: Long, muted: Long, outline: Long) = darkColorScheme(
    primary = Color(primary), onPrimary = Color(background),
    primaryContainer = Color(container), onPrimaryContainer = Color(ink),
    secondary = Color(primary), onSecondary = Color(background),
    secondaryContainer = Color(container), onSecondaryContainer = Color(ink),
    tertiary = Color(primary), onTertiary = Color(background),
    tertiaryContainer = Color(container), onTertiaryContainer = Color(ink),
    background = Color(background), onBackground = Color(ink),
    surface = Color(surface), onSurface = Color(ink),
    surfaceVariant = Color(container), onSurfaceVariant = Color(muted),
    surfaceDim = Color(background), surfaceBright = Color(container),
    surfaceContainerLowest = Color(background), surfaceContainerLow = Color(surface),
    surfaceContainer = Color(surface), surfaceContainerHigh = Color(container),
    surfaceContainerHighest = Color(container),
    outline = Color(outline), outlineVariant = Color(container),
    inverseSurface = Color(ink), inverseOnSurface = Color(background), inversePrimary = Color(container),
    surfaceTint = Color(primary),
)

private val warmLight = light(0xFFA65325, 0xFFFCE8D9, 0xFFFAF6F0, 0xFFFFFCF7, 0xFF392C29, 0xFF74615A, 0xFF968078)
private val warmDark = dark(0xFFF2B48C, 0xFF573B2C, 0xFF211A17, 0xFF2C231F, 0xFFF5E6DC, 0xFFD1BEB3, 0xFFA38B7E)
private val mintLight = light(0xFF286B53, 0xFFDCEFE4, 0xFFF3F8F3, 0xFFFAFDFA, 0xFF253830, 0xFF52685B, 0xFF7A8C80)
private val mintDark = dark(0xFF9DD6B8, 0xFF294C3C, 0xFF17221C, 0xFF202E26, 0xFFE0EFE4, 0xFFB6CBBE, 0xFF879E8F)
private val blueLight = light(0xFF3C638A, 0xFFDFEAF6, 0xFFF3F6FA, 0xFFFAFCFF, 0xFF293745, 0xFF586879, 0xFF8291A2)
private val blueDark = dark(0xFFABCAEC, 0xFF2D455F, 0xFF19212B, 0xFF232E3B, 0xFFE3EBF6, 0xFFBDCBDE, 0xFF8A9BB2)

fun sleepCatColorScheme(palette: ThemePalette, dark: Boolean): ColorScheme = when (palette) {
    ThemePalette.WARM -> if (dark) warmDark else warmLight
    ThemePalette.MINT -> if (dark) mintDark else mintLight
    ThemePalette.BLUE -> if (dark) blueDark else blueLight
}
