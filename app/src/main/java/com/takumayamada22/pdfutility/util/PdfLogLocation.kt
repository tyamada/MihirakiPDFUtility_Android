package com.takumayamada22.pdfutility.util

/** Fixed labels only: never return a user-controlled URI, folder name or authority. */
internal fun pdfLogLocation(scheme: String?, authority: String?, isSample: Boolean): String = when {
    isSample -> "Sample PDF"
    scheme == "file" || scheme == null -> "Local Device"
    authority == "com.android.externalstorage.documents" -> "Local Device"
    authority == "com.android.providers.downloads.documents" -> "Local Device (Downloads)"
    authority == "com.google.android.apps.docs.storage" -> "Google Drive"
    else -> "Document Provider"
}
