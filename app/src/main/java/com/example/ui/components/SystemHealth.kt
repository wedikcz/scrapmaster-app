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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
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
import com.example.ai.GgufModelOption
import com.example.ai.HardwareDiagnosticReport
import com.example.ai.InferenceEngineMode
import com.example.ui.theme.LocalScrapMasterColors
import com.example.ui.theme.TechMonoFamily

/**
 * SystemHealth Diagnostic Component for the AI Scanner.
 * Monitors WebGPU acceleration support, GGUF on-device quantized models,
 * thermal state, and enables smart switching between all models (GGUF, WebGPU, Cloud Flash/Pro, Safe).
 */
@Composable
fun SystemHealth(
  report: HardwareDiagnosticReport,
  isBenchmarking: Boolean,
  onRunBenchmark: () -> Unit,
  onPurgeBuffers: () -> Unit,
  onSelectInferenceMode: (InferenceEngineMode) -> Unit,
  availableGgufModels: List<GgufModelOption> = emptyList(),
  onSelectGgufModel: (String) -> Unit = {},
  onSmartAutoSelect: () -> Unit = {},
  modifier: Modifier = Modifier,
  initiallyExpanded: Boolean = false
) {
  val colors = LocalScrapMasterColors.current
  val health = report.systemHealth
  val gguf = report.ggufTelemetry
  var isExpanded by remember { mutableStateOf(initiallyExpanded) }

  val isGguf = report.mode == InferenceEngineMode.LOCAL_GGUF_ONDEVICE
  val isWebGpu = report.mode == InferenceEngineMode.LOCAL_WEBGPU_ACCELERATED
  val isCloudFlash = report.mode == InferenceEngineMode.CLOUD_GATEWAY_FALLBACK
  val isCloudPro = report.mode == InferenceEngineMode.CLOUD_GEMINI_PRO
  val isSafe = report.mode == InferenceEngineMode.SAFE_DETERMINISTIC_PASS

  val modeColor by animateColorAsState(
    targetValue = when {
      health.isThermalThrottling || health.isMemoryCritical -> colors.alertAccent
      isGguf -> Color(0xFF8B5CF6) // Purple for local GGUF NPU
      isWebGpu -> colors.secondaryAccent // Cyan/Green for WebGPU
      isCloudFlash -> Color(0xFFF59E0B) // Amber for Flash
      isCloudPro -> Color(0xFF3B82F6) // Blue for Gemini Pro
      else -> colors.primaryAccent
    },
    label = "system_health_mode_color"
  )

  val infiniteTransition = rememberInfiniteTransition(label = "system_health_pulse")
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1100, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "system_health_pulse_alpha"
  )

  CyberCard(
    modifier = modifier.testTag("system_health_component"),
    borderColor = modeColor.copy(alpha = 0.65f)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {

      // --- 1. Header Bar: Status Beacon & Active Inference Mode ---
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { isExpanded = !isExpanded }
          .testTag("system_health_header_toggle"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(modeColor.copy(alpha = pulseAlpha))
              .border(1.dp, modeColor, CircleShape)
          )
          Spacer(modifier = Modifier.width(8.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "SYSTEM HEALTH:",
                fontFamily = TechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = colors.textPrimary,
                letterSpacing = 0.5.sp
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = when {
                  isGguf -> "GGUF NPU EDGE LLM"
                  isWebGpu -> "WEBGPU ACCELERATED"
                  isCloudFlash -> "GEMINI FLASH CLOUD"
                  isCloudPro -> "GEMINI PRO REASONER"
                  else -> "SAFE DETERMINISTIC"
                },
                fontFamily = TechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp,
                color = modeColor
              )
            }
            Text(
              text = "${report.measuredLatencyMs}ms latence • ${report.activeComputeDevice}",
              fontFamily = TechMonoFamily,
              fontSize = 9.5.sp,
              color = colors.textMuted,
              maxLines = 1
            )
          }
        }

        // Chevron Toggle
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surfaceVariant)
            .border(0.8.dp, colors.cardBorder, RoundedCornerShape(6.dp)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (isExpanded) "Sbalit" else "Rozbalit",
            tint = colors.textPrimary,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      // --- 2. Smart Multi-Model Switcher Strip ---
      Spacer(modifier = Modifier.height(10.dp))
      LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
      ) {
        // GGUF 4-bit Edge
        item {
          ModelChip(
            label = "GGUF Q4 (NPU)",
            isSelected = isGguf,
            accentColor = Color(0xFF8B5CF6),
            icon = Icons.Default.Psychology,
            onClick = { onSelectInferenceMode(InferenceEngineMode.LOCAL_GGUF_ONDEVICE) },
            testTag = "model_select_gguf"
          )
        }

        // WebGPU Local Shader
        item {
          ModelChip(
            label = "WebGPU (GPU)",
            isSelected = isWebGpu,
            accentColor = colors.secondaryAccent,
            icon = Icons.Default.Bolt,
            onClick = { onSelectInferenceMode(InferenceEngineMode.LOCAL_WEBGPU_ACCELERATED) },
            testTag = "model_select_webgpu"
          )
        }

        // Cloud Gemini Flash
        item {
          ModelChip(
            label = "Gemini Flash",
            isSelected = isCloudFlash,
            accentColor = Color(0xFFF59E0B),
            icon = Icons.Default.CloudQueue,
            onClick = { onSelectInferenceMode(InferenceEngineMode.CLOUD_GATEWAY_FALLBACK) },
            testTag = "model_select_flash"
          )
        }

        // Cloud Gemini Pro
        item {
          ModelChip(
            label = "Gemini Pro",
            isSelected = isCloudPro,
            accentColor = Color(0xFF3B82F6),
            icon = Icons.Default.AutoAwesome,
            onClick = { onSelectInferenceMode(InferenceEngineMode.CLOUD_GEMINI_PRO) },
            testTag = "model_select_pro"
          )
        }

        // Safe Rules Engine
        item {
          ModelChip(
            label = "Safe Rules",
            isSelected = isSafe,
            accentColor = colors.primaryAccent,
            icon = Icons.Default.Security,
            onClick = { onSelectInferenceMode(InferenceEngineMode.SAFE_DETERMINISTIC_PASS) },
            testTag = "model_select_safe"
          )
        }
      }

      // Smart Auto-Select Button
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colors.primaryAccent.copy(alpha = 0.12f))
            .border(0.8.dp, colors.primaryAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .clickable { onSmartAutoSelect() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("smart_auto_select_btn")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.AutoAwesome,
              contentDescription = null,
              tint = colors.primaryAccent,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "CHYTRÝ AUTO VÝBĚR DLE ZÁTĚŽE A TEPLOTY",
              fontFamily = TechMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 9.5.sp,
              color = colors.primaryAccent
            )
          }
        }

        if (health.isThermalThrottling) {
          IndustrialBadge(text = "THROTTLING DETEKOVÁN", color = colors.alertAccent)
        }
      }

      // --- 3. GGUF Model Selector (Visible when GGUF mode is active) ---
      if (isGguf && availableGgufModels.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF8B5CF6).copy(alpha = 0.1f))
            .border(0.8.dp, Color(0xFF8B5CF6).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Column {
            Text(
              text = "GGUF ON-DEVICE KVANTOVANÉ MODELY (EDGE NPU):",
              fontFamily = TechMonoFamily,
              fontSize = 9.5.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF8B5CF6)
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              items(availableGgufModels) { model ->
                val isCurrent = model.id == gguf.modelId
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isCurrent) Color(0xFF8B5CF6).copy(alpha = 0.3f) else colors.surfaceVariant)
                    .border(
                      width = if (isCurrent) 1.2.dp else 0.8.dp,
                      color = if (isCurrent) Color(0xFF8B5CF6) else colors.cardBorder,
                      shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelectGgufModel(model.id) }
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .testTag("gguf_model_${model.id}")
                ) {
                  Column {
                    Text(
                      text = model.displayName,
                      fontFamily = TechMonoFamily,
                      fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                      fontSize = 10.sp,
                      color = if (isCurrent) Color(0xFF8B5CF6) else colors.textPrimary
                    )
                    Text(
                      text = "${model.ramRequiredMb}MB RAM • ${model.speedTokensPerSec} t/s",
                      fontFamily = TechMonoFamily,
                      fontSize = 8.5.sp,
                      color = colors.textMuted
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Kvantování: ${gguf.quantization} • Kontext: ${gguf.contextWindowTokens} tokenů • Vrstvy: ${gguf.offloadedLayers}",
              fontFamily = TechMonoFamily,
              fontSize = 9.sp,
              color = colors.textMuted
            )
          }
        }
      }

      // --- 4. Expandable Detailed Telemetry & Gauges ---
      AnimatedVisibility(visible = isExpanded) {
        Column(modifier = Modifier.padding(top = 12.dp)) {

          // WebGPU Memory Pressure Bar
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Memory,
                  contentDescription = null,
                  tint = colors.primaryAccent,
                  modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "WebGPU Tlak Paměti (VRAM)",
                  fontFamily = TechMonoFamily,
                  fontSize = 11.sp,
                  color = colors.textPrimary
                )
              }
              Text(
                text = "${health.memoryPressurePercent}% (${health.webGpuAllocatedMb}MB / ${health.webGpuBudgetMb}MB)",
                fontFamily = TechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = when {
                  health.memoryPressurePercent > 80 -> colors.alertAccent
                  health.memoryPressurePercent > 50 -> Color(0xFFF59E0B)
                  else -> colors.secondaryAccent
                }
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
              progress = { (health.memoryPressurePercent / 100f).coerceIn(0f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = when {
                health.memoryPressurePercent > 80 -> colors.alertAccent
                health.memoryPressurePercent > 50 -> Color(0xFFF59E0B)
                else -> colors.secondaryAccent
              },
              trackColor = colors.surfaceVariant
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Thermal Status & Worker Load Grid
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.surfaceVariant.copy(alpha = 0.5f))
                .border(0.8.dp, colors.cardBorder, RoundedCornerShape(6.dp))
                .padding(8.dp)
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = null,
                    tint = if (health.isThermalThrottling) colors.alertAccent else colors.secondaryAccent,
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Teplotní Stav",
                    fontFamily = TechMonoFamily,
                    fontSize = 10.sp,
                    color = colors.textMuted
                  )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                  text = health.thermalStatusLabel,
                  fontFamily = TechMonoFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  color = if (health.isThermalThrottling) colors.alertAccent else colors.textPrimary
                )
                Text(
                  text = "Chladicí rezerva: ${health.thermalHeadroomPercent}%",
                  fontFamily = TechMonoFamily,
                  fontSize = 9.sp,
                  color = colors.textMuted
                )
              }
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.surfaceVariant.copy(alpha = 0.5f))
                .border(0.8.dp, colors.cardBorder, RoundedCornerShape(6.dp))
                .padding(8.dp)
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = colors.primaryAccent,
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Worker Vlákna",
                    fontFamily = TechMonoFamily,
                    fontSize = 10.sp,
                    color = colors.textMuted
                  )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                  text = "${health.activeWorkersCount}/${health.totalWorkersCount} Vlákna (${health.workerLoadPercent}% zátěž)",
                  fontFamily = TechMonoFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 10.sp,
                  color = colors.textPrimary
                )
                Text(
                  text = health.continuousProcessingState,
                  fontFamily = TechMonoFamily,
                  fontSize = 9.sp,
                  color = colors.secondaryAccent
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Device & Hardware Stack
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(colors.surfaceVariant.copy(alpha = 0.4f))
              .padding(8.dp)
          ) {
            Column {
              Text(
                text = "AKTIVNÍ COMPUTE PIPELINE:",
                fontFamily = TechMonoFamily,
                fontSize = 9.sp,
                color = colors.textMuted,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = report.activeComputeDevice,
                fontFamily = TechMonoFamily,
                fontSize = 10.sp,
                color = colors.primaryAccent,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "GPU: ${report.gpuRenderer} • ${report.glEsVersion} • Vulkan: ${if (report.hasVulkan) "ANO" else "NE"}",
                fontFamily = TechMonoFamily,
                fontSize = 9.sp,
                color = colors.textMuted
              )
            }
          }

          // Benchmark & Purge Buttons
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.primaryAccent.copy(alpha = 0.15f))
                .border(0.8.dp, colors.primaryAccent.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                .clickable(enabled = !isBenchmarking) { onRunBenchmark() }
                .padding(vertical = 8.dp)
                .testTag("system_health_run_benchmark"),
              contentAlignment = Alignment.Center
            ) {
              if (isBenchmarking) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 1.5.dp,
                    color = colors.primaryAccent
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "MĚŘENÍ...",
                    fontFamily = TechMonoFamily,
                    fontSize = 10.sp,
                    color = colors.primaryAccent,
                    fontWeight = FontWeight.Bold
                  )
                }
              } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = colors.primaryAccent,
                    modifier = Modifier.size(12.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "BENCHMARK HW",
                    fontFamily = TechMonoFamily,
                    fontSize = 10.sp,
                    color = colors.primaryAccent,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(6.dp))
                .background(colors.surfaceVariant)
                .border(0.8.dp, colors.cardBorder, RoundedCornerShape(6.dp))
                .clickable { onPurgeBuffers() }
                .padding(vertical = 8.dp)
                .testTag("system_health_purge_vram"),
              contentAlignment = Alignment.Center
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Refresh,
                  contentDescription = null,
                  tint = colors.textMuted,
                  modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "UVOLNIT VRAM",
                  fontFamily = TechMonoFamily,
                  fontSize = 10.sp,
                  color = colors.textPrimary,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun ModelChip(
  label: String,
  isSelected: Boolean,
  accentColor: Color,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit,
  testTag: String
) {
  val colors = LocalScrapMasterColors.current
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(if (isSelected) accentColor.copy(alpha = 0.22f) else colors.surfaceVariant)
      .border(
        width = if (isSelected) 1.2.dp else 0.8.dp,
        color = if (isSelected) accentColor else colors.cardBorder,
        shape = RoundedCornerShape(6.dp)
      )
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .testTag(testTag),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (isSelected) accentColor else colors.textMuted,
        modifier = Modifier.size(12.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        fontFamily = TechMonoFamily,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        fontSize = 10.sp,
        color = if (isSelected) accentColor else colors.textPrimary
      )
    }
  }
}
