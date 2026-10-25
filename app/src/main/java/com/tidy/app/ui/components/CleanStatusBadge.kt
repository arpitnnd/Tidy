package com.tidy.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.tidy.app.R
import com.tidy.app.theme.cleanResultColours

/** Shared status presentation for Home results and History entries. */
@Composable
fun CleanStatusBadge(removedCount: Int, modifier: Modifier = Modifier) {
    val colours = MaterialTheme.colorScheme.cleanResultColours()
    StatusBadge(
        text = if (removedCount > 0) {
            pluralStringResource(R.plurals.main_cleaned_count, removedCount, removedCount)
        } else {
            stringResource(R.string.history_item_clean)
        },
        modifier = modifier,
        containerColor = colours.container,
        contentColor = colours.content
    )
}
