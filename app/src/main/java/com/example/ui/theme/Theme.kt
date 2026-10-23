package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.example.data.local.ThemeMode

private val DarkColorScheme = darkColorScheme(
  primary = CyanNeon,
  onPrimary = Color(0xFF002028),
  primaryContainer = Color(0xFF004D5A),
  onPrimaryContainer = Color(0xFFA6EEFF),
  secondary = EmeraldNeon,
  onSecondary = Color(0xFF003822),
  secondaryContainer = EmeraldDark,
  onSecondaryContainer = EmeraldAccent,
  tertiary = AmberIndustrial,
  onTertiary = Color(0xFF452B00),
  tertiaryContainer = AmberDark,
  onTertiaryContainer = AmberGlow,
  background = DarkBg,
  onBackground = TextPrimaryDark,
  surface = DarkSurface,
  onSurface = TextPrimaryDark,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = TextSecondaryDark,
  outline = DarkCardBorder,
  error = RedCrimson,
  onError = Color.White
)

private val LightColorScheme = lightColorScheme(
  primary = CyanMuted,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFBAE6FD),
  onPrimaryContainer = Color(0xFF0369A1),
  secondary = EmeraldAccent,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFA7F3D0),
  onSecondaryContainer = Color(0xFF065F46),
  tertiary = AmberDark,
  onTertiary = Color.White,
  tertiaryContainer = Color(0xFFFDE68A),
  onTertiaryContainer = Color(0xFF92400E),
  background = LightBg,
  onBackground = TextPrimaryLight,
  surface = LightSurface,
  onSurface = TextPrimaryLight,
  surfaceVariant = LightSurfaceVariant,
  onSurfaceVariant = TextSecondaryLight,
  outline = LightCardBorder,
  error = RedCrimson,
  onError = Color.White
)

@Composable
fun ScrapMasterTheme(
  themeMode: ThemeMode = ThemeMode.DARK,
  content: @Composable () -> Unit
) {
  val isDark = when (themeMode) {
    ThemeMode.DARK -> true
    ThemeMode.LIGHT -> false
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
  }

  val customColors = if (isDark) {
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
  } else {
    ScrapMasterColors(
      background = LightBg,
      backgroundSecondary = LightBgSecondary,
      surface = LightSurface,
      surfaceVariant = LightSurfaceVariant,
      cardBorder = LightCardBorder,
      primaryAccent = CyanMuted,
      secondaryAccent = EmeraldAccent,
      alertAccent = AmberDark,
      textPrimary = TextPrimaryLight,
      textSecondary = TextSecondaryLight,
      textMuted = TextMutedLight,
      isDark = false
    )
  }

  val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

  CompositionLocalProvider(LocalScrapMasterColors provides customColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}
