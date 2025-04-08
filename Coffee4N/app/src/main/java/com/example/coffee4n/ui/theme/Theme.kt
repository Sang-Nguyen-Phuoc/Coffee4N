package com.example.coffee4n.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2E2E2E),
    onPrimary = Color.White,
    secondary = Color.Gray,
    onSecondary = Color.LightGray,
    tertiary = Color.Magenta,
    background = Color.Black,  // App background color
         // Surface container color
//    surfaceVariant = Color.Red.copy(alpha = 0.8f),  // Alternative surface color
//    surfaceTint = Color.Red,  // Used for tinting surfaces for elevation

    // Status bar related colors
    inverseSurface = Color.Black,  // We'll use this for status bar background
    inverseOnSurface = Color.White // Status bar content color
)

@Composable
fun Coffee4NTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content,
    )
}