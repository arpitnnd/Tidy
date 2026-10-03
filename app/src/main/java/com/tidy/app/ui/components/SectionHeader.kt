package com.tidy.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A primary-coloured, semibold title used to label top-level sections on a screen
 * (e.g. "Dashboard", "Rules", "Clean History").
 *
 * Style: titleMedium · SemiBold · primary colour.
 */
@Composable
fun ScreenSectionHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(vertical = 4.dp).semantics { heading() }
    )
}

/**
 * A smaller primary-coloured label used as a heading inside a card group
 * (e.g. "Built by", "Licence", "Included with Tidy+").
 *
 * Style: labelLarge · SemiBold · primary colour.
 */
@Composable
fun CardSectionLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.semantics { heading() }
    )
}
