package com.tidy.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalMediaQueryApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.LocalUiMediaScope
import androidx.compose.ui.UiMediaScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.tidy.app.data.HistoryEntry
import com.tidy.app.theme.TidyTheme
import com.tidy.app.ui.components.CleanStatusBadge
import com.tidy.app.ui.components.RemovedParameterChip
import com.tidy.app.ui.history.HistoryScreenContent
import com.tidy.app.ui.history.HistoryScreenViewModel
import com.tidy.app.ui.main.MainScreenContent
import com.tidy.app.ui.main.MainScreenViewModel
import com.tidy.app.ui.settings.SettingsScreenContent
import com.tidy.app.ui.settings.SettingsScreenViewModel

@Preview(
    name = "Dark",
    widthDp = 400,
    heightDp = 500,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Large text", widthDp = 400, heightDp = 500, fontScale = 1.5f)
@Preview(name = "Narrow large text", widthDp = 320, heightDp = 800, fontScale = 2f)
annotation class ScreenVariations

@PreviewTest
@WindowPreviews
@ScreenVariations
@Composable
fun HomeWelcome() {
    ScreenshotTheme {
        MainScreenContent(
            state = MainScreenViewModel.UiState(isInitialLoading = false),
            onSettingsClick = {}, onHistoryClick = {}
        )
    }
}

@PreviewTest
@WindowPreviews
@ScreenVariations
@Composable
fun HomeCleanedResult() {
    ScreenshotTheme {
        MainScreenContent(
            state = MainScreenViewModel.UiState(
                inputUrl = "https://example.com/article?utm_source=newsletter&ref=share",
                originalUrl = "https://example.com/article?utm_source=newsletter&ref=share",
                cleanedUrl = "https://example.com/article",
                removedParams = listOf("utm_source", "ref"),
                isCleaned = true, isInitialLoading = false,
                totalCleanedCount = 24, totalTrackersBlocked = 63
            ),
            onSettingsClick = {}, onHistoryClick = {}
        )
    }
}

@PreviewTest
@WindowPreviews
@ScreenVariations
@Composable
fun HistoryWithEntries() {
    ScreenshotTheme {
        HistoryScreenContent(
            state = historyState(), onBackClick = {}
        )
    }
}

@PreviewTest
@Preview(name = "Empty", widthDp = 400, heightDp = 500)
@Composable
fun HistoryEmpty() {
    ScreenshotTheme {
        HistoryScreenContent(
            state = HistoryScreenViewModel.UiState(isInitialLoading = false), onBackClick = {}
        )
    }
}

@PreviewTest
@WindowPreviews
@ScreenVariations
@Composable
fun SettingsDefault() {
    ScreenshotTheme {
        SettingsScreenContent(
            state = SettingsScreenViewModel.UiState(checkClipboardForLinks = false),
            onBackClick = {}, onAboutClick = {}
        )
    }
}

class ThemePreviewProvider : PreviewParameterProvider<String> {
    override val values = sequenceOf("forest", "ocean", "velvet", "dynamic")
}

class ResultThemePreviewProvider : PreviewParameterProvider<String> {
    override val values = sequenceOf("slate", "forest", "ocean", "velvet", "dynamic")
}

@Preview(name = "Light", widthDp = 400, heightDp = 500)
@Preview(
    name = "Dark",
    widthDp = 400,
    heightDp = 500,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
annotation class ThemePreviews

@PreviewTest
@ThemePreviews
@Composable
fun HomeThemes(@PreviewParameter(ThemePreviewProvider::class) theme: String) {
    ScreenshotTheme(theme) {
        MainScreenContent(
            state = MainScreenViewModel.UiState(isInitialLoading = false),
            onSettingsClick = {}, onHistoryClick = {}
        )
    }
}

@PreviewTest
@ThemePreviews
@Composable
fun HomeResultThemes(@PreviewParameter(ResultThemePreviewProvider::class) theme: String) {
    ScreenshotTheme(theme) {
        MainScreenContent(
            state = MainScreenViewModel.UiState(
                originalUrl = "https://example.com/article?fbclid=share",
                cleanedUrl = "https://example.com/article",
                removedParams = listOf("fbclid"),
                isCleaned = true, isInitialLoading = false
            ),
            onSettingsClick = {}, onHistoryClick = {}
        )
    }
}

@PreviewTest
@ThemePreviews
@Composable
fun HomeCleanThemes(@PreviewParameter(ResultThemePreviewProvider::class) theme: String) {
    ScreenshotTheme(theme) {
        MainScreenContent(
            state = MainScreenViewModel.UiState(
                originalUrl = "https://example.com/article",
                cleanedUrl = "https://example.com/article",
                isCleaned = true, isInitialLoading = false
            ),
            onSettingsClick = {}, onHistoryClick = {}
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@PreviewTest
@ThemePreviews
@Preview(name = "Large text light", widthDp = 400, heightDp = 1000, fontScale = 2f)
@Preview(
    name = "Large text dark", widthDp = 400, heightDp = 1000, fontScale = 2f,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun CleaningColoursThemes(@PreviewParameter(ResultThemePreviewProvider::class) theme: String) {
    ScreenshotTheme(theme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                val scheme = MaterialTheme.colorScheme
                for (parent in listOf(
                    scheme.surface,
                    scheme.primaryContainer,
                    scheme.tertiaryContainer
                )) {
                    Surface(color = parent, shape = MaterialTheme.shapes.large) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                CleanStatusBadge(0)
                                CleanStatusBadge(2)
                            }
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                RemovedParameterChip("utm_source", onClick = {})
                                RemovedParameterChip("fbclid", onClick = {})
                            }
                        }
                    }
                }
            }
        }
    }
}

