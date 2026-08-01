package com.tidy.app.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tidy.app.R
import com.tidy.app.TidyApp
import com.tidy.app.data.PlusFeature
import com.tidy.app.ui.components.AppIconBox
import com.tidy.app.ui.components.FeatureRow
import com.tidy.app.ui.components.TidyModalBottomSheet
import kotlinx.coroutines.launch
import java.io.File

object SettingsMigrationViews {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun UpgradePromptRow(onUpgradeClick: () -> Unit) {
        val context = LocalContext.current
        val settingsRepository = TidyApp.instance.settingsRepository
        val dismissed by settingsRepository.migrationFollowupDismissed.collectAsStateWithLifecycle(
            initialValue = false
        )

        var showFollowup by remember { mutableStateOf(false) }
        var showComingSoonSheet by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            val playInstalled = try {
                context.packageManager.getPackageInfo("com.tidy.app.play", 0)
                true
            } catch (e: Exception) {
                false
            }
            val backupFile = File(context.filesDir, "TidyBackup.json")
            val isBackupOld =
                backupFile.exists() && (System.currentTimeMillis() - backupFile.lastModified() > 24 * 60 * 60 * 1000)
            showFollowup = playInstalled && isBackupOld && !dismissed
        }

        val isAvailable = com.tidy.app.BuildConfig.TIDY_PLUS_AVAILABLE

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Get Tidy+ Row
            Card(
                onClick = {
                    if (isAvailable) {
                        onUpgradeClick()
                    } else {
                        showComingSoonSheet = true
                    }
                },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAvailable) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isAvailable) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (isAvailable) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            },
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.settings_tidy_plus_get_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isAvailable) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                }
                            )
                            if (!isAvailable) {
                                ComingSoonBadge()
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.settings_tidy_plus_upgrade_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isAvailable) {
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                            }
                        )
                    }

                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = if (isAvailable) {
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }

            // Optional Follow-up Banner
            AnimatedVisibility(visible = showFollowup) {
                val scope = rememberCoroutineScope()
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CloudDownload,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.migration_followup_title),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.migration_followup_desc),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.migration_followup_uninstall_action),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable {
                                    val uninstallIntent = Intent(Intent.ACTION_DELETE).apply {
                                        data = Uri.parse("package:com.tidy.app")
                                    }
                                    context.startActivity(uninstallIntent)
                                }
                            )
                        }
                        IconButton(onClick = {
                            scope.launch {
                                settingsRepository.setMigrationFollowupDismissed(true)
                                showFollowup = false
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.banner_clipboard_callout_dismiss),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showComingSoonSheet) {
            ComingSoonSheet(onDismiss = { showComingSoonSheet = false })
        }
    }

    @Composable
    private fun ComingSoonBadge() {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        ) {
            Text(
                text = stringResource(R.string.settings_coming_soon_badge),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }

    /**
     * The "Tidy+ (Coming soon)" prompt shown from the upgrade row above. Also the prompt
     * every other locked/Plus-gated control in the FOSS build surfaces on tap, via
     * FlavorConfig.ShowUpsellBottomSheet -- there's no in-app purchase flow to offer here,
     * so this is the FOSS equivalent of the Play flavor's upsell sheet.
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ComingSoonSheet(onDismiss: () -> Unit) {
        TidyModalBottomSheet(
            onDismissRequest = onDismiss
        ) { comingSoonScrollFix ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, bottom = 32.dp)
                    .navigationBarsPadding()
                    .nestedScroll(comingSoonScrollFix),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.tidy_plus_coming_soon_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.tidy_plus_coming_soon_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(stringResource(R.string.dialog_ok), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MigrationSheet(
        onDismiss: () -> Unit
    ) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        var currentPage by remember { mutableStateOf(1) }
        var backupFileState by remember { mutableStateOf<File?>(null) }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val backupFailedTemplate = stringResource(R.string.migration_backup_failed_format)
        val shareChooserTitle = stringResource(R.string.migration_share_backup_chooser_title)
        val snackbarHostState = remember { SnackbarHostState() }

        TidyModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState
        ) { migrationScrollFix ->
            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .nestedScroll(migrationScrollFix),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (currentPage == 1) {
                            item {
                                // Icon / Logo Box (matching Welcome Intro design)
                                AppIconBox(
                                    icon = Icons.Outlined.CleaningServices,
                                    boxSize = 48.dp,
                                    iconSize = 24.dp,
                                    cornerRadius = 12.dp
                                )
                            }

                            item {
                                Text(
                                    text = stringResource(R.string.migration_headline),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            item {
                                Text(
                                    text = stringResource(R.string.migration_body),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }

                            item {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(
                                        alpha = 0.12f
                                    )
                                )
                            }

                            item {
                                Text(
                                    text = stringResource(R.string.migration_included_heading),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Start
                                )
                            }

                            // Plus Features list items
                            items(PlusFeature.values().size) { index ->
                                val feature = PlusFeature.values()[index]
                                val icon = when (feature) {
                                    PlusFeature.SHARE_AUTOMATION -> Icons.Outlined.Bolt
                                    PlusFeature.BULK_CLEAN -> Icons.Outlined.DynamicFeed
                                    PlusFeature.EXTRA_THEMES -> Icons.Outlined.Palette
                                    PlusFeature.TEXT_SELECTION -> Icons.Outlined.SelectAll
                                }

                                val title = when (feature) {
                                    PlusFeature.SHARE_AUTOMATION -> stringResource(R.string.plus_feature_share_automation_title)
                                    PlusFeature.BULK_CLEAN -> stringResource(R.string.plus_feature_bulk_clean_title)
                                    PlusFeature.EXTRA_THEMES -> stringResource(R.string.plus_feature_extra_themes_title)
                                    PlusFeature.TEXT_SELECTION -> stringResource(R.string.plus_feature_text_selection_title)
                                }

                                val description = when (feature) {
                                    PlusFeature.SHARE_AUTOMATION -> stringResource(R.string.plus_feature_share_automation_desc)
                                    PlusFeature.BULK_CLEAN -> stringResource(R.string.plus_feature_bulk_clean_desc)
                                    PlusFeature.EXTRA_THEMES -> stringResource(R.string.plus_feature_extra_themes_desc)
                                    PlusFeature.TEXT_SELECTION -> stringResource(R.string.plus_feature_text_selection_desc)
                                }

                                FeatureRow(
                                    icon = icon,
                                    title = title,
                                    description = description
                                )
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            item {
                                // Ideology Card: One-time fee vs recurring subscription
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = stringResource(R.string.plus_migration_value_proposition),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(14.dp),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            item {
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            item {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                val json = com.tidy.app.data.buildMigrationBackup(
                                                    TidyApp.instance.historyRepository,
                                                    TidyApp.instance.settingsRepository,
                                                    com.tidy.app.data.BackupPreference.isAllowed(
                                                        context
                                                    )
                                                )
                                                val backupFile =
                                                    File(context.filesDir, "TidyBackup.json")
                                                backupFile.writeText(json)
                                                backupFileState = backupFile
                                                currentPage = 2
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar(
                                                    backupFailedTemplate.format(
                                                        com.tidy.app.data.errorDetail(
                                                            e.message
                                                        )
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.migration_backup_continue_button),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            item {
                                TextButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.dialog_cancel))
                                }
                            }
                        } else {
                            // Page 2: Backup Share & Play link
                            item {
                                Text(
                                    text = stringResource(R.string.migration_backup_saved_title),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            item {
                                Text(
                                    text = stringResource(R.string.migration_backup_saved_desc),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }

                            backupFileState?.let { file ->
                                item {
                                    Card(
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                                alpha = 0.5f
                                            )
                                        ),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = stringResource(
                                                R.string.migration_backup_location_format,
                                                file.absolutePath
                                            ),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                item {
                                    Button(
                                        onClick = {
                                            val uri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                file
                                            )
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "application/json"
                                                putExtra(Intent.EXTRA_STREAM, uri)
                                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                            }
                                            context.startActivity(
                                                Intent.createChooser(
                                                    shareIntent,
                                                    shareChooserTitle
                                                )
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary
                                        )
                                    ) {
                                        Text(
                                            stringResource(R.string.migration_share_backup_button),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            item {
                                Button(
                                    onClick = {
                                        val playId = "com.tidy.app.play"
                                        val playIntent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse("market://details?id=$playId")
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        try {
                                            context.startActivity(playIntent)
                                        } catch (e: Exception) {
                                            val webIntent = Intent(Intent.ACTION_VIEW).apply {
                                                data =
                                                    Uri.parse("https://play.google.com/store/apps/details?id=$playId")
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(webIntent)
                                        }
                                        onDismiss()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        stringResource(R.string.migration_open_play_button),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            item {
                                TextButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.dialog_cancel))
                                }
                            }
                        }
                    }
                }
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
