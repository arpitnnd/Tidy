package com.tidy.app

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tidy.app.data.PlayEntitlementManager
import kotlinx.coroutines.launch

object FlavorConfig {
    val isPlayFlavor: Boolean = true

    val jsonInstance = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }

    fun createEntitlementManager(context: android.content.Context): com.tidy.app.data.AndroidEntitlementManager {
        return PlayEntitlementManager(context)
    }

    @Composable
    fun SettingsUpgradeRow(
        onUpgradeClick: () -> Unit
    ) {
        com.tidy.app.ui.settings.SettingsMigrationViews.UpgradePromptRow(onUpgradeClick)
    }

    @Composable
    fun OnboardingExtra(onDismiss: () -> Unit) {
        PlayOnboardingRestoreView(onDismiss)
    }

    @Composable
    fun ShowUpsellBottomSheet(onDismiss: () -> Unit) {
        val context = LocalContext.current
        val activity = context as? android.app.Activity
        com.tidy.app.featureplus.PlusUpsellBottomSheet(
            onDismiss = onDismiss,
            launchPurchaseFlow = { act ->
                createEntitlementManager(context).launchPurchaseFlow(act) { errorMsg ->
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    fun getPremiumColorScheme(
        themeName: String,
        darkTheme: Boolean
    ): androidx.compose.material3.ColorScheme? {
        return com.tidy.app.featureplus.PremiumThemes.getColorScheme(themeName, darkTheme)
    }

    @Composable
    fun BulkClipboardCleanCard(
        bulkClipboardUrls: List<String>?,
        onDismiss: () -> Unit,
        isPlusUnlocked: Boolean,
        onShowUpsell: () -> Unit,
        onCleanExecute: () -> Unit
    ) {
        com.tidy.app.featureplus.BulkClipboardCleanCard(
            bulkClipboardUrls = bulkClipboardUrls,
            onDismiss = onDismiss,
            isPlusUnlocked = isPlusUnlocked,
            onShowUpsell = onShowUpsell,
            onCleanExecute = onCleanExecute
        )
    }

    suspend fun resolveClipboardTier(
        settingsRepository: com.tidy.app.data.SettingsRepository
    ): com.tidy.app.data.ClipboardCleanTier {
        return com.tidy.app.featureplus.PlusAutomation.resolveClipboardTier(settingsRepository)
    }

    suspend fun handleShareAutomation(
        settingsRepository: com.tidy.app.data.SettingsRepository,
        onOutcome: (com.tidy.app.data.ShareAutomationOutcome) -> Unit
    ) {
        com.tidy.app.featureplus.PlusAutomation.handleShareAutomation(
            settingsRepository,
            onOutcome
        )
    }

    val isProcessTextAvailable: Boolean = true

    fun isProcessTextEnabled(context: android.content.Context): Boolean {
        return com.tidy.app.featureplus.ProcessTextMenu.isEnabled(context)
    }

    fun setProcessTextEnabled(context: android.content.Context, enabled: Boolean) {
        com.tidy.app.featureplus.ProcessTextMenu.setEnabled(context, enabled)
    }
}

@Composable
fun PlayOnboardingRestoreView(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val historyRepository = TidyApp.instance.historyRepository
    val settingsRepository = TidyApp.instance.settingsRepository

    var showResultDialog by remember { mutableStateOf(false) }
    var resultText by remember { mutableStateOf("") }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val json = inputStream?.bufferedReader()?.readText()
                    inputStream?.close()
                    if (json != null) {
                        val importedCount = historyRepository.importFromJson(json)
                        if (importedCount >= 0) {
                            val list = FlavorConfig.jsonInstance
                                .decodeFromString<List<com.tidy.app.data.HistoryEntry>>(json)
                            val totalTrackers = list.sumOf { it.removedParamsCount }

                            // Save stats to settings repository
                            settingsRepository.addStats(list.size, totalTrackers)

                            resultText =
                                "Restored: ${list.size} URLs cleaned, $totalTrackers trackers blocked, 0 whitelist rules"
                            showResultDialog = true
                        } else {
                            Toast.makeText(
                                context,
                                "Import failed: invalid file format",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(
            onClick = { importLauncher.launch("application/json") }
        ) {
            Text(
                text = "Used Tidy before? Restore your data",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showResultDialog) {
        AlertDialog(
            onDismissRequest = {
                showResultDialog = false
                onDismiss()
            },
            title = { Text("Restore Complete", fontWeight = FontWeight.Bold) },
            text = { Text(resultText) },
            confirmButton = {
                Button(
                    onClick = {
                        showResultDialog = false
                        onDismiss()
                    }
                ) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
