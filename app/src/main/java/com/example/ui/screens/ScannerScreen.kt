package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.ComponentTaggingEngine
import com.example.ai.HardwareDiagnosticReport
import com.example.ai.SampleBoard
import com.example.ai.SampleBoards
import androidx.compose.material.icons.filled.Videocam
import com.example.ui.components.CameraXLiveScannerView
import com.example.ui.components.CyberCard
import com.example.ui.components.IndustrialBadge
import com.example.ui.components.NeonButton
import com.example.ui.components.OutlinedCyberButton
import com.example.ui.components.SystemHealth
import com.example.ui.components.SystemHealthDashboard
import com.example.ui.components.WebGpuDiagnosticIndicator
import com.example.ui.components.WorkshopTab
import com.example.ui.theme.LocalScrapMasterColors
import com.example.ui.theme.TechMonoFamily
import com.example.viewmodel.BatchProcessingStatus
import com.example.viewmodel.ScannerStatus
import com.example.viewmodel.WorkshopViewModel

enum class ScannerViewMode {
  SINGLE,
  BATCH_QUEUE,
  CAMERAX_LIVE
}

@Composable
fun ScannerScreen(
  viewModel: WorkshopViewModel,
  onNavigateToTab: (WorkshopTab) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalScrapMasterColors.current
  val context = LocalContext.current

  var viewMode by remember { mutableStateOf(ScannerViewMode.SINGLE) }

  val selectedBoard by viewModel.selectedSampleBoard.collectAsStateWithLifecycle()
  val capturedBitmap by viewModel.capturedBitmap.collectAsStateWithLifecycle()
  val status by viewModel.scannerStatus.collectAsStateWithLifecycle()
  val lastResult by viewModel.lastScanResult.collectAsStateWithLifecycle()
  val logs by viewModel.scannerLogs.collectAsStateWithLifecycle()
  val diagReport by viewModel.diagnosticReport.collectAsStateWithLifecycle()
  val isBenchmarking by viewModel.isBenchmarking.collectAsStateWithLifecycle()

  // Batch State
  val batchQueue by viewModel.batchQueue.collectAsStateWithLifecycle()
  val batchResults by viewModel.batchResults.collectAsStateWithLifecycle()
  val batchStatus by viewModel.batchStatus.collectAsStateWithLifecycle()
  val batchProgress by viewModel.batchProgress.collectAsStateWithLifecycle()
  val currentBatchIdx by viewModel.currentBatchIndex.collectAsStateWithLifecycle()

  // Schematic OCR & RAG State
  val lastSchematicResult by viewModel.lastSchematicResult.collectAsStateWithLifecycle()
  val isExtractingSchematic by viewModel.isExtractingSchematic.collectAsStateWithLifecycle()
  val isIngestedToRag by viewModel.isIngestedToRag.collectAsStateWithLifecycle()

  var feedbackMessage by remember { mutableStateOf<String?>(null) }

  // 1. Live Camera Launcher (Single / Batch)
  val cameraLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.TakePicturePreview()
  ) { bitmap: Bitmap? ->
    if (bitmap != null) {
      if (viewMode == ScannerViewMode.BATCH_QUEUE) {
        viewModel.addToBatch(bitmap, null, "Kamera #${batchQueue.size + 1}")
      } else {
        viewModel.setCapturedBitmap(bitmap)
        viewModel.startScanAnalysis()
      }
    }
  }

  // 2. Single Photo Picker Launcher
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickVisualMedia()
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        val bitmap = loadBitmapFromUri(context, uri)
        if (viewMode == ScannerViewMode.BATCH_QUEUE) {
          viewModel.addToBatch(bitmap, null, "Snímek #${batchQueue.size + 1}")
        } else {
          viewModel.setCapturedBitmap(bitmap)
          viewModel.startScanAnalysis()
        }
      } catch (_: Exception) {}
    }
  }

  // 3. Multi-Photo Picker for Batch Mode
  val multiPhotoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
  ) { uris: List<Uri> ->
    uris.forEachIndexed { idx, uri ->
      try {
        val bitmap = loadBitmapFromUri(context, uri)
        viewModel.addToBatch(bitmap, null, "Dávka #${batchQueue.size + idx + 1}")
      } catch (_: Exception) {}
    }
  }

  // Animated laser scan line for live HUD
  val infiniteTransition = rememberInfiniteTransition(label = "hud_scan")
  val scanLineProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(1800, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "laser_y"
  )

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Mode Switcher Header
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "AI SKENER SOUČÁSTEK",
            fontFamily = TechMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = colors.textPrimary,
            letterSpacing = 0.5.sp
          )
          Text(
            text = if (viewMode == ScannerViewMode.SINGLE) "Jednotlivý sken s reflexí" else "Dávková fronta pro rychlé třídění",
            fontFamily = TechMonoFamily,
            fontSize = 11.sp,
            color = colors.textMuted
          )
        }

        // Mode Switch Buttons
        Row(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
            .padding(2.dp)
        ) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (viewMode == ScannerViewMode.SINGLE) colors.primaryAccent.copy(alpha = 0.25f) else Color.Transparent)
              .clickable { viewMode = ScannerViewMode.SINGLE }
              .padding(horizontal = 8.dp, vertical = 5.dp)
          ) {
            Text(
              text = "SINGLE",
              fontFamily = TechMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              color = if (viewMode == ScannerViewMode.SINGLE) colors.primaryAccent else colors.textMuted
            )
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (viewMode == ScannerViewMode.BATCH_QUEUE) colors.secondaryAccent.copy(alpha = 0.25f) else Color.Transparent)
              .clickable { viewMode = ScannerViewMode.BATCH_QUEUE }
              .padding(horizontal = 8.dp, vertical = 5.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "BATCH",
                fontFamily = TechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (viewMode == ScannerViewMode.BATCH_QUEUE) colors.secondaryAccent else colors.textMuted
              )
              if (batchQueue.isNotEmpty()) {
                Spacer(modifier = Modifier.width(3.dp))
                Box(
                  modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(colors.secondaryAccent)
                )
              }
            }
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(if (viewMode == ScannerViewMode.CAMERAX_LIVE) colors.primaryAccent.copy(alpha = 0.25f) else Color.Transparent)
              .clickable { viewMode = ScannerViewMode.CAMERAX_LIVE }
              .padding(horizontal = 8.dp, vertical = 5.dp)
              .testTag("camerax_live_mode_tab")
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Videocam,
                contentDescription = null,
                tint = if (viewMode == ScannerViewMode.CAMERAX_LIVE) colors.primaryAccent else colors.textMuted,
                modifier = Modifier.size(11.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "CAMERAX",
                fontFamily = TechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (viewMode == ScannerViewMode.CAMERAX_LIVE) colors.primaryAccent else colors.textMuted
              )
            }
          }
        }
      }
    }

    // Real-Time System Health Component with WebGPU / GGUF / Cloud Multi-Model Smart Switcher
    item {
      SystemHealth(
        report = diagReport,
        isBenchmarking = isBenchmarking,
        onRunBenchmark = { viewModel.runStressBenchmark() },
        onPurgeBuffers = { viewModel.purgeMemoryBuffers() },
        onSelectInferenceMode = { mode -> viewModel.setInferenceMode(mode) },
        availableGgufModels = viewModel.availableGgufModels,
        onSelectGgufModel = { modelId -> viewModel.selectGgufModel(modelId) },
        onSmartAutoSelect = { viewModel.smartAutoSelectBestModel() },
        initiallyExpanded = false
      )
    }

    // --- CAMERAX LIVE SCANNER VIEW ---
    if (viewMode == ScannerViewMode.CAMERAX_LIVE) {
      item {
        CameraXLiveScannerView(
          onFrameCaptured = { bmp ->
            viewModel.setCapturedBitmap(bmp)
            viewModel.performOcrPreprocessingAndIngest(bitmap = bmp, sampleBoard = null)
            viewModel.startScanAnalysis()
            viewMode = ScannerViewMode.SINGLE
            feedbackMessage = "Snímek zachycen z CameraX! Probíhá OCR analýza potisku & AI identifikace..."
          },
          onBatchFrameAdded = { bmp ->
            viewModel.addToBatch(bmp, null, "CameraX #${batchQueue.size + 1}")
            viewModel.performOcrPreprocessingAndIngest(bitmap = bmp, sampleBoard = null)
            feedbackMessage = "+1 snímek přidán z CameraX do dávky (#${batchQueue.size + 1}) s OCR předzpracováním!"
          },
          onClose = {
            viewMode = ScannerViewMode.SINGLE
          }
        )
      }
    }

    // --- BATCH QUEUE MODE VIEW ---
    if (viewMode == ScannerViewMode.BATCH_QUEUE) {
      item {
        CyberCard(borderColor = colors.secondaryAccent.copy(alpha = 0.6f)) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.DynamicFeed,
                  contentDescription = null,
                  tint = colors.secondaryAccent,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "DÁVKOVÁ FRONTA E-WASTE (${batchQueue.size})",
                  fontFamily = TechMonoFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = colors.textPrimary
                )
              }

              if (batchQueue.isNotEmpty()) {
                Text(
                  text = "Vyčistit frontu",
                  fontFamily = TechMonoFamily,
                  fontSize = 10.sp,
                  color = colors.alertAccent,
                  modifier = Modifier.clickable { viewModel.clearBatch() }
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rapid Capture Action Buttons
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              NeonButton(
                text = "CameraX Live",
                onClick = { viewMode = ScannerViewMode.CAMERAX_LIVE },
                icon = Icons.Default.Videocam,
                color = colors.primaryAccent,
                modifier = Modifier.weight(1.1f),
                testTag = "batch_camerax_btn"
              )

              OutlinedCyberButton(
                text = "+ Foto",
                onClick = { cameraLauncher.launch(null) },
                icon = Icons.Default.CameraAlt,
                color = colors.secondaryAccent,
                modifier = Modifier.weight(0.9f),
                testTag = "batch_camera_btn"
              )

              OutlinedCyberButton(
                text = "+ Galerie",
                onClick = {
                  multiPhotoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                  )
                },
                icon = Icons.Default.PhotoLibrary,
                color = colors.primaryAccent,
                modifier = Modifier.weight(0.9f),
                testTag = "batch_gallery_btn"
              )
            }
          }
        }
      }

      // Quick add sample boards into queue
      item {
        Column {
          Text(
            text = "RYCHLÉ PŘIDÁNÍ REFERENČNÍCH DESEK DO DÁVKY:",
            fontFamily = TechMonoFamily,
            fontSize = 10.5.sp,
            color = colors.textSecondary,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(6.dp))

          LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(SampleBoards.list) { board ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(colors.surfaceVariant)
                  .border(0.8.dp, colors.cardBorder, RoundedCornerShape(8.dp))
                  .clickable {
                    viewModel.addToBatch(null, board, board.chipHint)
                  }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = colors.secondaryAccent,
                    modifier = Modifier.size(13.dp)
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = board.chipHint,
                    fontFamily = TechMonoFamily,
                    fontSize = 10.sp,
                    color = colors.textPrimary
                  )
                }
              }
            }
          }
        }
      }

      // Queued Items Strip
      if (batchQueue.isNotEmpty()) {
        item {
          Column {
            Text(
              text = "NAČTENÉ POLOŽKY VE FRONTĚ:",
              fontFamily = TechMonoFamily,
              fontSize = 10.5.sp,
              color = colors.textSecondary,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              items(batchQueue, key = { it.id }) { item ->
                Box(
                  modifier = Modifier
                    .width(130.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceVariant)
                    .border(1.dp, colors.secondaryAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
                ) {
                  Column {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      IndustrialBadge(
                        text = "READY",
                        color = colors.secondaryAccent
                      )
                      Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Odebrat",
                        tint = colors.textMuted,
                        modifier = Modifier
                          .size(14.dp)
                          .clickable { viewModel.removeFromBatch(item.id) }
                      )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (item.bitmap != null) {
                      Image(
                        bitmap = item.bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(55.dp)
                          .clip(RoundedCornerShape(4.dp))
                      )
                    } else {
                      Box(
                        modifier = Modifier
                          .fillMaxWidth()
                          .height(55.dp)
                          .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF070E18)),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(
                          imageVector = Icons.Default.Psychology,
                          contentDescription = null,
                          tint = colors.secondaryAccent.copy(alpha = 0.4f),
                          modifier = Modifier.size(24.dp)
                        )
                      }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = item.label,
                      fontFamily = TechMonoFamily,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = colors.textPrimary,
                      maxLines = 1
                    )
                  }
                }
              }
            }
          }
        }

        // Trigger Batch Processing Button
        item {
          NeonButton(
            text = if (batchStatus == BatchProcessingStatus.PROCESSING) {
              "ZPRACOVÁVÁM DÁVKU ($currentBatchIdx/${batchQueue.size})..."
            } else "⚡ SPUSTIT DÁVKOVOU ANALÝZU (${batchQueue.size} POLOŽEK)",
            onClick = { viewModel.runBatchAnalysisQueue() },
            icon = Icons.Default.Search,
            color = colors.secondaryAccent,
            modifier = Modifier.fillMaxWidth(),
            enabled = batchStatus != BatchProcessingStatus.PROCESSING,
            testTag = "run_batch_analysis_btn"
          )
        }
      }

      // Processing Progress Bar
      if (batchStatus == BatchProcessingStatus.PROCESSING) {
        item {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = "Pipelined inference: Položka $currentBatchIdx z ${batchQueue.size}",
                fontFamily = TechMonoFamily,
                fontSize = 11.sp,
                color = colors.secondaryAccent
              )
              Text(
                text = "${(batchProgress * 100).toInt()}%",
                fontFamily = TechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = colors.secondaryAccent
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
              progress = { batchProgress },
              modifier = Modifier.fillMaxWidth(),
              color = colors.secondaryAccent
            )
          }
        }
      }

      // Batch Results Overview
      if (batchResults.isNotEmpty()) {
        item {
          CyberCard(borderColor = colors.secondaryAccent) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "VÝSLEDEK DÁVKOVÉ ANALÝZY",
                    fontFamily = TechMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = colors.secondaryAccent
                  )
                  Text(
                    text = "Identifikováno ${batchResults.size} součástek s MoE reflexí",
                    fontFamily = TechMonoFamily,
                    fontSize = 10.5.sp,
                    color = colors.textMuted
                  )
                }

                IndustrialBadge(
                  text = "${batchResults.size} ROZPOZNÁNO",
                  color = colors.secondaryAccent
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              NeonButton(
                text = "📥 PŘIDAT VŠECHNO DO INVENTÁŘE (${batchResults.size})",
                onClick = {
                  viewModel.addAllBatchResultsToInventory()
                  feedbackMessage = "Všech ${batchResults.size} součástek uloženo do inventáře!"
                },
                icon = Icons.Default.AddShoppingCart,
                color = colors.secondaryAccent,
                modifier = Modifier.fillMaxWidth(),
                testTag = "batch_add_all_to_inventory_btn"
              )

              feedbackMessage?.let { msg ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = "✓ $msg",
                  fontFamily = TechMonoFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = colors.secondaryAccent
                )
              }
            }
          }
        }

        items(batchResults) { res ->
          val scan = res.scanEntity
          CyberCard {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = scan.componentName,
                    fontFamily = TechMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = colors.textPrimary
                  )
                  Text(
                    text = "${scan.packageType} • ${scan.pinCount} pinů • Záchrana: ${scan.salvageRating}",
                    fontFamily = TechMonoFamily,
                    fontSize = 10.5.sp,
                    color = colors.primaryAccent
                  )
                }
                IndustrialBadge(
                  text = "${(scan.confidence * 100).toInt()}%",
                  color = colors.secondaryAccent
                )
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = scan.specsSummary,
                fontFamily = TechMonoFamily,
                fontSize = 10.5.sp,
                color = colors.textSecondary
              )
            }
          }
        }
      }
    }

    // --- SINGLE SCAN MODE VIEW ---
    if (viewMode == ScannerViewMode.SINGLE) {
      // Live Capture / Input Bar
      item {
        CyberCard(borderColor = colors.primaryAccent.copy(alpha = 0.5f)) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "VSTUP OBRAZU PRO ANALÝZU",
              fontFamily = TechMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = colors.primaryAccent,
              letterSpacing = 0.8.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              NeonButton(
                text = "CameraX Live",
                onClick = { viewMode = ScannerViewMode.CAMERAX_LIVE },
                icon = Icons.Default.Videocam,
                color = colors.primaryAccent,
                modifier = Modifier.weight(1.2f),
                testTag = "scanner_camerax_button"
              )

              OutlinedCyberButton(
                text = "Foto",
                onClick = { cameraLauncher.launch(null) },
                icon = Icons.Default.CameraAlt,
                modifier = Modifier.weight(0.9f),
                testTag = "scanner_camera_button"
              )

              OutlinedCyberButton(
                text = "Galerie",
                onClick = {
                  photoPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                  )
                },
                icon = Icons.Default.PhotoLibrary,
                modifier = Modifier.weight(0.9f),
                testTag = "scanner_gallery_button"
              )
            }
          }
        }
      }

      // Test Sample PCB Carousel
      item {
        Column {
          Text(
            text = "NEBO VYBERTE REFERENČNÍ E-WASTE DPS:",
            fontFamily = TechMonoFamily,
            fontSize = 11.sp,
            color = colors.textSecondary,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(8.dp))

          LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(SampleBoards.list) { board ->
              val isSelected = selectedBoard?.id == board.id && capturedBitmap == null
              Box(
                modifier = Modifier
                  .width(170.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(
                    if (isSelected) colors.primaryAccent.copy(alpha = 0.16f)
                    else colors.surfaceVariant
                  )
                  .border(
                    width = if (isSelected) 1.5.dp else 0.8.dp,
                    color = if (isSelected) colors.primaryAccent else colors.cardBorder,
                    shape = RoundedCornerShape(10.dp)
                  )
                  .clickable {
                    viewModel.selectSampleBoard(board)
                    viewModel.startScanAnalysis()
                  }
                  .padding(10.dp)
              ) {
                Column {
                  IndustrialBadge(
                    text = board.packageHint,
                    color = if (isSelected) colors.primaryAccent else colors.secondaryAccent
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = board.title,
                    fontFamily = TechMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp,
                    color = colors.textPrimary,
                    maxLines = 2
                  )
                  Spacer(modifier = Modifier.height(3.dp))
                  Text(
                    text = board.chipHint,
                    fontFamily = TechMonoFamily,
                    fontSize = 10.sp,
                    color = colors.primaryAccent
                  )
                }
              }
            }
          }
        }
      }

      // Live Viewfinder / HUD Card
      item {
        CyberCard(
          borderColor = if (status == ScannerStatus.PHASE1_DETECTING || status == ScannerStatus.PHASE2_REFLECTING) {
            colors.primaryAccent
          } else colors.cardBorder
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            // HUD Top Bar
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                      if (status == ScannerStatus.PHASE1_DETECTING || status == ScannerStatus.PHASE2_REFLECTING) {
                        colors.primaryAccent
                      } else colors.secondaryAccent
                    )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "OPTICKÝ HUD SKENERU",
                  fontFamily = TechMonoFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = colors.textPrimary
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                WebGpuDiagnosticIndicator(
                  report = diagReport,
                  isBenchmarking = isBenchmarking,
                  onRunBenchmark = { viewModel.runStressBenchmark() }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "FPS: 60",
                  fontFamily = TechMonoFamily,
                  fontSize = 10.sp,
                  color = colors.textMuted
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Viewfinder Target Box
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFF060B12))
                .border(1.dp, colors.primaryAccent.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
              contentAlignment = Alignment.Center
            ) {
              if (capturedBitmap != null) {
                Image(
                  bitmap = capturedBitmap!!.asImageBitmap(),
                  contentDescription = "Snímaná součástka",
                  modifier = Modifier.fillMaxSize()
                )
              } else {
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = colors.primaryAccent.copy(alpha = 0.4f),
                    modifier = Modifier.size(46.dp)
                  )
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = selectedBoard?.title ?: "Vyberte desku nebo pořiďte snímek",
                    fontFamily = TechMonoFamily,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                  )
                }
              }

              // Crosshair overlay reticle
              Box(
                modifier = Modifier
                  .size(70.dp)
                  .border(1.dp, colors.primaryAccent.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
              )

              // Animated Scanning Laser Line
              if (status == ScannerStatus.PHASE1_DETECTING || status == ScannerStatus.PHASE2_REFLECTING) {
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.TopCenter)
                    .padding(top = (170 * scanLineProgress).dp)
                    .background(
                      Brush.horizontalGradient(
                        listOf(Color.Transparent, colors.primaryAccent, Color.Transparent)
                      )
                    )
                )
              }

              // Corner Reticles
              Text(
                text = "[+]",
                fontFamily = TechMonoFamily,
                fontSize = 12.sp,
                color = colors.primaryAccent,
                modifier = Modifier.align(Alignment.TopStart).padding(6.dp)
              )
              Text(
                text = "[+]",
                fontFamily = TechMonoFamily,
                fontSize = 12.sp,
                color = colors.primaryAccent,
                modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            NeonButton(
              text = if (status == ScannerStatus.PHASE1_DETECTING || status == ScannerStatus.PHASE2_REFLECTING) {
                "PROBÍHÁ ANALÝZA..."
              } else "SPUSTIT 2-FÁZOVÝ SKEN",
              onClick = { viewModel.startScanAnalysis() },
              icon = Icons.Default.Search,
              modifier = Modifier.fillMaxWidth(),
              enabled = status != ScannerStatus.PHASE1_DETECTING && status != ScannerStatus.PHASE2_REFLECTING,
              testTag = "run_scan_analysis_button"
            )
          }
        }
      }

      // Single Scan Results Card
      lastResult?.let { result ->
        val scan = result.scanEntity

        item {
          CyberCard(borderColor = colors.secondaryAccent.copy(alpha = 0.7f)) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = scan.componentName,
                    fontFamily = TechMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = colors.primaryAccent
                  )
                  Text(
                    text = "${scan.packageType} • ${scan.pinCount} Pinů • ${scan.functionalRole}",
                    fontFamily = TechMonoFamily,
                    fontSize = 11.sp,
                    color = colors.textSecondary
                  )
                }

                IndustrialBadge(
                  text = "${(scan.confidence * 100).toInt()}% DŮVĚRA",
                  color = colors.secondaryAccent
                )
              }

              // Automated Tagging Engine Output
              val autoTag = remember(scan) { ComponentTaggingEngine.analyzeScan(scan) }
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(colors.primaryAccent.copy(alpha = 0.12f))
                  .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                    text = "AUTO KATEGORIE:",
                    fontFamily = TechMonoFamily,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.primaryAccent
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  IndustrialBadge(
                    text = autoTag.suggestedCategory,
                    color = colors.secondaryAccent
                  )
                }
                Text(
                  text = autoTag.estimatedSalvageYield,
                  fontFamily = TechMonoFamily,
                  fontSize = 9.5.sp,
                  color = colors.textMuted
                )
              }

              if (autoTag.suggestedTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(autoTag.suggestedTags) { tag ->
                    IndustrialBadge(text = tag, color = colors.primaryAccent)
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceVariant)
                    .padding(8.dp)
                ) {
                  Column {
                    Text(text = "ZÁCHRANA:", fontFamily = TechMonoFamily, fontSize = 9.sp, color = colors.textMuted)
                    Text(text = scan.salvageRating, fontFamily = TechMonoFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.secondaryAccent)
                  }
                }

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceVariant)
                    .padding(8.dp)
                ) {
                  Column {
                    Text(text = "ODPÁJENÍ:", fontFamily = TechMonoFamily, fontSize = 9.sp, color = colors.textMuted)
                    Text(text = scan.desolderDifficulty, fontFamily = TechMonoFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.alertAccent)
                  }
                }

                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceVariant)
                    .padding(8.dp)
                ) {
                  Column {
                    Text(text = "REFLEXE:", fontFamily = TechMonoFamily, fontSize = 9.sp, color = colors.textMuted)
                    Text(text = "VERIFIED", fontFamily = TechMonoFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.primaryAccent)
                  }
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(colors.surface)
                  .border(1.dp, colors.primaryAccent.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                  .padding(10.dp)
              ) {
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Default.CheckCircle,
                      contentDescription = null,
                      tint = colors.secondaryAccent,
                      modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = "VÝSLEDEK REFLEXE & BEZPEČNOSTNÍ VERIFIKACE:",
                      fontFamily = TechMonoFamily,
                      fontWeight = FontWeight.Bold,
                      fontSize = 11.sp,
                      color = colors.secondaryAccent
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = result.phaseTwoReflection,
                    fontFamily = TechMonoFamily,
                    fontSize = 11.5.sp,
                    color = colors.textPrimary,
                    lineHeight = 16.sp
                  )
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                NeonButton(
                  text = "Přidat do inventáře",
                  onClick = {
                    viewModel.convertCurrentScanToInventory()
                    feedbackMessage = "Součástka přidána do inventáře dílny!"
                  },
                  icon = Icons.Default.AddShoppingCart,
                  color = colors.secondaryAccent,
                  modifier = Modifier.weight(1f),
                  testTag = "scan_add_to_inventory_btn"
                )

                OutlinedCyberButton(
                  text = "Pinout",
                  onClick = { onNavigateToTab(WorkshopTab.PINOUTS) },
                  icon = Icons.Default.Hub,
                  testTag = "scan_view_pinout_btn"
                )

                OutlinedCyberButton(
                  text = "Uložit",
                  onClick = {
                    viewModel.saveCurrentScanToHistory()
                    feedbackMessage = "Sken uložen do historie!"
                  },
                  icon = Icons.Default.BookmarkBorder,
                  testTag = "scan_save_history_btn"
                )
              }

              feedbackMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "✓ $msg",
                  fontFamily = TechMonoFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = colors.secondaryAccent
                )
              }
            }
          }
        }
      }

      // Schematic & Text OCR / RAG Ingestion Section
      item {
        CyberCard(borderColor = colors.primaryAccent.copy(alpha = 0.6f)) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.MenuBook,
                  contentDescription = null,
                  tint = colors.primaryAccent,
                  modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "OCR POTISK DPS & SCHÉMA DO ASISTENTA",
                  fontFamily = TechMonoFamily,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = colors.textPrimary
                )
              }

              IndustrialBadge(
                text = "OCR + RAG",
                color = colors.primaryAccent
              )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Předzpracování a extrakce textu z potisku desky (silkscreen), značení čipů a netlistu přímo do vyhledávacího kontextu asistenta s přesnými citacemi.",
              fontFamily = TechMonoFamily,
              fontSize = 11.sp,
              color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              NeonButton(
                text = if (isExtractingSchematic) "EXTRAHUJI POTISK..." else "🔍 OCR POTISKU DO ASISTENTA",
                onClick = { viewModel.performOcrPreprocessingAndIngest() },
                icon = Icons.Default.Search,
                color = colors.secondaryAccent,
                enabled = !isExtractingSchematic,
                modifier = Modifier.weight(1.1f),
                testTag = "ocr_preprocessing_silkscreen_btn"
              )

              OutlinedCyberButton(
                text = "Schéma & Netlist",
                onClick = { viewModel.extractSchematicFromScan() },
                icon = Icons.Default.Hub,
                color = colors.primaryAccent,
                modifier = Modifier.weight(0.9f),
                testTag = "extract_schematic_ocr_btn"
              )
            }

            lastSchematicResult?.let { schRes ->
              Spacer(modifier = Modifier.height(12.dp))
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(colors.surfaceVariant)
                  .padding(10.dp)
              ) {
                Column {
                  Text(
                    text = schRes.title,
                    fontFamily = TechMonoFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = colors.primaryAccent
                  )
                  Text(
                    text = "Soubor: ${schRes.fileName} • ${schRes.keyParameters}",
                    fontFamily = TechMonoFamily,
                    fontSize = 10.sp,
                    color = colors.textMuted
                  )

                  Spacer(modifier = Modifier.height(8.dp))

                  // Detected Components
                  if (schRes.detectedComponents.isNotEmpty()) {
                    Text(
                      text = "ROZPOZNANÉ SOUČÁSTKY:",
                      fontFamily = TechMonoFamily,
                      fontSize = 9.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = colors.secondaryAccent
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                      items(schRes.detectedComponents) { comp ->
                        IndustrialBadge(text = comp, color = colors.secondaryAccent)
                      }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                  }

                  // Detected Nets
                  if (schRes.detectedNets.isNotEmpty()) {
                    Text(
                      text = "SIGNÁLOVÉ CESTY & NETLIST:",
                      fontFamily = TechMonoFamily,
                      fontSize = 9.5.sp,
                      fontWeight = FontWeight.Bold,
                      color = colors.alertAccent
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                      items(schRes.detectedNets) { net ->
                        IndustrialBadge(text = net, color = colors.alertAccent)
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(10.dp))

                  // RAG Index Action
                  if (!isIngestedToRag) {
                    NeonButton(
                      text = "📥 INDEXOVAT DO RAG PRO ASISTENTA",
                      onClick = { viewModel.indexSchematicToRag(schRes) },
                      icon = Icons.Default.AddShoppingCart,
                      color = colors.secondaryAccent,
                      modifier = Modifier.fillMaxWidth(),
                      testTag = "index_schematic_to_rag_btn"
                    )
                  } else {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                          imageVector = Icons.Default.CheckCircle,
                          contentDescription = null,
                          tint = colors.secondaryAccent,
                          modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                          text = "ZAINDEXOVÁNO DO RAG!",
                          fontFamily = TechMonoFamily,
                          fontWeight = FontWeight.Bold,
                          fontSize = 11.sp,
                          color = colors.secondaryAccent
                        )
                      }

                      OutlinedCyberButton(
                        text = "Otevřít asistenta",
                        onClick = { onNavigateToTab(WorkshopTab.ASSISTANT) },
                        icon = Icons.Default.Hub,
                        testTag = "go_to_assistant_btn"
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

    // Telemetry Logs Card
    item {
      CyberCard {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = "SYSTÉMOVÝ TELEMETRICKÝ LOG",
            fontFamily = TechMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = colors.textMuted
          )
          Spacer(modifier = Modifier.height(6.dp))
          logs.takeLast(4).forEach { logLine ->
            Text(
              text = "> $logLine",
              fontFamily = TechMonoFamily,
              fontSize = 10.5.sp,
              color = colors.textSecondary,
              lineHeight = 15.sp
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(80.dp))
    }
  }
}

private fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap {
  return if (Build.VERSION.SDK_INT < 28) {
    @Suppress("DEPRECATION")
    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
  } else {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
      decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
      decoder.isMutableRequired = true
    }
  }
}
