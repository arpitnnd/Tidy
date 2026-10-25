package com.tidy.app.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import com.tidy.designsystem.TidyStyleTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsAccessibilityTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun expandedStateAndActionUpdateTogether() {
        val expanded = mutableStateOf(false)
        compose.setContent {
            MaterialTheme {
                TidyStyleTheme {
                    ExpandableSettingRow("Rules", "Summary", expanded.value, {
                        expanded.value = !expanded.value
                    })
                }
            }
        }
        compose.onNodeWithText("Rules")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Collapsed"))
            .performClick()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, "Expanded"))
        compose.onAllNodes(hasClickAction()).assertCountEquals(1)
    }

    @Test
    fun selectedTierAndLockedActionRemainDistinct() {
        val selected = mutableStateOf(0)
        var lockedTaps = 0
        compose.setContent {
            MaterialTheme {
                TidyStyleTheme {
                    TierSelectorRow(
                        cards = listOf(
                            TierCard(0, Icons.Default.Check, "Free", "First"),
                            TierCard(1, Icons.Default.Check, "Other", "Second"),
                            TierCard(2, Icons.Default.Check, "Locked", "Third", locked = true)
                        ),
                        selected = selected.value,
                        onSelect = { selected.value = it },
                        onLockedTap = { lockedTaps++ }
                    )
                }
            }
        }
        compose.onNodeWithText("Free").assertIsSelected()
        val widths = listOf("Free", "Other", "Locked").map {
            compose.onNodeWithText(it).fetchSemanticsNode().boundsInRoot.width
        }
        org.junit.Assert.assertTrue(widths.max() - widths.min() <= 1f)
        compose.onNodeWithText("Other").performClick().assertIsSelected()
        compose.onNodeWithText("Free").assertIsNotSelected()
        compose.onNodeWithText("Locked")
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.RadioButton))
            .performClick().assertIsNotSelected()
        compose.runOnIdle {
            assertEquals(1, selected.value)
            assertEquals(1, lockedTaps)
        }
        compose.onAllNodes(hasClickAction()).assertCountEquals(3)
    }
}
