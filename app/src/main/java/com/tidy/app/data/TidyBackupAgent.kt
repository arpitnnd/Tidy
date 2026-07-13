package com.tidy.app.data

import android.app.backup.BackupAgent
import android.app.backup.BackupDataInput
import android.app.backup.BackupDataOutput
import android.app.backup.FullBackupDataOutput
import android.os.ParcelFileDescriptor

class TidyBackupAgent : BackupAgent() {

    override fun onFullBackup(data: FullBackupDataOutput) {
        // Full-data (Auto Backup) is opt-in: history and settings are only handed to the
        // system backup transport once the user explicitly enables it from
        // Settings > Back up app data. See BackupPreference.
        if (BackupPreference.isAllowed(applicationContext)) {
            super.onFullBackup(data)
        }
    }

    override fun onBackup(
        oldState: ParcelFileDescriptor?,
        data: BackupDataOutput?,
        newState: ParcelFileDescriptor?
    ) {
        // Key/value backup is unused; only the full-data path (onFullBackup) applies.
    }

    override fun onRestore(
        data: BackupDataInput?,
        appVersionCode: Int,
        newState: ParcelFileDescriptor?
    ) {
        // Key/value backup is unused; restores arrive via the full-data path.
    }
}
