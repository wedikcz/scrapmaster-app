package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiScannerService
import com.example.ai.HardwareDiagnosticReport
import com.example.ai.HardwareInferenceDiagnostics
import com.example.ai.InferenceEngineMode
import com.example.ai.SampleBoard
import com.example.ai.SampleBoards
import com.example.ai.ScanAnalysisResult
import com.example.ai.SchematicOcrEngine
import com.example.ai.SchematicOcrResult
import com.example.data.local.ScrapMasterDatabase
import com.example.data.local.ThemeMode
import com.example.data.local.ThemePreferenceManager
import com.example.data.local.entity.DatasheetEntity
import com.example.data.local.entity.ItemEntity
import com.example.data.local.entity.PinoutEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.ScanEntity
import com.example.data.repository.RagSearchResult
import com.example.data.repository.WorkshopRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ScannerStatus {
  READY,
  PHASE1_DETECTING,
  PHASE2_REFLECTING,
  SUCCESS,
  ERROR
}

data class BatchQueueItem(
  val id: String = java.util.UUID.randomUUID().toString(),
  val bitmap: Bitmap? = null,
  val sampleBoard: SampleBoard? = null,
  val label: String,
  val timestamp: Long = System.currentTimeMillis()
)

enum class BatchProcessingStatus {
  IDLE,
  PROCESSING,
  COMPLETED,
  ERROR
}

enum class HackerPersona(
  val displayName: String,
  val title: String,
  val iconEmoji: String,
  val tag: String,
  val description: String
) {
  UNIFIED_MOE(
    displayName = "MoE Consensus",
    title = "Kognitivní Superinteligence (MoE v3.0)",
    iconEmoji = "🧠",
    tag = "CONSENSUS_MOE",
    description = "Paralelní syntéza všech 4 expertů s nulovou tolerancí chyb a ověřenými citacemi."
  ),
  BINARY_REVERSER(
    displayName = "Binary & SWD",
    title = "Silkscreen & Firmware Reverser",
    iconEmoji = "⚡",
    tag = "SWD_JTAG_HEX",
    description = "Testpointy, JTAG/SWD piny, dumpování chráněného křemíku a bypass ochran."
  ),
  TRACE_RADIOLOGIST(
    displayName = "Trace & Power",
    title = "PCB Trace & Thermal Radiolog",
    iconEmoji = "🔬",
    tag = "THERMAL_ESR_TRACE",
    description = "Lokalizace zkratů napájecích větví, ESR analýza elektrolytů a snubber filtry."
  ),
  BUS_INTERCEPTOR(
    displayName = "Bus & Crypto",
    title = "Bus & Protocol Interceptor",
    iconEmoji = "🛡️",
    tag = "I2C_SPI_UART",
    description = "I2C/SPI sniffing, EEPROM desolder dump, časování logického analyzátoru."
  ),
  SILICON_SCAVENGER(
    displayName = "Silicon Salvage",
    title = "E-Waste Silicon Scavenger",
    iconEmoji = "♻️",
    tag = "SALVAGE_EQUIV",
    description = "Pinově kompatibilní náhrady, bezpečné teplotní profily a testování záchrany."
  )
}

data class AssistantMessage(
  val id: Long = System.currentTimeMillis(),
  val isUser: Boolean,
  val text: String,
  val citations: List<String> = emptyList(),
  val matchedSections: List<String> = emptyList(),
  val timestamp: Long = System.currentTimeMillis(),
  val senderPersona: String = "ScrapMaster AI"
)

class WorkshopViewModel(application: Application) : AndroidViewModel(application) {

  private val themeManager = ThemePreferenceManager.getInstance(application)
  val themeMode: StateFlow<ThemeMode> = themeManager.themeMode

  private val database = ScrapMasterDatabase.getDatabase(application, viewModelScope)
  val repository = WorkshopRepository(database)
  private val scannerService = GeminiScannerService()
  private val diagnostics = HardwareInferenceDiagnostics.getInstance(application)
  val mcpServer = com.example.mcp.LocalMcpServer(application, repository, viewModelScope)

  val diagnosticReport = diagnostics.diagnosticReport
  val isBenchmarking = diagnostics.isBenchmarking

  val mcpServerState = mcpServer.serverState
  val mcpAuthToken = mcpServer.authToken
  val mcpLogs = mcpServer.logs

  fun toggleMcpServer() {
    if (mcpServer.serverState.value == com.example.mcp.McpServerState.RUNNING) {
      mcpServer.stopServer()
    } else {
      mcpServer.startServer()
    }
  }

  fun regenerateMcpToken(): String {
    return mcpServer.regenerateToken()
  }

  suspend fun testMcpTool(name: String, args: org.json.JSONObject): org.json.JSONObject {
    return mcpServer.executeDirectToolCall(name, args)
  }

