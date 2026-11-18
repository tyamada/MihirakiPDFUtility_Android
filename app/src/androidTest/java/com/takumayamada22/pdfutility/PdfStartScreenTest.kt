package com.takumayamada22.pdfutility

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.takumayamada22.pdfutility.ui.components.PdfStartScreen
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PdfStartScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun smallScreenLargeTextKeepsEveryStartActionReachable() {
        val clicks = mutableListOf<String>()
        lateinit var labels: List<String>
        compose.setContent {
            val context = LocalContext.current
            labels = listOf(R.string.start_open_pdf, R.string.menu_open_folder, R.string.sample_pdf, R.string.action_add_pdf).map { context.getString(it) }
            val density = LocalDensity.current
            MaterialTheme {
                CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                    Box(Modifier.width(320.dp).height(480.dp)) {
                        PdfStartScreen(false, { clicks.add("open") }, { clicks.add("folder") }, { clicks.add("sample") }, { clicks.add("append") })
                    }
                }
            }
        }
        labels.forEach { compose.onNodeWithText(it).performScrollTo().assertIsDisplayed().performClick() }
        compose.runOnIdle { assertEquals(listOf("open", "folder", "sample", "append"), clicks) }
    }

    @Test fun busyStateDisablesStartingAnotherOperation() {
        lateinit var labels: List<String>
        compose.setContent {
            val context = LocalContext.current
            labels = listOf(R.string.start_open_pdf, R.string.menu_open_folder, R.string.sample_pdf, R.string.action_add_pdf).map { context.getString(it) }
            MaterialTheme { PdfStartScreen(true, {}, {}, {}, {}) }
        }
        labels.forEach { compose.onNodeWithText(it).assertIsNotEnabled() }
    }
}
