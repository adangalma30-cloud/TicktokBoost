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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.MockData
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.AnimatedCoinText
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.CoinIcon
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.AppTopBar
import com.tiktokboost.app.ui.components.SecondaryButton
import com.tiktokboost.app.ui.theme.GoodGreen

@Composable
fun EarnScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var msg by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar("Earn Coins", onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenH)
        ) {
            Spacer(Modifier.height(8.dp))

            // balance header
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    Text(
                        "Current balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    AnimatedCoinText(
                        amount = AppState.coins,
                        style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                    )
                }
            }

            AnimatedVisibility(visible = msg.isNotBlank(), enter = fadeIn(tween(220)) + slideInVertically(tween(260)) { it / 6 }) {
                Row(
                    Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .padding(vertical = 4.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    CoinIcon(size = 16.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(msg, color = GoodGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(14.dp))

            TaskRow(
                icon = "📅",
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
                icon = "🤝",
                title = "Follow 3 creators",
                desc = "Progress: $followProgress/3 — follow users in Discovery",
                reward = 15,
                claimed = AppState.isTaskClaimed("t_follow3"),
                claimable = followProgress >= 3 && !AppState.isTaskClaimed("t_follow3")
            ) {
                val ok = AppState.claimTask("t_follow3", 15, "Task: Follow 3 creators")
                msg = if (ok) "+15 claimed! 🎉" else "Task already claimed"
            }

            Spacer(Modifier.height(10.dp))

            TaskRow(
                icon = "✨",
                title = "Complete your profile",
                desc = "Add your display name and TikTok handle",
                reward = 10,
                claimed = AppState.isTaskClaimed("t_profile")
            ) {
                val ok = AppState.claimTask("t_profile", 10, "Task: Complete profile")
                msg = if (ok) "+10 claimed!" else "Task already claimed"
            }

            Spacer(Modifier.height(10.dp))

            TaskRow(
                icon = "🎁",
                title = "Invite a friend",
                desc = "Share your invite link with a friend",
                reward = 50,
                claimed = AppState.isTaskClaimed("t_invite")
            ) {
                val link = "https://tiktokboost.app/i/${AppState.tiktokUsername}"
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("TickTokBoost invite", link))
                val ok = AppState.claimTask("t_invite", 50, "Task: Invite a friend")
                msg = if (ok) "+50 claimed! Invite link copied 📋" else "Invite link copied 📋"
            }

            Spacer(Modifier.height(10.dp))

            TaskRow(
                icon = "📤",
                title = "Share the app",
                desc = "Share TickTokBoost with your community",
                reward = 5,
                claimed = AppState.isTaskClaimed("t_share")
            ) {
                val ok = AppState.claimTask("t_share", 5, "Task: Share the app")
                msg = if (ok) "+5 claimed!" else "Task already claimed"
            }

            Spacer(Modifier.height(18.dp))

            BrandCard {
                Text(
                    "💡 Tip",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Every completed exchange in Discovery earns its coin reward (released after the counterpart confirms), and every confirmed follow-back earns ${MockData.REWARD_FOLLOW_BACK}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
        Row(
            Modifier.padding(14.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                Text(desc, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            if (claimed) {
                Text("✓", color = cs.secondary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            } else {
                SecondaryButton(
                    "+$reward",
                    enabled = claimable,
                    modifier = Modifier.width(76.dp),
                    onClick = onClaim
                )
            }
        }
    }
}
