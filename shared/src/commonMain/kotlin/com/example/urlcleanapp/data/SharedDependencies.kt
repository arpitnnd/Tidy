package com.example.urlcleanapp.data

import kotlin.jvm.JvmStatic

object SharedDependencies {
    @JvmStatic
    @Volatile
    var entitlementManager: EntitlementManager? = null

    @JvmStatic
    @Volatile
    var settingsRepository: SettingsRepository? = null
}
