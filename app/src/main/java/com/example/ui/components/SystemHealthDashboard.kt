package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.HardwareDiagnosticReport
import com.example.ui.theme.LocalScrapMasterColors
import com.example.ui.theme.TechMonoFamily

@Composable
fun SystemHealthDashboard(
  report: HardwareDiagnosticReport,
  isBenchmarking: Boolean,
  onRunBenchmark: () -> Unit,
  onPurgeBuffers: () -> Unit,
  modifier: Modifier = Modifier,
  initiallyExpanded: Boolean = true
) {
  val colors = LocalScrapMasterColors.current
  val health = report.systemHealth
  var isExpanded by remember { mutableStateOf(initiallyExpanded) }

  // Pulsing live indicator
  val infiniteTransition = rememberInfiniteTransition(label = "health_pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1200, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "health_alpha"
  )

  val healthStateColor by animateColorAsState(
    targetValue = when {
      health.isThermalThrottling || health.isMemoryCritical -> colors.alertAccent
      health.memoryPressurePercent > 50 || health.workerLoadPercent > 70 -> Color(0xFFF59E0B)
      else -> colors.secondaryAccent
    },
    label = "health_color"
  )

  CyberCard(
    modifier = modifier.testTag("system_health_dashboard"),
    borderColor = healthStateColor.copy(alpha = 0.5f)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Header Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { isExpanded = !isExpanded },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(9.dp)
              .clip(CircleShape)
              .background(healthStateColor.copy(alpha = pulseAlpha))
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "SYSTEM HEALTH & HW VÝKON",
              fontFamily = TechMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = colors.textPrimary,
              letterSpacing = 0.5.sp
            )
            Text(
              text = "Monitor WebGPU VRAM • Teploty • Web Worker zátěže",
              fontFamily = TechMonoFamily,
              fontSize = 10.sp,
              color = colors.textMuted
            )
          }
        }

        IndustrialBadge(
          text = if (health.isThermalThrottling) "THROTTLED" else if (isBenchmarking) "TESTING..." else "ONLINE",
          color = healthStateColor
        )
      }

      AnimatedVisibility(visible = isExpanded) {
        Column(modifier = Modifier.padding(top = 12.dp)) {
          // Status Box for Continuous Processing
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(healthStateColor.copy(alpha = 0.12f))
              .border(0.8.dp, healthStateColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = if (health.isThermalThrottling) Icons.Default.Warning else Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = healthStateColor,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "KONTINUÁLNÍ E-WASTE:",
                  fontFamily = TechMonoFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  color = colors.textPrimary
                )
              }
              Text(
                text = health.continuousProcessingState,
                fontFamily = TechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = healthStateColor
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 1. WebGPU Memory Pressure
          MetricRow(
            icon = Icons.Default.Memory,
            title = "WebGPU VRAM Tlak",
            valueText = "${health.webGpuAllocatedMb} MB / ${health.webGpuBudgetMb} MB (${health.memoryPressurePercent}%)",
            statusLabel = health.memoryPressureStatus,
            statusColor = if (health.memoryPressurePercent > 80) colors.alertAccent else colors.primaryAccent,
            progress = (health.memoryPressurePercent / 100f).coerceIn(0f, 1f)
          )

          Spacer(modifier = Modifier.height(10.dp))

          // 2. Thermal Status API
          MetricRow(
            icon = Icons.Default.Speed,
            title = "Termální Stav & Headroom",
            valueText = "${health.thermalStatusLabel} • Rezerva ${health.thermalHeadroomPercent}%",
            statusLabel = if (health.isThermalThrottling) "PŘEHŘÁTO" else "BEZPEČNÉ",
            statusColor = if (health.isThermalThrottling) colors.alertAccent else colors.secondaryAccent,
            progress = (health.thermalHeadroomPercent / 100f).coerceIn(0f, 1f)
          )

          Spacer(modifier = Modifier.height(10.dp))

          // 3. Web Worker Load
          MetricRow(
            icon = Icons.Default.Bolt,
            title = "Inference Worker Zátěž",
            valueText = "${health.activeWorkersCount} vlákna • ${health.workerLoadPercent}% zatížení",
            statusLabel = if (health.workerLoadPercent > 80) "VYSOKÁ" else "OPTIMÁLNÍ",
            statusColor = if (health.workerLoadPercent > 80) Color(0xFFF59E0B) else colors.primaryAccent,
            progress = (health.workerLoadPercent / 100f).coerceIn(0f, 1f)
          )

          Spacer(modifier = Modifier.height(12.dp))

          // Interactive Controls (Stress Benchmark & Purge VRAM Buffer)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            OutlinedCyberButton(
              text = if (isBenchmarking) "Testuji..." else "Zátěžový test",
              onClick = onRunBenchmark,
              icon = Icons.Default.Speed,
              enabled = !isBenchmarking,
              modifier = Modifier.weight(1f),
              testTag = "run_health_stress_benchmark_btn"
            )

            OutlinedCyberButton(
              text = "Uvolnit VRAM",
              onClick = onPurgeBuffers,
              icon = Icons.Default.Refresh,
              modifier = Modifier.weight(1f),
              testTag = "purge_vram_buffers_btn"
            )
          }
        }
      }
    }
  }
}

@Composable
private fun MetricRow(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  valueText: String,
  statusLabel: String,
  statusColor: Color,
  progress: Float
) {
  val colors = LocalScrapMasterColors.current
  Column {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = colors.textMuted,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = title,
          fontFamily = TechMonoFamily,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          color = colors.textPrimary
        )
      }

      Text(
        text = statusLabel,
        fontFamily = TechMonoFamily,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = statusColor
      )
    }

    Spacer(modifier = Modifier.height(3.dp))

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = valueText,
        fontFamily = TechMonoFamily,
        fontSize = 10.sp,
        color = colors.textMuted
      )
    }

    Spacer(modifier = Modifier.height(4.dp))

    LinearProgressIndicator(
      progress = { progress },
      modifier = Modifier
        .fillMaxWidth()
        .height(4.dp)
        .clip(RoundedCornerShape(2.dp)),
      color = statusColor,
      trackColor = colors.surfaceVariant
    )
  }
}
