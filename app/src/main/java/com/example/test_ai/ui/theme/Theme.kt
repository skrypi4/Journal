package com.example.test_ai.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = OGTI_Blue,
    secondary = OGTI_LightBlue,
    tertiary = Color.White

)

private val LightColorScheme = lightColorScheme(
    primary = OGTI_Blue,
    secondary = OGTI_LightBlue,
    tertiary = OGTI_DarkBlue,
    background = Color(0xFFF5F7FA),
    surface = Color.White
)

@Composable
fun Test_aiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
