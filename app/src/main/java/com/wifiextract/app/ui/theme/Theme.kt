package com.wifiextract.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Design tokens from the MNA mockup. Surfaces separate content; no gray borders are used. */
object MnaColors {
    val Background = Color(0xFF0A0A0B)
    val Surface = Color(0xFF17171A)
    val SurfaceRaised = Color(0xFF202024)
    val SurfaceFocus = Color(0xFF2A2A30)
    val NavBar = Color(0xFF101012)
    val Accent = Color(0xFFE5322D)        // icons, large text
    val AccentButton = Color(0xFFD92B26)  // button container (4.8:1 with white text)
    val AccentText = Color(0xFFFF5A55)    // small red text on dark surfaces
    val TextPrimary = Color(0xFFF2F2F3)
    val TextSecondary = Color(0xFFA0A0A8)
    val Connected = Color(0xFF38BDF8)     // High-contrast cyan blue for active connection (non-green)
    val Disconnected = Color(0xFFFF5A55)  // Coral red for disconnected
    val Success = Color(0xFF38BDF8)
    val Warning = Color(0xFFE5B45C)
}

object MnaShapes {
    val Card = RoundedCornerShape(4.dp)
    val Button = RoundedCornerShape(12.dp)
    val Chip = RoundedCornerShape(4.dp)
}

@Composable
fun MnaTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = MnaColors.Accent,
        onPrimary = Color.White,
        background = MnaColors.Background,
        onBackground = MnaColors.TextPrimary,
        surface = MnaColors.Surface,
        onSurface = MnaColors.TextPrimary,
        surfaceVariant = MnaColors.SurfaceRaised,
        onSurfaceVariant = MnaColors.TextSecondary,
        error = MnaColors.AccentText
    )
    MaterialTheme(colorScheme = scheme, content = content)
}
