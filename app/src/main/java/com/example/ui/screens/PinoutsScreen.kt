package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.PinoutEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.IndustrialBadge
import com.example.ui.components.NeonButton
import com.example.ui.components.PinoutVisualizer
import com.example.ui.theme.LocalScrapMasterColors
import com.example.ui.theme.TechMonoFamily
import com.example.viewmodel.WorkshopViewModel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@Composable
fun PinoutsScreen(
  viewModel: WorkshopViewModel,
  modifier: Modifier = Modifier
) {
  val colors = LocalScrapMasterColors.current
  val pinouts by viewModel.pinouts.collectAsStateWithLifecycle()
  val scope = rememberCoroutineScope()

  var searchQuery by remember { mutableStateOf("") }
  var showAddDialog by remember { mutableStateOf(false) }
  var showImportJsonDialog by remember { mutableStateOf(false) }
  var importSuccessMsg by remember { mutableStateOf<String?>(null) }

  val filteredPinouts = pinouts.filter { p ->
    searchQuery.isBlank() ||
        p.componentModel.contains(searchQuery, ignoreCase = true) ||
        p.description.contains(searchQuery, ignoreCase = true) ||
        p.packageType.contains(searchQuery, ignoreCase = true)
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "KATALOG PINOUTŮ",
            fontFamily = TechMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = colors.textPrimary,
            letterSpacing = 0.5.sp
          )
          Text(
            text = "${pinouts.size} definic součástek • Interaktivní pinouty & JSON import",
            fontFamily = TechMonoFamily,
            fontSize = 10.5.sp,
            color = colors.textMuted
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          NeonButton(
            text = "IMPORT JSON",
            onClick = { showImportJsonDialog = true },
            icon = Icons.Default.UploadFile,
            color = colors.secondaryAccent,
            testTag = "pinouts_import_json_btn"
          )

          NeonButton(
            text = "+ Vlastní",
            onClick = { showAddDialog = true },
            icon = Icons.Default.Add,
            testTag = "pinouts_add_btn"
          )
        }
      }
    }

    if (importSuccessMsg != null) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.secondaryAccent.copy(alpha = 0.15f))
            .border(1.dp, colors.secondaryAccent, RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.CheckCircle,
              contentDescription = null,
              tint = colors.secondaryAccent,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = importSuccessMsg ?: "",
              fontFamily = TechMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = colors.secondaryAccent
            )
          }
        }
      }
    }

    // Search bar
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = {
          Text(
            text = "Hledat model IC (NE555, LM7805, ESP32, DRV8825, IRF)...",
            fontFamily = TechMonoFamily,
            fontSize = 12.sp,
            color = colors.textMuted
          )
        },
        leadingIcon = {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = colors.primaryAccent
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .testTag("pinouts_search_input"),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = colors.primaryAccent,
          unfocusedBorderColor = colors.cardBorder,
          focusedTextColor = colors.textPrimary,
          unfocusedTextColor = colors.textPrimary
        ),
        singleLine = true
      )
    }

    // Pinouts list
    items(filteredPinouts, key = { it.id }) { pinout ->
      CyberCard(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("pinout_card_${pinout.id}")
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          // Model & Package Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Hub,
                contentDescription = null,
                tint = colors.primaryAccent,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = pinout.componentModel,
                fontFamily = TechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = colors.primaryAccent
              )
              if (pinout.isCustom) {
                Spacer(modifier = Modifier.width(6.dp))
                IndustrialBadge(text = "CUSTOM JSON", color = colors.secondaryAccent)
              }
            }

            IndustrialBadge(text = pinout.packageType)
          }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = pinout.description,
            fontFamily = TechMonoFamily,
            fontSize = 11.5.sp,
            color = colors.textPrimary
          )

          Spacer(modifier = Modifier.height(8.dp))

          // Pinout Visualizer
          PinoutVisualizer(pinout = pinout)

          Spacer(modifier = Modifier.height(8.dp))

          // Application & Voltage info
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = "Napájení: ${pinout.supplyVoltage}",
              fontFamily = TechMonoFamily,
              fontSize = 10.5.sp,
              color = colors.secondaryAccent
            )
            Text(
              text = pinout.typicalApp,
              fontFamily = TechMonoFamily,
              fontSize = 10.5.sp,
              color = colors.textMuted
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }

  if (showAddDialog) {
    AddPinoutDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { model, pkg, desc, count, volt, pinsJson ->
        viewModel.addNewPinout(model, pkg, desc, count, volt, pinsJson)
        showAddDialog = false
      }
    )
  }

  if (showImportJsonDialog) {
    ImportPinoutJsonDialog(
      onDismiss = { showImportJsonDialog = false },
      onImport = { jsonStr ->
        scope.launch {
          val res = viewModel.importPinoutFromJson(jsonStr)
          if (res.isSuccess) {
            importSuccessMsg = "Úspěšně importováno ${res.getOrNull()} definic pinoutů do báze!"
            showImportJsonDialog = false
          }
        }
      }
    )
  }
}

