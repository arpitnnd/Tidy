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
        // FOSS has no in-app purchase flow, so this mirrors whatever the "Get Tidy+"
        // row itself shows on tap: the migration flow once Plus ships, the coming-soon
        // sheet until then.
        if (com.tidy.app.BuildConfig.TIDY_PLUS_AVAILABLE) {
            com.tidy.app.ui.settings.SettingsMigrationViews.MigrationSheet(onDismiss = onDismiss)
        } else {
            com.tidy.app.ui.settings.SettingsMigrationViews.ComingSoonSheet(onDismiss = onDismiss)
        }
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

    suspend fun resolveClipboardTier(
        settingsRepository: com.tidy.app.data.SettingsRepository
    ): com.tidy.app.data.ClipboardCleanTier {
        // FOSS has no Tidy+ automation: always the free Suggest baseline.
        return com.tidy.app.data.ClipboardCleanTier.SUGGEST
    }

    suspend fun handleShareAutomation(
        settingsRepository: com.tidy.app.data.SettingsRepository,
        onOutcome: (com.tidy.app.data.ShareAutomationOutcome) -> Unit
    ) {
        // No-op for FOSS: the free Clean baseline shows the result in-app.
    }

    // The text-selection "Clean with Tidy" entry ships in the plus module only.
    val isProcessTextAvailable: Boolean = false

    fun isProcessTextEnabled(context: android.content.Context): Boolean = false

    fun setProcessTextEnabled(context: android.content.Context, enabled: Boolean) {
        // No-op for FOSS
    }
}
