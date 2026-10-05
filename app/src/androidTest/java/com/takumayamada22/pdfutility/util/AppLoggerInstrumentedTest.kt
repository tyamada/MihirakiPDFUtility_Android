package com.takumayamada22.pdfutility.util

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppLoggerInstrumentedTest {

    @Test
    fun testLogWritingAndRetrieval() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        AppLogger.log(context, "INFO", "Instrumented test message")
        val logs = AppLogger.getLogs(context)
        assertTrue(logs.contains("Instrumented test message"))
        assertTrue(logs.contains("[INFO]"))
    }
}