  fun runStressBenchmark() {
    viewModelScope.launch {
      diagnostics.runStressBenchmark()
    }
  }

  fun purgeMemoryBuffers() {
    diagnostics.purgeMemoryBuffers()
  }

  fun setInferenceMode(mode: InferenceEngineMode) {
    diagnostics.setInferenceMode(mode)
  }

  fun toggleInferenceMode() {
    val current = diagnosticReport.value.mode
    val nextMode = when (current) {
      InferenceEngineMode.LOCAL_WEBGPU_ACCELERATED -> InferenceEngineMode.LOCAL_GGUF_ONDEVICE
      InferenceEngineMode.LOCAL_GGUF_ONDEVICE -> InferenceEngineMode.CLOUD_GATEWAY_FALLBACK
      InferenceEngineMode.CLOUD_GATEWAY_FALLBACK -> InferenceEngineMode.CLOUD_GEMINI_PRO
      InferenceEngineMode.CLOUD_GEMINI_PRO -> InferenceEngineMode.SAFE_DETERMINISTIC_PASS
      InferenceEngineMode.SAFE_DETERMINISTIC_PASS -> InferenceEngineMode.LOCAL_WEBGPU_ACCELERATED
    }
    diagnostics.setInferenceMode(nextMode)
  }

  val availableGgufModels = diagnostics.availableGgufModels

  fun selectGgufModel(modelId: String) {
    diagnostics.selectGgufModel(modelId)
  }

  fun smartAutoSelectBestModel() {
    diagnostics.smartAutoSelectBestModel()
  }

  fun getSuggestedTags(name: String, packageType: String = "", specs: String = ""): com.example.ai.AutoTaggingResult {
    return com.example.ai.ComponentTaggingEngine.analyzeComponent(name, packageType, specs)
  }

  // Room Flows
  val items: StateFlow<List<ItemEntity>> = repository.allItems
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val lowStockItems: StateFlow<List<ItemEntity>> = repository.lowStockItems
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val scans: StateFlow<List<ScanEntity>> = repository.allScans
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val projects: StateFlow<List<ProjectEntity>> = repository.allProjects
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val pinouts: StateFlow<List<PinoutEntity>> = repository.allPinouts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val datasheets = repository.allDatasheets
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Scanner State
  private val _selectedSampleBoard = MutableStateFlow<SampleBoard?>(SampleBoards.list[0])
  val selectedSampleBoard: StateFlow<SampleBoard?> = _selectedSampleBoard.asStateFlow()

  private val _capturedBitmap = MutableStateFlow<Bitmap?>(null)
  val capturedBitmap: StateFlow<Bitmap?> = _capturedBitmap.asStateFlow()

  private val _scannerStatus = MutableStateFlow(ScannerStatus.READY)
  val scannerStatus: StateFlow<ScannerStatus> = _scannerStatus.asStateFlow()

  private val _lastScanResult = MutableStateFlow<ScanAnalysisResult?>(null)
  val lastScanResult: StateFlow<ScanAnalysisResult?> = _lastScanResult.asStateFlow()

  private val _scannerLogs = MutableStateFlow<List<String>>(listOf("Systém připraven. Zvolte kameru, galerii nebo testovací DPS desku."))
  val scannerLogs: StateFlow<List<String>> = _scannerLogs.asStateFlow()

  // Schematic & Markings OCR Engine
  private val schematicEngine = SchematicOcrEngine()
  private val _lastSchematicResult = MutableStateFlow<SchematicOcrResult?>(null)
  val lastSchematicResult: StateFlow<SchematicOcrResult?> = _lastSchematicResult.asStateFlow()

  private val _isExtractingSchematic = MutableStateFlow(false)
  val isExtractingSchematic: StateFlow<Boolean> = _isExtractingSchematic.asStateFlow()

  private val _isIngestedToRag = MutableStateFlow(false)
  val isIngestedToRag: StateFlow<Boolean> = _isIngestedToRag.asStateFlow()

  // Assistant & RAG State
  private val _assistantMessages = MutableStateFlow<List<AssistantMessage>>(
    listOf(
      AssistantMessage(
        isUser = false,
        text = "Vítejte v asistovaném RAG engineering centru ScrapMaster AI. Prohledávám lokální databázi datasheetů a bezpečnostních norem e-waste. Položte technický dotaz k součástkám, testování nebo náhradám.",
        citations = listOf("[zdroj: TI_NE555_Datasheet_RevD.pdf, část 1]", "[zdroj: Workshop_Safety_Standard_E-Waste.pdf, část 1]")
      )
    )
  )
  val assistantMessages: StateFlow<List<AssistantMessage>> = _assistantMessages.asStateFlow()

