package com.tiktokboost.app.ui.screens

import android.content.Context
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.MockData
import com.tiktokboost.app.data.User
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.Chip
import com.tiktokboost.app.ui.components.GradientAvatar
import com.tiktokboost.app.ui.components.GradientButton
import com.tiktokboost.app.ui.components.formatCount
import com.tiktokboost.app.ui.components.openTikTok
import com.tiktokboost.app.ui.theme.CardBg
import com.tiktokboost.app.ui.theme.ChipBg
import com.tiktokboost.app.ui.theme.TextPrimary
import com.tiktokboost.app.ui.theme.TextSecondary
import com.tiktokboost.app.ui.theme.TikTokCyan
import com.tiktokboost.app.ui.theme.TikTokPink

@Composable
fun ExchangeScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf("All") }

    val niches = remember { listOf("All") + MockData.users.map { it.niche }.distinct() }

    val filtered = remember(query, filter) {
        MockData.users.filter { u ->
            (filter == "All" || u.niche == filter) &&
                (query.isBlank() ||
                    u.username.contains(query.trim().removePrefix("@"), ignoreCase = true) ||
                    u.displayName.contains(query, ignoreCase = true))
        }
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            Spacer(Modifier.height(20.dp))
            Text(
                "Follow Exchange",
                color = TextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "Follow real creators → they follow you back.",
                color = TextSecondary,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search @username") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
        }

        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(niches) { n ->
                FilterChip(
                    selected = filter == n,
                    onClick = { filter = n },
                    label = { Text(n) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TikTokPink,
                        selectedLabelColor = Color.White,
                        containerColor = ChipBg,
                        labelColor = TextPrimary
                    )
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filtered, key = { it.id }) { user ->
                ExchangeCard(user, context)
            }
            if (filtered.isEmpty()) {
                item {
                    Text(
                        "No creators found. Try another search.",
                        color = TextSecondary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ExchangeCard(user: User, context: Context) {
    val status = AppState.followStatus(user.id)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                GradientAvatar(user.displayName, user.hueSeed, 52.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "@${user.username}",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(user.country, fontSize = 13.sp)
                    }
                    Text(
                        user.bio,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(user.niche)
                Chip("Seeking ~${formatCount(user.followersRequested)}")
                Chip("${formatCount(user.followers)} followers")
            }

            Spacer(Modifier.height(12.dp))

            when (status) {
                null -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            onClick = { openTikTok(context, user.profileUrl) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Open TikTok")
                        }
                        GradientButton(
                            text = "I've Followed  +10",
                            onClick = { AppState.confirmFollow(user) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                "followed" -> {
                    Text(
                        "\u2713 You followed @${user.username} — waiting for a follow-back",
                        color = TikTokCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    OutlinedButton(
                        onClick = { openTikTok(context, user.profileUrl) },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open their profile again")
                    }
                    Spacer(Modifier.height(8.dp))
                    GradientButton(
                        text = "They followed me back  +5",
                        onClick = { AppState.confirmFollowBack(user) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {
                    Text(
                        "\u2713\u2713 Mutual follow!  +${com.tiktokboost.app.data.MockData.REWARD_FOLLOW + com.tiktokboost.app.data.MockData.REWARD_FOLLOW_BACK} \uD83E\uDE99",
                        color = Color(0xFFFFC233),
                        fontWeight = FontWeight.Bold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}
