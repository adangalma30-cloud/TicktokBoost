package com.tiktokboost.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TikTokColorScheme = darkColorScheme(
    primary = TikTokPink,
    onPrimary = Color.White,
    secondary = TikTokCyan,
    onSecondary = Color.Black,
    tertiary = AccentPurple,
    background = TikTokBg,
    onBackground = TextPrimary,
    surface = CardBg,
    onSurface = TextPrimary,
    surfaceVariant = ChipBg,
    onSurfaceVariant = TextSecondary,
    error = TikTokPink
)

@Composable
fun TikTokBoostTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TikTokColorScheme,
        content = content
    )
}
