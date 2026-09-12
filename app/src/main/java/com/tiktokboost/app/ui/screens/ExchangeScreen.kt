package com.tiktokboost.app.ui.screens

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.R
import com.tiktokboost.app.data.EconomyConfig
import com.tiktokboost.app.data.FailureReason
import com.tiktokboost.app.data.MockData
import com.tiktokboost.app.data.PremiumTier
import com.tiktokboost.app.data.TxStatus
import com.tiktokboost.app.data.User
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.CoinIcon
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.EmptyState
import com.tiktokboost.app.ui.components.GradientAvatar
import com.tiktokboost.app.ui.components.PremiumBadge
import com.tiktokboost.app.ui.components.SecondaryButton
import com.tiktokboost.app.ui.components.ShimmerBox
import com.tiktokboost.app.ui.components.StaggeredAppear
import com.tiktokboost.app.ui.components.StatusBadge
import com.tiktokboost.app.ui.components.TrustBadge
import com.tiktokboost.app.ui.components.formatCount
import com.tiktokboost.app.ui.components.openTikTok
import com.tiktokboost.app.ui.components.statusHint
import com.tiktokboost.app.ui.theme.GoodGreen

@Composable
fun ExchangeScreen(onOpenPremium: () -> Unit, onOpenCreator: (String) -> Unit = {}) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(MockData.filters.first()) }
    var category by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }
    var toast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loading = true
        kotlinx.coroutines.delay(700)
        loading = false
    }

    val ranked = remember(query, filter, category, AppState.transactions.size, AppState.premium) {
        AppState.rankedCreators(query, filter, category)
    }

    LaunchedEffect(toast) {
        if (toast != null) {
            kotlinx.coroutines.delay(3200)
            toast = null
        }
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = Dimens.screenH)) {
            Spacer(Modifier.height(14.dp))
            Text(
                "Creator Discovery",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Real creators, ranked by trust & relevance.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search creators, @username, category") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(Dimens.cornerControl),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp)
)
        }

        // main filters
        LazyRow(
            contentPadding = PaddingValues(horizontal = Dimens.screenH),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MockData.filters) { f ->
                FilterChip(
                    selected = filter == f && category == null,
                    onClick = { filter = f; category = null },
                    label = { Text(f) },
                    colors = chipColors()
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        // category filters (advanced filters are a premium perk)
        LazyRow(
            contentPadding = PaddingValues(horizontal = Dimens.screenH),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(EconomyConfig.categories) { c ->
                FilterChip(
                    selected = category == c,
                    onClick = { category = if (category == c) null else c },
                    label = { Text(c) },
                    colors = chipColors()
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        toast?.let { msg ->
            Text(
                msg,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.screenH)
            )
            Spacer(Modifier.height(4.dp))
        }

        when {
            loading -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = Dimens.screenH, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) { items(3) { SkeletonCreatorCard() } }
            }
            ranked.isEmpty() -> {
                EmptyState(
                    icon = Icons.Filled.Search,
                    title = "No creators found.",
                    subtitle = "Try a different search or filter."
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = Dimens.screenH, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(ranked, key = { it.id }) { user ->
                        StaggeredAppear(index = ranked.indexOf(user) % 6) {
                            CreatorCard(user, context, onOpenCreator) { toast = it }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun chipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primary,
    selectedLabelColor = androidx.compose.ui.graphics.Color.White,
    containerColor = MaterialTheme.colorScheme.surfaceVariant,
    labelColor = MaterialTheme.colorScheme.onSurface
)

@Composable
private fun SkeletonCreatorCard() {
    BrandCard {
        Column(Modifier.padding(Dimens.card)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                ShimmerBox(modifier = Modifier.size(52.dp), corner = 26.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.45f).height(16.dp))
                    Spacer(Modifier.height(6.dp))
                    ShimmerBox(modifier = Modifier.fillMaxWidth(0.7f).height(12.dp))
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerBox(modifier = Modifier.width(84.dp).height(24.dp), corner = 12.dp)
                ShimmerBox(modifier = Modifier.width(64.dp).height(24.dp), corner = 12.dp)
                ShimmerBox(modifier = Modifier.width(74.dp).height(24.dp), corner = 12.dp)
            }
        }
    }
}

val reportReasons = listOf("Spam", "Harassment", "Fake profile", "Suspicious behavior", "Repeated false claims", "Other")

@Composable
private fun CreatorCard(user: User, context: Context, onOpenCreator: (String) -> Unit, onToast: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val status = AppState.followStatus(user.id)
    val tx = AppState.transactionFor(user.id)
    val block = AppState.exchangeBlockReason(user)
    var actionsOpen by remember { mutableStateOf(false) }

    if (actionsOpen) {
        AlertDialog(
            onDismissRequest = { actionsOpen = false },
            title = { Text("@${user.username}") },
            text = {
                Column {
                    Text(
                        if (AppState.isBlocked(user.id)) "Unblock this creator" else "Block this creator",
                        style = MaterialTheme.typography.bodyLarge,
                        color = cs.onSurface,
                        modifier = Modifier.fillMaxWidth().clickable {
                            val blocked = AppState.toggleBlock(user.id)
                            onToast(if (blocked) "@${user.username} blocked — hidden from discovery" else "Unblocked @${user.username}")
                            actionsOpen = false
                        }.padding(vertical = 8.dp)
                    )
                    Text(
                        "Report creator",
                        style = MaterialTheme.typography.bodyLarge,
                        color = cs.onSurface,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    )
                    reportReasons.forEach { reason ->
                        Text(
                            reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = cs.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth().clickable {
                                com.tiktokboost.app.data.Session.addReport(user.username, reason)
                                onToast("Report sent for review — thank you")
                                actionsOpen = false
                            }.padding(vertical = 5.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { actionsOpen = false }) { Text("Cancel") }
            }
        )
    }

    BrandCard(onClick = { onOpenCreator(user.id) }, onLongClick = { actionsOpen = true }) {
        Column(Modifier.padding(Dimens.card)) {

            // ── identity: avatar | name/username; badges flow on their OWN row ──
            Row(verticalAlignment = Alignment.Top) {
                GradientAvatar(user.displayName, user.hueSeed, 52.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        user.displayName,
                        style = MaterialTheme.typography.titleMedium, color = cs.onSurface,
                        maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "@${user.username} · ${user.country}",
                        style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(5.dp))
                    // trust + premium badges: full-width row, never squeezed, never vertical
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TrustBadge(level = user.trustLevel)
                        PremiumBadge(tier = user.premium, compact = true)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                "${user.category} · active ${formatActive(user.lastActiveMinutesAgo)}",
                style = MaterialTheme.typography.labelSmall,
                color = cs.primary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(8.dp))
            Text(
                user.bio, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant,
                maxLines = 2, overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))

            // ── stats: rate · exchanges · reward · visibility ──────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null, tint = cs.primary, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${user.completionRate}%", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Done, null, tint = GoodGreen, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${user.successfulExchanges} exch.", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoinIcon(size = 13.dp)
                    Spacer(Modifier.width(4.dp))
                    Text("+${user.coinReward}", color = androidx.compose.ui.graphics.Color(0xFFF5A623), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Spacer(Modifier.weight(1f))
                if (user.activeBoost != null) {
                    Text("🚀 ${user.activeBoost!!.label}", style = MaterialTheme.typography.labelSmall, color = cs.secondary, fontWeight = FontWeight.Bold)
                } else {
                    Text("${formatCount(user.followers)} followers", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── actions by state ──────────────────────────────────────────
            when {
                status == null && block == null -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SecondaryButton("Open Profile", modifier = Modifier.weight(1f)) {
                            openTikTok(context, user.profileUrl)
                        }
                        val successHaptic = com.tiktokboost.app.ui.components.rememberSuccessHaptic()
                        BrandButton(
                            "Complete  +${user.coinReward}",
                            modifier = Modifier.weight(1.2f)
                        ) {
                            successHaptic()
                            val res = AppState.completeFollow(user)
                            onToast(if (res is com.tiktokboost.app.data.EconomyResult.Success) (res.message ?: "Pending confirmation") else AppState.detailFor((res as com.tiktokboost.app.data.EconomyResult.Failure).reason))
                        }
                    }
                }
                status == null && block != null -> {
                    val detail = when (block) {
                        FailureReason.COOLDOWN_ACTIVE -> "Cooldown active — ${AppState.cooldownRemainingMs / 60000 + 1} min left"
                        else -> AppState.detailFor(block)
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.Lock, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(detail, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                    Spacer(Modifier.height(6.dp))
                    SecondaryButton("Open Profile", modifier = Modifier.fillMaxWidth()) {
                        openTikTok(context, user.profileUrl)
                    }
                }
                status == "followed" -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusBadge(tx?.status ?: TxStatus.PENDING)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            statusHint(tx?.status ?: TxStatus.PENDING),
                            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SecondaryButton("Open Profile", modifier = Modifier.weight(1f)) {
                            openTikTok(context, user.profileUrl)
                        }
                        if (tx?.status == TxStatus.PENDING) {
                            BrandButton("Waiting…", enabled = false, modifier = Modifier.weight(1.2f)) {}
                        } else {
                            BrandButton("They followed me back", modifier = Modifier.weight(1.2f)) {
                                val res = AppState.confirmFollowBack(user)
                                if (res is com.tiktokboost.app.data.EconomyResult.Failure) {
                                    onToast(AppState.detailFor(res.reason))
                                }
                            }
                        }
                    }
                    if (tx?.status == TxStatus.PENDING) {
                        Spacer(Modifier.height(6.dp))
                        DisputeInlinePicker(tx = tx)
                    }
                }
                else -> {
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = GoodGreen, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Exchange complete",
                            color = GoodGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

private fun formatActive(minutesAgo: Int): String = when {
    minutesAgo <= 5 -> "now"
    minutesAgo < 60 -> "${minutesAgo}m ago"
    else -> "${minutesAgo / 60}h ago"
}

/** Minimal inline dispute flow: tap → pick a reason → dispute submitted. */
@Composable
private fun DisputeInlinePicker(tx: com.tiktokboost.app.data.Transaction) {
    var open by remember { mutableStateOf(false) }
    Text(
        "Report an issue",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { open = !open }
            .padding(4.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center
    )
    if (open) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            MockData.disputeReasons.forEach { reason ->
                Text(
                    reason,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            AppState.submitDispute(tx, reason)
                            open = false
                        }
                        .padding(8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
