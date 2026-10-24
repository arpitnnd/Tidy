package com.tidy.app.ui.components

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.style.CommonStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.tidy.designsystem.TidyStyleTheme
import com.tidy.designsystem.background
import com.tidy.designsystem.size
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class StylesAdaptiveTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun callerStyleOverridesCardDefaults() {
        compose.setContent {
            MaterialTheme {
                TidyStyleTheme {
                    SettingCard(
                        modifier = Modifier.testTag("card"),
                        style = CommonStyle { size(88.dp); background(Color.Red) }
                    ) {}
                }
            }
        }
        compose.onNodeWithTag("card")
            .assertWidthIsEqualTo(88.dp)
            .assertHeightIsEqualTo(88.dp)
        val pixels = compose.onNodeWithTag("card").captureToImage().toPixelMap()
        org.junit.Assert.assertEquals(Color.Red, pixels[pixels.width / 2, pixels.height / 2])
    }

    @Test
    fun cardsStayOpaqueAcrossLivePaletteChanges() {
        val dark = mutableStateOf(false)
        val lightPalette = lightColorScheme(surface = Color(0xFFF1F5F9))
        val darkPalette = darkColorScheme(surface = Color(0xFF1B1C1E))
        compose.setContent {
            MaterialTheme(colorScheme = if (dark.value) darkPalette else lightPalette) {
                TidyStyleTheme {
                    Box(Modifier.background(Color.Magenta)) {
                        SettingCard(
                            modifier = Modifier.testTag("card"),
                            style = CommonStyle { size(88.dp) }
                        ) {}
                    }
                }
            }
        }
        val lightPixels = compose.onNodeWithTag("card").captureToImage().toPixelMap()
        assertEquals(
            lightPalette.surface,
            lightPixels[lightPixels.width / 2, lightPixels.height / 2]
        )
        compose.runOnIdle { dark.value = true }
        val darkPixels = compose.onNodeWithTag("card").captureToImage().toPixelMap()
        assertEquals(
            darkPalette.surface,
            darkPixels[darkPixels.width / 2, darkPixels.height / 2]
        )
    }

    @Test
    fun largeTextStacksTierCardsInReadingOrder() {
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, 2f)) {
                MaterialTheme {
                    TidyStyleTheme {
                        TierSelectorRow(
                            cards = listOf(
                                TierCard(0, Icons.Default.Check, "First", "Description"),
                                TierCard(1, Icons.Default.Check, "Second", "Description"),
                                TierCard(
                                    2,
                                    Icons.Default.Check,
                                    "Third",
                                    "Description",
                                    locked = true
                                )
                            ),
                            selected = 0,
                            onSelect = {},
                            onLockedTap = {},
                            modifier = Modifier.width(320.dp)
                        )
                    }
                }
            }
        }
        val first = compose.onNodeWithText("First").fetchSemanticsNode().boundsInRoot
        val second = compose.onNodeWithText("Second").fetchSemanticsNode().boundsInRoot
        val third = compose.onNodeWithText("Third").fetchSemanticsNode().boundsInRoot
        assertTrue(second.top >= first.bottom)
        assertTrue(third.top >= second.bottom)
    }
}
