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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.PremiumTier
import com.tiktokboost.app.data.TxType
import com.tiktokboost.app.data.TxStatus
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.PremiumBadge
import com.tiktokboost.app.ui.components.SectionTitle
import kotlin.random.Random

/**
 * Advanced analytics — a Premium perk. Clearly labelled as TickTokBoost
 * platform statistics (never claimed as external-platform data).
 */
@Composable
fun AnalyticsScreen(onBack: () -> Unit, onOpenPremium: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val premium = AppState.premium

    androidx.compose.material3.Scaffold(
        containerColor = cs.background,
        topBar = { com.tiktokboost.app.ui.components.AppTopBar("Analytics", onBack) }
    ) { padding ->
        if (premium == PremiumTier.FREE) {
            Column(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier.size(72.dp).clip(RoundedCornerShape(50))
                        .background(cs.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) { Icon(Icons.Filled.Lock, null, tint = cs.primary, modifier = Modifier.size(30.dp)) }
                Spacer(Modifier.height(14.dp))
                Text("Advanced analytics is a Premium feature", style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.align(Alignment.CenterHorizontally))
                Spacer(Modifier.height(6.dp))
                Text(
                    "Profile views, discovery appearances, boost performance and more — inside TickTokBoost.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(18.dp))
                BrandButton("See Premium plans", modifier = Modifier.width(240.dp), onClick = onOpenPremium)
            }
            return@Scaffold
        }

        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenH)
        ) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                PremiumBadge(tier = premium)
                Spacer(Modifier.width(8.dp))
                Text("TickTokBoost platform statistics", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))

            // deterministic mock platform stats derived from real local signals
            val exchanges = AppState.myExchanges
            val views = remember(exchanges) { 120 + exchanges * 86 + Random(exchanges).nextInt(60) }
            val appearances = remember(exchanges) { 340 + exchanges * 210 + Random(exchanges + 1).nextInt(120) }
            val interactions = AppState.transactions.count { it.type == TxType.FOLLOW }
            val boostPerf = remember(exchanges) { 60 + (exchanges * 3).coerceAtMost(35) }

            StatGrid(
                "Profile views" to "$views",
                "Discovery appearances" to "$appearances",
                "Creator interactions" to "$interactions",
                "Boost performance" to "$boostPerf%"
            )

            Spacer(Modifier.height(16.dp))
            SectionTitle("Trust progression")
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    ProgressRow("Trust score", "${AppState.myTrustScore}/100", AppState.myTrustScore / 100f)
                    Spacer(Modifier.height(10.dp))
                    ProgressRow("Completion rate", "${AppState.myCompletionRate}%", AppState.myCompletionRate / 100f)
                    Spacer(Modifier.height(10.dp))
                    ProgressRow("Profile completion", "${AppState.profileCompleteness}%", AppState.profileCompleteness / 100f)
                }
            }

            Spacer(Modifier.height(16.dp))
            SectionTitle("Activity history")
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    val txs = AppState.transactions
                    val earned = txs.count { it.coins > 0 }
                    val spent = txs.count { it.coins < 0 }
                    val pending = txs.count { it.status == TxStatus.PENDING || it.status == TxStatus.DISPUTED }
                    ProgressRow("Earning events", "$earned", (earned / 20f).coerceAtMost(1f))
                    Spacer(Modifier.height(10.dp))
                    ProgressRow("Spending events", "$spent", (spent / 20f).coerceAtMost(1f))
                    Spacer(Modifier.height(10.dp))
                    ProgressRow("Awaiting confirmation", "$pending", (pending / 10f).coerceAtMost(1f))
                    Spacer(Modifier.height(6.dp))
                    Text(
                        if (premium == PremiumTier.PRO)
                            "Pro analytics: expanded history and deeper breakdowns unlock as you grow."
                        else
                            "Upgrade to Premium Pro for expanded history analytics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatGrid(vararg stats: Pair<String, String>) {
    val cs = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        stats.toList().chunked(2).forEach { rowStats ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowStats.forEach { (label, value) ->
                    BrandCard(Modifier.weight(1f)) {
                        Column(Modifier.padding(14.dp)) {
                            Text(value, style = MaterialTheme.typography.titleLarge, color = cs.primary, fontWeight = FontWeight.ExtraBold)
                            Spacer(Modifier.height(2.dp))
                            Text(label, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                        }
                    }
                }
                if (rowStats.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ProgressRow(label: String, value: String, fraction: Float) {
    val cs = MaterialTheme.colorScheme
    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = cs.onSurface, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.labelMedium, color = cs.primary, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(5.dp))
        androidx.compose.material3.LinearProgressIndicator(
            progress = { fraction.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
            color = cs.primary,
            trackColor = cs.surfaceVariant
        )
    }
}
