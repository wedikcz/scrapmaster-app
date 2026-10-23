package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class ItemEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val name: String,
  val category: String, // IC, Transistor, Diode, Capacitor, Resistor, Sensor, Connector, Relay, Module
  val quantity: Int,
  val minStock: Int = 2,
  val location: String = "Bin A-01",
  val packageType: String = "DIP-8",
  val specifications: String = "",
  val pinoutRef: String = "",
  val salvageSource: String = "PC Power Supply / E-Waste",
  val condition: String = "Tested / Functional",
  val updatedAt: Long = System.currentTimeMillis()
)
