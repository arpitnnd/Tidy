package com.tidy.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.tidy.app.R
import com.tidy.app.theme.TidyCardTokens
import com.tidy.app.theme.TidyTheme
import com.tidy.app.ui.about.AboutScreen
import com.tidy.app.ui.components.ExpandableSettingRow
import com.tidy.app.ui.components.SettingCard
import com.tidy.app.ui.components.TierCard
import com.tidy.app.ui.components.TierSelectorRow

@Preview(name = "Compact short", widthDp = 400, heightDp = 400)
@Preview(name = "Compact medium", widthDp = 400, heightDp = 500)
@Preview(name = "Compact tall", widthDp = 400, heightDp = 1000)
@Preview(name = "Medium short", widthDp = 610, heightDp = 400)
@Preview(name = "Medium medium", widthDp = 610, heightDp = 500)
@Preview(name = "Medium tall", widthDp = 610, heightDp = 1000)
@Preview(name = "Expanded short", widthDp = 900, heightDp = 400)
@Preview(name = "Expanded medium", widthDp = 900, heightDp = 500)
@Preview(name = "Expanded tall", widthDp = 900, heightDp = 1000)
annotation class WindowPreviews

@PreviewTest
@WindowPreviews
@Composable
fun AboutWindowSizes() {
    TidyTheme(selectedTheme = "slate", isPlusUnlocked = false, darkTheme = false) {
        AboutScreen(onBackClick = {})
    }
}

@PreviewTest
@Preview(name = "Light", widthDp = 400, heightDp = 500)
@Preview(name = "Large text", widthDp = 400, heightDp = 800, fontScale = 1.5f)
@Preview(name = "Narrow large text", widthDp = 320, heightDp = 800, fontScale = 2f)
@Preview(name = "Wide", widthDp = 900, heightDp = 500)
@Composable
fun SettingsComponentsLight() {
    SettingsComponents(darkTheme = false)
}

@PreviewTest
@Preview(name = "Dark", widthDp = 400, heightDp = 500)
@Composable
fun SettingsComponentsDark() {
    SettingsComponents(darkTheme = true)
}

@Composable
private fun SettingsComponents(darkTheme: Boolean) {
    TidyTheme(selectedTheme = "slate", isPlusUnlocked = false, darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SettingCard {
                    Column(Modifier.padding(TidyCardTokens.contentPadding)) {
                        ExpandableSettingRow(
                            title = stringResource(R.string.settings_clipboard_tier_heading),
                            summary = stringResource(R.string.tier_suggest_subtitle),
                            expanded = true,
                            onToggle = {}
                        )
                    }
                }
                TierSelectorRow(
                    cards = listOf(
                        TierCard(
                            0, Icons.Outlined.CheckCircle,
                            stringResource(R.string.tier_suggest_title),
                            stringResource(R.string.tier_suggest_subtitle)
                        ),
                        TierCard(
                            1, Icons.Outlined.ContentCopy,
                            stringResource(R.string.tier_suggest_copy_title),
                            stringResource(R.string.tier_suggest_copy_subtitle), locked = true
                        ),
                        TierCard(
                            2, Icons.Outlined.CleaningServices,
                            stringResource(R.string.tier_auto_clean_title),
                            stringResource(R.string.tier_auto_clean_subtitle), locked = true
                        )
                    ),
                    selected = 0,
                    onSelect = {},
                    onLockedTap = {},
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
