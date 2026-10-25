package com.tidy.app.ui.settings

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.tidy.app.theme.TidyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

class UpgradeBadgeLayoutTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun narrowLargeTextKeepsTheWholeBadgeLabel() {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(
                LocalDensity provides Density(
                    density.density,
                    fontScale = 2f
                )
            ) {
                TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                    Box(Modifier.width(288.dp)) {
                        SettingsMigrationViews.UpgradePromptRow(
                            onUpgradeClick = {},
                            showFollowup = false
                        )
                    }
                }
            }
        }
        val results = mutableListOf<TextLayoutResult>()
        compose.onNodeWithText("Coming soon")
            .performSemanticsAction(SemanticsActions.GetTextLayoutResult) {
                it(results)
            }
        assertEquals(1, results.size)
        assertEquals(1, results.single().lineCount)
        assertFalse("The badge label must fit without clipping", results.single().hasVisualOverflow)
    }
}
