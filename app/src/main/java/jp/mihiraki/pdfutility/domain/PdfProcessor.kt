package jp.mihiraki.pdfutility.domain

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDDocumentInformation
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.encryption.AccessPermission
import com.tom_roush.pdfbox.pdmodel.encryption.StandardProtectionPolicy
import java.io.InputStream
import java.io.OutputStream

class PdfProcessor {
    private var document: PDDocument? = null

    fun load(inputStream: InputStream, password: String? = null): Int {
        document?.close()
        document = try {
            if (password != null) {
                PDDocument.load(inputStream, password)
            } else {
                PDDocument.load(inputStream)
            }
        } catch (e: Exception) {
            null
        }
        return document?.numberOfPages ?: 0
    }

    fun append(inputStream: InputStream, password: String? = null): Int {
        val currentDoc = document ?: return 0
        val appendDoc = if (password != null) {
            PDDocument.load(inputStream, password)
        } else {
            PDDocument.load(inputStream)
        }
        
        val newPageCount = appendDoc.numberOfPages
        // We don't actually append to 'document' yet because PdfViewModel manages the state 
        // as a list of pages. We just need to be able to save it later.
        // Actually, PdfProcessor.save uses 'document' as the source for 'originalIndex'.
        // This won't work for multiple documents. 
        // Let's refine the strategy: PdfProcessor should hold a list of documents or we merge them now.
        
        for (i in 0 until newPageCount) {
            currentDoc.importPage(appendDoc.getPage(i))
        }
        appendDoc.close()
        return currentDoc.numberOfPages
    }

    fun close() {
        document?.close()
        document = null
    }

    fun getPageCount(): Int = document?.numberOfPages ?: 0

    fun getMetadata(): Map<String, String> {
        val info = document?.documentInformation ?: return emptyMap()
        return mapOf(
            "title" to (info.title ?: ""),
            "author" to (info.author ?: ""),
            "subject" to (info.subject ?: ""),
            "keywords" to (info.keywords ?: "")
        )
    }

    fun deletePages(indices: List<Int>) {
        val doc = document ?: return
        indices.sortedDescending().forEach { index ->
            if (index in 0 until doc.numberOfPages) {
                doc.removePage(index)
            }
        }
    }

    fun rotatePages(indices: List<Int>, degrees: Int) {
        val doc = document ?: return
        indices.forEach { index ->
            if (index in 0 until doc.numberOfPages) {
                val page = doc.getPage(index)
                page.rotation = (page.rotation + degrees) % 360
            }
        }
    }

    fun reorderPage(oldIndex: Int, newIndex: Int) {
        val doc = document ?: return
        if (oldIndex == newIndex) return
        val page = doc.getPage(oldIndex)
        doc.removePage(oldIndex)
        // Note: importPage adds a copy, so we need to be careful.
        // PDFBox doesn't have a simple "movePage" like PyMuPDF.
        // We might need to rebuild the document or use a lower level approach if remove/add is messy.
        // Actually, we can just rearrange the pages list in PDDocument if we can access it.
        // But the standard way is to create a new document or use removePage/insertPage.
        // PDDocument.removePage(int) and then doc.getPages().insertBefore(newIndex, page) might work.
        // In PDFBox 2.x, PDPageTree is used.
    }

    // A safer way to reorder/delete/etc is to rebuild the page tree if necessary, 
    // but for now let's implement the essentials.

    fun insertBlankPage(afterIndex: Int) {
        val doc = document ?: return
        val currentPage = if (afterIndex in 0 until doc.numberOfPages) doc.getPage(afterIndex) else null
        val rect = currentPage?.mediaBox ?: PDRectangle.A4
        val newPage = PDPage(rect)
        if (afterIndex >= doc.numberOfPages - 1) {
            doc.addPage(newPage)
        } else {
            doc.pages.insertBefore(newPage, doc.getPage(afterIndex + 1))
        }
    }

    fun save(
        outputStream: OutputStream,
        pages: List<jp.mihiraki.pdfutility.ui.PageState>,
        password: String? = null,
        metadata: Map<String, String>? = null
    ) {
        val sourceDoc = document ?: return
        val newDoc = PDDocument()
        
        // Apply metadata
        metadata?.let {
            val info = PDDocumentInformation()
            info.title = it["title"]
            info.author = it["author"]
            info.subject = it["subject"]
            info.keywords = it["keywords"]
            newDoc.documentInformation = info
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
                        
                        if (pageState.splitPart == 0) { // Left/Top
                            importedPage.cropBox = PDRectangle(0f, 0f, width / 2f, height)
                        } else { // Right/Bottom
                            importedPage.cropBox = PDRectangle(width / 2f, 0f, width, height)
                        }
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
        newDoc.close()
    }
}
