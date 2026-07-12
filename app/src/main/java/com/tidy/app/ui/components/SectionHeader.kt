package com.tidy.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A primary-coloured, extra-bold title used to label top-level sections on a screen
 * (e.g. "Dashboard", "Rules", "Clean History").
 *
 * Style: titleMedium · ExtraBold · primary colour.
 */
@Composable
fun ScreenSectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = 4.dp)
    )
}

/**
 * A smaller primary-coloured label used as a heading inside a card group
 * (e.g. "Built by", "License", "Included with Tidy+").
 *
 * Style: labelLarge · Bold · primary colour.
 */
@Composable
fun CardSectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}
