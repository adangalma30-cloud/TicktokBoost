package com.tiktokboost.app.ui.screens

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.MockData
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.CoinPill
import com.tiktokboost.app.ui.components.GradientAvatar
import com.tiktokboost.app.ui.components.GradientButton
import com.tiktokboost.app.ui.components.QuickActionCard
import com.tiktokboost.app.ui.components.StatBox
import com.tiktokboost.app.ui.theme.CardBg
import com.tiktokboost.app.ui.theme.TextPrimary
import com.tiktokboost.app.ui.theme.TextSecondary
import com.tiktokboost.app.ui.theme.TikTokBg
import com.tiktokboost.app.ui.theme.TikTokCyan

@Composable
fun HomeScreen(
    onOpenExchange: () -> Unit,
    onOpenEarn: () -> Unit,
    onOpenHistory: () -> Unit
) {
    var checkInMsg by remember { mutableStateOf("") }

    Scaffold(containerColor = TikTokBg) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Hey ${AppState.displayName.ifBlank { "creator" }} \uD83D\uDC4B",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("Ready to grow?", color = TextSecondary, fontSize = 14.sp)
                }
                GradientAvatar(AppState.displayName.ifBlank { "T" }, 0, 46.dp)
            }

            Spacer(Modifier.height(20.dp))

            // Points / daily check-in card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("\uD83E\uDE99", fontSize = 24.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "${AppState.coins} points",
                            color = Color(0xFFFFC233),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Earn points by participating in the follow exchange.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(14.dp))
                    if (AppState.canCheckIn()) {
                        GradientButton("Daily Check-in  +2 \uD83E\uDE99") {
                            val earned = AppState.checkIn()
                            checkInMsg = if (earned > 0) "+$earned points claimed!" else "Already claimed today"
                        }
                    } else {
                        Text(
                            "\u2713 Checked in today",
                            color = TikTokCyan,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                    }
                    if (checkInMsg.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(checkInMsg, color = TikTokCyan, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox("Followed", "${AppState.followedCount}", Modifier.weight(1f))
                StatBox("Followed back", "${AppState.returnedCount}", Modifier.weight(1f))
                StatBox("Points", "${AppState.coins}", Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            QuickActionCard(
                icon = "\uD83E\uDD1D",
                title = "Follow Exchange",
                subtitle = "${MockData.users.size} creators want follows",
                action = "Open",
                onClick = onOpenExchange
            )
            Spacer(Modifier.height(10.dp))
            QuickActionCard(
                icon = "\uD83E\uDE99",
                title = "Earn Coins",
                subtitle = "Complete tasks for points",
                action = "Earn",
                onClick = onOpenEarn
            )
            Spacer(Modifier.height(10.dp))
            QuickActionCard(
                icon = "\uD83D\uDD59",
                title = "History",
                subtitle = "Your follow activity",
                action = "View",
                onClick = onOpenHistory
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