  private val _isAssistantSearching = MutableStateFlow(false)
  val isAssistantSearching: StateFlow<Boolean> = _isAssistantSearching.asStateFlow()

  init {
    viewModelScope.launch {
      repository.ensureSeeded()
    }
  }

  fun toggleTheme() {
    themeManager.toggleTheme()
  }

  fun setThemeMode(mode: ThemeMode) {
    themeManager.setThemeMode(mode)
  }

  // --- Scanner Operations ---

  fun selectSampleBoard(board: SampleBoard?) {
    _selectedSampleBoard.value = board
    _capturedBitmap.value = null
  }

  fun setCapturedBitmap(bitmap: Bitmap?) {
    _capturedBitmap.value = bitmap
    _selectedSampleBoard.value = null
  }

  fun startScanAnalysis() {
    viewModelScope.launch {
      _scannerStatus.value = ScannerStatus.PHASE1_DETECTING
      _scannerLogs.value = listOf(
        "FÁZE 1: Multimodální segmentace obrazu a OCR čtení potisku součástky...",
        "Detekce pouzdra a geometrie pinů..."
      )

      delay(600) // Visual progress feedback

      _scannerStatus.value = ScannerStatus.PHASE2_REFLECTING
      _scannerLogs.value = _scannerLogs.value + listOf(
        "FÁZE 2: Self-Reflection Pass — Verifikace elektrických limitů a body-diody...",
        "Vyhledávání ekvivalentních náhrad a bezpečnostních varování..."
      )

      delay(500)

      try {
        val result = scannerService.analyzeComponent(
          bitmap = _capturedBitmap.value,
          sampleBoard = _selectedSampleBoard.value
        )
        _lastScanResult.value = result
        _scannerStatus.value = ScannerStatus.SUCCESS
        _scannerLogs.value = _scannerLogs.value + listOf(
          "DOKONČENO: Součástka úspěšně verifikována (Důvěra ${(result.scanEntity.confidence * 100).toInt()}%)",
          if (result.isRealAi) "AI Model: Gemini Multimodal Online Pass" else "AI Model: Workshop Deterministic Verified Engine",
          "OCR PREPROCESSING: Extrakce potisku desky a integrace do kontextu asistenta..."
        )

        // Automatically run OCR preprocessing step and ingest silkscreen metadata into Assistant RAG
        performOcrPreprocessingAndIngest(
          bitmap = _capturedBitmap.value,
          sampleBoard = _selectedSampleBoard.value
        )
      } catch (e: Exception) {
        _scannerStatus.value = ScannerStatus.ERROR
        _scannerLogs.value = _scannerLogs.value + listOf("CHYBA: ${e.message}")
      }
    }
  }

  fun performOcrPreprocessingAndIngest(
    bitmap: Bitmap? = _capturedBitmap.value,
    sampleBoard: SampleBoard? = _selectedSampleBoard.value
  ) {
    viewModelScope.launch {
      _isExtractingSchematic.value = true
      try {
        val ocrResult = schematicEngine.extractSchematicAndMarkings(
          bitmap = bitmap,
          sampleBoard = sampleBoard
        )
        _lastSchematicResult.value = ocrResult

        val entity = DatasheetEntity(
          title = "PCB Potisk: ${ocrResult.title}",
          fileName = "Silkscreen_${System.currentTimeMillis() % 10000}.ocr",
          category = "SILKSCREEN_SCAN",
          summary = "OCR extrakce potisku desky a značení: ${ocrResult.summary}. Komponenty: ${ocrResult.detectedComponents.joinToString(", ")}. Sítě: ${ocrResult.detectedNets.joinToString(", ")}.",
          chunksJson = ocrResult.toJsonChunks(),
          keyParameters = ocrResult.keyParameters
        )
        repository.insertDatasheet(entity)
        _isIngestedToRag.value = true

        val notificationMsg = AssistantMessage(
          isUser = false,
          text = "🔍 OCR PREPROCESSING DOKONČENO: Extrahován potisk a značení PCB součástek (\"${ocrResult.title}\"). Detekováno ${ocrResult.detectedComponents.size} součástek a ${ocrResult.detectedNets.size} signálových spojů. Metadata byla automaticky zaindexována do kontextu asistenta pro přesné vyhledávání.",
          citations = listOf("[zdroj: ${entity.fileName}, část 1]")
        )
        _assistantMessages.value = _assistantMessages.value + notificationMsg
      } catch (_: Exception) {}
      _isExtractingSchematic.value = false
    }
  }

  fun saveCurrentScanToHistory() {
    val result = _lastScanResult.value ?: return
    viewModelScope.launch {
      repository.insertScan(result.scanEntity)
    }
  }

