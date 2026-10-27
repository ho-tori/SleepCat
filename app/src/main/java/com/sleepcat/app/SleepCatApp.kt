package com.sleepcat.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sleepcat.app.core.designsystem.theme.SleepCatTheme
import com.sleepcat.app.core.navigation.SleepCatNavHost
import com.sleepcat.app.data.preferences.isDark
import com.sleepcat.app.feature.settings.SettingsViewModel

@Composable
fun SleepCatApp(container: AppContainer, onSystemBars: (Boolean) -> Unit) {
    val settings: SettingsViewModel = viewModel(factory = viewModelFactory {
        initializer { SettingsViewModel(container.themeRepository) }
    })
    val state by settings.uiState.collectAsStateWithLifecycle()
    val dark = state.preference.isDark(isSystemInDarkTheme())
    SideEffect { onSystemBars(dark) }
    SleepCatTheme(state.preference) {
        SleepCatNavHost(state, settings::setPalette, settings::setMode, settings::retryRead)
    }
}
