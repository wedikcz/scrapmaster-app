package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.HardwareDiagnosticReport
import com.example.data.local.ThemeMode
import com.example.ui.theme.LocalScrapMasterColors
import com.example.ui.theme.TechMonoFamily

@Composable
fun TopWorkshopHeader(
  currentTheme: ThemeMode,
  onToggleTheme: () -> Unit,
  lowStockCount: Int = 0,
  diagnosticReport: HardwareDiagnosticReport? = null,
  isBenchmarking: Boolean = false,
  onRunBenchmark: () -> Unit = {},
  onOpenMcpTerminal: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val colors = LocalScrapMasterColors.current

  Box(
    modifier = modifier
      .fillMaxWidth()
      .background(colors.background.copy(alpha = 0.96f))
      .padding(horizontal = 14.dp, vertical = 8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Left: Logo & Workshop Brand
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.primaryAccent.copy(alpha = 0.15f))
            .border(1.dp, colors.primaryAccent.copy(alpha = 0.6f), RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Memory,
            contentDescription = "Logo",
            tint = colors.primaryAccent,
            modifier = Modifier.size(20.dp)
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = "SCRAPMASTER",
              fontFamily = TechMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.5.sp,
              color = colors.textPrimary,
              letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "AI",
              fontFamily = TechMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = colors.primaryAccent
            )
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(colors.secondaryAccent)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "REVERSE ENG v2.0",
              fontFamily = TechMonoFamily,
              fontSize = 8.5.sp,
              color = colors.textMuted,
              letterSpacing = 0.5.sp
            )
          }
        }
      }

      // Right: WebGPU / NPU Indicator + Theme Toggle
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (diagnosticReport != null) {
          WebGpuDiagnosticIndicator(
            report = diagnosticReport,
            isBenchmarking = isBenchmarking,
            onRunBenchmark = onRunBenchmark
          )
          Spacer(modifier = Modifier.width(6.dp))
        }

        // Theme Toggle Button
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
            .clickable { onToggleTheme() }
            .testTag("theme_toggle_button"),
          contentAlignment = Alignment.Center
        ) {
          AnimatedContent(
            targetState = currentTheme,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "theme_icon"
          ) { theme ->
            val isDark = when (theme) {
              ThemeMode.DARK -> true
              ThemeMode.LIGHT -> false
              ThemeMode.SYSTEM -> colors.isDark
            }
            if (isDark) {
              Icon(
                imageVector = Icons.Default.LightMode,
                contentDescription = "Přepnout na světlý režim",
                tint = colors.alertAccent,
                modifier = Modifier.size(18.dp)
              )
            } else {
              Icon(
                imageVector = Icons.Default.DarkMode,
                contentDescription = "Přepnout na tmavý režim",
                tint = colors.primaryAccent,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // MCP Server Terminal Button
        Box(
          modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant)
            .border(1.dp, colors.primaryAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .clickable { onOpenMcpTerminal() }
            .testTag("open_mcp_terminal_button"),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Cable,
            contentDescription = "MCP Server Terminal",
            tint = colors.primaryAccent,
            modifier = Modifier.size(18.dp)
          )
        }
      }
    }
  }
}
