package com.tidy.app.ui.main

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tidy.app.FlavorConfig
import com.tidy.app.TidyApp
import com.tidy.app.data.ClipboardCleanTier
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MainScreenViewModel(
    private val settingsRepository: SettingsRepository = TidyApp.instance.settingsRepository,
    private val historyRepository: HistoryRepository = TidyApp.instance.historyRepository,
    private val urlCleaner: UrlCleaner = UrlCleaner()
) : ViewModel() {

    sealed interface AutomationAction {
        // sourceClipText, when non-null, is the full clipboard text an automatic clean
        // ran against -- used to splice cleanedUrl back into it rather than overwriting
        // the whole clipboard and losing any text the user had around the link. Null for
        // paths that don't originate from the clipboard (a share intent, the bulk-clean
        // card, manual input) or from an explicit user tap, where replacing the clipboard
        // with just the cleaned URL is the expected, requested outcome.
        data class Copy(val cleanedUrl: String, val sourceClipText: String? = null) :
            AutomationAction

        data class CopyAndShare(val cleanedUrl: String, val sourceClipText: String? = null) :
            AutomationAction

        data class CopyAndClose(val cleanedUrl: String, val sourceClipText: String? = null) :
            AutomationAction
    }

    private val _automationEvents = MutableSharedFlow<AutomationAction>(extraBufferCapacity = 1)
    val automationEvents: SharedFlow<AutomationAction> = _automationEvents.asSharedFlow()

    // The raw clipboard text AUTO_CLEAN last acted on -- see checkClipboardText's
    // AUTO_CLEAN branch. Deliberately not part of UiState/not reset by clear(): it tracks
    // what's already been auto-cleaned, independent of what's currently displayed.
    private var lastAutoCleanedClipText: String? = null

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
        val autoCleanOnInput: Boolean = true,
        // Clipboard suggestion state, driven by checkClipboardText(). Living here (rather
        // than as remember/rememberSaveable in MainScreen) means it survives Settings/
        // History disposing and recomposing the screen -- see checkClipboardText's KDoc.
        val clipboardSuggestionUrl: String? = null,
        val bulkClipboardUrls: List<String>? = null,
        val suggestionCopiesOnClean: Boolean = false
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
        copyResultToClipboard: Boolean = false,
        sourceClipText: String? = null
    ) {
        if (url.isBlank()) return
        val trimmed = UrlDetection.normalize(url)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Read before short-link expansion, not after: a domain whitelisted to
                // "skip entirely" must never trigger the outbound expansion request at
                // all, not just have the (already-made) request's result discarded.
                val whitelist = settingsRepository.whitelistedDomains.first()
                val isWhitelisted = urlCleaner.isDomainWhitelisted(trimmed, whitelist)

                val isShort = !isWhitelisted && UrlExpander.isShortUrl(trimmed)
                val autoExpand = settingsRepository.autoExpandShortUrls.first()

                val didAutoExpand = isShort && autoExpand
                val resolvedUrl = if (didAutoExpand) {
                    UrlExpander.resolve(trimmed)
                } else {
                    trimmed
                }

                val customBlacklist = settingsRepository.blacklistedParams.first()
                val domainParams = settingsRepository.domainWhitelistedParams.first()
                val autoRemoveMobile = settingsRepository.autoRemoveMobileSubdomains.first()
                val dropTrailingSlash = settingsRepository.dropTrailingSlash.first()
                val trackers = settingsRepository.trackers.first()
                val result = urlCleaner.clean(
                    urlStr = resolvedUrl,
                    whitelistedDomains = whitelist,
                    customBlacklistParams = customBlacklist,
                    domainWhitelistedParams = domainParams,
                    removeMobileSubdomains = autoRemoveMobile,
                    dropTrailingSlash = dropTrailingSlash,
                    trackers = trackers
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
                    _automationEvents.tryEmit(
                        AutomationAction.Copy(
                            result.cleanedUrl,
                            sourceClipText
                        )
                    )
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
     * returns the detected URL when it isn't already on display, or null when it is
     * (matching the current input, original, cleaned, or expanded URL) or the clipboard
     * doesn't look like a URL at all. Any URL on the clipboard that isn't currently on
     * display is suggested -- including one cleaning wouldn't change -- so the suggestion
     * is predictable: it always reflects what's actually on the clipboard right now.
     */
    suspend fun evaluateClipboardCandidate(clipText: String): String? {
        val trimmedClip = clipText.trim()
        val detected = UrlDetection.findFirstUrl(trimmedClip) ?: return null
        val normalizedCandidate = UrlDetection.normalize(detected)

        val state = _uiState.value
        val normInput =
            if (state.inputUrl.isNotBlank()) UrlDetection.normalize(state.inputUrl) else null
        val normOriginal =
            if (state.originalUrl.isNotBlank()) UrlDetection.normalize(state.originalUrl) else null
        val normCleaned =
            if (state.cleanedUrl.isNotBlank()) UrlDetection.normalize(state.cleanedUrl) else null
        val normExpanded =
            state.expandedUrl?.let { if (it.isNotBlank()) UrlDetection.normalize(it) else null }

        if (normalizedCandidate == normInput || normalizedCandidate == normOriginal ||
            normalizedCandidate == normCleaned || normalizedCandidate == normExpanded
        ) {
            return null
        }
        return detected
    }

    /**
     * The single clipboard-check entry point, called on every ON_RESUME (cold start, and
     * every return from Settings/History -- see MainScreen.kt's DisposableEffect). Owning
     * this state here rather than in MainScreen's own remember blocks means it survives
     * that screen being disposed and recomposed on every such round trip, instead of
     * resetting to nothing and needing a lifecycle-level guess about whether the resume
     * was "real" -- see this function's own history for why that guess doesn't work.
     */
    suspend fun checkClipboardText(text: String?) {
        if (!settingsRepository.checkClipboardForLinks.first() || text == null) {
            _uiState.update { it.copy(clipboardSuggestionUrl = null, bulkClipboardUrls = null) }
            return
        }
        val urls = UrlDetection.findAllUrls(text)
        when {
            urls.size > 1 -> {
                _uiState.update { it.copy(bulkClipboardUrls = urls, clipboardSuggestionUrl = null) }
            }

            urls.size == 1 -> {
                val candidate = evaluateClipboardCandidate(text)
                if (candidate == null) {
                    _uiState.update {
                        it.copy(
                            clipboardSuggestionUrl = null,
                            bulkClipboardUrls = null
                        )
                    }
                } else {
                    when (FlavorConfig.resolveClipboardTier(settingsRepository)) {
                        ClipboardCleanTier.AUTO_CLEAN -> {
                            _uiState.update {
                                it.copy(
                                    clipboardSuggestionUrl = null,
                                    bulkClipboardUrls = null
                                )
                            }
                            // Fires once per distinct clipboard change, not once per check.
                            // Without this, clearing the displayed result (which wipes the
                            // state evaluateClipboardCandidate compares against, same as
                            // SUGGEST intentionally re-showing the banner) would make
                            // automatic cleaning re-fire on the exact content just cleared
                            // -- silently rewriting the clipboard and adding a duplicate
                            // history entry every time. lastAutoCleanedClipText is keyed on
                            // the raw clip text (not just the extracted URL) and, unlike
                            // the display state, is deliberately never reset by clear().
                            if (text != lastAutoCleanedClipText) {
                                lastAutoCleanedClipText = text
                                cleanUrl(
                                    candidate,
                                    copyResultToClipboard = true,
                                    sourceClipText = text
                                )
                            }
                        }

                        ClipboardCleanTier.SUGGEST_AND_COPY -> {
                            _uiState.update {
                                it.copy(
                                    suggestionCopiesOnClean = true,
                                    clipboardSuggestionUrl = candidate,
                                    bulkClipboardUrls = null
                                )
                            }
                        }

                        ClipboardCleanTier.SUGGEST -> {
                            _uiState.update {
                                it.copy(
                                    suggestionCopiesOnClean = false,
                                    clipboardSuggestionUrl = candidate,
                                    bulkClipboardUrls = null
                                )
                            }
                        }
                    }
                }
            }

            else -> _uiState.update {
                it.copy(
                    clipboardSuggestionUrl = null,
                    bulkClipboardUrls = null
                )
            }
        }
    }

    fun clearClipboardSuggestion() {
        _uiState.update { it.copy(clipboardSuggestionUrl = null, bulkClipboardUrls = null) }
    }

    fun expandShortUrl() {
        val original = _uiState.value.originalUrl
        if (original.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isExpanding = true) }
            try {
                val resolvedUrl = UrlExpander.resolve(original)
                // addToHistory = false: the short link's own clean already added an entry
                // and incremented analytics when it was first displayed; expanding it
                // further must update that same result, not add a second row for one link.
                cleanUrl(resolvedUrl, originalShortUrl = original, addToHistory = false)
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
