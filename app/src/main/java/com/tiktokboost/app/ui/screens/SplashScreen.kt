package com.tiktokboost.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiktokboost.app.ui.components.TbEmblem
import com.tiktokboost.app.ui.theme.AppFont
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * ═════════════════════════════════════════════════════════════════════════════
 *  TickTokBoost startup animation (~1.95s, tap to skip) — assembled from the
 *  official TB emblem's own layers, so the brand never changes shape:
 *
 *   1. near-black stage            (0.00–0.10)
 *   2. subtle cyan/pink glow       (0.06–0.30)
 *   3. motion trails converge      (0.10–0.42)
 *   4. emblem locks together       (0.18–0.62)  ghosts slide in, face appears
 *   5. subtle forward boost        (0.66–0.86)
 *   6. emblem settles              (0.86–1.00)
 *   7. wordmark                    (0.62–0.80)
 *   8. tagline                     (0.76–0.92)
 *   9. hand over to the app        (1.00)
 * ═════════════════════════════════════════════════════════════════════════════
 */
@Composable
fun SplashScreen(onDone: () -> Unit) {
    val stage = Color(0xFF0A0A12)

    var t by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(Unit) {
        // delay-stepped timeline: real time on device, virtual time in tests
        val totalMs = 1950f
        val step = 16L
        var elapsed = 0f
        while (elapsed < totalMs) {
            t = (elapsed / totalMs).coerceIn(0f, 1f)
            delay(step)
            elapsed += step
        }
        t = 1f
        onDone()
    }

    fun seg(a: Float, b: Float): Float = ((t - a) / (b - a)).coerceIn(0f, 1f)
    fun easeOut(f: Float): Float = 1f - (1f - f) * (1f - f)

    val glow = 0.30f * seg(0.06f, 0.30f)
    val wordVisible = seg(0.62f, 0.80f) > 0.5f

    // ── 4. ghosts converge and lock (staggered), face fades in last ─────────
    val cyanGhost = 46f - 43f * easeOut(seg(0.18f, 0.55f))     // 46 → 3
    val pinkGhost = 46f - 43f * easeOut(seg(0.24f, 0.60f))     // 46 → 3
    val cyanAlpha = seg(0.18f, 0.40f)
    val pinkAlpha = seg(0.24f, 0.44f)
    val faceAlpha = seg(0.48f, 0.66f)

    // ── 5/6. subtle forward boost, then settle ───────────────────────────────
    val boost = seg(0.66f, 0.86f)
    val nudgeX = 9f * sin(boost * Math.PI).toFloat()           // out and back
    val emblemScale = 1f + 0.045f * sin(boost * Math.PI).toFloat()

    Box(
        Modifier
            .fillMaxSize()
            .background(stage)
            .pointerInput(Unit) { detectTapGestures { onDone() } }   // premium = skippable
    ) {
        // ── 2. ambient dual glow near the center ────────────────────────────
        if (glow > 0.01f) {
            Canvas(Modifier.fillMaxSize()) {
                val c = center
                drawCircle(
                    Color(0xFF25F4EE).copy(alpha = glow * 0.30f),
                    radius = size.minDimension * 0.42f,
                    center = Offset(c.x - size.width * 0.10f, c.y - size.height * 0.06f)
                )
                drawCircle(
                    Color(0xFFFE2C55).copy(alpha = glow * 0.26f),
                    radius = size.minDimension * 0.42f,
                    center = Offset(c.x + size.width * 0.10f, c.y + size.height * 0.06f)
                )
            }
        }

        // ── 3. cyan/pink motion trails moving toward the center ─────────────
        val trailT = seg(0.10f, 0.42f)
        if (trailT in 0.001f..0.999f) {
            Canvas(Modifier.fillMaxSize()) {
                val c = center
                val e = easeOut(trailT)
                repeat(6) { i ->
                    val dir = i % 2 == 0
                    val color = if (dir) Color(0xFF25F4EE) else Color(0xFFFE2C55)
                    val ang = (-52f + i * 21f) * (if (dir) 1f else -1f)
                    val dist = (1f - e) * size.minDimension * (0.34f + (i % 3) * 0.07f)
                    val rad = Math.toRadians(ang.toDouble())
                    val pos = Offset(
                        (c.x + dist * cos(rad)).toFloat(),
                        (c.y + dist * sin(rad)).toFloat()
                    )
                    rotate(degrees = ang + 90f, pivot = pos) {
                        drawRoundRect(
                            color = color.copy(alpha = 0.55f * (1f - trailT * 0.6f)),
                            topLeft = Offset(pos.x - 2f, pos.y - 26f),
                            size = Size(4f, 52f),
                            cornerRadius = CornerRadius(2f, 2f)
                        )
                    }
                }
            }
        }

        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            TbEmblem(
                size = 128.dp,
                cyanGhostUnits = cyanGhost,
                pinkGhostUnits = pinkGhost,
                cyanAlpha = cyanAlpha,
                pinkAlpha = pinkAlpha,
                faceAlpha = faceAlpha,
                modifier = Modifier
                    .offset(x = nudgeX.dp)
                    .scale(emblemScale)
            )

            AnimatedVisibility(visible = wordVisible, enter = fadeIn(tween(360))) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "TickTokBoost",
                        color = Color(0xFFF5F7FF),
                        fontSize = 30.sp,
                        fontFamily = AppFont,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.4).sp
                    )
                    Text(
                        "Boost your presence. Grow your audience.",
                        color = Color(0xFF9C9CB0),
                        fontSize = 14.sp,
                        fontFamily = AppFont,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
