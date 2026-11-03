package com.takumayamada22.pdfutility.data

import java.io.File
import java.io.IOException
import java.security.MessageDigest

/** Owns only hash-versioned sample files. Legacy downloads are read, never removed. */
internal class SamplePdfCache(private val directory: File) {
    private fun target(item: SamplePdfItem): File {
        require(item.fileName.matches(Regex("[A-Za-z0-9_.-]+\\.pdf")))
        require(item.sha256.matches(Regex("[0-9a-f]{64}")))
        return File(directory, "verified/${item.sha256}/${item.fileName}")
    }

    private fun deletedMarker(item: SamplePdfItem) = File(target(item).path + ".deleted")

    fun matches(file: File, item: SamplePdfItem): Boolean = try {
        file.isFile && file.length() == item.bytes && file.inputStream().use { input ->
            val header = ByteArray(5)
            input.read(header) == 5 && header.contentEquals("%PDF-".toByteArray(Charsets.US_ASCII))
        } && file.inputStream().use { input ->
            val digest = MessageDigest.getInstance("SHA-256")
            val buffer = ByteArray(64 * 1024)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
            digest.digest().joinToString("") { "%02x".format(it.toInt() and 0xff) } == item.sha256
        }
    } catch (_: IOException) { false }

    fun downloadedFile(item: SamplePdfItem): File? {
        val current = target(item)
        if (matches(current, item)) return current
        if (deletedMarker(item).exists()) return null
        // An unchanged v1 PDF remains usable offline, only after a full hash check.
        return File(directory, item.fileName).takeIf { matches(it, item) }
    }

    fun temporaryFile(item: SamplePdfItem): File {
        val parent = requireNotNull(target(item).parentFile)
        if (!parent.isDirectory && !parent.mkdirs()) throw IOException("Sample directory unavailable")
        return File.createTempFile(item.fileName + ".", ".part", parent)
    }

    fun publish(item: SamplePdfItem, temporary: File) {
        if (!matches(temporary, item)) throw SamplePdfIntegrityException()
        // Same-directory rename publishes only completely verified bytes. No pre-delete.
        if (!temporary.renameTo(target(item))) throw IOException("Unable to publish verified sample")
        deletedMarker(item).delete()
    }

    fun delete(item: SamplePdfItem): Boolean {
        val current = target(item)
        val parent = requireNotNull(current.parentFile)
        if (!parent.isDirectory && !parent.mkdirs()) return false
        // Explicit removal hides a reusable legacy copy without deleting the original PDF.
        if (!deletedMarker(item).exists() && !deletedMarker(item).createNewFile()) return false
        return !current.exists() || current.delete()
    }
}

internal class SamplePdfIntegrityException : IOException("PDF size, header or SHA-256 verification failed")
