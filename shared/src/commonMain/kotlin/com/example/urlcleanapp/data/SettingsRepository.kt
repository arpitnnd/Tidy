package com.example.urlcleanapp.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    companion object {
        val KEY_WHITELISTED_DOMAINS = stringSetPreferencesKey("whitelisted_domains")
        val KEY_BLACKLISTED_PARAMS = stringSetPreferencesKey("blacklisted_params")
        val KEY_DOMAIN_WHITELISTED_PARAMS = stringSetPreferencesKey("domain_whitelisted_params")
        val KEY_AUTO_COPY_ON_SHARE = booleanPreferencesKey("auto_copy_on_share")
        val KEY_AUTO_CLOSE_ON_SHARE = booleanPreferencesKey("auto_close_on_share")
        val KEY_AUTO_EXPAND_SHORT_URLS = booleanPreferencesKey("auto_expand_short_urls")
        val KEY_AUTO_REMOVE_MOBILE_SUBDOMAINS = booleanPreferencesKey("auto_remove_mobile_subdomains")
        val KEY_FIRST_LAUNCH_DONE = booleanPreferencesKey("first_launch_done")
        val KEY_TOTAL_CLEANED_COUNT = intPreferencesKey("total_cleaned_count")
        val KEY_TOTAL_TRACKERS_BLOCKED = intPreferencesKey("total_trackers_blocked")
        val KEY_AUTO_CLEAN_CLIPBOARD_ON_LAUNCH = booleanPreferencesKey("auto_clean_clipboard_on_launch")
        val KEY_AUTO_CLEAN_ON_INPUT = booleanPreferencesKey("auto_clean_on_input")
        val KEY_LAST_CLEANED_URL = stringPreferencesKey("last_cleaned_url")
        val KEY_DONT_ASK_AGAIN_CRASH = booleanPreferencesKey("dont_ask_again_crash")
        val KEY_MIGRATION_DONE = booleanPreferencesKey("migration_done")
        val KEY_MIGRATION_FOLLOWUP_DISMISSED = booleanPreferencesKey("migration_followup_dismissed")
        val KEY_SELECTED_THEME = stringPreferencesKey("selected_theme")
        val KEY_BLOCKLIST_JSON = stringPreferencesKey("blocklist_json")
        val KEY_BLOCKLIST_ETAG = stringPreferencesKey("blocklist_etag")
        val KEY_BLOCKLIST_LAST_FETCH_TIME = longPreferencesKey("blocklist_last_fetch_time")
        
        const val DEFAULT_BLOCKLIST_JSON = """[
  {"name": "utm_source", "description": "Google Analytics campaign source parameter, used to track referrer website/app."},
  {"name": "utm_medium", "description": "Google Analytics campaign medium parameter, used to identify marketing channel (e.g. email, CPC)."},
  {"name": "utm_campaign", "description": "Google Analytics campaign name parameter, used to identify a specific marketing campaign."},
  {"name": "utm_term", "description": "Google Analytics campaign term parameter, used to track keywords for search ads."},
  {"name": "utm_content", "description": "Google Analytics campaign content parameter, used to distinguish different links in the same ad/email."},
  {"name": "utm_id", "description": "Google Analytics campaign ID parameter, used to identify a specific ad campaign."},
  {"name": "utm_source_platform", "description": "Google Analytics campaign source platform parameter, identifying the ad platform."},
  {"name": "utm_marketing_tactic", "description": "Google Analytics campaign tactic parameter, used to track the marketing approach."},
  {"name": "fbclid", "description": "Facebook Click ID, used by Facebook to track user clicks and link them to advertising campaigns."},
  {"name": "gclid", "description": "Google Click ID, used by Google AdWords to track clicks and attribute conversions."},
  {"name": "msclkid", "description": "Microsoft Click ID, used by Bing Ads to track clicks and attribute conversions."},
  {"name": "yclid", "description": "Yandex Click ID, used by Yandex Direct to track clicks and attribute conversions."},
  {"name": "dclid", "description": "DoubleClick Click ID, used to track display advertisements."},
  {"name": "si", "description": "Spotify Share ID, containing analytical telemetry to identify the sharing source profile."},
  {"name": "igsh", "description": "Instagram Share ID, containing analytical telemetry to identify the sharing source profile."},
  {"name": "mc_eid", "description": "Mailchimp Email ID, used to link an email click back to a subscriber profile."},
  {"name": "gclsrc", "description": "Google Click Source parameter, specifying the AdWords platform source for conversion tracking."},
  {"name": "rb_clickid", "description": "Rebounce Click ID, used for redirect tracking and attribution."},
  {"name": "affclick", "description": "Affiliate Click ID, used to track affiliate sales conversions."},
  {"name": "campid", "description": "Campaign ID, used by various marketing platforms to identify the active campaign."}
]"""
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

    val autoCopyOnShare: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_AUTO_COPY_ON_SHARE] ?: false
    }

    val autoCloseOnShare: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_AUTO_CLOSE_ON_SHARE] ?: false
    }

    val autoExpandShortUrls: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_AUTO_EXPAND_SHORT_URLS] ?: true
    }

    val autoRemoveMobileSubdomains: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_AUTO_REMOVE_MOBILE_SUBDOMAINS] ?: true
    }

    val autoCleanClipboardOnLaunch: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_AUTO_CLEAN_CLIPBOARD_ON_LAUNCH] ?: false
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

    suspend fun setAutoCopyOnShare(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_COPY_ON_SHARE] = enabled
        }
    }

    suspend fun setAutoCloseOnShare(enabled: Boolean) {
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

    suspend fun setAutoCleanClipboardOnLaunch(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[KEY_AUTO_CLEAN_CLIPBOARD_ON_LAUNCH] = enabled
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
