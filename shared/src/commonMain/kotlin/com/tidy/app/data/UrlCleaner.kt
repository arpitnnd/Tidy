package com.tidy.app.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class TrackerEntry(
    val name: String,
    val description: String,
    // Empty means global (stripped from every domain, the default for most entries). Non-empty
    // scopes this entry to only the listed domains (and their subdomains) -- for a param name
    // that's too generic to strip safely everywhere, e.g. "ref" on Amazon's own domains.
    val domains: List<String> = emptyList()
)

class UrlCleaner {
    companion object {
        // Derived from the same build-generated constant as SettingsRepository.DEFAULT_BLOCKLIST_JSON,
        // which in turn is generated from blocklist/trackers.json — the single authored source of truth.
        val DEFAULT_TRACKERS: List<TrackerEntry> by lazy {
            Json.decodeFromString<List<TrackerEntry>>(GENERATED_DEFAULT_BLOCKLIST_JSON)
        }
    }

    data class CleanResult(
        val originalUrl: String,
        val cleanedUrl: String,
        val removedParams: List<String>
    )

    /**
     * True when [urlStr]'s host (or a parent of it) is in [whitelistedDomains] -- the same
     * check [clean] applies internally before doing anything else to a whitelisted URL.
     * Exposed so callers can skip whitelisted domains *before* work clean() itself doesn't
     * do, e.g. an outbound short-link-expansion network request: a "skip entirely" domain
     * should never trigger that fetch in the first place, not just have its result ignored.
     */
    fun isDomainWhitelisted(urlStr: String, whitelistedDomains: Set<String>): Boolean {
        val host = extractHost(urlStr.trim())
        return whitelistedDomains.any { domain ->
            val cleanDomain = domain.trim().lowercase()
            if (cleanDomain.isEmpty()) false
            else host.equals(cleanDomain, ignoreCase = true) || host.endsWith(
                ".$cleanDomain",
                ignoreCase = true
            )
        }
    }

    fun clean(
        urlStr: String,
        whitelistedDomains: Set<String> = emptySet(),
        customBlacklistParams: Set<String> = emptySet(),
        domainWhitelistedParams: Set<String> = emptySet(),
        removeMobileSubdomains: Boolean = false,
        trackers: List<TrackerEntry> = DEFAULT_TRACKERS
    ): CleanResult {
        var trimmed = urlStr.trim()
        if (trimmed.isEmpty()) {
            return CleanResult(urlStr, urlStr, emptyList())
        }

        // Checked against the untouched input's host, before removeMobileSubdomains gets a
        // chance to rewrite it -- a domain the user whitelisted to "skip entirely" must stay
        // completely untouched, including its host, not just keep its query params.
        if (isDomainWhitelisted(trimmed, whitelistedDomains)) {
            return CleanResult(trimmed, trimmed, emptyList())
        }

        if (removeMobileSubdomains) {
            trimmed = removeMobileSubdomain(trimmed)
        }

        val host = extractHost(trimmed)

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

            // The utm_ prefix is stripped unconditionally, independent of the trackers
            // list: that list is user-editable and remote-syncable (see BlocklistSyncer),
            // so the app's single most important rule shouldn't be silently disabled by a
            // remote payload or a local edit that happens to omit a utm_* entry. A domain-
            // scoped isParamWhitelisted entry (e.g. "utm_source" kept on one specific site)
            // still overrides this floor below, same as it overrides the trackers list.
            val isDefaultTracking = keyLower.startsWith("utm_") || trackers.any { entry ->
                matchParamPattern(keyLower, entry.name) && (
                    entry.domains.isEmpty() || entry.domains.any { d ->
                        matchDomainPattern(host, d)
                    }
                )
            }
            val isCustomBlacklisted =
                customBlacklistParams.any { matchParamPattern(keyLower, it) }

            val isParamWhitelisted = domainWhitelistedParams.any { entry ->
                val entryParts = entry.split(':', limit = 2)
                if (entryParts.size == 2) {
                    val cleanDomain = entryParts[0].trim().lowercase()
                    val cleanParam = entryParts[1].trim().lowercase()
                    matchParamPattern(keyLower, cleanParam) && matchDomainPattern(host, cleanDomain)
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

    private fun matchParamPattern(key: String, pattern: String): Boolean {
        val cleanPattern = pattern.trim().lowercase()
        if (cleanPattern.isEmpty()) return false
        if (cleanPattern.contains('*')) {
            val parts = cleanPattern.split('*').map { Regex.escape(it) }
            val regex = Regex("^" + parts.joinToString(".*") + "$")
            return key.matches(regex)
        }
        return key.equals(cleanPattern, ignoreCase = true)
    }

    private fun matchDomainPattern(host: String, domainPattern: String): Boolean {
        val cleanDomain = domainPattern.trim().lowercase()
        if (cleanDomain.isEmpty()) return false
        if (cleanDomain.contains('*')) {
            val parts = cleanDomain.split('*').map { Regex.escape(it) }
            val regex = Regex("^" + parts.joinToString("[^.]+") + "$")
            val parentRegex = Regex(".*\\." + parts.joinToString("[^.]+") + "$")
            return host.matches(regex) || host.matches(parentRegex)
        }
        return host == cleanDomain || host.endsWith(".$cleanDomain")
    }
}
