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
 *  THE TickTokBoost brand symbol — "Motion Lock" TB emblem.
 *
 *  The T's rising crossbar shares its stem with the B's spine: the two letters
 *  lock into ONE emblem. Rendered in the signature duotone (cyan ghost offset
 *  up-left, pink ghost down-right, solid white face).
 *
 *  This is the SINGLE brand asset: the launcher icon (adaptive foreground),
 *  the splash animation and every in-app LogoMark draw this exact geometry.
 *  Coordinates mirror tools_tbemblem.py (CONCEPT A) — keep both in sync.
 * ═════════════════════════════════════════════════════════════════════════════
 */
object TbEmblemGeometry {
    /** 108-unit icon viewport. Emblem optical center = (55, 54). */
    const val CENTER_X = 55f
    const val CENTER_Y = 54f
    const val GHOST = 3f          // duotone offset in viewport units

    val BrandCyan = Color(0xFF25F4EE)
    val BrandPink = Color(0xFFFE2C55)
    val FaceWhite = Color(0xFFF5F7FF)

    fun build(): Path = Path().apply {
        fillType = PathFillType.EvenOdd
        // rising T crossbar
        moveTo(27f, 32f); lineTo(77f, 29f); lineTo(77f, 37f); lineTo(27f, 40f); close()
        // shared spine (T stem = B spine)
        addRect(Rect(48f, 29f, 56f, 79f))
        // B top bowl (outer + counter)
        addRoundRect(RoundRect(56f, 36f, 81f, 57f, CornerRadius(10f)))
        addRoundRect(RoundRect(63f, 41f, 74f, 52f, CornerRadius(5f)))
        // B bottom bowl (outer + counter)
        addRoundRect(RoundRect(56f, 57f, 83f, 79f, CornerRadius(11f)))
        addRoundRect(RoundRect(63f, 62f, 76f, 74f, CornerRadius(5.5f)))
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
