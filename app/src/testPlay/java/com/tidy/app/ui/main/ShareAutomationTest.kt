package com.tidy.app.ui.main

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.viewModelScope
import com.tidy.app.data.EntitlementManager
import com.tidy.app.data.HistoryRepository
import com.tidy.app.data.SettingsRepository
import com.tidy.app.data.ShareCleanTier
import com.tidy.app.data.SharedDependencies
import com.tidy.app.data.UrlDetection
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class ShareAutomationTest {
    @get:Rule
    val folder = TemporaryFolder()

    private class MemoryPreferences : DataStore<Preferences> {
        private val preferences = MutableStateFlow<Preferences>(emptyPreferences())
        override val data: Flow<Preferences> = preferences
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            transform(preferences.value).also { preferences.value = it }
    }

    @Test
    fun everyShareOutcomeRetainsTheMessage() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        val entitlement = object : EntitlementManager {
            override val isPlusUnlocked = MutableStateFlow(true)
            override val isPurchasePending = MutableStateFlow(false)
        }
        val previous = SharedDependencies.entitlementManager
        SharedDependencies.entitlementManager = entitlement
        val settings = SettingsRepository(MemoryPreferences())
        val viewModel = MainScreenViewModel(
            settings,
            HistoryRepository(testFile = File(folder.root, "history.json"))
        )
        try {
            val message = "  Read https://example.com/page?fbclid=private, please.  "
            for ((tier, close, expected) in listOf(
                Triple(
                    ShareCleanTier.CLEAN_AND_COPY,
                    false,
                    MainScreenViewModel.AutomationAction.Copy::class.java
                ),
                Triple(
                    ShareCleanTier.CLEAN_COPY_AND_SHARE,
                    false,
                    MainScreenViewModel.AutomationAction.CopyAndShare::class.java
                ),
                Triple(
                    ShareCleanTier.CLEAN_COPY_AND_SHARE,
                    true,
                    MainScreenViewModel.AutomationAction.CopyAndClose::class.java
                )
            )) {
                settings.setShareCleanTier(tier)
                settings.setCloseInsteadOfSharing(close)
                val event =
                    async(start = CoroutineStart.UNDISPATCHED) { viewModel.automationEvents.first() }
                viewModel.cleanUrl(message, isShared = true)
                val action = event.await()
                assertEquals(expected, action.javaClass)
                val source = when (action) {
                    is MainScreenViewModel.AutomationAction.Copy -> action.sourceClipText
                    is MainScreenViewModel.AutomationAction.CopyAndShare -> action.sourceClipText
                    is MainScreenViewModel.AutomationAction.CopyAndClose -> action.sourceClipText
                }
                assertEquals(message, source)
                assertEquals(
                    "  Read https://example.com/page, please.  ",
                    viewModel.uiState.value.outputText
                )
                assertEquals(
                    viewModel.uiState.value.outputText,
                    UrlDetection.spliceUrl(
                        source!!,
                        UrlDetection.findFirstUrl(source)!!,
                        viewModel.uiState.value.cleanedUrl
                    )
                )
            }
            val events = mutableListOf<MainScreenViewModel.AutomationAction>()
            val collector = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                viewModel.automationEvents.collect { events.add(it) }
            }
            settings.setShareCleanTier(ShareCleanTier.CLEAN)
            viewModel.cleanUrl(message, isShared = true, addToHistory = false)
            viewModel.uiState.first { !it.isLoading }
            assertTrue(events.isEmpty())
            settings.setShareCleanTier(ShareCleanTier.CLEAN_COPY_AND_SHARE)
            entitlement.isPlusUnlocked.value = false
            viewModel.cleanUrl(message, isShared = true, addToHistory = false)
            viewModel.uiState.first { !it.isLoading }
            assertTrue(events.isEmpty())
            entitlement.isPlusUnlocked.value = true
            viewModel.cleanUrl("https://other.org/page", isShared = false, addToHistory = false)
            viewModel.uiState.first { !it.isLoading }
            assertTrue(events.isEmpty())
            collector.cancel()
        } finally {
            viewModel.viewModelScope.coroutineContext[Job]?.cancelAndJoin()
            SharedDependencies.entitlementManager = previous
            Dispatchers.resetMain()
        }
    }
}
