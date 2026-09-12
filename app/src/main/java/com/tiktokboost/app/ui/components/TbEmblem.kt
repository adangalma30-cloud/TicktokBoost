package com.tiktokboost.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ═════════════════════════════════════════════════════════════════════════════
 *  THE TickTokBoost brand symbol — "Creator Rise".
 *
 *  A creator (head + shoulders) beside three ascending growth bars: the
 *  product story in one silhouette. Rendered in the signature duotone
 *  (cyan ghost offset up-left, pink ghost down-right, solid white face).
 *
 *  This is the SINGLE brand asset: the launcher icon (adaptive foreground),
 *  the splash animation and every in-app LogoMark draw this exact geometry.
 *  Coordinates mirror tools_iconconcepts.py (CONCEPT 2, winner) — keep in sync.
 * ═════════════════════════════════════════════════════════════════════════════
 */
object TbEmblemGeometry {
    /** 108-unit icon viewport. Emblem optical center = (56.5, 52.75). */
    const val CENTER_X = 56.5f
    const val CENTER_Y = 52.75f
    const val GHOST = 3f          // duotone offset in viewport units

    val BrandCyan = Color(0xFF25F4EE)
    val BrandPink = Color(0xFFFE2C55)
    val FaceWhite = Color(0xFFF5F7FF)

    fun build(): Path = Path().apply {
        fillType = PathFillType.EvenOdd
        // creator head
        addOval(Rect(26.5f, 26.5f, 47.5f, 47.5f))
        // creator shoulders
        addRoundRect(RoundRect(22f, 52f, 52f, 79f, CornerRadius(11f)))
        // three ascending growth bars
        addRoundRect(RoundRect(58f, 57f, 67f, 79f, CornerRadius(4.5f)))
        addRoundRect(RoundRect(70f, 45f, 79f, 79f, CornerRadius(4.5f)))
        addRoundRect(RoundRect(82f, 32f, 91f, 79f, CornerRadius(4.5f)))
    }
}

/**
 * Draws the TB emblem. Layer parameters exist so the splash animation can
 * assemble the emblem on screen (ghosts converging, face fading in) while
 * LogoMark uses the settled defaults — same geometry everywhere.
 */
@Composable
fun TbEmblem(
    modifier: Modifier = Modifier,
    size: Dp,
    cyanGhostUnits: Float = TbEmblemGeometry.GHOST,
    pinkGhostUnits: Float = TbEmblemGeometry.GHOST,
    cyanAlpha: Float = 1f,
    pinkAlpha: Float = 1f,
    faceAlpha: Float = 1f,
    faceColor: Color = TbEmblemGeometry.FaceWhite
) {
    val path = remember { TbEmblemGeometry.build() }
    Canvas(modifier.size(size)) {
        val s = this.size.width / 108f
        scale(s) {
            translate(left = 54f - TbEmblemGeometry.CENTER_X, top = 54f - TbEmblemGeometry.CENTER_Y) {
                translate(left = -cyanGhostUnits, top = -cyanGhostUnits) {
                    drawPath(path, TbEmblemGeometry.BrandCyan.copy(alpha = cyanAlpha))
                }
                translate(left = pinkGhostUnits, top = pinkGhostUnits) {
                    drawPath(path, TbEmblemGeometry.BrandPink.copy(alpha = pinkAlpha))
                }
                drawPath(path, faceColor.copy(alpha = faceAlpha))
            }
        }
    }
}
