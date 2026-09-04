package com.tidy.app.ui.history

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.viewModelScope
import com.tidy.app.data.HistoryEntry
import com.tidy.app.data.HistoryRepository
import com.tidy.app.data.SettingsRepository
import kotlinx.coroutines.CompletableDeferred
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
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryScreenViewModelTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = UnconfinedTestDispatcher()
    private val fakeDataStore = FakeDataStore()
    private lateinit var historyRepository: HistoryRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var viewModel: HistoryScreenViewModel

    private class FakeDataStore : DataStore<Preferences> {
        val stateFlow = MutableStateFlow<Preferences>(emptyPreferences())
        override val data: Flow<Preferences> = stateFlow

        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
            return transform(stateFlow.value).also { stateFlow.value = it }
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        historyRepository = HistoryRepository(
            testFile = File(tempFolder.root, "history.json")
        )
        settingsRepository = SettingsRepository(fakeDataStore)
        viewModel = HistoryScreenViewModel(historyRepository, settingsRepository)
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun importingHistoryDoesNotIncrementLifetimeStats() = runTest {
        settingsRepository.recordClean(trackersBlockedCount = 2)
        val exportedHistory = Json.encodeToString(
            listOf(
                HistoryEntry(
                    id = "restored-entry",
                    originalUrl = "https://example.com/?utm_source=test",
                    cleanedUrl = "https://example.com/",
                    removedParamsCount = 1,
                    domain = "example.com"
                )
            )
        )
        val importedCount = CompletableDeferred<Int>()

        viewModel.importHistory(exportedHistory) { importedCount.complete(it) }

        assertEquals(1, importedCount.await())
        assertEquals(1, settingsRepository.totalCleanedCount.first())
        assertEquals(2, settingsRepository.totalTrackersBlocked.first())
    }
}
