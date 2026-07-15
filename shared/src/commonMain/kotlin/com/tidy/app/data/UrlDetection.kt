package com.tidy.app.data

/**
 * The single "looks like a URL" detector shared by every feature that needs one:
 * clipboard checking, share-received handling, the Quick Settings tile, and manual input.
 *
 * Deliberately loose rather than strict scheme/RFC validation: bare domains without a
 * scheme are accepted, and false positives are preferred over missing real links.
 */
object UrlDetection {

    /** Returns true if [text] plausibly is (or contains, as a single token) a link. */
    fun looksLikeUrl(text: String): Boolean {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return false
        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            return trimmed.length > 8
        }
        if (trimmed.contains(' ') || trimmed.contains('\n')) return false
        if (!trimmed.contains('.')) return false
        val firstSlash = trimmed.indexOf('/')
        val hostPart = if (firstSlash != -1) trimmed.substring(0, firstSlash) else trimmed
        val host = hostPart.substringBefore('?').substringBefore('#')
        val lastDot = host.lastIndexOf('.')
        if (lastDot == -1 || lastDot == host.length - 1) return false
        val tld = host.substring(lastDot + 1)
        return tld.length >= 2 && tld.all { it.isLetter() }
    }

    /** Extracts every URL-looking token from free-form [text], in order. */
    fun findAllUrls(text: String): List<String> {
        return text.split(' ', '\n', '\t', '\r')
            .map { it.trim().trimEnd(',', ';', ')', ']', '>', '"', '\'') }
            .filter { it.isNotEmpty() && looksLikeUrl(it) }
    }

    /** Extracts the first URL-looking token from free-form [text], or null. */
    fun findFirstUrl(text: String?): String? {
        if (text == null) return null
        return findAllUrls(text).firstOrNull()
    }

    /** Prefixes https:// when [url] has no scheme, so it can be cleaned or opened. */
    fun normalize(url: String): String {
        val trimmed = url.trim()
        return if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true)
        ) {
            trimmed
        } else {
            "https://$trimmed"
        }
    }
}
