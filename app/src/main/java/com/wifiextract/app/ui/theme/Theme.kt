package com.wifiextract.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class MnaColorScheme(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceFocus: Color,
    val navBar: Color,
    val accent: Color,
    val accentButton: Color,
    val accentText: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val connected: Color,
    val disconnected: Color,
    val warning: Color
)

val DarkMnaColorScheme = MnaColorScheme(
    isDark = true,
    background = Color(0xFF0A0A0B),
    surface = Color(0xFF17171A),
    surfaceRaised = Color(0xFF202024),
    surfaceFocus = Color(0xFF2A2A30),
    navBar = Color(0xFF101012),
    accent = Color(0xFFE5322D),
    accentButton = Color(0xFFD92B26),
    accentText = Color(0xFFFF5A55),
    textPrimary = Color(0xFFF2F2F3),
    textSecondary = Color(0xFFA0A0A8),
    connected = Color(0xFF38BDF8),
    disconnected = Color(0xFFFF5A55),
    warning = Color(0xFFE5B45C)
)

val LightMnaColorScheme = MnaColorScheme(
    isDark = false,
    background = Color(0xFFF4F4F6),
    surface = Color(0xFFFFFFFF),
    surfaceRaised = Color(0xFFEAEAEE),
    surfaceFocus = Color(0xFFE0E0E6),
    navBar = Color(0xFFEAEAEA),
    accent = Color(0xFFD92B26),
    accentButton = Color(0xFFD92B26),
    accentText = Color(0xFFC0221E),
    textPrimary = Color(0xFF1C1C1E),
    textSecondary = Color(0xFF6C6C70),
    connected = Color(0xFF0284C7),
    disconnected = Color(0xFFDC2626),
    warning = Color(0xFFD97706)
)

val LocalMnaColors = staticCompositionLocalOf { DarkMnaColorScheme }

/** Design tokens from the MNA mockup. Surfaces separate content; no gray borders are used. */
object MnaColors {
    val isDark: Boolean @Composable get() = LocalMnaColors.current.isDark
    val Background: Color @Composable get() = LocalMnaColors.current.background
    val Surface: Color @Composable get() = LocalMnaColors.current.surface
    val SurfaceRaised: Color @Composable get() = LocalMnaColors.current.surfaceRaised
    val SurfaceFocus: Color @Composable get() = LocalMnaColors.current.surfaceFocus
    val NavBar: Color @Composable get() = LocalMnaColors.current.navBar
    val Accent: Color @Composable get() = LocalMnaColors.current.accent
    val AccentButton: Color @Composable get() = LocalMnaColors.current.accentButton
    val AccentText: Color @Composable get() = LocalMnaColors.current.accentText
    val TextPrimary: Color @Composable get() = LocalMnaColors.current.textPrimary
    val TextSecondary: Color @Composable get() = LocalMnaColors.current.textSecondary
    val Connected: Color @Composable get() = LocalMnaColors.current.connected
    val Disconnected: Color @Composable get() = LocalMnaColors.current.disconnected
    val Success: Color @Composable get() = LocalMnaColors.current.connected
    val Warning: Color @Composable get() = LocalMnaColors.current.warning
}

object MnaShapes {
    val Card = RoundedCornerShape(16.dp)
    val Button = RoundedCornerShape(12.dp)
    val Chip = RoundedCornerShape(50)
    val Pill = RoundedCornerShape(50)
}

@Composable
fun MnaTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkMnaColorScheme else LightMnaColorScheme
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.accent,
            onPrimary = Color.White,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.surfaceRaised,
            onSurfaceVariant = colors.textSecondary,
            error = colors.accentText
        )
    } else {
        lightColorScheme(
            primary = colors.accent,
            onPrimary = Color.White,
            background = colors.background,
            onBackground = colors.textPrimary,
            surface = colors.surface,
            onSurface = colors.textPrimary,
            surfaceVariant = colors.surfaceRaised,
            onSurfaceVariant = colors.textSecondary,
            error = colors.accentText
        )
    }

    CompositionLocalProvider(LocalMnaColors provides colors) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
