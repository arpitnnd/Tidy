package com.example.urlcleanapp

import android.app.Application
import com.example.urlcleanapp.data.HistoryRepository
import com.example.urlcleanapp.data.SettingsRepository
import com.example.urlcleanapp.data.createPlatformDataStore
import org.acra.ACRA
import org.acra.config.CoreConfigurationBuilder
import org.acra.data.StringFormat
import org.acra.ReportField

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UrlCleanApp : Application() {
    lateinit var settingsRepository: SettingsRepository
        private set
    lateinit var historyRepository: HistoryRepository
        private set
    lateinit var entitlementManager: com.example.urlcleanapp.data.AndroidEntitlementManager
        private set

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    override fun attachBaseContext(base: android.content.Context) {
        super.attachBaseContext(base)
        val builder = CoreConfigurationBuilder()
            .withBuildConfigClass(UrlCleanApp::class.java)
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

        com.example.urlcleanapp.data.SharedDependencies.entitlementManager = entitlementManager
        com.example.urlcleanapp.data.SharedDependencies.settingsRepository = settingsRepository

        applicationScope.launch {
            com.example.urlcleanapp.data.BlocklistSyncer.sync(this@UrlCleanApp, settingsRepository)
        }
    }

    companion object {
        lateinit var instance: UrlCleanApp
            private set
    }
}
