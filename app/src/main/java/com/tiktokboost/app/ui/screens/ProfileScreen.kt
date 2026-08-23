package com.tiktokboost.app.ui.screens

import android.content.Context
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
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
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.GradientAvatar
import com.tiktokboost.app.ui.components.GradientButton
import com.tiktokboost.app.ui.components.StatBox
import com.tiktokboost.app.ui.components.openTikTok
import com.tiktokboost.app.ui.theme.CardBg
import com.tiktokboost.app.ui.theme.TextPrimary
import com.tiktokboost.app.ui.theme.TextSecondary
import com.tiktokboost.app.ui.theme.TikTokBg
import com.tiktokboost.app.ui.theme.TikTokCyan

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

    Scaffold(containerColor = TikTokBg) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                GradientAvatar(AppState.displayName.ifBlank { "T" }, 0, 72.dp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(
                        AppState.displayName.ifBlank { "Creator" },
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("@${AppState.tiktokUsername}", color = TikTokCyan, fontSize = 15.sp)
                    Text(
                        AppState.email,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // TikTok profile link
            TextButton(onClick = { openTikTok(context, "https://www.tiktok.com/@${AppState.tiktokUsername}") }) {
                Text("Open my TikTok profile ↗", color = TikTokCyan, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(12.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatBox("Followed", "${AppState.followedCount}", Modifier.weight(1f))
                StatBox("Followed back", "${AppState.returnedCount}", Modifier.weight(1f))
                StatBox("Points", "${AppState.coins}", Modifier.weight(1f))
            }

            Spacer(Modifier.height(24.dp))

            // Edit profile
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Edit profile",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
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
                        Text(saved, color = TikTokCyan, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(12.dp))
                    GradientButton("Save changes") {
                        Session.displayName = name.trim().ifBlank { "Creator" }
                        Session.tiktokUsername = tiktok.trim().removePrefix("@").ifBlank { "creator" }
                        AppState.refresh()
                        saved = "Saved \u2713"
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            OutlinedButton(
                onClick = onHistory,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View history")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onSettings,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Settings")
            }

            Spacer(Modifier.height(12.dp))
            TextButton(onClick = onSignOut, modifier = Modifier.fillMaxWidth()) {
                Text("Sign out", color = TextSecondary, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