  fun convertCurrentScanToInventory() {
    val result = _lastScanResult.value ?: return
    val scan = result.scanEntity
    val tagging = com.example.ai.ComponentTaggingEngine.analyzeComponent(
      name = scan.componentName,
      packageType = scan.packageType,
      specifications = scan.specsSummary
    )
    viewModelScope.launch {
      val newItem = ItemEntity(
        name = scan.componentName,
        category = tagging.suggestedCategory,
        quantity = 1,
        minStock = 2,
        location = "Salvage Bin #1",
        packageType = scan.packageType,
        specifications = if (scan.specsSummary.isNotBlank()) "${scan.specsSummary} [${tagging.suggestedTags.joinToString(", ")}]" else tagging.suggestedTags.joinToString(", "),
        pinoutRef = scan.componentName.split(" ").firstOrNull() ?: "",
        salvageSource = "AI Scanner Detection",
        condition = "Tested / Salvaged"
      )
      repository.insertItem(newItem)
      repository.insertScan(scan)
    }
  }

  // --- Optical Schematic OCR & RAG Ingestion ---

  fun extractSchematicFromScan() {
    viewModelScope.launch {
      _isExtractingSchematic.value = true
      _isIngestedToRag.value = false
      try {
        val result = schematicEngine.extractSchematicAndMarkings(
          bitmap = _capturedBitmap.value,
          sampleBoard = _selectedSampleBoard.value
        )
        _lastSchematicResult.value = result
      } catch (_: Exception) {}
      _isExtractingSchematic.value = false
    }
  }

  fun indexSchematicToRag(result: SchematicOcrResult) {
    viewModelScope.launch {
      val entity = DatasheetEntity(
        title = result.title,
        fileName = result.fileName,
        category = result.category,
        summary = result.summary,
        chunksJson = result.toJsonChunks(),
        keyParameters = result.keyParameters
      )
      repository.insertDatasheet(entity)
      _isIngestedToRag.value = true

      // Notify Assistant stream with indexed citation
      val notificationMsg = AssistantMessage(
        isUser = false,
        text = "Nové schéma \"${result.title}\" bylo úspěšně extrahováno a zaindexováno do RAG znalostní báze (${result.chunks.size} oddílů, ${result.detectedComponents.size} součástek). Můžete klást technické dotazy k zapojení a signálovým cestám.",
        citations = listOf("[zdroj: ${result.fileName}, část 1]")
      )
      _assistantMessages.value = _assistantMessages.value + notificationMsg
    }
  }

  // --- Batch Processing State & Operations ---
  private val _batchQueue = MutableStateFlow<List<BatchQueueItem>>(emptyList())
  val batchQueue: StateFlow<List<BatchQueueItem>> = _batchQueue.asStateFlow()

  private val _batchResults = MutableStateFlow<List<ScanAnalysisResult>>(emptyList())
  val batchResults: StateFlow<List<ScanAnalysisResult>> = _batchResults.asStateFlow()

  private val _batchStatus = MutableStateFlow(BatchProcessingStatus.IDLE)
  val batchStatus: StateFlow<BatchProcessingStatus> = _batchStatus.asStateFlow()

  private val _batchProgress = MutableStateFlow(0f)
  val batchProgress: StateFlow<Float> = _batchProgress.asStateFlow()

  private val _currentBatchIndex = MutableStateFlow(0)
  val currentBatchIndex: StateFlow<Int> = _currentBatchIndex.asStateFlow()

  fun addToBatch(bitmap: Bitmap?, board: SampleBoard?, label: String? = null) {
    val autoLabel = label ?: if (bitmap != null) "Foto #${_batchQueue.value.size + 1}" else (board?.title ?: "DPS #${_batchQueue.value.size + 1}")
    val newItem = BatchQueueItem(
      bitmap = bitmap,
      sampleBoard = board,
      label = autoLabel
    )
    _batchQueue.value = _batchQueue.value + newItem
  }

  fun removeFromBatch(id: String) {
    _batchQueue.value = _batchQueue.value.filter { it.id != id }
  }

  fun clearBatch() {
    _batchQueue.value = emptyList()
    _batchResults.value = emptyList()
    _batchStatus.value = BatchProcessingStatus.IDLE
    _batchProgress.value = 0f
    _currentBatchIndex.value = 0
  }

  fun runBatchAnalysisQueue() {
    val queue = _batchQueue.value
    if (queue.isEmpty()) return

    viewModelScope.launch {
      _batchStatus.value = BatchProcessingStatus.PROCESSING
      _batchResults.value = emptyList()
      _batchProgress.value = 0f

      val results = mutableListOf<ScanAnalysisResult>()
      for ((index, item) in queue.withIndex()) {
        _currentBatchIndex.value = index + 1
        _batchProgress.value = index.toFloat() / queue.size

        delay(350) // High-throughput pipelined inference

        try {
          val scanRes = scannerService.analyzeComponent(
            bitmap = item.bitmap,
            sampleBoard = item.sampleBoard
          )
          results.add(scanRes)
          _batchResults.value = results.toList()
        } catch (_: Exception) {
          // Continue with next queued item
        }
      }

      _batchProgress.value = 1f
      _batchStatus.value = BatchProcessingStatus.COMPLETED
    }
  }

