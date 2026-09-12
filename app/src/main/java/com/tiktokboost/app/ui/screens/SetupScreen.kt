package com.tiktokboost.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.tiktokboost.app.data.EconomyConfig
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.ProfileAvatar
import com.tiktokboost.app.ui.components.SecondaryButton
import java.io.File

/**
 * Post-signup profile setup: display name + picture → TikTok handle →
 * category → bio. Optional steps can be skipped; progress shown at top.
 * Feeds the profile-completion score and discovery matching.
 */
@Composable
fun SetupScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf(Session.displayName) }
    var handle by remember { mutableStateOf(Session.tiktokUsername) }
    var category by remember { mutableStateOf(Session.category) }
    var bio by remember { mutableStateOf(Session.bio) }
    var error by remember { mutableStateOf<String?>(null) }
    val cs = MaterialTheme.colorScheme

    val picturePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        try {
            val out = File(context.filesDir, "profile.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                out.outputStream().use { input.copyTo(it) }
            }
            Session.profilePicturePath = out.absolutePath
            AppState.refresh()
        } catch (e: Exception) {
            // invalid image — keep initials avatar
        }
    }

    fun saveAndFinish() {
        Session.displayName = name.trim().ifBlank { "Creator" }
        Session.tiktokUsername = handle.trim().removePrefix("@").ifBlank { "creator" }
        Session.category = category
        Session.bio = bio.trim().take(160)
        AppState.checkAchievements()
        AppState.refresh()
        onFinish()
    }

    Scaffold(containerColor = cs.background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Spacer(Modifier.height(18.dp))
            Text("Set up your profile", style = MaterialTheme.typography.headlineSmall, color = cs.onSurface)
            Spacer(Modifier.height(6.dp))
            Text(
                "Complete profiles rank higher in discovery and get better matches.",
                style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
            )
            Spacer(Modifier.height(14.dp))
            // progress
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(4) { i ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(if (i <= step) cs.primary else cs.surfaceVariant)
                    )
                }
            }
            Spacer(Modifier.height(22.dp))

            when (step) {
                0 -> {
                    Text("1 · Who are you?", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                    Spacer(Modifier.height(12.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        ProfileAvatar(name.ifBlank { "?" }, 0, 96.dp)
                        Spacer(Modifier.height(8.dp))
                        TextButton(onClick = { picturePicker.launch("image/*") }) {
                            Text("Choose profile picture (optional)", color = cs.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = name, onValueChange = { name = it },
                        label = { Text("Display name") }, singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                1 -> {
                    Text("2 · Your TikTok profile", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Other creators use this to find and verify you on TikTok. TickTokBoost never logs into your account.",
                        style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = handle, onValueChange = { handle = it },
                        label = { Text("TikTok username") }, placeholder = { Text("@yourhandle") },
                        singleLine = true, modifier = Modifier.fillMaxWidth()
                    )
                }
                2 -> {
                    Text("3 · Your creator category", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                    Spacer(Modifier.height(8.dp))
                    Text("Used to match you with creators in the same niche.", style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    EconomyConfig.categories.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                            row.forEach { c ->
                                FilterChip(
                                    selected = category == c,
                                    onClick = { category = c },
                                    label = { Text(c) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
                else -> {
                    Text("4 · Say hello (optional)", style = MaterialTheme.typography.titleMedium, color = cs.onSurface)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = bio, onValueChange = { bio = it },
                        label = { Text("Bio") },
                        supportingText = { Text("${bio.length}/160 · skip if you like") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = cs.error, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))
            if (step < 3) {
                BrandButton("Continue", modifier = Modifier.fillMaxWidth()) {
                    error = when {
                        step == 0 && name.isBlank() -> "Please add a display name."
                        step == 1 && handle.isBlank() -> "Please add your TikTok username."
                        step == 2 && category.isBlank() -> "Please pick a category."
                        else -> null
                    }
                    if (error == null) step += 1
                }
                val skippable = step == 0 && name.isNotBlank() || step == 3
                if (skippable) {
                    Spacer(Modifier.height(4.dp))
                    TextButton(onClick = { step += 1 }, modifier = Modifier.fillMaxWidth()) {
                        Text("Skip for now", color = cs.onSurfaceVariant)
                    }
                }
            } else {
                BrandButton("Finish setup", modifier = Modifier.fillMaxWidth()) { saveAndFinish() }
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}


