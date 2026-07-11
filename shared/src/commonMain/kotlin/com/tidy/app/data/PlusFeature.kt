package com.tidy.app.data

enum class PlusFeature(val title: String, val description: String) {
    SHARE_AUTOMATION(
        title = "Share automation",
        description = "Automatically clean, copy, and close when sharing links."
    ),
    BULK_CLEAN(
        title = "Bulk clipboard clean",
        description = "Clean multiple URLs from your clipboard in one tap."
    ),
    EXTRA_THEMES(
        title = "Extra themes",
        description = "Expressive colourways like Forest Green and Ocean Blue."
    ),
    TEXT_SELECTION(
        title = "Inline text cleaning",
        description = "Clean URLs directly from any app's text-selection menu."
    )
}
