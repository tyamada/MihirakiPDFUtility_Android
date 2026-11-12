package com.takumayamada22.pdfutility.util

import java.io.File

/** Called once at process startup, before any document can be opened. */
internal fun cleanupStalePdfCopies(cacheDir: File) {
    cacheDir.listFiles()?.filter { file ->
        file.isFile && ((file.name.startsWith("pdf-render-") && file.name.endsWith(".pdf")) || (file.name.startsWith("folder_images_") && file.name.endsWith(".pdf")))
    }?.forEach { it.delete() }
}
