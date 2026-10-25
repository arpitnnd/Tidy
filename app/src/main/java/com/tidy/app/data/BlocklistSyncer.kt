package com.tidy.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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

    // Guards the whole read-check-fetch-write sequence below: sync() is now triggered
    // independently from app startup, activity resume, and a manual Settings refresh, and
    // without this a second caller can read the stale last-check time before the first
    // caller's fetch has written a new one, racing into a redundant concurrent fetch.
    private val syncMutex = Mutex()

    sealed interface SyncResult {
        data object UpToDate : SyncResult
        data class Updated(val newRulesCount: Int) : SyncResult
        data object Skipped : SyncResult
        data class Error(val message: String? = null) : SyncResult
    }

    suspend fun sync(
        context: Context,
        settingsRepository: SettingsRepository,
        urlStr: String = DEFAULT_BLOCKLIST_URL,
        force: Boolean = false
    ): SyncResult = syncMutex.withLock {
        withContext(Dispatchers.IO) {
            if (!force && !settingsRepository.firstLaunchDone.first()) {
                return@withContext SyncResult.Skipped
            }
            val lastSuccessfulCheck = settingsRepository.blocklistLastSuccessfulCheckTime.first()
            val now = System.currentTimeMillis()

            // Fetch every 3 days (3 * 24 * 60 * 60 * 1000 = 259200000 ms)
            val cacheDuration = 259200000L
            if (!force && now - lastSuccessfulCheck < cacheDuration) {
                return@withContext SyncResult.Skipped
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
                    settingsRepository.setBlocklistLastSuccessfulCheckTime(now)
                    return@withContext SyncResult.UpToDate
                }

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val etag = connection.getHeaderField("ETag") ?: ""
                    val text = connection.inputStream.bufferedReader().use { it.readText() }

                    // Validate it parses correctly before saving. ignoreUnknownKeys so a
                    // future schema field this app doesn't know about yet doesn't break
                    // sync entirely (see DEFAULT_BLOCKLIST_URL's comment above).
                    val trackers = json.decodeFromString<List<TrackerEntry>>(text)
                    if (trackers.isNotEmpty()) {
                        val previousTrackers = settingsRepository.trackers.first()
                        val newRulesCount = trackers.count { it !in previousTrackers }
                        settingsRepository.setBlocklistJson(text)
                        if (etag.isNotEmpty()) {
                            settingsRepository.setBlocklistEtag(etag)
                        }
                        settingsRepository.setBlocklistLastSuccessfulCheckTime(now)
                        return@withContext SyncResult.Updated(newRulesCount = newRulesCount)
                    }
                    // A successful response with no entries is not an error: just nothing
                    // new to apply.
                    settingsRepository.setBlocklistLastSuccessfulCheckTime(now)
                    return@withContext SyncResult.UpToDate
                }
                SyncResult.Error("HTTP $responseCode")
            } catch (e: Exception) {
                // Keep using currently cached blocklist silently on failure
                SyncResult.Error(e.message)
            } finally {
                connection?.disconnect()
            }
        }
    }
}