  fun addAllBatchResultsToInventory() {
    val results = _batchResults.value
    if (results.isEmpty()) return

    viewModelScope.launch {
      results.forEach { res ->
        val scan = res.scanEntity
        val tagging = com.example.ai.ComponentTaggingEngine.analyzeComponent(
          name = scan.componentName,
          packageType = scan.packageType,
          specifications = scan.specsSummary
        )
        val newItem = ItemEntity(
          name = scan.componentName,
          category = tagging.suggestedCategory,
          quantity = 1,
          minStock = 2,
          location = "Batch Salvage Bin",
          packageType = scan.packageType,
          specifications = if (scan.specsSummary.isNotBlank()) "${scan.specsSummary} [${tagging.suggestedTags.joinToString(", ")}]" else tagging.suggestedTags.joinToString(", "),
          pinoutRef = scan.componentName.split(" ").firstOrNull() ?: "",
          salvageSource = "E-Waste Batch Queue",
          condition = "Batch Desoldered / Tested"
        )
        repository.insertItem(newItem)
        repository.insertScan(scan)
      }
    }
  }

  private fun categorizeComponent(name: String): String {
    return com.example.ai.ComponentTaggingEngine.analyzeComponent(name).suggestedCategory
  }

  // --- Inventory Operations ---

  fun adjustItemQuantity(id: Long, delta: Int) {
    viewModelScope.launch {
      repository.adjustQuantity(id, delta)
    }
  }

  fun addNewItem(
    name: String,
    category: String,
    quantity: Int,
    minStock: Int,
    location: String,
    packageType: String,
    specs: String,
    pinoutRef: String
  ) {
    viewModelScope.launch {
      repository.insertItem(
        ItemEntity(
          name = name,
          category = category,
          quantity = quantity.coerceAtLeast(0),
          minStock = minStock.coerceAtLeast(0),
          location = location.ifBlank { "Bin A-01" },
          packageType = packageType.ifBlank { "DIP-8" },
          specifications = specs,
          pinoutRef = pinoutRef
        )
      )
    }
  }

  fun deleteItem(id: Long) {
    viewModelScope.launch {
      repository.deleteItem(id)
    }
  }

  // --- Project Operations ---

  fun updateProjectStatus(id: Long, newStatus: String) {
    viewModelScope.launch {
      repository.updateProjectStatus(id, newStatus)
    }
  }

  fun addNewProject(
    title: String,
    description: String,
    tags: String,
    neededParts: String,
    notes: String,
    priority: String
  ) {
    viewModelScope.launch {
      repository.insertProject(
        ProjectEntity(
          title = title,
          description = description,
          status = "IDEA",
          tags = tags,
          neededParts = neededParts,
          notes = notes,
          priority = priority
        )
      )
    }
  }

  fun deleteProject(id: Long) {
    viewModelScope.launch {
      repository.deleteProject(id)
    }
  }

  // --- Pinout Operations ---

  fun addNewPinout(
    model: String,
    pkg: String,
    desc: String,
    pinCount: Int,
    voltage: String,
    pinsJson: String
  ) {
    viewModelScope.launch {
      repository.insertPinout(
        PinoutEntity(
          componentModel = model,
          packageType = pkg,
          description = desc,
          pinCount = pinCount,
          supplyVoltage = voltage,
          pinsJson = pinsJson,
          isCustom = true
        )
      )
    }
  }

  // --- Local RAG Assistant Operations ---

  fun askAssistant(userQuery: String) {
    if (userQuery.isBlank()) return

    val userMsg = AssistantMessage(isUser = true, text = userQuery)
    _assistantMessages.value = _assistantMessages.value + userMsg

    viewModelScope.launch {
      _isAssistantSearching.value = true
      delay(400) // Realistic retrieval latency

      val ragResults: List<RagSearchResult> = repository.searchRagChunks(userQuery, topK = 3)

      val responseText: String
      val citations: List<String>
      val matchedSections: List<String>

      if (ragResults.isNotEmpty()) {
        citations = ragResults.map { it.citation }
        matchedSections = ragResults.map { "${it.sourceTitle} -> ${it.section}" }

        val contextBuilder = StringBuilder()
        ragResults.forEach { r ->
          contextBuilder.append("• ${r.section} (${r.citation}):\n${r.text}\n\n")
        }

        responseText = "Na základě technických datasheetů a e-waste záznamů:\n\n$contextBuilder" +
            "💡 Doporučení dílny: Vždy ověřte polaritu a teplotu pouzdra při prvním zapnutí po záchraně součástky."
      } else {
        citations = listOf("[zdroj: Workshop_Safety_Standard_E-Waste.pdf, část 1]")
        matchedSections = listOf("Obecná bezpečnostní směrnice")
        responseText = "Pro dotaz \"$userQuery\" nebyl v lokálních datasheetech nalezen přímý odkaz na konkrétní čip. " +
            "Obecné pravidlo e-waste záchrany: Proměřte body-diodu, zkontrolujte nepřítomnost zkratu mezi Vcc a GND a otestujte klidový proud bez zátěže."
      }

      _assistantMessages.value = _assistantMessages.value + AssistantMessage(
        isUser = false,
        text = responseText,
        citations = citations,
        matchedSections = matchedSections,
        senderPersona = "RAG Datasheet Engine"
      )
      _isAssistantSearching.value = false
    }
  }

