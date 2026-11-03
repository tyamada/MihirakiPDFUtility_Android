package com.takumayamada22.pdfutility

import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.takumayamada22.pdfutility.domain.ThumbnailProvider
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.encryption.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class ThumbnailPasswordCacheTest {
    @Test fun unlockedRenderingCopyIsPrivateTemporaryAndOriginalStaysEncrypted() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val source = File(context.cacheDir, "thumbnail-password-fixture.pdf")
        val password = "temporary-fixture-password"
        PDDocument().use { doc ->
            doc.addPage(PDPage())
            doc.protect(StandardProtectionPolicy(password, password, AccessPermission()).apply { encryptionKeyLength = 128 })
            doc.save(source)
        }
        val original = source.readBytes()
        fun copies() = context.cacheDir.listFiles()!!.filter { it.name.startsWith("pdf-render-") }.toSet()
        val before = copies()
        val provider = ThumbnailProvider(context)
        try {
            try { provider.prepare(Uri.fromFile(source), "wrong"); fail("Wrong password must fail") }
            catch (_: InvalidPasswordException) {}
            assertEquals(before, copies())
            provider.prepare(Uri.fromFile(source), password)
            assertEquals(1, provider.getPageCount(Uri.fromFile(source)))
            assertNotNull(provider.getThumbnail(Uri.fromFile(source), 0, 120))
            val added = copies() - before
            assertEquals(1, added.size)
            assertEquals(context.cacheDir.canonicalFile, added.single().parentFile!!.canonicalFile)
            PDDocument.load(added.single()).use { assertEquals(1, it.numberOfPages); assertFalse(it.isEncrypted) }
            assertArrayEquals(original, source.readBytes())
            PDDocument.load(source, password).use { assertTrue(it.isEncrypted) }
        } finally { provider.close() }
        assertEquals(before, copies())
    }
}
