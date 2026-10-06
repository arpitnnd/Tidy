@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package com.tidy.designsystem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.style.CommonStyle
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

/** A custom card surface whose geometry and appearance are supplied entirely through Styles. */
@Composable
fun TidyStyledCard(
    modifier: Modifier = Modifier,
    style: TidyStyle = CommonStyle,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val resolver = rememberTidyStyle(CommonStyle, style, interactionSource, enabled)
    Column(
        modifier.tidyStyleSurface(resolver)
            .then(
                if (onClick == null) Modifier else Modifier.clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick
                )
            )
            .tidyStyleContent(resolver),
        content = content
    )
}
