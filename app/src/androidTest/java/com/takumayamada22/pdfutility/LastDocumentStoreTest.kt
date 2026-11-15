package com.takumayamada22.pdfutility

import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import com.takumayamada22.pdfutility.util.LastDocumentStore
import com.takumayamada22.pdfutility.util.PreferencesManager
import com.takumayamada22.pdfutility.ui.ActionButtonType
import com.takumayamada22.pdfutility.ui.ButtonDisplayType
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.File
import java.util.UUID

class LastDocumentStoreTest {
    private lateinit var directory: File
    private lateinit var context: Context
    @Before fun setup() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        directory = File(base.cacheDir, "retention-test-${UUID.randomUUID()}").apply { mkdirs() }
        context = object : ContextWrapper(base) {
            override fun getNoBackupFilesDir() = directory
            override fun getSharedPreferences(name: String, mode: Int) =
                super.getSharedPreferences("retention-test-$name", mode)
        }
        context.getSharedPreferences("mihiraki_pdf_prefs", 0).edit().clear().commit()
    }
    @After fun cleanup() {
        directory.deleteRecursively()
        context.getSharedPreferences("mihiraki_pdf_prefs", 0).edit().clear().commit()
    }
    @Test fun copiedSourceSurvivesOriginalRemovalAndNewStoreInstance() {
        val source = File(directory, "source.pdf").apply { writeText("PDF fixture bytes") }
        val store = LastDocumentStore(context)
        val copy = store.prepare(Uri.fromFile(source))
        store.commit(copy, "日本語.pdf", true)
        source.delete()
        val restored = LastDocumentStore(context).read()!!
        assertEquals("日本語.pdf", restored.name)
        assertTrue(restored.isSample)
        assertEquals("PDF fixture bytes", File(restored.uri.path!!).readText())
    }
    @Test fun abandonedOpenDoesNotReplaceLastSuccessfulDocument() {
        val source = File(directory, "source.pdf").apply { writeText("first") }
        val store = LastDocumentStore(context)
        val first = store.prepare(Uri.fromFile(source))
        store.commit(first, "first.pdf", false)
        source.writeText("second")
        val failed = store.prepare(Uri.fromFile(source))
        store.discardUncommitted(failed)
        assertFalse(File(failed.path!!).exists())
        assertEquals(first, LastDocumentStore(context).read()!!.uri)
        assertEquals("first", File(first.path!!).readText())
    }
    @Test fun replaceCleansOnlyOwnedCopiesAndKeepsOriginal() {
        val original = File(directory, "original.pdf").apply { writeText("source") }
        val store = LastDocumentStore(context)
        val old = store.prepare(Uri.fromFile(original))
        store.commit(old, "old.pdf", false)
        val next = store.prepare(Uri.fromFile(original))
        store.commit(next, "new.pdf", false)
        assertFalse(File(old.path!!).exists())
        assertTrue(original.exists())
        assertEquals(next, store.read()!!.uri)
    }
    @Test fun allDisplayModesAndExplicitEmptySelectionPersist() {
        for (mode in ButtonDisplayType.entries) {
            PreferencesManager.setButtonDisplayType(context, mode)
            assertEquals(mode, PreferencesManager.getButtonDisplayType(context))
        }
        val defaults = setOf(ActionButtonType.OPEN, ActionButtonType.SAVE)
        assertEquals(defaults, PreferencesManager.getVisibleActionButtons(context, defaults))
        PreferencesManager.setVisibleActionButtons(context, emptySet())
        assertTrue(PreferencesManager.getVisibleActionButtons(context, defaults).isEmpty())
        PreferencesManager.setVisibleActionButtons(context, setOf(ActionButtonType.SAVE))
        assertEquals(setOf(ActionButtonType.SAVE), PreferencesManager.getVisibleActionButtons(context, defaults))
    }
    @Test fun obsoletePreferenceValuesDoNotCrash() {
        context.getSharedPreferences("mihiraki_pdf_prefs", 0).edit()
            .putString("button_display_type", "REMOVED")
            .putStringSet("visible_action_buttons", setOf("OPEN", "REMOVED")).commit()
        assertEquals(ButtonDisplayType.ICON_ONLY, PreferencesManager.getButtonDisplayType(context))
        assertEquals(setOf(ActionButtonType.OPEN), PreferencesManager.getVisibleActionButtons(context, emptySet()))
    }
}
