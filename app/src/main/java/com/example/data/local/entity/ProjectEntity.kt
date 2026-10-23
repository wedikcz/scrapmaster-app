package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val description: String,
  val status: String, // IDEA, IN_PROGRESS, COMPLETED
  val tags: String = "E-Waste, DIY",
  val neededParts: String = "",
  val notes: String = "",
  val priority: String = "NORMAL", // LOW, NORMAL, HIGH
  val updatedAt: Long = System.currentTimeMillis()
)
