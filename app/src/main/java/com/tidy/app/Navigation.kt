package com.tidy.app

import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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

    // Strictly increasing for the composition's lifetime (rememberSaveable survives
    // rotation and process-death restore alongside backStack itself), so it can never
    // repeat a value already used by an entry still on the back stack (see Main's
    // shareSequence KDoc).
    var nextShareSequence by rememberSaveable { mutableIntStateOf(1) }

    LaunchedEffect(sharedUrls) {
        sharedUrls.collect { url ->
            backStack.add(Main(url, nextShareSequence))
            nextShareSequence++
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
