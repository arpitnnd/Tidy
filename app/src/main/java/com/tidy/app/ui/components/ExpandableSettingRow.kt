@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package com.tidy.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.style.CommonStyle
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import com.tidy.designsystem.TidyComponentStyles
import com.tidy.designsystem.TidyStyle
import com.tidy.designsystem.rememberTidyStyle
import com.tidy.designsystem.tidyStyleContent
import com.tidy.designsystem.tidyStyleSurface
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tidy.app.R
import com.tidy.app.theme.TidyCardTokens

/**
 * A full-width clickable row used as a collapsible section header in settings cards.
 * Shows a title, a summary subtitle, and an up/down chevron that reflects [expanded].
 *
 * @param title    The primary setting name.
 * @param summary  A one-line summary shown below the title.
 * @param expanded Whether the section is currently expanded.
 * @param onToggle Called when the user taps the row to toggle.
 * @param modifier Optional modifier for the Row.
 */
@Composable
fun ExpandableSettingRow(
    title: String,
    summary: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
    style: TidyStyle = CommonStyle
) {
    val interactionSource = remember { MutableInteractionSource() }
    val resolver = rememberTidyStyle(TidyComponentStyles.settingsRow, style, interactionSource)
    val actionLabel = stringResource(
        if (expanded) R.string.settings_collapse else R.string.settings_expand
    )
    val expandedState = stringResource(
        if (expanded) R.string.settings_expanded else R.string.settings_collapsed
    )
    Row(
        modifier = modifier
            .tidyStyleSurface(resolver)
            .semantics { stateDescription = expandedState }
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                role = Role.Button,
                onClickLabel = actionLabel,
                onClick = onToggle
            )
            .tidyStyleContent(resolver),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(TidyCardTokens.rowTitleSpacing))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
