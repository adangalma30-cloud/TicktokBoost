package com.tiktokboost.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.EconomyConfig
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.AppTopBar
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.ProfileAvatar
import kotlinx.coroutines.delay
import java.io.File

/**
 * Dedicated Edit Profile screen — opened only when the user chooses to edit.
 * Local form state only; NOTHING is persisted until "Save Changes" is pressed.
 * Cancel/Back simply discards.
 */
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val cs = MaterialTheme.colorScheme

    // form state seeded from the current profile (copies — cancel discards)
    var name by remember { mutableStateOf(Session.displayName) }
    var tiktok by remember { mutableStateOf(Session.tiktokUsername) }
    var bio by remember { mutableStateOf(Session.bio) }
    var category by remember { mutableStateOf(Session.category.ifBlank { EconomyConfig.categories.first() }) }
    var error by remember { mutableStateOf<String?>(null) }
    var saved by remember { mutableStateOf(false) }

    val avatarPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        try {
            val out = File(context.filesDir, "profile.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                out.outputStream().use { input.copyTo(it) }
            }
            Session.profilePicturePath = out.absolutePath
            AppState.refresh()
        } catch (e: Exception) { /* keep current avatar */ }
    }

    LaunchedEffect(saved) {
        if (saved) {
            delay(800)   // let the confirmation land, then return to the profile
            onBack()
        }
    }

    Scaffold(
        containerColor = cs.background,
        topBar = { AppTopBar("Edit Profile", onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(20.dp))

            // ── avatar ─────────────────────────────────────────────────
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Box {
                    ProfileAvatar(name.ifBlank { "?" }, 0, 110.dp)
                    Surface(
                        onClick = { avatarPicker.launch("image/*") },
                        shape = CircleShape,
                        color = cs.primary,
                        modifier = Modifier
                            .size(28.dp)
                            .align(Alignment.BottomEnd)
                    ) {
                        Text(
                            "✎",
                            color = Color.Black,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = { avatarPicker.launch("image/*") }) {
                    Text("Change photo", color = cs.primary, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(14.dp))

            // ── fields ─────────────────────────────────────────────────
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = tiktok,
                onValueChange = { tiktok = it },
                label = { Text("TikTok username") },
                placeholder = { Text("@yourhandle") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                supportingText = { Text("${bio.length}/160") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Text("Category", style = MaterialTheme.typography.labelMedium, color = cs.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            EconomyConfig.categories.chunked(2).forEach { row ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                ) {
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

            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = cs.error, fontSize = 13.sp)
            }

            Spacer(Modifier.height(24.dp))

            if (!saved) {
                BrandButton("Save Changes", modifier = Modifier.fillMaxWidth()) {
                    when {
                        name.isBlank() -> error = "Display name can't be empty."
                        tiktok.isBlank() -> error = "TikTok username can't be empty."
                        else -> {
                            Session.displayName = name.trim()
                            Session.tiktokUsername = tiktok.trim().removePrefix("@").ifBlank { "creator" }
                            Session.bio = bio.trim().take(160)
                            Session.category = category
                            AppState.checkAchievements()
                            AppState.refresh()
                            saved = true
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel", color = cs.onSurfaceVariant, fontWeight = FontWeight.Bold)
                }
            } else {
                // subtle confirmation before returning
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = cs.primary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Profile updated successfully",
                        color = cs.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(30.dp))
        }
    }
}
