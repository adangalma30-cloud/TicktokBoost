package com.tiktokboost.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
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
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.CoinIcon
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.GradientAvatar
import com.tiktokboost.app.ui.components.SectionTitle
import com.tiktokboost.app.ui.components.SecondaryButton
import com.tiktokboost.app.ui.components.StatusBadge
import com.tiktokboost.app.ui.components.relativeTime
import com.tiktokboost.app.ui.theme.AccentPurple
import com.tiktokboost.app.ui.theme.TikTokPink
import com.tiktokboost.app.ui.theme.WarnAmber

@Composable
fun CoinsScreen(onEarn: () -> Unit) {
    var msg by remember { mutableStateOf("") }
    var released by remember { mutableStateOf(false) }

    val cs = MaterialTheme.colorScheme

    // flash a friendly "released" chip whenever pending coins land
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
        Spacer(Modifier.height(16.dp))
        Text(
            "Coins",
            style = MaterialTheme.typography.headlineMedium,
            color = cs.onBackground
        )
        Text("Your visibility currency", style = MaterialTheme.typography.bodyLarge, color = cs.onSurfaceVariant)

        Spacer(Modifier.height(16.dp))

        // ── balance hero ──────────────────────────────────────────────────
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.linearGradient(listOf(TikTokPink, AccentPurple)))
                .padding(22.dp)
        ) {
            Text("Available balance", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
            Spacer(Modifier.height(4.dp))
            AnimatedCoinText(
                amount = AppState.coins,
                style = MaterialTheme.typography.displaySmall.copy(color = Color.White)
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CoinIcon(size = 15.dp)
                Spacer(Modifier.width(6.dp))
                Text(
                    "${AppState.pendingCoins} pending — waiting for confirmations",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            AnimatedVisibility(visible = released, enter = fadeIn(tween(250)) + scaleIn(initialScale = 0.85f)) {
                Text(
                    "🎉 +${AppState.lastCoinReleaseAmount} coins released from pending!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ── what coins do ────────────────────────────────────────────────
        BrandCard {
            Row(Modifier.padding(Dimens.card), verticalAlignment = Alignment.Top) {
                CoinIcon(size = 30.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("What are coins?", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Coins are in-app credits used to increase your visibility within TickTokBoost.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // ── recent earned / spent ─────────────────────────────────────────
        SectionTitle("Recent coin activity")
        val recent = AppState.transactions.take(5)
        if (recent.isEmpty()) {
            Text(
                "Nothing yet — complete your first exchange!",
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recent.forEach { tx ->
                    BrandCard {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            GradientAvatar(tx.username, tx.username.hashCode(), 32.dp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    when (tx.type) {
                                        TxType.FOLLOW -> "Exchange · @${tx.username}"
                                        TxType.FOLLOW_BACK -> "Follow back · @${tx.username}"
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
                            if (tx.status == com.tiktokboost.app.data.TxStatus.PENDING) {
                                StatusBadge(tx.status)
                            } else {
                                Text(
                                    (if (tx.coins >= 0) "+" else "") + "${tx.coins}",
                                    color = if (tx.coins >= 0) cs.secondary else cs.error,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── spend ─────────────────────────────────────────────────────────
        SectionTitle("Spend coins")
        StoreItem(
            icon = "⭐",
            title = "Featured slot — 24h",
            desc = "Pin your profile to the top of Discovery",
            price = 50,
            onBuy = {
                msg = if (AppState.spendCoins(50, "Featured slot — 24h")) "You're featured for 24 hours! 🎉"
                else "Not enough coins — earn more!"
            }
        )
        Spacer(Modifier.height(8.dp))
        StoreItem(
            icon = "📌",
            title = "Priority listing — 24h",
            desc = "Show above regular creators",
            price = 30,
            onBuy = {
                msg = if (AppState.spendCoins(30, "Priority listing — 24h")) "You're now in priority listing! 🚀"
                else "Not enough coins — earn more!"
            }
        )
        Spacer(Modifier.height(8.dp))
        StoreItem(
            icon = "💎",
            title = "Coin packs",
            desc = "Buying coins is coming in a later version",
            price = 0,
            enabled = false,
            onBuy = {}
        )

        msg.takeIf { it.isNotBlank() }?.let {
            Spacer(Modifier.height(10.dp))
            Text(it, color = cs.secondary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(Modifier.height(16.dp))
        SectionTitle("Earn more")
        BrandButton(text = "Go to Earn Coins", onClick = onEarn)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StoreItem(
    icon: String,
    title: String,
    desc: String,
    price: Int,
    enabled: Boolean = true,
    onBuy: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    BrandCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                Text(desc, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            }
            if (enabled) {
                SecondaryButton(
                    "$price",
                    modifier = Modifier.width(86.dp),
                    onClick = onBuy
                )
            } else {
                Text("Soon", color = cs.onSurfaceVariant, fontSize = 13.sp)
            }
        }
    }
}
