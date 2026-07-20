package com.tidy.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class MigrationBackupTest {

    // In-memory fake DataStore so tests don't touch disk.
    private class FakeDataStore : DataStore<Preferences> {
        val stateFlow = MutableStateFlow<Preferences>(emptyPreferences())
        override val data: Flow<Preferences> = stateFlow
        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val new = transform(stateFlow.value)
            stateFlow.value = new
            return new
        }
    }

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var historyRepository: HistoryRepository
    private lateinit var settingsRepository: SettingsRepository

    @Before
    fun setUp() {
        historyRepository = HistoryRepository(testFile = File(tempFolder.root, "history.json"))
        settingsRepository = SettingsRepository(FakeDataStore())
    }

    private fun sampleEntry(
        originalUrl: String,
        removedParamsCount: Int = 2
    ) = HistoryEntry(
        originalUrl = originalUrl,
        cleanedUrl = originalUrl,
        removedParamsCount = removedParamsCount,
        domain = HistoryRepository.extractDomain(originalUrl)
    )

    @Test
    fun buildThenRestoreRoundTripsHistoryRulesAndSettings() = runTest {
        historyRepository.addEntry(sampleEntry("https://a.com/1"))
        historyRepository.addEntry(sampleEntry("https://b.com/2"))
        settingsRepository.addWhitelistedDomain("example.com")
        settingsRepository.addBlacklistedParam("ref")
        settingsRepository.addDomainWhitelistedParam("shop.com", "sku")
        settingsRepository.setCheckClipboardForLinks(true)
        settingsRepository.setClipboardCleanTier(ClipboardCleanTier.AUTO_CLEAN)
        settingsRepository.setShareCleanTier(ShareCleanTier.CLEAN_COPY_AND_SHARE)
        settingsRepository.setCloseInsteadOfSharing(true)
        settingsRepository.setAutoExpandShortUrls(false)
        settingsRepository.setAutoRemoveMobileSubdomains(false)
        settingsRepository.setAutoCleanOnInput(false)
        settingsRepository.setSelectedTheme("forest")
        settingsRepository.setDontAskAgainCrash(true)

        val backupJson =
            buildMigrationBackup(historyRepository, settingsRepository, allowSystemBackup = true)

        val freshHistory = HistoryRepository(testFile = File(tempFolder.root, "restored.json"))
        val freshSettings = SettingsRepository(FakeDataStore())

        val result = restoreMigrationBackup(backupJson, freshHistory, freshSettings)

        assertEquals(
            MigrationRestoreResult(
                urlsCleaned = 2,
                trackersBlocked = 4,
                whitelistRulesCount = 3,
                allowSystemBackup = true
            ),
            result
        )
        assertEquals(setOf("example.com"), freshSettings.whitelistedDomains.first())
        assertEquals(setOf("ref"), freshSettings.blacklistedParams.first())
        assertEquals(setOf("shop.com:sku"), freshSettings.domainWhitelistedParams.first())
        assertTrue(freshSettings.checkClipboardForLinks.first())
        assertEquals(ClipboardCleanTier.AUTO_CLEAN, freshSettings.clipboardCleanTier.first())
        assertEquals(ShareCleanTier.CLEAN_COPY_AND_SHARE, freshSettings.shareCleanTier.first())
        assertTrue(freshSettings.closeInsteadOfSharing.first())
        assertFalse(freshSettings.autoExpandShortUrls.first())
        assertFalse(freshSettings.autoRemoveMobileSubdomains.first())
        assertFalse(freshSettings.autoCleanOnInput.first())
        assertEquals("forest", freshSettings.selectedTheme.first())
        assertTrue(freshSettings.dontAskAgainCrash.first())
    }

    @Test
    fun restoreMergesRulesWithoutDroppingExistingOnes() = runTest {
        settingsRepository.addWhitelistedDomain("already-here.com")
        historyRepository.addEntry(sampleEntry("https://a.com/1"))

        val other = HistoryRepository(testFile = File(tempFolder.root, "other.json"))
        val otherSettings = SettingsRepository(FakeDataStore())
        otherSettings.addWhitelistedDomain("imported.com")
        val backupJson = buildMigrationBackup(other, otherSettings, allowSystemBackup = false)

        restoreMigrationBackup(backupJson, historyRepository, settingsRepository)

        assertEquals(
            setOf("already-here.com", "imported.com"),
            settingsRepository.whitelistedDomains.first()
        )
    }

    @Test
    fun restoreAcceptsOldPlainHistoryArrayFormatWithoutTouchingRulesOrSettings() = runTest {
        settingsRepository.setSelectedTheme("velvet")
        settingsRepository.setAutoExpandShortUrls(false)

        val other = HistoryRepository(testFile = File(tempFolder.root, "legacy.json"))
        other.addEntry(sampleEntry("https://legacy.com/1"))
        val legacyJson = other.exportToJson()

        val result = restoreMigrationBackup(legacyJson, historyRepository, settingsRepository)

        assertEquals(1, result?.urlsCleaned)
        assertEquals(0, result?.whitelistRulesCount)
        assertNull(result?.allowSystemBackup)
        assertTrue(settingsRepository.whitelistedDomains.first().isEmpty())
        // Pre-existing local settings must survive an old-format restore untouched, since a
        // legacy backup carries no settings fields at all (they decode as null, not false/default).
        assertEquals("velvet", settingsRepository.selectedTheme.first())
        assertFalse(settingsRepository.autoExpandShortUrls.first())
    }

    @Test
    fun restoreReturnsNullOnMalformedJson() = runTest {
        val result =
            restoreMigrationBackup("{ not valid json ", historyRepository, settingsRepository)
        assertNull(result)
    }
}
