package com.bentech.parschat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
