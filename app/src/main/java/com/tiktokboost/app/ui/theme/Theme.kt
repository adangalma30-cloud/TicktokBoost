package com.tiktokboost.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

private val DarkScheme = darkColorScheme(
    primary = BrandPink,
    onPrimary = Color.White,
    primaryContainer = BrandPink.copy(alpha = 0.16f),
    onPrimaryContainer = BrandPink,
    secondary = BrandCyan,
    onSecondary = Color(0xFF00323A),
    secondaryContainer = BrandCyan.copy(alpha = 0.14f),
    onSecondaryContainer = BrandCyan,
    tertiary = BrandIndigoSoft,
    onTertiary = Color.White,
    tertiaryContainer = BrandIndigo.copy(alpha = 0.20f),
    onTertiaryContainer = BrandIndigoSoft,
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
    primary = BrandPink,
    onPrimary = Color.White,
    primaryContainer = BrandPink.copy(alpha = 0.10f),
    onPrimaryContainer = BrandPink,
    secondary = BrandCyanDeep,
    onSecondary = Color.White,
    secondaryContainer = BrandCyanDeep.copy(alpha = 0.12f),
    onSecondaryContainer = Color(0xFF006975),
    tertiary = BrandIndigo,
    onTertiary = Color.White,
    tertiaryContainer = BrandIndigo.copy(alpha = 0.10f),
    onTertiaryContainer = Color(0xFF2A3FBE),
    background = Daylight,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    outlineVariant = LightOutline.copy(alpha = 0.7f),
    error = AlertRed,
    onError = Color.White,
    errorContainer = AlertRed.copy(alpha = 0.10f),
    onErrorContainer = Color(0xFFB02540)
)

/** One type scale for the whole product — weights and sizes stay consistent across screens. */
val BoostTypography = Typography(
    displaySmall = TextStyle(fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp),
    headlineMedium = TextStyle(fontSize = 26.sp, lineHeight = 32.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp),
    headlineSmall = TextStyle(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.Bold),
    titleLarge = TextStyle(fontSize = 19.sp, lineHeight = 26.sp, fontWeight = FontWeight.Bold),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.SemiBold),
    titleSmall = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 22.sp, fontWeight = FontWeight.Normal),
    bodyMedium = TextStyle(fontSize = 13.sp, lineHeight = 19.sp, fontWeight = FontWeight.Normal),
    bodySmall = TextStyle(fontSize = 11.sp, lineHeight = 16.sp, fontWeight = FontWeight.Normal),
    labelLarge = TextStyle(fontSize = 14.sp, lineHeight = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.2.sp),
    labelSmall = TextStyle(fontSize = 10.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium, letterSpacing = 0.3.sp)
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
