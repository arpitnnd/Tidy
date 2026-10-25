package com.tidy.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tidy.app.theme.removedParameterColours

/** The same muted removal treatment in Home and History, independent of accent palette. */
@Composable
fun RemovedParameterChip(parameter: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colours = MaterialTheme.colorScheme.removedParameterColours()
    AssistChip(
        onClick = onClick,
        label = { Text(parameter, style = MaterialTheme.typography.labelMedium) },
        trailingIcon = {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
        },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = colours.container,
            labelColor = colours.content,
            trailingIconContentColor = colours.content
        ),
        shape = CircleShape,
        border = null
    )
}
