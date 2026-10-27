package com.sleepcat.app.data.preferences

import org.junit.Assert.*
import org.junit.Test

class ThemePreferenceTest {
    @Test fun missingValuesUseWarmSystem() {
        assertEquals(ThemePreference(), decodeTheme(null, null))
    }

    @Test fun unknownPaletteDoesNotEraseValidMode() {
        assertEquals(ThemePreference(ThemePalette.WARM, ThemeMode.DARK), decodeTheme("old", "dark"))
    }

    @Test fun unknownModeDoesNotEraseValidPalette() {
        assertEquals(ThemePreference(ThemePalette.MINT, ThemeMode.SYSTEM), decodeTheme("mint", "old"))
    }

    @Test fun allStableKeysRoundTrip() {
        for (palette in ThemePalette.entries) for (mode in ThemeMode.entries) {
            assertEquals(ThemePreference(palette, mode), decodeTheme(palette.key, mode.key))
        }
    }

    @Test fun explicitModeOverridesSystemIndependentlyOfPalette() {
        for (palette in ThemePalette.entries) {
            assertFalse(ThemePreference(palette, ThemeMode.LIGHT).isDark(true))
            assertTrue(ThemePreference(palette, ThemeMode.DARK).isDark(false))
            assertTrue(ThemePreference(palette, ThemeMode.SYSTEM).isDark(true))
            assertFalse(ThemePreference(palette, ThemeMode.SYSTEM).isDark(false))
        }
    }
}
