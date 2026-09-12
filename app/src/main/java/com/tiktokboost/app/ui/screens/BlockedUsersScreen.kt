package com.tiktokboost.app.ui.screens

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
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.MockData
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.AppTopBar
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.EmptyState
import com.tiktokboost.app.ui.components.GradientAvatar
import com.tiktokboost.app.ui.theme.GoodGreen

/** Settings → Blocked users: unblock creators; history is always preserved. */
@Composable
fun BlockedUsersScreen(onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val refresh = remember { mutableStateOf(0) }

    val blocked = MockData.users.filter { Session.isBlocked(it.id) }

    Scaffold(
        containerColor = cs.background,
        topBar = { AppTopBar("Blocked users", onBack) }
    ) { padding ->
        if (blocked.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding)) {
                EmptyState(
                    icon = androidx.compose.material.icons.Icons.Filled.Done,
                    tint = cs.onSurfaceVariant,
                    title = "No blocked creators.",
                    subtitle = "Long-press any creator card in Discover to block or report them."
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp)
            ) {
                items(blocked.size) { i ->
                    val u = blocked[i]
                    BrandCard {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            GradientAvatar(u.displayName, u.hueSeed, 40.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(u.displayName, style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                                Text("@${u.username}", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            }
                            TextButton(onClick = {
                                AppState.toggleBlock(u.id)
                                refresh.value++
                            }) {
                                Text("Unblock", color = GoodGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
