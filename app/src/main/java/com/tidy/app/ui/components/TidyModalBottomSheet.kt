package com.tidy.app.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.unit.dp

/**
 * A standardised ModalBottomSheet for the Tidy application.
 *
 * It configures standard Material 3 specs (surface colour, tonal elevation, drag handle,
 * and skipping partial expansion by default) and exposes a [NestedScrollConnection]
 * (via rememberSheetNestedScrollFix) to the content scope to prevent sheet vibration/jitter
 * during internal scrolling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TidyModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable ColumnScope.(NestedScrollConnection) -> Unit
) {
    val scrollFix = rememberSheetNestedScrollFix()
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier
    ) {
        content(scrollFix)
    }
}
