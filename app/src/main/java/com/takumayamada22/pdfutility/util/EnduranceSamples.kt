package com.takumayamada22.pdfutility.util

import com.takumayamada22.pdfutility.data.SamplePdfItem
import java.io.File
import java.io.FileNotFoundException

/** The map is supplied by SamplePdfRepository after size/header/SHA-256 validation. */
internal fun requireEnduranceSamples(
    catalogWithFiles: Pair<List<SamplePdfItem>, Map<String, File?>>
): List<File> {
    val (catalog, verifiedFiles) = catalogWithFiles
    val samples = catalog.mapNotNull { verifiedFiles[it.id] }.distinct()
    if (samples.isEmpty()) {
        throw FileNotFoundException(
            "No verified sample PDFs are available. Download sample PDFs in the app first; " +
                "the endurance test uses the same verified sample cache."
        )
    }
    return samples
}
