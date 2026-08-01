package com.tidy.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tidy.app.TidyApp
import com.tidy.app.data.BackupPreference
import com.tidy.app.data.ClipboardCleanTier
import com.tidy.app.data.SettingsRepository
import com.tidy.app.data.ShareCleanTier
import com.tidy.app.data.TrackerEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsScreenViewModel(
    private val settingsRepository: SettingsRepository = TidyApp.instance.settingsRepository
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
        val checkClipboardForLinks: Boolean = true,
        val clipboardCleanTier: ClipboardCleanTier = ClipboardCleanTier.SUGGEST,
        val shareCleanTier: ShareCleanTier = ShareCleanTier.CLEAN,
        val closeInsteadOfSharing: Boolean = false,
        val autoExpandShortUrls: Boolean = true,
        val autoRemoveMobileSubdomains: Boolean = true,
        val dropTrailingSlash: Boolean = true,
        val autoCleanOnInput: Boolean = true,
        val allowSystemBackup: Boolean = false
    )

    private data class AutomationSettings(
        val checkClipboard: Boolean,
        val clipboardTier: ClipboardCleanTier,
        val shareTier: ShareCleanTier,
        val closeInsteadOfSharing: Boolean,
        val autoExpand: Boolean,
        val autoRemoveMobile: Boolean,
        val dropTrailingSlash: Boolean,
        val autoCleanInput: Boolean
    )

    private data class RulesData(
        val whitelist: Set<String>,
        val blacklist: Set<String>,
        val domainParams: Set<String>,
        val trackers: List<TrackerEntry>
    )

    private val _uiState = MutableStateFlow(
        UiState(allowSystemBackup = BackupPreference.isAllowed(TidyApp.instance))
    )
    val uiState: StateFlow<UiState> = combine(
        combine(
            settingsRepository.whitelistedDomains,
            settingsRepository.blacklistedParams,
            settingsRepository.domainWhitelistedParams,
            settingsRepository.trackers
        ) { whitelist, blacklist, domainParams, trackers ->
            RulesData(whitelist, blacklist, domainParams, trackers)
        },
        combine<Any, AutomationSettings>(
            settingsRepository.checkClipboardForLinks,
            settingsRepository.clipboardCleanTier,
            settingsRepository.shareCleanTier,
            settingsRepository.closeInsteadOfSharing,
            settingsRepository.autoExpandShortUrls,
            settingsRepository.autoRemoveMobileSubdomains,
            settingsRepository.dropTrailingSlash,
            settingsRepository.autoCleanOnInput
        ) { array ->
            AutomationSettings(
                checkClipboard = array[0] as Boolean,
                clipboardTier = array[1] as ClipboardCleanTier,
                shareTier = array[2] as ShareCleanTier,
                closeInsteadOfSharing = array[3] as Boolean,
                autoExpand = array[4] as Boolean,
                autoRemoveMobile = array[5] as Boolean,
                dropTrailingSlash = array[6] as Boolean,
                autoCleanInput = array[7] as Boolean
            )
        },
        _uiState
    ) { reposFlow1, reposFlow2, state ->
        state.copy(
            whitelistedDomains = reposFlow1.whitelist,
            blacklistedParams = reposFlow1.blacklist,
            domainWhitelistedParams = reposFlow1.domainParams,
            trackers = reposFlow1.trackers,
            checkClipboardForLinks = reposFlow2.checkClipboard,
            clipboardCleanTier = reposFlow2.clipboardTier,
            shareCleanTier = reposFlow2.shareTier,
            closeInsteadOfSharing = reposFlow2.closeInsteadOfSharing,
            autoExpandShortUrls = reposFlow2.autoExpand,
            autoRemoveMobileSubdomains = reposFlow2.autoRemoveMobile,
            dropTrailingSlash = reposFlow2.dropTrailingSlash,
            autoCleanOnInput = reposFlow2.autoCleanInput
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())

    fun setCheckClipboardForLinks(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCheckClipboardForLinks(enabled)
        }
    }

    fun setClipboardCleanTier(tier: ClipboardCleanTier) {
        viewModelScope.launch {
            settingsRepository.setClipboardCleanTier(tier)
        }
    }

    fun setShareCleanTier(tier: ShareCleanTier) {
        viewModelScope.launch {
            settingsRepository.setShareCleanTier(tier)
        }
    }

    fun setCloseInsteadOfSharing(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCloseInsteadOfSharing(enabled)
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

    fun setDropTrailingSlash(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDropTrailingSlash(enabled)
        }
    }

    fun setAutoCleanOnInput(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoCleanOnInput(enabled)
        }
    }

    fun setAllowSystemBackup(enabled: Boolean) {
        BackupPreference.setAllowed(TidyApp.instance, enabled)
        _uiState.update { it.copy(allowSystemBackup = enabled) }
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
