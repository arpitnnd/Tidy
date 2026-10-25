package com.tidy.app.ui

import android.content.ClipboardManager
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.core.content.IntentCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.tidy.app.data.HistoryRepository
import com.tidy.app.data.SettingsRepository
import com.tidy.app.ui.main.MainScreenViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.io.File

class ShareMessageTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private class MemoryPreferences : DataStore<Preferences> {
        private val preferences = MutableStateFlow<Preferences>(emptyPreferences())
        override val data: Flow<Preferences> = preferences
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            transform(preferences.value).also { preferences.value = it }
    }

    @Test
    fun copyAndShareEmitThePreservedMessage() {
        compose.setContent { Text("Share message test") }
        compose.onNodeWithText("Share message test").assertIsDisplayed()
        compose.waitUntil(5_000) { compose.activity.hasWindowFocus() }
        lateinit var viewModel: MainScreenViewModel
        var chooser: Intent? = null
        val context = object : ContextWrapper(compose.activity) {
            override fun startActivity(intent: Intent) {
                chooser = intent
            }
        }
        compose.runOnIdle {
            viewModel = MainScreenViewModel(
                SettingsRepository(MemoryPreferences()),
                HistoryRepository(
                    testFile = File(
                        compose.activity.cacheDir,
                        "share-message-test.json"
                    )
                )
            )
            viewModel.cleanUrl(
                "  Read (example.com/page?fbclid=private),\nplease.  ",
                isShared = true,
                addToHistory = false
            )
        }
        compose.waitUntil(5_000) { viewModel.uiState.value.isCleaned }
        val expected = "  Read (https://example.com/page),\nplease.  "
        compose.runOnIdle {
            viewModel.copyToClipboard(context)
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            assertEquals(expected, clipboard.primaryClip?.getItemAt(0)?.text?.toString())
            viewModel.shareUrl(context)
            val send =
                IntentCompat.getParcelableExtra(chooser!!, Intent.EXTRA_INTENT, Intent::class.java)
            assertEquals("text/plain", send?.type)
            assertEquals(expected, send?.getStringExtra(Intent.EXTRA_TEXT))
            viewModel.clear()
        }
    }
}
