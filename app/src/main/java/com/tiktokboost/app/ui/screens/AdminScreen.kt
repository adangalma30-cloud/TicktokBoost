package com.tiktokboost.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.DisputeStatus
import com.tiktokboost.app.data.MockData
import com.tiktokboost.app.data.TxStatus
import com.tiktokboost.app.data.TxType
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.AppTopBar
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.SecondaryButton
import com.tiktokboost.app.ui.components.StatusBadge
import com.tiktokboost.app.ui.components.relativeTime

/**
 * Demo admin dashboard: platform overview (mock) + this device's real ledger
 * + the dispute review queue and abuse watchlist. Helps tune the economy.
 */
@Composable
fun AdminScreen(onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme

    androidx.compose.material3.Scaffold(
        containerColor = cs.background,
        topBar = { AppTopBar("Admin dashboard (demo)", onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenH)
        ) {
            Spacer(Modifier.height(10.dp))

            val txs = AppState.transactions
            val issued = remember(txs.size) { txs.filter { it.coins > 0 }.sumOf { it.coins } }
            val spent = remember(txs.size) { txs.filter { it.coins < 0 }.sumOf { -it.coins } }
            val held = AppState.coins
            val confirmed = txs.count { it.status == TxStatus.VERIFIED }
            val disputed = txs.count { it.status == TxStatus.DISPUTED }
            val confirmationRate = if (confirmed + disputed == 0) 100 else confirmed * 100 / (confirmed + disputed)
            val disputeRate = if (txs.isEmpty()) 0 else disputed * 100 / txs.size
            val today = txs.count { System.currentTimeMillis() - it.createdAt < 86_400_000L }

            // ── users ──────────────────────────────────────────────────
            Section("Users")
            StatGrid4(
                "Total users" to "${MockData.Admin.TOTAL_USERS}",
                "Active (24h)" to "${MockData.Admin.ACTIVE_USERS}",
                "Premium" to "${MockData.Admin.PREMIUM_USERS}",
                "Premium Pro" to "${MockData.Admin.PRO_USERS}"
            )

            Spacer(Modifier.height(14.dp))
            Section("Exchanges")
            StatGrid4(
                "Exchanges" to "${MockData.Admin.MOCK_EXCHANGES}",
                "Confirmed" to "${MockData.Admin.MOCK_CONFIRMED}",
                "Disputed" to "${MockData.Admin.MOCK_DISPUTED}",
                "Confirm rate" to "${MockData.Admin.MOCK_CONFIRMED * 100 / MockData.Admin.MOCK_EXCHANGES}%"
            )

            Spacer(Modifier.height(14.dp))
            Section("Economy analytics")
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    MetricRow("Total coins issued (you)", "+$issued")
                    MetricRow("Total coins spent (you)", "-$spent")
                    MetricRow("Coins currently held", "$held")
                    MetricRow("Average user balance (mock)", "34")
                    MetricRow("Daily coins issued (you)", "$today")
                    MetricRow("Confirmation rate", "$confirmationRate%")
                    MetricRow("Dispute rate", "$disputeRate%")
                }
            }

            Spacer(Modifier.height(14.dp))
            Section("Active boosts")
            AppState.activeBoost?.let { (tier, until) ->
                BrandCard {
                    Row(Modifier.padding(14.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("🚀 ${tier.label}", fontWeight = FontWeight.Bold, color = cs.onSurface, modifier = Modifier.weight(1f))
                        Text("ends in ${"%.1f".format((until - System.currentTimeMillis()) / 3_600_000f)}h", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                }
            } ?: Text("No active boosts.", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)

            Spacer(Modifier.height(14.dp))
            Section("Reports & moderation")
            val openReports = remember { com.tiktokboost.app.data.Session.reports().filter { !it.resolved } }
            if (openReports.isEmpty()) {
                Text("No open reports.", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            } else {
                openReports.forEach { rep ->
                    BrandCard {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Text("@${rep.targetUsername}", fontWeight = FontWeight.Bold, color = cs.onSurface, modifier = Modifier.weight(1f))
                                Text(relativeTime(rep.createdAt), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(rep.reason, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            Spacer(Modifier.height(10.dp))
                            SecondaryButton("Mark reviewed", modifier = Modifier.fillMaxWidth()) {
                                com.tiktokboost.app.data.Session.resolveReport(rep.id)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(14.dp))
            Section("Dispute queue")
            val openDisputes = AppState.disputes.filter { it.status == DisputeStatus.OPEN || it.status == DisputeStatus.UNDER_REVIEW }
            if (openDisputes.isEmpty()) {
                Text("No disputes awaiting review.", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            } else {
                openDisputes.forEach { d ->
                    BrandCard {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                Text("vs @${d.counterpart}", fontWeight = FontWeight.Bold, color = cs.onSurface, modifier = Modifier.weight(1f))
                                Text(relativeTime(d.createdAt), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(d.reason, style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                SecondaryButton("Release coins", modifier = Modifier.weight(1f)) {
                                    AppState.adminResolveDispute(d.id, true)
                                }
                                SecondaryButton("Deny", modifier = Modifier.weight(1f)) {
                                    AppState.adminResolveDispute(d.id, false)
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(8.dp))
            Section("Suspicious activity & review")
            BrandCard {
                Column(Modifier.padding(14.dp)) {
                    MetricRow("Your status", AppState.abuse.label)
                    MetricRow("Account risk", AppState.riskLevel().label)
                    MetricRow("Suspicious flags", "${com.tiktokboost.app.data.Session.suspiciousFlags}")
                    MetricRow("Referrals invited", "${com.tiktokboost.app.data.Session.referralInvited}")
                    if (AppState.abuse != com.tiktokboost.app.data.AbuseStatus.NORMAL) {
                        Spacer(Modifier.height(10.dp))
                        SecondaryButton("Clear flags (admin)", modifier = Modifier.fillMaxWidth()) {
                            AppState.adminClearAbuse()
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Section("Testing controls")
            BrandCard {
                Column(Modifier.padding(14.dp)) {
                    Text(
                        "Reset cooldowns, pairings, daily cap and abuse flags — handy for testing the economy rules without waiting.",
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    SecondaryButton("Reset economy guards", modifier = Modifier.fillMaxWidth()) {
                        AppState.adminResetEconomyGuards()
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun Section(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium, color = cs_title(), modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun cs_title() = MaterialTheme.colorScheme.onSurface

@Composable
private fun StatGrid4(vararg stats: Pair<String, String>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        stats.toList().chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { (label, value) ->
                    BrandCard(Modifier.weight(1f)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(value, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                            Spacer(Modifier.height(2.dp))
                            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
}
