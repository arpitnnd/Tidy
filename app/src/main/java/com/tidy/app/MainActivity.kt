package com.tidy.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tidy.app.data.UrlCleaner
import com.tidy.app.theme.TidyURLTheme
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    private val sharedUrls = MutableSharedFlow<String>(extraBufferCapacity = 1)

    companion object {
        private var lastExitTimestamp: Long = 0L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TidyURL)
        super.onCreate(savedInstanceState)

        val crashReportsDir = java.io.File(filesDir, "crash_reports")
        val crashReportText = if (crashReportsDir.exists()) {
            val files = crashReportsDir.listFiles()
            if (!files.isNullOrEmpty()) {
                files.sortBy { it.lastModified() }
                val latestFile = files.last()
                try {
                    latestFile.readText()
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        } else {
            null
        }

        val initialUrl = handleIntent(intent)
        if (initialUrl != null) {
            sharedUrls.tryEmit(initialUrl)
        } else if (crashReportText == null) {
            val now = System.currentTimeMillis()
            val settings = TidyURLApp.instance.settingsRepository
            val shouldAutoClean = runBlocking { settings.autoCleanClipboardOnLaunch.first() }
            val isFromHistory = (intent?.flags?.and(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) ?: 0) != 0
            val isFreshLaunch = savedInstanceState == null && !isFromHistory
            if (shouldAutoClean && isFreshLaunch && (now - lastExitTimestamp > 3000)) {
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                if (clipboard.hasPrimaryClip()) {
                    val item = clipboard.primaryClip?.getItemAt(0)
                    val text = item?.text?.toString()?.trim()
                    if (text != null && (text.startsWith("http://", ignoreCase = true) || text.startsWith("https://", ignoreCase = true) || (text.contains(".") && !text.contains(" ")))) {
                        val formattedText = if (!text.startsWith("http://", ignoreCase = true) && !text.startsWith("https://", ignoreCase = true)) {
                            "https://$text"
                        } else {
                            text
                        }
                        val lastCleaned = runBlocking { settings.lastCleanedUrl.first() }
                        if (formattedText != lastCleaned) {
                            val whitelist = runBlocking { settings.whitelistedDomains.first() }
                            val customBlacklist = runBlocking { settings.blacklistedParams.first() }
                            val domainParams = runBlocking { settings.domainWhitelistedParams.first() }
                            val removeMobile = runBlocking { settings.autoRemoveMobileSubdomains.first() }
                            val cleanResult = UrlCleaner().clean(
                                urlStr = formattedText,
                                whitelistedDomains = whitelist,
                                customBlacklistParams = customBlacklist,
                                domainWhitelistedParams = domainParams,
                                removeMobileSubdomains = removeMobile
                            )
                            if (cleanResult.removedParams.isNotEmpty()) {
                                runBlocking {
                                    settings.setLastCleanedUrl(cleanResult.cleanedUrl)
                                }
                                val clip = ClipData.newPlainText(getString(R.string.main_cleaned_url), cleanResult.cleanedUrl)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(this, getString(R.string.toast_cleaned_copied), Toast.LENGTH_SHORT).show()
                                lastExitTimestamp = now
                                finish()
                                return
                            }
                        }
                    }
                }
            }
        }

        val showPlusUpsell = intent?.getBooleanExtra("show_plus_upsell", false) ?: false

        enableEdgeToEdge()
        setContent {
            TidyURLTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainNavigation(initialUrl, sharedUrls, crashReportText, showPlusUpsell)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val url = handleIntent(intent)
        if (url != null) {
            sharedUrls.tryEmit(url)
        }
    }

    private fun handleIntent(intent: Intent): String? {
        if (intent.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            return extractUrl(sharedText)
        }
        return null
    }

    private fun extractUrl(text: String?): String? {
        if (text == null) return null
        val regex = """https?://[^\s]+""".toRegex()
        val match = regex.find(text)
        return match?.value
    }
}
