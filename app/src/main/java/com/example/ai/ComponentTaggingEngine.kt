package com.example.ai

import com.example.data.local.entity.ItemEntity
import com.example.data.local.entity.ScanEntity
import java.util.Locale

data class AutoTaggingResult(
  val suggestedCategory: String,
  val suggestedTags: List<String>,
  val confidenceScore: Float,
  val categoryReason: String,
  val estimatedSalvageYield: String
)

object ComponentTaggingEngine {

  val STANDARD_CATEGORIES = listOf(
    "Power Regulation",
    "Microcontrollers",
    "Power Semiconductors",
    "Passives",
    "Diodes & Rectifiers",
    "Analog & Timers",
    "Optoelectronics",
    "Connectors & Relays",
    "Logic & Digital ICs"
  )

  /**
   * Analyzes parsed component metadata and derives precise categorization & tags
   * for e-waste salvage, inventory organization, and RAG retrieval.
   */
  fun analyzeComponent(
    name: String,
    packageType: String = "",
    specifications: String = "",
    functionalRole: String = "",
    salvageSource: String = ""
  ): AutoTaggingResult {
    val combinedText = "$name $packageType $specifications $functionalRole $salvageSource".uppercase(Locale.ROOT)
    val upperName = name.uppercase(Locale.ROOT).trim()

    // 1. Power Regulation
    if (matchesPowerRegulation(upperName, combinedText)) {
      val tags = mutableListOf("Napájení", "Regulátor")
      if (combinedText.contains("LDO") || upperName.contains("AMS1117") || upperName.contains("LP29")) tags.add("LDO")
      if (combinedText.contains("SMPS") || combinedText.contains("BUCK") || combinedText.contains("BOOST") || combinedText.contains("PWM")) tags.add("Spínaný SMPS")
      if (packageType.isNotBlank()) tags.add(packageType)
      if (combinedText.contains("5V")) tags.add("5V Výstup")
      if (combinedText.contains("3.3V") || combinedText.contains("3V3")) tags.add("3.3V Výstup")
      if (combinedText.contains("ADJ") || combinedText.contains("LM317")) tags.add("Nastavitelné (ADJ)")

      return AutoTaggingResult(
        suggestedCategory = "Power Regulation",
        suggestedTags = tags.distinct(),
        confidenceScore = 0.96f,
        categoryReason = "Detekován stabilizátor/PWM řadič napájecích větví (${extractChipModel(upperName)})",
        estimatedSalvageYield = "Vysoká (Kritické pro opravy zdrojů)"
      )
    }

    // 2. Microcontrollers & SoCs
    if (matchesMicrocontrollers(upperName, combinedText)) {
      val tags = mutableListOf("MCU", "Programovatelné", "Digitální")
      if (upperName.contains("ESP32") || upperName.contains("ESP8266")) {
        tags.add("Wi-Fi/BLE")
        tags.add("IoT")
      }
      if (upperName.contains("STM32") || combinedText.contains("CORTEX") || combinedText.contains("ARM")) tags.add("ARM 32-bit")
      if (upperName.contains("ATMEGA") || upperName.contains("ATTINY") || combinedText.contains("AVR")) tags.add("8-bit AVR")
      if (upperName.contains("RP2040")) tags.add("Dual-Core RP2040")
      if (packageType.isNotBlank()) tags.add(packageType)

      return AutoTaggingResult(
        suggestedCategory = "Microcontrollers",
        suggestedTags = tags.distinct(),
        confidenceScore = 0.98f,
        categoryReason = "Detekován programovatelný mikrokontrolér / SoC čip",
        estimatedSalvageYield = "Vysoká (Vzácný křemík z e-waste)"
      )
    }

    // 3. Power Semiconductors (MOSFETs, IGBTs, Triacs, Thyristors, Power BJTs)
    if (matchesPowerSemiconductors(upperName, combinedText)) {
      val tags = mutableListOf("Polovodiče", "Výkonové spínání")
      if (combinedText.contains("MOSFET") || upperName.contains("IRF") || upperName.contains("STP")) {
        tags.add("MOSFET")
        if (combinedText.contains("P-CH") || combinedText.contains("P-CHANNEL")) tags.add("P-Kanál") else tags.add("N-Kanál")
      }
      if (combinedText.contains("TRIAC") || upperName.contains("BTA") || upperName.contains("BT13")) tags.add("Triak AC")
      if (combinedText.contains("IGBT")) tags.add("IGBT")
      if (packageType.isNotBlank()) tags.add(packageType)
      tags.add("Chladič vyžadován")

      return AutoTaggingResult(
        suggestedCategory = "Power Semiconductors",
        suggestedTags = tags.distinct(),
        confidenceScore = 0.95f,
        categoryReason = "Identifikován výkonový spínací tranzistor/triak pro vysoké proudy",
        estimatedSalvageYield = "Vysoká (Před použitím proměřit body-diodu)"
      )
    }

    // 4. Passives (Capacitors, Resistors, Inductors, Chokes, Potentiometers)
    if (matchesPassives(upperName, combinedText)) {
      val tags = mutableListOf("Pasivní součástky")
      if (combinedText.contains("CAPACITOR") || combinedText.contains("KONDENZ") || combinedText.contains("UF") || combinedText.contains("NF")) {
        tags.add("Kondenzátor")
        if (combinedText.contains("ELECTROLYT") || combinedText.contains("ELYT")) tags.add("Elektrolytický")
        if (combinedText.contains("400V") || combinedText.contains("450V") || combinedText.contains("VN")) tags.add("VN Bezpečnost")
      }
      if (combinedText.contains("RESISTOR") || combinedText.contains("ODPOR") || combinedText.contains("OHM") || upperName.contains("10K")) {
        tags.add("Rezistor")
      }
      if (combinedText.contains("INDUCTOR") || combinedText.contains("TLUMIV") || combinedText.contains("CÍVK") || combinedText.contains("CHOKE")) {
        tags.add("Tlumivka/Cívka")
      }
      if (packageType.isNotBlank()) tags.add(packageType)

      return AutoTaggingResult(
        suggestedCategory = "Passives",
        suggestedTags = tags.distinct(),
        confidenceScore = 0.92f,
        categoryReason = "Pasivní RLC součástka pro filtraci nebo ladění obvodů",
        estimatedSalvageYield = "Běžná (Ověřit ESR a kapacitu)"
      )
    }

    // 5. Diodes & Rectifiers
    if (matchesDiodes(upperName, combinedText)) {
      val tags = mutableListOf("Diody & Usměrňovače")
      if (upperName.contains("1N400") || combinedText.contains("RECTIFIER") || combinedText.contains("USMĚR")) tags.add("Síťový usměrňovač")
      if (combinedText.contains("SCHOTTKY") || upperName.contains("1N58") || upperName.contains("SS") || upperName.contains("MBR")) tags.add("Schottky (Nízký úbytek)")
      if (combinedText.contains("ZENER") || upperName.contains("BZX")) tags.add("Zenerova dioda")
      if (combinedText.contains("BRIDGE") || upperName.contains("KBU") || upperName.contains("GBU")) tags.add("Můstkový usměrňovač")
      if (packageType.isNotBlank()) tags.add(packageType)

      return AutoTaggingResult(
        suggestedCategory = "Diodes & Rectifiers",
        suggestedTags = tags.distinct(),
        confidenceScore = 0.95f,
        categoryReason = "Usměrňovací nebo ochranná polovodičová dioda",
        estimatedSalvageYield = "Střední (Zkontrolovat úbytek Vf multimetrem)"
      )
    }

    // 6. Analog & Timers (NE555, Op-Amps, Audio ICs)
    if (matchesAnalogAndTimers(upperName, combinedText)) {
      val tags = mutableListOf("Analogové IO")
      if (upperName.contains("555") || combinedText.contains("TIMER") || combinedText.contains("ČASOV")) tags.add("Časovač NE555")
      if (upperName.contains("LM358") || upperName.contains("LM324") || upperName.contains("TL07") || combinedText.contains("OP-AMP")) tags.add("Operační zesilovač")
      if (upperName.contains("LM393") || upperName.contains("LM339") || combinedText.contains("COMPARATOR")) tags.add("Komparátor")
      if (upperName.contains("TDA") || upperName.contains("LM386") || combinedText.contains("AUDIO")) tags.add("Audio zesilovač")
      if (packageType.isNotBlank()) tags.add(packageType)

      return AutoTaggingResult(
        suggestedCategory = "Analog & Timers",
        suggestedTags = tags.distinct(),
        confidenceScore = 0.94f,
        categoryReason = "Analogový operační zesilovač, komparátor nebo časovač",
        estimatedSalvageYield = "Vysoká (Univerzální dílenské použití)"
      )
    }

    // 7. Optoelectronics (Optocouplers, LEDs, Displays)
    if (matchesOpto(upperName, combinedText)) {
      val tags = mutableListOf("Optoelektronika")
      if (upperName.contains("PC817") || upperName.contains("4N35") || upperName.contains("EL817") || combinedText.contains("OPTO")) {
        tags.add("Optočlen")
        tags.add("Galvanické oddělení")
      }
      if (combinedText.contains("LED") || combinedText.contains("DISPLAY") || combinedText.contains("OLED")) tags.add("Optický indikátor")
      if (packageType.isNotBlank()) tags.add(packageType)

      return AutoTaggingResult(
        suggestedCategory = "Optoelectronics",
        suggestedTags = tags.distinct(),
        confidenceScore = 0.95f,
        categoryReason = "Optické oddělení nebo světelná signalizace",
        estimatedSalvageYield = "Vysoká (Kritické pro bezpečnost SMPS zdrojů)"
      )
    }

    // 8. Connectors, Relays & Electromechanical
    if (matchesConnectorsAndRelays(upperName, combinedText)) {
      val tags = mutableListOf("Elektromechanické")
      if (combinedText.contains("RELAY") || combinedText.contains("RELÉ")) tags.add("Relé")
      if (combinedText.contains("CONNECTOR") || combinedText.contains("USB") || combinedText.contains("HEADER") || combinedText.contains("TERMINAL")) tags.add("Konektor")
      if (packageType.isNotBlank()) tags.add(packageType)

      return AutoTaggingResult(
        suggestedCategory = "Connectors & Relays",
        suggestedTags = tags.distinct(),
        confidenceScore = 0.90f,
        categoryReason = "Konektor, mechanické relé nebo přepínač",
        estimatedSalvageYield = "Střední (Zkontrolovat odpor kontaktů)"
      )
    }

    // Default Fallback
    return AutoTaggingResult(
      suggestedCategory = "Logic & Digital ICs",
      suggestedTags = listOf("Integrovaný obvod", if (packageType.isNotBlank()) packageType else "DIP/SMD", "E-Waste"),
      confidenceScore = 0.85f,
      categoryReason = "Digitální nebo univerzální integrovaný obvod",
      estimatedSalvageYield = "Standardní e-waste součástka"
    )
  }

