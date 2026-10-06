package com.tidy.app.security

import androidx.core.content.FileProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.File

class FileProviderTest {
    @Test
    fun onlyTheMigrationBackupCanBeShared() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val authority = "${context.packageName}.fileprovider"
        val backup = File(context.cacheDir, "migration_backups/TidyBackup.json")
        val uri = FileProvider.getUriForFile(context, authority, backup)
        assertEquals("content", uri.scheme)
        assertEquals(authority, uri.authority)

        listOf("history.json", "datastore/settings.preferences_pb", "crash_reports/report.txt")
            .forEach { path ->
                assertThrows(IllegalArgumentException::class.java) {
                    FileProvider.getUriForFile(context, authority, File(context.filesDir, path))
                }
            }
        assertThrows(IllegalArgumentException::class.java) {
            FileProvider.getUriForFile(context, authority, File(context.cacheDir, "history.json"))
        }
    }
}
