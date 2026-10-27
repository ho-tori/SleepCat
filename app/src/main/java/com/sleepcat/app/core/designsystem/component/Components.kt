package com.sleepcat.app.core.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sleepcat.app.core.designsystem.theme.SleepCatSpacing
import com.sleepcat.app.core.designsystem.theme.SleepCatTheme
import com.sleepcat.app.data.preferences.*

@Composable
fun SleepCatCard(modifier: Modifier = Modifier, emphasized: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (emphasized) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
            contentColor = if (emphasized) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
        ),
    ) {
        Column(Modifier.padding(SleepCatSpacing.page), verticalArrangement = Arrangement.spacedBy(SleepCatSpacing.medium), content = content)
    }
}

@Composable
fun SleepCatButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier.fillMaxWidth().heightIn(min = 52.dp), shape = MaterialTheme.shapes.small) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
fun SleepCatPage(content: @Composable ColumnScope.() -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = SleepCatSpacing.maxContentWidth).fillMaxWidth().verticalScroll(rememberScrollState()).padding(SleepCatSpacing.page),
            verticalArrangement = Arrangement.spacedBy(SleepCatSpacing.page), content = content,
        )
    }
}

@Composable
fun PageHeading(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(SleepCatSpacing.small)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
    }
}

@Preview(name = "Warm light", showBackground = true)
@Composable
private fun WarmCardPreview() { ComponentPreview(ThemePreference(ThemePalette.WARM, ThemeMode.LIGHT)) }

@Preview(name = "Mint dark", showBackground = true)
@Composable
private fun MintCardPreview() { ComponentPreview(ThemePreference(ThemePalette.MINT, ThemeMode.DARK)) }

@Composable
private fun ComponentPreview(preference: ThemePreference) {
    SleepCatTheme(preference) {
        Surface {
            SleepCatCard(Modifier.padding(16.dp)) {
                Text("给今天一个温柔的收尾", style = MaterialTheme.typography.titleLarge)
                SleepCatButton("开始准备", {})
            }
        }
    }
}
