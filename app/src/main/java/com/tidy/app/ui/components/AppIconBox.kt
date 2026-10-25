@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package com.tidy.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.style.CommonStyle
import com.tidy.designsystem.TidyComponentStyles
import com.tidy.designsystem.TidyStyle
import com.tidy.designsystem.rememberTidyStyle
import com.tidy.designsystem.tidyStyleSurface

/**
 * A rounded-rectangle box containing a centred icon. Used as the leading visual
 * in sheet headers, feature rows, stats cards, and app logo placements.
 *
 * @param icon      The vector icon to render.
 * @param iconSize  The icon dimension inside the box (default 24.dp).
 * @param tint      Icon tint colour (defaults to primary).
 * @param modifier  Optional modifier for the outer Box.
 */
@Composable
fun AppIconBox(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    style: TidyStyle = CommonStyle
) {
    val resolver = rememberTidyStyle(TidyComponentStyles.iconBox, style)
    Box(
        modifier = modifier.tidyStyleSurface(resolver),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}
