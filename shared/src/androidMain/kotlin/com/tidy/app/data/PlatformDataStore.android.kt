package com.tidy.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences

actual fun createPlatformDataStore(context: Any): DataStore<Preferences> {
    val ctx = context as Context
    return PreferenceDataStoreFactory.create(
        produceFile = {
            ctx.filesDir.resolve("TidyURLApp.preferences_pb")
        }
    )
}
