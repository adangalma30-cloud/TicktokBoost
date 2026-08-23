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
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.Trust
import com.tiktokboost.app.data.TxType
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.AnimatedCoinText
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.CoinIcon
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.GradientAvatar
import com.tiktokboost.app.ui.components.LogoMark
import com.tiktokboost.app.ui.components.SecondaryButton
import com.tiktokboost.app.ui.components.StaggeredAppear
import com.tiktokboost.app.ui.components.StatusBadge
import com.tiktokboost.app.ui.components.TrustBadge
import com.tiktokboost.app.ui.components.TrustProgress
import com.tiktokboost.app.ui.components.relativeTime
import com.tiktokboost.app.ui.theme.CoinGoldDeep
import com.tiktokboost.app.ui.theme.WarnAmber

@Composable
fun HomeScreen(
    onOpenExchange: () -> Unit,
    onOpenEarn: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    var checkInMsg by remember { mutableStateOf("") }

    val cs = MaterialTheme.colorScheme

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.screenH)
    ) {
        Spacer(Modifier.height(14.dp))

        // ── top bar: brand, notifications, avatar ────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            LogoMark(size = 30.dp)
            Spacer(Modifier.width(8.dp))
            Text(
                "TickTokBoost",
                style = MaterialTheme.typography.titleLarge,
                color = cs.onBackground
            )
            Spacer(Modifier.weight(1f))
            IconButton(onClick = onOpenNotifications) {
                BadgedNotificationIcon(unread = AppState.unreadCount)
            }
            Spacer(Modifier.width(2.dp))
            Box(Modifier.clip(CircleShape)) {
                GradientAvatar(AppState.displayName.ifBlank { "T" }, 0, 40.dp)
            }
        }

        Spacer(Modifier.height(18.dp))
        Text(
            "Hey ${AppState.displayName.ifBlank { "creator" }} 👋",
            style = MaterialTheme.typography.headlineMedium,
            color = cs.onBackground
        )
        Text(
            "Ready to grow today?",
            style = MaterialTheme.typography.bodyLarge,
            color = cs.onSurfaceVariant
        )

        Spacer(Modifier.height(18.dp))

        // ── wallet: coins, pending, trust ────────────────────────────────────
        StaggeredAppear(0) {
            BrandCard {
                Column(Modifier.padding(Dimens.card + 2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                "Available coins",
                                style = MaterialTheme.typography.labelMedium,
                                color = cs.onSurfaceVariant
                            )
                            Spacer(Modifier.height(2.dp))
                            AnimatedCoinText(
                                amount = AppState.coins,
                                style = MaterialTheme.typography.headlineMedium.copy(color = CoinGoldDeep)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Pending", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CoinIcon(size = 15.dp)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "${AppState.pendingCoins}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = WarnAmber
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // coin-release flash: appears when pending coins are released
                    var released by remember { mutableStateOf(false) }
                    LaunchedEffect(AppState.lastCoinReleaseAt) {
                        if (AppState.lastCoinReleaseAt > 0L) {
                            released = true
                            kotlinx.coroutines.delay(2600)
                            released = false
                        }
                    }
                    AnimatedVisibility(visible = released, enter = fadeIn() + scaleIn(initialScale = 0.85f)) {
                        Row(
                            Modifier
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
                                color = CoinGoldDeep,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TrustBadge(level = AppState.myTrustLevel, animate = true)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            Trust.toNextLevel(AppState.myExchanges)?.let { n -> "$n more to next level" } ?: "Top level — Elite",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    TrustProgress(successfulExchanges = AppState.myExchanges)

                    Spacer(Modifier.height(12.dp))

                    // daily check-in (existing functionality, restyled)
                    if (AppState.canCheckIn()) {
                        BrandButton("Daily check-in  +2") {
                            val earned = AppState.checkIn()
                            checkInMsg = if (earned > 0) "+$earned coins claimed!" else "Already claimed today"
                        }
                    } else {
                        Text(
                            "✓ Checked in today",
                            color = cs.secondary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        )
                    }
                    if (checkInMsg.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(checkInMsg, color = cs.secondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(Dimens.gap + 2.dp))

        // ── primary + secondary actions ─────────────────────────────────────
        StaggeredAppear(1) {
            Column {
                BrandButton("Find Creators", icon = Icons.Filled.Person, onClick = onOpenExchange)
                Spacer(Modifier.height(8.dp))
                SecondaryButton("Earn Coins", icon = Icons.Filled.Star, onClick = onOpenEarn)
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── recent activity ─────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Recent activity",
                style = MaterialTheme.typography.titleMedium,
                color = cs.onSurface,
                modifier = Modifier.weight(1f)
            )
            Text(
                "View all",
                color = cs.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenHistory() }
                    .padding(6.dp)
            )
        }

        Spacer(Modifier.height(4.dp))

        val recent = AppState.transactions.take(3)
        val empty = recent.isEmpty()
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (empty) {
                BrandCard {
                    Text(
                        "You're all caught up.",
                        style = MaterialTheme.typography.titleSmall,
                        color = cs.onSurface,
                        modifier = Modifier.padding(Dimens.card)
                    )
                    Text(
                        "Complete an exchange to see activity here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant,
                        modifier = Modifier.padding(start = Dimens.card, end = Dimens.card, bottom = Dimens.card)
                    )
                }
            }
            recent.forEach { tx ->
                BrandCard {
                    Row(
                        Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GradientAvatar(tx.username, tx.username.hashCode(), 36.dp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                when (tx.type) {
                                    TxType.FOLLOW -> "Exchange with @${tx.username}"
                                    TxType.FOLLOW_BACK -> "@${tx.username} followed you back"
                                    TxType.PURCHASE -> tx.note.ifBlank { "Purchase" }
                                    TxType.BONUS -> tx.note.ifBlank { "Bonus" }
                                },
                                style = MaterialTheme.typography.titleSmall,
                                color = cs.onSurface,
                                maxLines = 1
                            )
                            Text(
                                relativeTime(tx.createdAt),
                                style = MaterialTheme.typography.bodySmall,
                                color = cs.onSurfaceVariant
                            )
                        }
                        StatusBadge(tx.status)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun BadgedNotificationIcon(unread: Int) {
    Box {
        Icon(
            Icons.Filled.Notifications,
            contentDescription = "Notifications",
            tint = MaterialTheme.colorScheme.onBackground
        )
        if (unread > 0) {
            Box(
                Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.TopEnd),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (unread > 9) "9+" else "$unread",
                    color = androidx.compose.ui.graphics.Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
