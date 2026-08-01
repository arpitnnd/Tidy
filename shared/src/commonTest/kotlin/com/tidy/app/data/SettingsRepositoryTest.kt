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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SettingsRepositoryTest {

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

    private lateinit var dataStore: FakeDataStore
    private lateinit var repository: SettingsRepository

    @Before
    fun setUp() {
        dataStore = FakeDataStore()
        repository = SettingsRepository(dataStore)
    }

    @Test
    fun defaultsMatchDocumentedBehavior() = runTest {
        assertFalse(repository.checkClipboardForLinks.first())
        assertFalse(repository.clipboardCalloutDismissed.first())
        assertEquals(ClipboardCleanTier.SUGGEST, repository.clipboardCleanTier.first())
        assertEquals(ShareCleanTier.CLEAN, repository.shareCleanTier.first())
        assertFalse(repository.closeInsteadOfSharing.first())
        assertTrue(repository.autoExpandShortUrls.first())
        assertTrue(repository.autoRemoveMobileSubdomains.first())
        assertTrue(repository.dropTrailingSlash.first())
        assertTrue(repository.autoCleanOnInput.first())
        assertFalse(repository.dontAskAgainCrash.first())
        assertFalse(repository.migrationDone.first())
        assertFalse(repository.migrationFollowupDismissed.first())
        assertEquals("slate", repository.selectedTheme.first())
        assertTrue(repository.whitelistedDomains.first().isEmpty())
        assertTrue(repository.blacklistedParams.first().isEmpty())
    }

    @Test
    fun settersPersistNewValues() = runTest {
        repository.setCheckClipboardForLinks(true)
        assertTrue(repository.checkClipboardForLinks.first())

        repository.setClipboardCalloutDismissed(true)
        assertTrue(repository.clipboardCalloutDismissed.first())

        repository.setClipboardCleanTier(ClipboardCleanTier.AUTO_CLEAN)
        assertEquals(ClipboardCleanTier.AUTO_CLEAN, repository.clipboardCleanTier.first())

        repository.setShareCleanTier(ShareCleanTier.CLEAN_COPY_AND_SHARE)
        assertEquals(ShareCleanTier.CLEAN_COPY_AND_SHARE, repository.shareCleanTier.first())

        repository.setCloseInsteadOfSharing(true)
        assertTrue(repository.closeInsteadOfSharing.first())

        repository.setAutoExpandShortUrls(false)
        assertFalse(repository.autoExpandShortUrls.first())

        repository.setSelectedTheme("midnight")
        assertEquals("midnight", repository.selectedTheme.first())
    }

    @Test
    fun legacyShareAutomationKeysSeedShareTier() = runTest {
        dataStore.updateData { prefs ->
            val mutable = prefs.toMutablePreferences()
            mutable[SettingsRepository.KEY_AUTO_COPY_ON_SHARE] = true
            mutable
        }
        assertEquals(ShareCleanTier.CLEAN_AND_COPY, repository.shareCleanTier.first())

        dataStore.updateData { prefs ->
            val mutable = prefs.toMutablePreferences()
            mutable[SettingsRepository.KEY_AUTO_CLOSE_ON_SHARE] = true
            mutable
        }
        assertEquals(ShareCleanTier.CLEAN_COPY_AND_SHARE, repository.shareCleanTier.first())
        assertTrue(repository.closeInsteadOfSharing.first())

        // An explicit tier choice wins over the legacy keys.
        repository.setShareCleanTier(ShareCleanTier.CLEAN)
        assertEquals(ShareCleanTier.CLEAN, repository.shareCleanTier.first())
    }

    @Test
    fun legacyLaunchAutoCleanKeySeedsClipboardTier() = runTest {
        dataStore.updateData { prefs ->
            val mutable = prefs.toMutablePreferences()
            mutable[SettingsRepository.KEY_AUTO_CLEAN_CLIPBOARD_ON_LAUNCH] = true
            mutable
        }
        assertEquals(ClipboardCleanTier.AUTO_CLEAN, repository.clipboardCleanTier.first())

        repository.setClipboardCleanTier(ClipboardCleanTier.SUGGEST_AND_COPY)
        assertEquals(ClipboardCleanTier.SUGGEST_AND_COPY, repository.clipboardCleanTier.first())
    }

    @Test
    fun legacyLaunchAutoCleanKeySeedsCheckClipboardForLinks() = runTest {
        // Clipboard checking is off by default, but a pre-tiered user who'd already
        // opted into launch auto-clean keeps clipboard checking on after upgrading.
        dataStore.updateData { prefs ->
            val mutable = prefs.toMutablePreferences()
            mutable[SettingsRepository.KEY_AUTO_CLEAN_CLIPBOARD_ON_LAUNCH] = true
            mutable
        }
        assertTrue(repository.checkClipboardForLinks.first())

        // An explicit choice wins over the legacy key.
        repository.setCheckClipboardForLinks(false)
        assertFalse(repository.checkClipboardForLinks.first())
    }

    @Test
    fun whitelistedDomainsAreTrimmedLowercasedAndDeduped() = runTest {
        repository.addWhitelistedDomain("  Example.COM ")
        repository.addWhitelistedDomain("example.com")

        assertEquals(setOf("example.com"), repository.whitelistedDomains.first())

        repository.removeWhitelistedDomain("EXAMPLE.com")
        assertTrue(repository.whitelistedDomains.first().isEmpty())
    }

    @Test
    fun blacklistedParamsAreTrimmedAndLowercased() = runTest {
        repository.addBlacklistedParam(" Ref_Code ")
        assertEquals(setOf("ref_code"), repository.blacklistedParams.first())

        repository.removeBlacklistedParam("REF_CODE")
        assertTrue(repository.blacklistedParams.first().isEmpty())
    }

    @Test
    fun blankDomainOrParamIsIgnored() = runTest {
        repository.addWhitelistedDomain("   ")
        repository.addBlacklistedParam("   ")

        assertTrue(repository.whitelistedDomains.first().isEmpty())
        assertTrue(repository.blacklistedParams.first().isEmpty())
    }

    @Test
    fun domainWhitelistedParamsUseCompositeKey() = runTest {
        repository.addDomainWhitelistedParam("YouTube.com", "V")
        assertEquals(setOf("youtube.com:v"), repository.domainWhitelistedParams.first())

        repository.removeDomainWhitelistedParam("youtube.com", "v")
        assertTrue(repository.domainWhitelistedParams.first().isEmpty())
    }

    @Test
    fun statsAccumulateAcrossCalls() = runTest {
        repository.addStats(cleanedCount = 2, trackersBlockedCount = 5)
        repository.incrementAnalytics(trackersBlockedCount = 3)

        assertEquals(3, repository.totalCleanedCount.first())
        assertEquals(8, repository.totalTrackersBlocked.first())
    }

    @Test
    fun trackersFlowParsesDefaultBlocklistJson() = runTest {
        val trackers = repository.trackers.first()
        assertTrue(trackers.isNotEmpty())
        assertTrue(trackers.any { it.name == "utm_*" })

        val descriptions = repository.trackerDescriptions.first()
        assertEquals(trackers.size, descriptions.size)
        assertEquals(
            trackers.first { it.name == "utm_*" }.description,
            descriptions["utm_*"]
        )
    }

    @Test
    fun trackersFlowFallsBackToDefaultOnInvalidStoredJson() = runTest {
        repository.setBlocklistJson("not valid json")

        val trackers = repository.trackers.first()
        assertTrue(trackers.isNotEmpty())
        assertTrue(trackers.any { it.name == "utm_*" })
    }

    @Test
    fun blocklistMetadataRoundTrips() = runTest {
        repository.setBlocklistEtag("abc123")
        repository.setBlocklistLastFetchTime(42L)

        assertEquals("abc123", repository.blocklistEtag.first())
        assertEquals(42L, repository.blocklistLastFetchTime.first())
    }
}
