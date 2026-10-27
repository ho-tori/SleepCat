package com.sleepcat.app.core.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.sleepcat.app.data.preferences.*
import com.sleepcat.app.feature.home.HomeScreen
import com.sleepcat.app.feature.review.ReviewScreen
import com.sleepcat.app.feature.settings.*
import com.sleepcat.app.feature.tasks.TaskListScreen

private object Routes {
    const val HOME = "home"
    const val TASKS = "tasks"
    const val REVIEW = "review"
    const val SETTINGS = "settings"
}
private data class Tab(val route: String, val label: String, val icon: ImageVector)
private val tabs = listOf(
    Tab(Routes.HOME, "首页", Icons.Default.Home),
    Tab(Routes.REVIEW, "回顾", Icons.Default.DateRange),
    Tab(Routes.SETTINGS, "我的", Icons.Default.Person),
)

@Composable
fun SleepCatNavHost(state: SettingsUiState, onPalette: (ThemePalette) -> Unit, onMode: (ThemeMode) -> Unit, onRetry: () -> Unit) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route ?: Routes.HOME
    SleepCatScaffold(
        secondaryPage = route == Routes.TASKS,
        onBack = { nav.popBackStack() },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = route == tab.route,
                        onClick = {
                            nav.navigate(tab.route) {
                                popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },
                        modifier = Modifier.testTag("tab-${tab.route}"),
                    )
                }
            }
        },
    ) { modifier ->
        NavHost(navController = nav, startDestination = Routes.HOME, modifier = modifier) {
            composable(Routes.HOME) { HomeScreen { nav.navigate(Routes.TASKS) { launchSingleTop = true } } }
            composable(Routes.TASKS) { TaskListScreen() }
            composable(Routes.REVIEW) { ReviewScreen() }
            composable(Routes.SETTINGS) { SettingsScreen(state, onPalette, onMode, onRetry) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SleepCatScaffold(secondaryPage: Boolean, onBack: () -> Unit, bottomBar: @Composable () -> Unit, content: @Composable (Modifier) -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text("SleepCat", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                navigationIcon = {
                    if (secondaryPage) IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, contentDescription = "返回首页")
                    }
                },
            )
        },
        bottomBar = { if (!secondaryPage) bottomBar() },
    ) { padding ->
        content(Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding))
    }
}
