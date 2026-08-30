package com.tiktokboost.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.TxType
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.AnimatedCoinText
import com.tiktokboost.app.ui.components.BoostChip
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.CoinIcon
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.GradientAvatar
import com.tiktokboost.app.ui.components.LogoMark
import com.tiktokboost.app.ui.components.PremiumBadge
import com.tiktokboost.app.ui.components.SecondaryButton
import com.tiktokboost.app.ui.components.StaggeredAppear
import com.tiktokboost.app.ui.components.StreakChip
import com.tiktokboost.app.ui.components.StatusBadge
import com.tiktokboost.app.ui.components.TrustBadge
import com.tiktokboost.app.ui.components.TrustProgress
import com.tiktokboost.app.ui.components.relativeTime
import com.tiktokboost.app.ui.theme.CoinGoldDeep
import com.tiktokboost.app.ui.theme.SigGradientEnd
import com.tiktokboost.app.ui.theme.SigGradientStart

@Composable
fun HomeScreen(
    onOpenDiscover: () -> Unit,
    onOpenBoost: () -> Unit,
    onOpenEarn: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    var released by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme

    LaunchedEffect(AppState.lastCoinReleaseAt) {
        if (AppState.lastCoinReleaseAt > 0L) {
            released = true
            kotlinx.coroutines.delay(2600)
            released = false
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.screenH)
    ) {
        Spacer(Modifier.height(14.dp))

        // ── brand header: logo · premium · streak · bell · avatar ───────
        Row(verticalAlignment = Alignment.CenterVertically) {
            LogoMark(size = 28.dp)
            Spacer(Modifier.width(8.dp))
            Text("TickTokBoost", style = MaterialTheme.typography.titleLarge, color = cs.onBackground)
            Spacer(Modifier.weight(1f))
            StreakChip(days = AppState.streakDays)
            Spacer(Modifier.width(6.dp))
            IconButton(onClick = onOpenNotifications) {
                Box {
                    Icon(Icons.Filled.Notifications, contentDescription = "Notifications", tint = cs.onBackground)
                    if (AppState.unreadCount > 0) {
                        Box(
                            Modifier.size(15.dp).clip(CircleShape).background(cs.secondary).align(Alignment.TopEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (AppState.unreadCount > 9) "9+" else "${AppState.unreadCount}",
                                color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            GradientAvatar(AppState.displayName.ifBlank { "T" }, 0, 38.dp)
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "Hey ${AppState.displayName.ifBlank { "creator" }} 👋",
            style = MaterialTheme.typography.headlineMedium,
            color = cs.onBackground
        )
        Text("Ready to grow today?", style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant)

        Spacer(Modifier.height(16.dp))

        // ── wallet: the four coin types ─────────────────────────────────
        StaggeredAppear(0) {
            BrandCard {
                Column(Modifier.padding(Dimens.card + 2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Available coins", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                            Spacer(Modifier.height(2.dp))
                            AnimatedCoinText(
                                amount = AppState.coins,
                                style = MaterialTheme.typography.headlineMedium.copy(color = CoinGoldDeep)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CoinIcon(size = 13.dp)
                                Spacer(Modifier.width(4.dp))
                                Text("${AppState.pendingCoins} pending", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                            }
                            Text(
                                "Lifetime ${AppState.lifetimeEarned}↑ ${AppState.lifetimeSpent}↓",
                                style = MaterialTheme.typography.labelSmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Today's earning progress — "12 / 25 coins earned"
                    Text(
                        "Today's progress: ${AppState.dailyEarnedCoins} / ${AppState.dailyCap} coins earned",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (AppState.dailyEarnedCoins >= AppState.dailyCap) cs.secondary else cs.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { AppState.dailyEarnedCoins / AppState.dailyCap.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = if (AppState.dailyEarnedCoins >= AppState.dailyCap) cs.secondary else cs.primary,
                        trackColor = cs.surfaceVariant
                    )
                    if (AppState.dailyEarnedCoins >= AppState.dailyCap) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "You've reached today's earning limit. Come back tomorrow.",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    AnimatedVisibility(visible = released, enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.85f)) {
                        Row(
                            Modifier
                                .padding(top = 8.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CoinGoldDeep.copy(alpha = 0.12f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CoinIcon(size = 16.dp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "+${AppState.lastCoinReleaseAmount} coins released 🎉",
                                color = CoinGoldDeep, fontWeight = FontWeight.Bold, fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TrustBadge(level = AppState.myTrustLevel, animate = true)
                        Spacer(Modifier.width(8.dp))
                        AppState.activeBoost?.let { (tier, until) ->
                            BoostChip(tier = tier, msRemaining = until - System.currentTimeMillis())
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(
                            "Score ${AppState.myTrustScore}/100",
                            style = MaterialTheme.typography.labelSmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    TrustProgress(successfulExchanges = AppState.myExchanges)
                }
            }
        }

        Spacer(Modifier.height(Dimens.gap + 2.dp))

        // ── primary & secondary CTAs ────────────────────────────────────
        StaggeredAppear(1) {
            Column {
                BrandButton("Discover Creators", icon = Icons.Filled.Search, onClick = onOpenDiscover)
                Spacer(Modifier.height(8.dp))
                SecondaryButton("Boost My Profile", icon = Icons.Filled.Done, onClick = onOpenBoost)
            }
        }

        Spacer(Modifier.height(18.dp))

        // ── premium strip (tasteful) ────────────────────────────────────
        if (AppState.premium == com.tiktokboost.app.data.PremiumTier.FREE) {
            StaggeredAppear(2) {
                BrandCard(onClick = onOpenBoost) {
                    Row(
                        Modifier.padding(Dimens.card),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                                .background(Brush.linearGradient(listOf(SigGradientStart, SigGradientEnd))),
                            contentAlignment = Alignment.Center
                        ) { Text("💎", fontSize = 20.sp) }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Unlock More With TickTokBoost Premium", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                            Text("Better tools, visibility & analytics", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                        }
                        Text("→", color = cs.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        } else {
            StaggeredAppear(2) {
                BrandCard(onClick = onOpenBoost) {
                    Row(Modifier.padding(Dimens.card), verticalAlignment = Alignment.CenterVertically) {
                        PremiumBadge(tier = AppState.premium)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (AppState.premium == com.tiktokboost.app.data.PremiumTier.PRO) "Premium Pro active"
                                else "Premium active",
                                style = MaterialTheme.typography.titleSmall, color = cs.onSurface
                            )
                            AppState.premiumExpiresAt?.let {
                                Text(
                                    "Renews ${relativeTime(it)}", style = MaterialTheme.typography.bodySmall,
                                    color = cs.onSurfaceVariant
                                )
                            }
                        }
                        Text("Manage →", color = cs.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        // ── recent activity ─────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text("Recent activity", style = MaterialTheme.typography.titleMedium, color = cs.onSurface, modifier = Modifier.weight(1f))
            Text(
                "View all", color = cs.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp,
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenHistory() }.padding(6.dp)
            )
        }
        Spacer(Modifier.height(4.dp))

        val recent = AppState.transactions.take(3)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (recent.isEmpty()) {
                BrandCard {
                    Text(
                        "You're all caught up.",
                        style = MaterialTheme.typography.titleSmall, color = cs.onSurface,
                        modifier = Modifier.padding(Dimens.card)
                    )
                }
            }
            recent.forEach { tx -> TxRow(tx) }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun TxRow(tx: com.tiktokboost.app.data.Transaction) {
    val cs = MaterialTheme.colorScheme
    BrandCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            GradientAvatar(tx.username, tx.username.hashCode(), 34.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    when (tx.type) {
                        TxType.FOLLOW -> "Exchange · @${tx.username}"
                        TxType.FOLLOW_BACK -> "@${tx.username} followed you back"
                        TxType.PURCHASE -> tx.note.ifBlank { "Purchase" }
                        TxType.BOOST -> tx.note.ifBlank { "Boost" }
                        TxType.PREMIUM -> tx.note.ifBlank { "Subscription" }
                        TxType.STREAK -> tx.note.ifBlank { "Streak bonus" }
                        TxType.ACHIEVEMENT -> tx.note.ifBlank { "Achievement" }
                        TxType.BONUS -> tx.note.ifBlank { "Bonus" }
                    },
                    style = MaterialTheme.typography.titleSmall, color = cs.onSurface, maxLines = 1
                )
                Text(relativeTime(tx.createdAt), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
            }
            if (tx.status == com.tiktokboost.app.data.TxStatus.PENDING ||
                tx.status == com.tiktokboost.app.data.TxStatus.DISPUTED
            ) {
                StatusBadge(tx.status)
            } else {
                Text(
                    (if (tx.coins >= 0) "+" else "") + "${tx.coins}",
                    color = if (tx.coins >= 0) cs.primary else cs.secondary,
                    fontWeight = FontWeight.ExtraBold, fontSize = 15.sp
                )
            }
        }
    }
}
