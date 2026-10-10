package com.tidy.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.ExperimentalMediaQueryApi
import androidx.compose.ui.LocalUiMediaScope
import androidx.compose.ui.UiMediaScope
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.tidy.app.data.HistoryEntry
import com.tidy.app.theme.TidyTheme
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
fun HomeResultThemes(@PreviewParameter(ThemePreviewProvider::class) theme: String) {
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
fun HistoryThemes(@PreviewParameter(ThemePreviewProvider::class) theme: String) {
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
        historyEntry("guide", "developer.android.com", "/guide"),
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
        override val windowPosture = UiMediaScope.Posture.Flat
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
