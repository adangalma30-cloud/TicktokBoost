package com.tiktokboost.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.sp

private val DarkScheme = darkColorScheme(
    primary = BrandCyan,
    onPrimary = Color(0xFF00302E),
    primaryContainer = BrandCyan.copy(alpha = 0.14f),
    onPrimaryContainer = BrandCyan,
    secondary = BrandPink,
    onSecondary = Color.White,
    secondaryContainer = BrandPink.copy(alpha = 0.16f),
    onSecondaryContainer = BrandPink,
    tertiary = BrandPink,
    onTertiary = Color.White,
    tertiaryContainer = BrandPink.copy(alpha = 0.16f),
    onTertiaryContainer = BrandPink,
    background = Midnight,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkOutline,
    outlineVariant = DarkOutline.copy(alpha = 0.6f),
    error = AlertRed,
    onError = Color.White,
    errorContainer = AlertRed.copy(alpha = 0.16f),
    onErrorContainer = AlertRed
)

private val LightScheme = lightColorScheme(
    primary = BrandCyanDeep,
    onPrimary = Color.White,
    primaryContainer = BrandCyanDeep.copy(alpha = 0.12f),
    onPrimaryContainer = Color(0xFF00615F),
    secondary = BrandPinkDeep,
    onSecondary = Color.White,
    secondaryContainer = BrandPinkDeep.copy(alpha = 0.10f),
    onSecondaryContainer = BrandPinkDeep,
    tertiary = BrandPinkDeep,
    onTertiary = Color.White,
    tertiaryContainer = BrandPinkDeep.copy(alpha = 0.10f),
    onTertiaryContainer = BrandPinkDeep,
    background = Daylight,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    outlineVariant = LightOutline.copy(alpha = 0.7f),
    error = BrandPinkDeep,
    onError = Color.White,
    errorContainer = BrandPinkDeep.copy(alpha = 0.10f),
    onErrorContainer = Color(0xFF8E1037)
)

private const val NO_LIGATURES = "liga off, clig off, dlig off"

val BoostTypography = Typography(
    displaySmall = TextStyle(fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp, fontFeatureSettings = NO_LIGATURES),
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp, fontFeatureSettings = NO_LIGATURES),
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold, fontFeatureSettings = NO_LIGATURES),
    titleLarge = TextStyle(fontSize = 19.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold, fontFeatureSettings = NO_LIGATURES),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold, fontFeatureSettings = NO_LIGATURES),
    titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold, fontFeatureSettings = NO_LIGATURES),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, fontFeatureSettings = NO_LIGATURES),
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 19.sp, fontFeatureSettings = NO_LIGATURES),
    bodySmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontFeatureSettings = NO_LIGATURES),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.sp, fontFeatureSettings = NO_LIGATURES),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp, fontFeatureSettings = NO_LIGATURES),
    labelSmall = TextStyle(fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp, fontFeatureSettings = NO_LIGATURES)
)

@Composable
fun TikTokBoostTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = BoostTypography,
        content = content
    )
}
