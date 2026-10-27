package com.sleepcat.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepcat.app.data.preferences.*
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preference: ThemePreference = ThemePreference(),
    val loading: Boolean = true,
    val error: String? = null,
)

class SettingsViewModel(private val repository: ThemePreferencesRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(SettingsUiState())
    val uiState = mutableState.asStateFlow()
    private var observation: Job? = null

    init { retryRead() }

    fun retryRead() {
        observation?.cancel()
        observation = viewModelScope.launch {
            mutableState.update { it.copy(loading = true, error = null) }
            try {
                repository.theme.collect { preference ->
                    mutableState.update { it.copy(preference = preference, loading = false, error = null) }
                }
            } catch (_: IOException) {
                mutableState.update { it.copy(loading = false, error = "无法读取主题设置，正在使用当前配色。请重试。") }
            }
        }
    }

    fun setPalette(palette: ThemePalette) = save { repository.setPalette(palette) }
    fun setMode(mode: ThemeMode) = save { repository.setMode(mode) }

    private fun save(write: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                write()
                mutableState.update { it.copy(error = null) }
                // A failed read ends collection; restart it after a successful recovery write.
                if (observation?.isActive != true) retryRead()
            } catch (_: IOException) {
                mutableState.update { it.copy(error = "主题未保存，请重新选择或稍后重试。") }
            }
        }
    }
}
