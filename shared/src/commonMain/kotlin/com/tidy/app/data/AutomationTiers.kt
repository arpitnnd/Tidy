package com.tidy.app.data

/**
 * What happens when a link is found on the clipboard while opening Tidy.
 * [SUGGEST] is the free baseline; the other tiers are Tidy+ and resolved
 * through the entitlement-gated automation in the plus module.
 */
enum class ClipboardCleanTier(val key: String) {
    SUGGEST("suggest"),
    SUGGEST_AND_COPY("suggest_and_copy"),
    AUTO_CLEAN("auto_clean");

    companion object {
        fun fromKey(key: String?): ClipboardCleanTier =
            entries.firstOrNull { it.key == key } ?: SUGGEST
    }
}

/**
 * What happens when a link is shared to Tidy from another app.
 * [CLEAN] is the free baseline; the other tiers are Tidy+.
 */
enum class ShareCleanTier(val key: String) {
    CLEAN("clean"),
    CLEAN_AND_COPY("clean_and_copy"),
    CLEAN_COPY_AND_SHARE("clean_copy_and_share");

    companion object {
        fun fromKey(key: String?): ShareCleanTier =
            entries.firstOrNull { it.key == key } ?: CLEAN
    }
}

/**
 * The automation side effect the share flow should perform after cleaning,
 * as decided by the plus module. The free baseline (show the result in-app)
 * emits no outcome at all.
 */
enum class ShareAutomationOutcome {
    COPY,
    COPY_AND_SHARE,
    COPY_AND_CLOSE
}
