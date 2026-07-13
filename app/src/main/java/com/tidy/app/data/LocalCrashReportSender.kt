package com.tidy.app.data

import android.content.Context
import org.acra.config.CoreConfiguration
import org.acra.data.CrashReportData
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderFactory
import java.io.File

class LocalCrashReportSender : ReportSender {
    override fun send(context: Context, errorContent: CrashReportData) {
        try {
            val crashReportsDir = File(context.filesDir, "crash_reports")
            if (!crashReportsDir.exists()) {
                crashReportsDir.mkdirs()
            }
            val reportFile = File(crashReportsDir, "crash_${System.currentTimeMillis()}.txt")

            val appVersionName =
                errorContent.getString(org.acra.ReportField.APP_VERSION_NAME) ?: "Unknown"
            val appVersionCode =
                errorContent.getString(org.acra.ReportField.APP_VERSION_CODE) ?: "Unknown"
            val androidVersion =
                errorContent.getString(org.acra.ReportField.ANDROID_VERSION) ?: "Unknown"
            val phoneModel = errorContent.getString(org.acra.ReportField.PHONE_MODEL) ?: "Unknown"
            val stackTrace =
                errorContent.getString(org.acra.ReportField.STACK_TRACE) ?: "No stacktrace"

            val reportText = buildString {
                appendLine("App Version: $appVersionName ($appVersionCode)")
                appendLine("Android Version: $androidVersion")
                appendLine("Device Model: $phoneModel")
                appendLine("Stack Trace:")
                appendLine(stackTrace)
            }
            reportFile.writeText(reportText)
        } catch (_: Exception) {
            // Best-effort local write; nothing to do if it fails.
        }
    }
}

class LocalCrashReportSenderFactory : ReportSenderFactory {
    override fun create(context: Context, config: CoreConfiguration): ReportSender {
        return LocalCrashReportSender()
    }

    override fun enabled(config: CoreConfiguration): Boolean {
        return true
    }
}
