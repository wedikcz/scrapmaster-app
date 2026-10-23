package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.LocalScrapMasterColors
import com.example.ui.theme.TechMonoFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraXLiveScannerView(
  onFrameCaptured: (Bitmap) -> Unit,
  onBatchFrameAdded: (Bitmap) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  val colors = LocalScrapMasterColors.current
  val scope = rememberCoroutineScope()

  var hasCameraPermission by remember {
    mutableStateOf(
      ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    )
  }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { granted ->
    hasCameraPermission = granted
  }

  var camera by remember { mutableStateOf<Camera?>(null) }
  var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
  var previewViewInstance by remember { mutableStateOf<PreviewView?>(null) }
  var isTorchOn by remember { mutableStateOf(false) }
  var isCapturing by remember { mutableStateOf(false) }

  val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
  DisposableEffect(Unit) {
    onDispose {
      cameraExecutor.shutdown()
    }
  }

  // Laser scanner animation
  val infiniteTransition = rememberInfiniteTransition(label = "laser_anim")
  val scanProgress by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(2200, easing = LinearEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "laser_beam"
  )

  if (!hasCameraPermission) {
    // Permission request panel
    Box(
      modifier = modifier
        .fillMaxWidth()
        .height(380.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(colors.surface)
        .border(1.dp, colors.primaryAccent.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        .padding(20.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Box(
          modifier = Modifier
            .size(60.dp)
            .clip(CircleShape)
            .background(colors.primaryAccent.copy(alpha = 0.15f))
            .border(1.dp, colors.primaryAccent, CircleShape),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Videocam,
            contentDescription = null,
            tint = colors.primaryAccent,
            modifier = Modifier.size(32.dp)
          )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "CAMERAX LIVE PCB SKENER",
          fontFamily = TechMonoFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = colors.textPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Pro real-time snímání desek, čtení silkscreen potisků a OCR analýzu čipů je vyžadován přístup k fotoaparátu.",
          fontFamily = TechMonoFamily,
          fontSize = 11.5.sp,
          color = colors.textMuted,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        NeonButton(
          text = "POVOLIT KAMERU",
          onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
          icon = Icons.Default.Security,
          color = colors.primaryAccent,
          testTag = "grant_camera_permission_btn"
        )
      }
    }
  } else {
    // CameraX Live Surface Viewfinder
    Box(
      modifier = modifier
        .fillMaxWidth()
        .height(420.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(Color.Black)
        .border(1.5.dp, colors.primaryAccent.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
        .testTag("camerax_live_viewfinder")
    ) {
      // 1. AndroidView PreviewView
      AndroidView(
        factory = { ctx ->
          PreviewView(ctx).apply {
            previewViewInstance = this
            scaleType = PreviewView.ScaleType.FILL_CENTER
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
              try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                  it.setSurfaceProvider(surfaceProvider)
                }

                val capture = ImageCapture.Builder()
                  .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                  .build()
                imageCapture = capture

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                  lifecycleOwner,
                  cameraSelector,
                  preview,
                  capture
                )
              } catch (e: Exception) {
                e.printStackTrace()
              }
            }, ContextCompat.getMainExecutor(ctx))
          }
        },
        modifier = Modifier.fillMaxSize()
      )

      // 2. Cyberpunk HUD Overlay with Reticle & Laser Beam
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(14.dp)
      ) {
        // Laser Scan line
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .align(Alignment.TopCenter)
            .padding(top = (scanProgress * 360).dp)
            .background(
              Brush.horizontalGradient(
                colors = listOf(
                  Color.Transparent,
                  colors.primaryAccent.copy(alpha = 0.8f),
                  colors.primaryAccent,
                  colors.primaryAccent.copy(alpha = 0.8f),
                  Color.Transparent
                )
              )
            )
        )

        // Corner Targeting Reticle
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
            .border(1.dp, colors.primaryAccent.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
        )

        // Top Status HUD Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.65f))
            .border(0.8.dp, colors.primaryAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(colors.secondaryAccent)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "CAMERAX LIVE HUD • SILKSCREEN OCR READY",
              fontFamily = TechMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 9.5.sp,
              color = colors.primaryAccent,
              letterSpacing = 0.5.sp
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            // Flashlight button
            IconButton(
              onClick = {
                val nextState = !isTorchOn
                camera?.cameraControl?.enableTorch(nextState)
                isTorchOn = nextState
              },
              modifier = Modifier.size(28.dp).testTag("camerax_torch_btn")
            ) {
              Icon(
                imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = "Blesk",
                tint = if (isTorchOn) colors.alertAccent else colors.textMuted,
                modifier = Modifier.size(16.dp)
              )
            }

            // Close button
            IconButton(
              onClick = onClose,
              modifier = Modifier.size(28.dp).testTag("camerax_close_btn")
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Zavřít kameru",
                tint = colors.textMuted,
                modifier = Modifier.size(16.dp)
              )
            }
          }
        }

        // Bottom Capture Controls
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(bottom = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          if (isCapturing) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.8f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = colors.primaryAccent,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Extrakce silkscreenu z CameraX...",
                fontFamily = TechMonoFamily,
                fontSize = 10.sp,
                color = colors.primaryAccent
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Single Capture & OCR button
            NeonButton(
              text = "SNÍMAT FRAME & OCR",
              onClick = {
                val previewBitmap = previewViewInstance?.bitmap
                if (previewBitmap != null) {
                  onFrameCaptured(previewBitmap)
                } else {
                  val capture = imageCapture
                  if (capture != null) {
                    isCapturing = true
                    capture.takePicture(
                      cameraExecutor,
                      object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                          val bmp = image.toBitmap()
                          image.close()
                          scope.launch(Dispatchers.Main) {
                            isCapturing = false
                            onFrameCaptured(bmp)
                          }
                        }

                        override fun onError(exception: ImageCaptureException) {
                          scope.launch(Dispatchers.Main) {
                            isCapturing = false
                          }
                        }
                      }
                    )
                  }
                }
              },
              icon = Icons.Default.QrCodeScanner,
              color = colors.primaryAccent,
              enabled = !isCapturing,
              testTag = "camerax_capture_ocr_btn"
            )

            // Add to Batch button
            NeonButton(
              text = "+ DÁVKA",
              onClick = {
                val previewBitmap = previewViewInstance?.bitmap
                if (previewBitmap != null) {
                  onBatchFrameAdded(previewBitmap)
                } else {
                  val capture = imageCapture
                  if (capture != null) {
                    isCapturing = true
                    capture.takePicture(
                      cameraExecutor,
                      object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                          val bmp = image.toBitmap()
                          image.close()
                          scope.launch(Dispatchers.Main) {
                            isCapturing = false
                            onBatchFrameAdded(bmp)
                          }
                        }

                        override fun onError(exception: ImageCaptureException) {
                          scope.launch(Dispatchers.Main) {
                            isCapturing = false
                          }
                        }
                      }
                    )
                  }
                }
              },
              icon = Icons.Default.Layers,
              color = colors.secondaryAccent,
              enabled = !isCapturing,
              testTag = "camerax_add_batch_btn"
            )
          }
        }
      }
    }
  }
}
