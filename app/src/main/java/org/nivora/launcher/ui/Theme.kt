package org.nivora.launcher.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NivoraDarkColors = darkColorScheme(
    primary = Color(0xFF66D9E8),
    onPrimary = Color(0xFF10141D),
    secondary = Color(0xFFC8B7FF),
    onSecondary = Color(0xFF1B1830),
    background = Color(0xFF10141D),
    onBackground = Color(0xFFF4F6FA),
    surface = Color(0xFF192333),
    onSurface = Color(0xFFF4F6FA),
    surfaceVariant = Color(0xFF2A3342),
    onSurfaceVariant = Color(0xFFB8C1CF)
)

@Composable
fun NivoraTheme(content: @Composable () -> Unit) {
    // Foundation milestone deliberately starts with a coherent dark theme.
    // Theme switching is planned as a later milestone; system mode is retained
    // here so the design system can be extended without changing screen code.
    val colorScheme = if (isSystemInDarkTheme()) NivoraDarkColors else NivoraDarkColors
    MaterialTheme(colorScheme = colorScheme, content = content)
}
