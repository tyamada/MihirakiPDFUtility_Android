package com.takumayamada22.pdfutility

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.takumayamada22.pdfutility.util.DiagnosticTestManager
import com.takumayamada22.pdfutility.util.TestPdfGenerator
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DiagnosticBenchmarkTest {

    @Test
    fun testTestPdfGeneratorCreationAndCleanup() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Test functional PDF creation
        val functionalPdf = TestPdfGenerator.createFunctionalTestPdf(context)
        assertNotNull("Functional test PDF should be created", functionalPdf)
        assertTrue("Functional test PDF file should exist", functionalPdf!!.exists())
        assertTrue("Functional test PDF should have size > 0", functionalPdf.length() > 0)

        // Test search PDF creation
        val searchPdf = TestPdfGenerator.createSearchTestPdf(context)
        assertNotNull("Search test PDF should be created", searchPdf)
        assertTrue("Search test PDF file should exist", searchPdf!!.exists())
        assertTrue("Search test PDF should have size > 0", searchPdf.length() > 0)

        // Cleanup
        TestPdfGenerator.cleanupTestPdfs(context)
        assertFalse("Functional test PDF should be deleted after cleanup", functionalPdf.exists())
        assertFalse("Search test PDF should be deleted after cleanup", searchPdf.exists())
    }

    @Test
    fun testDiagnosticBenchmarkExecution() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        val result = DiagnosticTestManager.runDiagnosticBenchmark(context)
        assertNotNull("Diagnostic benchmark result should not be null", result)
        assertNotNull("Timestamp should not be null", result.timestamp)
        assertTrue("Available storage should be >= 0", result.availableStorageMb >= 0.0)
        assertNotNull("Functional test status should not be null", result.functionalTestStatus)
        assertNotNull("Search test status should not be null", result.searchTestStatus)
        assertNotNull("Overall status should not be null", result.overallStatus)
        assertNotNull("Details string should not be null", result.details)
        assertTrue("Details string should contain header", result.details.contains("MihirakiPDF Utility Advanced Diagnostics"))
    }
}
