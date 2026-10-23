package com.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Dark Cyber-Industrial Palette
val DarkBg = Color(0xFF090D16)
val DarkBgSecondary = Color(0xFF0D1117)
val DarkSurface = Color(0xFF131B29)
val DarkSurfaceVariant = Color(0xFF1A2436)
val DarkCardBorder = Color(0xFF1E2D44)

// Light Clean-Tech Neutral Palette
val LightBg = Color(0xFFF1F5F9)
val LightBgSecondary = Color(0xFFE2E8F0)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF8FAFC)
val LightCardBorder = Color(0xFFCBD5E1)

// Shared High-Tech Accents
val CyanNeon = Color(0xFF00F0FF)
val CyanAccent = Color(0xFF38BDF8)
val CyanMuted = Color(0xFF0284C7)

val EmeraldNeon = Color(0xFF10B981)
val EmeraldDark = Color(0xFF052E16)
val EmeraldAccent = Color(0xFF34D399)

val AmberIndustrial = Color(0xFFF59E0B)
val AmberDark = Color(0xFFD97706)
val AmberGlow = Color(0xFFFBBF24)

val RedCrimson = Color(0xFFEF4444)
val VioletPulse = Color(0xFFA855F7)

val TextPrimaryDark = Color(0xFFF3F4F6)
val TextSecondaryDark = Color(0xFF94A3B8)
val TextMutedDark = Color(0xFF64748B)

val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)
val TextMutedLight = Color(0xFF64748B)

@Immutable
data class ScrapMasterColors(
  val background: Color,
  val backgroundSecondary: Color,
  val surface: Color,
  val surfaceVariant: Color,
  val cardBorder: Color,
  val primaryAccent: Color,
  val secondaryAccent: Color,
  val alertAccent: Color,
  val textPrimary: Color,
  val textSecondary: Color,
  val textMuted: Color,
  val isDark: Boolean
)

val LocalScrapMasterColors = staticCompositionLocalOf {
  ScrapMasterColors(
    background = DarkBg,
    backgroundSecondary = DarkBgSecondary,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    cardBorder = DarkCardBorder,
    primaryAccent = CyanNeon,
    secondaryAccent = EmeraldNeon,
    alertAccent = AmberIndustrial,
    textPrimary = TextPrimaryDark,
    textSecondary = TextSecondaryDark,
    textMuted = TextMutedDark,
    isDark = true
  )
}
