package com.tiktokboost.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.EconomyConfig
import com.tiktokboost.app.data.EconomyResult
import com.tiktokboost.app.data.QuestDef
import com.tiktokboost.app.data.QuestStatus
import com.tiktokboost.app.data.QuestType
import com.tiktokboost.app.data.Quests
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.AnimatedCoinText
import com.tiktokboost.app.ui.components.AppTopBar
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.CoinIcon
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.PremiumBadge
import com.tiktokboost.app.ui.components.SecondaryButton
import com.tiktokboost.app.ui.components.StreakChip
import com.tiktokboost.app.ui.theme.GoodGreen

/**
 * Earn tab — quest engine UI. Quests show real progress; taps open the
 * relevant action (check-in sheet, Discover, profile, referral, native
 * share) and rewards can only be CLAIMED from READY_TO_CLAIM.
 */
@Composable
fun EarnScreen(
    onBack: (() -> Unit)? = null,
    showTopBar: Boolean = true,
    onOpenDiscover: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onOpenReferral: () -> Unit = {}
) {
    val context = LocalContext.current
    var msg by remember { mutableStateOf<String?>(null) }
    var checkInOpen by remember { mutableStateOf(false) }
    val cs = MaterialTheme.colorScheme

    LaunchedEffect(msg) { if (msg != null) { kotlinx.coroutines.delay(3200); msg = null } }

    fun resultText(res: EconomyResult): String = when (res) {
        is EconomyResult.Success -> res.message ?: "+${res.coins} coins"
        is EconomyResult.Failure -> AppState.detailFor(res.reason)
    }

    Scaffold(
        containerColor = cs.background,
        topBar = { if (showTopBar) AppTopBar("Earn Coins", onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenH)
        ) {
            Spacer(Modifier.height(if (showTopBar) 8.dp else 16.dp))

            // ── balance + daily limit ───────────────────────────────────
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Available coins", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
                            Spacer(Modifier.height(2.dp))
                            AnimatedCoinText(
                                amount = AppState.coins,
                                style = MaterialTheme.typography.headlineMedium.copy(color = cs.onSurface)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            StreakChip(days = AppState.streakDays)
                            Spacer(Modifier.height(4.dp))
                            PremiumBadge(tier = AppState.premium, compact = true)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Today's progress: ${AppState.dailyEarnedCoins} / ${AppState.dailyCap} coins earned",
                        style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { AppState.dailyEarnedCoins / AppState.dailyCap.toFloat() },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = cs.primary, trackColor = cs.surfaceVariant
                    )
                    if (AppState.dailyEarnedCoins >= AppState.dailyCap) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "You've reached today's earning limit. Come back tomorrow.",
                            style = MaterialTheme.typography.bodySmall, color = cs.secondary, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            AnimatedVisibility(visible = msg != null, enter = fadeIn(tween(220)) + slideInVertically(tween(260)) { it / 6 }) {
                Row(
                    Modifier.padding(top = 10.dp, bottom = 2.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CoinIcon(size = 16.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(msg ?: "", color = GoodGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("Quests", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
            Spacer(Modifier.height(8.dp))

            Quests.all().forEach { (def, state) ->
                QuestCard(def = def, state = state, cs = cs,
                    onAction = {
                        when (def.type) {
                            QuestType.DAILY_CHECKIN -> checkInOpen = true
                            QuestType.FOLLOW_CREATORS -> onOpenDiscover()
                            QuestType.COMPLETE_PROFILE -> onOpenProfile()
                            QuestType.INVITE_FRIEND -> onOpenReferral()
                            QuestType.SHARE_APP -> openShareSheet(context)
                        }
                    },
                    onClaim = { msg = resultText(Quests.claim(def)) }
                )
                Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(6.dp))
            BrandCard {
                Text("💡 How earning works", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Confirmed exchanges pay ${EconomyConfig.COINS_PER_CONFIRMED_EXCHANGE} coins each. Daily cap: " +
                        "${EconomyConfig.DAILY_EARNING_CAP}. Cooldown: ${EconomyConfig.EXCHANGE_COOLDOWN_MINUTES} min " +
                        "(shorter as trust grows). Quest rewards require real qualifying actions — opening a quest never pays.",
                    style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── daily check-in interaction ────────────────────────────────────
    if (checkInOpen) {
        AlertDialog(
            onDismissRequest = { checkInOpen = false },
            title = { Text("Daily Check-in") },
            text = {
                Column {
                    Text("Claim your daily bonus and continue your streak.")
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StreakChip(days = AppState.streakDays)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "+${EconomyConfig.DAILY_CHECKIN_REWARD} coin" +
                                if (AppState.streakDays + 1 >= EconomyConfig.STREAK_BONUS_START_DAY)
                                    " + ${EconomyConfig.STREAK_BONUS} streak bonus" else "",
                            style = MaterialTheme.typography.labelMedium,
                            color = cs.primary, fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val def = Quests.byId("q_checkin")!!
                    val res = Quests.claim(def)
                    msg = resultText(res)
                    checkInOpen = false
                }) { Text("Claim") }
            },
            dismissButton = {
                TextButton(onClick = { checkInOpen = false }) { Text("Later") }
            }
        )
    }
}

fun openShareSheet(context: Context) {
    val text = "Join me on TickTokBoost — discover creators, build your reputation, and grow your visibility. " +
        com.tiktokboost.app.data.Session.referralLink()
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
        putExtra(Intent.EXTRA_TITLE, "TickTokBoost")
    }
    try {
        context.startActivity(Intent.createChooser(intent, "Share TickTokBoost"))
        Quests.onAppShared()
    } catch (e: Exception) {
        // no share targets available
    }
}

@Composable
private fun QuestCard(
    def: QuestDef,
    state: com.tiktokboost.app.data.QuestState,
    cs: androidx.compose.material3.ColorScheme,
    onAction: () -> Unit,
    onClaim: () -> Unit
) {
    val emoji = when (def.type) {
        QuestType.DAILY_CHECKIN -> "📅"
        QuestType.FOLLOW_CREATORS -> "🤝"
        QuestType.COMPLETE_PROFILE -> "✨"
        QuestType.INVITE_FRIEND -> "🎁"
        QuestType.SHARE_APP -> "📤"
    }
    BrandCard {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 22.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(def.title, style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                    Text(def.description, style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                }
                when (state.status) {
                    QuestStatus.COMPLETED -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("✓", color = cs.primary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Completed", style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant)
                        }
                    }
                    QuestStatus.READY_TO_CLAIM -> {
                        BrandButton("Claim +${def.reward}", onClick = onClaim)
                    }
                    else -> {
                        SecondaryButton(def.action, onClick = onAction)
                    }
                }
            }
            if (state.status == QuestStatus.IN_PROGRESS) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "${state.progress} / ${def.requirement}" + if (def.type == QuestType.COMPLETE_PROFILE) "% complete" else "",
                    style = MaterialTheme.typography.labelSmall,
                    color = cs.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (state.progress.toFloat() / def.requirement).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                    color = cs.primary, trackColor = cs.surfaceVariant
                )
            }
        }
    }
}
