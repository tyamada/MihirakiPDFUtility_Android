package com.takumayamada22.pdfutility

import android.content.res.Configuration
import android.os.LocaleList
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.takumayamada22.pdfutility.ui.components.PdfStartScreen
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.io.File

class StartScreenLocalizationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private data class Case(val language: String, val scale: Float, val landscape: Boolean)

    @Test fun additionalLocalesWrapAndKeepStartActionsReachable() {
        val current = mutableStateOf(Case("de", 1f, false))
        val clicks = mutableListOf<String>()
        compose.activity.runOnUiThread { compose.activity.setContent {
            val case = current.value
            val base = LocalContext.current
            val configuration = Configuration(base.resources.configuration).apply {
                setLocales(LocaleList.forLanguageTags(case.language))
            }
            val context = base.createConfigurationContext(configuration)
            val density = LocalDensity.current
            CompositionLocalProvider(LocalContext provides context, LocalConfiguration provides configuration,
                LocalDensity provides Density(density.density, case.scale)) {
                MaterialTheme {
                    Box(Modifier.size(if (case.landscape) 560.dp else 320.dp,
                        if (case.landscape) 260.dp else 480.dp)) {
                        PdfStartScreen(false, { clicks.add("open") }, { clicks.add("folder") },
                            { clicks.add("sample") }, { clicks.add("append") })
                    }
                }
            }
        } }
        for (language in listOf("de", "es", "fr", "id", "it", "ko", "ms", "ne", "pt", "fil", "vi", "zh-CN", "zh-HK", "zh-TW"))
            for (scale in listOf(1f, 2f)) for (landscape in listOf(false, true)) {
                val case = Case(language, scale, landscape)
                compose.runOnIdle { current.value = case; clicks.clear() }
                compose.waitForIdle()
                val config = Configuration(compose.activity.resources.configuration).apply {
                    setLocales(LocaleList.forLanguageTags(language))
                }
                val context = compose.activity.createConfigurationContext(config)
                for (id in listOf(R.string.start_title, R.string.start_description)) {
                    val node = compose.onNodeWithText(context.getString(id))
                    node.performScrollTo().assertIsDisplayed()
                    val layouts = mutableListOf<TextLayoutResult>()
                    node.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
                    assertTrue("Missing text layout: $case", layouts.isNotEmpty())
                    // Text width is rounded to integer pixels; the paragraph can be <1px wider.
                    // Check line extents and ellipsis rather than treating that rounding as clipping.
                    assertTrue("Text clipped: $case text=${context.getString(id)}", layouts.all { layout ->
                        !layout.didOverflowHeight && (0 until layout.lineCount).all { line ->
                            !layout.isLineEllipsized(line) && layout.getLineRight(line) <= layout.size.width + 1f &&
                                layout.getLineLeft(line) >= -1f
                        }
                    })
                }
                for (id in listOf(R.string.start_open_pdf, R.string.menu_open_folder, R.string.sample_pdf, R.string.action_add_pdf)) {
                    compose.onNodeWithText(context.getString(id)).performScrollTo().assertIsDisplayed().performClick()
                }
                compose.runOnIdle { assertEquals("Actions: $case", listOf("open", "folder", "sample", "append"), clicks) }
                if (scale == 1f && !landscape) {
                    compose.onNodeWithText(context.getString(R.string.start_title)).performScrollTo()
                    compose.onRoot().captureToImage().asAndroidBitmap().let { image ->
                        File(compose.activity.getExternalFilesDir(null), "start-$language.png").outputStream().use {
                            image.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
                        }
                    }
                }
            }
    }
}
