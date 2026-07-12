package com.tidy.app.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.layout.*
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.tidy.app.R
import com.tidy.app.TidyURLApp
import com.tidy.app.data.PlusFeature
import com.tidy.app.ui.components.AppIconBox
import com.tidy.app.ui.components.FeatureRow
import com.tidy.app.ui.components.TidyModalBottomSheet
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

object SettingsMigrationViews {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun UpgradePromptRow(onUpgradeClick: () -> Unit) {
        val context = LocalContext.current
        val settingsRepository = TidyURLApp.instance.settingsRepository
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
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
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
                        Text(
                            text = if (isAvailable) {
                                stringResource(R.string.settings_tidy_plus_get_title)
                            } else {
                                stringResource(R.string.settings_tidy_plus_get_title) + " (Coming soon)"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isAvailable) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            }
                        )
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
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
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
                                text = "Tidy+ is set up",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "You can safely uninstall this version anytime.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Uninstall",
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
                                contentDescription = "Dismiss",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        if (showComingSoonSheet) {
            TidyModalBottomSheet(
                onDismissRequest = { showComingSoonSheet = false }
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
                        onClick = { showComingSoonSheet = false },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("OK", fontWeight = FontWeight.Bold)
                    }
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

        TidyModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState
        ) { migrationScrollFix ->
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
                                text = "Tidy+",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        item {
                            Text(
                                text = "Tidy+ is distributed as a separate version via Google Play, which is what makes Play Billing and automatic updates possible. The core engine stays 100% free and open source.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        item {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                        }

                        item {
                            Text(
                                text = "Included with Tidy+",
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
                                            val json =
                                                TidyURLApp.instance.historyRepository.exportToJson()
                                            val backupFile =
                                                File(context.filesDir, "TidyBackup.json")
                                            backupFile.writeText(json)
                                            backupFileState = backupFile
                                            currentPage = 2
                                        } catch (e: Exception) {
                                            Toast.makeText(
                                                context,
                                                "Backup failed: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text("Back up & continue", fontWeight = FontWeight.Bold)
                            }
                        }

                        item {
                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel")
                            }
                        }
                    } else {
                        // Page 2: Backup Share & Play link
                        item {
                            Text(
                                text = "Back Up Created",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        item {
                            Text(
                                text = "Your data has been backed up locally. Share this backup to secure it, then download Tidy+ from Google Play.",
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
                                        text = "Backup location:\n${file.absolutePath}",
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
                                                "Share Tidy Backup"
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    Text("Share backup file", fontWeight = FontWeight.Bold)
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
                                Text("Open Tidy+ on Play", fontWeight = FontWeight.Bold)
                            }
                        }

                        item {
                            TextButton(
                                onClick = onDismiss,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Cancel")
                            }
                        }
                    }
                }
            }
        }
    }
}
