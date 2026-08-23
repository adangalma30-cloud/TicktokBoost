package com.tiktokboost.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.EconomyConfig
import com.tiktokboost.app.data.EconomyResult
import com.tiktokboost.app.data.PremiumPlan
import com.tiktokboost.app.data.PremiumTier
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.PremiumBadge
import com.tiktokboost.app.ui.components.SecondaryButton
import com.tiktokboost.app.ui.theme.SigGradientEnd
import com.tiktokboost.app.ui.theme.SigGradientStart

/**
 * Premium upgrade page: benefits, badge preview, feature comparison,
 * monthly/yearly plans and a MOCK payment flow (no real payments in v0.0.3).
 */
@Composable
fun PremiumScreen(onBack: () -> Unit) {
    var selectedPlan by remember { mutableStateOf(PremiumPlan.MONTHLY) }
    var processingTier by remember { mutableStateOf<PremiumTier?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }
    val cs = MaterialTheme.colorScheme

    fun price(tier: PremiumTier, plan: PremiumPlan): String {
        val cents = when (tier) {
            PremiumTier.PREMIUM ->
                if (plan == PremiumPlan.MONTHLY) EconomyConfig.PREMIUM_MONTHLY_CENTS else EconomyConfig.PREMIUM_YEARLY_CENTS
            else ->
                if (plan == PremiumPlan.MONTHLY) EconomyConfig.PRO_MONTHLY_CENTS else EconomyConfig.PRO_YEARLY_CENTS
        }
        return "$${cents / 100}.${"%02d".format(cents % 100)}"
    }

    LaunchedEffect(toast) { if (toast != null) { kotlinx.coroutines.delay(3000); toast = null } }

    androidx.compose.material3.Scaffold(
        containerColor = cs.background,
        topBar = { com.tiktokboost.app.ui.components.AppTopBar("TickTokBoost Premium", onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenH)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── hero ────────────────────────────────────────────────────
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(SigGradientStart, SigGradientEnd)))
                    .padding(22.dp)
            ) {
                Text("💎", fontSize = 34.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Unlock More With TickTokBoost Premium",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Better tools, visibility inside TickTokBoost and analytics — never guaranteed followers.",
                    color = Color.White.copy(alpha = 0.92f),
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PremiumBadge(tier = PremiumTier.PREMIUM)
                    Spacer(Modifier.width(8.dp))
                    PremiumBadge(tier = PremiumTier.PRO)
                }
            }

            toast?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = cs.secondary, fontWeight = FontWeight.Bold, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(16.dp))

            // ── plan selector ───────────────────────────────────────────
            Text("Choose a plan", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PlanCard("Monthly", selectedPlan == PremiumPlan.MONTHLY, Modifier.weight(1f)) { selectedPlan = PremiumPlan.MONTHLY }
                PlanCard("Yearly · save 50%", selectedPlan == PremiumPlan.YEARLY, Modifier.weight(1.4f)) { selectedPlan = PremiumPlan.YEARLY }
            }

            Spacer(Modifier.height(16.dp))

            // ── feature comparison ─────────────────────────────────────
            Text("Compare plans", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            Spacer(Modifier.height(8.dp))
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    ComparisonHeader()
                    ComparisonRow("Creator Discovery", true, true, true)
                    ComparisonRow("Standard Visibility", true, true, true)
                    ComparisonRow("Priority Discovery", false, true, true)
                    ComparisonRow("Advanced Analytics", false, true, true)
                    ComparisonRow("Premium Badge", false, true, true)
                    ComparisonRow("Advanced Filters", false, true, true)
                    ComparisonRow("Premium Customization", false, true, true)
                    ComparisonRow("Pro Analytics", false, false, true)
                    ComparisonRow("Higher Boost Limits", false, false, true)
                    ComparisonRow("Reduced Ads", false, true, true)
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── mock purchase ──────────────────────────────────────────
            if (AppState.premium != PremiumTier.FREE) {
                BrandCard {
                    Column(Modifier.padding(Dimens.card), horizontalAlignment = Alignment.CenterHorizontally) {
                        PremiumBadge(tier = AppState.premium)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "${AppState.premium.label} is active 🎉",
                            style = MaterialTheme.typography.titleSmall, color = cs.onSurface
                        )
                        Text(
                            "Manage your subscription from Settings.",
                            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                        )
                    }
                }
            } else {
                processingTier?.let { tier ->
                    BrandCard {
                        Column(
                            Modifier.padding(Dimens.card + 8.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = cs.primary, modifier = Modifier.size(28.dp))
                            Spacer(Modifier.height(10.dp))
                            Text(
                                "Processing mock payment…",
                                style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
                            )
                        }
                    }
                } ?: run {
                    BrandButton(
                        "Upgrade to Premium · ${price(PremiumTier.PREMIUM, selectedPlan)}",
                        modifier = Modifier.fillMaxWidth()
                    ) { processingTier = PremiumTier.PREMIUM }
                    Spacer(Modifier.height(8.dp))
                    SecondaryButton(
                        "Upgrade to Premium Pro · ${price(PremiumTier.PRO, selectedPlan)}",
                        modifier = Modifier.fillMaxWidth()
                    ) { processingTier = PremiumTier.PRO }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    "Mock payment flow for v0.0.3 — no real charge. Real billing arrives in a later version.",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(
                "Premium never grants unlimited coins and never promises external-platform followers.",
                style = MaterialTheme.typography.labelSmall,
                color = cs.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(24.dp))
        }

        // simulate payment completion
        LaunchedEffect(processingTier) {
            val tier = processingTier ?: return@LaunchedEffect
            kotlinx.coroutines.delay(1600)
            val res = AppState.purchasePremium(tier, selectedPlan)
            processingTier = null
            toast = when (res) {
                is EconomyResult.Success -> "${tier.label} activated — enjoy! ✨"
                is EconomyResult.Failure -> AppState.detailFor(res.reason)
            }
        }
    }
}

@Composable
private fun PlanCard(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier
            .clip(shape)
            .background(if (selected) cs.primaryContainer else cs.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .clickable { onClick() }
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold,
            color = if (selected) cs.primary else cs.onSurfaceVariant
        )
    }
}

@Composable
private fun ComparisonHeader() {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.weight(1.2f))
        Text("Free", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant, modifier = Modifier.weight(0.6f), textAlign = TextAlign.Center)
        Text("💎", style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(0.6f))
        Text("👑", style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, modifier = Modifier.weight(0.6f))
    }
    HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.4f))
    Spacer(Modifier.height(4.dp))
}

@Composable
fun ComparisonRow(feature: String, free: Boolean, premium: Boolean, pro: Boolean) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(feature, style = MaterialTheme.typography.bodySmall, color = cs.onSurface, modifier = Modifier.weight(1.2f))
        Cell(free, Modifier.weight(0.6f))
        Cell(premium, Modifier.weight(0.6f))
        Cell(pro, Modifier.weight(0.6f))
    }
}

@Composable
private fun Cell(on: Boolean, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Box(modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Icon(
            if (on) Icons.Filled.Check else Icons.Filled.Close,
            contentDescription = null,
            tint = if (on) cs.primary else cs.outline,
            modifier = Modifier.size(14.dp)
        )
    }
}
