package com.tiktokboost.app.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.R
import com.tiktokboost.app.data.Trust
import com.tiktokboost.app.data.TxStatus
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.theme.SigGradientStart
import com.tiktokboost.app.ui.theme.SigGradientEnd
import com.tiktokboost.app.data.PremiumTier
import com.tiktokboost.app.data.BoostTier
import com.tiktokboost.app.ui.theme.BrandCyan
import com.tiktokboost.app.ui.theme.BrandPink
import com.tiktokboost.app.ui.theme.CoinGold
import com.tiktokboost.app.ui.theme.CoinGoldDeep
import com.tiktokboost.app.ui.theme.TikTokPink
import com.tiktokboost.app.ui.theme.TrustL1
import com.tiktokboost.app.ui.theme.TrustL2
import com.tiktokboost.app.ui.theme.TrustL3
import com.tiktokboost.app.ui.theme.TrustL4
import com.tiktokboost.app.ui.theme.TrustL5
import kotlinx.coroutines.delay
import java.util.Locale
import kotlin.math.hypot
import kotlin.math.min

// ═══════════════════════════════ spacing tokens ═══════════════════════════════

object Dimens {
    val screenH = 20.dp
    val card = 16.dp
    val gap = 10.dp
    val cornerCard = 20.dp
    val cornerControl = 14.dp
    val buttonHeight = 52.dp
}

// ═══════════════════════════════ logo (matches the app icon mark) ═══════════════

/**
 * The TickTokBoost brand mark — the official "Motion Lock" TB emblem
 * (see [TbEmblem]). Identical to the launcher icon; used on the splash,
 * Home header, Welcome, auth and Settings screens.
 */
@Composable
fun LogoMark(modifier: Modifier = Modifier, size: Dp) {
    TbEmblem(modifier = modifier, size = size)
}

/** Wordmark: brand mark + product name. Used on Home and auth screens. */
@Composable
fun Wordmark(modifier: Modifier = Modifier, markSize: Dp = 30.dp) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        LogoMark(size = markSize)
        Spacer(Modifier.width(8.dp))
        Text(
            "TickTokBoost",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

// ═══════════════════════════════ press feedback ═══════════════════════════════

/** Light haptic for meaningful moments (claims, purchases, completions). */
@Composable
fun rememberSuccessHaptic(): () -> Unit {
    val hf = androidx.compose.ui.platform.LocalHapticFeedback.current
    return { hf.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress) }
}

/** Smooth press-down scale driven by an interaction source you share with the clickable. */
@Composable
fun rememberPressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.97f,
    enabled: Boolean = true
): Float {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = 900f),
        label = "pressScale"
    )
    return scale
}

// ═══════════════════════════════ buttons ═══════════════════════════════

/** Primary CTA — brand gradient, bold, with smooth press feedback. */
@Composable
fun BrandButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction, enabled = enabled)
    Box(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(Dimens.cornerControl))
            .background(
                Brush.horizontalGradient(
                    if (enabled) listOf(SigGradientStart, SigGradientEnd) else listOf(Color(0xFF8A8A99), Color(0xFF7A7A8C))
                )
            )
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeight),
            shape = RoundedCornerShape(Dimens.cornerControl),
            interactionSource = interaction,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

/** Secondary action — tonal surface button. */
@Composable
fun SecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val scale = rememberPressScale(interaction, enabled = enabled)
    val cs = MaterialTheme.colorScheme
    Box(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(Dimens.cornerControl))
            .background(cs.surfaceVariant)
    ) {
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeight),
            shape = RoundedCornerShape(Dimens.cornerControl),
            interactionSource = interaction,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = cs.onSurface
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp)
        ) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
            }
            Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

/** v0.0.1 name kept working — now renders as the standard brand button. */
@Composable
fun GradientButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    BrandButton(text = text, modifier = modifier, enabled = enabled, onClick = onClick)
}

// ═══════════════════════════════ cards ═══════════════════════════════

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun BrandCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(Dimens.cornerCard)
    val base = modifier.clip(shape).background(cs.surface)
    if (onClick == null && onLongClick == null) {
        Box(base) { content() }
    } else {
        val interaction = remember { MutableInteractionSource() }
        val scale = rememberPressScale(interaction, pressedScale = 0.98f)
        Box(
            base
                .graphicsLayer { scaleX = scale; scaleY = scale }
                .combinedClickable(
                    interactionSource = interaction,
                    indication = androidx.compose.material.ripple.rememberRipple(),
                    onClick = { onClick?.invoke() },
                    onLongClick = onLongClick
                )
        ) { content() }
    }
}

