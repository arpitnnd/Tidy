package com.tidy.app.ui.main

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.tidy.app.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun setup() {
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
    composeTestRule.onNodeWithText("Tidy Up Your URLs").assertExists()
  }
}
