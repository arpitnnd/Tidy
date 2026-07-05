package com.example.urlcleanapp.ui.history

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.urlcleanapp.UrlCleanApp
import com.example.urlcleanapp.data.HistoryEntry
import com.example.urlcleanapp.data.HistoryRepository
import com.example.urlcleanapp.data.SettingsRepository
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
    private val historyRepository: HistoryRepository = UrlCleanApp.instance.historyRepository,
    private val settingsRepository: SettingsRepository = UrlCleanApp.instance.settingsRepository
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

    fun importHistory(context: Context, json: String) {
        viewModelScope.launch {
            val count = historyRepository.importFromJson(json)
            if (count >= 0) {
                Toast.makeText(context, "Imported $count new entries", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Import failed: invalid file format", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
