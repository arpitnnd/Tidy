package com.tidy.app

import androidx.compose.runtime.Composable
import com.tidy.app.data.EntitlementManager
import com.tidy.app.data.FossEntitlementManager

object FlavorConfig {
    val isPlayFlavor: Boolean = false

    fun createEntitlementManager(context: android.content.Context): com.tidy.app.data.AndroidEntitlementManager {
        return FossEntitlementManager()
    }

    @Composable
    fun SettingsUpgradeRow(
        onUpgradeClick: () -> Unit
    ) {
        com.tidy.app.ui.settings.SettingsMigrationViews.UpgradePromptRow(onUpgradeClick)
    }

    @Composable
    fun OnboardingExtra(onDismiss: () -> Unit) {
        // No-op for FOSS
    }

    @Composable
    fun ShowUpsellBottomSheet(onDismiss: () -> Unit) {
        // No-op for FOSS
    }

    fun getPremiumColorScheme(
        themeName: String,
        darkTheme: Boolean
    ): androidx.compose.material3.ColorScheme? {
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
        // No-op for FOSS
    }

    suspend fun handleShareAutomation(
        cleanedUrl: String,
        settingsRepository: com.tidy.app.data.SettingsRepository,
        onCopyAndClose: (Boolean) -> Unit
    ) {
        // No-op for FOSS
    }
}
