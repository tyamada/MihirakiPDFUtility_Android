package com.takumayamada22.pdfutility.util

import android.content.Context
import android.net.Uri
import android.util.AtomicFile
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.UUID

/** A private durable copy avoids relying on a temporary GetContent URI grant or cache file.
 * Only the last successfully opened source is retained. Passwords are never persisted.
 * This reopens the source PDF; it is not an autosave of unsaved page edits.
 */
class LastDocumentStore(private val context: Context) {
    data class Document(val uri: Uri, val name: String, val isSample: Boolean)
    private val directory = File(context.noBackupFilesDir, "last-document").apply { mkdirs() }
    private val manifest = AtomicFile(File(directory, "document.json"))

    fun read(): Document? {
        if (!manifest.baseFile.exists()) return null
        val json = JSONObject(manifest.openRead().bufferedReader().use { it.readText() })
        val file = File(directory, json.getString("file"))
        require(file.canonicalFile.parentFile == directory.canonicalFile)
        if (!file.isFile) throw IOException("Last document is unavailable")
        return Document(Uri.fromFile(file), json.getString("name"), json.getBoolean("sample"))
    }

    fun prepare(uri: Uri): Uri {
        if (uri.scheme == "file" && File(uri.path!!).canonicalFile.parentFile == directory.canonicalFile) return uri
        val file = File(directory, "${UUID.randomUUID()}.pdf")
        try {
            val input = if (uri.scheme == "file") File(uri.path!!).inputStream()
                else context.contentResolver.openInputStream(uri) ?: throw IOException("Cannot open PDF")
            input.use { source -> file.outputStream().use { target ->
                source.copyTo(target)
                target.fd.sync()
            } }
            return Uri.fromFile(file)
        } catch (e: Exception) {
            file.delete()
            throw e
        }
    }

    fun commit(uri: Uri, name: String, isSample: Boolean) {
        val file = File(uri.path!!)
        require(file.canonicalFile.parentFile == directory.canonicalFile && file.isFile)
        val bytes = JSONObject().put("file", file.name).put("name", name)
            .put("sample", isSample).toString().toByteArray(Charsets.UTF_8)
        val stream = manifest.startWrite()
        try {
            stream.write(bytes)
            manifest.finishWrite(stream)
        } catch (e: Exception) {
            manifest.failWrite(stream)
            throw e
        }
        // Only files owned by this store; never touch external originals.
        directory.listFiles()?.filter { it.extension == "pdf" && it != file }?.forEach { it.delete() }
    }

    fun discardUncommitted(uri: Uri) {
        if (uri.scheme != "file") return
        val file = File(uri.path ?: return)
        if (file.canonicalFile.parentFile == directory.canonicalFile && read()?.uri != uri) file.delete()
    }
}
