package com.tidy.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.viewModelScope
import com.tidy.app.data.HistoryRepository
import com.tidy.app.data.SettingsRepository
import com.tidy.app.theme.TidyTheme
import com.tidy.app.ui.main.MainScreenActions
import com.tidy.app.ui.main.MainScreenContent
import com.tidy.app.ui.main.MainScreenViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File
import java.util.concurrent.atomic.AtomicInteger

class MainInputDebounceTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private class MemoryPreferences : DataStore<Preferences> {
        private val preferences = MutableStateFlow<Preferences>(emptyPreferences())
        override val data: Flow<Preferences> = preferences
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            transform(preferences.value).also { preferences.value = it }
    }

    @Test
    fun pendingSharedExpansionKeepsItsMessage() {
        val settings = SettingsRepository(MemoryPreferences())
        runBlocking {
            settings.setAutoExpandShortUrls(true)
            settings.setFirstLaunchDone()
        }
        val resolution = CompletableDeferred<String>()
        val requests = AtomicInteger()
        lateinit var viewModel: MainScreenViewModel
        compose.runOnIdle {
            viewModel = MainScreenViewModel(
                settings,
                HistoryRepository(
                    testFile = File(
                        compose.activity.cacheDir,
                        "debounce-history.json"
                    )
                ),
                resolveUrl = { _, _ ->
                    requests.incrementAndGet()
                    resolution.await()
                }
            )
        }
        try {
            compose.setContent {
                val state by viewModel.uiState.collectAsState()
                TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                    MainScreenContent(
                        state = state,
                        sharedText = "Read https://bit.ly/example, please.",
                        actions = viewModel,
                        onSettingsClick = {},
                        onHistoryClick = {}
                    )
                }
            }
            compose.waitUntil(5_000) { requests.get() > 0 }
            // Hold expansion past the manual-input debounce without any user edit.
            compose.waitForIdle()
            compose.mainClock.advanceTimeBy(800)
            compose.runOnIdle { assertEquals(1, requests.get()) }
            resolution.complete("https://example.com/page?fbclid=private")
            compose.waitUntil(5_000) { viewModel.uiState.value.isCleaned }
            compose.runOnIdle {
                assertEquals(
                    "Read https://example.com/page, please.",
                    viewModel.uiState.value.outputText
                )
                assertEquals(1, viewModel.uiState.value.totalCleanedCount)
            }
            compose.mainClock.advanceTimeBy(800)
            compose.runOnIdle { assertEquals(1, requests.get()) }
        } finally {
            compose.runOnIdle { viewModel.viewModelScope.coroutineContext[Job]?.cancel() }
        }
    }

    @Test
    fun completedInputCancelsDebounceAndNewInputStillCleans() {
        val firstUrl = "https://example.com/first"
        val secondUrl = "https://example.com/second"
        val state = mutableStateOf(MainScreenViewModel.UiState(isInitialLoading = false))
        val cleanedInputs = mutableListOf<String>()
        val actions = object : MainScreenActions {
            override fun cleanUrl(
                url: String,
                isShared: Boolean,
                originalShortUrl: String?,
                addToHistory: Boolean,
                copyResultToClipboard: Boolean,
                sourceClipText: String?,
                forceRefresh: Boolean
            ) {
                cleanedInputs.add(url)
                state.value = state.value.copy(originalUrl = url, cleanedUrl = url)
            }
        }
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                MainScreenContent(
                    state = state.value,
                    actions = actions,
                    onSettingsClick = {},
                    onHistoryClick = {}
                )
            }
        }
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { state.value = state.value.copy(inputUrl = firstUrl) }
        compose.mainClock.advanceTimeBy(200)
        // A different clean completes while the input debounce is still pending.
        compose.runOnIdle { state.value = state.value.copy(originalUrl = firstUrl) }
        compose.mainClock.advanceTimeBy(800)
        compose.runOnIdle { assertEquals(emptyList<String>(), cleanedInputs) }

        compose.runOnIdle { state.value = state.value.copy(inputUrl = secondUrl) }
        compose.mainClock.advanceTimeBy(800)
        compose.runOnIdle { assertEquals(listOf(secondUrl), cleanedInputs) }
        compose.mainClock.advanceTimeBy(800)
        compose.runOnIdle { assertEquals(listOf(secondUrl), cleanedInputs) }

        // Clearing a failed programmatic clean must not start an automatic retry.
        compose.runOnIdle {
            state.value = state.value.copy(inputUrl = "https://bit.ly/failed", isLoading = true)
        }
        compose.mainClock.advanceTimeBy(32)
        compose.runOnIdle { state.value = state.value.copy(isLoading = false) }
        compose.mainClock.advanceTimeBy(800)
        compose.runOnIdle { assertEquals(listOf(secondUrl), cleanedInputs) }
    }
}
