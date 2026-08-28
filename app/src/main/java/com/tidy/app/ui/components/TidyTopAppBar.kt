package com.tidy.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.tidy.app.R

/**
 * The standard Tidy top app bar. Provides a consistent transparent bar with a bold title
 * and a back-navigation button wrapped in a [TooltipWrapper].
 *
 * @param title      The screen title to display.
 * @param onBackClick Called when the back arrow is pressed. Pass `null` to hide the nav icon.
 * @param actions    Optional trailing action composable slot.
 * @param modifier   Optional modifier for the bar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TidyTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                TooltipWrapper(tooltipText = stringResource(R.string.tooltip_back)) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.settings_back_desc),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
    )
}
