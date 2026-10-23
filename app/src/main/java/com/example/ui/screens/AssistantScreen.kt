package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CyberCard
import com.example.ui.components.IndustrialBadge
import com.example.ui.theme.LocalScrapMasterColors
import com.example.ui.theme.TechMonoFamily
import com.example.viewmodel.AssistantMessage
import com.example.viewmodel.HackerPersona
import com.example.viewmodel.WorkshopViewModel

@Composable
fun AssistantScreen(
  viewModel: WorkshopViewModel,
  modifier: Modifier = Modifier
) {
  val colors = LocalScrapMasterColors.current

  val messages by viewModel.assistantMessages.collectAsStateWithLifecycle()
  val isSearching by viewModel.isAssistantSearching.collectAsStateWithLifecycle()
  val datasheets by viewModel.datasheets.collectAsStateWithLifecycle()
  val selectedPersona by viewModel.selectedHackerPersona.collectAsStateWithLifecycle()

  var inputQuery by remember { mutableStateOf("") }
  var showPersonaDetails by remember { mutableStateOf(false) }

  val superintelligencePrompts = listOf(
    "🧠 Plný MoE Audit: reverzní analýza neznámé DPS",
    "🔍 Prohledej OCR potisk a značení zachycené desky",
    "⚡ Lokalizuj SWD/JTAG testpointy a bypass ochran",
    "🔬 Detekce zkratu na 3.3V větvi (Kelvin 4-wire)",
    "🛡️ I2C/SPI Sniffing a extrakce EEPROM dat",
    "♻️ Najdi drop-in náhradu za spálený MOSFET",
    "Jak otestovat body-diodu tranzistoru?",
    "Bezpečné vybití VN kondenzátoru v SMPS"
  )

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp)
  ) {
    Spacer(modifier = Modifier.height(4.dp))

    // Header & Superintelligence MoE Status
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Psychology,
            contentDescription = null,
            tint = colors.primaryAccent,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "HACKOPS SUPERINTELLIGENCE",
            fontFamily = TechMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = colors.textPrimary,
            letterSpacing = 0.5.sp
          )
        }
        Text(
          text = "Smart Mixture-of-Hackers v3.0 & RAG Datasheet Citace",
          fontFamily = TechMonoFamily,
          fontSize = 10.5.sp,
          color = colors.textMuted
        )
      }

      IndustrialBadge(
        text = "${datasheets.size} DOKUMENTŮ",
        color = colors.primaryAccent,
        icon = Icons.AutoMirrored.Filled.MenuBook
      )
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Smart Mixture-of-Hackers Persona Selector
    Text(
      text = "AKTIVNÍ EXPERTNÍ AGENT (SMART MIXTURE-OF-HACKERS):",
      fontFamily = TechMonoFamily,
      fontSize = 9.5.sp,
      fontWeight = FontWeight.Bold,
      color = colors.secondaryAccent,
      letterSpacing = 0.5.sp
    )
    Spacer(modifier = Modifier.height(4.dp))

    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(HackerPersona.values()) { persona ->
        val isSelected = persona == selectedPersona
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) colors.primaryAccent.copy(alpha = 0.2f) else colors.surfaceVariant)
            .border(
              width = if (isSelected) 1.2.dp else 0.8.dp,
              color = if (isSelected) colors.primaryAccent else colors.cardBorder,
              shape = RoundedCornerShape(8.dp)
            )
            .clickable {
              viewModel.setHackerPersona(persona)
              showPersonaDetails = true
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("persona_${persona.tag.lowercase()}")
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = persona.iconEmoji, fontSize = 12.sp)
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = persona.displayName,
              fontFamily = TechMonoFamily,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              fontSize = 11.sp,
              color = if (isSelected) colors.primaryAccent else colors.textPrimary
            )
          }
        }
      }
    }

    // Expandable Persona Superpower Details
    AnimatedVisibility(visible = showPersonaDetails) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(colors.surfaceVariant.copy(alpha = 0.7f))
          .border(0.8.dp, colors.secondaryAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
          .clickable { showPersonaDetails = !showPersonaDetails }
          .padding(10.dp)
      ) {
        Column {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "${selectedPersona.iconEmoji} ${selectedPersona.title}",
              fontFamily = TechMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 11.5.sp,
              color = colors.secondaryAccent
            )
            Text(
              text = "SKRÝT",
              fontFamily = TechMonoFamily,
              fontSize = 9.sp,
              color = colors.textMuted
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = selectedPersona.description,
            fontFamily = TechMonoFamily,
            fontSize = 10.sp,
            color = colors.textSecondary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Superpowers & Quick Prompt Shortcuts
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxWidth()
    ) {
      items(superintelligencePrompts) { shortcut ->
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surfaceVariant)
            .border(0.8.dp, colors.primaryAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .clickable {
              inputQuery = shortcut
              viewModel.askSuperintelligenceHacker(shortcut, selectedPersona)
              inputQuery = ""
            }
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          Text(
            text = shortcut,
            fontFamily = TechMonoFamily,
            fontSize = 10.sp,
            color = colors.textPrimary
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Chat Message Stream
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(messages, key = { it.id }) { msg ->
        ChatMessageItem(message = msg)
      }

      if (isSearching) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
              color = colors.primaryAccent,
              strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "${selectedPersona.iconEmoji} ${selectedPersona.displayName} provádí hloubkovou reverzní analýzu...",
              fontFamily = TechMonoFamily,
              fontSize = 10.5.sp,
              color = colors.primaryAccent
            )
          }
        }
      }
    }

    // Input Bar with Send Action
    CyberCard(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        OutlinedTextField(
          value = inputQuery,
          onValueChange = { inputQuery = it },
          placeholder = {
            Text(
              "Zadejte dotaz pro ${selectedPersona.displayName}...",
              fontFamily = TechMonoFamily,
              fontSize = 11.5.sp,
              color = colors.textMuted
            )
          },
          modifier = Modifier
            .weight(1f)
            .testTag("assistant_query_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primaryAccent,
            unfocusedBorderColor = colors.cardBorder,
            focusedTextColor = colors.textPrimary,
            unfocusedTextColor = colors.textPrimary,
            cursorColor = colors.primaryAccent
          ),
          textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = TechMonoFamily,
            fontSize = 12.sp
          ),
          singleLine = true
        )

        Spacer(modifier = Modifier.width(8.dp))

        IconButton(
          onClick = {
            if (inputQuery.isNotBlank()) {
              val q = inputQuery
              inputQuery = ""
              viewModel.askSuperintelligenceHacker(q, selectedPersona)
            }
          },
          enabled = inputQuery.isNotBlank() && !isSearching,
          modifier = Modifier.testTag("assistant_send_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = "Odeslat dotaz",
            tint = if (inputQuery.isNotBlank() && !isSearching) colors.primaryAccent else colors.textMuted
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(72.dp))
  }
}

