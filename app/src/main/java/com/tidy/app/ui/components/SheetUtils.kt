package com.tidy.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

/**
 * Returns a [NestedScrollConnection] that consumes all unconsumed upward scroll and fling
 * velocity before it reaches the parent [ModalBottomSheet] drag handler.
 *
 * This prevents the sheet from vibrating or jittering when the user scrolls up inside
 * a scrollable sheet content. The downward scroll path is left untouched so the
 * swipe-down-to-dismiss gesture still works naturally.
 */
@Composable
fun rememberSheetNestedScrollFix(): NestedScrollConnection = remember {
    object : NestedScrollConnection {
        override fun onPreScroll(
            available: Offset,
            source: NestedScrollSource
        ): Offset = Offset.Zero

        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset = if (available.y < 0) available else Offset.Zero

        override suspend fun onPostFling(
            consumed: Velocity,
            available: Velocity
        ): Velocity = if (available.y < 0) available else Velocity.Zero
    }
}
