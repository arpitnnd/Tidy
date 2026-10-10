package com.tidy.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tidy.app.TidyApp
import com.tidy.app.data.HistoryEntry
import com.tidy.app.data.HistoryRepository
import com.tidy.app.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryScreenViewModel(
    private val historyRepository: HistoryRepository = TidyApp.instance.historyRepository,
    private val settingsRepository: SettingsRepository = TidyApp.instance.settingsRepository
) : ViewModel(), HistoryScreenActions {

    data class UiState(
        val history: List<HistoryEntry> = emptyList(),
        val totalCleanedCount: Int = 0,
        val totalTrackersBlocked: Int = 0,
        val showClearConfirmation: Boolean = false,
        val isInitialLoading: Boolean = true,
        val trackerDescriptions: Map<String, String> = emptyMap()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var lastClearedHistory: List<HistoryEntry>? = null

    init {
        viewModelScope.launch {
            historyRepository.loadHistory()
        }
        viewModelScope.launch {
            combine(
                historyRepository.historyFlow,
                settingsRepository.totalCleanedCount,
                settingsRepository.totalTrackersBlocked,
                settingsRepository.trackerDescriptions
            ) { history, count, blocked, descriptions ->
                _uiState.update {
                    it.copy(
                        history = history,
                        totalCleanedCount = count,
                        totalTrackersBlocked = blocked,
                        trackerDescriptions = descriptions,
                        isInitialLoading = false
                    )
                }
            }.collect {}
        }
    }

    override fun showClearConfirmation() {
        _uiState.update { it.copy(showClearConfirmation = true) }
    }

    override fun dismissClearConfirmation() {
        _uiState.update { it.copy(showClearConfirmation = false) }
    }

    override fun clearHistory() {
        lastClearedHistory = _uiState.value.history.takeIf { it.isNotEmpty() }
        viewModelScope.launch {
            historyRepository.clearAll()
            _uiState.update { it.copy(showClearConfirmation = false) }
        }
    }

    override fun undoClearHistory() {
        val backup = lastClearedHistory
        if (backup != null) {
            lastClearedHistory = null
            viewModelScope.launch {
                val jsonStr = kotlinx.serialization.json.Json.encodeToString(backup)
                historyRepository.importFromJson(jsonStr)
            }
        }
    }

    override fun discardClearUndo() {
        lastClearedHistory = null
    }

    override suspend fun getExportJson(): String {
        return historyRepository.exportToJson()
    }

    override fun importHistory(json: String, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val result = historyRepository.importFromJsonWithResult(json)
            if (result != null) onResult(result.newEntriesCount)
            else onResult(-1)
        }
    }

    override fun addDomainWhitelistedParam(domain: String, param: String) {
        viewModelScope.launch {
            settingsRepository.addDomainWhitelistedParam(domain, param)
        }
    }
}
