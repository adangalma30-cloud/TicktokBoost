package com.tiktokboost.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.EconomyConfig
import com.tiktokboost.app.data.EconomyResult
import com.tiktokboost.app.data.ReferralStatus
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.AppTopBar
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.EmptyState
import com.tiktokboost.app.ui.components.SecondaryButton
import com.tiktokboost.app.ui.components.relativeTime
import com.tiktokboost.app.ui.theme.GoodGreen

/**
 * Invite Friends — a PERMANENT, repeatable referral system (not a one-time quest).
 * Code + copy + native share; persistent per-friend records with their own
 * lifecycle; idempotent rewards; daily anti-abuse limit.
 */
@Composable
fun ReferralScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    var toast by remember { mutableStateOf<String?>(null) }
    val cs = MaterialTheme.colorScheme

    val (stats, coinsEarned) = remember { mutableStateOf(Triple(0, 0, 0)) }
    val referralStats = AppState.referralStats()
    val successful = referralStats.second
    val coinsFromReferrals = referralStats.third

    Scaffold(
        containerColor = cs.background,
        topBar = { AppTopBar("Invite Friends", onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenH)
        ) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Invite more creators and earn rewards.",
                style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
            )

            Spacer(Modifier.height(14.dp))

            // ── stats: successful referrals + rewards earned ────────────
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BrandCard(Modifier.weight(1f)) {
                    Column(Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("$successful", style = MaterialTheme.typography.headlineSmall, color = cs.primary, fontWeight = FontWeight.ExtraBold)
                        Text("Successful referrals", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    }
                }
                BrandCard(Modifier.weight(1f)) {
                    Column(Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("+$coinsFromReferrals", style = MaterialTheme.typography.headlineSmall, color = cs.primary, fontWeight = FontWeight.ExtraBold)
                        Text("Rewards earned", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    }
                }
                BrandCard(Modifier.weight(1f)) {
                    Column(Modifier.padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${Session.referralInvited}", style = MaterialTheme.typography.headlineSmall, color = cs.onSurface, fontWeight = FontWeight.ExtraBold)
                        Text("Invites sent", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── code + actions ─────────────────────────────────────────
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    Text("Your referral code", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        Session.referralCode(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = cs.primary, fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(Session.referralLink(), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Row {
                        SecondaryButton("Copy code", modifier = Modifier.weight(1f), onClick = {
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("TickTokBoost referral code", Session.referralCode()))
                            copied = true
                        })
                        Spacer(Modifier.width(8.dp))
                        BrandButton("Invite Friend", modifier = Modifier.weight(1.3f), onClick = {
                            Session.referralInvited = Session.referralInvited + 1
                            openShareSheet(context)
                        })
                    }
                    if (copied) {
                        Spacer(Modifier.height(6.dp))
                        Text("Code copied ✓", style = MaterialTheme.typography.labelMedium, color = cs.primary, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Reward: +${EconomyConfig.REFERRAL_REWARD} coins per qualified referral · up to ${EconomyConfig.REFERRAL_DAILY_REWARD_LIMIT}/day.",
                        style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
                    )
                }
            }

            toast?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.labelMedium, color = cs.primary, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(14.dp))

            // ── referral records ───────────────────────────────────────
            Text("Your referrals", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            Spacer(Modifier.height(8.dp))
            if (AppState.referrals.isEmpty()) {
                BrandCard {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 24.dp, horizontal = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💌", fontSize = 30.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("No referrals yet", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                        Text(
                            "Invite a friend — when they join and complete their first confirmed exchange, you earn +${EconomyConfig.REFERRAL_REWARD} coins. Repeat as often as you like.",
                            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                AppState.referrals.forEach { r ->
                    BrandCard {
                        Column(Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(r.referredName, style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                                    Text(
                                        "joined ${relativeTime(r.createdAt)} · ${r.referralCode}",
                                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                                    )
                                }
                                ReferralStatusBadge(r.status)
                            }
                            when (r.status) {
                                ReferralStatus.REGISTERED -> {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Waiting for ${r.referredName} to complete a confirmed exchange (demo: simulate below).",
                                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    SecondaryButton("Simulate qualifying exchange (demo)", modifier = Modifier.fillMaxWidth()) {
                                        AppState.simulateFriendQualifying(r.id)
                                    }
                                }
                                ReferralStatus.QUALIFIED -> {
                                    Spacer(Modifier.height(8.dp))
                                    BrandButton("Claim +${r.rewardAmount}", modifier = Modifier.fillMaxWidth(), onClick = {
                                        val res = AppState.claimReferralReward(r.id)
                                        toast = when (res) {
                                            is EconomyResult.Success -> res.message
                                            is EconomyResult.Failure -> AppState.detailFor(res.reason)
                                        }
                                    })
                                }
                                else -> {}
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(6.dp))

            // ── demo: simulate a friend joining via your code ───────────
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    Text("Demo: friend joining", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "In production the friend signs up with your code and qualifies by exchanging. " +
                            "Tap to simulate a friend registering with your code right now.",
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    SecondaryButton("Simulate friend joining (demo)", modifier = Modifier.fillMaxWidth()) {
                        AppState.simulateFriendJoining()
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReferralStatusBadge(status: ReferralStatus) {
    val (label, color) = when (status) {
        ReferralStatus.INVITED -> "Invited" to csNeutral()
        ReferralStatus.REGISTERED -> "Joined" to csWarn()
        ReferralStatus.QUALIFYING -> "Qualifying" to csWarn()
        ReferralStatus.QUALIFIED -> "Ready to claim" to GoodGreen
        ReferralStatus.REWARDED -> "✓ Rewarded" to GoodGreen
        ReferralStatus.REJECTED -> "Rejected" to csErr()
    }
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = color,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun csNeutral() = MaterialTheme.colorScheme.onSurfaceVariant
@Composable
private fun csWarn() = com.tiktokboost.app.ui.theme.WarnAmber
@Composable
private fun csErr() = MaterialTheme.colorScheme.error
