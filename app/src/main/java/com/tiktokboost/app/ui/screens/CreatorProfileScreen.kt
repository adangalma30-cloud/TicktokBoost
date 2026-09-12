package com.tiktokboost.app.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.tiktokboost.app.data.ContentItem
import com.tiktokboost.app.data.EconomyResult
import com.tiktokboost.app.data.FailureReason
import com.tiktokboost.app.data.MediaType
import com.tiktokboost.app.data.MockData
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.data.TxStatus
import com.tiktokboost.app.data.User
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.AppTopBar
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.GradientAvatar
import com.tiktokboost.app.ui.components.MatchChip
import com.tiktokboost.app.ui.components.PremiumBadge
import com.tiktokboost.app.ui.components.SecondaryButton
import com.tiktokboost.app.ui.components.StatBox
import com.tiktokboost.app.ui.components.TrustBadge
import com.tiktokboost.app.ui.components.openTikTok
import com.tiktokboost.app.ui.components.statusHint

/**
 * Public creator profile — opened from For You / Discover cards.
 * Profile header → bio/category → trust + stats → primary actions →
 * content grid → achievements → shared activity.
 */
@Composable
fun CreatorProfileScreen(userId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val user = remember(userId) { MockData.users.firstOrNull { it.id == userId } }
    val cs = MaterialTheme.colorScheme

    if (user == null) {
        Scaffold(containerColor = cs.background, topBar = { AppTopBar("Creator", onBack) }) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🚫", fontSize = 40.sp)
                Spacer(Modifier.height(10.dp))
                Text("This creator is no longer available.", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                Spacer(Modifier.height(16.dp))
                SecondaryButton("Go back", modifier = Modifier.width(160.dp), onClick = onBack)
            }
        }
        return
    }

    var actionsOpen by remember { mutableStateOf(false) }
    var reportOpen by remember { mutableStateOf(false) }
    var viewing by remember { mutableStateOf<ContentItem?>(null) }
    var toast by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = cs.background,
        topBar = { AppTopBar("@${user.username}", onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenH)
        ) {
            Spacer(Modifier.height(14.dp))

            // ── header ─────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                GradientAvatar(user.displayName, user.hueSeed, 72.dp)
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        user.displayName,
                        style = MaterialTheme.typography.headlineSmall, color = cs.onSurface,
                        maxLines = 2, overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "@${user.username} · ${user.country}",
                            style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant,
                            maxLines = 1, overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (user.premium != com.tiktokboost.app.data.PremiumTier.FREE) {
                            Spacer(Modifier.width(6.dp))
                            PremiumBadge(tier = user.premium, compact = true)
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(user.category, style = MaterialTheme.typography.labelSmall, color = cs.primary, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(8.dp))
                        MatchChip(percent = AppState.matchScore(user))
                    }
                }
                TrustBadge(level = user.trustLevel)
            }

            Spacer(Modifier.height(10.dp))

            // ── bio ────────────────────────────────────────────────
            Text(
                user.bio,
                style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
            )

            Spacer(Modifier.height(14.dp))

            // ── trust + stats ──────────────────────────────────────
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TrustBadge(level = user.trustLevel)
                        Spacer(Modifier.weight(1f))
                        Text("Trust score ${user.trustScore}/100", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBox("Exchanges", "${user.successfulExchanges}", Modifier.weight(1f))
                        StatBox("Rate", "${user.completionRate}%", Modifier.weight(1f))
                        StatBox("Disputes", "${user.disputes}", Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── primary actions (existing exchange flow) ────────────
            CreatorActions(user, context) { toast = it }

            // secondary
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TextButton(onClick = { reportOpen = true }) {
                    Text("Report", color = cs.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                TextButton(onClick = { actionsOpen = true }) {
                    Text(
                        if (AppState.isBlocked(user.id)) "Unblock" else "Block",
                        color = cs.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 13.sp
                    )
                }
            }

            toast?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.labelMedium, color = cs.primary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(14.dp))

            // ── content ────────────────────────────────────────────
            Text("Content", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            Spacer(Modifier.height(4.dp))
            Text(
                "Shared by the creator on TickTokBoost",
                style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            val content = remember(user.id) { MockData.contentFor(user) }
            ContentGrid(items = content, onOpen = { viewing = it })

            Spacer(Modifier.height(16.dp))

            // ── achievements ────────────────────────────────────────
            Text("Achievements", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            Spacer(Modifier.height(8.dp))
            BrandCard {
                Row(
                    Modifier.fillMaxWidth().padding(Dimens.card),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    AchievementTile("🤝", "First Exchange", user.successfulExchanges >= 1)
                    AchievementTile("🏅", "10 Exchanges", user.successfulExchanges >= 10)
                    AchievementTile("🛡️", "Trusted", user.trustLevel >= 3)
                    AchievementTile("🏆", "Elite", user.trustLevel >= 5)
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── shared activity ─────────────────────────────────────
            val shared = AppState.transactions.filter { it.username == user.username }.take(3)
            if (shared.isNotEmpty()) {
                Text("Your activity together", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                Spacer(Modifier.height(8.dp))
                shared.forEach { tx ->
                    BrandCard {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(tx.note.ifBlank { tx.type.name }, style = MaterialTheme.typography.titleSmall, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(com.tiktokboost.app.ui.components.relativeTime(tx.createdAt), style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                            }
                            com.tiktokboost.app.ui.components.StatusBadge(tx.status)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── dialogs ───────────────────────────────────────────────────
    if (viewing != null) {
        MediaViewerDialog(item = viewing!!, onDismiss = { viewing = null })
    }
    if (actionsOpen) {
        AlertDialog(
            onDismissRequest = { actionsOpen = false },
            title = { Text(if (AppState.isBlocked(user.id)) "Unblock @${user.username}?" else "Block @${user.username}?") },
            text = { Text(if (AppState.isBlocked(user.id)) "They will appear in your discovery again." else "They will be hidden from your discovery and can't exchange with you. Your history is preserved.") },
            confirmButton = {
                TextButton(onClick = {
                    val blocked = AppState.toggleBlock(user.id)
                    toast = if (blocked) "@${user.username} blocked" else "Unblocked @${user.username}"
                    actionsOpen = false
                }) { Text(if (AppState.isBlocked(user.id)) "Unblock" else "Block", color = cs.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { actionsOpen = false }) { Text("Cancel") } }
        )
    }
    if (reportOpen) {
        AlertDialog(
            onDismissRequest = { reportOpen = false },
            title = { Text("Report @${user.username}") },
            text = {
                Column {
                    com.tiktokboost.app.ui.screens.reportReasons.forEach { reason ->
                        Text(
                            reason,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth()
                                .clickable { Session.addReport(user.username, reason); reportOpen = false; toast = "Report sent for review — thank you" }
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { reportOpen = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun CreatorActions(user: User, context: Context, onToast: (String) -> Unit) {
    val status = AppState.followStatus(user.id)
    val tx = AppState.transactionFor(user.id)
    val block = AppState.exchangeBlockReason(user)

    Column {
        when {
            status == null && block == null -> {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton("Open TikTok Profile", modifier = Modifier.weight(1f)) { openTikTok(context, user.profileUrl) }
                    BrandButton("Complete  +${user.coinReward}", modifier = Modifier.weight(1f)) {
                        val res = AppState.completeFollow(user)
                        onToast(if (res is EconomyResult.Success) (res.message ?: "Pending confirmation") else AppState.detailFor((res as EconomyResult.Failure).reason))
                    }
                }
            }
            status == null && block != null -> {
                SecondaryButton("Open TikTok Profile", modifier = Modifier.fillMaxWidth()) { openTikTok(context, user.profileUrl) }
                Spacer(Modifier.height(6.dp))
                Text(
                    when (block) {
                        FailureReason.COOLDOWN_ACTIVE -> "Cooldown active — ${AppState.cooldownRemainingMs / 60000 + 1} min left"
                        else -> AppState.detailFor(block)
                    },
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
            }
            status == "followed" -> {
                com.tiktokboost.app.ui.components.StatusBadge(tx?.status ?: TxStatus.PENDING)
                Spacer(Modifier.height(4.dp))
                Text(statusHint(tx?.status ?: TxStatus.PENDING), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SecondaryButton("Open TikTok Profile", modifier = Modifier.weight(1f)) { openTikTok(context, user.profileUrl) }
                    if (tx?.status == TxStatus.PENDING) {
                        BrandButton("Waiting…", enabled = false, modifier = Modifier.weight(1f)) {}
                    } else {
                        BrandButton("They followed me back", modifier = Modifier.weight(1f)) {
                            val res = AppState.confirmFollowBack(user)
                            if (res is EconomyResult.Failure) onToast(AppState.detailFor(res.reason))
                        }
                    }
                }
            }
            else -> {
                Text(
                    "✓ Exchange complete",
                    color = com.tiktokboost.app.ui.theme.GoodGreen, fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                SecondaryButton("Open TikTok Profile", modifier = Modifier.fillMaxWidth()) { openTikTok(context, user.profileUrl) }
            }
        }
    }
}

@Composable
private fun AchievementTile(emoji: String, label: String, earned: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 22.sp, color = if (earned) androidx.compose.ui.graphics.Color.Unspecified else androidx.compose.ui.graphics.Color(0xFF5A5A6E))
        Text(
            label, fontSize = 9.sp, lineHeight = 11.sp, textAlign = TextAlign.Center,
            color = if (earned) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ═══ content grid + media viewer ═══════════════════════════════════════════

@Composable
fun ContentGrid(items: List<ContentItem>, onOpen: (ContentItem) -> Unit, editable: Boolean = false, onEdit: ((ContentItem) -> Unit)? = null, onDelete: ((ContentItem) -> Unit)? = null) {
    if (items.isEmpty()) {
        BrandCard {
            Column(
                Modifier.fillMaxWidth().padding(vertical = 28.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("📸", fontSize = 32.sp)
                Spacer(Modifier.height(8.dp))
                Text("No content yet", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(
                    if (editable) "Add your first photo or video below" else "This creator hasn't shared content yet",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { item ->
                    ContentTile(item, Modifier.weight(1f), onOpen)
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun ContentTile(item: ContentItem, modifier: Modifier = Modifier, onOpen: (ContentItem) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(
        modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(cs.surfaceVariant, cs.surface)))
            .clickable { onOpen(item) }
    ) {
        if (item.demoVisual != null) {
            Text(
                item.demoVisual,
                fontSize = 30.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (item.path != null) {
            val bmp = remember(item.path) { android.graphics.BitmapFactory.decodeFile(item.path) }
            if (bmp != null) {
                androidx.compose.foundation.Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = item.caption.ifBlank { "Content" },
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text("🖼️", fontSize = 26.sp, modifier = Modifier.align(Alignment.Center))
            }
        } else {
            Text("🖼️", fontSize = 26.sp, modifier = Modifier.align(Alignment.Center))
        }
        if (item.mediaType == MediaType.VIDEO) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(cs.primary.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Play video", tint = androidx.compose.ui.graphics.Color.Black, modifier = Modifier.size(20.dp))
            }
        }
        if (item.caption.isNotBlank()) {
            Text(
                item.caption,
                style = MaterialTheme.typography.labelSmall,
                color = androidx.compose.ui.graphics.Color.White,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }
    }
}

/** Full media viewer: photos zoom to a dialog; videos play with loading/error states. */
@Composable
fun MediaViewerDialog(item: ContentItem, onDismiss: () -> Unit) {
    if (item.mediaType == MediaType.VIDEO && item.path != null) {
        VideoPlayerDialog(item, onDismiss)
        return
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item.mediaType == MediaType.VIDEO) "Video" else "Photo", style = MaterialTheme.typography.titleSmall) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (item.path != null) {
                    val bmp = remember(item.path) { android.graphics.BitmapFactory.decodeFile(item.path) }
                    if (bmp != null) {
                        androidx.compose.foundation.Image(
                            bitmap = bmp.asImageBitmap(),
                            contentDescription = null,
                            contentScale = androidx.compose.ui.layout.ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        )
                    }
                } else {
                    Text(item.demoVisual ?: "🎬", fontSize = 64.sp)
                }
                if (item.caption.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(item.caption, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
                if (item.mediaType == MediaType.VIDEO) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Demo video content — playback available for your own uploads.",
                        style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun VideoPlayerDialog(item: ContentItem, onDismiss: () -> Unit) {
    var state by remember { mutableStateOf("loading") } // loading | ready | error
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Video", style = MaterialTheme.typography.titleSmall) },
        text = {
            Column {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(androidx.compose.ui.graphics.Color.Black)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoURI(Uri.parse(item.path))
                                setMediaController(MediaController(ctx).apply { setAnchorView(this@apply) })
                                setOnPreparedListener { state = "ready"; it.start() }
                                setOnErrorListener { _, _, _ -> state = "error"; true }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    when (state) {
                        "loading" -> CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Center).size(34.dp)
                        )
                        "error" -> Column(
                            Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("⚠️", fontSize = 28.sp)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Couldn't play this video.",
                                color = androidx.compose.ui.graphics.Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                if (item.caption.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(item.caption, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
