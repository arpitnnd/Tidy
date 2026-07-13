package com.tidy.app

import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.tidy.app.ui.history.HistoryScreen
import com.tidy.app.ui.main.MainScreen
import com.tidy.app.ui.settings.SettingsScreen
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun MainNavigation(
    initialUrl: String?,
    sharedUrls: SharedFlow<String>,
    crashReportText: String?,
    showPlusUpsell: Boolean = false
) {
    val backStack = rememberNavBackStack(Main(initialUrl))

    LaunchedEffect(sharedUrls) {
        sharedUrls.collect { url ->
            backStack.add(Main(url))
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider =
            entryProvider {
                entry<Main> { key ->
                    MainScreen(
                        sharedUrl = key.sharedUrl,
                        crashReportText = crashReportText,
                        onSettingsClick = { backStack.add(Settings) },
                        onHistoryClick = { backStack.add(History) },
                        modifier = Modifier.systemBarsPadding(),
                        showPlusUpsell = showPlusUpsell
                    )
                }
                entry<Settings> {
                    SettingsScreen(
                        onBackClick = { backStack.removeLastOrNull() },
                        onAboutClick = { backStack.add(About) },
                        modifier = Modifier.systemBarsPadding()
                    )
                }
                entry<History> {
                    HistoryScreen(
                        onBackClick = { backStack.removeLastOrNull() },
                        modifier = Modifier.systemBarsPadding()
                    )
                }
                entry<About> {
                    com.tidy.app.ui.about.AboutScreen(
                        onBackClick = { backStack.removeLastOrNull() },
                        modifier = Modifier.systemBarsPadding()
                    )
                }
            },
    )
}
