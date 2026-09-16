package com.tiktokboost.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.BoostTaskStatus
import com.tiktokboost.app.data.ContentItem
import com.tiktokboost.app.data.MediaType
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.data.Trust
import com.tiktokboost.app.data.TxType
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.ProfileAvatar
import com.tiktokboost.app.ui.components.SecondaryButton
import com.tiktokboost.app.ui.components.StatBox
import com.tiktokboost.app.ui.components.TrustBadge
import com.tiktokboost.app.ui.components.TrustProgress
import com.tiktokboost.app.ui.components.openTikTok
import com.tiktokboost.app.ui.components.relativeTime
import com.tiktokboost.app.ui.components.trustColor
import com.tiktokboost.app.ui.theme.GoodGreen
import java.io.File

/**
 * Profile — a clean, read-only creator profile. Editing lives on the dedicated
 * EditProfileScreen (explicit Save Changes); content actions live behind ⋯ menus.
 */
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    onSettings: () -> Unit,
    onHistory: () -> Unit,
    onPremium: () -> Unit,
    onAnalytics: () -> Unit,
    onEditProfile: () -> Unit = {}
) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme

    // content management state (uploads + per-item dialogs)
    var saving by remember { mutableStateOf<String?>(null) }
    var editItem by remember { mutableStateOf<ContentItem?>(null) }
    var deleteItem by remember { mutableStateOf<ContentItem?>(null) }
    var contentTab by remember { mutableStateOf("All") }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        saving = "Saving photo…"
        Thread {
            try {
                val f = File(context.filesDir, "content_${System.currentTimeMillis()}.jpg")
                context.contentResolver.openInputStream(uri)?.use { input -> f.outputStream().use { input.copyTo(it) } }
                AppState.addContent(
                    ContentItem(
                        "c_${System.currentTimeMillis()}", MediaType.PHOTO,
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
                val f = File(context.filesDir, "content_${System.currentTimeMillis()}.mp4")
                context.contentResolver.openInputStream(uri)?.use { input -> f.outputStream().use { input.copyTo(it) } }
                AppState.addContent(
                    ContentItem(
                        "c_${System.currentTimeMillis()}", MediaType.VIDEO,
                        f.absolutePath, "", System.currentTimeMillis(), System.currentTimeMillis()
                    )
                )
            } catch (e: Exception) { /* skip invalid media */ }
            saving = null
        }.start()
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.screenH)
    ) {
        Spacer(Modifier.height(18.dp))

        // ═══ PROFILE HEADER (read-only) ═════════════════════════════
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(AppState.displayName.ifBlank { "T" }, 0, 78.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        AppState.displayName.ifBlank { "Creator" },
                        style = MaterialTheme.typography.headlineSmall,
                        color = cs.onSurface,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(6.dp))
                    com.tiktokboost.app.ui.components.PremiumBadge(tier = AppState.premium, compact = true)
                }
                Text(
                    "@${AppState.tiktokUsername}",
                    color = cs.primary, fontSize = 15.sp, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Member since day ${Session.accountAgeDays() + 1}",
                    style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        // bio
        if (AppState.bio.isNotBlank()) {
            Text(
                AppState.bio,
                style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }

        // category chip
        if (AppState.category.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(50))
                        .background(cs.primaryContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        AppState.category,
                        style = MaterialTheme.typography.labelMedium,
                        color = cs.primary, fontWeight = FontWeight.Bold
                    )
                }
                Spacer(Modifier.weight(1f))
                TrustBadge(level = AppState.myTrustLevel)
            }
        } else {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TrustBadge(level = AppState.myTrustLevel)
            }
        }

        Spacer(Modifier.height(12.dp))

        // primary actions
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SecondaryButton(
                text = "Edit Profile",
                icon = Icons.Filled.Edit,
                modifier = Modifier.weight(1f),
                onClick = onEditProfile
            )
            SecondaryButton("Open TikTok", modifier = Modifier.weight(1f)) {
                openTikTok(context, "https://www.tiktok.com/@${AppState.tiktokUsername}")
            }
        }

        Spacer(Modifier.height(10.dp))

        // profile completeness (feeds discovery ranking) — tap to edit
        if (AppState.profileCompleteness < 100) {
            com.tiktokboost.app.ui.components.ProfileCompletionBar(
                percent = AppState.profileCompleteness,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditProfile() }
            )
        }

        Spacer(Modifier.height(14.dp))

        // ═══ STATS ═════════════════════════════════════════════════
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatBox("Coins", "${AppState.coins}", Modifier.weight(1f))
            StatBox("Tasks done", "${AppState.boostTasks.count { it.status == BoostTaskStatus.COMPLETED && !it.createdByMe }}", Modifier.weight(1f))
            StatBox("Boosts bought", "${AppState.transactions.count { it.type == TxType.BOOST && it.coins < 0 }}", Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatBox("Exchanges", "${AppState.myExchanges}", Modifier.weight(1f))
            StatBox("Followed", "${AppState.followedCount}", Modifier.weight(1f))
            StatBox("Lifetime ↑", "${AppState.lifetimeEarned}", Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))

        // ═══ TRUST ═════════════════════════════════════════════════
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
                Spacer(Modifier.height(8.dp))
                Text(
                    Trust.toNextLevel(AppState.myExchanges)?.let { n ->
                        "Complete $n more verified exchange${if (n == 1) "" else "s"} to level up."
                    } ?: "You've reached Elite — the highest trust level. 🏆",
                    style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ═══ ACHIEVEMENTS ══════════════════════════════════════════
        BrandCard {
            Column(Modifier.padding(Dimens.card)) {
                Text("Achievements", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                Spacer(Modifier.height(10.dp))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    com.tiktokboost.app.data.Achievements.all.forEach { a ->
                        val unlocked = a.id in AppState.achievements
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(a.emoji, fontSize = 22.sp)
                            Text(
                                a.title,
                                fontSize = 9.sp, lineHeight = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = if (unlocked) cs.onSurface else cs.onSurfaceVariant,
                                fontWeight = if (unlocked) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ═══ CONTENT ═══════════════════════════════════════════════
        Text("Content", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
        Spacer(Modifier.height(8.dp))

        // filter tabs
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("All", "Photos", "Videos").forEach { tab ->
                FilterChip(
                    selected = contentTab == tab,
                    onClick = { contentTab = tab },
                    label = { Text(tab) }
                )
            }
        }
        Spacer(Modifier.height(10.dp))

        val visibleContent = when (contentTab) {
            "Photos" -> AppState.contentItems.filter { it.mediaType == MediaType.PHOTO }
            "Videos" -> AppState.contentItems.filter { it.mediaType == MediaType.VIDEO }
            else -> AppState.contentItems
        }

        if (visibleContent.isEmpty()) {
            BrandCard {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 26.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📸", fontSize = 32.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("No content yet", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Share your first photo or video.",
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BrandButton("📷 Add Photo", modifier = Modifier.weight(1f)) { photoPicker.launch("image/*") }
                        BrandButton("🎬 Add Video", modifier = Modifier.weight(1f)) { videoPicker.launch("video/*") }
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                visibleContent.forEach { item ->
                    ContentCard(
                        item = item,
                        onEdit = { editItem = item },
                        onDelete = { deleteItem = item }
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SecondaryButton("📷 Add Photo", modifier = Modifier.weight(1f)) { photoPicker.launch("image/*") }
                SecondaryButton("🎬 Add Video", modifier = Modifier.weight(1f)) { videoPicker.launch("video/*") }
            }
        }

        saving?.let {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = cs.primary)
                Spacer(Modifier.width(8.dp))
                Text(it, style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(18.dp))

        // ═══ ACCOUNT ═══════════════════════════════════════════════
        BrandCard {
            Column(Modifier.padding(vertical = 6.dp, horizontal = 8.dp)) {
                AccountRow("🕘", "Transaction history", cs, onHistory)
                AccountRow("📊", "Analytics", cs, onAnalytics)
                if (AppState.premium == com.tiktokboost.app.data.PremiumTier.FREE) {
                    AccountRow("💎", "Upgrade to Premium", cs, onPremium)
                } else {
                    AccountRow("💎", "Manage subscription", cs, onPremium)
                }
                AccountRow("⚙️", "Settings", cs, onSettings)
            }
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
            Text("Sign out", color = cs.onSurfaceVariant, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(24.dp))
    }

    // ── edit caption dialog (from ⋯ → Edit) ─────────────────────────
    editItem?.let { item ->
        var caption by remember(item.id) { mutableStateOf(item.caption) }
        AlertDialog(
            onDismissRequest = { editItem = null },
            title = { Text("Edit content") },
            text = {
                Column {
                    Text(
                        "${if (item.mediaType == MediaType.VIDEO) "Video" else "Photo"} · ${relativeTime(item.createdAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = caption,
                        onValueChange = { caption = it },
                        label = { Text("Caption") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    AppState.updateContent(item.copy(caption = caption.trim(), updatedAt = System.currentTimeMillis()))
                    editItem = null
                }) { Text("Save Changes", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { editItem = null }) { Text("Cancel") }
            }
        )
    }

    // ── delete confirmation (from ⋯ → Delete) ────────────────────────
    deleteItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteItem = null },
            title = { Text("Delete this content?") },
            text = { Text("This removes it from your public profile. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    AppState.deleteContent(item.id)
                    deleteItem = null
                }) { Text("Delete", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { deleteItem = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AccountRow(icon: String, label: String, cs: androidx.compose.material3.ColorScheme, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = cs.onSurface,
            modifier = Modifier.weight(1f)
        )
        Text("›", color = cs.onSurfaceVariant, fontSize = 20.sp)
    }
}

/**
 * Professional content card: thumbnail with type indicator, caption, date,
 * and a ⋯ menu (Edit / Delete behind it — never permanently visible).
 */
@Composable
private fun ContentCard(item: ContentItem, onEdit: () -> Unit, onDelete: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    var menuOpen by remember { mutableStateOf(false) }

    BrandCard {
        Row(
            Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // thumbnail + type indicator
            Box(
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(listOf(cs.surfaceVariant, cs.surface))
                    )
            ) {
                val bmp = remember(item.path) {
                    item.path?.let { android.graphics.BitmapFactory.decodeFile(it) }
                }
                when {
                    bmp != null -> androidx.compose.foundation.Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = item.caption.ifBlank { "Content" },
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    else -> Text(
                        if (item.mediaType == MediaType.VIDEO) "🎬" else "📷",
                        fontSize = 24.sp,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                if (item.mediaType == MediaType.VIDEO) {
                    Box(
                        Modifier
                            .align(Alignment.Center)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(cs.primary.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Video",
                            tint = androidx.compose.ui.graphics.Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.caption.ifBlank { "No caption" },
                    style = MaterialTheme.typography.titleSmall,
                    color = cs.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${if (item.mediaType == MediaType.VIDEO) "Video" else "Photo"} · ${relativeTime(item.createdAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant
                )
            }
            // ⋯ menu — actions hidden until requested
            Box {
                IconButton(
                    onClick = { menuOpen = true },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "More options",
                        tint = cs.onSurfaceVariant
                    )
                }
                DropdownMenu(
                    expanded = menuOpen,
                    onDismissRequest = { menuOpen = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = { menuOpen = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                "Delete",
                                color = cs.error,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        onClick = { menuOpen = false; onDelete() }
                    )
                }
            }
        }
    }
}
