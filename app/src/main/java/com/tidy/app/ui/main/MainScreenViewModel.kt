package com.tidy.app.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tidy.app.TidyApp
import com.tidy.app.data.HistoryEntry
import com.tidy.app.data.HistoryRepository
import com.tidy.app.data.SettingsRepository
import com.tidy.app.data.ShareAutomationOutcome
import com.tidy.app.data.UrlCleaner
import com.tidy.app.data.UrlDetection
import com.tidy.app.data.UrlExpander
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainScreenViewModel(
    private val settingsRepository: SettingsRepository = TidyApp.instance.settingsRepository,
    private val historyRepository: HistoryRepository = TidyApp.instance.historyRepository,
    private val urlCleaner: UrlCleaner = UrlCleaner()
) : ViewModel() {

    sealed interface AutomationAction {
        data class Copy(val cleanedUrl: String) : AutomationAction
        data class CopyAndShare(val cleanedUrl: String) : AutomationAction
        data class CopyAndClose(val cleanedUrl: String) : AutomationAction
    }

    private val _automationEvents = MutableSharedFlow<AutomationAction>(extraBufferCapacity = 1)
    val automationEvents: SharedFlow<AutomationAction> = _automationEvents.asSharedFlow()

    data class UiState(
        val inputUrl: String = "",
        val originalUrl: String = "",
        val expandedUrl: String? = null,
        val cleanedUrl: String = "",
        val removedParams: List<String> = emptyList(),
        val isCleaned: Boolean = false,
        val copySuccess: Boolean = false,
        val totalCleanedCount: Int = 0,
        val totalTrackersBlocked: Int = 0,
        val firstLaunchDone: Boolean = true, // default true to avoid flash
        val isLoading: Boolean = false,
        val isInitialLoading: Boolean = true,
        val canExpand: Boolean = false,
        val isExpanding: Boolean = false,
        val autoExpandShortUrls: Boolean = false,
        val autoRemoveMobileSubdomains: Boolean = true,
        val domainWhitelistedParams: Set<String> = emptySet(),
        val autoCleanOnInput: Boolean = true
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.totalCleanedCount,
                settingsRepository.totalTrackersBlocked,
                settingsRepository.firstLaunchDone,
                settingsRepository.autoExpandShortUrls,
                settingsRepository.autoRemoveMobileSubdomains,
                settingsRepository.domainWhitelistedParams,
                settingsRepository.autoCleanOnInput
            ) { array ->
                UiStateUpdates(
                    count = array[0] as Int,
                    blocked = array[1] as Int,
                    done = array[2] as Boolean,
                    autoExpand = array[3] as Boolean,
                    autoRemoveMobile = array[4] as Boolean,
                    domainParams = @Suppress("UNCHECKED_CAST") (array[5] as Set<String>),
                    autoCleanInput = array[6] as Boolean
                )
            }.collect { updates ->
                _uiState.update {
                    it.copy(
                        totalCleanedCount = updates.count,
                        totalTrackersBlocked = updates.blocked,
                        firstLaunchDone = updates.done,
                        autoExpandShortUrls = updates.autoExpand,
                        autoRemoveMobileSubdomains = updates.autoRemoveMobile,
                        domainWhitelistedParams = updates.domainParams,
                        autoCleanOnInput = updates.autoCleanInput,
                        isInitialLoading = false
                    )
                }
            }
        }
    }

    private data class UiStateUpdates(
        val count: Int,
        val blocked: Int,
        val done: Boolean,
        val autoExpand: Boolean,
        val autoRemoveMobile: Boolean,
        val domainParams: Set<String>,
        val autoCleanInput: Boolean
    )

    fun markFirstLaunchDone() {
        viewModelScope.launch {
            settingsRepository.setFirstLaunchDone()
        }
    }

    fun onUrlInput(url: String) {
        _uiState.update { it.copy(inputUrl = url) }
    }

    fun cleanUrl(
        url: String,
        isShared: Boolean = false,
        originalShortUrl: String? = null,
        addToHistory: Boolean = true,
        copyResultToClipboard: Boolean = false
    ) {
        if (url.isBlank()) return
        val trimmed = UrlDetection.normalize(url)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val isShort = UrlExpander.isShortUrl(trimmed)
                val autoExpand = settingsRepository.autoExpandShortUrls.first()

                val didAutoExpand = isShort && autoExpand
                val resolvedUrl = if (didAutoExpand) {
                    UrlExpander.resolve(trimmed)
                } else {
                    trimmed
                }

                val whitelist = settingsRepository.whitelistedDomains.first()
                val customBlacklist = settingsRepository.blacklistedParams.first()
                val domainParams = settingsRepository.domainWhitelistedParams.first()
                val autoRemoveMobile = settingsRepository.autoRemoveMobileSubdomains.first()
                val trackerNames = settingsRepository.trackers.first().map { it.name }.toSet()
                val result = urlCleaner.clean(
                    urlStr = resolvedUrl,
                    whitelistedDomains = whitelist,
                    customBlacklistParams = customBlacklist,
                    domainWhitelistedParams = domainParams,
                    removeMobileSubdomains = autoRemoveMobile,
                    trackingParams = trackerNames
                )

                val initialOriginal = originalShortUrl ?: trimmed
                val expandedVal =
                    if (originalShortUrl != null || didAutoExpand) resolvedUrl else null

                _uiState.update {
                    it.copy(
                        inputUrl = trimmed,
                        originalUrl = initialOriginal,
                        expandedUrl = expandedVal,
                        cleanedUrl = result.cleanedUrl,
                        removedParams = result.removedParams,
                        isCleaned = true,
                        canExpand = (originalShortUrl == null) && !didAutoExpand
                    )
                }

                if (addToHistory) {
                    // Increment local analytics counters
                    settingsRepository.incrementAnalytics(result.removedParams.size)

                    // Add to history
                    val domain = HistoryRepository.extractDomain(resolvedUrl)
                    historyRepository.addEntry(
                        HistoryEntry(
                            originalUrl = initialOriginal,
                            cleanedUrl = result.cleanedUrl,
                            removedParamsCount = result.removedParams.size,
                            domain = domain
                        )
                    )
                }

                if (copyResultToClipboard) {
                    _automationEvents.tryEmit(AutomationAction.Copy(result.cleanedUrl))
                }

                if (isShared) {
                    // The plus module decides the tiered outcome; the free baseline
                    // (just show the result in-app) emits nothing.
                    com.tidy.app.FlavorConfig.handleShareAutomation(
                        settingsRepository
                    ) { outcome ->
                        _automationEvents.tryEmit(
                            when (outcome) {
                                ShareAutomationOutcome.COPY ->
                                    AutomationAction.Copy(result.cleanedUrl)
                                ShareAutomationOutcome.COPY_AND_SHARE ->
                                    AutomationAction.CopyAndShare(result.cleanedUrl)
                                ShareAutomationOutcome.COPY_AND_CLOSE ->
                                    AutomationAction.CopyAndClose(result.cleanedUrl)
                            }
                        )
                    }
                }
            } catch (_: Throwable) {
                // Swallowed: isLoading reset below leaves the UI in its pre-clean state.
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * The shared skip rule for clipboard suggestions, applied once for every tier:
     * returns the detected link when a suggestion (or auto-clean) should happen, or
     * null when nothing should happen at all — the clipboard doesn't look like a URL,
     * cleaning it wouldn't change what's already on the clipboard, or the link is
     * already on display.
     */
    suspend fun evaluateClipboardCandidate(clipText: String): String? {
        val trimmedClip = clipText.trim()
        val detected = UrlDetection.findFirstUrl(trimmedClip) ?: return null
        val normalized = UrlDetection.normalize(detected)

        val state = _uiState.value
        if (state.inputUrl.isNotBlank() && normalized == UrlDetection.normalize(state.inputUrl)) {
            return null
        }
        if (normalized == state.originalUrl || normalized == state.cleanedUrl ||
            normalized == state.expandedUrl
        ) {
            return null
        }

        val whitelist = settingsRepository.whitelistedDomains.first()
        val customBlacklist = settingsRepository.blacklistedParams.first()
        val domainParams = settingsRepository.domainWhitelistedParams.first()
        val removeMobile = settingsRepository.autoRemoveMobileSubdomains.first()
        val trackerNames = settingsRepository.trackers.first().map { it.name }.toSet()
        val result = urlCleaner.clean(
            urlStr = normalized,
            whitelistedDomains = whitelist,
            customBlacklistParams = customBlacklist,
            domainWhitelistedParams = domainParams,
            removeMobileSubdomains = removeMobile,
            trackingParams = trackerNames
        )

        // Cleaning would leave the clipboard exactly as it is: nothing to do — unless
        // it's a short link that auto-expansion would still unwrap.
        if (result.cleanedUrl == trimmedClip) {
            val expandable =
                UrlExpander.isShortUrl(normalized) && settingsRepository.autoExpandShortUrls.first()
            if (!expandable) return null
        }
        return detected
    }

    fun expandShortUrl() {
        val original = _uiState.value.originalUrl
        if (original.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isExpanding = true) }
            try {
                val resolvedUrl = UrlExpander.resolve(original)
                cleanUrl(resolvedUrl, originalShortUrl = original)
            } catch (_: Throwable) {
                // Swallowed: isExpanding reset below leaves the UI in its pre-expand state.
            } finally {
                _uiState.update { it.copy(isExpanding = false) }
            }
        }
    }

    fun addDomainToWhitelist(domain: String) {
        val cleanDomain = domain.trim().lowercase()
        if (cleanDomain.isNotEmpty()) {
            viewModelScope.launch {
                settingsRepository.addWhitelistedDomain(cleanDomain)
                val original = _uiState.value.originalUrl
                val expanded = _uiState.value.expandedUrl
                if (original.isNotEmpty()) {
                    if (expanded != null) {
                        cleanUrl(expanded, originalShortUrl = original, addToHistory = false)
                    } else {
                        cleanUrl(original, addToHistory = false)
                    }
                }
            }
        }
    }

    fun addDomainWhitelistedParam(domain: String, param: String) {
        viewModelScope.launch {
            settingsRepository.addDomainWhitelistedParam(domain, param)
            val original = _uiState.value.originalUrl
            val expanded = _uiState.value.expandedUrl
            if (original.isNotEmpty()) {
                if (expanded != null) {
                    cleanUrl(expanded, originalShortUrl = original, addToHistory = false)
                } else {
                    cleanUrl(original, addToHistory = false)
                }
            }
        }
    }

    fun clear() {
        _uiState.update {
            it.copy(
                inputUrl = "",
                originalUrl = "",
                expandedUrl = null,
                cleanedUrl = "",
                removedParams = emptyList(),
                isCleaned = false,
                copySuccess = false,
                isLoading = false,
                canExpand = false,
                isExpanding = false
            )
        }
    }

    fun copyToClipboard(context: Context) {
        val cleaned = _uiState.value.cleanedUrl
        if (cleaned.isNotEmpty()) {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(
                context.getString(com.tidy.app.R.string.main_cleaned_url),
                cleaned
            )
            clipboard.setPrimaryClip(clip)
            _uiState.update { it.copy(copySuccess = true) }
            viewModelScope.launch {
                kotlinx.coroutines.delay(2.seconds)
                _uiState.update { it.copy(copySuccess = false) }
            }
        }
    }

    fun shareUrl(context: Context) {
        val cleaned = _uiState.value.cleanedUrl
        if (cleaned.isNotEmpty()) {
            val sendIntent: Intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, cleaned)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(shareIntent)
        }
    }
}
