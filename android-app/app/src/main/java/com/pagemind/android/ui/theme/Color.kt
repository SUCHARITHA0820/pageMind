package com.pagemind.android.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// PageMind Web App Color Palette
val BgDark = Color(0xFF0A0C16)
val BgSurface = Color(0xFF111425)
val CardBg = Color(0xFF161B2E)
val CardBorder = Color(0x1AFFFFFF)
val BorderSubtle = Color(0x14FFFFFF)

val PrimaryViolet = Color(0xFF6366F1)
val PrimaryHover = Color(0xFF4F46E5)
val AccentCyan = Color(0xFF06B6D4)
val AccentPink = Color(0xFFEC4899)

val TextMain = Color(0xFFF8FAFC)
val TextMuted = Color(0xFF94A3B8)

// Legacy names for screen colors
val BackgroundDark = BgDark
val SurfaceDark = CardBg
val TextSecondary = TextMuted
val AccentGradientStart = PrimaryViolet
val AccentGradientEnd = AccentCyan

// Gradients matching web app buttons & headers
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(PrimaryViolet, AccentCyan)
)

val SecondaryGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFA5B4FC), Color(0xFF38BDF8), Color(0xFFF472B6))
)

val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(BgDark, Color(0xFF0F172A), BgSurface)
)