/** Cards appear gently, staggered by their index — subtle, never bouncy. */
@Composable
fun StaggeredAppear(index: Int = 0, content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(min(index, 8) * 55L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(240)) + slideInVertically(tween(280)) { it / 14 }
    ) { content() }
}

// ═══════════════════════════════ coins ═══════════════════════════════

/** Drawn coin — gold disc with rim and sparkle. Consistent at any size. */
@Composable
fun CoinIcon(modifier: Modifier = Modifier, size: Dp) {
    Canvas(modifier.size(size)) {
        val r = this.size.minDimension / 2f
        drawCircle(Brush.radialGradient(listOf(CoinGold, CoinGoldDeep)), radius = r, center = center)
        drawCircle(Color(0xFFFFE9B8).copy(alpha = 0.9f), radius = r * 0.72f, center = center)
        drawCircle(CoinGoldDeep.copy(alpha = 0.35f), radius = r * 0.60f, center = center)
        // sparkle
        val s = r * 0.42f
        val c = Offset(center.x - r * 0.05f, center.y - r * 0.05f)
        drawLine(Color.White, Offset(c.x - s, c.y), Offset(c.x + s, c.y), strokeWidth = r * 0.16f)
        drawLine(Color.White, Offset(c.x, c.y - s), Offset(c.x, c.y + s), strokeWidth = r * 0.16f)
    }
}

/** Coin amount that counts smoothly when it changes. */
@Composable
fun AnimatedCoinText(amount: Int, modifier: Modifier = Modifier, style: TextStyle = MaterialTheme.typography.headlineMedium) {
    val displayed by animateIntAsState(
        targetValue = amount,
        animationSpec = tween(durationMillis = 700),
        label = "coins"
    )
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        CoinIcon(size = (style.fontSize.value * 0.95f + 6f).dp)
        Spacer(Modifier.width(8.dp))
        Text("%,d".format(displayed), style = style)
    }
}

// ═══════════════════════════════ trust ═══════════════════════════════

fun trustColor(level: Int): Color = when (level) {
    1 -> TrustL1
    2 -> TrustL2
    3 -> TrustL3
    4 -> TrustL4
    else -> TrustL5
}

/**
 * Trust badge, tiers 1–5. Animates a subtle pulse once whenever the user's
 * trust level increases (when [animate] is true and AppState reports a bump).
 */
@Composable
fun TrustBadge(level: Int, modifier: Modifier = Modifier, animate: Boolean = false) {
    val color = trustColor(level)
    val tier = Trust.tier(level)
    var pulse by remember { mutableStateOf(1f) }
    val scale by animateFloatAsState(pulse, spring(dampingRatio = Spring.DampingRatioMediumBouncy), label = "trustPulse")
    if (animate) {
        LaunchedEffect(AppState.lastTrustUpAt) {
            if (AppState.lastTrustUpAt > 0L) {
                pulse = 1.18f
                delay(320)
                pulse = 1f
            }
        }
    }
    Row(
        modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(R.drawable.ic_shield),
            contentDescription = "Trust level $level",
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            "L$level · ${tier.name}",
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            maxLines = 1,
            softWrap = false
        )
    }
}

/** Progress toward the next trust tier. */
@Composable
fun TrustProgress(successfulExchanges: Int, modifier: Modifier = Modifier) {
    val tiers = Trust.tiers
    val current = Trust.levelFor(successfulExchanges)
    val next = tiers.firstOrNull { it.minExchanges > successfulExchanges }
    val cs = MaterialTheme.colorScheme
    Column(modifier) {
        if (next != null) {
            val prevMin = Trust.tier(current).minExchanges
            val fraction = ((successfulExchanges - prevMin).toFloat() / (next.minExchanges - prevMin)).coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = trustColor(current),
                trackColor = cs.surfaceVariant
            )
        } else {
            LinearProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = TrustL5,
                trackColor = cs.surfaceVariant
            )
        }
    }
}

// ═══════════════════════════════ transaction status ═══════════════════════════════

fun statusColor(status: TxStatus): Color = when (status) {
    TxStatus.PENDING -> Color(0xFFFFB020)
    TxStatus.VERIFIED -> Color(0xFF2ECC71)
    TxStatus.DISPUTED -> Color(0xFFFF5A6E)
    TxStatus.COMPLETED -> Color(0xFF3E8BFF)
    TxStatus.EXPIRED -> Color(0xFF8E99B4)
}

fun statusLabel(status: TxStatus): String = when (status) {
    TxStatus.PENDING -> "Pending"
    TxStatus.VERIFIED -> "Verified"
    TxStatus.DISPUTED -> "Disputed"
    TxStatus.COMPLETED -> "Done"
    TxStatus.EXPIRED -> "Expired"
}

