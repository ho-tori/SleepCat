package com.sleepcat.app

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModelStore
import com.sleepcat.app.data.preferences.*
import com.sleepcat.app.feature.settings.SettingsViewModel
import java.io.IOException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsViewModelTest {
    @Test fun successfulSameValueWriteClearsPreviousWriteError() = runBlocking {
        val store = FailingOnceStore()
        val owner = ViewModelStore()
        val model = withContext(Dispatchers.Main) {
            SettingsViewModel(ThemePreferencesRepository(store)).also { owner.put("settings", it) }
        }
        try {
            withTimeout(3000) { model.uiState.first { !it.loading } }
            store.failNextWrite = true
            withContext(Dispatchers.Main) { model.setPalette(ThemePalette.BLUE) }
            withTimeout(3000) { model.uiState.first { it.error != null } }
            assertEquals(ThemePalette.WARM, model.uiState.value.preference.palette)
            // A successful write of the existing value emits no new Flow value.
            withContext(Dispatchers.Main) { model.setPalette(ThemePalette.WARM) }
            withTimeout(3000) { model.uiState.first { it.error == null } }
        } finally { withContext(Dispatchers.Main) { owner.clear() } }
        Unit
    }

    private class FailingOnceStore : DataStore<Preferences> {
        override val data = MutableStateFlow(preferencesOf(stringPreferencesKey("theme_palette") to "warm"))
        var failNextWrite = false
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
            if (failNextWrite) {
                failNextWrite = false
                throw IOException("Simulated disk write failure")
            }
            val changed = transform(data.value)
            data.value = changed
            return changed
        }
    }
}
