package com.example

import com.example.ai.ComponentTaggingEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun `ComponentTaggingEngine correctly categorizes Power Regulation components`() {
    val result7805 = ComponentTaggingEngine.analyzeComponent(
      name = "LM7805",
      packageType = "TO-220",
      specifications = "Linear positive voltage regulator 5V 1.5A"
    )
    assertEquals("Power Regulation", result7805.suggestedCategory)
    assertTrue(result7805.suggestedTags.contains("Regulátor"))
    assertTrue(result7805.suggestedTags.contains("5V Výstup"))

    val resultAms = ComponentTaggingEngine.analyzeComponent(
      name = "AMS1117-3.3",
      packageType = "SOT-223",
      specifications = "3.3V LDO regulator"
    )
    assertEquals("Power Regulation", resultAms.suggestedCategory)
    assertTrue(resultAms.suggestedTags.contains("LDO"))
  }

  @Test
  fun `ComponentTaggingEngine correctly categorizes Microcontrollers`() {
    val resultEsp = ComponentTaggingEngine.analyzeComponent(
      name = "ESP32-WROOM-32",
      packageType = "Module",
      specifications = "Dual-core Xtensa 32-bit MCU with Wi-Fi and Bluetooth"
    )
    assertEquals("Microcontrollers", resultEsp.suggestedCategory)
    assertTrue(resultEsp.suggestedTags.contains("Wi-Fi/BLE"))
    assertTrue(resultEsp.suggestedTags.contains("MCU"))

    val resultStm = ComponentTaggingEngine.analyzeComponent(
      name = "STM32F103C8T6",
      packageType = "LQFP-48",
      specifications = "ARM Cortex-M3 32-bit MCU"
    )
    assertEquals("Microcontrollers", resultStm.suggestedCategory)
    assertTrue(resultStm.suggestedTags.contains("ARM 32-bit"))
  }

  @Test
  fun `ComponentTaggingEngine correctly categorizes Passives and Power Semiconductors`() {
    val resultMosfet = ComponentTaggingEngine.analyzeComponent(
      name = "IRFZ44N",
      packageType = "TO-220",
      specifications = "N-Channel MOSFET 55V 49A"
    )
    assertEquals("Power Semiconductors", resultMosfet.suggestedCategory)
    assertTrue(resultMosfet.suggestedTags.contains("MOSFET"))
    assertTrue(resultMosfet.suggestedTags.contains("N-Kanál"))

    val resultCap = ComponentTaggingEngine.analyzeComponent(
      name = "100uF 400V",
      packageType = "Radial",
      specifications = "High voltage electrolytic capacitor for SMPS"
    )
    assertEquals("Passives", resultCap.suggestedCategory)
    assertTrue(resultCap.suggestedTags.contains("Kondenzátor"))
  }

  @Test
  fun `ComponentTaggingEngine correctly categorizes Analog and Opto`() {
    val result555 = ComponentTaggingEngine.analyzeComponent(
      name = "NE555P",
      packageType = "DIP-8",
      specifications = "Precision timer IC"
    )
    assertEquals("Analog & Timers", result555.suggestedCategory)
    assertTrue(result555.suggestedTags.contains("Časovač NE555"))

    val resultOpto = ComponentTaggingEngine.analyzeComponent(
      name = "PC817",
      packageType = "DIP-4",
      specifications = "Phototransistor optocoupler for galvanic isolation"
    )
    assertEquals("Optoelectronics", resultOpto.suggestedCategory)
    assertTrue(resultOpto.suggestedTags.contains("Optočlen"))
  }
}
