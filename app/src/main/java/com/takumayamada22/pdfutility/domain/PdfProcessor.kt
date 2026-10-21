package com.takumayamada22.pdfutility.domain

import com.tom_roush.pdfbox.cos.COSDictionary
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PageLayout
import com.tom_roush.pdfbox.pdmodel.interactive.viewerpreferences.PDViewerPreferences
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import android.net.Uri
import com.takumayamada22.pdfutility.ui.SplitDirection
import java.io.InputStream
import java.io.OutputStream

class PdfProcessor {
    private var document: PDDocument? = null
    private val sourceDocs = mutableListOf<PDDocument>()

    fun load(inputStream: InputStream, password: String? = null): Int {
        close()
        val memoryUsage = com.tom_roush.pdfbox.io.MemoryUsageSetting.setupTempFileOnly()
        document = try {
            val doc = if (password != null) {
                PDDocument.load(inputStream, password, memoryUsage)
            } else {
                PDDocument.load(inputStream, memoryUsage)
            }
            sourceDocs.add(doc)
            doc
        } catch (_: Exception) {
            null
        }
        return document?.numberOfPages ?: 0
    }

    fun createPdfFromImages(imageUris: List<Uri>, context: android.content.Context): Int {
        close()
        val newDoc = PDDocument()
        sourceDocs.add(newDoc)
        for (uri in imageUris) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri) ?: continue
                val bytes = inputStream.readBytes()
                inputStream.close()

                val pdImage = com.tom_roush.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray(newDoc, bytes, uri.toString())
                val imageWidth = pdImage.width.toFloat()
                val imageHeight = pdImage.height.toFloat()

                val page = PDPage(PDRectangle(imageWidth, imageHeight))
                newDoc.addPage(page)

                val contentStream = com.tom_roush.pdfbox.pdmodel.PDPageContentStream(newDoc, page)
                contentStream.drawImage(pdImage, 0f, 0f, imageWidth, imageHeight)
                contentStream.close()
            } catch (e: Exception) {
                // Ignore failed images
            }
        }
        document = newDoc
        return document?.numberOfPages ?: 0
    }

    fun append(inputStream: InputStream, password: String? = null): Int {
        val currentDoc = document ?: return 0
        val memoryUsage = com.tom_roush.pdfbox.io.MemoryUsageSetting.setupTempFileOnly()
        val appendDoc = if (password != null) {
            PDDocument.load(inputStream, password, memoryUsage)
        } else {
            PDDocument.load(inputStream, memoryUsage)
        }
        sourceDocs.add(appendDoc)
        
        val newPageCount = appendDoc.numberOfPages
        for (i in 0 until newPageCount) {
            currentDoc.importPage(appendDoc.getPage(i))
        }
        return currentDoc.numberOfPages
    }

    fun close() {
        document?.close()
        document = null
        sourceDocs.forEach { 
            try { it.close() } catch (_: Exception) {} 
        }
        sourceDocs.clear()
    }

    fun getPageCount(): Int = document?.numberOfPages ?: 0

    fun getMetadata(): Map<String, String> {
        val doc = document ?: return emptyMap()
        val info = doc.documentInformation
        val metadata = mutableMapOf(
            "title" to (info.title ?: ""),
            "author" to (info.author ?: ""),
            "subject" to (info.subject ?: ""),
            "keywords" to (info.keywords ?: ""),
            "version" to doc.version.toString()
        )
        
        val catalog = doc.documentCatalog
        val vp = catalog.viewerPreferences
        metadata["direction"] = vp?.readingDirection ?: "L2R"
        metadata["layout"] = catalog.pageLayout?.stringValue() ?: "SinglePage"
        
        return metadata
    }

    fun save(
        outputStream: OutputStream,
        pages: List<com.takumayamada22.pdfutility.ui.PageState>,
        password: String? = null,
        metadata: Map<String, String>? = null
    ) {
        val sourceDoc = document ?: return
        val newDoc = PDDocument()
        sourceDocs.add(newDoc)
        
        // Apply metadata and viewer preferences
        metadata?.let { meta ->
            val info = PDDocumentInformation()
            info.title = meta["title"]
            info.author = meta["author"]
            info.subject = meta["subject"]
            info.keywords = meta["keywords"]
            newDoc.documentInformation = info

            meta["version"]?.let { verStr ->
                try {
                    val ver = verStr.toFloat()
                    if (newDoc.version < ver) {
                        newDoc.version = ver
                    }
                } catch (_: Exception) {}
            }

            val catalog = newDoc.documentCatalog
            meta["layout"]?.let { layoutStr ->
                try {
                    val layout = PageLayout.fromString(layoutStr)
                    catalog.pageLayout = layout
                    if (layoutStr == "TwoPageLeft" || layoutStr == "TwoPageRight") {
                        if (newDoc.version < 1.5f) {
                            newDoc.version = 1.5f
                        }
                    }
                } catch (_: Exception) {}
            }
            
            var vp = catalog.viewerPreferences
            if (vp == null) {
                vp = PDViewerPreferences(COSDictionary())
                catalog.viewerPreferences = vp
            }
            
            meta["direction"]?.let { dir ->
                try { vp.readingDirection = if (dir == "R2L") PDViewerPreferences.READING_DIRECTION_R2L else PDViewerPreferences.READING_DIRECTION_L2R } catch (_: Exception) {}
            }
        }

        pages.forEach { pageState ->
            if (pageState.isBlank) {
                newDoc.addPage(PDPage(PDRectangle.A4))
            } else {
                if (pageState.originalIndex in 0 until sourceDoc.numberOfPages) {
                    val page = sourceDoc.getPage(pageState.originalIndex)
                    val importedPage = newDoc.importPage(page)
                    importedPage.rotation = (page.rotation + pageState.rotation) % 360
                    
                    if (pageState.isSplit) {
                        val mediaBox = importedPage.mediaBox
                        val width = mediaBox.width
                        val height = mediaBox.height
                        
                        if (pageState.splitDirection == SplitDirection.VERTICAL) {
                            if (pageState.splitPart == 0) { // Left
                                importedPage.cropBox = PDRectangle(0f, 0f, width / 2f, height)
                            } else { // Right
                                importedPage.cropBox = PDRectangle(width / 2f, 0f, width, height)
                            }
                        } else {
                            if (pageState.splitPart == 0) { // Top
                                importedPage.cropBox = PDRectangle(0f, height / 2f, width, height)
                            } else { // Bottom
                                importedPage.cropBox = PDRectangle(0f, 0f, width, height / 2f)
                            }
                        }
                    }
                    
                    if (pageState.cropMargin > 0f) {
                        val cb = importedPage.cropBox
                        val w = cb.width
                        val h = cb.height
                        val mx = w * pageState.cropMargin
                        val my = h * pageState.cropMargin
                        importedPage.cropBox = PDRectangle(cb.lowerLeftX + mx, cb.lowerLeftY + my, w - 2*mx, h - 2*my)
                    }
                }
            }
        }

        if (password != null) {
            val ap = AccessPermission()
            val spp = StandardProtectionPolicy(password, password, ap)
            spp.encryptionKeyLength = 256
            newDoc.protect(spp)
        }
        
        newDoc.save(outputStream)
    }

    fun save(outputStream: OutputStream) {
        val doc = document ?: return
        doc.save(outputStream)
    }
}
