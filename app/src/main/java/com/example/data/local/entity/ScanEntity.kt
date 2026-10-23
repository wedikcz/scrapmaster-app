package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scans")
data class ScanEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val timestamp: Long = System.currentTimeMillis(),
  val componentName: String,
  val packageType: String,
  val confidence: Float,
  val specsSummary: String,
  val pinCount: Int,
  val functionalRole: String,
  val salvageRating: String, // High, Medium, Low
  val desolderDifficulty: String, // Easy, Moderate, Advanced
  val reflectionVerified: Boolean = true,
  val isDemo: Boolean = false,
  val rawAnalysis: String = "",
  val imageUri: String? = null
)
