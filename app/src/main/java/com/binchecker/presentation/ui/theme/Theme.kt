package com.binchecker.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF2563EB),
    onPrimary = Color.White,
    secondary = Color(0xFFDBEAFE),
    onSecondary = Color(0xFF1F2937),
    background = Color(0xFFF9FAFB),
    onBackground = Color(0xFF1F2937),
    surface = Color.White,
    onSurface = Color(0xFF1F2937),
    error = Color(0xFFDC2626),
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF3B82F6),
    onPrimary = Color.White,
    secondary = Color(0xFF1E3A8A),
    onSecondary = Color.White,
    background = Color(0xFF0F172A),
    onBackground = Color.White,
    surface = Color(0xFF1E293B),
    onSurface = Color.White,
    error = Color(0xFFEF4444),
    onError = Color.White
)

@Composable
fun BINCheckerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
} 