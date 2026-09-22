package com.takumayamada22.pdfutility.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File

class ThumbnailProvider(private val context: Context) {
    private val renderers = mutableMapOf<Uri, PdfRenderer>()
    private val pfds = mutableMapOf<Uri, ParcelFileDescriptor>()

    fun getThumbnail(uri: Uri, pageIndex: Int, width: Int): Bitmap? {
        val renderer = getRenderer(uri) ?: return null
        if (pageIndex !in 0 until renderer.pageCount) return null

        val page = renderer.openPage(pageIndex)
        val height = (width * page.height / page.width)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        
        bitmap.eraseColor(Color.WHITE)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        return bitmap
    }

    fun getPageCount(uri: Uri): Int = getRenderer(uri)?.pageCount ?: 0

    private fun getRenderer(uri: Uri): PdfRenderer? {
        renderers[uri]?.let { return it }
        
        return try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            if (pfd != null) {
                val renderer = PdfRenderer(pfd)
                pfds[uri] = pfd
                renderers[uri] = renderer
                renderer
            } else null
        } catch (e: Exception) {
            null
        }
    }

    fun close() {
        renderers.values.forEach { it.close() }
        pfds.values.forEach { it.close() }
        renderers.clear()
        pfds.clear()
    }
    
    // Legacy support or internal use
    fun open(uri: Uri) = getRenderer(uri)
}
