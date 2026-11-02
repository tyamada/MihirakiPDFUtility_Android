package com.takumayamada22.pdfutility.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AppLoggerTest {
    @Test
    fun `persisted timestamps use ASCII digits regardless of device language`() {
        val previous = Locale.getDefault()
        try {
            for (locale in listOf(Locale.US, Locale.JAPAN, Locale.forLanguageTag("ar-EG"))) {
                Locale.setDefault(locale)
                val format = newLogDateFormat().apply { timeZone = TimeZone.getTimeZone("UTC") }
                assertEquals("1970-01-01 00:00:00", format.format(Date(0)))
                assertEquals(Date(0), format.parse("1970-01-01 00:00:00"))
            }
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test
    fun `existing timestamp remains parseable after switching language`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.JAPAN)
            val writer = newLogDateFormat().apply { timeZone = TimeZone.getTimeZone("UTC") }
            val timestamp = writer.format(Date(0))
            Locale.setDefault(Locale.forLanguageTag("ar-EG"))
            val reader = newLogDateFormat().apply { timeZone = TimeZone.getTimeZone("UTC") }
            assertEquals(Date(0), reader.parse(timestamp))
        } finally {
            Locale.setDefault(previous)
        }
    }
}
