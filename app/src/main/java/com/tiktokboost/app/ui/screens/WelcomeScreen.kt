package com.tiktokboost.app.ui.screens

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
import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.BrandCard
import com.tiktokboost.app.ui.components.Dimens
import com.tiktokboost.app.ui.components.LogoMark
import com.tiktokboost.app.ui.components.StaggeredAppear

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit, onLogin: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Scaffold(containerColor = cs.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(52.dp))
            StaggeredAppear(0) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    androidx.compose.foundation.layout.Box(
                        Modifier
                            .size(124.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(cs.surface),
                        contentAlignment = Alignment.Center
                    ) {
                        LogoMark(size = 96.dp)
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "TickTokBoost",
                        style = MaterialTheme.typography.displaySmall,
                        color = cs.onBackground
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Boost your presence. Grow your audience.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = cs.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(28.dp))
            StaggeredAppear(1) { FeatureRow("👀", "Discover", "Browse real creators who want more followers.") }
            StaggeredAppear(2) { FeatureRow("🤝", "Follow on TikTok", "Follow them in the TikTok app — no bots, ever.") }
            StaggeredAppear(3) { FeatureRow("🪙", "Earn coins", "Get rewarded for participating and get featured.") }
            Spacer(Modifier.height(32.dp))
            StaggeredAppear(4) {
                Column {
                    BrandButton(text = "Get Started", onClick = onGetStarted)
                    TextButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
                        Text("I already have an account", color = cs.secondary, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeatureRow(icon: String, title: String, desc: String) {
    BrandCard(Modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(Dimens.card),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
