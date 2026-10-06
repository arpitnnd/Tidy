@file:OptIn(androidx.compose.foundation.style.ExperimentalFoundationStyleApi::class)

package com.tidy.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tidy.app.R
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Grid
import androidx.compose.foundation.layout.GridTrackSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.style.CommonStyle
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import com.tidy.designsystem.TidyComponentStyles
import com.tidy.designsystem.TidyStyle
import com.tidy.designsystem.rememberTidyStyle
import com.tidy.designsystem.tidyStyleContent
import com.tidy.designsystem.tidyStyleSurface

/**
 * One selectable card in a [TierSelectorRow]. [locked] marks a Tidy+ tier that the
 * current user hasn't unlocked; tapping it triggers the upsell instead of selecting.
 */
data class TierCard<T>(
    val value: T,
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val locked: Boolean = false
)

/**
 * An adaptive group of selectable cards (icon, title, one-line subtitle),
 * with the selected card highlighted by a primary border — the battery-mode-selector
 * pattern. Cards stack when width or font scaling makes a row too narrow.
 * Locked cards retain readable content and show a lock badge.
 */
@Composable
fun <T> TierSelectorRow(
    cards: List<TierCard<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    onLockedTap: () -> Unit,
    modifier: Modifier = Modifier,
    style: TidyStyle = CommonStyle
) {
    val fontScale = LocalDensity.current.fontScale
    Grid(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        config = {
            val count = cards.size.coerceAtLeast(1)
            val availableWidth = constraints.maxWidth.toDp() - 8.dp * (count - 1)
            val columns = if (availableWidth / count / fontScale < 96.dp) 1 else count
            repeat(columns) { column(GridTrackSize.MinMax(0.dp, 1.fr)) }
            gap(8.dp)
        }
    ) {
        cards.forEach { card ->
            androidx.compose.runtime.key(card.value) {
                TierOption(
                    card, card.value == selected, onSelect, onLockedTap,
                    Modifier.fillMaxWidth().fillMaxHeight(), style
                )
            }
        }
    }
}

@Composable
private fun <T> TierOption(
    card: TierCard<T>,
    isSelected: Boolean,
    onSelect: (T) -> Unit,
    onLockedTap: () -> Unit,
    modifier: Modifier,
    style: TidyStyle
) {
    val lockedDescription = stringResource(R.string.tooltip_premium_feature)
    Box(
        modifier = modifier
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val resolver = rememberTidyStyle(
            TidyComponentStyles.tierCard, style, interactionSource, selected = isSelected
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxHeight()
                .tidyStyleSurface(resolver)
                .semantics { if (card.locked) stateDescription = lockedDescription }
                .selectable(
                    selected = isSelected,
                    interactionSource = interactionSource,
                    indication = ripple(),
                    role = Role.RadioButton,
                    onClick = { if (card.locked) onLockedTap() else onSelect(card.value) }
                )
                .tidyStyleContent(resolver)
        ) {
            Icon(
                imageVector = card.icon,
                contentDescription = null,
                tint = (if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = card.title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = card.subtitle,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (card.locked) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(18.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
    }
}
