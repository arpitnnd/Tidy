package com.tidy.app

import android.app.Application
import androidx.compose.ui.ComposeUiFlags
import androidx.compose.ui.ExperimentalComposeUiApi
import com.tidy.app.data.HistoryRepository
import com.tidy.app.data.SettingsRepository
import com.tidy.app.data.createPlatformDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.acra.ACRA
import org.acra.ReportField
import org.acra.config.CoreConfigurationBuilder
import org.acra.data.StringFormat

class TidyApp : Application() {
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var historyRepository: HistoryRepository
        private set
    lateinit var entitlementManager: com.tidy.app.data.AndroidEntitlementManager
        private set

    private val applicationScope = CoroutineScope(Dispatchers.Default)
    private lateinit var automaticSyncAllowed: Deferred<Boolean>

    override fun attachBaseContext(base: android.content.Context) {
        super.attachBaseContext(base)
        val builder = CoreConfigurationBuilder()
            .withBuildConfigClass(TidyApp::class.java)
            .withReportFormat(StringFormat.JSON)
            .withReportContent(
                ReportField.APP_VERSION_NAME,
                ReportField.APP_VERSION_CODE,
                ReportField.ANDROID_VERSION,
                ReportField.PHONE_MODEL,
                ReportField.STACK_TRACE
            )
        ACRA.init(this, builder)
    }

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate() {
        ComposeUiFlags.isMediaQueryIntegrationEnabled = true
        super.onCreate()
        val dataStore = createPlatformDataStore(this)
        settingsRepository = SettingsRepository(dataStore)
        // Capture the session's starting preference before onboarding can change it.
        // A fresh installation uses bundled rules throughout its first session.
        automaticSyncAllowed = applicationScope.async { settingsRepository.firstLaunchDone.first() }
        historyRepository = HistoryRepository(this)
        instance = this
        com.tidy.app.data.SharedDependencies.settingsRepository = settingsRepository

        entitlementManager = FlavorConfig.createEntitlementManager(this)
        entitlementManager.init()

        com.tidy.app.data.SharedDependencies.entitlementManager = entitlementManager

        syncBlocklistInBackground()
    }

    fun syncBlocklistInBackground() {
        applicationScope.launch {
            if (automaticSyncAllowed.await()) {
                com.tidy.app.data.BlocklistSyncer.sync(this@TidyApp, settingsRepository)
            }
        }
    }

    companion object {
        lateinit var instance: TidyApp
            private set
    }
}
