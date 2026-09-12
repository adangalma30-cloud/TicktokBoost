package com.tiktokboost.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.BuildConfig
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.AppTopBar
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.LogoMark
import com.tiktokboost.app.ui.components.PremiumBadge
import com.tiktokboost.app.ui.components.SecondaryButton

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onSignOut: () -> Unit,
    onAdmin: () -> Unit,
    onPremium: () -> Unit,
    onBlocked: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    Scaffold(
        containerColor = cs.background,
        topBar = { AppTopBar("Settings", onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.screenH)
        ) {
            Spacer(Modifier.height(8.dp))

            BrandCard {
                Row(Modifier.padding(Dimens.card), verticalAlignment = Alignment.CenterVertically) {
                    LogoMark(size = 38.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("TickTokBoost", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                        Text(
                            "Boost your presence. Grow your audience.",
                            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                        )
                    }
                    Text("v${BuildConfig.VERSION_NAME}", color = cs.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── subscription ───────────────────────────────────────────
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PremiumBadge(tier = AppState.premium)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            if (AppState.premium == com.tiktokboost.app.data.PremiumTier.FREE) "Free plan"
                            else "${AppState.premium.label} active",
                            style = MaterialTheme.typography.titleSmall, color = cs.onSurface
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    if (AppState.premium == com.tiktokboost.app.data.PremiumTier.FREE) {
                        SecondaryButton("See Premium plans 💎", modifier = Modifier.fillMaxWidth(), onClick = onPremium)
                    } else {
                        SecondaryButton("Manage subscription", modifier = Modifier.fillMaxWidth(), onClick = onPremium)
                        Spacer(Modifier.height(8.dp))
                        TextButton(
                            onClick = {
                                AppState.cancelPremium()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancel subscription (demo)", color = cs.onSurfaceVariant, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    Text("About", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "TickTokBoost is a follow-for-follow social discovery app. " +
                            "You discover real creators who want followers, follow them " +
                            "yourself in the TikTok app, and they follow you back. " +
                            "TickTokBoost never logs into your TikTok account and never " +
                            "performs follows automatically. Boosts and Premium only affect " +
                            "visibility inside TickTokBoost — never guaranteed external followers.",
                        style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    Text("Demo data", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "v0.0.3 runs on mock users with a mock payment flow. Counterpart " +
                            "confirmations happen automatically seconds after you complete an " +
                            "exchange. Resetting clears everything and returns to onboarding.",
                        style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    SecondaryButton(
                        text = "Reset demo data",
                        onClick = {
                            Session.resetAll()
                            AppState.refresh()
                            onSignOut()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── blocked users ───────────────────────────────────────────
            BrandCard(onClick = onBlocked) {
                Row(Modifier.padding(Dimens.card), verticalAlignment = Alignment.CenterVertically) {
                    Text("🚫", fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Blocked users", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                        Text("Manage creators you've blocked", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                    Text("→", color = cs.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── admin (demo) ───────────────────────────────────────────
            BrandCard(onClick = onAdmin) {
                Row(Modifier.padding(Dimens.card), verticalAlignment = Alignment.CenterVertically) {
                    Text("🛠️", fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Admin dashboard", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                        Text("Economy analytics, disputes & review queue (demo)", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                    Text("→", color = cs.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text("Sign out", color = cs.onSurfaceVariant, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
