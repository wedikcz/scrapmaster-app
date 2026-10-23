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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ai.ComponentTaggingEngine
import com.example.data.local.entity.ItemEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.IndustrialBadge
import com.example.ui.components.NeonButton
import com.example.ui.theme.LocalScrapMasterColors
import com.example.ui.theme.TechMonoFamily
import com.example.viewmodel.WorkshopViewModel

@Composable
fun InventoryScreen(
  viewModel: WorkshopViewModel,
  modifier: Modifier = Modifier
) {
  val colors = LocalScrapMasterColors.current
  val items by viewModel.items.collectAsStateWithLifecycle()

  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("Vše") }
  var showAddDialog by remember { mutableStateOf(false) }

  val categories = remember(items) {
    listOf("Vše") + (items.map { it.category } + ComponentTaggingEngine.STANDARD_CATEGORIES).distinct()
  }

  val filteredItems = items.filter { item ->
    val matchesCat = selectedCategory == "Vše" || item.category == selectedCategory
    val matchesSearch = searchQuery.isBlank() ||
        item.name.contains(searchQuery, ignoreCase = true) ||
        item.specifications.contains(searchQuery, ignoreCase = true) ||
        item.location.contains(searchQuery, ignoreCase = true)
    matchesCat && matchesSearch
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header & Add Button
    item {
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "INVENTÁŘ DÍLNY",
            fontFamily = TechMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = colors.textPrimary,
            letterSpacing = 0.5.sp
          )
          Text(
            text = "${items.size} uložených součástek • Rychlá správa stavu",
            fontFamily = TechMonoFamily,
            fontSize = 11.sp,
            color = colors.textMuted
          )
        }

        NeonButton(
          text = "+ Přidat",
          onClick = { showAddDialog = true },
          icon = Icons.Default.Add,
          testTag = "inventory_add_item_btn"
        )
      }
    }

    // Search bar
    item {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = {
          Text(
            text = "Hledat součástku, parametry, přihrádku...",
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
          .testTag("inventory_search_field"),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = colors.primaryAccent,
          unfocusedBorderColor = colors.cardBorder,
          focusedTextColor = colors.textPrimary,
          unfocusedTextColor = colors.textPrimary,
          focusedContainerColor = colors.surfaceVariant,
          unfocusedContainerColor = colors.surfaceVariant
        ),
        singleLine = true
      )
    }

    // Category Filter Chips
    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { cat ->
          val isSelected = cat == selectedCategory
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(
                if (isSelected) colors.primaryAccent.copy(alpha = 0.2f)
                else colors.surfaceVariant
              )
              .border(
                width = if (isSelected) 1.2.dp else 0.8.dp,
                color = if (isSelected) colors.primaryAccent else colors.cardBorder,
                shape = RoundedCornerShape(8.dp)
              )
              .clickable { selectedCategory = cat }
              .padding(horizontal = 10.dp, vertical = 5.dp)
          ) {
            Text(
              text = cat,
              fontFamily = TechMonoFamily,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              fontSize = 11.sp,
              color = if (isSelected) colors.primaryAccent else colors.textSecondary
            )
          }
        }
      }
    }

    // Item List
    if (filteredItems.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Žádná součástka neodpovídá vyhledávání.",
            fontFamily = TechMonoFamily,
            fontSize = 12.sp,
            color = colors.textMuted
          )
        }
      }
    } else {
      items(filteredItems, key = { it.id }) { item ->
        InventoryItemCard(
          item = item,
          onAdjustQty = { delta -> viewModel.adjustItemQuantity(item.id, delta) },
          onDelete = { viewModel.deleteItem(item.id) }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(80.dp))
    }
  }

  // Add Item Dialog
  if (showAddDialog) {
    AddItemDialog(
      onDismiss = { showAddDialog = false },
      onConfirm = { name, cat, qty, min, loc, pkg, specs, pinRef ->
        viewModel.addNewItem(name, cat, qty, min, loc, pkg, specs, pinRef)
        showAddDialog = false
      }
    )
  }
}

