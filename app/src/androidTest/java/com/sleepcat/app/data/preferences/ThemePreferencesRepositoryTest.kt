package com.sleepcat.app.data.preferences

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ThemePreferencesRepositoryTest {
    @get:Rule val folder = TemporaryFolder()

    @Test fun preferencesSurviveClosingAndReopeningStore() = runBlocking {
        val file = folder.newFolder().resolve("theme.preferences_pb")
        var job = SupervisorJob()
        var store = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file }
        try {
            val repository = ThemePreferencesRepository(store)
            repository.setPalette(ThemePalette.BLUE)
            repository.setMode(ThemeMode.DARK)
            assertEquals(ThemePreference(ThemePalette.BLUE, ThemeMode.DARK), repository.theme.first())
            job.cancelAndJoin()
            job = SupervisorJob()
            store = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file }
            assertEquals(ThemePreference(ThemePalette.BLUE, ThemeMode.DARK), ThemePreferencesRepository(store).theme.first())
        } finally { job.cancelAndJoin() }
    }

    @Test fun concurrentDimensionWritesDoNotLoseEachOther() = runBlocking {
        val job = SupervisorJob()
        val file = folder.newFolder().resolve("theme.preferences_pb")
        val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file }
        try {
            val repository = ThemePreferencesRepository(store)
            coroutineScope {
                launch { repository.setPalette(ThemePalette.MINT) }
                launch { repository.setMode(ThemeMode.LIGHT) }
            }
            assertEquals(ThemePreference(ThemePalette.MINT, ThemeMode.LIGHT), repository.theme.first())
            repository.setPalette(ThemePalette.BLUE)
            assertEquals(ThemePreference(ThemePalette.BLUE, ThemeMode.LIGHT), repository.theme.first())
        } finally { job.cancelAndJoin() }
    }

    @Test fun invalidStoredValuesFallBackPerDimension() = runBlocking {
        val job = SupervisorJob()
        val file = folder.newFolder().resolve("theme.preferences_pb")
        val store = PreferenceDataStoreFactory.create(scope = CoroutineScope(Dispatchers.IO + job)) { file }
        try {
            store.edit {
                it[stringPreferencesKey("theme_palette")] = "retired-palette"
                it[stringPreferencesKey("theme_mode")] = "dark"
            }
            assertEquals(ThemePreference(ThemePalette.WARM, ThemeMode.DARK), ThemePreferencesRepository(store).theme.first())
        } finally { job.cancelAndJoin() }
    }
}
