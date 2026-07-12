package com.tidy.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tidy.app.R
import com.tidy.app.TidyURLApp
import com.tidy.app.ui.components.AppIconBox

object SettingsMigrationViews {

    @Composable
    fun UpgradePromptRow(onUpgradeClick: () -> Unit) {
        val entitlementManager = TidyURLApp.instance.entitlementManager
        val isUnlocked by entitlementManager.isPlusUnlocked.collectAsStateWithLifecycle(initialValue = false)
        val isPending by entitlementManager.isPurchasePending.collectAsStateWithLifecycle(
            initialValue = false
        )

        Card(
            onClick = {
                if (!isUnlocked && !isPending) {
                    onUpgradeClick()
                }
            },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUnlocked) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                } else {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                }
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AppIconBox(
                    icon = Icons.Filled.Star,
                    boxSize = 48.dp,
                    iconSize = 24.dp,
                    cornerRadius = 12.dp,
                    background = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )

                Column(modifier = Modifier.weight(1f)) {
                    val title = if (isUnlocked) {
                        stringResource(R.string.settings_tidy_plus_active_title)
                    } else if (isPending) {
                        stringResource(R.string.settings_tidy_plus_pending_title)
                    } else {
                        stringResource(R.string.settings_tidy_plus_upgrade_title)
                    }
                    val subtitle = if (isUnlocked) {
                        stringResource(R.string.settings_tidy_plus_active_desc)
                    } else if (isPending) {
                        stringResource(R.string.settings_tidy_plus_pending_desc)
                    } else {
                        stringResource(R.string.settings_tidy_plus_upgrade_desc)
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (!isUnlocked && !isPending) {
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }

    @Composable
    fun MigrationSheet(onDismiss: () -> Unit) {
        // No migration sheet needed for play flavor
    }
}
