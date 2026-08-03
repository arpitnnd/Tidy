package com.tidy.app.data

import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class MigrationBackup(
    val history: List<HistoryEntry> = emptyList(),
    val whitelistedDomains: Set<String> = emptySet(),
    val blacklistedParams: Set<String> = emptySet(),
    val domainWhitelistedParams: Set<String> = emptySet(),
    // Nullable so an older/bare-history backup (all fields default to null) never overwrites
    // a real preference on the restoring device with an implicit default.
    val checkClipboardForLinks: Boolean? = null,
    val clipboardCleanTier: String? = null,
    val shareCleanTier: String? = null,
    val closeInsteadOfSharing: Boolean? = null,
    val autoExpandShortUrls: Boolean? = null,
    val autoRemoveMobileSubdomains: Boolean? = null,
    val dropTrailingSlash: Boolean? = null,
    val autoCleanOnInput: Boolean? = null,
    val selectedTheme: String? = null,
    val dontAskAgainCrash: Boolean? = null,
    val allowSystemBackup: Boolean? = null
)

data class MigrationRestoreResult(
    val urlsCleaned: Int,
    val trackersBlocked: Int,
    val whitelistRulesCount: Int,
    val allowSystemBackup: Boolean?
)

private val migrationJson = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
}

/**
 * Deliberately excluded from the bundle: first-launch/migration flow flags
 * (firstLaunchDone, migrationDone, migrationFollowupDismissed), the clipboard
 * callout dismissal, lifetime stat counters, the last-cleaned-url cache, and the
 * synced tracker blocklist cache. These are per-install or derived state, not
 * portable preferences: copying firstLaunchDone in particular would skip the
 * Play app's own first-launch screen, which is where this restore flow lives.
 */
suspend fun buildMigrationBackup(
    historyRepository: HistoryRepository,
    settingsRepository: SettingsRepository,
    allowSystemBackup: Boolean
): String {
    val backup = MigrationBackup(
        history = historyRepository.getEntriesSnapshot(),
        whitelistedDomains = settingsRepository.whitelistedDomains.first(),
        blacklistedParams = settingsRepository.blacklistedParams.first(),
        domainWhitelistedParams = settingsRepository.domainWhitelistedParams.first(),
        checkClipboardForLinks = settingsRepository.checkClipboardForLinks.first(),
        clipboardCleanTier = settingsRepository.clipboardCleanTier.first().key,
        shareCleanTier = settingsRepository.shareCleanTier.first().key,
        closeInsteadOfSharing = settingsRepository.closeInsteadOfSharing.first(),
        autoExpandShortUrls = settingsRepository.autoExpandShortUrls.first(),
        autoRemoveMobileSubdomains = settingsRepository.autoRemoveMobileSubdomains.first(),
        dropTrailingSlash = settingsRepository.dropTrailingSlash.first(),
        autoCleanOnInput = settingsRepository.autoCleanOnInput.first(),
        selectedTheme = settingsRepository.selectedTheme.first(),
        dontAskAgainCrash = settingsRepository.dontAskAgainCrash.first(),
        allowSystemBackup = allowSystemBackup
    )
    return migrationJson.encodeToString(backup)
}

suspend fun restoreMigrationBackup(
    jsonStr: String,
    historyRepository: HistoryRepository,
    settingsRepository: SettingsRepository
): MigrationRestoreResult? {
    val backup = try {
        migrationJson.decodeFromString<MigrationBackup>(jsonStr)
    } catch (e: Exception) {
        // Older backups (or the History screen's own export) are a bare HistoryEntry array.
        try {
            MigrationBackup(history = migrationJson.decodeFromString<List<HistoryEntry>>(jsonStr))
        } catch (e2: Exception) {
            return null
        }
    }

    val importedCount =
        historyRepository.importFromJson(migrationJson.encodeToString(backup.history))
    if (importedCount < 0) return null

    settingsRepository.mergeWhitelistedDomains(backup.whitelistedDomains)
    settingsRepository.mergeBlacklistedParams(backup.blacklistedParams)
    settingsRepository.mergeDomainWhitelistedParams(backup.domainWhitelistedParams)

    backup.checkClipboardForLinks?.let { settingsRepository.setCheckClipboardForLinks(it) }
    backup.clipboardCleanTier?.let {
        settingsRepository.setClipboardCleanTier(ClipboardCleanTier.fromKey(it))
    }
    backup.shareCleanTier?.let {
        settingsRepository.setShareCleanTier(ShareCleanTier.fromKey(it))
    }
    backup.closeInsteadOfSharing?.let { settingsRepository.setCloseInsteadOfSharing(it) }
    backup.autoExpandShortUrls?.let { settingsRepository.setAutoExpandShortUrls(it) }
    backup.autoRemoveMobileSubdomains?.let { settingsRepository.setAutoRemoveMobileSubdomains(it) }
    backup.dropTrailingSlash?.let { settingsRepository.setDropTrailingSlash(it) }
    backup.autoCleanOnInput?.let { settingsRepository.setAutoCleanOnInput(it) }
    backup.selectedTheme?.let { settingsRepository.setSelectedTheme(it) }
    backup.dontAskAgainCrash?.let { settingsRepository.setDontAskAgainCrash(it) }

    val trackersBlocked = backup.history.sumOf { it.removedParamsCount }
    val whitelistRulesCount = backup.whitelistedDomains.size +
            backup.blacklistedParams.size +
            backup.domainWhitelistedParams.size

    return MigrationRestoreResult(
        urlsCleaned = backup.history.size,
        trackersBlocked = trackersBlocked,
        whitelistRulesCount = whitelistRulesCount,
        allowSystemBackup = backup.allowSystemBackup
    )
}
