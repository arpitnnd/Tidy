package com.tidy.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tidy.app.data.UrlDetection
import com.tidy.app.theme.TidyTheme
import kotlinx.coroutines.flow.MutableSharedFlow

// Clipboard checking deliberately does NOT live here: it is hooked into the main
// screen's ON_RESUME observer so cold start and resume behave identically, and so
// that opening Tidy directly can never auto-close the app. The only close-the-app
// automation is the "Close Tidy instead of sharing" toggle, which fires solely in
// response to a genuine incoming ACTION_SEND intent from another app.
class MainActivity : ComponentActivity() {
    private val sharedUrls = MutableSharedFlow<String>(extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_Tidy)
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
        }

        val showPlusUpsell = intent?.getBooleanExtra("show_plus_upsell", false) ?: false

        enableEdgeToEdge()
        setContent {
            TidyTheme {
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
            // Shared detection rule: shares without anything link-like are skipped silently.
            return UrlDetection.findFirstUrl(sharedText)
        }
        return null
    }
}
