package com.tidy.app.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.tidy.app.FlavorConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeColoursTest {
    @Test
    fun canvasCardsAndNestedSurfacesStayDistinctAcrossPalettes() {
        for (dark in listOf(false, true)) {
            for (source in palettes(dark)) {
                val scheme = source.withTidySurfaces(dark)
                assertEquals(source.primary, scheme.primary)
                if (!dark) {
                    assertEquals(TidyLightBackground, scheme.background)
                    assertEquals(TidyLightSurface, scheme.surface)
                }
                assertTrue(
                    "Canvas/card separation: $scheme",
                    contrast(scheme.background, scheme.surface) >= 1.1f
                )
                assertTrue(
                    "Card/nested separation: $scheme",
                    contrast(scheme.surface, scheme.surfaceVariant) >= 1.1f
                )
                val containers = listOf(
                    scheme.surfaceContainerLowest, scheme.surfaceContainerLow,
                    scheme.surfaceContainer, scheme.surfaceContainerHigh,
                    scheme.surfaceContainerHighest
                )
                assertEquals(containers.size, containers.distinct().size)
                (containers + scheme.background + scheme.surfaceVariant).forEach {
                    assertEquals(1f, it.alpha)
                    assertTrue(contrast(it, scheme.onSurface) >= 4.5f)
                }
            }
        }
    }

    @Test
    fun cleanBadgesAndRemovedChipsStayMutedAndReadableAcrossPalettes() {
        for (dark in listOf(false, true)) {
            val reference = palettes(dark).first().withTidySurfaces(dark)
            for (source in palettes(dark)) {
                val scheme = source.withTidySurfaces(dark)
                val success = scheme.cleanResultColours()
                val removal = scheme.removedParameterColours()
                assertEquals(reference.cleanResultColours(), success)
                assertEquals(reference.removedParameterColours(), removal)
                assertTrue(success.container.green > success.container.red)
                assertTrue(success.content.green > success.content.red)
                assertTrue(removal.container.red > removal.container.green)
                assertTrue(removal.content.red > removal.content.green)
                for (colours in listOf(success, removal)) {
                    assertEquals(1f, colours.container.alpha)
                    assertTrue(contrast(colours.container, colours.content) >= 4.5f)
                    // Low-chroma fills prevent accent themes from turning these into bright signals.
                    val channels = listOf(
                        colours.container.red,
                        colours.container.green,
                        colours.container.blue
                    )
                    assertTrue(channels.max() - channels.min() <= 0.12f)
                    if (dark) {
                        assertTrue(
                            "Dark semantic fills must stay dim",
                            colours.container.luminance() <= 0.06f
                        )
                        assertTrue(
                            "Dark semantic text must stay muted",
                            colours.content.luminance() <= 0.55f
                        )
                    } else {
                        assertTrue(contrast(colours.container, scheme.surface) >= 1.1f)
                    }
                }
            }
        }
    }

    private fun palettes(dark: Boolean): List<ColorScheme> = listOf(
        if (dark) darkColorScheme() else lightColorScheme(),
        // Wallpaper palettes may give background and surface exactly the same colour.
        if (dark) darkColorScheme(background = Color(0xFF141218), surface = Color(0xFF141218))
        else lightColorScheme(background = Color(0xFFFFF7FF), surface = Color(0xFFFFF7FF))
    ) + listOfNotNull(
        FlavorConfig.getPremiumColorScheme("forest", dark),
        FlavorConfig.getPremiumColorScheme("ocean", dark),
        FlavorConfig.getPremiumColorScheme("velvet", dark)
    )

    private fun contrast(first: Color, second: Color): Float {
        val a = first.luminance()
        val b = second.luminance()
        return (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f)
    }
}
