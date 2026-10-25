package com.tidy.app.ui

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.test.platform.app.InstrumentationRegistry
import androidx.activity.ComponentActivity
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.tidy.app.theme.TidyTheme
import com.tidy.app.ui.history.HistoryScreenContent
import com.tidy.app.ui.history.HistoryScreenViewModel
import com.tidy.app.ui.main.MainScreenContent
import com.tidy.app.ui.main.MainScreenViewModel
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Run on a landscape device with a physical or simulated display cutout. */
class CutoutInsetsTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun useLandscapeCutout() {
        compose.activity.requestedOrientation =
            if (InstrumentationRegistry.getArguments().getString("reverseLandscape") == "true") {
                ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            } else {
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        compose.waitUntil(5_000) {
            compose.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        }
        compose.waitForIdle()
        val cutout = compose.activity.window.decorView.rootWindowInsets?.displayCutout
        assumeTrue(
            "A landscape cutout is required", cutout != null &&
                    (cutout.safeInsetLeft > 0 || cutout.safeInsetRight > 0)
        )
    }

    @Test
    fun homeInputAvoidsTheCutout() {
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                MainScreenContent(
                    state = MainScreenViewModel.UiState(isInitialLoading = false),
                    onSettingsClick = {}, onHistoryClick = {}
                )
            }
        }
        assertOutsideCutout(compose.onNode(hasSetTextAction()).fetchSemanticsNode().boundsInRoot)
        assertOutsideCutout(compose.onNodeWithText("Tidy").fetchSemanticsNode().boundsInRoot)
    }

    @Test
    fun historyContentAvoidsTheCutout() {
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                HistoryScreenContent(
                    state = HistoryScreenViewModel.UiState(isInitialLoading = false),
                    onBackClick = {}
                )
            }
        }
        assertOutsideCutout(
            compose.onNodeWithText("Privacy dashboard").fetchSemanticsNode().boundsInRoot
        )
    }

    private fun assertOutsideCutout(bounds: Rect) {
        val decor = compose.activity.window.decorView
        val cutout = requireNotNull(decor.rootWindowInsets.displayCutout)
        assertTrue("Content overlaps the left cutout", bounds.left >= cutout.safeInsetLeft)
        assertTrue(
            "Content overlaps the right cutout",
            bounds.right <= decor.width - cutout.safeInsetRight
        )
    }
}
