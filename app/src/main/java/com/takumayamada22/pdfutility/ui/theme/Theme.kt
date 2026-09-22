package com.takumayamada22.pdfutility.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable

private val PrimaryColor = Color(0xFF007074)
private val PrimaryDarkColor = Color(0xFF004D40)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryColor,
    secondary = PrimaryDarkColor
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryColor,
    secondary = PrimaryDarkColor
)

@Composable
fun MihirakiPDFUtilityTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