@PreviewTest
@ThemePreviews
@Composable
fun HistoryThemes(@PreviewParameter(ThemePreviewProvider::class) theme: String) {
    ScreenshotTheme(theme) {
        HistoryScreenContent(state = historyState(), onBackClick = {})
    }
}

@PreviewTest
@Preview(name = "Light", widthDp = 400, heightDp = 800)
@Preview(
    name = "Dark", widthDp = 400, heightDp = 800,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Large text", widthDp = 400, heightDp = 1000, fontScale = 1.5f)
@Composable
fun HistoryStatusThemes(@PreviewParameter(ResultThemePreviewProvider::class) theme: String) {
    ScreenshotTheme(theme) {
        HistoryScreenContent(state = historyState(), onBackClick = {})
    }
}

@PreviewTest
@ThemePreviews
@Composable
fun SettingsThemes(@PreviewParameter(ThemePreviewProvider::class) theme: String) {
    ScreenshotTheme(theme) {
        SettingsScreenContent(
            state = SettingsScreenViewModel.UiState(checkClipboardForLinks = false),
            onBackClick = {}, onAboutClick = {}
        )
    }
}

private fun historyState() = HistoryScreenViewModel.UiState(
    history = listOf(
        historyEntry("article", "example.com", "/article"),
        historyEntry("guide", "developer.android.com", "/guide").copy(
            originalUrl = "https://developer.android.com/guide", removedParamsCount = 0
        ),
        historyEntry("project", "github.com", "/example/project"),
        historyEntry("long", "example.org", "/a-long-article-title-to-check-wrapping")
    ),
    totalCleanedCount = 24, totalTrackersBlocked = 63, isInitialLoading = false
)

private fun historyEntry(id: String, domain: String, path: String) = HistoryEntry(
    id = id, timestamp = 1791244800000L,
    originalUrl = "https://$domain$path?utm_source=newsletter&ref=share",
    cleanedUrl = "https://$domain$path", removedParamsCount = 2, domain = domain
)

@OptIn(ExperimentalMediaQueryApi::class)
@Composable
private fun ScreenshotTheme(theme: String = "slate", content: @Composable () -> Unit) {
    val configuration = LocalConfiguration.current
    val mediaScope = object : UiMediaScope {
        override val windowWidth = configuration.screenWidthDp.dp
        override val windowHeight = configuration.screenHeightDp.dp
        override val windowPosture = UiMediaScope.WindowPosture(emptyList())
        override val pointerPrecision = UiMediaScope.PointerPrecision.Coarse
        override val keyboardKind = UiMediaScope.KeyboardKind.Virtual
        override val hasMicrophone = false
        override val hasCamera = false
        override val viewingDistance = UiMediaScope.ViewingDistance.Near
    }
    CompositionLocalProvider(LocalUiMediaScope provides mediaScope) {
        TidyTheme(
            selectedTheme = theme,
            isPlusUnlocked = com.tidy.app.FlavorConfig.isPlayFlavor,
            content = content
        )
    }
}
