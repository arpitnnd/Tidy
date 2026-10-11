package com.tidy.app.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tidy.app.theme.TidyTheme
import com.tidy.app.ui.main.MainScreenActions
import com.tidy.app.ui.main.MainScreenContent
import com.tidy.app.ui.main.MainScreenViewModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class ClipboardLifecycleTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun resumeWaitsForWindowFocusAndPauseCancelsPendingRead() {
        val focused = mutableStateOf(false)
        val reads = AtomicInteger()
        lateinit var owner: LifecycleOwner
        compose.runOnUiThread {
            owner = object : LifecycleOwner {
                override val lifecycle = LifecycleRegistry(this)
            }
            (owner.lifecycle as LifecycleRegistry).currentState = Lifecycle.State.RESUMED
        }
        compose.setContent {
            val actualWindow = LocalWindowInfo.current
            val window = remember(actualWindow) {
                object : WindowInfo by actualWindow {
                    override val isWindowFocused: Boolean
                        get() = focused.value
                }
            }
            CompositionLocalProvider(
                LocalWindowInfo provides window,
                LocalLifecycleOwner provides owner
            ) {
                TidyTheme(selectedTheme = "slate", isPlusUnlocked = false) {
                    MainScreenContent(
                        state = MainScreenViewModel.UiState(isInitialLoading = false),
                        onSettingsClick = {},
                        onHistoryClick = {},
                        actions = object : MainScreenActions {
                            override suspend fun checkClipboardText(text: String?) {
                                reads.incrementAndGet()
                            }
                        }
                    )
                }
            }
        }
        compose.runOnIdle {
            assertEquals(0, reads.get())
            focused.value = true
        }
        compose.waitUntil(5_000) { reads.get() == 1 }
        compose.runOnIdle {
            (owner.lifecycle as LifecycleRegistry).currentState = Lifecycle.State.STARTED
            focused.value = false
            (owner.lifecycle as LifecycleRegistry).currentState = Lifecycle.State.RESUMED
        }
        compose.runOnIdle {
            assertEquals(1, reads.get())
            (owner.lifecycle as LifecycleRegistry).currentState = Lifecycle.State.STARTED
            focused.value = true
        }
        compose.runOnIdle {
            assertEquals(1, reads.get())
            (owner.lifecycle as LifecycleRegistry).currentState = Lifecycle.State.RESUMED
        }
        compose.waitUntil(5_000) { reads.get() == 2 }
    }
}