fun statusHint(status: TxStatus): String = when (status) {
    TxStatus.PENDING -> "Waiting for confirmation"
    TxStatus.VERIFIED -> "Verified — Coins released"
    TxStatus.DISPUTED -> "Under review"
    TxStatus.COMPLETED -> "Completed"
    TxStatus.EXPIRED -> "Expired — no action taken"
}

@Composable
fun StatusBadge(status: TxStatus, modifier: Modifier = Modifier) {
    val color = statusColor(status)
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val icon: ImageVector = when (status) {
            TxStatus.PENDING -> Icons.Filled.Done // replaced below by painter
            TxStatus.VERIFIED -> Icons.Filled.CheckCircle
            TxStatus.DISPUTED -> Icons.Filled.Warning
            TxStatus.COMPLETED -> Icons.Filled.Done
            TxStatus.EXPIRED -> Icons.Filled.Info
        }
        if (status == TxStatus.PENDING) {
            Icon(painterResource(R.drawable.ic_clock), null, tint = color, modifier = Modifier.size(13.dp))
        } else {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
        }
        Spacer(Modifier.width(5.dp))
        Text(statusLabel(status), color = color, fontWeight = FontWeight.Bold, fontSize = 10.sp)
    }
}

// ═══════════════════════════════ empty / error / loading ═══════════════════════════════

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Info,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    title: String,
    subtitle: String? = null
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(tint.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        if (subtitle != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

/** Friendly error state with a retry action — raw technical errors are never shown to users. */
@Composable
fun ErrorState(
    modifier: Modifier = Modifier,
    message: String = "Something went wrong",
    subtitle: String = "This isn't your fault. Give it another try.",
    onRetry: () -> Unit
) {
    Column(
        modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.errorContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(32.dp))
        }
        Spacer(Modifier.height(14.dp))
        Text(message, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(4.dp))
        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(18.dp))
        SecondaryButton("Try again", modifier = Modifier.width(180.dp), onClick = onRetry)
    }
}

/** Shimmering placeholder box used to build skeleton screens. */
@Composable
fun ShimmerBox(modifier: Modifier = Modifier, corner: Dp = 12.dp) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Restart),
        label = "shift"
    )
    val cs = MaterialTheme.colorScheme
    val base = cs.surfaceVariant
    val highlight = if (cs.surface.luminanceSafe() > 0.5f) Color.White.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.10f)
    var widthPx by remember { mutableStateOf(1f) }
    Box(
        modifier
            .onGloballyPositioned { widthPx = it.size.width.toFloat() }
            .clip(RoundedCornerShape(corner))
            .background(base)
    ) {
        val band = widthPx * 0.7f
        val x0 = -band + (widthPx + 2 * band) * shift
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color.Transparent, highlight, Color.Transparent),
                        start = Offset(x0, 0f),
                        end = Offset(x0 + band, 0f)
                    )
                )
        )
    }
}

private fun Color.luminanceSafe(): Float {
    val r = red * 255; val g = green * 255; val b = blue * 255
    return (0.299f * r + 0.587f * g + 0.114f * b) / 255f
}

// ═══════════════════════════════ small helpers (v0.0.1 API kept) ═══════════════════════════════

/** Gradient palettes used for avatar placeholders (initials). */
private val palettes = listOf(
    listOf(Color(0xFF25F4EE), Color(0xFF0066FF)),
    listOf(Color(0xFFFE2C55), Color(0xFFFF7A00)),
    listOf(Color(0xFF7B2FF2), Color(0xFF25F4EE)),
    listOf(Color(0xFFFF2E63), Color(0xFFB84FFF)),
    listOf(Color(0xFF00C2A8), Color(0xFF007BFF)),
    listOf(Color(0xFFFFB300), Color(0xFFFE2C55)),
    listOf(Color(0xFF3D6BFF), Color(0xFF00E0C6)),
    listOf(Color(0xFFFF5FA2), Color(0xFF6A3BFF))
)

/** Avatar showing the user's chosen profile picture (or initials fallback). */
@Composable
fun ProfileAvatar(name: String, seed: Int, size: Dp) {
    val path = com.tiktokboost.app.data.Session.profilePicturePath
    val file = remember(path) { path?.let { java.io.File(it) } }
    if (file != null && file.exists()) {
        val bmp = remember(path) {
            android.graphics.BitmapFactory.decodeFile(file.absolutePath)
        }
        if (bmp != null) {
            androidx.compose.foundation.Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = "Profile picture",
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                modifier = Modifier.size(size).clip(CircleShape)
            )
            return
        }
    }
    GradientAvatar(name, seed, size)
}

