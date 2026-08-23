package com.tiktokboost.app.ui.theme

import androidx.compose.ui.graphics.Color

// ═══════════ TickTokBoost identity v0.0.3 — "short-form creator energy" ═══════════
// Near-black foundation · cyan/teal accent · pink/magenta accent · soft white text.
// Signature gradient: cyan → pink (used selectively for CTAs, premium, boosts).

val BrandCyan = Color(0xFF25F4EE)
val BrandCyanDeep = Color(0xFF0AA8B5)      // readable cyan for light surfaces
val BrandPink = Color(0xFFFE2C55)
val BrandPinkDeep = Color(0xFFE51A46)      // readable pink for light surfaces
val BrandIndigoSoft = Color(0xFF7B8CF5)
val BrandViolet = Color(0xFF8B5CF6)

// ---- dark scheme (the flagship TickTokBoost look) --------------------------------
val Midnight = Color(0xFF0A0A12)           // near-black app background
val DarkSurface = Color(0xFF13131F)        // cards
val DarkSurfaceVariant = Color(0xFF1C1C2C) // chips / inputs
val DarkOutline = Color(0xFF2A2A3F)
val DarkTextPrimary = Color(0xFFF5F5FA)
val DarkTextSecondary = Color(0xFF9C9CB0)

// ---- light scheme ------------------------------------------------------------------
val Daylight = Color(0xFFF6F7FB)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFEDEFF7)
val LightOutline = Color(0xFFDDE0EC)
val LightTextPrimary = Color(0xFF15151E)
val LightTextSecondary = Color(0xFF5C5C6E)

// ---- semantic -----------------------------------------------------------------------
val CoinGold = Color(0xFFFFC233)
val CoinGoldDeep = Color(0xFFF5A623)
val GoodGreen = Color(0xFF2ECC71)
val WarnAmber = Color(0xFFFFB020)
val AlertRed = Color(0xFFFF5A6E)
val InfoBlue = Color(0xFF3E8BFF)

// ---- trust ladder ----------------------------------------------------------------------
val TrustL1 = Color(0xFF8E99B4)
val TrustL2 = Color(0xFF25C9D0)
val TrustL3 = Color(0xFF2ECC71)
val TrustL4 = Color(0xFF8B5CF6)
val TrustL5 = Color(0xFFFFB020)

// legacy aliases
val TikTokCyan = BrandCyan
val TikTokPink = BrandPink
val AccentPurple = BrandPink
val TikTokBg = Midnight
val CardBg = DarkSurface
val ChipBg = DarkSurfaceVariant
val TextPrimary = DarkTextPrimary
val TextSecondary = DarkTextSecondary

// ---- signature gradient anchors (deep variants: white text stays readable) -----------
val SigGradientStart = Color(0xFF0AA8B5)   // cyan/teal
val SigGradientEnd = Color(0xFFE51A46)     // pink/magenta
// bright pair — for glow/decor on dark surfaces
val SigGlowStart = BrandCyan
val SigGlowEnd = BrandPink
