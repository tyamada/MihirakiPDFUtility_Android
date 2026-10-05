package com.takumayamada22.pdfutility.domain

import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class PdfProcessorCancellationTest {

    @Test
    fun testLoadCancellationHandling() = runBlocking {
        val processor = PdfProcessor()
        val dummyDoc = PDDocument()
        repeat(50) { dummyDoc.addPage(PDPage()) }
        val out = ByteArrayOutputStream()
        dummyDoc.save(out)
        dummyDoc.close()

        val deferred = async(Dispatchers.IO) {
            val input = ByteArrayInputStream(out.toByteArray())
            processor.load(input)
        }

        deferred.cancelAndJoin()
        processor.close()
        assertNotNull(processor)
    }
}
