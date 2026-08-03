package com.tidy.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

object BlocklistSyncer {
    // v2, not the bare "trackers.json" path: see shared/build.gradle.kts's
    // generateTrackerDefaults for why that path is frozen at its 1.1.0-era schema and new
    // schema versions get their own versioned filename instead of being added to it.
    const val DEFAULT_BLOCKLIST_URL =
        "https://raw.githubusercontent.com/arpitnnd/Tidy/main/blocklist/trackers.v2.json"

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun sync(
        context: Context,
        settingsRepository: SettingsRepository,
        urlStr: String = DEFAULT_BLOCKLIST_URL
    ) =
        withContext(Dispatchers.IO) {
            val lastFetch = settingsRepository.blocklistLastFetchTime.first()
            val now = System.currentTimeMillis()

            // Fetch every 3 days (3 * 24 * 60 * 60 * 1000 = 259200000 ms)
            val cacheDuration = 259200000L
            if (now - lastFetch < cacheDuration) {
                return@withContext
            }

            var connection: HttpURLConnection? = null
            try {
                val url = URL(urlStr)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.instanceFollowRedirects = true

                // Add a modern user agent
                connection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 10) Tidy/1.0"
                )

                val cachedEtag = settingsRepository.blocklistEtag.first()
                if (cachedEtag.isNotEmpty()) {
                    connection.setRequestProperty("If-None-Match", cachedEtag)
                }

                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                    settingsRepository.setBlocklistLastFetchTime(now)
                    return@withContext
                }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val etag = connection.getHeaderField("ETag") ?: ""
                    val text = connection.inputStream.bufferedReader().use { it.readText() }

                    // Validate it parses correctly before saving. ignoreUnknownKeys so a
                    // future schema field this app doesn't know about yet doesn't break
                    // sync entirely (see DEFAULT_BLOCKLIST_URL's comment above).
                    val trackers = json.decodeFromString<List<TrackerEntry>>(text)
                    if (trackers.isNotEmpty()) {
                        settingsRepository.setBlocklistJson(text)
                        if (etag.isNotEmpty()) {
                            settingsRepository.setBlocklistEtag(etag)
                        }
                        settingsRepository.setBlocklistLastFetchTime(now)
                    }
                }
            } catch (_: Exception) {
                // Keep using currently cached blocklist silently on failure
            } finally {
                connection?.disconnect()
            }
        }
}
