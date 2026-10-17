package com.takumayamada22.pdfutility.domain

import android.net.Uri

data class ImageFileItem(
    val uri: Uri,
    val name: String,
    val size: Long,
    val creationTime: Long,
    val lastModifiedTime: Long
)

enum class ImageSortField {
    NAME,
    CREATION_DATE,
    MODIFIED_DATE
}

enum class ImageSortOrder {
    ASCENDING,
    DESCENDING
}