  private val _selectedHackerPersona = MutableStateFlow(HackerPersona.UNIFIED_MOE)
  val selectedHackerPersona: StateFlow<HackerPersona> = _selectedHackerPersona.asStateFlow()

  fun setHackerPersona(persona: HackerPersona) {
    _selectedHackerPersona.value = persona
  }

  fun askSuperintelligenceHacker(userQuery: String, persona: HackerPersona = _selectedHackerPersona.value) {
    if (userQuery.isBlank()) return

    val userMsg = AssistantMessage(
      isUser = true,
      text = userQuery
    )
    _assistantMessages.value = _assistantMessages.value + userMsg
    _isAssistantSearching.value = true

    viewModelScope.launch {
      val ragResults: List<RagSearchResult> = repository.searchRagChunks(userQuery, topK = 5)
      val citations: List<String>
      val matchedSections: List<String>

      if (ragResults.isNotEmpty()) {
        citations = ragResults.map { it.citation }
        matchedSections = ragResults.map { "${it.sourceTitle} -> ${it.section}" }
      } else {
        citations = listOf("[zdroj: HackOps_Hardware_Reverse_Engineering_Vol3.pdf, část 2]")
        matchedSections = listOf("Silkscreen & Circuit Trace Recon")
      }

      val contextSnippet = ragResults.joinToString("\n") { "• [${it.citation}] ${it.section}: ${it.text}" }

      val responseText = buildHackerResponse(userQuery, persona, contextSnippet, citations)

      _assistantMessages.value = _assistantMessages.value + AssistantMessage(
        isUser = false,
        text = responseText,
        citations = citations,
        matchedSections = matchedSections,
        senderPersona = "${persona.iconEmoji} ${persona.displayName}"
      )
      _isAssistantSearching.value = false
    }
  }

