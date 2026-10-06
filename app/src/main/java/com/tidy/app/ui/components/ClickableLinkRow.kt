@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package com.tidy.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.style.CommonStyle
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.ui.semantics.Role
import com.tidy.designsystem.TidyComponentStyles
import com.tidy.designsystem.TidyStyle
import com.tidy.designsystem.rememberTidyStyle
import com.tidy.designsystem.tidyStyleContent
import com.tidy.designsystem.tidyStyleSurface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A full-width clickable row that displays a text label on the left and a trailing
 * icon on the right. Used for external links inside cards on the About screen.
 *
 * @param label        The link text to display.
 * @param onClick      Called when the row is tapped.
 * @param trailingIcon The icon shown on the right edge (defaults to OpenInNew).
 * @param modifier     Optional modifier for the Row.
 */
@Composable
fun ClickableLinkRow(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector = Icons.AutoMirrored.Filled.OpenInNew,
    style: TidyStyle = CommonStyle
) {
    val interactionSource = remember { MutableInteractionSource() }
    val resolver = rememberTidyStyle(TidyComponentStyles.settingsRow, style, interactionSource)
    Row(
        modifier = modifier
            .tidyStyleSurface(resolver)
            .clickable(interactionSource, ripple(), role = Role.Button, onClick = onClick)
            .tidyStyleContent(resolver),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = trailingIcon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.size(18.dp)
        )
    }
}
