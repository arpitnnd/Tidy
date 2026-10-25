@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package com.tidy.designsystem

import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.style.Breadth
import androidx.compose.foundation.style.CommonStyle
import androidx.compose.foundation.style.CommonStyleScope
import androidx.compose.foundation.style.Fill
import androidx.compose.foundation.style.StyleResolver
import androidx.compose.foundation.style.appearance
import androidx.compose.foundation.style.placement
import androidx.compose.foundation.style.rememberUpdatedStyleState
import androidx.compose.foundation.style.stylePropertyOf
import androidx.compose.foundation.style.styleResolver
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/** Styles shared by the app's custom components, resolved during layout and drawing. */
typealias TidyStyle = CommonStyle

private object Properties {
    val background = stylePropertyOf("Tidy.background") { Color.Transparent }
    val shape = stylePropertyOf<Shape>("Tidy.shape") { RectangleShape }
    val borderColour = stylePropertyOf("Tidy.borderColour") { Color.Transparent }
    val borderWidth = stylePropertyOf("Tidy.borderWidth") { Dp.Unspecified }
    val width = stylePropertyOf<Breadth>("Tidy.width") { Breadth.None }
    val height = stylePropertyOf<Breadth>("Tidy.height") { Breadth.None }
    val minHeight = stylePropertyOf("Tidy.minHeight") { Dp.Unspecified }
    val padding = stylePropertyOf<PaddingValues>("Tidy.contentPadding") { PaddingValues(0.dp) }
}

fun CommonStyleScope.background(colour: Color) = Properties.background.provide(colour)
fun CommonStyleScope.shape(value: Shape) = Properties.shape.provide(value)
fun CommonStyleScope.border(width: Dp, colour: Color) {
    Properties.borderWidth.provide(width)
    Properties.borderColour.provide(colour)
}

fun CommonStyleScope.size(value: Dp) {
    Properties.width.provide(Breadth(value))
    Properties.height.provide(Breadth(value))
}

fun CommonStyleScope.fillWidth() = Properties.width.provide(Breadth(1f))
fun CommonStyleScope.minHeight(value: Dp) = Properties.minHeight.provide(value)
fun CommonStyleScope.contentPadding(value: PaddingValues) = Properties.padding.provide(value)
fun CommonStyleScope.contentPadding(all: Dp) = contentPadding(PaddingValues(all))
fun CommonStyleScope.contentPadding(horizontal: Dp, vertical: Dp) =
    contentPadding(PaddingValues(horizontal = horizontal, vertical = vertical))

/**
 * Applies component defaults first and caller overrides last. Interaction modifiers belong
 * between [tidyStyleSurface] and [tidyStyleContent], so padding participates in the hit target.
 */
@Composable
fun rememberTidyStyle(
    defaults: TidyStyle,
    style: TidyStyle = CommonStyle,
    interactionSource: InteractionSource? = null,
    enabled: Boolean = true,
    selected: Boolean = false
): StyleResolver {
    val state = rememberUpdatedStyleState(interactionSource) {
        it.isEnabled = enabled
        it.isSelected = selected
    }
    return remember(defaults, style, state) { StyleResolver(CommonStyle(defaults, style), state) }
}

/** Binds the style once and clips both content and interaction feedback to its shape. */
fun Modifier.tidyStyleSurface(resolver: StyleResolver): Modifier =
    styleResolver(resolver)
        .placement {
            resolver.resolve {
                width = Properties.width.value
                height = Properties.height.value
                minHeight = Properties.minHeight.value
            }
        }
        .graphicsLayer {
            shape = resolver.resolve { Properties.shape.value }
            clip = true
        }
        .appearance {
            resolver.resolve {
                background = Fill(Properties.background.value)
                shape = Properties.shape.value
                border = Fill(Properties.borderColour.value)
                borderWidth = Properties.borderWidth.value
            }
        }

/** Reads padding during measurement, allowing theme and state changes without rebuilding styles. */
fun Modifier.tidyStyleContent(resolver: StyleResolver): Modifier = padding(
    object : PaddingValues {
        override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
            resolver.resolve { Properties.padding.value.calculateLeftPadding(layoutDirection) }

        override fun calculateTopPadding(): Dp =
            resolver.resolve { Properties.padding.value.calculateTopPadding() }

        override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
            resolver.resolve { Properties.padding.value.calculateRightPadding(layoutDirection) }

        override fun calculateBottomPadding(): Dp =
            resolver.resolve { Properties.padding.value.calculateBottomPadding() }
    }
)
