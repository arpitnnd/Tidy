package com.tidy.app.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import com.tidy.designsystem.hasLightSurfaces

data class CleanResultColours(val container: Color, val content: Color)

/** Cleaning is always a green success, including results with parameters cleaned. */
fun ColorScheme.cleanResultColours(): CleanResultColours =
    if (hasLightSurfaces) {
        CleanResultColours(Color(0xFFDDE6DF), Color(0xFF36533E))
    } else {
        CleanResultColours(Color(0xFF24392B), Color(0xFFA9BCAF))
    }

/** Removed parameter chips use a separate muted red, with dim fills in dark mode. */
fun ColorScheme.removedParameterColours(): CleanResultColours =
    if (hasLightSurfaces) {
        CleanResultColours(Color(0xFFEBDDDD), Color(0xFF704A48))
    } else {
        CleanResultColours(Color(0xFF3E2C2D), Color(0xFFC4A6A4))
    }
