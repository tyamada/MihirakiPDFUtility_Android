package com.takumayamada22.pdfutility

import com.takumayamada22.pdfutility.ui.suggestEditedPdfFileName
import org.junit.Assert.assertEquals
import org.junit.Test

class PdfSaveFileNameTest {
    @Test fun existingExtensionIsNotDuplicated() {
        assertEquals("edited_book.pdf", suggestEditedPdfFileName("book.pdf"))
    }
    @Test fun uppercaseExtensionIsNormalized() {
        assertEquals("edited_book.pdf", suggestEditedPdfFileName("book.PDF"))
    }
    @Test fun missingExtensionIsAdded() {
        assertEquals("edited_book.pdf", suggestEditedPdfFileName("book"))
    }
    @Test fun repeatedExtensionsAreCollapsed() {
        assertEquals("edited_book.pdf", suggestEditedPdfFileName("book.pdf.PDF"))
    }
    @Test fun embeddedDotsAndUnicodeArePreserved() {
        assertEquals("edited_見開き.v1.pdf", suggestEditedPdfFileName("見開き.v1.pdf"))
    }
    @Test fun missingOrEmptyBaseUsesDefault() {
        for (name in listOf(null, "", ".pdf")) {
            assertEquals("edited_document.pdf", suggestEditedPdfFileName(name))
        }
    }
}
