package com.tidy.app.data

fun Map<String, String>.trackerDescriptionFor(param: String): String? {
    val key = param.trim().lowercase()
    if (key.isEmpty()) return null

    // A specific explanation takes precedence over a matching family rule.
    return entries.firstOrNull { it.key.trim().equals(key, ignoreCase = true) }?.value
        ?: entries.firstOrNull { matchParamPattern(key, it.key) }?.value
}