@Composable
private fun ImportPinoutJsonDialog(
  onDismiss: () -> Unit,
  onImport: (String) -> Unit
) {
  val colors = LocalScrapMasterColors.current
  val context = LocalContext.current
  var jsonText by remember { mutableStateOf("") }
  var errorText by remember { mutableStateOf<String?>(null) }

  // File Picker for JSON files
  val filePicker = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        val stream = context.contentResolver.openInputStream(uri)
        if (stream != null) {
          val reader = BufferedReader(InputStreamReader(stream))
          jsonText = reader.readText()
          reader.close()
          errorText = null
        }
      } catch (e: Exception) {
        errorText = "Chyba při čtení souboru: ${e.message}"
      }
    }
  }

  val sampleEsp32 = """
  {
    "componentModel": "ESP32-S3-WROOM-1",
    "packageType": "SMD Module 44-pin",
    "description": "Dual-Core Xtensa LX7 MCU s Wi-Fi a Bluetooth 5 (LE) pro Edge AI",
    "typicalApp": "IoT & Kognitivní senzory",
    "supplyVoltage": "3.0V - 3.6V",
    "pins": [
      {"pin": 1, "label": "GND", "type": "POWER", "desc": "Zemnící plocha"},
      {"pin": 2, "label": "3V3", "type": "POWER", "desc": "Napájení 3.3V"},
      {"pin": 3, "label": "EN", "type": "RESET", "desc": "Aktivace čipu (Enable)"},
      {"pin": 4, "label": "IO4", "type": "IO", "desc": "GPIO4 / ADC1_CH3"},
      {"pin": 5, "label": "IO5", "type": "IO", "desc": "GPIO5 / ADC1_CH4"},
      {"pin": 6, "label": "IO6", "type": "IO", "desc": "GPIO6 / JTAG_MTCK"},
      {"pin": 7, "label": "IO7", "type": "IO", "desc": "GPIO7 / JTAG_MTDO"},
      {"pin": 8, "label": "IO15", "type": "IO", "desc": "GPIO15 / XTAL_32K_P"}
    ]
  }
  """.trimIndent()

  val sampleDrv8825 = """
  {
    "componentModel": "DRV8825",
    "packageType": "HTSSOP-28",
    "description": "Krokový motor driver s regulací proudu a mikrokrokováním 1/32",
    "typicalApp": "3D tiskárny a CNC desky",
    "supplyVoltage": "8.2V - 45V",
    "pins": [
      {"pin": 1, "label": "CP1", "type": "POWER", "desc": "Charge pump cap 1"},
      {"pin": 2, "label": "CP2", "type": "POWER", "desc": "Charge pump cap 2"},
      {"pin": 3, "label": "VCP", "type": "POWER", "desc": "Charge pump reservoir"},
      {"pin": 4, "label": "VMA", "type": "POWER", "desc": "Napájení motoru větev A"},
      {"pin": 5, "label": "AOUT1", "type": "OUTPUT", "desc": "Výstup vinutí A1"},
      {"pin": 6, "label": "ISENA", "type": "ANALOG", "desc": "Snímací rezistor proudu A"},
      {"pin": 7, "label": "AOUT2", "type": "OUTPUT", "desc": "Výstup vinutí A2"},
      {"pin": 8, "label": "GND", "type": "POWER", "desc": "Výkonová zem"}
    ]
  }
  """.trimIndent()

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.DataObject,
          contentDescription = null,
          tint = colors.primaryAccent,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "IMPORT DEFINICE PINOUTŮ Z JSON",
          fontFamily = TechMonoFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = colors.primaryAccent
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Text(
          text = "Importujte vlastní schémata pinů podle JSON specifikace nebo načtěte soubor z úložiště:",
          fontFamily = TechMonoFamily,
          fontSize = 11.sp,
          color = colors.textMuted
        )

        // Template and File Picker shortcuts
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          NeonButton(
            text = "NAČÍST .JSON",
            onClick = { filePicker.launch("application/json") },
            icon = Icons.Default.FileOpen,
            color = colors.primaryAccent,
            modifier = Modifier.weight(1f)
          )

          NeonButton(
            text = "VZOR ESP32",
            onClick = {
              jsonText = sampleEsp32
              errorText = null
            },
            color = colors.secondaryAccent,
            modifier = Modifier.weight(1f)
          )

          NeonButton(
            text = "VZOR DRV8825",
            onClick = {
              jsonText = sampleDrv8825
              errorText = null
            },
            color = colors.alertAccent,
            modifier = Modifier.weight(1f)
          )
        }

        OutlinedTextField(
          value = jsonText,
          onValueChange = {
            jsonText = it
            errorText = null
          },
          label = { Text("JSON obsah (objekt nebo pole objektů)", fontFamily = TechMonoFamily, fontSize = 10.sp) },
          placeholder = { Text("{\"componentModel\": \"...\", \"pins\": [...]}", fontFamily = TechMonoFamily, fontSize = 10.sp) },
          modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .testTag("pinout_json_input"),
          textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = TechMonoFamily,
            fontSize = 11.sp
          ),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primaryAccent,
            unfocusedBorderColor = colors.cardBorder,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary
          )
        )

        if (errorText != null) {
          Text(
            text = errorText ?: "",
            fontFamily = TechMonoFamily,
            fontSize = 10.5.sp,
            color = colors.alertAccent
          )
        }
      }
    },
    confirmButton = {
      NeonButton(
        text = "ULOŽIT DO PINOUTŮ",
        onClick = {
          if (jsonText.isBlank()) {
            errorText = "Zadejte nebo vložte platný JSON řetězec."
          } else {
            onImport(jsonText)
          }
        },
        color = colors.primaryAccent,
        testTag = "confirm_import_pinout_json_btn"
      )
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("ZRUŠIT", fontFamily = TechMonoFamily, color = colors.textMuted)
      }
    },
    containerColor = colors.surface,
    shape = RoundedCornerShape(16.dp)
  )
}

