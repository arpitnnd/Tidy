package com.tidy.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val stackCards = cards.isNotEmpty() &&
                maxWidth / cards.size / LocalDensity.current.fontScale < 96.dp
        if (stackCards) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().selectableGroup()
            ) {
                cards.forEach { card ->
                    TierOption(
                        card, card.value == selected, onSelect, onLockedTap,
                        Modifier.fillMaxWidth(), fillHeight = false
                    )
                }
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min).selectableGroup()
            ) {
                cards.forEach { card ->
                    TierOption(
                        card, card.value == selected, onSelect, onLockedTap,
                        Modifier.weight(1f).fillMaxHeight(), fillHeight = true
                    )
                }
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
    fillHeight: Boolean
) {
    val lockedDescription = stringResource(R.string.tooltip_premium_feature)
    Box(
        modifier = modifier
    ) {
        Card(
            onClick = {
                if (card.locked) onLockedTap() else onSelect(card.value)
            },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            ),
            border = if (isSelected) {
                BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
            } else {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
            },
            modifier = Modifier
                .then(if (fillHeight) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
                .semantics {
                    role = Role.RadioButton
                    this.selected = isSelected
                    if (card.locked) stateDescription = lockedDescription
                }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (fillHeight) Modifier.fillMaxHeight() else Modifier)
                    .padding(horizontal = 8.dp, vertical = 14.dp)
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
