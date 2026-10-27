package com.sleepcat.app

import android.app.Application
import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.sleepcat.app.data.preferences.ThemePreferencesRepository

private val Context.themeStore by preferencesDataStore(
    name = "theme",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
)

class AppContainer(context: Context) {
    val themeRepository = ThemePreferencesRepository(context.applicationContext.themeStore)
}

class SleepCatApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
