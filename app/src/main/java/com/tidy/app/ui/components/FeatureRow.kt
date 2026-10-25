package com.tidy.app.ui.components

import androidx.compose.foundation.style.CommonStyle
import androidx.compose.foundation.shape.RoundedCornerShape
import com.tidy.designsystem.TidyComponentStyles
import com.tidy.designsystem.TidyStyle
import com.tidy.designsystem.rememberTidyStyle
import com.tidy.designsystem.tidyStyleSurface
import com.tidy.designsystem.tidyStyleContent
import com.tidy.designsystem.LocalTidyColours
import com.tidy.designsystem.background
import com.tidy.designsystem.shape
import com.tidy.designsystem.size

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
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
    modifier: Modifier = Modifier,
    style: TidyStyle = CommonStyle
) {
    val resolver = rememberTidyStyle(TidyComponentStyles.featureRow, style)
    Row(
        modifier = modifier.tidyStyleSurface(resolver).tidyStyleContent(resolver),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AppIconBox(
            icon = icon,
            iconSize = 20.dp,
            style = CommonStyle {
                size(40.dp)
                shape(RoundedCornerShape(10.dp))
                val colours = LocalTidyColours.currentValue
                background(
                    colours.primaryContainer.copy(alpha = 0.4f).compositeOver(colours.surface)
                )
            }
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
