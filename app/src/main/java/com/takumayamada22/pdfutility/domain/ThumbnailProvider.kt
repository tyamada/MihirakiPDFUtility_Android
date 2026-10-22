package com.takumayamada22.pdfutility.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.LruCache
import android.util.Log
import java.io.File
import java.io.IOException

class ThumbnailProvider(private val context: Context) {
    private val renderers = mutableMapOf<Uri, PdfRenderer>()
    private val pfds = mutableMapOf<Uri, ParcelFileDescriptor>()
    
    private val cacheSize = 50 // max 50 thumbnails in memory cache
    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun entryRemoved(expired: Boolean, key: String, oldValue: Bitmap, newValue: Bitmap?) {
            try {
                if (!oldValue.isRecycled) {
                    oldValue.recycle()
                }
            } catch (_: Exception) {}
        }
    }

    fun getThumbnail(uri: Uri, pageIndex: Int, width: Int): Bitmap? {
        val cacheKey = "$uri-$pageIndex-$width"
        memoryCache.get(cacheKey)?.let { return it }

        val renderer = getRenderer(uri) ?: throw IOException("Failed to open PdfRenderer for $uri")
        if (pageIndex !in 0 until renderer.pageCount) {
            throw IndexOutOfBoundsException("Page index $pageIndex out of bounds for page count ${renderer.pageCount}")
        }

        try {
            val page = renderer.openPage(pageIndex)
            val height = (width * page.height / page.width)
            if (width <= 0 || height <= 0) {
                page.close()
                throw IllegalArgumentException("Invalid thumbnail dimensions: ${width}x$height")
            }
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            
            bitmap.eraseColor(Color.WHITE)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            // Check if rendered bitmap is completely blank white (detect render failure / white screen error)
            if (isBitmapBlank(bitmap)) {
                Log.e("ThumbnailProvider", "Thumbnail render error: page $pageIndex of $uri rendered as blank white screen.")
                throw IOException("Thumbnail rendering failed: page $pageIndex rendered as blank white screen.")
            }

            memoryCache.put(cacheKey, bitmap)
            return bitmap
        } catch (e: Exception) {
            Log.e("ThumbnailProvider", "Failed to render thumbnail for $uri page $pageIndex", e)
            if (e is IOException) throw e
            throw IOException("Thumbnail rendering failed for page $pageIndex: ${e.message}", e)
        }
    }

    private fun isBitmapBlank(bitmap: Bitmap): Boolean {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= 0 || h <= 0) return true
        val samplePoints = intArrayOf(
            bitmap.getPixel(w / 2, h / 2),
            bitmap.getPixel(w / 4, h / 4),
            bitmap.getPixel(3 * w / 4, 3 * h / 4),
            bitmap.getPixel(w / 2, h / 4),
            bitmap.getPixel(w / 2, 3 * h / 4)
        )
        return samplePoints.all { it == Color.WHITE || it == Color.TRANSPARENT }
    }

    fun getPageCount(uri: Uri): Int = getRenderer(uri)?.pageCount ?: 0

    private fun getRenderer(uri: Uri): PdfRenderer? {
        renderers[uri]?.let { return it }
        
        return try {
            val pfd = if (uri.scheme == "file" && uri.path != null) {
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
    }
    
    // Legacy support or internal use
    fun open(uri: Uri) = getRenderer(uri)
}
