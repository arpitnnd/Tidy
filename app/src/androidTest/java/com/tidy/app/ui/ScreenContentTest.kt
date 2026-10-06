package com.tidy.app.ui

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.tidy.app.data.HistoryEntry
import com.tidy.app.theme.TidyTheme
import com.tidy.app.ui.components.TidyTopAppBar
import com.tidy.app.ui.history.HistoryScreenContent
import com.tidy.app.ui.history.HistoryScreenViewModel
import com.tidy.app.ui.main.MainScreenActions
import com.tidy.app.ui.main.MainScreenContent
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
        compose.onNodeWithText("Domains to skip entirely").performClick()
        compose.onNodeWithText("example.com").assertIsDisplayed()
    }
}
