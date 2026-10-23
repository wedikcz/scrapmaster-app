package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalScrapMasterColors
import com.example.ui.theme.TechMonoFamily

@Composable
fun CyberCard(
  modifier: Modifier = Modifier,
  borderColor: Color? = null,
  cornerRadius: Dp = 14.dp,
  content: @Composable () -> Unit
) {
  val colors = LocalScrapMasterColors.current
  val border = borderColor ?: colors.cardBorder

  Surface(
    modifier = modifier
      .clip(RoundedCornerShape(cornerRadius))
      .border(
        width = 1.dp,
        color = border,
        shape = RoundedCornerShape(cornerRadius)
      ),
    color = colors.surface.copy(alpha = if (colors.isDark) 0.85f else 0.95f),
    shape = RoundedCornerShape(cornerRadius)
  ) {
    content()
  }
}

@Composable
fun IndustrialBadge(
  text: String,
  color: Color? = null,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null
) {
  val colors = LocalScrapMasterColors.current
  val badgeColor = color ?: colors.primaryAccent
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(6.dp))
      .background(badgeColor.copy(alpha = if (colors.isDark) 0.18f else 0.12f))
      .border(0.8.dp, badgeColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
      .padding(horizontal = 7.dp, vertical = 3.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = badgeColor,
          modifier = Modifier.size(11.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
      }
      Text(
        text = text,
        color = badgeColor,
        fontFamily = TechMonoFamily,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.6.sp
      )
    }
  }
}

@Composable
fun NeonButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  color: Color? = null,
  enabled: Boolean = true,
  testTag: String = "neon_button"
) {
  val colors = LocalScrapMasterColors.current
  val baseColor = color ?: colors.primaryAccent

  Button(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.testTag(testTag),
    shape = RoundedCornerShape(10.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = baseColor,
      contentColor = if (colors.isDark) Color(0xFF031018) else Color.White,
      disabledContainerColor = colors.cardBorder,
      disabledContentColor = colors.textMuted
    ),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          modifier = Modifier.size(17.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
      }
      Text(
        text = text,
        fontFamily = TechMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp
      )
    }
  }
}

@Composable
fun OutlinedCyberButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  icon: ImageVector? = null,
  color: Color? = null,
  enabled: Boolean = true,
  testTag: String = "outlined_cyber_button"
) {
  val colors = LocalScrapMasterColors.current
  val baseColor = color ?: colors.primaryAccent

  OutlinedButton(
    onClick = onClick,
    enabled = enabled,
    modifier = modifier.testTag(testTag),
    shape = RoundedCornerShape(10.dp),
    border = BorderStroke(1.dp, if (enabled) baseColor.copy(alpha = 0.7f) else colors.cardBorder),
    colors = ButtonDefaults.outlinedButtonColors(
      contentColor = baseColor,
      disabledContentColor = colors.textMuted
    ),
    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      if (icon != null) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = baseColor,
          modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
      }
      Text(
        text = text,
        fontFamily = TechMonoFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp
      )
    }
  }
}
