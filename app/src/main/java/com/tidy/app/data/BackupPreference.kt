package com.tidy.app.data

import android.content.Context

object BackupPreference {
    private const val PREFS_NAME = "tidy_backup_prefs"
    private const val KEY_ALLOW_SYSTEM_BACKUP = "allow_system_backup"

    fun isAllowed(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ALLOW_SYSTEM_BACKUP, false)

    fun setAllowed(context: Context, allowed: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ALLOW_SYSTEM_BACKUP, allowed)
            .apply()
    }
}
