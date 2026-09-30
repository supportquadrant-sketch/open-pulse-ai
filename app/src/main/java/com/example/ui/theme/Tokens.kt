package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Design System Foundations & Semantic Tokens
 * Color, Spacing, Radius, Motion, and Typography Scales
 */
object DesignTokens {

    object ColorPalette {
        // Core Surface Tokens
        val SurfaceBase = Color(0xFF000000)
        val SurfaceMuted = Color(0xFF0F0F0F)
        val SurfaceRaised = Color(0xFF171717)
        val SurfaceElevated = Color(0xFF202124)
        val SurfaceStrong = Color(0xFF1F3B9B) // Deep Cobalt Foundation

        // Text & Content Tokens
        val TextPrimary = Color(0xFFE6E6E6)
        val TextSecondary = Color(0xFFE3E3E3)
        val TextTertiary = Color(0xFFC4C7C5)
        val TextInverse = Color(0xFFFFFFFF)

        // Accent & Interactive Tokens
        val AccentCobalt = Color(0xFF3B82F6)
        val AccentCobaltDeep = Color(0xFF1F3B9B)
        val AccentCobaltLight = Color(0xFF60A5FA)
        val AccentCyan = Color(0xFF00E5FF)
        val AccentEmerald = Color(0xFF38EF7D)

        // Border & Outline Tokens
        val BorderMuted = Color(0xFF262626)
        val BorderSubtle = Color(0xFF333333)
        val BorderActive = Color(0xFF3B82F6)

        // State & Feedback Tokens
        val StateError = Color(0xFFEF4444)
        val StateErrorBg = Color(0xFF2C1515)
        val StateSuccess = Color(0xFF10B981)
        val StateWarning = Color(0xFFF59E0B)
    }

    object Spacing {
        val Space1 = 1.dp
        val Space2 = 4.dp
        val Space3 = 5.dp
        val Space4 = 6.dp
        val Space5 = 8.dp
        val Space6 = 12.dp
        val Space7 = 16.dp
        val Space8 = 24.dp
        val Space9 = 32.dp
    }

    object Radius {
        val Xs = 9999.dp // Capsule / Pill
        val Sm = 4.dp
        val Md = 8.dp
        val Lg = 12.dp
        val Xl = 16.dp
        val Full = 9999.dp

        val ShapePill = CircleShape
        val ShapeCard = RoundedCornerShape(12.dp)
        val ShapeButton = CircleShape
        val ShapeTag = RoundedCornerShape(9999.dp)
        val ShapeInput = RoundedCornerShape(24.dp)
    }

    object TypographyScale {
        val SizeXs = 13.sp
        val SizeSm = 13.33.sp
        val SizeMd = 14.sp
        val SizeLg = 16.sp
        val SizeXl = 17.sp
        val Size2xl = 20.sp
        val Size3xl = 24.sp
        val Size4xl = 32.sp
    }

    object Motion {
        const val DurationInstant = 200
        const val DurationFast = 280
        const val DurationStandard = 350
    }
}
