package com.tidy.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.tidy.app.data.HistoryEntry
import com.tidy.app.data.HistoryRepository
import com.tidy.app.theme.TidyTheme
import com.tidy.app.ui.history.HistoryScreenActions
import com.tidy.app.ui.history.HistoryScreenContent
import com.tidy.app.ui.history.HistoryScreenViewModel
import com.tidy.app.ui.main.MainScreenContent
import com.tidy.app.ui.main.MainScreenViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File

class HistoryStorageFeedbackTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun clearWaitsForStorageAndReportsFailureWithoutUndo() {
        val state = mutableStateOf(
            HistoryScreenViewModel.UiState(
                showClearConfirmation = true,
                isInitialLoading = false
            )
        )
        var finish: ((Boolean) -> Unit)? = null
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                HistoryScreenContent(
                    state.value,
                    onBackClick = {},
                    actions = object : HistoryScreenActions {
                        override fun clearHistory(onResult: (Boolean) -> Unit) {
                            finish = onResult
                        }
                    })
            }
        }
        compose.onNodeWithText("Clear all").performClick()
        compose.onNodeWithText("History cleared").assertDoesNotExist()
        compose.onNodeWithText("Undo").assertDoesNotExist()
        compose.runOnIdle {
            state.value = state.value.copy(showClearConfirmation = false)
            requireNotNull(finish)(false)
        }
        compose.onNodeWithText("History could not be cleared. Please try again.")
            .assertIsDisplayed()
        compose.onNodeWithText("Undo").assertDoesNotExist()
    }

    @Test
    fun failedUndoReportsThatHistoryWasNotRestored() {
        val state = mutableStateOf(
            HistoryScreenViewModel.UiState(
                showClearConfirmation = true,
                isInitialLoading = false
            )
        )
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                HistoryScreenContent(
                    state.value,
                    onBackClick = {},
                    actions = object : HistoryScreenActions {
                        override fun clearHistory(onResult: (Boolean) -> Unit) {
                            state.value = state.value.copy(showClearConfirmation = false)
                            onResult(true)
                        }

                        override fun undoClearHistory(onResult: (Boolean) -> Unit) {
                            onResult(false)
                        }
                    })
            }
        }
        compose.onNodeWithText("Clear all").performClick()
        compose.onNodeWithText("History cleared").assertIsDisplayed()
        compose.onNodeWithText("Undo").performClick()
        compose.onNodeWithText("History could not be restored.").assertIsDisplayed()
    }

    @Test
    fun cleanedResultShowsWhenHistoryCouldNotBeSaved() {
        compose.setContent {
            TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                MainScreenContent(
                    MainScreenViewModel.UiState(
                        isInitialLoading = false, isCleaned = true,
                        cleanedUrl = "https://example.com/", historySaveFailed = true
                    ),
                    onSettingsClick = {}, onHistoryClick = {}
                )
            }
        }
        compose.onNodeWithText("Link cleaned, but History could not be saved.").assertIsDisplayed()
    }

    @Test
    fun deviceFileReplacementPreservesHistoryAcrossReloads() = runBlocking {
        val file = File.createTempFile("history-storage-", ".json", compose.activity.cacheDir)
        try {
            file.writeText("[]")
            val repository = HistoryRepository(testFile = file)
            val entry = HistoryEntry(
                originalUrl = "https://example.com/", cleanedUrl = "https://example.com/",
                removedParamsCount = 0, domain = "example.com"
            )
            repository.addEntry(entry)
            repository.addEntry(entry.copy(id = "second"))
            assertEquals(2, HistoryRepository(testFile = file).getEntriesSnapshot().size)
            repository.clearAll()
            assertEquals(
                emptyList<HistoryEntry>(),
                HistoryRepository(testFile = file).getEntriesSnapshot()
            )
        } finally {
            file.delete()
            File(file.path + ".new").delete()
        }
    }
}
