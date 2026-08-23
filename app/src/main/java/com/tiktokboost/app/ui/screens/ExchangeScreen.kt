package com.tiktokboost.app.ui.screens

import android.content.Context
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.tiktokboost.app.data.MockData
import com.tiktokboost.app.data.TxStatus
import com.tiktokboost.app.data.User
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.CoinIcon
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.EmptyState
import com.tiktokboost.app.ui.components.GradientAvatar
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
fun ExchangeScreen() {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(MockData.filters.first()) }
    var loading by remember { mutableStateOf(true) }

    // short skeleton pass on first entry — data loads instantly in the demo,
    // but the UI shows the same polished loading state a real backend would.
    LaunchedEffect(Unit) {
        loading = true
        kotlinx.coroutines.delay(700)
        loading = false
    }

    val filtered = remember(query, filter, AppState.transactions.size) {
        MockData.applyFilter(MockData.users, filter).filter { u ->
            query.isBlank() ||
                u.username.contains(query.trim().removePrefix("@"), ignoreCase = true) ||
                u.displayName.contains(query, ignoreCase = true) ||
                u.niche.contains(query, ignoreCase = true)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = Dimens.screenH)) {
            Spacer(Modifier.height(16.dp))
            Text(
                "Creator Discovery",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Follow real creators → they follow you back.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search creators, @username, niche") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(Dimens.cornerControl),
                colors = OutlinedTextFieldDefaults.colors(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = Dimens.screenH),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MockData.filters) { f ->
                FilterChip(
                    selected = filter == f,
                    onClick = { filter = f },
                    label = { Text(f) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        when {
            loading -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = Dimens.screenH, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(3) { SkeletonCreatorCard() }
                }
            }
            filtered.isEmpty() -> {
                EmptyState(
                    icon = Icons.Filled.Search,
                    title = "No creators found.",
                    subtitle = "Try a different name or filter."
                )
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = Dimens.screenH, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filtered, key = { it.id }) { user ->
                        StaggeredAppear(index = filtered.indexOf(user) % 6) {
                            CreatorCard(user, context)
                        }
                    }
                }
            }
        }
    }
}

/** Skeleton placeholder while the creator list "loads". */
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
            ShimmerBox(modifier = Modifier.fillMaxWidth().height(12.dp))
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerBox(modifier = Modifier.width(84.dp).height(24.dp), corner = 12.dp)
                ShimmerBox(modifier = Modifier.width(64.dp).height(24.dp), corner = 12.dp)
                ShimmerBox(modifier = Modifier.width(74.dp).height(24.dp), corner = 12.dp)
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ShimmerBox(modifier = Modifier.weight(1f).height(46.dp))
                ShimmerBox(modifier = Modifier.weight(1f).height(46.dp))
            }
        }
    }
}

@Composable
private fun CreatorCard(user: User, context: Context) {
    val cs = MaterialTheme.colorScheme
    val status = AppState.followStatus(user.id)
    val tx = AppState.transactionFor(user.id)

    BrandCard {
        Column(Modifier.padding(Dimens.card)) {

            // ── identity row ─────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                GradientAvatar(user.displayName, user.hueSeed, 54.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            user.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = cs.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(user.country, fontSize = 13.sp)
                    }
                    Text(
                        "@${user.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painterResource(R.drawable.ic_bolt),
                            contentDescription = null,
                            tint = cs.secondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "${user.platform}: @${user.externalHandle}",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                TrustBadge(level = user.trustLevel)
            }

            Spacer(Modifier.height(8.dp))
            Text(
                user.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = cs.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(10.dp))

            // ── exchange stats ───────────────────────────────────────────
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Stat(icon = { Icon(Icons.Filled.Done, null, tint = GoodGreen, modifier = Modifier.size(13.dp)) },
                     label = "${user.successfulExchanges} exchanges")
                Stat(icon = { Icon(Icons.Filled.CheckCircle, null, tint = cs.tertiary, modifier = Modifier.size(13.dp)) },
                     label = "${user.completionRate}% rate")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CoinIcon(size = 14.dp)
                    Spacer(Modifier.width(4.dp))
                    Text("+${user.coinReward}", color = androidx.compose.ui.graphics.Color(0xFFF5A623), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
                Spacer(Modifier.weight(1f))
                Text(
                    "${formatCount(user.followers)} followers",
                    style = MaterialTheme.typography.bodySmall,
                    color = cs.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── actions by relationship state ────────────────────────────
            when {
                status == null -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SecondaryButton(
                            "Open Profile",
                            modifier = Modifier.weight(1f),
                            onClick = { openTikTok(context, user.profileUrl) }
                        )
                        BrandButton(
                            "I've Completed  +${user.coinReward}",
                            modifier = Modifier.weight(1.25f),
                            onClick = { AppState.completeFollow(user) }
                        )
                    }
                }
                status == "followed" -> {
                    val pending = tx?.status == TxStatus.PENDING
                    val disputed = tx?.status == TxStatus.DISPUTED
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusBadge(tx?.status ?: TxStatus.PENDING)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            statusHint(tx?.status ?: TxStatus.PENDING) + " — @${user.username}",
                            style = MaterialTheme.typography.bodySmall,
                            color = cs.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        SecondaryButton(
                            "Open Profile",
                            modifier = Modifier.weight(1f),
                            onClick = { openTikTok(context, user.profileUrl) }
                        )
                        if (pending || disputed) {
                            BrandButton(
                                "Waiting…",
                                enabled = false,
                                modifier = Modifier.weight(1.25f),
                                onClick = {}
                            )
                        } else {
                            BrandButton(
                                "They followed me back  +5",
                                modifier = Modifier.weight(1.25f),
                                onClick = { AppState.confirmFollowBack(user) }
                            )
                        }
                    }
                    if (pending && tx != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Report an issue",
                            style = MaterialTheme.typography.labelMedium,
                            color = cs.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { AppState.dispute(tx) }
                                .padding(4.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
                else -> {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Filled.CheckCircle, null, tint = GoodGreen, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Exchange complete — +${user.coinReward + MockData.REWARD_FOLLOW_BACK} coins earned",
                            color = GoodGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Stat(icon: @Composable () -> Unit, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
