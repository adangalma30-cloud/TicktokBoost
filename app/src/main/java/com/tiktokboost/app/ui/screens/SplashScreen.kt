package com.tiktokboost.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.ui.components.LogoMark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

/**
 * Premium startup animation (~2s, tap to skip):
 *  1. clean dark stage with a subtle particle field
 *  2. a soft glow breathes behind the mark
 *  3. the TickTokBoost mark springs in; wordmark + tagline fade up
 *  4. hands over smoothly to the app
 */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    val stage = Color(0xFF0A0A12)

    // ---- particle field -------------------------------------------------------
    data class Particle(val x: Float, val y: Float, val r: Float, val speed: Float, val phase: Float, val tint: Color)
    val particles = remember {
        val rnd = Random(7)
        val tints = listOf(Color(0xFF25F4EE), Color(0xFFFE2C55), Color.White, Color(0xFF0AA8B5))
        List(26) {
            Particle(
                x = rnd.nextFloat(),
                y = rnd.nextFloat(),
                r = 1.2f + rnd.nextFloat() * 2.6f,
                speed = 0.05f + rnd.nextFloat() * 0.12f,
                phase = rnd.nextFloat() * (2f * Math.PI.toFloat()),
                tint = tints[rnd.nextInt(tints.size)]
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "splash")
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "t"
    )
    val glow = 0.5f + 0.5f * sin(t * 2f * Math.PI.toFloat())

    // ---- logo reveal ----------------------------------------------------------
    val logoScale = remember { Animatable(0.4f) }
    val logoAlpha = remember { Animatable(0f) }
    val wordVisible = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val fade = launch { logoAlpha.animateTo(1f, tween(320)) }
        logoScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = 170f))
        fade.join()
        delay(120)
        wordVisible.value = true
        delay(1500)
        onDone()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(stage)
            .pointerInput(Unit) { detectTapGestures { onDone() } }   // premium = skippable
    ) {
        // particles drifting slowly upward
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            particles.forEach { p ->
                val y = (p.y + t * p.speed) % 1f
                val alpha = 0.10f + 0.16f * (0.5f + 0.5f * sin(p.phase + t * 2.2f))
                drawCircle(
                    color = p.tint.copy(alpha = alpha),
                    radius = p.r.dp.toPx(),
                    center = Offset(p.x * w, (1f - y) * h)
                )
            }
        }

        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(Modifier.size(210.dp), contentAlignment = Alignment.Center) {
                // breathing glow behind the mark
                Box(
                    Modifier
                        .size(190.dp)
                        .scale(0.85f + 0.15f * glow)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFF25F4EE).copy(alpha = 0.06f + 0.20f * glow),
                                    Color(0xFFFE2C55).copy(alpha = 0.05f + 0.16f * glow),
                                    Color.Transparent
                                )
                            )
                        )
                )
                // the mark on a soft tile so it reads on the dark stage
                Box(
                    Modifier
                        .size(116.dp)
                        .scale(logoScale.value)
                        .graphicsLayer { alpha = logoAlpha.value }
                        .background(Color(0xFF12121D)),
                    contentAlignment = Alignment.Center
                ) {
                    LogoMark(size = 104.dp)
                }
            }
            AnimatedVisibility(visible = wordVisible.value, enter = fadeIn(tween(420))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "TickTokBoost",
                        color = Color(0xFFF2F4FA),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        "Boost your presence. Grow your audience.",
                        color = Color(0xFF9AA3B8),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
