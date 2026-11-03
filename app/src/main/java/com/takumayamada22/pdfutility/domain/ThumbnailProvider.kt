package com.takumayamada22.pdfutility.domain

import androidx.core.graphics.createBitmap
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.LruCache
import android.util.Log
import java.io.File

class ThumbnailProvider(private val context: Context) {
    private val renderers = mutableMapOf<Uri, PdfRenderer>()
    private val pfds = mutableMapOf<Uri, ParcelFileDescriptor>()
    private val unlockedCopies = mutableMapOf<Uri, File>()

    /** PdfRenderer on older Android versions cannot open password-protected PDFs.
     * Keep a decrypted rendering copy only in private cache, without retaining passwords.
     */
    fun prepare(uri: Uri, password: String?) {
        renderers.remove(uri)?.close()
        pfds.remove(uri)?.close()
        unlockedCopies.remove(uri)?.delete()
        memoryCache.evictAll()
        if (password == null) return
        val copy = File.createTempFile("pdf-render-", ".pdf", context.cacheDir)
        try {
            val stream = if (uri.scheme == "file") File(uri.path!!).inputStream()
                else context.contentResolver.openInputStream(uri)
                    ?: throw java.io.IOException("Cannot open PDF")
            stream.use { input ->
                com.tom_roush.pdfbox.pdmodel.PDDocument.load(input, password,
                    com.tom_roush.pdfbox.io.MemoryUsageSetting.setupTempFileOnly()).use { doc ->
                    doc.isAllSecurityToBeRemoved = true
                    doc.save(copy)
                }
            }
            unlockedCopies[uri] = copy
        } catch (e: Exception) {
            copy.delete()
            throw e
        }
    }
    
    private val cacheSize = 50 // max 50 thumbnails in memory cache
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun entryRemoved(expired: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
            // Bitmaps are managed by cache eviction and GC. Do not call recycle() here 
            // because Compose UI pages (PageState.thumbnail) may still hold references to evicted bitmaps.
        }
    }

    fun getThumbnail(uri: Uri, pageIndex: Int, width: Int): Bitmap? {
        val cacheKey = "$uri-$pageIndex-$width"
        memoryCache.get(cacheKey)?.let { return it }

        val renderer = getRenderer(uri) ?: return null
        if (pageIndex !in 0 until renderer.pageCount) return null

        return try {
            val page = renderer.openPage(pageIndex)
            val bitmap = try {
                val height = (width * page.height / page.width)
                if (width <= 0 || height <= 0) {
                    return null
                }
                val bmp = createBitmap(width, height, Bitmap.Config.ARGB_8888)
                bmp.eraseColor(Color.WHITE)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bmp
            } finally {
                try { page.close() } catch (_: Exception) {}
            }

            memoryCache.put(cacheKey, bitmap)
            bitmap
        } catch (e: Exception) {
            Log.e("ThumbnailProvider", "Failed to render thumbnail for $uri page $pageIndex", e)
            null
        }
    }

    fun getPageCount(uri: Uri): Int = getRenderer(uri)?.pageCount ?: 0

    private fun getRenderer(uri: Uri): PdfRenderer? {
        renderers[uri]?.let { return it }
        
        return try {
            val unlocked = unlockedCopies[uri]
            val pfd = if (unlocked != null) {
                ParcelFileDescriptor.open(unlocked, ParcelFileDescriptor.MODE_READ_ONLY)
            } else if (uri.scheme == "file" && uri.path != null) {
                ParcelFileDescriptor.open(File(uri.path!!), ParcelFileDescriptor.MODE_READ_ONLY)
            } else {
                context.contentResolver.openFileDescriptor(uri, "r")
            }
            if (pfd != null) {
                val renderer = PdfRenderer(pfd)
                if (renderers.size >= 5) {
                    val oldestKey = renderers.keys.firstOrNull()
                    if (oldestKey != null) {
                        renderers.remove(oldestKey)?.close()
                        pfds.remove(oldestKey)?.close()
                    }
                }
                pfds[uri] = pfd
                renderers[uri] = renderer
                renderer
            } else null
        } catch (e: Exception) {
            Log.e("ThumbnailProvider", "Failed to open renderer for $uri", e)
            null
        }
    }

    fun close() {
        memoryCache.evictAll()
        renderers.values.forEach { 
            try { it.close() } catch (_: Exception) {} 
        }
        pfds.values.forEach { 
            try { it.close() } catch (_: Exception) {} 
        }
        renderers.clear()
        pfds.clear()
        unlockedCopies.values.forEach { it.delete() }
        unlockedCopies.clear()
    }
    
    // Legacy support or internal use
    fun open(uri: Uri) = getRenderer(uri)
}