@Composable
private fun AddPinoutDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String, String, Int, String, String) -> Unit
) {
  val colors = LocalScrapMasterColors.current

  var model by remember { mutableStateOf("") }
  var packageType by remember { mutableStateOf("DIP-8") }
  var description by remember { mutableStateOf("") }
  var pinCount by remember { mutableStateOf("8") }
  var voltage by remember { mutableStateOf("5V - 12V") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "PŘIDAT VLASTNÍ PINOUT",
        fontFamily = TechMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = colors.primaryAccent
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = model,
          onValueChange = { model = it },
          label = { Text("Model IC (např. LM358)", fontFamily = TechMonoFamily, fontSize = 11.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = packageType,
            onValueChange = { packageType = it },
            label = { Text("Pouzdro", fontFamily = TechMonoFamily, fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = pinCount,
            onValueChange = { pinCount = it },
            label = { Text("Počet pinů", fontFamily = TechMonoFamily, fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }
        OutlinedTextField(
          value = voltage,
          onValueChange = { voltage = it },
          label = { Text("Napájecí napětí", fontFamily = TechMonoFamily, fontSize = 11.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Popis obvodu", fontFamily = TechMonoFamily, fontSize = 11.sp) },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          if (model.isNotBlank()) {
            val count = pinCount.toIntOrNull() ?: 8
            val pinsBuilder = StringBuilder("[")
            for (i in 1..count) {
              pinsBuilder.append("{\"pin\":$i,\"label\":\"P$i\",\"type\":\"IO\",\"desc\":\"Pin $i\"}")
              if (i < count) pinsBuilder.append(",")
            }
            pinsBuilder.append("]")

            onConfirm(model, packageType, description, count, voltage, pinsBuilder.toString())
          }
        }
      ) {
        Text("ULOŽIT", fontFamily = TechMonoFamily, fontWeight = FontWeight.Bold, color = colors.primaryAccent)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("ZRUŠIT", fontFamily = TechMonoFamily, color = colors.textMuted)
      }
    },
    containerColor = colors.surface,
    shape = RoundedCornerShape(14.dp)
  )
}
