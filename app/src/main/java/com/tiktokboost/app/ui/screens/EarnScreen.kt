package com.tiktokboost.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.EconomyConfig
import com.tiktokboost.app.data.EconomyResult
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.AnimatedCoinText
import androidx.compose.material3.TextButton
import com.tiktokboost.app.ui.components.AppTopBar
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.CoinIcon
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.PremiumBadge
import com.tiktokboost.app.ui.components.SecondaryButton
import com.tiktokboost.app.ui.components.StreakChip
import com.tiktokboost.app.ui.theme.GoodGreen

/** Earn tab — tasks, streaks and the daily earning limit. */
@Composable
fun EarnScreen(onBack: (() -> Unit)? = null, showTopBar: Boolean = true) {
    val context = LocalContext.current
    var msg by remember { mutableStateOf<String?>(null) }
    val cs = MaterialTheme.colorScheme

    LaunchedEffect(msg) { if (msg != null) { kotlinx.coroutines.delay(3200); msg = null } }

    Scaffold(
        containerColor = cs.background,
        topBar = { if (showTopBar) AppTopBar("Earn Coins", onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenH)
        ) {
            Spacer(Modifier.height(if (showTopBar) 8.dp else 16.dp))

            // ── balance + daily limit ───────────────────────────────────
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Available coins", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                            Spacer(Modifier.height(2.dp))
                            AnimatedCoinText(
                                amount = AppState.coins,
                                style = MaterialTheme.typography.headlineMedium.copy(color = cs.onSurface)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            StreakChip(days = AppState.streakDays)
                            Spacer(Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PremiumBadge(tier = AppState.premium, compact = true)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Today's progress: ${AppState.dailyEarnedCoins} / ${AppState.dailyCap} coins earned",
                        style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { AppState.dailyEarnedCoins / AppState.dailyCap.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = cs.primary, trackColor = cs.surfaceVariant
                    )
                    if (AppState.dailyEarnedCoins >= AppState.dailyCap) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "You've reached today's earning limit. Come back tomorrow.",
                            style = MaterialTheme.typography.bodySmall, color = cs.secondary, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            AnimatedVisibility(visible = msg != null, enter = fadeIn(tween(220)) + slideInVertically(tween(260)) { it / 6 }) {
                Row(
                    Modifier.padding(top = 10.dp, bottom = 2.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoinIcon(size = 16.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(msg ?: "", color = GoodGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(14.dp))

            val result: (EconomyResult) -> String = { res ->
                when (res) {
                    is EconomyResult.Success -> res.message ?: "+${res.coins} coins"
                    is EconomyResult.Failure -> AppState.detailFor(res.reason)
                }
            }

            TaskRow(
                icon = "📅", title = "Daily Check-in",
                desc = "Claim your small daily bonus + streak",
                reward = EconomyConfig.DAILY_CHECKIN_REWARD,
                claimed = !AppState.canCheckIn()
            ) { msg = result(AppState.checkIn()) }

            Spacer(Modifier.height(10.dp))

            val followProgress = minOf(AppState.followedCount, 3)
            TaskRow(
                icon = "🤝", title = "Follow 3 creators",
                desc = "Progress: $followProgress/3 — in Discover",
                reward = 5,
                claimed = AppState.isTaskClaimed("t_follow3"),
                claimable = followProgress >= 3 && !AppState.isTaskClaimed("t_follow3")
            ) { msg = result(AppState.claimTask("t_follow3", 5, "Task: Follow 3 creators")) }

            Spacer(Modifier.height(10.dp))

            TaskRow(
                icon = "✨", title = "Complete your profile",
                desc = "Add your bio and category",
                reward = 5,
                claimed = AppState.isTaskClaimed("t_profile")
            ) { msg = result(AppState.claimTask("t_profile", 5, "Task: Complete profile")) }

            Spacer(Modifier.height(10.dp))

            TaskRow(
                icon = "🎁", title = "Invite a friend",
                desc = "Share your invite link",
                reward = 10,
                claimed = AppState.isTaskClaimed("t_invite")
            ) {
                val link = "https://ticktokboost.app/i/${AppState.tiktokUsername}"
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("TickTokBoost invite", link))
                val res = AppState.claimTask("t_invite", 10, "Task: Invite a friend")
                msg = if (res is EconomyResult.Success) "+10 claimed! Invite link copied 📋" else "Invite link copied 📋"
            }

            Spacer(Modifier.height(10.dp))

            TaskRow(
                icon = "📤", title = "Share the app",
                desc = "Share TickTokBoost with your community",
                reward = 3,
                claimed = AppState.isTaskClaimed("t_share")
            ) { msg = result(AppState.claimTask("t_share", 3, "Task: Share the app")) }

            Spacer(Modifier.height(16.dp))

            // ── premium daily bonus ──────────────────────────────────────
            if (AppState.premium != com.tiktokboost.app.data.PremiumTier.FREE) {
                BrandCard {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("💎", fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Premium daily bonus", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                            Text("Small, controlled — counts toward your cap", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                        }
                        SecondaryButton("+${EconomyConfig.PREMIUM_DAILY_BONUS}", onClick = { msg = result(AppState.claimPremiumDailyBonus()) })
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            BrandCard {
                Text("💡 How earning works", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Confirmed exchanges pay ${EconomyConfig.COINS_PER_CONFIRMED_EXCHANGE} coins each, follow-backs ${EconomyConfig.COINS_PER_FOLLOW_BACK}. " +
                        "Daily cap: ${EconomyConfig.DAILY_EARNING_CAP} coins. Exchange cooldown: ${EconomyConfig.EXCHANGE_COOLDOWN_MINUTES} minutes " +
                        "(shorter as your trust grows). Coins are earned — never bought with real money in this version.",
                    style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TaskRow(
    icon: String,
    title: String,
    desc: String,
    reward: Int,
    claimed: Boolean,
    claimable: Boolean = true,
    onClaim: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    BrandCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                Text(desc, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            if (claimed) {
                Text("✓", color = cs.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            } else {
                SecondaryButton("+$reward", enabled = claimable, modifier = Modifier.width(72.dp), onClick = onClaim)
            }
        }
    }
}