@Composable
private fun InventoryItemCard(
  item: ItemEntity,
  onAdjustQty: (Int) -> Unit,
  onDelete: () -> Unit
) {
  val colors = LocalScrapMasterColors.current
  val isLowStock = item.quantity <= item.minStock

  CyberCard(
    borderColor = if (isLowStock) colors.alertAccent.copy(alpha = 0.5f) else colors.cardBorder
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = item.name,
            fontFamily = TechMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = colors.textPrimary
          )
          Text(
            text = "${item.packageType} • ${item.category}",
            fontFamily = TechMonoFamily,
            fontSize = 10.5.sp,
            color = colors.primaryAccent
          )
        }

        // Location & Condition Badge
        IndustrialBadge(
          text = item.location,
          color = colors.secondaryAccent
        )
      }

      val autoTag = remember(item) { ComponentTaggingEngine.analyzeItem(item) }

      if (item.specifications.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = item.specifications,
          fontFamily = TechMonoFamily,
          fontSize = 11.sp,
          color = colors.textSecondary,
          maxLines = 2
        )
      }

      if (autoTag.suggestedTags.isNotEmpty()) {
        Spacer(modifier = Modifier.height(6.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          items(autoTag.suggestedTags) { tag ->
            IndustrialBadge(text = tag, color = colors.primaryAccent.copy(alpha = 0.85f))
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Bottom Row: Stock Quantity Stepper & Actions
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          if (isLowStock) {
            Icon(
              imageVector = Icons.Default.Warning,
              contentDescription = "Nízký stav",
              tint = colors.alertAccent,
              modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "MIN ${item.minStock} KS",
              fontFamily = TechMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              color = colors.alertAccent
            )
          } else {
            Text(
              text = "SKLADEM",
              fontFamily = TechMonoFamily,
              fontSize = 10.sp,
              color = colors.textMuted
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // -1 Button
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(colors.surfaceVariant)
              .border(0.8.dp, colors.cardBorder, RoundedCornerShape(6.dp))
              .clickable { onAdjustQty(-1) },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Remove,
              contentDescription = "-1",
              tint = colors.textPrimary,
              modifier = Modifier.size(16.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Text(
            text = "${item.quantity}",
            fontFamily = TechMonoFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = if (isLowStock) colors.alertAccent else colors.primaryAccent
          )

          Spacer(modifier = Modifier.width(8.dp))

          // +1 Button
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(RoundedCornerShape(6.dp))
              .background(colors.primaryAccent.copy(alpha = 0.2f))
              .border(0.8.dp, colors.primaryAccent, RoundedCornerShape(6.dp))
              .clickable { onAdjustQty(+1) },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Add,
              contentDescription = "+1",
              tint = colors.primaryAccent,
              modifier = Modifier.size(16.dp)
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          // Delete
          Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Smazat položku",
            tint = colors.textMuted,
            modifier = Modifier
              .size(18.dp)
              .clickable { onDelete() }
          )
        }
      }
    }
  }
}

@Composable
private fun AddItemDialog(
  onDismiss: () -> Unit,
  onConfirm: (String, String, Int, Int, String, String, String, String) -> Unit
) {
  val colors = LocalScrapMasterColors.current

  var name by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("IC / Semiconductor") }
  var quantity by remember { mutableStateOf("5") }
  var minStock by remember { mutableStateOf("2") }
  var location by remember { mutableStateOf("Drawer A-01") }
  var packageType by remember { mutableStateOf("DIP-8") }
  var specs by remember { mutableStateOf("") }
  var pinoutRef by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        text = "PŘIDAT NOVOU SOUČÁSTKU",
        fontFamily = TechMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = colors.primaryAccent
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Název (např. LM358 Op-Amp)", fontFamily = TechMonoFamily, fontSize = 11.sp) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )

        // AI Category & Tag Suggestions Box
        if (name.isNotBlank()) {
          val autoTag = remember(name, packageType, specs) {
            ComponentTaggingEngine.analyzeComponent(name, packageType, specs)
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(colors.primaryAccent.copy(alpha = 0.12f))
              .border(0.8.dp, colors.primaryAccent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
              .padding(8.dp)
          ) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "⚡ AI DOPORUČENÁ KATEGORIE:",
                  fontFamily = TechMonoFamily,
                  fontSize = 9.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = colors.primaryAccent
                )

                Text(
                  text = "Nastavit: ${autoTag.suggestedCategory}",
                  fontFamily = TechMonoFamily,
                  fontSize = 9.5.sp,
                  fontWeight = FontWeight.Bold,
                  color = colors.secondaryAccent,
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { category = autoTag.suggestedCategory }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                )
              }

              Spacer(modifier = Modifier.height(3.dp))
              Text(
                text = "${autoTag.categoryReason} • ${autoTag.estimatedSalvageYield}",
                fontFamily = TechMonoFamily,
                fontSize = 9.sp,
                color = colors.textMuted
              )

              if (autoTag.suggestedTags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  items(autoTag.suggestedTags) { tag ->
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.surfaceVariant)
                        .clickable {
                          if (!specs.contains(tag)) {
                            specs = if (specs.isBlank()) tag else "$specs, $tag"
                          }
                        }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text(
                        text = "+ $tag",
                        fontFamily = TechMonoFamily,
                        fontSize = 8.5.sp,
                        color = colors.primaryAccent
                      )
                    }
                  }
                }
              }
            }
          }
        }

        OutlinedTextField(
          value = category,
          onValueChange = { category = it },
          label = { Text("Kategorie", fontFamily = TechMonoFamily, fontSize = 11.sp) },
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
            value = location,
            onValueChange = { location = it },
            label = { Text("Umístění", fontFamily = TechMonoFamily, fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedTextField(
            value = quantity,
            onValueChange = { quantity = it },
            label = { Text("Množství", fontFamily = TechMonoFamily, fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
          OutlinedTextField(
            value = minStock,
            onValueChange = { minStock = it },
            label = { Text("Min zásoba", fontFamily = TechMonoFamily, fontSize = 11.sp) },
            singleLine = true,
            modifier = Modifier.weight(1f)
          )
        }
        OutlinedTextField(
          value = specs,
          onValueChange = { specs = it },
          label = { Text("Technické parametry", fontFamily = TechMonoFamily, fontSize = 11.sp) },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      TextButton(
        onClick = {
          if (name.isNotBlank()) {
            onConfirm(
              name,
              category,
              quantity.toIntOrNull() ?: 1,
              minStock.toIntOrNull() ?: 2,
              location,
              packageType,
              specs,
              pinoutRef
            )
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
