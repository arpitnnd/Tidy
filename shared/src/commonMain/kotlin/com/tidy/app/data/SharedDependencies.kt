package com.tidy.app.data

object SharedDependencies {
    @JvmStatic
    @Volatile
    var entitlementManager: EntitlementManager? = null

    @JvmStatic
    @Volatile
    var settingsRepository: SettingsRepository? = null
}
