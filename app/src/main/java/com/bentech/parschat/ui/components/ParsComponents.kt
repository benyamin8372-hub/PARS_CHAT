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
import androidx.compose.ui.graphics.drawscope.Stroke
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
