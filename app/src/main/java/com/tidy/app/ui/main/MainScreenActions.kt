package com.tidy.app.ui.main

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

/** Actions supplied by the runtime host or a deterministic preview. */
interface MainScreenActions {
    val automationEvents: Flow<MainScreenViewModel.AutomationAction>
        get() = emptyFlow()

    fun markFirstLaunchDone() {}
    fun onUrlInput(url: String) {}
    fun cleanUrl(
        url: String,
        isShared: Boolean = false,
        originalShortUrl: String? = null,
        addToHistory: Boolean = true,
        copyResultToClipboard: Boolean = false,
        sourceClipText: String? = null,
        forceRefresh: Boolean = false
    ) {
    }

    suspend fun checkClipboardText(text: String?) {}
    fun clearClipboardSuggestion() {}
    fun expandShortUrl() {}
    fun addDomainWhitelistedParam(domain: String, param: String) {}
    suspend fun clearAndCheckClipboard(text: String?) {}
    fun copyToClipboard(context: Context) {}
    fun shareUrl(context: Context) {}
}
