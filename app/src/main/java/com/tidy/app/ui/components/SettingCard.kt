@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package com.tidy.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.style.CommonStyle
import com.tidy.designsystem.TidyComponentStyles
import com.tidy.designsystem.TidyStyle
import com.tidy.designsystem.TidyStyledCard

/**
 * The standard Tidy card style used on settings and about screens.
 *
 * Resolves its surface and dimensions through Styles, with caller overrides applied last.
 * Callers provide a [content] lambda for the card body.
 */
@Composable
fun SettingCard(
    modifier: Modifier = Modifier,
    style: TidyStyle = CommonStyle,
    content: @Composable () -> Unit
) {
    TidyStyledCard(modifier, CommonStyle(TidyComponentStyles.settingsCard, style)) {
        content()
    }
}
