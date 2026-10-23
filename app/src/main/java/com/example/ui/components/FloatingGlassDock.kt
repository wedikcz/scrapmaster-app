package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalScrapMasterColors
import com.example.ui.theme.TechMonoFamily

enum class WorkshopTab(val label: String, val icon: ImageVector) {
  DASHBOARD("Přehled", Icons.Default.Dashboard),
  SCANNER("Skener", Icons.Default.DocumentScanner),
  INVENTORY("Inventář", Icons.AutoMirrored.Filled.FormatListBulleted),
  PINOUTS("Pinouty", Icons.Default.Hub),
  PROJECTS("Kanban", Icons.AutoMirrored.Filled.Assignment),
  ASSISTANT("RAG AI", Icons.Default.AutoAwesome)
}

@Composable
fun FloatingGlassDock(
  currentTab: WorkshopTab,
  onSelectTab: (WorkshopTab) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalScrapMasterColors.current

  Box(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding()
      .padding(horizontal = 14.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    // Glassmorphic Dock Container
    Box(
      modifier = Modifier
        .shadow(
          elevation = 16.dp,
          shape = RoundedCornerShape(22.dp),
          ambientColor = colors.primaryAccent.copy(alpha = 0.25f),
          spotColor = colors.primaryAccent.copy(alpha = 0.35f)
        )
        .clip(RoundedCornerShape(22.dp))
        .background(
          brush = Brush.verticalGradient(
            colors = listOf(
              colors.surface.copy(alpha = if (colors.isDark) 0.92f else 0.96f),
              colors.surfaceVariant.copy(alpha = if (colors.isDark) 0.96f else 0.98f)
            )
          )
        )
        .border(
          width = 1.dp,
          brush = Brush.horizontalGradient(
            colors = listOf(
              colors.primaryAccent.copy(alpha = 0.4f),
              colors.cardBorder,
              colors.secondaryAccent.copy(alpha = 0.4f)
            )
          ),
          shape = RoundedCornerShape(22.dp)
        )
        .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        WorkshopTab.entries.forEach { tab ->
          val isSelected = tab == currentTab

          val tabBackground by animateColorAsState(
            targetValue = if (isSelected) {
              colors.primaryAccent.copy(alpha = if (colors.isDark) 0.16f else 0.12f)
            } else {
              Color.Transparent
            },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
            label = "tab_bg"
          )

          val iconColor by animateColorAsState(
            targetValue = if (isSelected) colors.primaryAccent else colors.textMuted,
            label = "icon_color"
          )

          Column(
            modifier = Modifier
              .clip(RoundedCornerShape(14.dp))
              .background(tabBackground)
              .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
              ) { onSelectTab(tab) }
              .padding(horizontal = 8.dp, vertical = 6.dp)
              .testTag("dock_tab_${tab.name.lowercase()}"),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(
              imageVector = tab.icon,
              contentDescription = tab.label,
              tint = iconColor,
              modifier = Modifier.size(20.dp)
            )

            Text(
              text = tab.label,
              fontFamily = TechMonoFamily,
              fontSize = 9.5.sp,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              color = if (isSelected) colors.primaryAccent else colors.textMuted
            )

            // Neon glowing indicator dot
            Box(
              modifier = Modifier
                .padding(top = 2.dp)
                .size(width = if (isSelected) 12.dp else 0.dp, height = 2.5.dp)
                .clip(CircleShape)
                .background(if (isSelected) colors.primaryAccent else Color.Transparent)
            )
          }
        }
      }
    }
  }
}
