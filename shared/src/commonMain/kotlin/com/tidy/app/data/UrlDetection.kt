package com.tidy.app.data

/**
 * The single "looks like a URL" detector shared by every feature that needs one:
 * clipboard checking, share-received handling, the Quick Settings tile, and manual input.
 *
 * Deliberately loose rather than strict scheme/RFC validation: bare domains without a
 * scheme are accepted, and false positives are preferred over missing real links.
 */
object UrlDetection {
    private val tokens = Regex("\\S+")

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
        // '@' only disqualifies a schemeless candidate in the authority position (e.g. an
        // email address's local@domain) - a path segment like "medium.com/@user" is a
        // legitimate bare-domain URL and must still be recognised.
        if (hostPart.contains('@')) return false
        val host = hostPart.substringBefore('?').substringBefore('#')
        val lastDot = host.lastIndexOf('.')
        if (lastDot == -1 || lastDot == host.length - 1) return false
        val tld = host.substring(lastDot + 1)
        return tld.length >= 2 && tld.all { it.isLetter() }
    }

    /** Extracts every URL-looking token from free-form [text], in order. */
    fun findAllUrls(text: String): List<String> = findUrlMatches(text).map { it.url }.toList()

    private data class UrlMatch(val url: String, val start: Int)

    private fun findUrlMatches(text: String): Sequence<UrlMatch> {
        return tokens.findAll(text)
            .map { match ->
                val token = match.value.trimStart('(', '[', '<', '"', '\'')
                val start = match.range.first + match.value.length - token.length
                var parentheses = 0
                var brackets = 0
                for (character in token) {
                    when (character) {
                        '(' -> parentheses++
                        ')' -> parentheses--
                        '[' -> brackets++
                        ']' -> brackets--
                    }
                }
                // Walk mixed wrappers once, retaining balanced brackets inside the URL.
                var end = token.length
                while (end > 0) {
                    when (token[end - 1]) {
                        '.', ',', ';', '!', '>', '"', '\'' -> end--
                        ')' -> if (parentheses < 0) {
                            parentheses++
                            end--
                        } else break

                        ']' -> if (brackets < 0) {
                            brackets++
                            end--
                        } else break

                        else -> break
                    }
                }
                UrlMatch(token.substring(0, end), start)
            }
            .filter { it.url.isNotEmpty() && looksLikeUrl(it.url) }
    }

    /** Extracts the first URL-looking token from free-form [text], or null. */
    fun findFirstUrl(text: String?): String? {
        if (text == null) return null
        return findUrlMatches(text).firstOrNull()?.url
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

    /**
     * Replaces the first detected URL equal to [oldUrl], preserving surrounding text.
     * Uses the detected position so matching text inside an email or another URL stays unchanged.
     * Returns [newUrl] alone if no detected URL matches.
     */
    fun spliceUrl(text: String, oldUrl: String, newUrl: String): String {
        val index = findUrlMatches(text).firstOrNull { it.url == oldUrl }?.start ?: return newUrl
        return text.substring(0, index) + newUrl + text.substring(index + oldUrl.length)
    }

    /** Transforms each detected URL once, preserving text outside its original range. */
    fun replaceUrls(text: String, transform: (String) -> String): String = buildString {
        var cursor = 0
        for (match in findUrlMatches(text)) {
            append(text, cursor, match.start)
            append(transform(match.url))
            cursor = match.start + match.url.length
        }
        append(text, cursor, text.length)
    }
}
