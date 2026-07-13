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

    private lateinit var repository: SettingsRepository

    @Before
    fun setUp() {
        repository = SettingsRepository(FakeDataStore())
    }

    @Test
    fun defaultsMatchDocumentedBehavior() = runTest {
        assertFalse(repository.autoCopyOnShare.first())
        assertFalse(repository.autoCloseOnShare.first())
        assertTrue(repository.autoExpandShortUrls.first())
        assertTrue(repository.autoRemoveMobileSubdomains.first())
        assertFalse(repository.autoCleanClipboardOnLaunch.first())
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
        repository.setAutoCopyOnShare(true)
        assertTrue(repository.autoCopyOnShare.first())

        repository.setAutoExpandShortUrls(false)
        assertFalse(repository.autoExpandShortUrls.first())

        repository.setSelectedTheme("midnight")
        assertEquals("midnight", repository.selectedTheme.first())
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
        assertTrue(trackers.any { it.name == "utm_source" })

        val descriptions = repository.trackerDescriptions.first()
        assertEquals(trackers.size, descriptions.size)
        assertEquals(trackers.first { it.name == "utm_source" }.description, descriptions["utm_source"])
    }

    @Test
    fun trackersFlowFallsBackToDefaultOnInvalidStoredJson() = runTest {
        repository.setBlocklistJson("not valid json")

        val trackers = repository.trackers.first()
        assertTrue(trackers.isNotEmpty())
        assertTrue(trackers.any { it.name == "utm_source" })
    }

    @Test
    fun blocklistMetadataRoundTrips() = runTest {
        repository.setBlocklistEtag("abc123")
        repository.setBlocklistLastFetchTime(42L)

        assertEquals("abc123", repository.blocklistEtag.first())
        assertEquals(42L, repository.blocklistLastFetchTime.first())
    }
}
