package com.takumayamada22.pdfutility

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.takumayamada22.pdfutility.ui.MainScreen
import com.takumayamada22.pdfutility.ui.theme.MihirakiPDFUtilityTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MihirakiPDFUtilityTheme {
                MainScreen()
            }
        }
    }
}
