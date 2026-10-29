// [新增] 应用主题：浅色/深色配色，跟随系统
package com.example.calculator.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Brand,
    onPrimary = Color.White,
    primaryContainer = BrandContainerLight,
    onPrimaryContainer = OnBrandContainerLight,
    background = SurfaceLight,
    onBackground = TextLight,
    surface = SurfaceLight,
    onSurface = TextLight,
    surfaceVariant = Color(0xFFEFEFF2),
    onSurfaceVariant = MutedLight,
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF2F2F5),
    surfaceContainer = Color(0xFFEDEDF0),
    surfaceContainerHigh = Color(0xFFE7E7EA),
    surfaceContainerHighest = Color(0xFFE2E2E6),
    error = ErrorColor,
    onError = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = BrandDark,
    onPrimary = Color.White,
    primaryContainer = BrandContainerDark,
    onPrimaryContainer = OnBrandContainerDark,
    background = SurfaceDark,
    onBackground = TextDark,
    surface = SurfaceDark,
    onSurface = TextDark,
    surfaceVariant = Color(0xFF262626),
    onSurfaceVariant = MutedDark,
    surfaceContainerLowest = Color(0xFF111111),
    surfaceContainerLow = Color(0xFF1D1D1F),
    surfaceContainer = Color(0xFF222225),
    surfaceContainerHigh = Color(0xFF2A2A2D),
    surfaceContainerHighest = Color(0xFF333336),
    error = ErrorColor,
    onError = Color.White,
)

@Composable
fun CalculatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}
