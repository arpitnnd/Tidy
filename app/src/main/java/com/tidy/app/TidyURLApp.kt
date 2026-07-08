package com.tidy.app

import android.app.Application
import com.tidy.app.data.HistoryRepository
import com.tidy.app.data.SettingsRepository
import com.tidy.app.data.createPlatformDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.acra.ACRA
import org.acra.ReportField
import org.acra.config.CoreConfigurationBuilder
import org.acra.data.StringFormat

class TidyURLApp : Application() {
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var historyRepository: HistoryRepository
        private set
    lateinit var entitlementManager: com.tidy.app.data.AndroidEntitlementManager
        private set

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun attachBaseContext(base: android.content.Context) {
        super.attachBaseContext(base)
        val builder = CoreConfigurationBuilder()
            .withBuildConfigClass(TidyURLApp::class.java)
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

    override fun onCreate() {
        super.onCreate()
        val dataStore = createPlatformDataStore(this)
        settingsRepository = SettingsRepository(dataStore)
        historyRepository = HistoryRepository(this)
        instance = this

        entitlementManager = FlavorConfig.createEntitlementManager(this)
        entitlementManager.init()

        com.tidy.app.data.SharedDependencies.entitlementManager = entitlementManager
        com.tidy.app.data.SharedDependencies.settingsRepository = settingsRepository

        applicationScope.launch {
            com.tidy.app.data.BlocklistSyncer.sync(this@TidyURLApp, settingsRepository)
        }
    }

    companion object {
        lateinit var instance: TidyURLApp
            private set
    }
}
