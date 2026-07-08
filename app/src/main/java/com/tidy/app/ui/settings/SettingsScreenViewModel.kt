package com.tidy.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tidy.app.TidyURLApp
import com.tidy.app.data.SettingsRepository
import com.tidy.app.data.TrackerEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsScreenViewModel(
    private val settingsRepository: SettingsRepository = TidyURLApp.instance.settingsRepository
) : ViewModel() {

    data class UiState(
        val whitelistedDomains: Set<String> = emptySet(),
        val blacklistedParams: Set<String> = emptySet(),
        val domainWhitelistedParams: Set<String> = emptySet(),
        val trackers: List<TrackerEntry> = emptyList(),
        val domainInput: String = "",
        val paramInput: String = "",
        val newParamWhitelistDomain: String = "",
        val newParamWhitelistParam: String = "",
        val autoCopyOnShare: Boolean = false,
        val autoCloseOnShare: Boolean = false,
        val autoExpandShortUrls: Boolean = true,
        val autoRemoveMobileSubdomains: Boolean = true,
        val autoCleanClipboardOnLaunch: Boolean = false,
        val autoCleanOnInput: Boolean = true
    )

    private data class BooleanSettings(
        val autoCopy: Boolean,
        val autoClose: Boolean,
        val autoExpand: Boolean,
        val autoRemoveMobile: Boolean,
        val autoCleanLaunch: Boolean,
        val autoCleanInput: Boolean
    )

    private data class RulesData(
        val whitelist: Set<String>,
        val blacklist: Set<String>,
        val domainParams: Set<String>,
        val trackers: List<TrackerEntry>
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = combine(
        combine(
            settingsRepository.whitelistedDomains,
            settingsRepository.blacklistedParams,
            settingsRepository.domainWhitelistedParams,
            settingsRepository.trackers
        ) { whitelist, blacklist, domainParams, trackers ->
            RulesData(whitelist, blacklist, domainParams, trackers)
        },
        combine(
            settingsRepository.autoCopyOnShare,
            settingsRepository.autoCloseOnShare,
            settingsRepository.autoExpandShortUrls,
            settingsRepository.autoRemoveMobileSubdomains,
            settingsRepository.autoCleanClipboardOnLaunch,
            settingsRepository.autoCleanOnInput
        ) { array ->
            BooleanSettings(
                autoCopy = array[0],
                autoClose = array[1],
                autoExpand = array[2],
                autoRemoveMobile = array[3],
                autoCleanLaunch = array[4],
                autoCleanInput = array[5]
            )
        },
        _uiState
    ) { reposFlow1, reposFlow2, state ->
        state.copy(
            whitelistedDomains = reposFlow1.whitelist,
            blacklistedParams = reposFlow1.blacklist,
            domainWhitelistedParams = reposFlow1.domainParams,
            trackers = reposFlow1.trackers,
            autoCopyOnShare = reposFlow2.autoCopy,
            autoCloseOnShare = reposFlow2.autoClose,
            autoExpandShortUrls = reposFlow2.autoExpand,
            autoRemoveMobileSubdomains = reposFlow2.autoRemoveMobile,
            autoCleanClipboardOnLaunch = reposFlow2.autoCleanLaunch,
            autoCleanOnInput = reposFlow2.autoCleanInput
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun setAutoCopyOnShare(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoCopyOnShare(enabled)
        }
    }

    fun setAutoCloseOnShare(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoCloseOnShare(enabled)
        }
    }

    fun onDomainInputChanged(value: String) {
        _uiState.update { it.copy(domainInput = value) }
    }

    fun onParamInputChanged(value: String) {
        _uiState.update { it.copy(paramInput = value) }
    }

    fun addDomain() {
        val domain = _uiState.value.domainInput.trim().lowercase()
        if (domain.isNotEmpty() && domain.contains(".") && !domain.contains(" ")) {
            viewModelScope.launch {
                settingsRepository.addWhitelistedDomain(domain)
                _uiState.update { it.copy(domainInput = "") }
            }
        }
    }

    fun removeDomain(domain: String) {
        viewModelScope.launch {
            settingsRepository.removeWhitelistedDomain(domain)
        }
    }

    fun addParam() {
        val param = _uiState.value.paramInput.trim()
        if (param.isNotEmpty() && !param.contains(" ") && !param.contains("?") && !param.contains("&") && !param.contains(
                "="
            )
        ) {
            viewModelScope.launch {
                settingsRepository.addBlacklistedParam(param)
                _uiState.update { it.copy(paramInput = "") }
            }
        }
    }

    fun removeParam(param: String) {
        viewModelScope.launch {
            settingsRepository.removeBlacklistedParam(param)
        }
    }

    fun setAutoExpandShortUrls(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoExpandShortUrls(enabled)
        }
    }

    fun setAutoRemoveMobileSubdomains(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoRemoveMobileSubdomains(enabled)
        }
    }

    fun setAutoCleanClipboardOnLaunch(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoCleanClipboardOnLaunch(enabled)
        }
    }

    fun setAutoCleanOnInput(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoCleanOnInput(enabled)
        }
    }

    fun onNewParamWhitelistDomainChanged(value: String) {
        _uiState.update { it.copy(newParamWhitelistDomain = value) }
    }

    fun onNewParamWhitelistParamChanged(value: String) {
        _uiState.update { it.copy(newParamWhitelistParam = value) }
    }

    fun addDomainWhitelistedParam() {
        val domain = _uiState.value.newParamWhitelistDomain.trim().lowercase()
        val param = _uiState.value.newParamWhitelistParam.trim().lowercase()
        if (domain.isNotEmpty() && param.isNotEmpty() && domain.contains(".") && !domain.contains(" ") && !param.contains(
                " "
            )
        ) {
            viewModelScope.launch {
                settingsRepository.addDomainWhitelistedParam(domain, param)
                _uiState.update {
                    it.copy(
                        newParamWhitelistDomain = "",
                        newParamWhitelistParam = ""
                    )
                }
            }
        }
    }

    fun removeDomainWhitelistedParam(entry: String) {
        val parts = entry.split(':', limit = 2)
        if (parts.size == 2) {
            viewModelScope.launch {
                settingsRepository.removeDomainWhitelistedParam(parts[0], parts[1])
            }
        }
    }
}