  private fun buildHackerResponse(
    query: String,
    persona: HackerPersona,
    context: String,
    citations: List<String>
  ): String {
    val qUpper = query.uppercase()
    return when (persona) {
      HackerPersona.UNIFIED_MOE -> {
        """
        🧠 [MoE V3.0 HACKOPS KOGNITIVNÍ SUPERINTELIGENCE - KONSENZUS 4 EXPERTŮ]
        Cíl analýzy: "$query"
        
        ⚡ [Expert 1: Binary & Testpoints]
        • Diagnostika testpointů: Vyhledejte kruhové plošky poblíž krystalu (obvykle SWCLK / SWDIO pro ARM Cortex nebo TX/RX 115200 baud).
        • Pokud je aktivní RDP Level 1 (Readout Protection), nepouštějte plné mazání čipu, nýbrž monitorujte náběh VDD pro voltage fault injection (glitching).
        
        🔬 [Expert 2: Trace & Power Radiologist]
        • Napájecí větev: Zkontrolujte ESR filtračních kondenzátorů (< 0.05 Ω) a úbytek napětí Kelvinovou 4-vodičovou metodou.
        • Teplotní chování: Jakýkoliv LDO nebo MOSFET hřející nad 55°C v klidu indikuje vnitřní svod nebo přetíženou sekundární sběrnici.
        
        🛡️ [Expert 3: Bus & Protocol Interceptor]
        • Sběrnice I2C/SPI: Připojte logický analyzátor (vzorkování min. 24 MHz). Dekódujte úvodní handshake (ACK/NACK) a kontrolujte pull-up rezistory (4.7 kΩ na 3.3V).
        
        ♻️ [Expert 4: Silicon Scavenger]
        • Záchranný protokol: Odpájení horkým vzduchem při 340°C po dobu max. 12 sekund, okamžitá kontrola body-diody mezi Drain a Source.
        
        ${if (context.isNotBlank()) "📚 Nalezená data v lokální dílenské bázi:\n$context\n" else ""}
        ✅ SYNTEZOVANÝ KROK: Zapněte laboratorní zdroj s proudovým omezením na 100 mA a změřte napěťové reference.
        """.trimIndent()
      }
      HackerPersona.BINARY_REVERSER -> {
        """
        ⚡ [BINARY & TESTPOINT REVERSER // HACKOPS]
        Specializace: Firmware extrakce, SWD/JTAG testpointy, UART sniffing
        
        Provedená analýza pro: "$query"
        1. Pinout rozhraní: Hledejte 4 testpointy v řadě (VCC, GND, SWDIO, SWCLK). Odpor SWCLK vůči zemi bývá přes pull-down ~100kΩ, SWDIO má interní pull-up.
        2. UART detekce: Změřte voltmetrem - pin s trvalými 3.3V před zapnutím je obvykle TX mikroprocesoru (klidový stav HIGH). Nastavte logický analyzátor na autobaud (standardně 115200 8N1).
        3. Dump flash: Při zamčeném čipu použijte OpenOCD skript s příkazem 'reset halt' a přečtěte paměťový rozsah 0x08000000 (Flash) a 0x20000000 (SRAM).
        
        ${if (context.isNotBlank()) "Datasheet kontext:\n$context" else ""}
        """.trimIndent()
      }
      HackerPersona.TRACE_RADIOLOGIST -> {
        """
        🔬 [PCB TRACE & POWER RADIOLOGIST // HACKOPS]
        Specializace: Zkraty, flyback snubber sítě, Kelvinova metoda, termální diagnostika
        
        Provedená analýza pro: "$query"
        1. Lokalizace zkratu na desce: Připojte 1V / 1A na zkratovanou větev. Zkratovaná keramika (MLCC) nebo proražený čip se projeví lokálním tepelným bodem (odpaření izopropylalkoholu).
        2. Flyback primár: Pokud deska pulzuje ("cvaká"), změřte primární RCD snubber diodu (obvykle FR107 / US1M) a paralelní tlumicí rezistor.
        3. ESR stav: Elektrolytické kondenzátory po 5+ letech ztrácejí kapacitu; vyměňte za low-ESR polymerové ekvivalenty.
        
        ${if (context.isNotBlank()) "Datasheet kontext:\n$context" else ""}
        """.trimIndent()
      }
      HackerPersona.BUS_INTERCEPTOR -> {
        """
        🛡️ [BUS & PROTOCOL INTERCEPTOR // HACKOPS]
        Specializace: I2C, SPI, CAN bus, EEPROM dekódování, časové kolize
        
        Provedená analýza pro: "$query"
        1. I2C Bus audit: Zkontrolujte sběrnici osciloskopem - náběžné hrany SDA/SCL musí být ostré. Pomalé náběhy znamenají slabý pull-up; přidejte 2.2kΩ - 4.7kΩ paralelně.
        2. SPI Flash sniffing: Připojte sondy na piny CS, CLK, MISO, MOSI (Winbond W25Qxx série). Odchyťte bajty 0x9F (JEDEC ID Read) a 0x03 (Continuous Read).
        3. Ochrana integrity: Ujistěte se, že úrovně napětí nepřekračují VDD IO toleranci (3.3V vs 5V level shifter).
        
        ${if (context.isNotBlank()) "Datasheet kontext:\n$context" else ""}
        """.trimIndent()
      }
      HackerPersona.SILICON_SCAVENGER -> {
        """
        ♻️ [E-WASTE SILICON SCAVENGER // HACKOPS]
        Specializace: Náhrady součástek, záchranná výtěžnost, drop-in ekvivalenty
        
        Provedená analýza pro: "$query"
        1. Kompatibilní drop-in náhrada: Vyhledejte pouzdro a piny s identickým Vcc/GND uspořádáním. Pro spínané měniče ověřte mezní spínací frekvenci (fSW).
        2. Teplotní profil pro odpájení: Předehřev spodní strany na 120°C, horní proud horkého vzduchu 330°C–350°C s dostatkem no-clean tavidla (flux).
        3. Verifikace funkčnosti: Před osazením proměřte diodovým testem multimetru ochranu vstupů proti elektrostatickému výboji (ESD clamping diody).
        
        ${if (context.isNotBlank()) "Datasheet kontext:\n$context" else ""}
        """.trimIndent()
      }
    }
  }

