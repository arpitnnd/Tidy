package com.tidy.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A horizontal row consisting of an [AppIconBox] on the left and a two-line
 * [Column] (title + description) on the right. Used for feature listings in
 * the Plus upsell sheet, migration sheet, and the welcome intro trust markers.
 *
 * @param icon        The icon shown in the leading box.
 * @param title       Bold label text.
 * @param description Supporting body text.
 * @param modifier    Optional modifier for the outer Row.
 */
@Composable
fun FeatureRow(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppIconBox(
            icon = icon,
            boxSize = 40.dp,
            iconSize = 20.dp,
            cornerRadius = 10.dp,
            background = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
