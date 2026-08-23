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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tiktokboost.app.data.Transaction
import com.tiktokboost.app.data.TxStatus
import com.tiktokboost.app.data.TxType
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.AppTopBar
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.EmptyState
import com.tiktokboost.app.ui.components.GradientAvatar
import com.tiktokboost.app.ui.components.StatusBadge
import com.tiktokboost.app.ui.components.statusHint
import com.tiktokboost.app.ui.components.relativeTime

@Composable
fun HistoryScreen(onBack: () -> Unit) {
    var tab by remember { mutableStateOf("All") }
    val tabs = listOf("All", "Pending", "Completed")

    val visible = remember(tab, AppState.transactions.size) {
        when (tab) {
            "Pending" -> AppState.transactions.filter { it.status == TxStatus.PENDING || it.status == TxStatus.DISPUTED }
            "Completed" -> AppState.transactions.filter { it.status == TxStatus.VERIFIED || it.status == TxStatus.COMPLETED }
            else -> AppState.transactions.toList()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar("History", onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs) { t ->
                    FilterChip(
                        selected = tab == t,
                        onClick = { tab = t },
                        label = { Text(t) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }
            }

            if (visible.isEmpty()) {
                EmptyState(
                    icon = Icons.Filled.Done,
                    tint = MaterialTheme.colorScheme.secondary,
                    title = if (tab == "Pending") "You're all caught up." else "Nothing here yet.",
                    subtitle = if (tab == "Pending")
                        "No exchanges waiting on confirmation."
                    else
                        "Complete an exchange in Discovery to get started."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(visible, key = { it.id }) { tx ->
                        TransactionRow(tx)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: Transaction) {
    val cs = MaterialTheme.colorScheme
    BrandCard {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GradientAvatar(tx.username, tx.username.hashCode(), 40.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        when (tx.type) {
                            TxType.FOLLOW -> "Exchange · @${tx.username}"
                            TxType.FOLLOW_BACK -> "@${tx.username} followed you back"
                            TxType.PURCHASE -> tx.note.ifBlank { "Purchase" }
                            TxType.BOOST -> tx.note.ifBlank { "Boost" }
                            TxType.PREMIUM -> tx.note.ifBlank { "Subscription" }
                            TxType.STREAK -> tx.note.ifBlank { "Streak bonus" }
                            TxType.ACHIEVEMENT -> tx.note.ifBlank { "Achievement" }
                            TxType.BONUS -> tx.note.ifBlank { "Bonus" }
                        },
                        style = MaterialTheme.typography.titleSmall,
                        color = cs.onSurface,
                        maxLines = 1
                    )
                    Text(
                        statusHint(tx.status) + " · " + relativeTime(tx.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
                Spacer(Modifier.width(8.dp))
                StatusBadge(tx.status)
            }
            if (tx.status == TxStatus.PENDING || tx.status == TxStatus.DISPUTED) {
                Spacer(Modifier.height(8.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        if (tx.coins >= 0) "+${tx.coins} coins on hold" else "${tx.coins} coins",
                        style = MaterialTheme.typography.labelMedium,
                        color = cs.onSurfaceVariant
                    )
                }
            }
        }
    }
}
