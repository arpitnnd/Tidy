package com.tidy.app.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.platform.app.InstrumentationRegistry
import com.tidy.app.TidyApp
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.tidy.app.ui.main.MainScreen]. */
class MainScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setup() {
        // Clear the system clipboard so the clipboard-suggestion sheet can't pop over the
        // welcome hero, and mark first-launch done so the intro bottom sheet can't either:
        // this test must exercise the welcome hero regardless of ambient device/app state.
        val targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        val clipboard =
            targetContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("", ""))
        runBlocking { TidyApp.instance.settingsRepository.setFirstLaunchDone() }

        composeTestRule.setContent {
            MainScreen(
                sharedUrl = null,
                crashReportText = null,
                onSettingsClick = {},
                onHistoryClick = {}
            )
        }
    }

    @Test
    fun welcomeTitle_exists() {
        composeTestRule.onNodeWithText("Tidy up your URLs").assertExists()
    }
}
