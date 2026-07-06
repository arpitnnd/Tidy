package com.tidy.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tidy.app.TidyURLApp
import com.tidy.app.data.HistoryEntry
import com.tidy.app.data.HistoryRepository
import com.tidy.app.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryScreenViewModel(
    private val historyRepository: HistoryRepository = TidyURLApp.instance.historyRepository,
    private val settingsRepository: SettingsRepository = TidyURLApp.instance.settingsRepository
) : ViewModel() {

    data class UiState(
        val history: List<HistoryEntry> = emptyList(),
        val totalCleanedCount: Int = 0,
        val totalTrackersBlocked: Int = 0,
        val showClearConfirmation: Boolean = false,
        val isInitialLoading: Boolean = true
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            historyRepository.loadHistory()
        }
        viewModelScope.launch {
            combine(
                historyRepository.historyFlow,
                settingsRepository.totalCleanedCount,
                settingsRepository.totalTrackersBlocked
            ) { history, count, blocked ->
                Triple(history, count, blocked)
            }.collect { triple ->
                _uiState.update { it.copy(
                    history = triple.first,
                    totalCleanedCount = triple.second,
                    totalTrackersBlocked = triple.third,
                    isInitialLoading = false
                ) }
            }
        }
    }

    fun showClearConfirmation() {
        _uiState.update { it.copy(showClearConfirmation = true) }
    }

    fun dismissClearConfirmation() {
        _uiState.update { it.copy(showClearConfirmation = false) }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clearAll()
            _uiState.update { it.copy(showClearConfirmation = false) }
        }
    }

    suspend fun getExportJson(): String {
        return historyRepository.exportToJson()
    }

    fun importHistory(json: String, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val count = historyRepository.importFromJson(json)
            onResult(count)
        }
    }
}
