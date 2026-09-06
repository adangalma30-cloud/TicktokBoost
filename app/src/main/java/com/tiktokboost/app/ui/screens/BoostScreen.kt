package com.tiktokboost.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.BoostTier
import com.tiktokboost.app.data.EconomyConfig
import com.tiktokboost.app.data.EconomyResult
import com.tiktokboost.app.data.PremiumTier
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.BoostChip
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.CoinIcon
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.PremiumBadge
import com.tiktokboost.app.ui.components.SectionTitle
import com.tiktokboost.app.ui.components.SecondaryButton
import com.tiktokboost.app.ui.theme.SigGradientEnd
import com.tiktokboost.app.ui.theme.SigGradientStart

/**
 * Boost Visibility hub: spend coins on Standard / Boosted / Featured tiers,
 * manage active boost, and jump into Premium. Boosts never guarantee
 * followers — they only increase visibility inside TickTokBoost.
 */
@Composable
fun BoostScreen(onOpenPremium: () -> Unit, onOpenAnalytics: () -> Unit) {
    var toast by remember { mutableStateOf<String?>(null) }
    val cs = MaterialTheme.colorScheme

    LaunchedEffect(toast) {
        if (toast != null) { kotlinx.coroutines.delay(3200); toast = null }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.screenH)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("Boost Visibility 🚀", style = MaterialTheme.typography.headlineMedium, color = cs.onBackground)
        Text(
            "Spend coins to stand out in TickTokBoost discovery. No follower guarantees — just visibility.",
            style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant
        )

        Spacer(Modifier.height(14.dp))

        // ── active boost status ─────────────────────────────────────────
        AppState.activeBoost?.let { (tier, until) ->
            BrandCard {
                Row(Modifier.padding(Dimens.card), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(46.dp).clip(RoundedCornerShape(14.dp))
                            .background(Brush.linearGradient(listOf(SigGradientStart, SigGradientEnd))),
                        contentAlignment = Alignment.Center
                    ) { Text("🚀", fontSize = 20.sp) }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("${tier.label} boost active", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                        Text(
                            "Ends in ${"%.1f".format((until - System.currentTimeMillis()) / 3_600_000f)}h",
                            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                        )
                    }
                    BoostChip(tier = tier, msRemaining = until - System.currentTimeMillis())
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        toast?.let {
            Text(it, color = cs.secondary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
        }

        // ── tiers ────────────────────────────────────────────────────────
        val successHaptic = com.tiktokboost.app.ui.components.rememberSuccessHaptic()
        BoostTier.entries.forEach { tier ->
            BoostTierCard(tier = tier) { res ->
                if (res is EconomyResult.Success) successHaptic()
                toast = when (res) {
                    is EconomyResult.Success -> res.message ?: "Boost activated!"
                    is EconomyResult.Failure -> AppState.detailFor(res.reason)
                }
            }
            Spacer(Modifier.height(10.dp))
        }

        Text(
            "Boost limits: ${EconomyService_boostLimitLabel()} active at a time on your plan.",
            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
        )

        Spacer(Modifier.height(18.dp))
        SectionTitle("Go further")

        // ── premium entry ────────────────────────────────────────────────
        BrandCard(onClick = onOpenPremium) {
            Row(Modifier.padding(Dimens.card), verticalAlignment = Alignment.CenterVertically) {
                Text("💎", fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("TickTokBoost Premium", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                    Text("Priority discovery, analytics, badges & more", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
                Text("→", color = cs.primary, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))

        // ── analytics entry (premium perk) ──────────────────────────────
        BrandCard(onClick = onOpenAnalytics) {
            Row(Modifier.padding(Dimens.card), verticalAlignment = Alignment.CenterVertically) {
                Text("📊", fontSize = 24.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Advanced analytics",
                            style = MaterialTheme.typography.titleSmall, color = cs.onSurface,
                            maxLines = 1,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(Modifier.width(6.dp))
                        PremiumBadge(tier = PremiumTier.PREMIUM, compact = true)
                    }
                    Text("Profile views, discovery appearances, boost performance", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                }
                Text("→", color = cs.primary, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

private fun EconomyService_boostLimitLabel(): String =
    when (com.tiktokboost.app.data.PremiumTier.entries.firstOrNull { it.name == com.tiktokboost.app.data.Session.premiumTier } ?: PremiumTier.FREE) {
        PremiumTier.FREE -> "${EconomyConfig.BOOST_LIMIT_FREE} boost"
        PremiumTier.PREMIUM -> "${EconomyConfig.BOOST_LIMIT_PREMIUM} boosts"
        PremiumTier.PRO -> "${EconomyConfig.BOOST_LIMIT_PRO} boosts"
    }

@Composable
private fun BoostTierCard(tier: BoostTier, onResult: (EconomyResult) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val featured = tier == BoostTier.FEATURED
    BrandCard {
        Column(Modifier.padding(Dimens.card)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // growth motif: rising bars
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.size(width = 40.dp, height = 30.dp)) {
                    listOf(8, 14, 22).forEach { h ->
                        Box(
                            Modifier
                                .width(8.dp)
                                .height(h.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (featured) Brush.linearGradient(listOf(SigGradientStart, SigGradientEnd))
                                    else Brush.linearGradient(listOf(cs.primary.copy(alpha = 0.6f), cs.primary))
                                )
                        )
                        Spacer(Modifier.width(3.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(tier.label, style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                    Text(
                        when (tier) {
                            BoostTier.STANDARD -> "A gentle nudge up in discovery"
                            BoostTier.BOOSTED -> "Clearly boosted placement"
                            BoostTier.FEATURED -> "Spotlight treatment at the top"
                        },
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoinIcon(size = 14.dp)
                    Spacer(Modifier.width(4.dp))
                    Text("${tier.cost}", fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                }
            }
            Spacer(Modifier.height(10.dp))
            if (AppState.activeBoost != null) {
                SecondaryButton("Boost active — ${EconomyConfig.BOOST_DURATION_HOURS}h max", enabled = false, onClick = {}, modifier = Modifier.fillMaxWidth())
            } else {
                BrandButton(
                    "Activate ${tier.label} · ${EconomyConfig.BOOST_DURATION_HOURS}h",
                    modifier = Modifier.fillMaxWidth()
                ) { onResult(AppState.purchaseBoost(tier)) }
            }
        }
    }
}
