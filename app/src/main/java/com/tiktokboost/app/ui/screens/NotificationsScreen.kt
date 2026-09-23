package com.tiktokboost.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.R
import com.tiktokboost.app.data.AppNotification
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.AppTopBar
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.EmptyState
import com.tiktokboost.app.ui.components.StaggeredAppear
import com.tiktokboost.app.ui.components.relativeTime
import com.tiktokboost.app.ui.theme.BrandCyan
import com.tiktokboost.app.ui.theme.CoinGoldDeep
import com.tiktokboost.app.ui.theme.GoodGreen
import com.tiktokboost.app.ui.theme.InfoBlue
import com.tiktokboost.app.ui.theme.WarnAmber

/**
 * Notification Center (v1.0.4): professional feed with per-kind icons, unread
 * dots, timestamps, tap-through actions (deep links) and ⋯ menus
 * (mark read/unread, delete). Read state persists.
 */
@Composable
fun NotificationsScreen(onBack: () -> Unit, onOpenRoute: (String) -> Unit = {}) {
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
                    subtitle = "Exchange updates, rewards and reminders will appear here."
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
                        NotificationCard(n, onOpenRoute)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(n: AppNotification, onOpenRoute: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    var menuOpen by remember { mutableStateOf(false) }

    val (tint, icon) = when (n.kind) {
        "exchange" -> GoodGreen to Icons.Filled.CheckCircle
        "coins" -> CoinGoldDeep to null
        "trust" -> InfoBlue to null
        "dispute" -> cs.error to Icons.Filled.Warning
        "abuse" -> cs.error to Icons.Filled.Warning
        "limit" -> WarnAmber to null
        "streak" -> WarnAmber to null
        "premium" -> InfoBlue to null
        "boost" -> InfoBlue to null
        "achievement" -> GoodGreen to null
        else -> cs.onSurfaceVariant to Icons.Filled.Notifications
    }

    BrandCard(onClick = {
        // tap-through: mark read + deep link to the relevant screen
        if (!n.read) AppState.markNotificationRead(n.id)
        n.target?.let { onOpenRoute(it) }
    }) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(38.dp).clip(CircleShape).background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                when (n.kind) {
                    "coins" -> com.tiktokboost.app.ui.components.CoinIcon(size = 18.dp)
                    "trust" -> Icon(painterResource(R.drawable.ic_trophy), null, tint = tint, modifier = Modifier.size(18.dp))
                    "limit" -> Icon(painterResource(R.drawable.ic_clock), null, tint = tint, modifier = Modifier.size(18.dp))
                    "streak" -> Text("🔥", fontSize = 16.sp)
                    "premium" -> Text(if (n.title.contains("Pro", true)) "👑" else "💎", fontSize = 16.sp)
                    "boost" -> Text("🚀", fontSize = 16.sp)
                    "achievement" -> Text("🏆", fontSize = 16.sp)
                    else -> Icon(icon ?: Icons.Filled.Notifications, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    n.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (n.read) cs.onSurfaceVariant else cs.onSurface,
                    fontWeight = if (n.read) FontWeight.SemiBold else FontWeight.Bold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    n.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        relativeTime(n.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = cs.onSurfaceVariant
                    )
                    if (!n.read) {
                        Spacer(Modifier.width(8.dp))
                        Box(Modifier.size(7.dp).clip(CircleShape).background(BrandCyan))
                    }
                }
            }
            // ⋯ per-item actions
            Box {
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Notification options", tint = cs.onSurfaceVariant)
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    if (n.read) {
                        DropdownMenuItem(
                            text = { Text("Mark unread") },
                            onClick = { menuOpen = false; AppState.markNotificationUnread(n.id) }
                        )
                    } else {
                        DropdownMenuItem(
                            leadingIcon = { Icon(Icons.Filled.Done, null, Modifier.size(18.dp)) },
                            text = { Text("Mark read") },
                            onClick = { menuOpen = false; AppState.markNotificationRead(n.id) }
                        )
                    }
                    DropdownMenuItem(
                        leadingIcon = { Icon(Icons.Filled.Delete, null, Modifier.size(18.dp), tint = cs.error) },
                        text = { Text("Delete", color = cs.error) },
                        onClick = { menuOpen = false; AppState.deleteNotification(n.id) }
                    )
                }
            }
        }
    }
}
