package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.ProjectEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.IndustrialBadge
import com.example.ui.components.NeonButton
import com.example.ui.theme.LocalScrapMasterColors
import com.example.ui.theme.TechMonoFamily
import com.example.viewmodel.WorkshopViewModel

@Composable
fun ProjectsScreen(
  viewModel: WorkshopViewModel,
  modifier: Modifier = Modifier
) {
  val colors = LocalScrapMasterColors.current
  val projects by viewModel.projects.collectAsStateWithLifecycle()

  var selectedStatusFilter by remember { mutableStateOf("VŠECHNY") }
  var showAddDialog by remember { mutableStateOf(false) }

  val statuses = listOf("VŠECHNY", "IDEA", "IN_PROGRESS", "COMPLETED")

  val filteredProjects = if (selectedStatusFilter == "VŠECHNY") {
    projects
  } else {
    projects.filter { it.status == selectedStatusFilter }
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
            text = "DÍLENSKÝ KANBAN",
            fontFamily = TechMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = colors.textPrimary,
            letterSpacing = 0.5.sp
          )
          Text(
            text = "Správa projektů, přestaveb a potřebných e-waste dílů",
            fontFamily = TechMonoFamily,
            fontSize = 11.sp,
            color = colors.textMuted
          )
        }

        NeonButton(
          text = "+ Projekt",
          onClick = { showAddDialog = true },
          icon = Icons.Default.Add,
          testTag = "projects_add_btn"
        )
      }
    }

    // Status Filter Tabs
    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(statuses) { status ->
          val isSelected = status == selectedStatusFilter
          val label = when (status) {
            "IDEA" -> "Nápad (${projects.count { it.status == "IDEA" }})"
            "IN_PROGRESS" -> "V řešení (${projects.count { it.status == "IN_PROGRESS" }})"
            "COMPLETED" -> "Hotovo (${projects.count { it.status == "COMPLETED" }})"
            else -> "Všechny (${projects.size})"
          }
          val tabColor = when (status) {
            "IDEA" -> colors.alertAccent
            "IN_PROGRESS" -> colors.primaryAccent
            "COMPLETED" -> colors.secondaryAccent
            else -> colors.textPrimary
          }

          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(
                if (isSelected) tabColor.copy(alpha = 0.2f)
                else colors.surfaceVariant
              )
              .border(
                width = if (isSelected) 1.2.dp else 0.8.dp,
                color = if (isSelected) tabColor else colors.cardBorder,
                shape = RoundedCornerShape(8.dp)
              )
              .clickable { selectedStatusFilter = status }
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = label,
              fontFamily = TechMonoFamily,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              fontSize = 11.sp,
              color = if (isSelected) tabColor else colors.textSecondary
            )
          }
        }
      }
    }

    // Projects list
    if (filteredProjects.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "V této kategorii nejsou žádné projekty.",
            fontFamily = TechMonoFamily,
            fontSize = 12.sp,
            color = colors.textMuted
          )
        }
      }
    } else {
      items(filteredProjects, key = { it.id }) { project ->
        KanbanProjectCard(
          project = project,
          onMoveStatus = { newStatus -> viewModel.updateProjectStatus(project.id, newStatus) },
          onDelete = { viewModel.deleteProject(project.id) }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }

  if (showAddDialog) {
    AddProjectDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { title, desc, tags, parts, notes, priority ->
        viewModel.addNewProject(title, desc, tags, parts, notes, priority)
        showAddDialog = false
      }
    )
  }
}

@Composable
private fun KanbanProjectCard(
  project: ProjectEntity,
  onMoveStatus: (String) -> Unit,
  onDelete: () -> Unit
) {
  val colors = LocalScrapMasterColors.current

  val statusColor = when (project.status) {
    "IDEA" -> colors.alertAccent
    "IN_PROGRESS" -> colors.primaryAccent
    "COMPLETED" -> colors.secondaryAccent
    else -> colors.textMuted
  }

  val statusLabel = when (project.status) {
    "IDEA" -> "NÁPAD"
    "IN_PROGRESS" -> "V ŘEŠENÍ"
    "COMPLETED" -> "HOTOVO"
    else -> project.status
  }

  CyberCard(borderColor = statusColor.copy(alpha = 0.5f)) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = project.title,
          fontFamily = TechMonoFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          color = colors.textPrimary,
          modifier = Modifier.weight(1f)
        )

        IndustrialBadge(
          text = statusLabel,
          color = statusColor
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = project.description,
        fontFamily = TechMonoFamily,
        fontSize = 11.5.sp,
        color = colors.textSecondary,
        lineHeight = 16.sp
      )

      if (project.neededParts.isNotBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(colors.surfaceVariant)
            .padding(8.dp)
        ) {
          Column {
            Text(
              text = "POTŘEBNÉ DÍLY:",
              fontFamily = TechMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 9.sp,
              color = colors.alertAccent
            )
            Text(
              text = project.neededParts,
              fontFamily = TechMonoFamily,
              fontSize = 10.5.sp,
              color = colors.textPrimary
            )
          }
        }
      }

      if (project.notes.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Poznámka: ${project.notes}",
          fontFamily = TechMonoFamily,
          fontSize = 10.sp,
          color = colors.textMuted
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Status Shift Arrows
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (project.status != "IDEA") {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(colors.surfaceVariant)
                .clickable {
                  val prev = if (project.status == "COMPLETED") "IN_PROGRESS" else "IDEA"
                  onMoveStatus(prev)
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Zpět", fontFamily = TechMonoFamily, fontSize = 10.sp, color = colors.textSecondary)
              }
            }
            Spacer(modifier = Modifier.width(8.dp))
          }

          if (project.status != "COMPLETED") {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(colors.primaryAccent.copy(alpha = 0.2f))
                .clickable {
                  val next = if (project.status == "IDEA") "IN_PROGRESS" else "COMPLETED"
                  onMoveStatus(next)
                }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Postoupit", fontFamily = TechMonoFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.primaryAccent)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = colors.primaryAccent, modifier = Modifier.size(13.dp))
              }
            }
          }
        }

        Icon(
          imageVector = Icons.Default.Delete,
          contentDescription = "Smazat projekt",
          tint = colors.textMuted,
          modifier = Modifier
            .size(18.dp)
            .clickable { onDelete() }
        )
      }
    }
  }
}

@Composable
private fun AddProjectDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String, String, String, String, String) -> Unit
) {
  val colors = LocalScrapMasterColors.current

  var title by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var tags by remember { mutableStateOf("E-Waste, DIY") }
  var neededParts by remember { mutableStateOf("") }
  var notes by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "NOVÝ PROJEKT V DÍLNĚ",
        fontFamily = TechMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = colors.primaryAccent
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Název projektu", fontFamily = TechMonoFamily, fontSize = 11.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Popis cíle", fontFamily = TechMonoFamily, fontSize = 11.sp) },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = neededParts,
          onValueChange = { neededParts = it },
          label = { Text("Potřebné e-waste součástky", fontFamily = TechMonoFamily, fontSize = 11.sp) },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = notes,
          onValueChange = { notes = it },
          label = { Text("Technické poznámky", fontFamily = TechMonoFamily, fontSize = 11.sp) },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          if (title.isNotBlank()) {
            onConfirm(title, description, tags, neededParts, notes, "NORMAL")
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
