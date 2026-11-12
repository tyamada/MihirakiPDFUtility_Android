package com.takumayamada22.pdfutility

import android.app.Application
import androidx.core.content.edit
import com.takumayamada22.pdfutility.util.cleanupStalePdfCopies
import com.takumayamada22.pdfutility.util.PersistentLogManager
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

class PdfApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        cleanupStalePdfCopies(cacheDir)
        PDFBoxResourceLoader.init(applicationContext)
        val privacy = getSharedPreferences("log_privacy", MODE_PRIVATE)
        if (!privacy.getBoolean("fixed_location_labels_v1", false)) {
            val audit = java.io.File(filesDir, "app_audit_logs.txt")
            val logs = java.io.File(filesDir, "app_logs")
            val auditRemoved = !audit.exists() || audit.delete()
            val logsRemoved = !logs.exists() || logs.deleteRecursively()
            if (auditRemoved && logsRemoved) {
                privacy.edit { putBoolean("fixed_location_labels_v1", true) }
            }
        }
        PersistentLogManager.init(applicationContext)
    }
}
