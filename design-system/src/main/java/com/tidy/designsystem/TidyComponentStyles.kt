@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package com.tidy.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.style.CommonStyle
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

/** The current Material palette is supplied by each host's theme. */
val LocalTidyColours = compositionLocalOf<ColorScheme> {
    error("Tidy styles require a Material palette")
}

/** Stable component definitions read their palette when resolved, including live theme changes. */
object TidyComponentStyles {
    val statisticsCard = CommonStyle {
        shape(RoundedCornerShape(20.dp))
        background(LocalTidyColours.currentValue.surfaceVariant.copy(alpha = 0.35f))
        contentPadding(16.dp)
    }

    val historyCard = CommonStyle {
        fillWidth()
        shape(RoundedCornerShape(16.dp))
        background(LocalTidyColours.currentValue.surfaceVariant.copy(alpha = 0.25f))
        contentPadding(14.dp)
        if (state.isFocused) border(2.dp, LocalTidyColours.currentValue.primary)
    }

    val clipboardAction = CommonStyle {
        shape(RoundedCornerShape(20.dp))
        val colours = LocalTidyColours.currentValue
        background(colours.primaryContainer.copy(alpha = 0.6f))
        border(1.dp, colours.primary.copy(alpha = 0.25f))
        contentPadding(12.dp)
    }

    val clipboardCallout = CommonStyle {
        shape(RoundedCornerShape(20.dp))
        val colours = LocalTidyColours.currentValue
        background(colours.surfaceVariant.copy(alpha = 0.6f))
        border(1.dp, colours.primary.copy(alpha = 0.25f))
        contentPadding(12.dp)
    }

    val clipboardSuccess = CommonStyle {
        shape(RoundedCornerShape(20.dp))
        val colours = LocalTidyColours.currentValue
        background(colours.tertiaryContainer.copy(alpha = 0.6f))
        border(1.dp, colours.tertiary.copy(alpha = 0.25f))
        contentPadding(12.dp)
    }

    val settingsCard = CommonStyle {
        fillWidth()
        shape(RoundedCornerShape(TidyCardTokens.cornerRadius))
        background(LocalTidyColours.currentValue.surfaceVariant.copy(alpha = 0.4f))
    }

    val settingsRow = CommonStyle {
        fillWidth()
        minHeight(48.dp)
        shape(RoundedCornerShape(TidyCardTokens.rowCornerRadius))
        contentPadding(
            horizontal = TidyCardTokens.rowHorizontalPadding,
            vertical = TidyCardTokens.rowVerticalPadding
        )
        if (state.isFocused) {
            border(2.dp, LocalTidyColours.currentValue.primary)
        }
    }

    val iconBox = CommonStyle {
        size(48.dp)
        shape(RoundedCornerShape(12.dp))
        background(LocalTidyColours.currentValue.primaryContainer.copy(alpha = 0.5f))
    }

    val tierCard = CommonStyle {
        fillWidth()
        shape(RoundedCornerShape(16.dp))
        contentPadding(horizontal = 8.dp, vertical = 14.dp)
        val colours = LocalTidyColours.currentValue
        background(
            if (state.isSelected) colours.primaryContainer.copy(alpha = 0.35f)
            else colours.surfaceVariant.copy(alpha = 0.5f)
        )
        border(
            if (state.isSelected || state.isFocused) 2.dp else 1.dp,
            if (state.isSelected || state.isFocused) colours.primary
            else colours.outline.copy(alpha = 0.12f)
        )
    }

    val featureRow = CommonStyle { fillWidth() }

    val bulkCard = CommonStyle {
        fillWidth()
        shape(RoundedCornerShape(20.dp))
        background(LocalTidyColours.currentValue.primaryContainer.copy(alpha = 0.5f))
        contentPadding(16.dp)
        if (state.isFocused) border(2.dp, LocalTidyColours.currentValue.primary)
    }
}
