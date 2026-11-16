package com.bentech.parschat.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * PARS CHAT visual tokens.
 *
 * Inspired by pre-Islamic Iranian stonework and restrained metal inlay:
 * deep lapis, brushed gold, and a limited turquoise signal color.
 * Keep ornament and glow subtle so functional content remains legible.
 */
object ParsPalette {
    val Midnight = Color(0xFF091326)
    val Lapis = Color(0xFF142238)
    val LapisRaised = Color(0xFF1C304A)
    val LapisHighlight = Color(0xFF29435D)
    val StoneShadow = Color(0xFF07101E)

    val BrushedGold = Color(0xFFC4A568)
    val GoldLight = Color(0xFFE0C58D)
    val GoldDeep = Color(0xFF80643A)

    val StoneTurquoise = Color(0xFF397F7B)
    val TurquoiseLight = Color(0xFF72AAA2)

    val Limestone = Color(0xFFF1EEE6)
    val LimestoneRaised = Color(0xFFFAF8F2)
    val Ink = Color(0xFF202B38)
    val MutedInk = Color(0xFF66717C)

    val TextPrimaryDark = Color(0xFFF3F0E8)
    val TextSecondaryDark = Color(0xFFB9C3CC)
    val DividerDark = Color(0xFF35475A)
    val Danger = Color(0xFFB85C58)
    val Success = Color(0xFF57947C)
}

object ParsBrushes {
    val LapisDepth = Brush.verticalGradient(
        colors = listOf(
            ParsPalette.LapisHighlight,
            ParsPalette.Lapis,
            ParsPalette.Midnight
        )
    )

    val RaisedStone = Brush.linearGradient(
        colors = listOf(
            ParsPalette.LapisRaised,
            ParsPalette.Lapis,
            ParsPalette.StoneShadow
        )
    )

    val BrushedGold = Brush.linearGradient(
        colors = listOf(
            ParsPalette.GoldLight,
            ParsPalette.BrushedGold,
            ParsPalette.GoldDeep
        )
    )

    val Medallion = Brush.radialGradient(
        colors = listOf(
            ParsPalette.LapisHighlight,
            ParsPalette.Lapis,
            ParsPalette.Midnight
        )
    )

    val Limestone = Brush.verticalGradient(
        colors = listOf(
            ParsPalette.LimestoneRaised,
            ParsPalette.Limestone
        )
    )
}

object ParsShapes {
    val Panel = RoundedCornerShape(22.dp)
    val Card = RoundedCornerShape(18.dp)
    val Control = RoundedCornerShape(14.dp)
    val Bubble = RoundedCornerShape(18.dp)
    val Pill = RoundedCornerShape(50)
}

object ParsSpacing {
    val xxs = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val pageHorizontal = 20.dp
}
