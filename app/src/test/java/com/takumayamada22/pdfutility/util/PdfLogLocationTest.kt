package com.takumayamada22.pdfutility.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PdfLogLocationTest {
    @Test fun `untrusted provider names are never logged`() {
        assertEquals("Document Provider", pdfLogLocation("content", "customer-alice.private.provider", false))
        assertEquals("Document Provider", pdfLogLocation("content", "com.google.android.apps.docs.storage.attacker", false))
        assertEquals("Local Device", pdfLogLocation("file", "alice", false))
        assertEquals("Local Device", pdfLogLocation("content", "com.android.externalstorage.documents", false))
        assertEquals("Sample PDF", pdfLogLocation("content", "private", true))
    }
}
