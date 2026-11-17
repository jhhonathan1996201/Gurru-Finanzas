package com.gurru.finanzas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = EmeraldOnPrimary,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = EmeraldOnContainer,
    secondary = SlateSecondary,
    secondaryContainer = SlateContainer,
    tertiary = GoldAccent,
    error = CoralAlert,
    background = SurfaceLight,
    surface = SurfaceCardLight
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF5CD5B4),
    onPrimary = Color(0xFF00382B),
    primaryContainer = Color(0xFF00513F),
    onPrimaryContainer = EmeraldContainer,
    secondary = Color(0xFFA7CCC0),
    tertiary = Color(0xFFFBBF24),
    error = Color(0xFFF87171),
    background = SurfaceDark,
    surface = SurfaceCardDark
)

@Composable
fun GurruFinanzasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = GurruTypography,
        content = content
    )
}
