package com.bentech.parschat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Midnight = Color(0xFF091326)
private val Lapis = Color(0xFF14264A)
private val RaisedLapis = Color(0xFF263D68)
private val Gold = Color(0xFFC9A45D)
private val PaleGold = Color(0xFFE9D29B)
private val Turquoise = Color(0xFF559B96)
private val Ivory = Color(0xFFF3EFE5)


/**
 * Stylized Faravahar-inspired winged emblem for the PARS CHAT identity.
 * Original geometric artwork, rendered in metallic gold.
 */
@Composable
fun FaravaharMark(
    modifier: Modifier = Modifier,
    diameter: Dp = 64.dp,
    glow: Float = 0.55f
) {
    Box(
        modifier = modifier
            .size(diameter)
            .shadow(14.dp, CircleShape)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color(0xFF29436D), Midnight)))
            .border(1.5.dp, PaleGold.copy(alpha = glow), CircleShape)
            .padding(diameter * .12f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val sx = size.width / 108f
            val sy = size.height / 108f
            val gold = Color(0xFFE9D29B).copy(alpha = .96f)
            val fine = Color(0xFFC9A45D).copy(alpha = .88f)
            fun pt(x: Float, y: Float) = androidx.compose.ui.geometry.Offset(x * sx, y * sy)
            val stroke = 2.7f * sx

            val leftWing = Path().apply {
                moveTo(49f * sx, 43f * sy)
                cubicTo(37f * sx, 30f * sy, 20f * sx, 24f * sy, 5f * sx, 27f * sy)
                cubicTo(17f * sx, 35f * sy, 26f * sx, 42f * sy, 36f * sx, 48f * sy)
                cubicTo(24f * sx, 45f * sy, 15f * sx, 47f * sy, 7f * sx, 53f * sy)
                cubicTo(23f * sx, 56f * sy, 36f * sx, 54f * sy, 49f * sx, 50f * sy)
            }
            val rightWing = Path().apply {
                moveTo(59f * sx, 43f * sy)
                cubicTo(71f * sx, 30f * sy, 88f * sx, 24f * sy, 103f * sx, 27f * sy)
                cubicTo(91f * sx, 35f * sy, 82f * sx, 42f * sy, 72f * sx, 48f * sy)
                cubicTo(84f * sx, 45f * sy, 93f * sx, 47f * sy, 101f * sx, 53f * sy)
                cubicTo(85f * sx, 56f * sy, 72f * sx, 54f * sy, 59f * sx, 50f * sy)
            }
            drawPath(leftWing, gold, style = Stroke(stroke, cap = StrokeCap.Round))
            drawPath(rightWing, gold, style = Stroke(stroke, cap = StrokeCap.Round))
            for (i in 0..3) {
                val y = 31f + i * 5.2f
                drawLine(fine, pt(11f + i * 2f, y + 3f), pt(43f, y + 12f), strokeWidth = 1.35f * sx, cap = StrokeCap.Round)
                drawLine(fine, pt(97f - i * 2f, y + 3f), pt(65f, y + 12f), strokeWidth = 1.35f * sx, cap = StrokeCap.Round)
            }

            drawCircle(fine, radius = 17f * sx, center = pt(54f, 43f), style = Stroke(1.8f * sx))
            drawCircle(gold, radius = 5.2f * sx, center = pt(54f, 25f), style = Stroke(2.2f * sx))
            drawLine(gold, pt(54f, 31f), pt(54f, 55f), strokeWidth = 2.5f * sx, cap = StrokeCap.Round)
            drawLine(gold, pt(54f, 36f), pt(43f, 42f), strokeWidth = 2.2f * sx, cap = StrokeCap.Round)
            drawLine(gold, pt(54f, 36f), pt(65f, 42f), strokeWidth = 2.2f * sx, cap = StrokeCap.Round)
            drawCircle(gold, radius = 1.8f * sx, center = pt(42f, 42f))
            drawCircle(gold, radius = 1.8f * sx, center = pt(66f, 42f))

            val tail = Path().apply {
                moveTo(54f * sx, 55f * sy)
                cubicTo(47f * sx, 64f * sy, 42f * sx, 72f * sy, 35f * sx, 79f * sy)
                moveTo(54f * sx, 55f * sy)
                cubicTo(54f * sx, 66f * sy, 54f * sx, 74f * sy, 54f * sx, 84f * sy)
                moveTo(54f * sx, 55f * sy)
                cubicTo(61f * sx, 64f * sy, 66f * sx, 72f * sy, 73f * sx, 79f * sy)
            }
            drawPath(tail, gold, style = Stroke(2.2f * sx, cap = StrokeCap.Round))
            drawLine(fine, pt(35f, 79f), pt(43f, 75f), strokeWidth = 1.3f * sx)
            drawLine(fine, pt(54f, 84f), pt(54f, 75f), strokeWidth = 1.3f * sx)
            drawLine(fine, pt(73f, 79f), pt(65f, 75f), strokeWidth = 1.3f * sx)
        }
    }
}

