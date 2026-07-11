package com.dragonic.guard.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// DRAGONIC Guard — Deep Space Glassmorphism Palette
val GuardBlack       = Color(0xFF050810)
val GuardDeepBlue    = Color(0xFF0A0F1E)
val GuardNavy        = Color(0xFF0D1B3E)
val GuardGlass       = Color(0x1A4FC3F7)
val GuardGlassBorder = Color(0x334FC3F7)
val GuardCyan        = Color(0xFF4FC3F7)
val GuardCyanDim     = Color(0xFF29B6F6)
val GuardPurple      = Color(0xFF7C4DFF)
val GuardPurpleDim   = Color(0xFF512DA8)
val GuardRed         = Color(0xFFEF5350)
val GuardGreen       = Color(0xFF66BB6A)
val GuardAmber       = Color(0xFFFFCA28)
val GuardWhite       = Color(0xFFE8F4FD)
val GuardWhiteDim    = Color(0xFF90CAF9)
val GuardSurface     = Color(0x0DFFFFFF)
val GuardSurface2    = Color(0x1AFFFFFF)

private val DarkColorScheme = darkColorScheme(
    primary          = GuardCyan,
    onPrimary        = GuardBlack,
    primaryContainer = GuardNavy,
    secondary        = GuardPurple,
    onSecondary      = GuardWhite,
    background       = GuardBlack,
    onBackground     = GuardWhite,
    surface          = GuardDeepBlue,
    onSurface        = GuardWhite,
    surfaceVariant   = GuardGlass,
    error            = GuardRed,
    onError          = GuardWhite,
)

@Composable
fun DRAGONICGuardTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = GuardTypography,
        content     = content
    )
}