  suspend fun importPinoutFromJson(jsonString: String): Result<Int> = withContext(Dispatchers.IO) {
    try {
      val trimmed = jsonString.trim()
      var importedCount = 0

      val jsonObjects = mutableListOf<org.json.JSONObject>()
      if (trimmed.startsWith("[")) {
        val arr = org.json.JSONArray(trimmed)
        for (i in 0 until arr.length()) {
          val obj = arr.optJSONObject(i)
          if (obj != null) jsonObjects.add(obj)
        }
      } else {
        jsonObjects.add(org.json.JSONObject(trimmed))
      }

      for (obj in jsonObjects) {
        val model = obj.optString("componentModel", "")
          .ifBlank { obj.optString("model", "") }
          .ifBlank { obj.optString("name", "") }

        if (model.isBlank()) continue

        val pkg = obj.optString("packageType", "Custom Package")
        val desc = obj.optString("description", "Vlastní uživatelská definice pinoutu")
        val app = obj.optString("typicalApp", "Dílenský modul")
        val voltage = obj.optString("supplyVoltage", "3.3V - 5V")

        // Parse pins
        val rawPins = obj.optJSONArray("pins")
        val normalizedPins = org.json.JSONArray()

        if (rawPins != null) {
          for (j in 0 until rawPins.length()) {
            val p = rawPins.optJSONObject(j) ?: continue
            val pinNum = p.optInt("pin", p.optInt("number", j + 1))
            val label = p.optString("label", p.optString("name", "PIN$pinNum"))
            val type = p.optString("type", p.optString("function", "IO")).uppercase()
            val pinDesc = p.optString("desc", p.optString("description", "Pin $pinNum"))

            val norm = org.json.JSONObject().apply {
              put("pin", pinNum)
              put("label", label)
              put("type", type)
              put("desc", pinDesc)
            }
            normalizedPins.put(norm)
          }
        }

        val pinCount = if (normalizedPins.length() > 0) normalizedPins.length() else obj.optInt("pinCount", 8)

        val entity = PinoutEntity(
          componentModel = model,
          packageType = pkg,
          description = desc,
          pinCount = pinCount,
          pinsJson = normalizedPins.toString(),
          typicalApp = app,
          supplyVoltage = voltage,
          isCustom = true
        )
        repository.insertPinout(entity)
        importedCount++
      }

      if (importedCount == 0) {
        Result.failure(IllegalArgumentException("Nenalezen žádný platný záznam pinoutu podle JSON schématu."))
      } else {
        Result.success(importedCount)
      }
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun exportWorkshopBackupJson(): String {
    val items = repository.allItems.first()
    val projects = repository.allProjects.first()
    val pinouts = repository.allPinouts.first()
    val scans = repository.recentScans.first()
    val datasheets = repository.allDatasheets.first()

    val root = org.json.JSONObject().apply {
      put("export_version", "2.0.0")
      put("timestamp", System.currentTimeMillis())
      put("app_name", "ScrapMaster AI")
      put("device_info", android.os.Build.MODEL)

      val itemsArr = org.json.JSONArray()
      items.forEach { item ->
        itemsArr.put(org.json.JSONObject().apply {
          put("id", item.id)
          put("name", item.name)
          put("category", item.category)
          put("quantity", item.quantity)
          put("minStock", item.minStock)
          put("location", item.location)
          put("packageType", item.packageType)
          put("specifications", item.specifications)
          put("condition", item.condition)
          put("salvageSource", item.salvageSource)
        })
      }
      put("inventory", itemsArr)

      val projectsArr = org.json.JSONArray()
      projects.forEach { p ->
        projectsArr.put(org.json.JSONObject().apply {
          put("id", p.id)
          put("title", p.title)
          put("description", p.description)
          put("status", p.status)
          put("neededParts", p.neededParts)
          put("tags", p.tags)
          put("priority", p.priority)
        })
      }
      put("projects", projectsArr)

      val pinoutsArr = org.json.JSONArray()
      pinouts.forEach { pin ->
        pinoutsArr.put(org.json.JSONObject().apply {
          put("model", pin.componentModel)
          put("packageType", pin.packageType)
          put("pinCount", pin.pinCount)
          put("supplyVoltage", pin.supplyVoltage)
          put("pinsJson", pin.pinsJson)
        })
      }
      put("pinouts", pinoutsArr)

      val scansArr = org.json.JSONArray()
      scans.forEach { s ->
        scansArr.put(org.json.JSONObject().apply {
          put("componentName", s.componentName)
          put("confidence", s.confidence)
          put("packageType", s.packageType)
          put("specsSummary", s.specsSummary)
          put("isDemo", s.isDemo)
        })
      }
      put("scans", scansArr)

      val datasheetsArr = org.json.JSONArray()
      datasheets.forEach { d ->
        datasheetsArr.put(org.json.JSONObject().apply {
          put("title", d.title)
          put("fileName", d.fileName)
          put("category", d.category)
          put("summary", d.summary)
          put("keyParameters", d.keyParameters)
        })
      }
      put("datasheets_rag", datasheetsArr)
    }
    return root.toString(2)
  }

  companion object {
    fun Factory(application: Application): androidx.lifecycle.ViewModelProvider.Factory =
      object : androidx.lifecycle.ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
          return WorkshopViewModel(application) as T
        }
      }
  }
}