  fun analyzeScan(scan: ScanEntity): AutoTaggingResult {
    return analyzeComponent(
      name = scan.componentName,
      packageType = scan.packageType,
      specifications = scan.specsSummary,
      functionalRole = scan.functionalRole,
      salvageSource = scan.salvageRating
    )
  }

  fun analyzeItem(item: ItemEntity): AutoTaggingResult {
    return analyzeComponent(
      name = item.name,
      packageType = item.packageType,
      specifications = item.specifications,
      salvageSource = item.salvageSource
    )
  }

  private fun matchesPowerRegulation(name: String, text: String): Boolean {
    val patterns = listOf(
      "LM78", "LM79", "LM317", "LM1117", "AMS1117", "TPS", "MP1584", "MP23",
      "UC384", "TL494", "VIPER", "TOP2", "LDO", "REGULAT", "STABILIZ", "BUCK",
      "BOOST", "STEP-DOWN", "STEP-UP", "DC-DC", "PWM", "POWER SUPPLY"
    )
    return patterns.any { text.contains(it) }
  }

  private fun matchesMicrocontrollers(name: String, text: String): Boolean {
    val patterns = listOf(
      "ESP32", "ESP8266", "STM32", "ATMEGA", "ATTINY", "PIC16", "PIC18",
      "RP2040", "CORTEX", "MICROCONTROLLER", "MIKROKONTROL", "MCU", "SOC",
      "NRF52", "SAMD21", "AVR", "ARDUINO"
    )
    return patterns.any { text.contains(it) }
  }

