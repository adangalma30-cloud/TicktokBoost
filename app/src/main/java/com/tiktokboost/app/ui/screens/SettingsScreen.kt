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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
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
import com.tiktokboost.app.ui.theme.CardBg
import com.tiktokboost.app.ui.theme.TextPrimary
import com.tiktokboost.app.ui.theme.TextSecondary
import com.tiktokboost.app.ui.theme.TikTokBg
import com.tiktokboost.app.ui.theme.TikTokCyan

@Composable
fun SettingsScreen(onBack: () -> Unit, onSignOut: () -> Unit) {
    Scaffold(
        containerColor = TikTokBg,
        topBar = { AppTopBar("Settings", onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Row(
                    Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("\uD83D\uDE80", fontSize = 26.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("TikTokBoost", color = TextPrimary, fontWeight = FontWeight.Bold)
                        Text("Version ${BuildConfig.VERSION_NAME}", color = TextSecondary, fontSize = 13.sp)
                    }
                    Text("v${BuildConfig.VERSION_NAME}", color = TikTokCyan, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("About", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "TikTokBoost is a follow-for-follow social discovery app. " +
                            "You discover real creators who want followers, follow them " +
                            "yourself in the TikTok app, and they follow you back. " +
                            "TikTokBoost never logs into your TikTok account and never " +
                            "performs follows automatically.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Demo data", color = TextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "v0.0.1 runs on mock users. Resetting clears your coins, " +
                            "follows and history and returns to onboarding.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            Session.resetAll()
                            AppState.refresh()
                            onSignOut()
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset demo data")
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = CardBg)
            Spacer(Modifier.height(4.dp))

            TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text("Sign out", color = TextSecondary, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
