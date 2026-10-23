package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.HardwareDiagnosticReport
import com.example.ui.components.FloatingGlassDock
import com.example.ui.components.TopWorkshopHeader
import com.example.ui.components.WorkshopTab
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.PinoutsScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.theme.LocalScrapMasterColors
import com.example.ui.theme.ScrapMasterTheme
import com.example.viewmodel.WorkshopViewModel

class MainActivity : ComponentActivity() {

  private val viewModel: WorkshopViewModel by viewModels {
    WorkshopViewModel.Factory(application)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
      val lowStockItems by viewModel.lowStockItems.collectAsStateWithLifecycle()
      val diagnosticReport by viewModel.diagnosticReport.collectAsStateWithLifecycle()
      val isBenchmarking by viewModel.isBenchmarking.collectAsStateWithLifecycle()

      ScrapMasterTheme(themeMode = themeMode) {
        val colors = LocalScrapMasterColors.current
        var currentTab by remember { mutableStateOf(WorkshopTab.DASHBOARD) }

        // BackHandler: Return to Dashboard if on another tab
        BackHandler(enabled = currentTab != WorkshopTab.DASHBOARD) {
          currentTab = WorkshopTab.DASHBOARD
        }

        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
        ) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .windowInsetsPadding(WindowInsets.statusBars)
          ) {
            // Persistent Workshop Header with Dark/Light toggle
            TopWorkshopHeader(
              currentTheme = themeMode,
              onToggleTheme = { viewModel.toggleTheme() },
              lowStockCount = lowStockItems.size,
              diagnosticReport = diagnosticReport,
              isBenchmarking = isBenchmarking,
              onRunBenchmark = { viewModel.runStressBenchmark() }
            )

            // Animated Screen Switching
            Box(modifier = Modifier.weight(1f)) {
              AnimatedContent(
                targetState = currentTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
              ) { tab ->
                when (tab) {
                  WorkshopTab.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { currentTab = it }
                  )
                  WorkshopTab.SCANNER -> ScannerScreen(
                    viewModel = viewModel,
                    onNavigateToTab = { currentTab = it }
                  )
                  WorkshopTab.INVENTORY -> InventoryScreen(
                    viewModel = viewModel
                  )
                  WorkshopTab.PINOUTS -> PinoutsScreen(
                    viewModel = viewModel
                  )
                  WorkshopTab.PROJECTS -> ProjectsScreen(
                    viewModel = viewModel
                  )
                  WorkshopTab.ASSISTANT -> AssistantScreen(
                    viewModel = viewModel
                  )
                }
              }
            }
          }

          // Floating Glass Dock positioned at bottom
          FloatingGlassDock(
            currentTab = currentTab,
            onSelectTab = { currentTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
          )
        }
      }
    }
  }
}
