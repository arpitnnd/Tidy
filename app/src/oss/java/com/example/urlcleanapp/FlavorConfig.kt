package com.example.urlcleanapp

import androidx.compose.runtime.Composable
import com.example.urlcleanapp.data.EntitlementManager
import com.example.urlcleanapp.data.OssEntitlementManager

object FlavorConfig {
    val isPlayFlavor: Boolean = false

    fun createEntitlementManager(context: android.content.Context): EntitlementManager {
        return OssEntitlementManager()
    }

    @Composable
    fun SettingsUpgradeRow(
        onUpgradeClick: () -> Unit
    ) {
        com.example.urlcleanapp.ui.settings.SettingsMigrationViews.SettingsUpgradeRow(onUpgradeClick)
    }

    @Composable
    fun OnboardingExtra(onDismiss: () -> Unit) {
        // No-op for OSS
    }

    @Composable
    fun ShowUpsellBottomSheet(onDismiss: () -> Unit) {
        // No-op for OSS
    }

    fun getPremiumColorScheme(themeName: String, darkTheme: Boolean): androidx.compose.material3.ColorScheme? {
        return null
    }

    @Composable
    fun BulkClipboardCleanCard(
        bulkClipboardUrls: List<String>?,
        onDismiss: () -> Unit,
        isPlusUnlocked: Boolean,
        onShowUpsell: () -> Unit,
        onCleanExecute: () -> Unit
    ) {
        // No-op for OSS
    }

    suspend fun handleShareAutomation(
        cleanedUrl: String,
        settingsRepository: com.example.urlcleanapp.data.SettingsRepository,
        onCopyAndClose: (Boolean) -> Unit
    ) {
        // No-op for OSS
    }
}
