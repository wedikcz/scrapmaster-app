package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "datasheets")
data class DatasheetEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val fileName: String,
  val category: String, // IC, MOSFET, DIODE, VOLTAGE_REG, MICROCONTROLLER, SAFETY
  val summary: String,
  val chunksJson: String, // JSON list of chunks: [{"chunkIndex":1, "section":"Absolute Max Ratings", "text":"..."}]
  val keyParameters: String = "",
  val createdAt: Long = System.currentTimeMillis()
)
