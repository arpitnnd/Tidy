package com.example.urlcleanapp.data

enum class PlusFeature(val title: String, val description: String) {
    SHARE_AUTOMATION(
        title = "Share Automation",
        description = "Automatically clean and copy URLs when shared, and close the app immediately."
    ),
    BULK_CLEAN(
        title = "Bulk Clipboard Cleaning",
        description = "Clean multiple URLs from your clipboard at once with a single tap."
    ),
    EXTRA_THEMES(
        title = "Extra Premium Themes",
        description = "Unlock customized expressive styles like Forest Green, Ocean Blue, and Dark Velvet."
    ),
    TEXT_SELECTION(
        title = "Inline Selection Cleaning",
        description = "Clean text links in place from any third-party app via the text-selection toolbar."
    )
}
