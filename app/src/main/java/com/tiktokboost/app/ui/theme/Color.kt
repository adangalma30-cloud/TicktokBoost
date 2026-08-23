package com.tiktokboost.app.ui.theme

import androidx.compose.ui.graphics.Color

// ---- brand accents ------------------------------------------------------------
// TikTokBoost identity: energetic cyan + signal pink over a premium indigo base.
val BrandCyan = Color(0xFF25F4EE)
val BrandCyanDeep = Color(0xFF0BB4C0)      // cyan that stays readable on light surfaces
val BrandPink = Color(0xFFFE2C55)
val BrandIndigo = Color(0xFF4A63F5)
val BrandIndigoSoft = Color(0xFF7B8CF5)
val BrandViolet = Color(0xFF8B5CF6)

// ---- dark scheme surfaces -------------------------------------------------------
val Midnight = Color(0xFF0B0E1A)           // app background (dark)
val DarkSurface = Color(0xFF141929)        // cards (dark)
val DarkSurfaceVariant = Color(0xFF1E2438) // chips / inputs (dark)
val DarkOutline = Color(0xFF2A3149)
val DarkTextPrimary = Color(0xFFF2F4FA)
val DarkTextSecondary = Color(0xFF9AA3B8)

// ---- light scheme surfaces ------------------------------------------------------
val Daylight = Color(0xFFF6F8FD)           // app background (light)
val LightSurface = Color(0xFFFFFFFF)       // cards (light)
val LightSurfaceVariant = Color(0xFFEDF1FB)// chips / inputs (light)
val LightOutline = Color(0xFFDDE4F2)
val LightTextPrimary = Color(0xFF171A26)
val LightTextSecondary = Color(0xFF5A6172)

// ---- semantic ------------------------------------------------------------------
val CoinGold = Color(0xFFFFC233)
val CoinGoldDeep = Color(0xFFF5A623)
val GoodGreen = Color(0xFF2ECC71)
val WarnAmber = Color(0xFFFFB020)
val AlertRed = Color(0xFFFF5A6E)
val InfoBlue = Color(0xFF3E8BFF)

// ---- trust ladder ---------------------------------------------------------------
val TrustL1 = Color(0xFF8E99B4)
val TrustL2 = Color(0xFF25C9D0)
val TrustL3 = Color(0xFF2ECC71)
val TrustL4 = Color(0xFF8B5CF6)
val TrustL5 = Color(0xFFFFB020)

// legacy aliases (still referenced by CI-checked builds; theme-aware code should
// prefer MaterialTheme.colorScheme)
val TikTokCyan = BrandCyan
val TikTokPink = BrandPink
val AccentPurple = BrandViolet
val TikTokBg = Midnight
val CardBg = DarkSurface
val ChipBg = DarkSurfaceVariant
val TextPrimary = DarkTextPrimary
val TextSecondary = DarkTextSecondary
