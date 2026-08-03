package com.tidy.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object UrlExpander {
    private val SHORT_URL_DOMAINS = setOf(
        "bit.ly", "tinyurl.com", "t.co", "rebrand.ly", "shorturl.at",
        "is.gd", "buff.ly", "bit.do", "lnkd.in", "db.tt", "qr.ae",
        "goo.gl", "ow.ly", "tiny.cc", "t.ly", "cutt.ly",
        "share.google", "amzn.*", "a.co", "a.to", "z.cn",
        "v.gd", "rb.gy", "shrtco.de", "we.tl"
    )

    private const val CACHE_MAX_SIZE = 100
    private val cache = java.util.Collections.synchronizedMap(
        object : java.util.LinkedHashMap<String, String>(16, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
                return size > CACHE_MAX_SIZE
            }
        }
    )

    fun isShortUrl(urlStr: String): Boolean {
        val host = extractHost(urlStr)
        return SHORT_URL_DOMAINS.any { pattern ->
            matchDomainPattern(host, pattern)
        }
    }

    private fun matchDomainPattern(host: String, pattern: String): Boolean {
        val cleanPattern = pattern.trim().lowercase()
        if (cleanPattern.isEmpty()) return false
        if (cleanPattern.contains('*')) {
            // A single label, not ".*": "amzn.*" must match "amzn.to"/"amzn.in" but not
            // "amzn.evil.com": letting the wildcard span dots would make it match any
            // host merely starting with the label before it, regardless of registrant.
            val parts = cleanPattern.split('*').map { Regex.escape(it) }
            val regex = Regex("^" + parts.joinToString("[^.]+") + "$")
            val parentRegex = Regex(".*\\." + parts.joinToString("[^.]+") + "$")
            return host.matches(regex) || host.matches(parentRegex)
        }
        return host == cleanPattern || host.endsWith(".$cleanPattern")
    }

    suspend fun resolve(urlStr: String): String = withContext(Dispatchers.IO) {
        var currentUrl = urlStr.trim()
        val cached = cache[currentUrl]
        if (cached != null) {
            return@withContext cached
        }
        if (!currentUrl.startsWith(
                "http://",
                ignoreCase = true
            ) && !currentUrl.startsWith("https://", ignoreCase = true)
        ) {
            currentUrl = "https://$currentUrl"
        }
        val maxHops = 5
        var hop = 0
        val timeoutMs = 3000
        // Only a completed HTTP exchange (a final response, or a redirect this loop
        // deliberately stops following) is trustworthy enough to cache. An exception
        // (timeout, no connection, offline) must not be cached: it isn't "resolved to
        // itself", it's "not resolved at all yet", and caching it would poison this URL
        // for the rest of the process's lifetime the first time it's tried offline.
        var succeeded = false

        while (hop < maxHops) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(currentUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = timeoutMs
                connection.readTimeout = timeoutMs
                connection.instanceFollowRedirects = false

                // Add a modern user agent
                connection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 10) Tidy/1.0"
                )

                val responseCode = connection.responseCode
                if (responseCode in 300..399) {
                    val location = connection.getHeaderField("Location")
                    if (!location.isNullOrEmpty()) {
                        val nextUrl =
                            if (location.startsWith("http://") || location.startsWith("https://")) {
                                location
                            } else {
                                val base = URL(currentUrl)
                                URL(base, location).toString()
                            }
                        // Refuse to follow a redirect to a non-http(s) scheme (e.g.
                        // ftp:// or file:). Stop here and keep the last URL that was
                        // actually fetched over http(s), rather than surfacing a target
                        // this app never actually requested.
                        val nextScheme = try {
                            URL(nextUrl).protocol
                        } catch (e: Exception) {
                            null
                        }
                        if (nextScheme != "http" && nextScheme != "https") {
                            succeeded = true
                            break
                        }
                        currentUrl = nextUrl
                        hop++
                    } else {
                        succeeded = true
                        break
                    }
                } else {
                    // 200 OK or other response, stop resolving
                    succeeded = true
                    break
                }
            } catch (e: Exception) {
                // Return latest URL on failure, but don't cache it (see succeeded above).
                break
            } finally {
                connection?.disconnect()
            }
        }
        if (succeeded) {
            cache[urlStr.trim()] = currentUrl
        }
        currentUrl
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
        val qIndex = temp.indexOf('?')
        if (qIndex != -1) {
            temp = temp.substring(0, qIndex)
        }
        val hIndex = temp.indexOf('#')
        if (hIndex != -1) {
            temp = temp.substring(0, hIndex)
        }
        // Strip userinfo (user:pass@) before the port split below, otherwise
        // "https://user:pass@host/" would extract "user" as the host.
        val atIndex = temp.lastIndexOf('@')
        if (atIndex != -1) {
            temp = temp.substring(atIndex + 1)
        }
        val portIndex = temp.indexOf(':')
        if (portIndex != -1) {
            temp = temp.substring(0, portIndex)
        }
        return temp.trim().lowercase()
    }
}
