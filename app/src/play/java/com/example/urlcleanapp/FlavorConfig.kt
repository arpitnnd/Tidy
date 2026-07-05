package com.example.urlcleanapp

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.urlcleanapp.data.EntitlementManager
import com.example.urlcleanapp.data.PlayEntitlementManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

object FlavorConfig {
    val isPlayFlavor: Boolean = true

    fun createEntitlementManager(context: android.content.Context): com.example.urlcleanapp.data.AndroidEntitlementManager {
        return PlayEntitlementManager(context)
    }

    @Composable
    fun SettingsUpgradeRow(
        onUpgradeClick: () -> Unit
    ) {
        com.example.urlcleanapp.ui.settings.SettingsMigrationViews.SettingsUpgradeRow(onUpgradeClick)
    }

    @Composable
    fun OnboardingExtra(onDismiss: () -> Unit) {
        PlayOnboardingRestoreView(onDismiss)
    }

    @Composable
    fun ShowUpsellBottomSheet(onDismiss: () -> Unit) {
        val context = LocalContext.current
        val activity = context as? android.app.Activity
        com.example.urlcleanapp.featureplus.PlusUpsellBottomSheet(
            onDismiss = onDismiss,
            launchPurchaseFlow = { act ->
                createEntitlementManager(context).launchPurchaseFlow(act) { errorMsg ->
                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    fun getPremiumColorScheme(themeName: String, darkTheme: Boolean): androidx.compose.material3.ColorScheme? {
        return com.example.urlcleanapp.featureplus.PremiumThemes.getColorScheme(themeName, darkTheme)
    }

    @Composable
    fun BulkClipboardCleanCard(
        bulkClipboardUrls: List<String>?,
        onDismiss: () -> Unit,
        isPlusUnlocked: Boolean,
        onShowUpsell: () -> Unit,
        onCleanExecute: () -> Unit
    ) {
        com.example.urlcleanapp.featureplus.BulkClipboardCleanCard(
            bulkClipboardUrls = bulkClipboardUrls,
            onDismiss = onDismiss,
            isPlusUnlocked = isPlusUnlocked,
            onShowUpsell = onShowUpsell,
            onCleanExecute = onCleanExecute
        )
    }

    suspend fun handleShareAutomation(
        cleanedUrl: String,
        settingsRepository: com.example.urlcleanapp.data.SettingsRepository,
        onCopyAndClose: (Boolean) -> Unit
    ) {
        val isUnlocked = UrlCleanApp.instance.entitlementManager.isPlusUnlocked.first()
        if (isUnlocked) {
            val autoCopy = settingsRepository.autoCopyOnShare.first()
            if (autoCopy) {
                val autoClose = settingsRepository.autoCloseOnShare.first()
                onCopyAndClose(autoClose)
            }
        }
    }
}

@Composable
fun PlayOnboardingRestoreView(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val historyRepository = UrlCleanApp.instance.historyRepository
    val settingsRepository = UrlCleanApp.instance.settingsRepository

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
                            val list = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
                                .decodeFromString<List<com.example.urlcleanapp.data.HistoryEntry>>(json)
                            val totalTrackers = list.sumOf { it.removedParamsCount }
                            
                            // Save stats to settings repository
                            settingsRepository.addStats(list.size, totalTrackers)
                            
                            resultText = "Restored: ${list.size} URLs cleaned, $totalTrackers trackers blocked, 0 whitelist rules"
                            showResultDialog = true
                        } else {
                            Toast.makeText(context, "Import failed: invalid file format", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
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
