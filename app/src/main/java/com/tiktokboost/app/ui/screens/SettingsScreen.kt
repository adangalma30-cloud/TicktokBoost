package com.tiktokboost.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
    val context = LocalContext.current
    var showDelete by remember { mutableStateOf(false) }
    var showLanguage by remember { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }

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

            // ── Appearance ─────────────────────────────────────────────
            Section("Appearance")
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    listOf(
                        "system" to "System default",
                        "light" to "Light mode",
                        "dark" to "Dark mode"
                    ).forEach { (mode, label) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .selectable(selected = AppState.themeMode == mode, onClick = { AppState.setTheme(mode) })
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = AppState.themeMode == mode, onClick = { AppState.setTheme(mode) })
                            Spacer(Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge, color = cs.onSurface)
                        }
                    }
                    Text(
                        "Applies instantly and stays after restarting the app.",
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Notifications & feedback ───────────────────────────────
            Section("Notifications & feedback")
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    ToggleRow(
                        "Notifications",
                        "Exchange updates, coins, trust and quest events",
                        AppState.notificationsEnabled
                    ) { AppState.setNotifications(it) }
                    HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.3f))
                    ToggleRow(
                        "Vibration & haptics",
                        "Subtle feedback on claims and completions",
                        AppState.hapticsEnabled
                    ) { AppState.setHaptics(it) }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Language ───────────────────────────────────────────────
            Section("Language")
            BrandCard(onClick = { showLanguage = true }) {
                Row(Modifier.padding(Dimens.card), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("App language", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                        Text(
                            when (AppState.language) {
                                "en" -> "English"
                                "sw" -> "Kiswahili"
                                else -> "System default"
                            },
                            style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                        )
                    }
                    Text("→", color = cs.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Privacy ────────────────────────────────────────────────
            Section("Privacy")
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    ToggleRow(
                        "Appear in discovery",
                        "Other creators can find and match with you",
                        AppState.discoverable
                    ) { AppState.updateDiscoverable(it) }
                    HorizontalDivider(color = cs.outlineVariant.copy(alpha = 0.3f))
                    Row(
                        Modifier.fillMaxWidth().padding(top = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Blocked users", style = MaterialTheme.typography.bodyLarge, color = cs.onSurface, modifier = Modifier.weight(1f))
                        TextButton(onClick = onBlocked) { Text("Manage", color = cs.primary, fontWeight = FontWeight.Bold) }
                    }
                    Text(
                        "Blocked creators can't exchange with you and are hidden from your discovery.",
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Account ────────────────────────────────────────────────
            Section("Account")
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
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                        Text("Log out", color = cs.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { showDelete = true }, modifier = Modifier.fillMaxWidth()) {
                        Text("Delete account", color = cs.error, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── Help & support ─────────────────────────────────────────
            Section("Help & support")
            BrandCard(onClick = { showHelp = true }) {
                Row(Modifier.padding(Dimens.card), verticalAlignment = Alignment.CenterVertically) {
                    Text("❓", fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Help & Support", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                        Text("FAQ and contact", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                    Text("→", color = cs.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── About ──────────────────────────────────────────────────
            Section("About")
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
                        Text(
                            "Version ${BuildConfig.VERSION_NAME} · manual exchanges only — TickTokBoost never automates TikTok actions.",
                            style = MaterialTheme.typography.labelSmall, color = cs.onSurfaceVariant
                        )
                    }
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
                        Text("Economy analytics, disputes, reports & review queue (demo)", style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant)
                    }
                    Text("→", color = cs.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── demo data reset (kept from before) ─────────────────────
            BrandCard {
                Column(Modifier.padding(Dimens.card)) {
                    Text("Demo data", style = MaterialTheme.typography.titleSmall, color = cs.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Resetting clears everything (including your content uploads) and returns to onboarding.",
                        style = MaterialTheme.typography.bodySmall, color = cs.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    SecondaryButton(
                        "Reset demo data",
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { Session.resetAll(); AppState.refresh(); onSignOut() }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // ── language picker ─────────────────────────────────────────────
    if (showLanguage) {
        AlertDialog(
            onDismissRequest = { showLanguage = false },
            title = { Text("App language") },
            text = {
                Column {
                    listOf(
                        "system" to "System default",
                        "en" to "English",
                        "sw" to "Kiswahili"
                    ).forEach { (code, label) ->
                        Row(
                            Modifier.fillMaxWidth().selectable(
                                selected = AppState.language == code,
                                onClick = { AppState.updateLanguage(code); showLanguage = false }
                            ).padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = AppState.language == code, onClick = { AppState.updateLanguage(code); showLanguage = false })
                            Spacer(Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Text(
                        "Full translations arrive in a later release — the app currently ships English.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = { TextButton(onClick = { showLanguage = false }) { Text("Close") } }
        )
    }

    // ── delete account confirmation ─────────────────────────────────
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("Delete account?") },
            text = {
                Text("This permanently erases your profile, coins, trust, content uploads and history on this device. This cannot be undone.")
            },
            confirmButton = {
                TextButton(onClick = {
                    Session.resetAll()
                    AppState.refresh()
                    showDelete = false
                    onSignOut()
                }) { Text("Delete forever", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("Cancel") } }
        )
    }

    // ── help & support ──────────────────────────────────────────────
    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text("Help & Support") },
            text = {
                Column {
                    HelpItem("How do exchanges work?",
                        "Open a creator's TikTok profile, follow them yourself in TikTok, come back and tap Complete. When they confirm, your coins are released.")
                    HelpItem("Why are my coins pending?",
                        "Coins are held until the other creator confirms the exchange — this keeps rewards fair.")
                    HelpItem("What is the match score?",
                        "A TickTokBoost recommendation based on shared category, activity and trust. It's a suggestion, not a prediction.")
                    HelpItem("How do I get more visibility?",
                        "Spend coins on Boosts (Discover tab → Boost) or upgrade to Premium for priority discovery and analytics.")
                    Spacer(Modifier.height(10.dp))
                    TextButton(onClick = {
                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@ticktokboost.app"))
                                    .putExtra(Intent.EXTRA_SUBJECT, "TickTokBoost support")
                            )
                        } catch (e: Exception) { /* no mail app */ }
                    }) { Text("📧 Contact support", color = cs.primary, fontWeight = FontWeight.Bold) }
                }
            },
            confirmButton = { TextButton(onClick = { showHelp = false }) { Text("Close") } }
        )
    }
}

@Composable
private fun Section(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun ToggleRow(title: String, subtitle: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChecked,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = androidx.compose.ui.graphics.Color.White
            )
        )
    }
}

@Composable
private fun HelpItem(q: String, a: String) {
    var open by remember { mutableStateOf(false) }
    Column(Modifier.padding(vertical = 4.dp)) {
        Text(
            q,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.fillMaxWidth().selectable(selected = open, onClick = { open = !open }).padding(vertical = 4.dp)
        )
        if (open) {
            Text(a, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
