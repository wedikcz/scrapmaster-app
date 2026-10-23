package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.ThemeMode
import com.example.data.local.ThemePreferenceManager
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("ScrapMaster AI", appName)
  }

  @Test
  fun `theme toggle persists across sessions`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val themeManager = ThemePreferenceManager.getInstance(context)

    themeManager.setThemeMode(ThemeMode.DARK)
    assertEquals(ThemeMode.DARK, themeManager.themeMode.value)

    themeManager.toggleTheme()
    assertEquals(ThemeMode.LIGHT, themeManager.themeMode.value)

    themeManager.toggleTheme()
    assertEquals(ThemeMode.DARK, themeManager.themeMode.value)
  }

  @Test
  fun `HardwareInferenceDiagnostics generates valid SystemHealthTelemetry`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val diagnostics = com.example.ai.HardwareInferenceDiagnostics.getInstance(context)
    val report = diagnostics.runQuickDiagnostic()

    org.junit.Assert.assertNotNull(report.systemHealth)
    org.junit.Assert.assertTrue(report.systemHealth.webGpuBudgetMb > 0)
    org.junit.Assert.assertTrue(report.systemHealth.totalWorkersCount >= 1)
    org.junit.Assert.assertNotNull(report.systemHealth.continuousProcessingState)

    diagnostics.purgeMemoryBuffers()
    val purgedReport = diagnostics.diagnosticReport.value
    org.junit.Assert.assertTrue(purgedReport.systemHealth.memoryPressurePercent <= 50)
  }
}
