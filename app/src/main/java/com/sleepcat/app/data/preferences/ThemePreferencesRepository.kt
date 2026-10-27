package com.sleepcat.app.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.map

class ThemePreferencesRepository(private val store: DataStore<Preferences>) {
    val theme = store.data.map { decodeTheme(it[PALETTE], it[MODE]) }

    suspend fun setPalette(palette: ThemePalette) {
        store.edit { it[PALETTE] = palette.key }
    }

    suspend fun setMode(mode: ThemeMode) {
        store.edit { it[MODE] = mode.key }
    }

    private companion object {
        val PALETTE = stringPreferencesKey("theme_palette")
        val MODE = stringPreferencesKey("theme_mode")
    }
}
