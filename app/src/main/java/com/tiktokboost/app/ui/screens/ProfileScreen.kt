package com.tiktokboost.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import com.tiktokboost.app.ui.theme.GoodGreen
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
import com.tiktokboost.app.ui.components.ProfileAvatar
import com.tiktokboost.app.ui.components.GradientAvatar
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import java.io.File
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
    val picturePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        try {
            val out = File(context.filesDir, "profile.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                out.outputStream().use { input.copyTo(it) }
            }
            Session.profilePicturePath = out.absolutePath
            AppState.refresh()
        } catch (e: Exception) { /* keep initials avatar */ }
    }
    var saving by remember { mutableStateOf<String?>(null) }
    var editItem by remember { mutableStateOf<com.tiktokboost.app.data.ContentItem?>(null) }
    var deleteItem by remember { mutableStateOf<com.tiktokboost.app.data.ContentItem?>(null) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        saving = "Saving photo…"
        Thread {
            try {
                val f = java.io.File(context.filesDir, "content_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input -> f.outputStream().use { input.copyTo(it) } }
                AppState.addContent(
                    com.tiktokboost.app.data.ContentItem(
                        "c_${System.currentTimeMillis()}", com.tiktokboost.app.data.MediaType.PHOTO,
                        f.absolutePath, "", System.currentTimeMillis(), System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) { /* skip invalid media */ }
            saving = null
        }.start()
    }
    val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        saving = "Saving video…"
        Thread {
            try {
                val f = java.io.File(context.filesDir, "content_${System.currentTimeMillis()}.mp4")
                context.contentResolver.openInputStream(uri)?.use { input -> f.outputStream().use { input.copyTo(it) } }
                AppState.addContent(
                    com.tiktokboost.app.data.ContentItem(
                        "c_${System.currentTimeMillis()}", com.tiktokboost.app.data.MediaType.VIDEO,
                        f.absolutePath, "", System.currentTimeMillis(), System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) { /* skip invalid media */ }
            saving = null
        }.start()
    }
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
                androidx.compose.foundation.layout.Box {
                    ProfileAvatar(AppState.displayName.ifBlank { "T" }, 0, 68.dp)
                    androidx.compose.material3.Surface(
                        onClick = { picturePicker.launch("image/*") },
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = cs.primary,
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        androidx.compose.material3.Text(
                            "✎",
                            color = androidx.compose.ui.graphics.Color.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
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
                    Text(
                        "Account risk: ${AppState.riskLevel().label}",
                        style = MaterialTheme.typography.labelSmall,
                        color = when (AppState.riskLevel()) {
                            com.tiktokboost.app.data.RiskLevel.LOW -> GoodGreen
                            com.tiktokboost.app.data.RiskLevel.MEDIUM -> androidx.compose.ui.graphics.Color(0xFFFFB020)
                            com.tiktokboost.app.data.RiskLevel.HIGH -> androidx.compose.ui.graphics.Color(0xFFFF5A6E)
                        }
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

            // ── my content: photos & short videos on my public profile ──
            Text("My content", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(
                "Photos and videos shown on your public creator profile.",
                style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            com.tiktokboost.app.ui.screens.ContentGrid(
                items = AppState.contentItems,
                onOpen = { },
                editable = true
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SecondaryButton("📷 Add photo", modifier = Modifier.weight(1f)) { photoPicker.launch("image/*") }
                SecondaryButton("🎬 Add video", modifier = Modifier.weight(1f)) { videoPicker.launch("video/*") }
            }
            saving?.let {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = cs.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(it, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                }
            }
            // per-item manage rows
            AppState.contentItems.forEach { item ->
                Row(
                    Modifier.fillMaxWidth().padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        (if (item.mediaType == com.tiktokboost.app.data.MediaType.VIDEO) "🎬" else "📷") +
                            " " + item.caption.ifBlank { "(no caption)" },
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant,
                        maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    androidx.compose.material3.TextButton(onClick = { editItem = item }) { Text("Edit", color = cs.primary, fontSize = 12.sp) }
                    androidx.compose.material3.TextButton(onClick = { deleteItem = item }) { Text("Delete", color = cs.error, fontSize = 12.sp) }
                }
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

    // ── edit caption dialog ────────────────────────────────────────
    editItem?.let { item ->
        var caption by remember(item.id) { mutableStateOf(item.caption) }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { editItem = null },
            title = { Text("Edit caption") },
            text = {
                androidx.compose.material3.OutlinedTextField(
                    value = caption, onValueChange = { caption = it },
                    label = { Text("Caption") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    AppState.updateContent(item.copy(caption = caption.trim(), updatedAt = System.currentTimeMillis()))
                    editItem = null
                }) { Text("Save", color = cs.primary, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { editItem = null }) { Text("Cancel") } }
        )
    }

    // ── delete confirmation ────────────────────────────────────────
    deleteItem?.let { item ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { deleteItem = null },
            title = { Text("Delete this ${if (item.mediaType == com.tiktokboost.app.data.MediaType.VIDEO) "video" else "photo"}?") },
            text = { Text("This removes it from your public profile. This cannot be undone.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    AppState.deleteContent(item.id)
                    deleteItem = null
                }) { Text("Delete", color = cs.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { androidx.compose.material3.TextButton(onClick = { deleteItem = null }) { Text("Cancel") } }
        )
    }
}

private fun relativeJoined(): String = "day ${com.tiktokboost.app.data.Session.accountAgeDays() + 1}"
