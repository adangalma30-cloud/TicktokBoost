package com.tiktokboost.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.MockData
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.AppTopBar
import com.tiktokboost.app.ui.components.CoinPill
import com.tiktokboost.app.ui.theme.CardBg
import com.tiktokboost.app.ui.theme.TextPrimary
import com.tiktokboost.app.ui.theme.TextSecondary
import com.tiktokboost.app.ui.theme.TikTokBg
import com.tiktokboost.app.ui.theme.TikTokCyan

@Composable
fun EarnScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var msg by remember { mutableStateOf("") }

    Scaffold(
        containerColor = TikTokBg,
        topBar = { AppTopBar("Earn Coins", onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Current balance",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                CoinPill(AppState.coins)
            }

            Spacer(Modifier.height(16.dp))

            TaskRow(
                icon = "\uD83D\uDCC5",
                title = "Daily Check-in",
                desc = "Claim your free daily bonus",
                reward = 2,
                claimed = !AppState.canCheckIn()
            ) {
                val earned = AppState.checkIn()
                msg = if (earned > 0) "+$earned claimed!" else "Already claimed today"
            }

            Spacer(Modifier.height(10.dp))

            val followProgress = minOf(AppState.followedCount, 3)
            TaskRow(
                icon = "\uD83E\uDD1D",
                title = "Follow 3 creators",
                desc = "Progress: $followProgress/3 — follow users in the Exchange",
                reward = 15,
                claimed = AppState.isTaskClaimed("t_follow3"),
                claimable = followProgress >= 3 && !AppState.isTaskClaimed("t_follow3")
            ) {
                val ok = AppState.claimTask("t_follow3", 15)
                msg = if (ok) "+15 claimed! \uD83C\uDF89" else "Task already claimed"
            }

            Spacer(Modifier.height(10.dp))

            TaskRow(
                icon = "\u2728",
                title = "Complete your profile",
                desc = "Add your display name and TikTok handle",
                reward = 10,
                claimed = AppState.isTaskClaimed("t_profile")
            ) {
                val ok = AppState.claimTask("t_profile", 10)
                msg = if (ok) "+10 claimed!" else "Task already claimed"
            }

            Spacer(Modifier.height(10.dp))

            TaskRow(
                icon = "\uD83C\uDF81",
                title = "Invite a friend",
                desc = "Share your invite link with a friend",
                reward = 50,
                claimed = AppState.isTaskClaimed("t_invite")
            ) {
                val link = "https://tiktokboost.app/i/${AppState.tiktokUsername}"
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("TikTokBoost invite", link))
                val ok = AppState.claimTask("t_invite", 50)
                msg = if (ok) "+50 claimed! Invite link copied \uD83D\uDCCB" else "Invite link copied \uD83D\uDCCB"
            }

            Spacer(Modifier.height(10.dp))

            TaskRow(
                icon = "\uD83D\uDCE4",
                title = "Share the app",
                desc = "Share TikTokBoost with your community",
                reward = 5,
                claimed = AppState.isTaskClaimed("t_share")
            ) {
                val ok = AppState.claimTask("t_share", 5)
                msg = if (ok) "+5 claimed!" else "Task already claimed"
            }

            msg.takeIf { it.isNotBlank() }?.let {
                Spacer(Modifier.height(12.dp))
                Text(it, color = TikTokCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(Modifier.height(20.dp))
            Text(
                "Tip: every follow in the Exchange also earns ${MockData.REWARD_FOLLOW} coins, and every confirmed follow-back earns ${MockData.REWARD_FOLLOW_BACK}.",
                color = TextSecondary,
                fontSize = 13.sp
            )
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
            Spacer(Modifier.width(8.dp))
            if (claimed) {
                Text("\u2713", color = TikTokCyan, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            } else {
                OutlinedButton(
                    onClick = onClaim,
                    enabled = claimable,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+$reward", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
