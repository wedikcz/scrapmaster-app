package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CyberCard
import com.example.ui.components.IndustrialBadge
import com.example.ui.components.NeonButton
import com.example.ui.components.SystemHealthDashboard
import com.example.ui.components.WorkshopTab
import com.example.ui.theme.LocalScrapMasterColors
import com.example.ui.theme.TechMonoFamily
import com.example.viewmodel.WorkshopViewModel

@Composable
fun DashboardScreen(
  viewModel: WorkshopViewModel,
  onNavigateToTab: (WorkshopTab) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalScrapMasterColors.current

  val items by viewModel.items.collectAsStateWithLifecycle()
  val lowStock by viewModel.lowStockItems.collectAsStateWithLifecycle()
  val projects by viewModel.projects.collectAsStateWithLifecycle()
  val pinouts by viewModel.pinouts.collectAsStateWithLifecycle()
  val scans by viewModel.scans.collectAsStateWithLifecycle()
  val diagnosticReport by viewModel.diagnosticReport.collectAsStateWithLifecycle()
  val isBenchmarking by viewModel.isBenchmarking.collectAsStateWithLifecycle()

  val activeProjectsCount = projects.count { it.status == "IN_PROGRESS" }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Workshop Banner
    item {
      Spacer(modifier = Modifier.height(4.dp))
      CyberCard(
        borderColor = colors.primaryAccent.copy(alpha = 0.5f)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "DÍLNA REVERZNÍHO INŽENÝRSTVÍ",
                fontFamily = TechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = colors.textPrimary,
                letterSpacing = 0.5.sp
              )
              Text(
                text = "E-Waste záchrana • Recyklace křemíku • RAG asistent",
                fontFamily = TechMonoFamily,
                fontSize = 11.sp,
                color = colors.textMuted
              )
            }
            IndustrialBadge(
              text = "PROD v2.0",
              color = colors.primaryAccent
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          NeonButton(
            text = "SPUSTIT AI SKENER SOUČÁSTEK",
            onClick = { onNavigateToTab(WorkshopTab.SCANNER) },
            icon = Icons.Default.DocumentScanner,
            modifier = Modifier.fillMaxWidth(),
            testTag = "dashboard_launch_scanner_btn"
          )
        }
      }
    }

    // Real-Time System Health Dashboard
    item {
      SystemHealthDashboard(
        report = diagnosticReport,
        isBenchmarking = isBenchmarking,
        onRunBenchmark = { viewModel.runStressBenchmark() },
        onPurgeBuffers = { viewModel.purgeMemoryBuffers() }
      )
    }

    // Telemetry Stat Cards
    item {
      Column {
        Text(
          text = "TELEMETRIE DÍLNY",
          fontFamily = TechMonoFamily,
          fontSize = 11.sp,
          color = colors.textSecondary,
          fontWeight = FontWeight.Bold,
          letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          StatCard(
            title = "INVENTÁŘ",
            value = "${items.size}",
            color = colors.primaryAccent,
            modifier = Modifier.weight(1f)
          ) { onNavigateToTab(WorkshopTab.INVENTORY) }

          StatCard(
            title = "NÍZKÝ STAV",
            value = "${lowStock.size}",
            color = if (lowStock.isNotEmpty()) colors.alertAccent else colors.secondaryAccent,
            modifier = Modifier.weight(1f)
          ) { onNavigateToTab(WorkshopTab.INVENTORY) }

          StatCard(
            title = "PROJEKTY",
            value = "$activeProjectsCount / ${projects.size}",
            color = colors.secondaryAccent,
            modifier = Modifier.weight(1f)
          ) { onNavigateToTab(WorkshopTab.PROJECTS) }

          StatCard(
            title = "PINOUTY",
            value = "${pinouts.size}",
            color = Color(0xFFA855F7),
            modifier = Modifier.weight(1f)
          ) { onNavigateToTab(WorkshopTab.PINOUTS) }
        }
      }
    }

    // Low Stock Alert Section (If any)
    if (lowStock.isNotEmpty()) {
      item {
        CyberCard(
          borderColor = colors.alertAccent.copy(alpha = 0.6f)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Warning,
                  contentDescription = null,
                  tint = colors.alertAccent,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "POZOR: NÍZKÝ STAV SOUČÁSTEK (${lowStock.size})",
                  fontFamily = TechMonoFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = colors.alertAccent
                )
              }

              Text(
                text = "Zobrazit vše",
                fontFamily = TechMonoFamily,
                fontSize = 11.sp,
                color = colors.primaryAccent,
                modifier = Modifier.clickable { onNavigateToTab(WorkshopTab.INVENTORY) }
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              lowStock.take(3).forEach { item ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Column {
                    Text(
                      text = item.name,
                      fontFamily = TechMonoFamily,
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp,
                      color = colors.textPrimary
                    )
                    Text(
                      text = "${item.location} • Min: ${item.minStock} ks",
                      fontFamily = TechMonoFamily,
                      fontSize = 10.sp,
                      color = colors.textMuted
                    )
                  }

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                      text = "${item.quantity} ks",
                      fontFamily = TechMonoFamily,
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp,
                      color = colors.alertAccent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                      modifier = Modifier
                        .size(26.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(colors.secondaryAccent.copy(alpha = 0.2f))
                        .border(1.dp, colors.secondaryAccent, RoundedCornerShape(6.dp))
                        .clickable { viewModel.adjustItemQuantity(item.id, +1) },
                      contentAlignment = Alignment.Center
                    ) {
                      Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Přidat +1",
                        tint = colors.secondaryAccent,
                        modifier = Modifier.size(16.dp)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }
    }

    // Quick Workshop Shortcuts
    item {
      Column {
        Text(
          text = "RYCHLÉ MODULY",
          fontFamily = TechMonoFamily,
          fontSize = 11.sp,
          color = colors.textSecondary,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          QuickModuleCard(
            title = "Katalog Pinoutů",
            desc = "Čipy NE555, LM7805, ESP32...",
            icon = Icons.Default.Hub,
            color = Color(0xFFA855F7),
            modifier = Modifier.weight(1f)
          ) { onNavigateToTab(WorkshopTab.PINOUTS) }

          QuickModuleCard(
            title = "Datasheet RAG AI",
            desc = "Offline asistent s citacemi",
            icon = Icons.Default.AutoAwesome,
            color = colors.primaryAccent,
            modifier = Modifier.weight(1f)
          ) { onNavigateToTab(WorkshopTab.ASSISTANT) }
        }
      }
    }

    // E-Waste Salvage Protocol Tip
    item {
      CyberCard {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "PROTOKOL BEZPEČNOSTI PŘI DEMONTÁŽI E-WASTE",
            fontFamily = TechMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = colors.secondaryAccent
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "1. Vybijte vysokonapěťové kondenzátory ve spínaných zdrojích přes 1kΩ odpor.\n" +
                "2. Horkovzdušné odpájení QFP/SOP čipů provádějte na 330°C - 350°C s kapalinovým tavidlem.\n" +
                "3. Po odpájení očistěte piny izopropylalkoholem (IPA) pro spolehlivou optickou AI detekci.",
            fontFamily = TechMonoFamily,
            fontSize = 10.5.sp,
            color = colors.textSecondary,
            lineHeight = 16.sp
          )
        }
      }
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

@Composable
private fun StatCard(
  title: String,
  value: String,
  color: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val colors = LocalScrapMasterColors.current
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(colors.surfaceVariant)
      .border(0.8.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
      .clickable { onClick() }
      .padding(8.dp)
  ) {
    Column {
      Text(
        text = title,
        fontFamily = TechMonoFamily,
        fontSize = 8.5.sp,
        color = colors.textMuted,
        fontWeight = FontWeight.Bold
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = value,
        fontFamily = TechMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = color
      )
    }
  }
}

@Composable
private fun QuickModuleCard(
  title: String,
  desc: String,
  icon: ImageVector,
  color: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val colors = LocalScrapMasterColors.current
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(10.dp))
      .background(colors.surfaceVariant)
      .border(0.8.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
      .clickable { onClick() }
      .padding(12.dp)
  ) {
    Column {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = Modifier.size(20.dp)
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = title,
        fontFamily = TechMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = colors.textPrimary
      )
      Text(
        text = desc,
        fontFamily = TechMonoFamily,
        fontSize = 10.sp,
        color = colors.textMuted
      )
    }
  }
}
