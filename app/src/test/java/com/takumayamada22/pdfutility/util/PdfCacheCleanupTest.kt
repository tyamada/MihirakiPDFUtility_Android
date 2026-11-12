package com.takumayamada22.pdfutility.util

import org.junit.Assert.*
import org.junit.Test
import java.nio.file.Files
import java.io.File

class PdfCacheCleanupTest {
    @Test fun `startup removes abandoned document copies but preserves unrelated files`() {
        val dir = Files.createTempDirectory("pdf-cache-test").toFile()
        try {
            val stale = File(dir, "pdf-render-abandoned.pdf").apply { writeText("private document") }
            val folderCopy = File(dir, "folder_images_123.pdf").apply { writeText("private images") }
            val other = File(dir, "user.pdf").apply { writeText("keep") }
            val wrongExtension = File(dir, "pdf-render-notes.txt").apply { writeText("keep") }
            val directory = File(dir, "pdf-render-directory.pdf").apply { mkdir() }
            val nested = File(directory, "keep.pdf").apply { writeText("keep") }
            cleanupStalePdfCopies(dir)
            cleanupStalePdfCopies(dir)
            assertFalse(stale.exists())
            assertFalse(folderCopy.exists())
            assertTrue(other.exists())
            assertTrue(wrongExtension.exists())
            assertTrue(nested.exists())
        } finally { dir.deleteRecursively() }
    }
}