  private fun matchesPowerSemiconductors(name: String, text: String): Boolean {
    val patterns = listOf(
      "MOSFET", "IRF", "IRFZ", "STP", "IRFB", "SI44", "AO44", "IGBT",
      "TRIAC", "TRIAK", "THYRISTOR", "TYRISTOR", "BTA16", "BTA12", "BT136", "BT137",
      "2N3055", "TIP120", "TIP122", "TIP127", "TIP31", "TIP32", "POWER TRANSISTOR"
    )
    return patterns.any { text.contains(it) }
  }

  private fun matchesPassives(name: String, text: String): Boolean {
    val patterns = listOf(
      "CAPACITOR", "KONDENZ", "ELECTROLYTIC", "RESISTOR", "ODPOR", "POTENTIOMETER",
      "POTENCIOMETR", "INDUCTOR", "TLUMIVKA", "CÍVKA", "CHOKE", "FERRITE", "FERIT",
      "0805", "1206", "0603", "LOW-ESR", "UF", "NF", "PF", "KOHM", "MOHM"
    )
    return patterns.any { text.contains(it) }
  }

  private fun matchesDiodes(name: String, text: String): Boolean {
    val patterns = listOf(
      "1N400", "1N4148", "1N58", "1N54", "DIODE", "DIODA", "SCHOTTKY", "ZENER",
      "RECTIFIER", "USMĚRŇ", "MBR", "US1M", "SS14", "SS34", "BRIDGE RECTIFIER",
      "KBU", "GBU"
    )
    return patterns.any { text.contains(it) }
  }

