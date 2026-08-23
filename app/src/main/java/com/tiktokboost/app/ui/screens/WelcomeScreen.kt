package com.tiktokboost.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.ui.components.GradientButton
import com.tiktokboost.app.ui.theme.AccentPurple
import com.tiktokboost.app.ui.theme.TextPrimary
import com.tiktokboost.app.ui.theme.TextSecondary
import com.tiktokboost.app.ui.theme.TikTokBg
import com.tiktokboost.app.ui.theme.TikTokCyan

@Composable
fun WelcomeScreen(onGetStarted: () -> Unit, onLogin: () -> Unit) {
    Scaffold(containerColor = TikTokBg) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Brush.linearGradient(listOf(TikTokCyan, AccentPurple))),
                contentAlignment = Alignment.Center
            ) {
                Text("\uD83D\uDE80", fontSize = 56.sp)
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "TikTokBoost",
                color = TextPrimary,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Grow your TikTok with real follow-for-follow.",
                color = TextSecondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))
            FeatureRow("\uD83D\uDC40", "Discover", "Browse real creators who want more followers.")
            FeatureRow("\uD83E\uDD1D", "Follow on TikTok", "Follow them in the TikTok app — no bots, ever.")
            FeatureRow("\uD83E\uDE99", "Earn points", "Get rewarded for participating and get featured.")
            Spacer(Modifier.height(40.dp))
            GradientButton(text = "Get Started", onClick = onGetStarted)
            TextButton(onClick = onLogin) {
                Text("I already have an account", color = TikTokCyan, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeatureRow(icon: String, title: String, desc: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 26.sp)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold)
            Text(desc, color = TextSecondary, fontSize = 13.sp)
        }
    }
}
