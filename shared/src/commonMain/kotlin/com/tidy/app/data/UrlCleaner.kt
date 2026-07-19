package com.tidy.app.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TrackerEntry(
    val name: String,
    val description: String
)

class UrlCleaner {
    companion object {
        // Derived from the same build-generated constant as SettingsRepository.DEFAULT_BLOCKLIST_JSON,
        // which in turn is generated from blocklist/trackers.json — the single authored source of truth.
        val DEFAULT_TRACKING_PARAMS: Set<String> by lazy {
            Json.decodeFromString<List<TrackerEntry>>(GENERATED_DEFAULT_BLOCKLIST_JSON)
                .map { it.name }
                .toSet()
        }
    }

    data class CleanResult(
        val originalUrl: String,
        val cleanedUrl: String,
        val removedParams: List<String>
    )

    fun clean(
        urlStr: String,
        whitelistedDomains: Set<String> = emptySet(),
        customBlacklistParams: Set<String> = emptySet(),
        domainWhitelistedParams: Set<String> = emptySet(),
        removeMobileSubdomains: Boolean = false,
        trackingParams: Set<String> = DEFAULT_TRACKING_PARAMS
    ): CleanResult {
        var trimmed = urlStr.trim()
        if (trimmed.isEmpty()) {
            return CleanResult(urlStr, urlStr, emptyList())
        }

        if (removeMobileSubdomains) {
            trimmed = removeMobileSubdomain(trimmed)
        }

        val host = extractHost(trimmed)

        // Check if the domain (or its parent domain) is whitelisted
        val isWhitelisted = whitelistedDomains.any { domain ->
            val cleanDomain = domain.trim().lowercase()
            if (cleanDomain.isEmpty()) false
            else host.equals(cleanDomain, ignoreCase = true) || host.endsWith(
                ".$cleanDomain",
                ignoreCase = true
            )
        }

        if (isWhitelisted) {
            return CleanResult(trimmed, trimmed, emptyList())
        }

        val hashIndex = trimmed.indexOf('#')
        val fragment = if (hashIndex != -1) trimmed.substring(hashIndex) else ""
        val withoutFragment = if (hashIndex != -1) trimmed.substring(0, hashIndex) else trimmed

        val questionIndex = withoutFragment.indexOf('?')
        if (questionIndex == -1) {
            return CleanResult(trimmed, trimmed, emptyList())
        }

        val baseUrl = withoutFragment.substring(0, questionIndex)
        val queryString = withoutFragment.substring(questionIndex + 1)

        if (queryString.isEmpty()) {
            return CleanResult(trimmed, baseUrl + fragment, emptyList())
        }

        val params = queryString.split('&')
        val keptParams = mutableListOf<String>()
        val removedParams = mutableListOf<String>()

        for (param in params) {
            if (param.isEmpty()) continue
            val parts = param.split('=', limit = 2)
            val key = parts[0]
            val keyLower = key.lowercase().trim()

            val isDefaultTracking = trackingParams.contains(keyLower) || keyLower.startsWith("utm_")
            val isCustomBlacklisted =
                customBlacklistParams.any { it.trim().lowercase() == keyLower }

            val isParamWhitelisted = domainWhitelistedParams.any { entry ->
                val entryParts = entry.split(':', limit = 2)
                if (entryParts.size == 2) {
                    val cleanDomain = entryParts[0].trim().lowercase()
                    val cleanParam = entryParts[1].trim().lowercase()
                    cleanParam == keyLower && (host == cleanDomain || host.endsWith(".$cleanDomain"))
                } else false
            }

            if ((isDefaultTracking || isCustomBlacklisted) && !isParamWhitelisted) {
                removedParams.add(key)
            } else {
                keptParams.add(param)
            }
        }

        val newQueryString = if (keptParams.isNotEmpty()) {
            "?" + keptParams.joinToString("&")
        } else {
            ""
        }

        val cleanedUrl = baseUrl + newQueryString + fragment
        return CleanResult(trimmed, cleanedUrl, removedParams)
    }

    private fun removeMobileSubdomain(urlStr: String): String {
        val protoIndex = urlStr.indexOf("://")
        val startIndex = if (protoIndex != -1) protoIndex + 3 else 0

        var slashIndex = urlStr.indexOf('/', startIndex)
        var qIndex = urlStr.indexOf('?', startIndex)
        var hIndex = urlStr.indexOf('#', startIndex)

        val endIndex = listOf(slashIndex, qIndex, hIndex)
            .filter { it != -1 }
            .minOrNull() ?: urlStr.length

        val host = urlStr.substring(startIndex, endIndex)
        val hostLower = host.lowercase()

        val newHost = when {
            hostLower.startsWith("m.") -> host.substring(2)
            hostLower.startsWith("mobile.") -> host.substring(7)
            else -> null
        }

        return if (newHost != null && newHost.contains(".")) {
            urlStr.substring(0, startIndex) + newHost + urlStr.substring(endIndex)
        } else {
            urlStr
        }
    }

    private fun extractHost(url: String): String {
        var temp = url
        val protoIndex = temp.indexOf("://")
        if (protoIndex != -1) {
            temp = temp.substring(protoIndex + 3)
        }
        val slashIndex = temp.indexOf('/')
        if (slashIndex != -1) {
            temp = temp.substring(0, slashIndex)
        }
        val portIndex = temp.indexOf(':')
        if (portIndex != -1) {
            temp = temp.substring(0, portIndex)
        }
        val qIndex = temp.indexOf('?')
        if (qIndex != -1) {
            temp = temp.substring(0, qIndex)
        }
        val hIndex = temp.indexOf('#')
        if (hIndex != -1) {
            temp = temp.substring(0, hIndex)
        }
        return temp.trim().lowercase()
    }
}
