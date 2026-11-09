package com.vocab.core.designsystem.theme

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** One CSS-style shadow layer: an offset, a blur and a color, outside or inset. */
@Immutable
data class ShadowLayer(val dx: Dp, val dy: Dp, val blur: Dp, val color: Color, val inset: Boolean = false)

/** The clay recipe: soft outer drops, a light sheen inside, a shade inside. */
@Immutable
data class ClayShadow(val layers: List<ShadowLayer>) {
    val outer get() = layers.filter { !it.inset }
    val inner get() = layers.filter { it.inset }
}

private fun s(dx: Int, dy: Int, blur: Int, argb: Long, inset: Boolean = false) =
    ShadowLayer(dx.dp, dy.dp, blur.dp, Color(argb), inset)

/** The shadow tokens of the design system, per theme. */
@Immutable
data class ClayShadows(
    val card: ClayShadow,
    val raised: ClayShadow,
    val sunk: ClayShadow,
    val button: ClayShadow,
    val buttonPressed: ClayShadow,
    val buttonSoft: ClayShadow,
    val buttonGo: ClayShadow,
    val pill: ClayShadow,
)

val LightClayShadows = ClayShadows(
    card = ClayShadow(listOf(s(8, 10, 22, 0x73C9C3E6), s(-6, -6, 14, 0xCCFFFFFF), s(3, 3, 6, 0xE6FFFFFF, true), s(-4, -4, 8, 0x80DCD6F2, true))),
    raised = ClayShadow(listOf(s(4, 6, 10, 0x73C9C3E6), s(2, 2, 4, 0x99FFFFFF, true), s(-2, -3, 5, 0x14000000, true))),
    sunk = ClayShadow(listOf(s(3, 3, 6, 0x80CFC9E8, true), s(-3, -3, 6, 0xCCFFFFFF, true))),
    button = ClayShadow(listOf(s(0, 4, 0, 0xFF5C45C8), s(0, 8, 14, 0x40755BE8), s(0, 2, 3, 0x59FFFFFF, true), s(0, -3, 5, 0x1A000000, true))),
    buttonPressed = ClayShadow(listOf(s(0, 1, 0, 0xFF5C45C8), s(0, 2, 5, 0x21000000, true))),
    buttonSoft = ClayShadow(listOf(s(0, 4, 0, 0xFFDCD8EE), s(0, 8, 14, 0x4DC9C3E6), s(0, 2, 3, 0xFFFFFFFF, true))),
    buttonGo = ClayShadow(listOf(s(0, 4, 0, 0xFF6F9C46), s(0, 8, 14, 0x4096C469), s(0, 2, 3, 0x59FFFFFF, true), s(0, -3, 5, 0x1A000000, true))),
    pill = ClayShadow(listOf(s(2, 3, 6, 0x80C9C3E6), s(1, 1, 2, 0xD9FFFFFF, true), s(-1, -2, 3, 0x1A000000, true))),
)

val DarkClayShadows = ClayShadows(
    card = ClayShadow(listOf(s(10, 12, 24, 0x73000000), s(-4, -4, 12, 0x08FFFFFF), s(2, 2, 5, 0x14FFFFFF, true), s(-4, -4, 8, 0x4D000000, true))),
    raised = ClayShadow(listOf(s(4, 6, 10, 0x66000000), s(2, 2, 4, 0x1FFFFFFF, true), s(-2, -3, 5, 0x40000000, true))),
    sunk = ClayShadow(listOf(s(3, 3, 6, 0x80000000, true), s(-2, -2, 5, 0x0DFFFFFF, true))),
    button = ClayShadow(listOf(s(0, 4, 0, 0xFF6650D0), s(0, 8, 14, 0x66000000), s(0, 2, 3, 0x4DFFFFFF, true), s(0, -3, 5, 0x1F000000, true))),
    buttonPressed = ClayShadow(listOf(s(0, 1, 0, 0xFF6650D0), s(0, 2, 5, 0x40000000, true))),
    buttonSoft = ClayShadow(listOf(s(0, 4, 0, 0xFF14121B), s(0, 8, 14, 0x66000000), s(0, 2, 3, 0x14FFFFFF, true))),
    buttonGo = ClayShadow(listOf(s(0, 4, 0, 0xFF6F9C46), s(0, 8, 14, 0x66000000), s(0, 2, 3, 0x4DFFFFFF, true), s(0, -3, 5, 0x1F000000, true))),
    pill = ClayShadow(listOf(s(2, 3, 6, 0x66000000), s(1, 1, 2, 0x1FFFFFFF, true), s(-1, -2, 3, 0x40000000, true))),
)

/**
 * Paints a clay surface: outer shadows, the fill, then inset shadows clipped to the shape.
 * Use instead of `background` on anything that should look puffy.
 */
fun Modifier.clay(fill: Color, radius: Dp, shadow: ClayShadow): Modifier = this
    .drawBehind {
        val r = radius.toPx()
        shadow.outer.forEach { drawOuter(it, r) }
        drawRoundRect(fill, cornerRadius = androidx.compose.ui.geometry.CornerRadius(r, r))
    }
    .drawWithContent {
        drawContent()
        val r = radius.toPx()
        shadow.inner.forEach { drawInner(it, r) }
    }

private fun DrawScope.drawOuter(layer: ShadowLayer, radius: Float) {
    val paint = layer.paint(this)
    val dx = layer.dx.toPx()
    val dy = layer.dy.toPx()
    drawContext.canvas.nativeCanvas.drawRoundRect(
        RectF(dx, dy, size.width + dx, size.height + dy), radius, radius, paint,
    )
}

private fun DrawScope.drawInner(layer: ShadowLayer, radius: Float) {
    val canvas = drawContext.canvas.nativeCanvas
    val shape = Path().apply { addRoundRect(RectF(0f, 0f, size.width, size.height), radius, radius, Path.Direction.CW) }
    val spread = layer.blur.toPx() * 2 + maxOf(size.width, size.height)
    val frame = Path().apply {
        fillType = Path.FillType.EVEN_ODD
        addRect(-spread, -spread, size.width + spread, size.height + spread, Path.Direction.CW)
        addRoundRect(RectF(0f, 0f, size.width, size.height), radius, radius, Path.Direction.CW)
        offset(layer.dx.toPx(), layer.dy.toPx())
    }
    canvas.save()
    canvas.clipPath(shape)
    canvas.drawPath(frame, layer.paint(this))
    canvas.restore()
}

private fun ShadowLayer.paint(scope: DrawScope) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = this@paint.color.toArgb()
    val blurPx = with(scope) { blur.toPx() }
    if (blurPx > 0f) maskFilter = BlurMaskFilter(blurPx / 2f, BlurMaskFilter.Blur.NORMAL)
}
