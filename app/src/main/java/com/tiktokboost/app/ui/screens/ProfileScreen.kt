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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.Achievements
import com.tiktokboost.app.data.EconomyConfig
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.data.Trust
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.GradientAvatar
import com.tiktokboost.app.ui.components.PremiumBadge
import com.tiktokboost.app.ui.components.ProfileCompletionBar
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
    onHistory: () -> Unit,
    onPremium: () -> Unit,
    onAnalytics: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(AppState.displayName) }
    var tiktok by remember { mutableStateOf(AppState.tiktokUsername) }
    var bio by remember { mutableStateOf(AppState.bio) }
    var category by remember { mutableStateOf(AppState.category.ifBlank { EconomyConfig.categories.first() }) }
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

            // ── header ─────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                GradientAvatar(AppState.displayName.ifBlank { "T" }, 0, 68.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            AppState.displayName.ifBlank { "Creator" },
                            style = MaterialTheme.typography.headlineSmall, color = cs.onSurface,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(Modifier.width(6.dp))
                        PremiumBadge(tier = AppState.premium, compact = true)
                    }
                    Text("@${AppState.tiktokUsername}", color = cs.primary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "${AppState.email} · joined ${relativeJoined()}",
                        style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant, maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            TextButton(onClick = { openTikTok(context, "https://www.tiktok.com/@${AppState.tiktokUsername}") }) {
                Text("Open my TikTok profile ↗", color = cs.primary, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(10.dp))

            // ── profile completion ─────────────────────────────────────
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    ProfileCompletionBar(percent = AppState.profileCompleteness)
                    if (AppState.profileCompleteness < 100) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Complete profiles rank higher in discovery — add what's missing below.",
                            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── trust card ─────────────────────────────────────────────
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TrustBadge(level = AppState.myTrustLevel, animate = true)
                        Spacer(Modifier.weight(1f))
                        Text(
                            "Trust score ${AppState.myTrustScore}/100",
                            style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    TrustProgress(successfulExchanges = AppState.myExchanges)
                    Spacer(Modifier.height(12.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBox("Score", "${AppState.myTrustScore}", Modifier.weight(1f))
                        StatBox("Exchanges", "${AppState.myExchanges}", Modifier.weight(1f))
                        StatBox("Disputes", "${AppState.myDisputes}", Modifier.weight(1f))
                        StatBox("Rate", "${AppState.myCompletionRate}%", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier.fillMaxWidth().padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Trust.tiers.forEach { tier ->
                            val active = AppState.myTrustLevel == tier.level
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("L${tier.level}", color = trustColor(tier.level), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                Text(
                                    tier.name,
                                    color = if (active) trustColor(tier.level) else cs.onSurfaceVariant,
                                    fontSize = 9.sp,
                                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        Trust.toNextLevel(AppState.myExchanges)?.let { n ->
                            "Complete $n more verified exchange${if (n == 1) "" else "s"} to level up."
                        } ?: "You've reached Elite — the highest trust level. 🏆",
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                    )
                    Text(
                        "Account age: ${Session.accountAgeDays()} days · suspicious flags: ${Session.suspiciousFlags}",
                        style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── achievements ───────────────────────────────────────────
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    Text("Achievements", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Achievements.all.forEach { a ->
                            val unlocked = a.id in AppState.achievements
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    a.emoji,
                                    fontSize = 22.sp,
                                    color = if (unlocked) cs.onSurface else cs.outline,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                                Text(
                                    a.title,
                                    fontSize = 9.sp,
                                    lineHeight = 11.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = if (unlocked) cs.onSurface else cs.outline,
                                    fontWeight = if (unlocked) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatBox("Followed", "${AppState.followedCount}", Modifier.weight(1f))
                StatBox("Followed back", "${AppState.returnedCount}", Modifier.weight(1f))
                StatBox("Lifetime ↑", "${AppState.lifetimeEarned}", Modifier.weight(1f))
            }

            Spacer(Modifier.height(18.dp))

            // ── edit profile (all completeness fields) ───────────────────
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    Text("Edit profile", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Display name") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tiktok, onValueChange = { tiktok = it },
                        label = { Text("TikTok username") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = bio, onValueChange = { bio = it },
                        label = { Text("Bio") },
                        supportingText = { Text("${bio.length}/160") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Category", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(EconomyConfig.categories.size) { i ->
                            val c = EconomyConfig.categories[i]
                            androidx.compose.material3.FilterChip(
                                selected = category == c,
                                onClick = { category = c },
                                label = { Text(c) }
                            )
                        }
                    }
                    if (saved.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(saved, color = cs.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    BrandButton("Save changes") {
                        Session.displayName = name.trim().ifBlank { "Creator" }
                        Session.tiktokUsername = tiktok.trim().removePrefix("@").ifBlank { "creator" }
                        Session.bio = bio.trim().take(160)
                        Session.category = category
                        AppState.checkAchievements()
                        AppState.refresh()
                        saved = "Saved ✓"
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            SecondaryButton("View history", onClick = onHistory, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            SecondaryButton("Analytics", onClick = onAnalytics, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            if (AppState.premium == com.tiktokboost.app.data.PremiumTier.FREE) {
                SecondaryButton("Upgrade to Premium 💎", onClick = onPremium, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
            }
            SecondaryButton("Settings", onClick = onSettings, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text("Sign out", color = cs.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

private fun relativeJoined(): String = "day ${com.tiktokboost.app.data.Session.accountAgeDays() + 1}"