/** Horizontal brand lockup: Faravahar emblem followed by the Persian and Latin wordmarks. */
@Composable
fun ParsBrandLockup(
    modifier: Modifier = Modifier,
    emblemSize: Dp = 58.dp,
    glow: Float = 0.55f
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        FaravaharChatMark(diameter = emblemSize, glow = glow)
        Column(Modifier.padding(start = 12.dp)) {
            Text("چت پارس", color = PaleGold, fontSize = 23.sp, fontWeight = FontWeight.Black)
            Text("PARS CHAT", color = Ivory.copy(alpha = .82f), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.4.sp)
        }
    }
}

/**
 * Reusable raised lapis surface with a restrained metallic keyline.
 * Use for important content panels, not every list row.
 */
@Composable
fun GoldRimCard(
    modifier: Modifier = Modifier,
    radius: Dp = 22.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(radius)
    Column(
        modifier = modifier
            .shadow(12.dp, shape)
            .clip(shape)
            .background(
                Brush.linearGradient(
                    listOf(RaisedLapis, Lapis, Midnight)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(listOf(PaleGold.copy(alpha = .78f), Gold.copy(alpha = .25f), Gold.copy(alpha = .62f))),
                shape = shape
            )
            .padding(16.dp),
        content = content
    )
}

/** Dimensional circular emblem for the app identity and section highlights. */
@Composable
fun PersianMedallion(
    modifier: Modifier = Modifier,
    diameter: Dp = 76.dp,
    symbol: String = "𐎠"
) {
    Box(
        modifier = modifier
            .size(diameter)
            .shadow(14.dp, CircleShape)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(RaisedLapis, Lapis, Midnight)
                )
            )
            .border(
                2.dp,
                Brush.linearGradient(listOf(PaleGold, Gold, Color(0xFF80633A))),
                CircleShape
            )
            .padding(4.dp)
            .border(1.dp, Gold.copy(alpha = .38f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = PaleGold,
            fontSize = (diameter.value * .48f).sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}


/**
 * PARS CHAT signature: a stylized Faravahar framed by a speech bubble.
 * Drawn natively in Compose so the mark stays crisp at every screen density.
 */
@Composable
fun FaravaharChatMark(
    modifier: Modifier = Modifier,
    diameter: Dp = 88.dp,
    glow: Float = 1f
) {
    Box(
        modifier = modifier
            .size(diameter)
            .shadow(16.dp, CircleShape)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color(0xFF304A75), Lapis, Midnight)))
            .border(1.6.dp, PaleGold.copy(alpha = .78f * glow.coerceIn(.2f, 1f)), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize().padding(diameter * .13f)) {
            val w = size.width
            val h = size.height
            val gold = PaleGold.copy(alpha = glow.coerceIn(.2f, 1f))
            val teal = Turquoise
            val cx = w * .49f
            val cy = h * .43f
            val stroke = (w * .035f).coerceAtLeast(1.2.dp.toPx())
            // Wing roots and three ascending feather tiers.
            drawLine(gold, androidx.compose.ui.geometry.Offset(cx - w*.08f, cy-h*.02f), androidx.compose.ui.geometry.Offset(w*.08f, cy-h*.20f), stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(gold, androidx.compose.ui.geometry.Offset(cx + w*.08f, cy-h*.02f), androidx.compose.ui.geometry.Offset(w*.92f, cy-h*.20f), stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            for (i in 0..4) {
                val t = i / 4f
                val y = cy - h*(.19f - t*.025f)
                val leftEnd = w*(.10f + t*.055f)
                val rightEnd = w - leftEnd
                drawLine(gold, androidx.compose.ui.geometry.Offset(cx-w*.08f, cy-h*.015f), androidx.compose.ui.geometry.Offset(leftEnd, y), stroke*.82f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                drawLine(gold, androidx.compose.ui.geometry.Offset(cx+w*.08f, cy-h*.015f), androidx.compose.ui.geometry.Offset(rightEnd, y), stroke*.82f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            }
            // Central solar disc and the human figure's head/shoulders.
            drawCircle(gold, radius = w*.105f, center = androidx.compose.ui.geometry.Offset(cx, cy-h*.015f), style = Stroke(stroke))
            drawCircle(gold, radius = w*.035f, center = androidx.compose.ui.geometry.Offset(cx, cy-h*.015f))
            drawLine(gold, androidx.compose.ui.geometry.Offset(cx, cy+h*.075f), androidx.compose.ui.geometry.Offset(cx, cy+h*.29f), stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(gold, androidx.compose.ui.geometry.Offset(cx, cy+h*.13f), androidx.compose.ui.geometry.Offset(cx-w*.13f, cy+h*.08f), stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(gold, androidx.compose.ui.geometry.Offset(cx, cy+h*.13f), androidx.compose.ui.geometry.Offset(cx+w*.13f, cy+h*.08f), stroke, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            // Three tail feathers.
            for (i in -1..1) {
                drawLine(gold, androidx.compose.ui.geometry.Offset(cx, cy+h*.27f), androidx.compose.ui.geometry.Offset(cx+i*w*.13f, h*.91f), stroke*.9f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            }
            // Speech-bubble outline: a modern chat cue integrated at the lower-right.
            val bubbleLeft = w*.61f
            val bubbleTop = h*.59f
            val bubbleRight = w*.96f
            val bubbleBottom = h*.86f
            drawRoundRect(
                color = teal,
                topLeft = androidx.compose.ui.geometry.Offset(bubbleLeft, bubbleTop),
                size = androidx.compose.ui.geometry.Size(bubbleRight-bubbleLeft, bubbleBottom-bubbleTop),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(w*.07f),
                style = Stroke(width = stroke*1.15f)
            )
            drawLine(teal, androidx.compose.ui.geometry.Offset(w*.70f, bubbleBottom), androidx.compose.ui.geometry.Offset(w*.64f, h*.95f), stroke*1.1f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            drawLine(teal, androidx.compose.ui.geometry.Offset(w*.64f, h*.95f), androidx.compose.ui.geometry.Offset(w*.79f, bubbleBottom), stroke*1.1f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            for (i in 0..1) drawCircle(teal, radius = stroke*.72f, center = androidx.compose.ui.geometry.Offset(w*(.73f+i*.09f), h*.72f))
        }
    }
}

/** Avatar with inset stone surface and a thin brushed-gold rim. */
@Composable
fun DimensionalAvatar(
    label: String,
    modifier: Modifier = Modifier,
    diameter: Dp = 52.dp,
    online: Boolean = false
) {
    Box(modifier = modifier.size(diameter), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(diameter)
                .shadow(7.dp, CircleShape)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(PaleGold, Gold, Color(0xFF765A32))))
                .padding(2.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(RaisedLapis, Lapis, Midnight))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label.take(1),
                color = Ivory,
                fontSize = (diameter.value * .38f).sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        if (online) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(Turquoise)
                    .border(2.dp, Midnight, CircleShape)
            )
        }
    }
}

/** Gold primary action; reserve for the single most important action on a screen. */
@Composable
fun GoldActionSurface(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 50.dp)
            .shadow(if (enabled) 8.dp else 0.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (enabled) Brush.linearGradient(listOf(PaleGold, Gold, Color(0xFFAD8748)))
                else Brush.linearGradient(listOf(Color.Gray, Color.DarkGray))
            )
            .padding(horizontal = 22.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Midnight, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

/** Chat bubble surface with clear incoming/outgoing contrast and restrained gold detail. */
@Composable
fun ParsMessageBubble(
    message: String,
    time: String,
    outgoing: Boolean,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(
        topStart = 18.dp,
        topEnd = 18.dp,
        bottomEnd = if (outgoing) 5.dp else 18.dp,
        bottomStart = if (outgoing) 18.dp else 5.dp
    )
    Column(
        modifier = modifier
            .shadow(5.dp, shape)
            .clip(shape)
            .background(
                if (outgoing) Brush.linearGradient(listOf(Color(0xFF294C70), Color(0xFF193657)))
                else Brush.linearGradient(listOf(Color(0xFF233653), Color(0xFF172943)))
            )
            .border(
                width = if (outgoing) 1.dp else .5.dp,
                color = if (outgoing) Gold.copy(alpha = .58f) else Color.White.copy(alpha = .08f),
                shape = shape
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(message, color = Ivory, fontSize = 14.sp, lineHeight = 21.sp)
        Spacer(Modifier.size(5.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(time, color = PaleGold.copy(alpha = .78f), fontSize = 10.sp)
        }
    }
}
