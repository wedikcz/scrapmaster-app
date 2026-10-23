package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ai.InferenceEngineMode
import com.example.ui.theme.LocalScrapMasterColors
import com.example.ui.theme.TechMonoFamily

@Composable
fun WebGpuDiagnosticIndicator(
  report: HardwareDiagnosticReport,
  isBenchmarking: Boolean,
  onRunBenchmark: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalScrapMasterColors.current
  var showDetailsDialog by remember { mutableStateOf(false) }

  val isAccelerated = report.mode == InferenceEngineMode.LOCAL_WEBGPU_ACCELERATED
  val statusColor = if (isAccelerated) colors.secondaryAccent else colors.alertAccent
  val statusIcon = if (isAccelerated) Icons.Default.Bolt else Icons.Default.CloudQueue
  val statusLabel = if (isAccelerated) "WEBGPU / NPU LOCAL" else "CLOUD / SAFE MOCK"

  // Pulsing animation for the status dot
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val alphaPulse by infiniteTransition.animateFloat(
    initialValue = 0.5f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1000, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dot_alpha"
  )

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(statusColor.copy(alpha = 0.14f))
      .border(0.8.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
      .clickable { showDetailsDialog = true }
      .padding(horizontal = 8.dp, vertical = 4.dp)
      .testTag("webgpu_diagnostic_indicator")
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(7.dp)
          .clip(CircleShape)
          .background(statusColor.copy(alpha = alphaPulse))
      )
      Spacer(modifier = Modifier.width(5.dp))
      Icon(
        imageVector = statusIcon,
        contentDescription = null,
        tint = statusColor,
        modifier = Modifier.size(13.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = statusLabel,
        fontFamily = TechMonoFamily,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = statusColor,
        letterSpacing = 0.5.sp
      )
    }
  }

  if (showDetailsDialog) {
    HackOpsMoETerminalDialog(
      report = report,
      isBenchmarking = isBenchmarking,
      onRunBenchmark = onRunBenchmark,
      onDismiss = { showDetailsDialog = false }
    )
  }
}

@Composable
fun HackOpsMoETerminalDialog(
  report: HardwareDiagnosticReport,
  isBenchmarking: Boolean,
  onRunBenchmark: () -> Unit,
  onDismiss: () -> Unit
) {
  val colors = LocalScrapMasterColors.current
  val isAccelerated = report.mode == InferenceEngineMode.LOCAL_WEBGPU_ACCELERATED

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Memory,
            contentDescription = null,
            tint = colors.primaryAccent,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "HACKOPS HARDWARE AUDIT",
            fontFamily = TechMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = colors.primaryAccent
          )
        }
        IndustrialBadge(
          text = if (isAccelerated) "WEBGPU ACTIVE" else "FALLBACK SAFE",
          color = if (isAccelerated) colors.secondaryAccent else colors.alertAccent
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Status overview card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "INFERENCE PIPELINE:",
                fontFamily = TechMonoFamily,
                fontSize = 10.sp,
                color = colors.textMuted
              )
              Text(
                text = if (isAccelerated) "Lokální WebGPU / Tensor" else "Cloud / Bezpečný Fallback",
                fontFamily = TechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (isAccelerated) colors.secondaryAccent else colors.alertAccent
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "• GPU Compute: ${report.glEsVersion} (Vulkan: ${if (report.hasVulkan) "ANO" else "NE"})",
              fontFamily = TechMonoFamily,
              fontSize = 10.sp,
              color = colors.textSecondary
            )
            Text(
              text = "• Paměť: ${report.availableMemoryMb} MB volných z ${report.totalMemoryMb} MB",
              fontFamily = TechMonoFamily,
              fontSize = 10.sp,
              color = colors.textSecondary
            )
            Text(
              text = "• Latence: ${report.measuredLatencyMs} ms (${report.activeComputeDevice})",
              fontFamily = TechMonoFamily,
              fontSize = 10.sp,
              color = colors.primaryAccent
            )
          }
        }

        // MoE Self-Reflection Matrix
        Text(
          text = "MoE (MIXTURE OF EXPERTS) SEBEREFLEXE:",
          fontFamily = TechMonoFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp,
          color = colors.secondaryAccent
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          report.moeExperts.forEach { expert ->
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(colors.surface)
                .border(0.8.dp, colors.cardBorder, RoundedCornerShape(6.dp))
                .padding(8.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = expert.name,
                    fontFamily = TechMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.5.sp,
                    color = colors.textPrimary
                  )
                  Text(
                    text = "${expert.role} • ${expert.latencyMs}ms",
                    fontFamily = TechMonoFamily,
                    fontSize = 9.5.sp,
                    color = colors.textMuted
                  )
                }
                IndustrialBadge(
                  text = "${(expert.confidenceScore * 100).toInt()}%",
                  color = colors.secondaryAccent
                )
              }
            }
          }
        }

        // Security boundary
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(top = 2.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = colors.secondaryAccent,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Hardened: ${report.securityBoundaryStatus} • AST Verified",
            fontFamily = TechMonoFamily,
            fontSize = 9.5.sp,
            color = colors.textMuted
          )
        }

        if (isBenchmarking) {
          Column {
            Text(
              text = "Běží zátěžový test tensor compute pipeline...",
              fontFamily = TechMonoFamily,
              fontSize = 10.sp,
              color = colors.primaryAccent
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
              modifier = Modifier.fillMaxWidth(),
              color = colors.primaryAccent
            )
          }
        }
      }
    },
    confirmButton = {
      NeonButton(
        text = if (isBenchmarking) "MĚŘÍM..." else "SPUSTIT BENCHMARK",
        onClick = onRunBenchmark,
        icon = Icons.Default.Speed,
        enabled = !isBenchmarking,
        color = colors.primaryAccent,
        testTag = "run_benchmark_btn"
      )
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("ZAVŘÍT", fontFamily = TechMonoFamily, color = colors.textMuted)
      }
    },
    containerColor = colors.surface,
    shape = RoundedCornerShape(16.dp)
  )
}
