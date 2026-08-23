package com.tiktokboost.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.MockData
import com.tiktokboost.app.data.Session
import com.tiktokboost.app.ui.AppState
import com.tiktokboost.app.ui.components.AppTopBar
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.LogoMark

@Composable
fun LoginScreen(onDone: () -> Unit, onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar("Log in", onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Spacer(Modifier.height(20.dp))
            LogoMark(size = 44.dp)
            Spacer(Modifier.height(14.dp))
            Text(
                "Welcome back 👋",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Log in to continue growing.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(22.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(22.dp))
            BrandButton("Log in") {
                Session.isLoggedIn = true
                AppState.refresh()
                onDone()
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "Demo mode: any email and password work.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun SignupScreen(onDone: () -> Unit, onBack: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var tiktok by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { AppTopBar("Create account", onBack) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Spacer(Modifier.height(12.dp))
            LogoMark(size = 44.dp)
            Spacer(Modifier.height(14.dp))
            Text(
                "Join TickTokBoost 🚀",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Add your TikTok handle so other creators can find you.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(18.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Display name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
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
            error?.let {
                Spacer(Modifier.height(10.dp))
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
            Spacer(Modifier.height(18.dp))
            BrandButton("Create Account") {
                val handle = tiktok.trim().removePrefix("@")
                if (name.isBlank() || email.isBlank() || password.isBlank() || handle.isBlank()) {
                    error = "Please fill in all fields."
                } else {
                    Session.displayName = name.trim()
                    Session.email = email.trim()
                    Session.tiktokUsername = handle
                    Session.isLoggedIn = true
                    AppState.starterBonus(handle)
                    Session.addNotification(
                        "coins", "Starter coins: ${com.tiktokboost.app.data.EconomyConfig.STARTER_COINS}.",
                        "Thanks for joining TickTokBoost — here are ${com.tiktokboost.app.data.EconomyConfig.STARTER_COINS} starter coins."
                    )
                    AppState.refresh()
                    onDone()
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "You'll start with ${com.tiktokboost.app.data.EconomyConfig.STARTER_COINS} starter coins 🎉",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}
