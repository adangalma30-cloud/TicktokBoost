package com.tiktokboost.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.FollowEvent
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.ui.components.AppTopBar
import com.tiktokboost.app.ui.components.GradientAvatar
import com.tiktokboost.app.ui.components.relativeTime
import com.tiktokboost.app.ui.theme.TextPrimary
import com.tiktokboost.app.ui.theme.TextSecondary
import com.tiktokboost.app.ui.theme.TikTokBg

@Composable
fun HistoryScreen(onBack: () -> Unit) {
    val history = remember { Session.history() }

    Scaffold(
        containerColor = TikTokBg,
        topBar = { AppTopBar("History", onBack) }
    ) { padding ->
        if (history.isEmpty()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("\uD83D\uDD59", fontSize = 44.sp)
                Spacer(Modifier.height(12.dp))
                Text(
                    "No activity yet",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Follow someone from the Exchange to get started!",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history, key = { it.id }) { ev ->
                    HistoryRow(ev)
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(ev: FollowEvent) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GradientAvatar(ev.username, ev.username.hashCode(), 40.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(ev.action, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(relativeTime(ev.timestamp), color = TextSecondary, fontSize = 12.sp)
        }
        Spacer(Modifier.width(8.dp))
        Text("+${ev.coinsEarned} \uD83E\uDE99", color = TextPrimary, fontWeight = FontWeight.Bold)
    }
}
