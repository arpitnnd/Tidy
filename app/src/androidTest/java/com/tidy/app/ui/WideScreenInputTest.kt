package com.tidy.app.ui

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipe
import com.tidy.app.data.HistoryEntry
import com.tidy.app.theme.TidyTheme
import com.tidy.app.ui.about.AboutScreen
import com.tidy.app.ui.history.HistoryScreenContent
import com.tidy.app.ui.history.HistoryScreenViewModel
import com.tidy.app.ui.main.MainScreenActions
import com.tidy.app.ui.main.MainScreenContent
import com.tidy.app.ui.main.MainScreenViewModel
import com.tidy.app.ui.settings.SettingsScreenActions
import com.tidy.app.ui.settings.SettingsScreenContent
import com.tidy.app.ui.settings.SettingsScreenViewModel
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class WideScreenInputTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun useLandscape() {
        compose.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        compose.waitUntil(5_000) {
            compose.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        }
    }

    @Test
    fun invalidUrlIsQuietUntilSubmissionAndClearsWhenEdited() {
        val state = mutableStateOf(MainScreenViewModel.UiState(isInitialLoading = false))
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                MainScreenContent(
                    state = state.value, onSettingsClick = {}, onHistoryClick = {},
                    actions = object : MainScreenActions {
                        override fun onUrlInput(url: String) {
                            state.value = state.value.copy(inputUrl = url)
                        }
                    }
                )
            }
        }
        val field = compose.onNode(hasSetTextAction())
        field.performTextReplacement("not a URL")
        val action = compose.onNodeWithContentDescription("Clean URL")
        compose.onNodeWithText("Enter a URL, such as example.com").assertDoesNotExist()
        action.assertIsEnabled()
        val actionOffset =
            action.fetchSemanticsNode().boundsInRoot.top - field.fetchSemanticsNode().boundsInRoot.top
        action.performClick()
        compose.onNodeWithText("Enter a URL, such as example.com").assertIsDisplayed()
        org.junit.Assert.assertEquals(
            actionOffset,
            action.fetchSemanticsNode().boundsInRoot.top - field.fetchSemanticsNode().boundsInRoot.top,
            1f
        )
        field.performTextReplacement("example.com")
        compose.onNodeWithText("Enter a URL, such as example.com").assertDoesNotExist()
        compose.onNodeWithContentDescription("Clean URL").assertIsEnabled()
        field.performTextReplacement("")
        compose.onNodeWithText("Enter a URL, such as example.com").assertDoesNotExist()
    }

    @Test
    fun invalidDomainIsQuietUntilSubmissionAndClearsWhenEdited() {
        val state = mutableStateOf(SettingsScreenViewModel.UiState())
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                SettingsScreenContent(
                    state = state.value, onBackClick = {}, onAboutClick = {},
                    actions = object : SettingsScreenActions {
                        override fun onDomainInputChanged(input: String) {
                            state.value = state.value.copy(domainInput = input)
                        }
                    }
                )
            }
        }
        compose.onNodeWithText("Domains to skip entirely").performScrollTo().performClick()
        val field = compose.onNode(hasSetTextAction())
        field.performScrollTo().performTextReplacement("not a domain")
        compose.onNodeWithText("Enter a domain, such as example.com", useUnmergedTree = true)
            .assertDoesNotExist()
        val action = compose.onNodeWithContentDescription("Add domain")
        action.assertIsEnabled().performClick()
        val feedback =
            compose.onNodeWithText("Enter a domain, such as example.com", useUnmergedTree = true)
        feedback.performScrollTo().assertIsDisplayed()
        compose.onNodeWithContentDescription("Add domain").assertIsEnabled()
        field.performTextReplacement("example.com")
        compose.onNodeWithText("Enter a domain, such as example.com", useUnmergedTree = true)
            .assertDoesNotExist()
        compose.onNodeWithContentDescription("Add domain").assertIsEnabled()
    }

    @Test
    fun homeScrollsFromTheSideMargin() {
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                MainScreenContent(
                    state = MainScreenViewModel.UiState(isInitialLoading = false),
                    onSettingsClick = {}, onHistoryClick = {}, modifier = Modifier.testTag("screen")
                )
            }
        }
        assertSideSwipeScrolls()
    }

    @Test
    fun settingsScrollsFromTheSideMargin() {
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                SettingsScreenContent(
                    state = SettingsScreenViewModel.UiState(), onBackClick = {}, onAboutClick = {},
                    modifier = Modifier.testTag("screen")
                )
            }
        }
        assertSideSwipeScrolls()
    }

    @Test
    fun aboutScrollsFromTheSideMargin() {
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                AboutScreen(onBackClick = {}, modifier = Modifier.testTag("screen"))
            }
        }
        assertSideSwipeScrolls()
    }

    @Test
    fun historyScrollsFromTheSideMargin() {
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                HistoryScreenContent(
                    state = HistoryScreenViewModel.UiState(
                        isInitialLoading = false,
                        history = List(20) { index ->
                            HistoryEntry(
                                id = "$index",
                                originalUrl = "https://example.com/$index?utm_source=test",
                                cleanedUrl = "https://example.com/$index",
                                removedParamsCount = 1,
                                domain = "example.com"
                            )
                        }
                    ), onBackClick = {}, modifier = Modifier.testTag("screen")
                )
            }
        }
        assertSideSwipeScrolls()
    }

    @Test
    fun historyExpansionSurvivesScrollingOutOfComposition() {
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                HistoryScreenContent(
                    state = HistoryScreenViewModel.UiState(
                        isInitialLoading = false,
                        history = List(30) { index ->
                            HistoryEntry(
                                id = "$index",
                                originalUrl = "https://example.com/$index?utm_source=test",
                                cleanedUrl = "https://example.com/$index", domain = "example.com",
                                removedParamsCount = 1
                            )
                        }), onBackClick = {}
                )
            }
        }
        val first = compose.onNodeWithText("https://example.com/0")
        first.performScrollTo().performClick()
        compose.onNodeWithText("Original URL").performScrollTo().assertIsDisplayed()
        compose.onNode(hasScrollAction()).performScrollToIndex(29)
        first.assertDoesNotExist()
        compose.onNode(hasScrollAction()).performScrollToIndex(4)
        first.performScrollTo()
        compose.onNodeWithText("Original URL").performScrollTo().assertIsDisplayed()
    }

    private fun assertSideSwipeScrolls() {
        val scrollable = compose.onNode(hasScrollAction())
        val before = scrollable.fetchSemanticsNode()
            .config[SemanticsProperties.VerticalScrollAxisRange].value()
        val screen = compose.onNodeWithTag("screen")
        val screenBounds = screen.fetchSemanticsNode().boundsInRoot
        val scrollBounds = scrollable.fetchSemanticsNode().boundsInRoot
        val top = scrollBounds.top - screenBounds.top
        val cutoutInset = compose.activity.window.decorView.rootWindowInsets
            ?.displayCutout?.safeInsetLeft?.toFloat() ?: 0f
        val left = maxOf(scrollBounds.left - screenBounds.left, cutoutInset) + 8f
        screen.performTouchInput {
            swipe(
                start = Offset(left, top + scrollBounds.height * 0.8f),
                end = Offset(left, top + scrollBounds.height * 0.3f),
                durationMillis = 600
            )
        }
        val after = scrollable.fetchSemanticsNode()
            .config[SemanticsProperties.VerticalScrollAxisRange].value()
        assertTrue("A swipe in the side margin must scroll the content", after > before)
    }
}
