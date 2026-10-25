package com.tidy.app.ui.main

/** Preference values and operations needed by the Home presentation. */
internal data class MainScreenPreferences(
    val dontAskAgainCrash: Boolean = false,
    val checkClipboardForLinks: Boolean = false,
    val clipboardCalloutDismissed: Boolean = true,
    val isPlusUnlocked: Boolean = false,
    val trackerDescriptions: Map<String, String> = emptyMap(),
    val setDontAskAgainCrash: suspend (Boolean) -> Unit = {},
    val setCheckClipboardForLinks: suspend (Boolean) -> Unit = {},
    val setClipboardCalloutDismissed: suspend (Boolean) -> Unit = {},
    val cleanBulkUrls: suspend (List<String>) -> String = { it.joinToString("\n") }
)
