package com.takumayamada22.pdfutility

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.takumayamada22.pdfutility.ui.MainScreen
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.takumayamada22.pdfutility.ui.theme.MihirakiPDFUtilityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val initialUri = intent?.data
        setContent {
            MihirakiPDFUtilityTheme {
                val viewModel: PdfViewModel = viewModel()
                LaunchedEffect(initialUri) {
                    initialUri?.let { viewModel.loadPdf(it) }
                }
                MainScreen(viewModel)
            }
        }
    }
}
