package com.tidy.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        val KEY_WHITELISTED_DOMAINS = stringSetPreferencesKey("whitelisted_domains")
        val KEY_BLACKLISTED_PARAMS = stringSetPreferencesKey("blacklisted_params")
        val KEY_DOMAIN_WHITELISTED_PARAMS = stringSetPreferencesKey("domain_whitelisted_params")

        // Legacy single-behaviour automation keys, still read to seed the tier defaults
        // for users upgrading from the pre-tiered settings.
        val KEY_AUTO_COPY_ON_SHARE = booleanPreferencesKey("auto_copy_on_share")
        val KEY_AUTO_CLOSE_ON_SHARE = booleanPreferencesKey("auto_close_on_share")
        val KEY_CHECK_CLIPBOARD_FOR_LINKS = booleanPreferencesKey("check_clipboard_for_links")
        val KEY_CLIPBOARD_CALLOUT_DISMISSED = booleanPreferencesKey("clipboard_callout_dismissed")
        val KEY_CLIPBOARD_CLEAN_TIER = stringPreferencesKey("clipboard_clean_tier")
        val KEY_SHARE_CLEAN_TIER = stringPreferencesKey("share_clean_tier")
        val KEY_AUTO_EXPAND_SHORT_URLS = booleanPreferencesKey("auto_expand_short_urls")
        val KEY_AUTO_REMOVE_MOBILE_SUBDOMAINS =
            booleanPreferencesKey("auto_remove_mobile_subdomains")
        val KEY_FIRST_LAUNCH_DONE = booleanPreferencesKey("first_launch_done")
        val KEY_TOTAL_CLEANED_COUNT = intPreferencesKey("total_cleaned_count")
        val KEY_TOTAL_TRACKERS_BLOCKED = intPreferencesKey("total_trackers_blocked")
        val KEY_AUTO_CLEAN_CLIPBOARD_ON_LAUNCH =
            booleanPreferencesKey("auto_clean_clipboard_on_launch")
        val KEY_AUTO_CLEAN_ON_INPUT = booleanPreferencesKey("auto_clean_on_input")
        val KEY_LAST_CLEANED_URL = stringPreferencesKey("last_cleaned_url")
        val KEY_DONT_ASK_AGAIN_CRASH = booleanPreferencesKey("dont_ask_again_crash")
        val KEY_MIGRATION_DONE = booleanPreferencesKey("migration_done")
        val KEY_MIGRATION_FOLLOWUP_DISMISSED = booleanPreferencesKey("migration_followup_dismissed")
        val KEY_SELECTED_THEME = stringPreferencesKey("selected_theme")
        val KEY_BLOCKLIST_JSON = stringPreferencesKey("blocklist_json")
        val KEY_BLOCKLIST_ETAG = stringPreferencesKey("blocklist_etag")
        val KEY_BLOCKLIST_LAST_FETCH_TIME = longPreferencesKey("blocklist_last_fetch_time")

        // Generated at build time from blocklist/trackers.json — the single authored
        // source of truth, also fetched remotely by BlocklistSyncer. Do not hand-copy
        // the tracker list here; edit blocklist/trackers.json instead.
        const val DEFAULT_BLOCKLIST_JSON: String = GENERATED_DEFAULT_BLOCKLIST_JSON
    }

    val whitelistedDomains: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[KEY_WHITELISTED_DOMAINS] ?: emptySet()
    }

    val blacklistedParams: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[KEY_BLACKLISTED_PARAMS] ?: emptySet()
    }

    val domainWhitelistedParams: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[KEY_DOMAIN_WHITELISTED_PARAMS] ?: emptySet()
    }

    val checkClipboardForLinks: Flow<Boolean> = dataStore.data.map { preferences ->
        // Off by default; pre-tiered "auto-clean clipboard on launch" users who'd
        // already opted in keep clipboard checking on after upgrading.
        preferences[KEY_CHECK_CLIPBOARD_FOR_LINKS]
            ?: (preferences[KEY_AUTO_CLEAN_CLIPBOARD_ON_LAUNCH] == true)
    }

    val clipboardCalloutDismissed: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_CLIPBOARD_CALLOUT_DISMISSED] ?: false
    }

    val clipboardCleanTier: Flow<ClipboardCleanTier> = dataStore.data.map { preferences ->
        val stored = preferences[KEY_CLIPBOARD_CLEAN_TIER]
        when {
            stored != null -> ClipboardCleanTier.fromKey(stored)
            // Pre-tiered "auto-clean clipboard on launch" users keep hands-off cleaning.
            preferences[KEY_AUTO_CLEAN_CLIPBOARD_ON_LAUNCH] == true -> ClipboardCleanTier.AUTO_CLEAN
            else -> ClipboardCleanTier.SUGGEST
        }
    }

    val shareCleanTier: Flow<ShareCleanTier> = dataStore.data.map { preferences ->
        val stored = preferences[KEY_SHARE_CLEAN_TIER]
        when {
            stored != null -> ShareCleanTier.fromKey(stored)
            // Pre-tiered "Share automation" users: copy+close maps to the top tier with the
            // close toggle already on (same key), copy-only maps to the middle tier.
            preferences[KEY_AUTO_COPY_ON_SHARE] == true &&
                    preferences[KEY_AUTO_CLOSE_ON_SHARE] == true -> ShareCleanTier.CLEAN_COPY_AND_SHARE

            preferences[KEY_AUTO_COPY_ON_SHARE] == true -> ShareCleanTier.CLEAN_AND_COPY
            else -> ShareCleanTier.CLEAN
        }
    }

    val closeInsteadOfSharing: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_AUTO_CLOSE_ON_SHARE] ?: false
    }

    val autoExpandShortUrls: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_AUTO_EXPAND_SHORT_URLS] ?: true
    }

    val autoRemoveMobileSubdomains: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_AUTO_REMOVE_MOBILE_SUBDOMAINS] ?: true
    }

    val autoCleanOnInput: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_AUTO_CLEAN_ON_INPUT] ?: true
    }

    val lastCleanedUrl: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_LAST_CLEANED_URL] ?: ""
    }

    val firstLaunchDone: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_FIRST_LAUNCH_DONE] ?: false
    }

    suspend fun setFirstLaunchDone() {
        dataStore.edit { preferences ->
            preferences[KEY_FIRST_LAUNCH_DONE] = true
        }
    }

    val totalCleanedCount: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_TOTAL_CLEANED_COUNT] ?: 0
    }

    val totalTrackersBlocked: Flow<Int> = dataStore.data.map { preferences ->
        preferences[KEY_TOTAL_TRACKERS_BLOCKED] ?: 0
    }

    suspend fun setCheckClipboardForLinks(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_CHECK_CLIPBOARD_FOR_LINKS] = enabled
        }
    }

    suspend fun setClipboardCalloutDismissed(dismissed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_CLIPBOARD_CALLOUT_DISMISSED] = dismissed
        }
    }

    suspend fun setClipboardCleanTier(tier: ClipboardCleanTier) {
        dataStore.edit { preferences ->
            preferences[KEY_CLIPBOARD_CLEAN_TIER] = tier.key
        }
    }

    suspend fun setShareCleanTier(tier: ShareCleanTier) {
        dataStore.edit { preferences ->
            preferences[KEY_SHARE_CLEAN_TIER] = tier.key
        }
    }

    suspend fun setCloseInsteadOfSharing(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_CLOSE_ON_SHARE] = enabled
        }
    }

    suspend fun incrementAnalytics(trackersBlockedCount: Int) {
        dataStore.edit { preferences ->
            val currentCleaned = preferences[KEY_TOTAL_CLEANED_COUNT] ?: 0
            val currentBlocked = preferences[KEY_TOTAL_TRACKERS_BLOCKED] ?: 0
            preferences[KEY_TOTAL_CLEANED_COUNT] = currentCleaned + 1
            preferences[KEY_TOTAL_TRACKERS_BLOCKED] = currentBlocked + trackersBlockedCount
        }
    }

    suspend fun addStats(cleanedCount: Int, trackersBlockedCount: Int) {
        dataStore.edit { preferences ->
            val currentCleaned = preferences[KEY_TOTAL_CLEANED_COUNT] ?: 0
            val currentBlocked = preferences[KEY_TOTAL_TRACKERS_BLOCKED] ?: 0
            preferences[KEY_TOTAL_CLEANED_COUNT] = currentCleaned + cleanedCount
            preferences[KEY_TOTAL_TRACKERS_BLOCKED] = currentBlocked + trackersBlockedCount
        }
    }

    suspend fun addWhitelistedDomain(domain: String) {
        val cleanDomain = domain.trim().lowercase()
        if (cleanDomain.isNotEmpty()) {
            dataStore.edit { preferences ->
                val current = preferences[KEY_WHITELISTED_DOMAINS] ?: emptySet()
                preferences[KEY_WHITELISTED_DOMAINS] = current + cleanDomain
            }
        }
    }

    suspend fun removeWhitelistedDomain(domain: String) {
        val cleanDomain = domain.trim().lowercase()
        dataStore.edit { preferences ->
            val current = preferences[KEY_WHITELISTED_DOMAINS] ?: emptySet()
            preferences[KEY_WHITELISTED_DOMAINS] = current - cleanDomain
        }
    }

    suspend fun addBlacklistedParam(param: String) {
        val cleanParam = param.trim().lowercase()
        if (cleanParam.isNotEmpty()) {
            dataStore.edit { preferences ->
                val current = preferences[KEY_BLACKLISTED_PARAMS] ?: emptySet()
                preferences[KEY_BLACKLISTED_PARAMS] = current + cleanParam
            }
        }
    }

    suspend fun removeBlacklistedParam(param: String) {
        val cleanParam = param.trim().lowercase()
        dataStore.edit { preferences ->
            val current = preferences[KEY_BLACKLISTED_PARAMS] ?: emptySet()
            preferences[KEY_BLACKLISTED_PARAMS] = current - cleanParam
        }
    }

    suspend fun setAutoExpandShortUrls(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_EXPAND_SHORT_URLS] = enabled
        }
    }

    suspend fun setAutoRemoveMobileSubdomains(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_REMOVE_MOBILE_SUBDOMAINS] = enabled
        }
    }

    suspend fun setAutoCleanOnInput(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_CLEAN_ON_INPUT] = enabled
        }
    }

    suspend fun setLastCleanedUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[KEY_LAST_CLEANED_URL] = url
        }
    }

    suspend fun addDomainWhitelistedParam(domain: String, param: String) {
        val cleanDomain = domain.trim().lowercase()
        val cleanParam = param.trim().lowercase()
        if (cleanDomain.isNotEmpty() && cleanParam.isNotEmpty()) {
            dataStore.edit { preferences ->
                val current = preferences[KEY_DOMAIN_WHITELISTED_PARAMS] ?: emptySet()
                preferences[KEY_DOMAIN_WHITELISTED_PARAMS] = current + "$cleanDomain:$cleanParam"
            }
        }
    }

    suspend fun removeDomainWhitelistedParam(domain: String, param: String) {
        val cleanDomain = domain.trim().lowercase()
        val cleanParam = param.trim().lowercase()
        dataStore.edit { preferences ->
            val current = preferences[KEY_DOMAIN_WHITELISTED_PARAMS] ?: emptySet()
            preferences[KEY_DOMAIN_WHITELISTED_PARAMS] = current - "$cleanDomain:$cleanParam"
        }
    }

    suspend fun mergeWhitelistedDomains(domains: Set<String>) {
        dataStore.edit { preferences ->
            val current = preferences[KEY_WHITELISTED_DOMAINS] ?: emptySet()
            preferences[KEY_WHITELISTED_DOMAINS] = current + domains
        }
    }

    suspend fun mergeBlacklistedParams(params: Set<String>) {
        dataStore.edit { preferences ->
            val current = preferences[KEY_BLACKLISTED_PARAMS] ?: emptySet()
            preferences[KEY_BLACKLISTED_PARAMS] = current + params
        }
    }

    suspend fun mergeDomainWhitelistedParams(pairs: Set<String>) {
        dataStore.edit { preferences ->
            val current = preferences[KEY_DOMAIN_WHITELISTED_PARAMS] ?: emptySet()
            preferences[KEY_DOMAIN_WHITELISTED_PARAMS] = current + pairs
        }
    }

    val dontAskAgainCrash: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_DONT_ASK_AGAIN_CRASH] ?: false
    }

    suspend fun setDontAskAgainCrash(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_DONT_ASK_AGAIN_CRASH] = enabled
        }
    }

    val migrationDone: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_MIGRATION_DONE] ?: false
    }

    suspend fun setMigrationDone(done: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_MIGRATION_DONE] = done
        }
    }

    val migrationFollowupDismissed: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_MIGRATION_FOLLOWUP_DISMISSED] ?: false
    }

    suspend fun setMigrationFollowupDismissed(dismissed: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_MIGRATION_FOLLOWUP_DISMISSED] = dismissed
        }
    }

    val selectedTheme: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_SELECTED_THEME] ?: "slate"
    }

    suspend fun setSelectedTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[KEY_SELECTED_THEME] = theme
        }
    }

    val blocklistJson: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_BLOCKLIST_JSON] ?: DEFAULT_BLOCKLIST_JSON
    }

    suspend fun setBlocklistJson(json: String) {
        dataStore.edit { preferences ->
            preferences[KEY_BLOCKLIST_JSON] = json
        }
    }

    val blocklistEtag: Flow<String> = dataStore.data.map { preferences ->
        preferences[KEY_BLOCKLIST_ETAG] ?: ""
    }

    suspend fun setBlocklistEtag(etag: String) {
        dataStore.edit { preferences ->
            preferences[KEY_BLOCKLIST_ETAG] = etag
        }
    }

    val blocklistLastFetchTime: Flow<Long> = dataStore.data.map { preferences ->
        preferences[KEY_BLOCKLIST_LAST_FETCH_TIME] ?: 0L
    }

    suspend fun setBlocklistLastFetchTime(time: Long) {
        dataStore.edit { preferences ->
            preferences[KEY_BLOCKLIST_LAST_FETCH_TIME] = time
        }
    }

    val trackers: Flow<List<TrackerEntry>> = blocklistJson.map { json ->
        try {
            Json.decodeFromString<List<TrackerEntry>>(json)
        } catch (e: Exception) {
            Json.decodeFromString<List<TrackerEntry>>(DEFAULT_BLOCKLIST_JSON)
        }
    }

    val trackerDescriptions: Flow<Map<String, String>> = trackers.map { list ->
        list.associate { it.name to it.description }
    }
}
