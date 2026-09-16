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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.AppTopBar
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.EmptyState
import com.tiktokboost.app.ui.components.GradientAvatar
import com.tiktokboost.app.ui.components.PremiumBadge
import com.tiktokboost.app.ui.theme.CoinGoldDeep

/**
 * Leaderboard — creators ranked by legitimately earned coins (exchanges + activity).
 * Public info only: display name, points, badge. Your row is highlighted.
 */
@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val rows = remember(AppState.lifetimeEarned, AppState.transactions.size) { AppState.leaderboard() }
    val myRank = remember(rows.size) { rows.indexOfFirst { it.third } + 1 }

    Scaffold(
        containerColor = cs.background,
        topBar = { AppTopBar("Leaderboard", onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            // your position header
            BrandCard {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🏆", fontSize = 26.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            if (myRank > 0) "You're #$myRank" else "Not ranked yet",
                            style = MaterialTheme.typography.titleMedium, color = cs.onSurface
                        )
                        Text(
                            "${AppState.lifetimeEarned} lifetime coins · keep exchanging to climb",
                            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            if (rows.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Star,
                    tint = CoinGoldDeep,
                    title = "No leaderboard data yet.",
                    subtitle = "Complete exchanges to earn coins and appear here."
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 20.dp)
                ) {
                    items(rows.size) { i ->
                        val (name, pts, isMe) = rows[i]
                        val medal = when (i) { 0 -> "🥇"; 1 -> "🥈"; 2 -> "🥉"; else -> null }
                        BrandCard {
                            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    medal ?: "#${i + 1}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (medal == null) cs.onSurfaceVariant else cs.onSurface,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(38.dp)
                                )
                                GradientAvatar(name, name.hashCode(), 36.dp)
                                Spacer(Modifier.width(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        if (isMe) "$name (you)" else name,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (isMe) cs.primary else cs.onSurface,
                                        fontWeight = if (isMe) FontWeight.Bold else FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                    Text("lifetime coins", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                                }
                                Text(
                                    "$pts",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = CoinGoldDeep,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
