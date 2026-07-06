package com.tidy.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

expect fun createPlatformDataStore(context: Any): DataStore<Preferences>
