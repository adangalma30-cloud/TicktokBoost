package com.tiktokboost.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.CoinPill
import com.tiktokboost.app.ui.components.GradientButton
import com.tiktokboost.app.ui.components.SectionTitle
import com.tiktokboost.app.ui.theme.AccentPurple
import com.tiktokboost.app.ui.theme.CardBg
import com.tiktokboost.app.ui.theme.TextPrimary
import com.tiktokboost.app.ui.theme.TextSecondary
import com.tiktokboost.app.ui.theme.TikTokBg
import com.tiktokboost.app.ui.theme.TikTokCyan
import com.tiktokboost.app.ui.theme.TikTokPink

@Composable
fun CoinsScreen(onEarn: () -> Unit) {
    var msg by remember { mutableStateOf("") }

    Scaffold(containerColor = TikTokBg) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))
            Text(
                "Coins",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text("Your points balance", color = TextSecondary, fontSize = 14.sp)

            Spacer(Modifier.height(16.dp))

            // Balance card
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.linearGradient(listOf(TikTokPink, AccentPurple)))
                    .padding(24.dp)
            ) {
                Text("Balance", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text(
                    "\uD83E\uDE99 ${AppState.coins}",
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Spend coins to get featured and reach more creators.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(24.dp))
            SectionTitle("Spend coins")

            StoreItem(
                icon = "\u2B50",
                title = "Featured slot — 24h",
                desc = "Pin your profile to the top of the Exchange",
                price = 50,
                onBuy = {
                    msg = if (AppState.spendCoins(50)) "You're featured for 24 hours! \uD83C\uDF89"
                    else "Not enough coins — earn more!"
                }
            )
            Spacer(Modifier.height(10.dp))
            StoreItem(
                icon = "\uD83D\uDCCC",
                title = "Priority listing — 24h",
                desc = "Show above regular users",
                price = 30,
                onBuy = {
                    msg = if (AppState.spendCoins(30)) "You're now in priority listing! \uD83D\uDE80"
                    else "Not enough coins — earn more!"
                }
            )
            Spacer(Modifier.height(10.dp))
            StoreItem(
                icon = "\uD83D\uDC8E",
                title = "Coin packs",
                desc = "Buying coins is coming in a later version",
                price = 0,
                enabled = false,
                onBuy = {}
            )

            msg.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = TikTokCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(Modifier.height(24.dp))
            SectionTitle("Earn coins")
            GradientButton(text = "Go to Earn Coins", onClick = onEarn)
            Spacer(Modifier.height(24.dp))
        }
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.Bold)
                Text(desc, color = TextSecondary, fontSize = 12.sp)
            }
            if (enabled) {
                OutlinedButton(
                    onClick = onBuy,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("$price \uD83E\uDE99", fontWeight = FontWeight.Bold)
                }
            } else {
                Text("Soon", color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}
