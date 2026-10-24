package com.tidy.app.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.tidy.app.data.HistoryEntry
import com.tidy.app.theme.TidyTheme
import com.tidy.app.ui.components.TidyTopAppBar
import com.tidy.app.ui.history.HistoryScreenContent
import com.tidy.app.ui.history.HistoryScreenViewModel
import com.tidy.app.ui.main.MainScreenActions
import com.tidy.app.ui.main.MainScreenContent
import com.tidy.app.ui.main.MainScreenPreferences
import com.tidy.app.ui.main.MainScreenViewModel
import com.tidy.app.ui.settings.SettingsScreenContent
import com.tidy.app.ui.settings.SettingsScreenViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ScreenContentTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun explicitExpansionNetworkFailureShowsASnackbar() {
        val failures =
            kotlinx.coroutines.flow.MutableSharedFlow<MainScreenViewModel.ExpansionFailure>(
                extraBufferCapacity = 1
            )
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                MainScreenContent(
                    state = MainScreenViewModel.UiState(
                        isInitialLoading = false,
                        isCleaned = true,
                        originalUrl = "https://bit.ly/example",
                        cleanedUrl = "https://bit.ly/example",
                        canExpand = true
                    ), onSettingsClick = {}, onHistoryClick = {},
                    actions = object : MainScreenActions {
                        override val expansionFailures = failures
                        override fun expandShortUrl() {
                            failures.tryEmit(MainScreenViewModel.ExpansionFailure.Network)
                        }
                    })
            }
        }
        compose.onNodeWithContentDescription("Expand URL").performClick()
        compose.onNodeWithText("Could not expand URL. Check your internet connection and try again.")
            .assertIsDisplayed()
    }

    @Test
    fun homeTitleAnimatesIntoTheAppBarAfterCleaningAndBackAfterClearing() {
        val state = mutableStateOf(MainScreenViewModel.UiState(isInitialLoading = false))
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                MainScreenContent(state = state.value, onSettingsClick = {}, onHistoryClick = {})
            }
        }
        val title = compose.onNodeWithText("Tidy")
        val expanded = title.fetchSemanticsNode().boundsInRoot
        compose.mainClock.autoAdvance = false
        compose.runOnIdle {
            state.value = state.value.copy(
                isCleaned = true,
                originalUrl = "https://example.com/?utm_source=share",
                cleanedUrl = "https://example.com/",
                removedParams = listOf("utm_source")
            )
        }
        compose.mainClock.advanceTimeBy(160)
        val travelling = title.fetchSemanticsNode().boundsInRoot
        compose.mainClock.advanceTimeBy(400)
        val collapsed = title.fetchSemanticsNode().boundsInRoot
        assertTrue(travelling.top < expanded.top)
        assertTrue(travelling.top > collapsed.top)
        assertTrue(collapsed.left < expanded.left)

        compose.runOnIdle {
            state.value = MainScreenViewModel.UiState(isInitialLoading = false)
        }
        compose.mainClock.advanceTimeBy(500)
        val restored = title.fetchSemanticsNode().boundsInRoot
        assertEquals(expanded.top, restored.top, 1f)
        assertEquals(expanded.left, restored.left, 1f)
    }

    @Test
    fun appBarCoversUnderlyingContent() {
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                Box(Modifier.fillMaxSize().background(Color.Magenta)) {
                    TidyTopAppBar(title = "History", modifier = Modifier.testTag("app-bar"))
                }
            }
        }
        val pixels = compose.onNodeWithTag("app-bar").captureToImage().toPixelMap()
        assertNotEquals(Color.Magenta, pixels[pixels.width - 1, pixels.height / 2])
    }

    @Test
    fun cleanedResultRoutesCopyAndShareToTheHost() {
        val actions = mutableListOf<String>()
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                MainScreenContent(
                    state = MainScreenViewModel.UiState(
                        isInitialLoading = false, isCleaned = true,
                        originalUrl = "https://example.com/?utm_source=share",
                        cleanedUrl = "https://example.com/", removedParams = listOf("utm_source")
                    ),
                    onSettingsClick = {}, onHistoryClick = {},
                    actions = object : MainScreenActions {
                        override fun copyToClipboard(context: Context) {
                            actions += "copy"
                        }

                        override fun shareUrl(context: Context) {
                            actions += "share"
                        }
                    }
                )
            }
        }
        compose.onNodeWithText("Copy").performClick()
        compose.onNodeWithText("Share").performClick()
        compose.runOnIdle { assertEquals(listOf("copy", "share"), actions) }
    }

    @Test
    fun removedUtmExplanationIsAvailableFromHome() {
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                MainScreenContent(
                    state = MainScreenViewModel.UiState(
                        isInitialLoading = false, isCleaned = true,
                        originalUrl = "https://example.com/?UTM_SOURCE=share",
                        cleanedUrl = "https://example.com/", removedParams = listOf("UTM_SOURCE")
                    ),
                    onSettingsClick = {}, onHistoryClick = {},
                    preferences = MainScreenPreferences(
                        trackerDescriptions = mapOf("utm_*" to "Campaign explanation")
                    )
                )
            }
        }
        compose.onNodeWithText("Show details").performScrollTo().performClick()
        compose.onNodeWithText("UTM_SOURCE").performScrollTo().performClick()
        compose.onNodeWithText("Campaign explanation").assertIsDisplayed()
    }

    @Test
    fun removedUtmExplanationIsAvailableFromHistory() {
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                HistoryScreenContent(
                    state = HistoryScreenViewModel.UiState(
                        isInitialLoading = false,
                        trackerDescriptions = mapOf("utm_*" to "Campaign explanation"),
                        history = listOf(
                            HistoryEntry(
                                id = "entry", timestamp = 1791244800000L,
                                originalUrl = "https://example.com/?UTM_SOURCE=share",
                                cleanedUrl = "https://example.com/", removedParamsCount = 1,
                                domain = "example.com"
                            )
                        )
                    ), onBackClick = {}
                )
            }
        }
        compose.onNodeWithText("example.com").performScrollTo().performClick()
        compose.onNodeWithText("UTM_SOURCE").performScrollTo().performClick()
        compose.onNodeWithText("Campaign explanation").assertIsDisplayed()
    }

    @Test
    fun historyActionsStayReadableAtDoubleTextSize() {
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 2f)) {
                TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                    Box(Modifier.width(320.dp)) {
                        HistoryScreenContent(
                            state = HistoryScreenViewModel.UiState(
                                isInitialLoading = false,
                                history = listOf(
                                    HistoryEntry(
                                        id = "entry", timestamp = 1791244800000L,
                                        originalUrl = "https://example.com/?utm_source=share",
                                        cleanedUrl = "https://example.com/", removedParamsCount = 1,
                                        domain = "example.com"
                                    )
                                )
                            ), onBackClick = {}
                        )
                    }
                }
            }
        }
        compose.onNodeWithText("example.com").performScrollTo().performClick()
        listOf("Copy original", "Copy", "Open").forEach { label ->
            val layouts = mutableListOf<TextLayoutResult>()
            compose.onNodeWithText(label, useUnmergedTree = true)
                .performScrollTo()
                .assertIsDisplayed()
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
            assertEquals("$label must remain on one line", 1, layouts.single().lineCount)
            assertTrue("$label must fit its action", !layouts.single().hasVisualOverflow)
        }
    }

    @Test
    fun historyEntryStillExpandsInline() {
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                HistoryScreenContent(
                    state = HistoryScreenViewModel.UiState(
                        isInitialLoading = false,
                        history = listOf(
                            HistoryEntry(
                                id = "entry", timestamp = 1791244800000L,
                                originalUrl = "https://example.com/?utm_source=share",
                                cleanedUrl = "https://example.com/", removedParamsCount = 1,
                                domain = "example.com"
                            )
                        )
                    ), onBackClick = {}
                )
            }
        }
        compose.onNodeWithText("example.com").performScrollTo().performClick()
        compose.onNodeWithText("utm_source").assertIsDisplayed()
    }

    @Test
    fun settingsRulesExpandWithExplicitState() {
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                SettingsScreenContent(
                    state = SettingsScreenViewModel.UiState(whitelistedDomains = setOf("example.com")),
                    onBackClick = {}, onAboutClick = {}
                )
            }
        }
        compose.onNodeWithText("Ignored domains").performClick()
        compose.onNodeWithText("example.com").assertIsDisplayed()
    }
}
