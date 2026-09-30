package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = AccentCobalt,
    onPrimary = TextInverse,
    primaryContainer = AccentCobaltDeep,
    onPrimaryContainer = AccentCobaltLight,
    secondary = AccentCyan,
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF1E293B),
    onSecondaryContainer = Color(0xFF93C5FD),
    tertiary = AccentEmerald,
    background = SurfaceBase,
    onBackground = TextPrimary,
    surface = SurfaceRaised,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceElevated,
    onSurfaceVariant = TextTertiary,
    outline = BorderMuted,
    outlineVariant = BorderSubtle,
    error = AccentRose,
    onError = TextInverse
)

private val LightColorScheme = lightColorScheme(
    primary = AccentCobalt,
    onPrimary = TextInverse,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Color(0xFF0284C7),
    onSecondary = TextInverse,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = AccentEmerald,
    background = CrispLightBg,
    onBackground = TextPrimaryLight,
    surface = CrispLightSurface,
    onSurface = TextPrimaryLight,
    surfaceVariant = CrispLightSurfaceVariant,
    onSurfaceVariant = TextSecondaryLight,
    outline = CrispLightBorder,
    outlineVariant = Color(0xFFCBD5E1),
    error = Color(0xFFDC2626),
    onError = TextInverse
)

@Composable
fun OpenPulseAITheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