/** 🔥 92% Match — TickTokBoost recommendation score (not a prediction). */
@Composable
fun MatchChip(percent: Int, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(horizontal = 8.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🔥", fontSize = 10.sp)
        Spacer(Modifier.width(3.dp))
        Text(
            "$percent% Match",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            maxLines = 1, softWrap = false
        )
    }
}

@Composable
fun GradientAvatar(name: String, seed: Int, size: Dp) {
    val colors = palettes[Math.floorMod(seed, palettes.size)]
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(colors)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials(name),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = (size.value * 0.32f).sp
        )
    }
}

fun initials(name: String): String {
    val parts = name.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase(Locale.ROOT)
        else -> (parts[0].take(1) + parts[1].take(1)).uppercase(Locale.ROOT)
    }
}

fun formatCount(n: Int): String = when {
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000f)
    n >= 1_000 -> "%.1fk".format(n / 1_000f)
    else -> n.toString()
}

fun relativeTime(ts: Long): String {
    val minutes = (System.currentTimeMillis() - ts) / 60_000L
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "${minutes}m ago"
        minutes < 1440 -> "${minutes / 60}h ago"
        else -> "${minutes / 1440}d ago"
    }
}

fun openTikTok(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: Exception) {
        // No browser available
    }
}

@Composable
fun CoinPill(coins: Int, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CoinIcon(size = 15.dp)
        Spacer(Modifier.width(6.dp))
        Text("$coins", color = CoinGoldDeep, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    val cs = MaterialTheme.colorScheme
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(cs.surfaceVariant)
            .padding(vertical = 14.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, color = cs.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Spacer(Modifier.height(2.dp))
        Text(label, color = cs.onSurfaceVariant, fontSize = 12.sp)
    }
}

@Composable
fun Chip(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
fun SectionTitle(text: String, actionText: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            modifier = Modifier.weight(1f)
        )
        if (actionText != null && onAction != null) {
            Text(
                actionText,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val cs = MaterialTheme.colorScheme
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (onBack != null) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = cs.background,
            titleContentColor = cs.onSurface
        )
    )
}

// ═══════════════════════════════ v0.0.3 brand components ═══════════════════════════════

/**
 * 💎 Premium / 👑 Pro badge — elegant, subtle, gradient-anchored.
 * Shown next to usernames, in the nav area and on the Premium page.
 */
@Composable
fun PremiumBadge(tier: PremiumTier, modifier: Modifier = Modifier, compact: Boolean = false) {
    if (tier == PremiumTier.FREE) return
    val label = if (tier == PremiumTier.PRO) "Pro" else "Premium"
    val emoji = if (tier == PremiumTier.PRO) "👑" else "💎"
    val brush = Brush.horizontalGradient(listOf(SigGradientStart, SigGradientEnd))
    // FIXED height + single-line, no-wrap texts: this badge can NEVER collapse
    // into a vertical strip, no matter how tight the parent layout gets.
    Row(
        modifier
            .height(if (compact) 20.dp else 24.dp)
            .clip(RoundedCornerShape(50))
            .background(brush)
            .padding(horizontal = if (compact) 7.dp else 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            emoji,
            fontSize = if (compact) 10.sp else 12.sp,
            maxLines = 1,
            softWrap = false
        )
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = if (compact) 10.sp else 11.sp,
            maxLines = 1,
            softWrap = false
        )
    }
}

/** 🔥 streak chip — small, celebratory, never gaudy. */
@Composable
fun StreakChip(days: Int, modifier: Modifier = Modifier) {
    if (days <= 0) return
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🔥", fontSize = 12.sp)
        Spacer(Modifier.width(4.dp))
        Text("$days-Day Streak", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

/** Boost status chip with remaining time. */
@Composable
fun BoostChip(tier: BoostTier, msRemaining: Long, modifier: Modifier = Modifier) {
    Row(
        modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🚀", fontSize = 12.sp)
        Spacer(Modifier.width(4.dp))
        val hours = msRemaining / 3_600_000f
        Text(
            "${tier.label} · ${"%.1f".format(hours)}h left",
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/** Profile completion meter — "Profile 80% Complete". */
@Composable
fun ProfileCompletionBar(percent: Int, modifier: Modifier = Modifier) {
    Column(modifier) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Profile $percent% complete",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            if (percent < 100) {
                Text(
                    "Complete profile",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { percent / 100f },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

/** Signature gradient surface — used sparingly (premium hero, boost cards). */
@Composable
fun SignatureGradient(content: @Composable () -> Unit) {
    Box(Modifier.background(Brush.linearGradient(listOf(SigGradientStart, SigGradientEnd)))) { content() }
}
