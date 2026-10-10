package com.tidy.app.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

data class CleanResultColours(val container: Color, val content: Color)

/** A restrained green success pair, lightly tinted by the current theme. */
@Composable
fun cleanResultColours(): CleanResultColours {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.surface.luminance() < 0.5f
    return CleanResultColours(
        container = lerp(
            if (dark) Color(0xFF163B25) else Color(0xFFDDEBDF),
            scheme.primaryContainer,
            0.15f
        ),
        content = lerp(
            if (dark) Color(0xFFDDEBDF) else Color(0xFF163B25),
            scheme.onPrimaryContainer,
            0.15f
        )
    )
}
