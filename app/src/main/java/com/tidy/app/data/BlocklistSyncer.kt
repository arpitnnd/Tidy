package com.tidy.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

object BlocklistSyncer {
    const val DEFAULT_BLOCKLIST_URL =
        "https://raw.githubusercontent.com/arpitnnd/Tidy/main/blocklist/trackers.json"

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

                    // Validate it parses correctly before saving
                    val trackers = Json.decodeFromString<List<TrackerEntry>>(text)
                    if (trackers.isNotEmpty()) {
                        settingsRepository.setBlocklistJson(text)
                        if (etag.isNotEmpty()) {
                            settingsRepository.setBlocklistEtag(etag)
                        }
                        settingsRepository.setBlocklistLastFetchTime(now)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Keep using currently cached blocklist silently on failure
            } finally {
                connection?.disconnect()
            }
        }
}