@Composable
private fun ChatMessageItem(message: AssistantMessage) {
  val colors = LocalScrapMasterColors.current

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
  ) {
    if (!message.isUser) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 3.dp, start = 4.dp)
      ) {
        Box(
          modifier = Modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(colors.secondaryAccent)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = message.senderPersona,
          fontFamily = TechMonoFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 9.5.sp,
          color = colors.secondaryAccent
        )
      }
    }

    Box(
      modifier = Modifier
        .clip(
          RoundedCornerShape(
            topStart = 12.dp,
            topEnd = 12.dp,
            bottomStart = if (message.isUser) 12.dp else 2.dp,
            bottomEnd = if (message.isUser) 2.dp else 12.dp
          )
        )
        .background(
          if (message.isUser) colors.primaryAccent.copy(alpha = if (colors.isDark) 0.22f else 0.16f)
          else colors.surfaceVariant
        )
        .border(
          width = 0.8.dp,
          color = if (message.isUser) colors.primaryAccent.copy(alpha = 0.6f) else colors.cardBorder,
          shape = RoundedCornerShape(12.dp)
        )
        .padding(12.dp)
    ) {
      Column {
        Text(
          text = message.text,
          fontFamily = TechMonoFamily,
          fontSize = 11.5.sp,
          color = colors.textPrimary,
          lineHeight = 16.5.sp
        )

        // Exact Citations List
        if (message.citations.isNotEmpty()) {
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "POVINNÉ CITACE ZDROJŮ:",
            fontFamily = TechMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp,
            color = colors.secondaryAccent
          )
          Spacer(modifier = Modifier.height(2.dp))
          message.citations.forEach { citation ->
            Text(
              text = citation,
              fontFamily = TechMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              color = colors.primaryAccent
            )
          }
        }
      }
    }
  }
}
