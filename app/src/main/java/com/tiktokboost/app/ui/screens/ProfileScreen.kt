package com.tiktokboost.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.data.Trust
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.GradientAvatar
import com.tiktokboost.app.ui.components.SecondaryButton
import com.tiktokboost.app.ui.components.StatBox
import com.tiktokboost.app.ui.components.TrustBadge
import com.tiktokboost.app.ui.components.TrustProgress
import com.tiktokboost.app.ui.components.openTikTok
import com.tiktokboost.app.ui.components.trustColor

@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onSettings: () -> Unit,
    onHistory: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(AppState.displayName) }
    var tiktok by remember { mutableStateOf(AppState.tiktokUsername) }
    var saved by remember { mutableStateOf("") }

    val cs = MaterialTheme.colorScheme

    Scaffold(containerColor = cs.background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenH)
        ) {
            Spacer(Modifier.height(18.dp))

            // ── header ────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                GradientAvatar(AppState.displayName.ifBlank { "T" }, 0, 72.dp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        AppState.displayName.ifBlank { "Creator" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = cs.onSurface
                    )
                    Text("@${AppState.tiktokUsername}", color = cs.secondary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        AppState.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = cs.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            TextButton(onClick = { openTikTok(context, "https://www.tiktok.com/@${AppState.tiktokUsername}") }) {
                Text("Open my TikTok profile ↗", color = cs.secondary, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))

            // ── trust card ────────────────────────────────────────────────
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TrustBadge(level = AppState.myTrustLevel, animate = true)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "Trust score ${AppState.myTrustScore}/100",
                            style = MaterialTheme.typography.labelMedium,
                            color = cs.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    TrustProgress(successfulExchanges = AppState.myExchanges)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatBox("Score", "${AppState.myTrustScore}", Modifier.weight(1f))
                        StatBox("Exchanges", "${AppState.myExchanges}", Modifier.weight(1f))
                        StatBox("Disputes", "${AppState.myDisputes}", Modifier.weight(1f))
                        StatBox("Rate", "${AppState.myCompletionRate}%", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(12.dp))
                    // the ladder
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(cs.surfaceVariant)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Trust.tiers.forEach { tier ->
                            val active = AppState.myTrustLevel == tier.level
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    "L${tier.level}",
                                    color = trustColor(tier.level),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    tier.name,
                                    color = if (active) trustColor(tier.level) else cs.onSurfaceVariant,
                                    fontSize = 9.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        Trust.toNextLevel(AppState.myExchanges)?.let { n ->
                            "Complete $n more verified exchange${if (n == 1) "" else "s"} to level up."
                        } ?: "You've reached Elite — the highest trust level. 🏆",
                        style = MaterialTheme.typography.bodySmall,
                        color = cs.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox("Followed", "${AppState.followedCount}", Modifier.weight(1f))
                StatBox("Followed back", "${AppState.returnedCount}", Modifier.weight(1f))
                StatBox("Coins", "${AppState.coins}", Modifier.weight(1f))
            }

            Spacer(Modifier.height(20.dp))

            // ── edit profile (existing functionality) ─────────────────────
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    Text("Edit profile", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Display name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tiktok,
                        onValueChange = { tiktok = it },
                        label = { Text("TikTok username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (saved.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(saved, color = cs.secondary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    BrandButton("Save changes") {
                        Session.displayName = name.trim().ifBlank { "Creator" }
                        Session.tiktokUsername = tiktok.trim().removePrefix("@").ifBlank { "creator" }
                        AppState.refresh()
                        saved = "Saved ✓"
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            SecondaryButton("View history", onClick = onHistory, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SecondaryButton("Settings", onClick = onSettings, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text("Sign out", color = cs.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