  private fun matchesAnalogAndTimers(name: String, text: String): Boolean {
    val patterns = listOf(
      "555", "NE555", "LM555", "LM358", "LM324", "LM393", "LM339", "TL07",
      "TL08", "OP-AMP", "OPAMP", "OPERAČNÍ", "COMPARATOR", "KOMPARÁTOR",
      "TDA20", "TDA72", "LM386", "AUDIO AMP"
    )
    return patterns.any { text.contains(it) }
  }

  private fun matchesOpto(name: String, text: String): Boolean {
    val patterns = listOf(
      "PC817", "4N35", "4N25", "EL817", "MOC30", "OPTOCOUPLER", "OPTOČLEN",
      "OPTICAL ISOLAT", "PHOTODIODE", "PHOTOTRANSISTOR", "OLED", "LED DISPLAY"
    )
    return patterns.any { text.contains(it) }
  }

  private fun matchesConnectorsAndRelays(name: String, text: String): Boolean {
    val patterns = listOf(
      "RELAY", "RELÉ", "CONNECTOR", "KONEKTOR", "TERMINAL", "SVORKOVNICE",
      "USB-C", "MICRO-USB", "MOLEX", "HEADER", "JUMPER", "SWITCH", "PŘEPÍNAČ"
    )
    return patterns.any { text.contains(it) }
  }

  private fun extractChipModel(name: String): String {
    val parts = name.split(" ", "/", "-")
    return parts.firstOrNull { it.length >= 3 } ?: name
  }
}
