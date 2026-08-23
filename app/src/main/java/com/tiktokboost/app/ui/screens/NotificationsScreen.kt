package com.tiktokboost.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.dp
import com.tiktokboost.app.R
import com.tiktokboost.app.data.AppNotification
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.AppTopBar
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.EmptyState
import com.tiktokboost.app.ui.components.StaggeredAppear
import com.tiktokboost.app.ui.components.relativeTime
import com.tiktokboost.app.ui.theme.CoinGoldDeep
import com.tiktokboost.app.ui.theme.GoodGreen
import com.tiktokboost.app.ui.theme.BrandCyan
import com.tiktokboost.app.ui.theme.InfoBlue

@Composable
fun NotificationsScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AppTopBar(
                title = "Notifications",
                onBack = onBack,
                actions = {
                    if (AppState.unreadCount > 0) {
                        TextButton(onClick = { AppState.markAllRead() }) {
                            Text("Mark all read", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (AppState.notifications.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding)) {
                EmptyState(
                    icon = Icons.Filled.Notifications,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    title = "No new notifications.",
                    subtitle = "Exchange updates and coin releases will appear here."
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
                items(AppState.notifications, key = { it.id }) { n ->
                    StaggeredAppear(index = 0) {
                        NotificationCard(n)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(n: AppNotification) {
    val cs = MaterialTheme.colorScheme
    val (tint, icon) = when (n.kind) {
        "exchange" -> GoodGreen to Icons.Filled.CheckCircle
        "coins" -> CoinGoldDeep to null
        "trust" -> InfoBlue to null
        else -> cs.onSurfaceVariant to Icons.Filled.Notifications
    }
    BrandCard {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(38.dp).clip(CircleShape).background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    n.kind == "coins" -> com.tiktokboost.app.ui.components.CoinIcon(size = 18.dp)
                    n.kind == "trust" -> Icon(painterResource(R.drawable.ic_trophy), null, tint = tint, modifier = Modifier.size(18.dp))
                    else -> Icon(icon ?: Icons.Filled.Notifications, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    n.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (n.read) cs.onSurfaceVariant else cs.onSurface
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    n.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    relativeTime(n.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant
                )
            }
            if (!n.read) {
                Box(
                    Modifier
                        .padding(top = 4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(cs.primary)
                )
            }
        }
    }
}
