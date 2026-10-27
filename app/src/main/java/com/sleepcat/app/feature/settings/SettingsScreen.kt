package com.sleepcat.app.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.sleepcat.app.core.designsystem.component.*
import com.sleepcat.app.data.preferences.*

@Composable
fun SettingsScreen(state: SettingsUiState, onPalette: (ThemePalette) -> Unit, onMode: (ThemeMode) -> Unit, onRetry: () -> Unit) {
    SleepCatPage {
        PageHeading("我的", "选择让你感到舒服的颜色。")
        Text("当前：${state.preference.palette.label} · ${state.preference.mode.label}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        state.error?.let {
            SleepCatCard {
                Text(it, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onRetry) { Text("重试读取") }
            }
        }
        SleepCatCard {
            Text("主题配色", style = MaterialTheme.typography.titleLarge)
            Column(Modifier.selectableGroup()) {
                ThemePalette.entries.forEach { palette ->
                    ChoiceRow(palette.label, state.preference.palette == palette, !state.loading) { onPalette(palette) }
                }
            }
        }
        SleepCatCard {
            Text("显示模式", style = MaterialTheme.typography.titleLarge)
            Column(Modifier.selectableGroup()) {
                ThemeMode.entries.forEach { mode ->
                    ChoiceRow(mode.label, state.preference.mode == mode, !state.loading) { onMode(mode) }
                }
            }
        }
        Text("配色与明暗独立保存，重启后仍会保留。跟随系统时，明暗随设备设置变化。", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        Text("SleepCat 0.1.0 · 本地应用框架", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ChoiceRow(label: String, selected: Boolean, enabled: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 52.dp).selectable(selected, enabled = enabled, role = Role.RadioButton, onClick = onClick).padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null, enabled = enabled)
        Spacer(Modifier.width(12.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
