package com.tidy.app.ui.main

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.viewModelScope
import com.tidy.app.data.HistoryRepository
import com.tidy.app.data.SettingsRepository
import com.tidy.app.data.UrlCleaner
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class MainScreenViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var settingsRepository: SettingsRepository
    private lateinit var historyRepository: HistoryRepository
    private lateinit var viewModel: MainScreenViewModel
    private val fakeDataStore = FakeDataStore()

    // In-memory fake DataStore for preferences to avoid background disk I/O threads in unit tests
    private class FakeDataStore : DataStore<Preferences> {
        val stateFlow = MutableStateFlow<Preferences>(emptyPreferences())

        override val data: Flow<Preferences> = stateFlow

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
            val current = stateFlow.value
            val new = transform(current)
            stateFlow.value = new
            return new
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        settingsRepository = SettingsRepository(fakeDataStore)

        val historyFile = File(tempFolder.root, "history.json")
        historyRepository = HistoryRepository(testFile = historyFile)

        viewModel = MainScreenViewModel(
            settingsRepository = settingsRepository,
            historyRepository = historyRepository,
            urlCleaner = UrlCleaner()
        )
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialUiState() = runTest {
        val state = viewModel.uiState.value
        assertEquals("", state.inputUrl)
        assertEquals(false, state.isCleaned)
        assertEquals(0, state.totalCleanedCount)
        assertEquals(0, state.totalTrackersBlocked)
    }

    @Test
    fun testCleanUrl() = runTest {
        // "si" is deliberately not used here -- it's domain-scoped to Spotify/YouTube.
        viewModel.cleanUrl("https://example.com/page?utm_source=newsletter&utm_medium=email&fbclid=12345&igsh=abc")

        val state = viewModel.uiState.value
        assertEquals("https://example.com/page", state.cleanedUrl)
        assertEquals(true, state.isCleaned)
        assertEquals(4, state.removedParams.size)
        assertEquals(1, state.totalCleanedCount)
        assertEquals(4, state.totalTrackersBlocked)

        // Verify history contains entry
        historyRepository.loadHistory()
        val history = historyRepository.historyFlow.value
        assertEquals(1, history.size)
        assertEquals("https://example.com/page", history[0].cleanedUrl)
        assertEquals("example.com", history[0].domain)
        assertEquals(4, history[0].removedParamsCount)
    }

    @Test
    fun cleanUrlHonorsSyncedBlocklistOverCompiledDefault() = runTest {
        // Simulate a GitHub-synced blocklist containing only a never-before-seen param
        // (not utm_-prefixed, so only the live/synced list can cause it to be stripped).
        fakeDataStore.updateData { prefs ->
            val mutable = prefs.toMutablePreferences()
            mutable[SettingsRepository.KEY_BLOCKLIST_JSON] =
                """[{"name":"zzz_custom_tracker","description":"synced only"}]"""
            mutable
        }

        viewModel.cleanUrl("https://example.com/page?zzz_custom_tracker=1&fbclid=2")

        val state = viewModel.uiState.value
        // The synced param is stripped...
        assertTrue(state.removedParams.contains("zzz_custom_tracker"))
        // ...but fbclid (a compiled default absent from the synced list) is kept, proving
        // the live synced list is honoured instead of UrlCleaner.DEFAULT_TRACKERS.
        assertEquals("https://example.com/page?fbclid=2", state.cleanedUrl)
    }

    @Test
    fun cleanUrlUsesCompiledDefaultsWhenNoBlocklistSynced() = runTest {
        viewModel.cleanUrl("https://example.com/page?fbclid=2&keep=1")

        val state = viewModel.uiState.value
        assertEquals("https://example.com/page?keep=1", state.cleanedUrl)
        assertTrue(state.removedParams.contains("fbclid"))
    }

    @Test
    fun testMarkFirstLaunchDone() = runTest {
        viewModel.markFirstLaunchDone()

        val firstLaunchDone = settingsRepository.firstLaunchDone.first()
        assertTrue(firstLaunchDone)
    }
}
