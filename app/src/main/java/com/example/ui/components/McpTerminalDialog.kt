package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mcp.McpServerState
import com.example.ui.theme.LocalScrapMasterColors
import com.example.ui.theme.TechMonoFamily
import com.example.viewmodel.WorkshopViewModel
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun McpTerminalDialog(
  viewModel: WorkshopViewModel,
  onDismiss: () -> Unit
) {
  val colors = LocalScrapMasterColors.current
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  val serverState by viewModel.mcpServerState.collectAsStateWithLifecycle()
  val authToken by viewModel.mcpAuthToken.collectAsStateWithLifecycle()

  var copiedFeedback by remember { mutableStateOf(false) }
  var selectedTool by remember { mutableStateOf("search_inventory") }
  var testParam by remember { mutableStateOf("555") }
  var testOutput by remember { mutableStateOf<String?>(null) }
  var backupJsonOutput by remember { mutableStateOf<String?>(null) }

  val isRunning = serverState == McpServerState.RUNNING

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
            imageVector = Icons.Default.Cable,
            contentDescription = null,
            tint = colors.primaryAccent,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "MCP SERVER & INTEGRACE",
            fontFamily = TechMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = colors.primaryAccent
          )
        }
        IndustrialBadge(
          text = if (isRunning) "RUNNING :8765" else "STOPPED",
          color = if (isRunning) colors.secondaryAccent else colors.alertAccent
        )
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Status & Toggle Row
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant)
            .padding(10.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Model Context Protocol Endpoint:",
                fontFamily = TechMonoFamily,
                fontSize = 10.sp,
                color = colors.textMuted
              )
              Text(
                text = "http://localhost:8765/mcp",
                fontFamily = TechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = if (isRunning) colors.secondaryAccent else colors.textPrimary
              )
            }

            Switch(
              checked = isRunning,
              onCheckedChange = { viewModel.toggleMcpServer() },
              colors = SwitchDefaults.colors(
                checkedThumbColor = colors.secondaryAccent,
                checkedTrackColor = colors.secondaryAccent.copy(alpha = 0.3f)
              )
            )
          }
        }

        // Bearer Token Section
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "STATICKÝ HLAVIČKOVÝ TOKEN:",
                fontFamily = TechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
                color = colors.alertAccent
              )

              Row {
                Text(
                  text = "Přegenerovat",
                  fontFamily = TechMonoFamily,
                  fontSize = 9.5.sp,
                  color = colors.primaryAccent,
                  modifier = Modifier.clickable {
                    viewModel.regenerateMcpToken()
                  }
                )
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Bearer $authToken",
                fontFamily = TechMonoFamily,
                fontSize = 10.sp,
                color = colors.textSecondary,
                maxLines = 1,
                modifier = Modifier.weight(1f)
              )

              Box(
                modifier = Modifier
                  .size(26.dp)
                  .clip(RoundedCornerShape(4.dp))
                  .background(colors.surfaceVariant)
                  .clickable {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("MCP Token", "Bearer $authToken"))
                    copiedFeedback = true
                  },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (copiedFeedback) Icons.Default.Check else Icons.Default.ContentCopy,
                  contentDescription = "Kopírovat",
                  tint = if (copiedFeedback) colors.secondaryAccent else colors.textMuted,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }
        }

        // Enabled MCP Tools list
        Text(
          text = "POVOLENÉ MCP NÁSTROJE:",
          fontFamily = TechMonoFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 9.5.sp,
          color = colors.secondaryAccent
        )

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          listOf("search_inventory", "adjust_stock", "list_projects", "lookup_pinout", "query_rag").forEach { tool ->
            val isSel = tool == selectedTool
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isSel) colors.primaryAccent.copy(alpha = 0.2f) else colors.surfaceVariant)
                .border(0.8.dp, if (isSel) colors.primaryAccent else colors.cardBorder, RoundedCornerShape(6.dp))
                .clickable { selectedTool = tool }
                .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
              Text(
                text = tool,
                fontFamily = TechMonoFamily,
                fontSize = 8.5.sp,
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                color = if (isSel) colors.primaryAccent else colors.textMuted
              )
            }
          }
        }

        // Test tool console
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedTextField(
            value = testParam,
            onValueChange = { testParam = it },
            placeholder = { Text("Parametr (např. dotaz)", fontFamily = TechMonoFamily, fontSize = 10.sp) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
              focusedTextColor = colors.textPrimary,
              unfocusedTextColor = colors.textPrimary,
              focusedContainerColor = colors.surfaceVariant,
              unfocusedContainerColor = colors.surfaceVariant
            )
          )

          NeonButton(
            text = "Test",
            onClick = {
              scope.launch {
                val args = JSONObject()
                if (selectedTool == "search_inventory" || selectedTool == "query_rag") {
                  args.put("query", testParam)
                } else if (selectedTool == "lookup_pinout") {
                  args.put("model", testParam)
                } else if (selectedTool == "adjust_stock") {
                  args.put("itemId", 1L)
                  args.put("delta", testParam.toIntOrNull() ?: 1)
                }
                val res = viewModel.testMcpTool(selectedTool, args)
                testOutput = res.toString(2)
              }
            },
            icon = Icons.Default.PlayArrow,
            color = colors.primaryAccent
          )
        }

        testOutput?.let { out ->
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0xFF040810))
              .border(0.8.dp, colors.primaryAccent.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
              .padding(8.dp)
          ) {
            Text(
              text = out,
              fontFamily = TechMonoFamily,
              fontSize = 9.sp,
              color = colors.secondaryAccent,
              maxLines = 6
            )
          }
        }

        // Security note
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Security,
            contentDescription = null,
            tint = colors.secondaryAccent,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "MCP odmítá neautorizované požadavky (401/403). Žádný únik dat.",
            fontFamily = TechMonoFamily,
            fontSize = 9.sp,
            color = colors.textMuted
          )
        }

        // Export & Backup Section
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "EXPORT WORKSHOP ZÁLOHY (JSON)",
                fontFamily = TechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
                color = colors.primaryAccent
              )

              Text(
                text = if (backupJsonOutput != null) "✓ Zkopírováno!" else "Vygenerovat zálohu",
                fontFamily = TechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 9.5.sp,
                color = colors.secondaryAccent,
                modifier = Modifier.clickable {
                  scope.launch {
                    val json = viewModel.exportWorkshopBackupJson()
                    backupJsonOutput = json
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("ScrapMaster Backup", json))
                  }
                }
              )
            }

            backupJsonOutput?.let { json ->
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "✓ Kompletní JSON databáze (inventář, projekty, pinouty, RAG baze) byla zkopírována do schránky (${json.length} B).",
                fontFamily = TechMonoFamily,
                fontSize = 9.sp,
                color = colors.secondaryAccent
              )
            }
          }
        }
      }
    },
    confirmButton = {
      NeonButton(
        text = "ZAVŘÍT",
        onClick = onDismiss,
        color = colors.primaryAccent
      )
    },
    containerColor = colors.surface,
    shape = RoundedCornerShape(16.dp)
  )
}
