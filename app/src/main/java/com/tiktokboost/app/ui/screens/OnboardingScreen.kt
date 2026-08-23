package com.tiktokboost.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.data.MockData
import com.tiktokboost.app.ui.components.BrandButton
import com.tiktokboost.app.ui.components.LogoMark
import com.tiktokboost.app.ui.components.SecondaryButton
import com.tiktokboost.app.ui.theme.SigGradientEnd
import com.tiktokboost.app.ui.theme.SigGradientStart

/**
 * Four-step onboarding: discover → exchanges → trust → credits,
 * ending with an invitation to complete your profile.
 */
@Composable
fun OnboardingScreen(onGetStarted: () -> Unit, onLogin: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    val steps = MockData.onboardingSteps
    val cs = MaterialTheme.colorScheme

    Scaffold(containerColor = cs.background) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Row(
                Modifier.fillMaxWidth().padding(top = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LogoMark(size = 26.dp)
                Spacer(Modifier.width(8.dp))
                Text("TickTokBoost", style = MaterialTheme.typography.titleMedium, color = cs.onBackground)
                Spacer(Modifier.weight(1f))
                if (step < steps.lastIndex) {
                    TextButton(onClick = { step = steps.lastIndex }) { Text("Skip") }
                }
            }

            Spacer(Modifier.weight(1f))

            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    (fadeIn(tween(300)) + slideInVertically(tween(340)) { it / 10 }) togetherWith fadeOut(tween(200))
                },
                label = "onboarding"
            ) { i ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier
                            .size(148.dp)
                            .clip(CircleShape)
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(SigGradientStart.copy(alpha = 0.16f), SigGradientEnd.copy(alpha = 0.16f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(steps[i].first, fontSize = 62.sp)
                    }
                    Spacer(Modifier.height(28.dp))
                    // step indicator
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        steps.indices.forEach { idx ->
                            Box(
                                Modifier
                                    .size(width = if (idx == i) 22.dp else 8.dp, height = 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (idx <= i) cs.primary else cs.surfaceVariant
                                    )
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "${i + 1}. ${steps[i].second}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = cs.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        steps[i].third,
                        style = MaterialTheme.typography.bodyLarge,
                        color = cs.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            if (step < steps.lastIndex) {
                BrandButton("Next") { step += 1 }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
                    Text("I already have an account", color = cs.primary, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    "Complete your profile after signing up —\ncomplete profiles rank higher in discovery.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(14.dp))
                BrandButton("Get Started") { onGetStarted() }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
                    Text("I already have an account", color = cs.primary, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(22.dp))
        }
    }
}
