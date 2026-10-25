package com.tidy.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

/** Connects custom component styles to the host's current Material palette. */
@Composable
fun TidyStyleTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalTidyColours provides MaterialTheme.colorScheme, content = content)
}
