package com.tidy.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A rounded-rectangle box containing a centered icon. Used as the leading visual
 * in sheet headers, feature rows, stats cards, and app logo placements.
 *
 * @param icon      The vector icon to render.
 * @param boxSize   The outer box dimension (default 48.dp).
 * @param iconSize  The icon dimension inside the box (default 24.dp).
 * @param cornerRadius Corner radius of the rounded box (default 12.dp).
 * @param tint      Icon tint colour (defaults to primaryContainer at 50% alpha).
 * @param background Background colour of the box (defaults to primary).
 * @param modifier  Optional modifier for the outer Box.
 */
@Composable
fun AppIconBox(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    boxSize: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    cornerRadius: Dp = 12.dp,
    tint: Color = MaterialTheme.colorScheme.primary,
    background: Color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
) {
    Box(
        modifier = modifier
            .size(boxSize)
            .clip(RoundedCornerShape(cornerRadius))
            .background(background),
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
