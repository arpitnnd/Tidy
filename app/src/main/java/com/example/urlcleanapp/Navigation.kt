package com.example.urlcleanapp

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.urlcleanapp.ui.history.HistoryScreen
import com.example.urlcleanapp.ui.main.MainScreen
import com.example.urlcleanapp.ui.settings.SettingsScreen
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun MainNavigation(
  initialUrl: String?,
  sharedUrls: SharedFlow<String>,
  crashReportText: String?
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
            modifier = Modifier.systemBarsPadding()
          )
        }
        entry<Settings> {
          SettingsScreen(
            onBackClick = { backStack.removeLastOrNull() },
            modifier = Modifier.systemBarsPadding()
          )
        }
        entry<History> {
          HistoryScreen(
            onBackClick = { backStack.removeLastOrNull() },
            modifier = Modifier.systemBarsPadding()
          )
        }
      },
  )
}
