package com.takumayamada22.pdfutility

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.takumayamada22.pdfutility.ui.MainScreen
import com.takumayamada22.pdfutility.ui.PdfViewModel
import com.takumayamada22.pdfutility.ui.theme.MihirakiPDFUtilityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        androidx.core.view.WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars =
            (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) != android.content.res.Configuration.UI_MODE_NIGHT_YES
        val initialUri = intent?.data
        setContent {
            MihirakiPDFUtilityTheme {
                val context = LocalContext.current
                val application = context.applicationContext as Application
                val viewModel: PdfViewModel = viewModel(factory = PdfViewModel.provideFactory(application))
                LaunchedEffect(initialUri) {
                    initialUri?.let { viewModel.loadPdf(it) }
                }
                MainScreen(viewModel)
            }
        }
    }
}
