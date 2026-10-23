package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pinouts")
data class PinoutEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val componentModel: String,
  val packageType: String, // DIP-8, DIP-14, DIP-16, TO-220, SOIC-8, ESP-MODULE, USB-C
  val description: String,
  val pinCount: Int,
  val pinsJson: String, // JSON serialized list of pins: [{"pin":1,"label":"GND","type":"POWER","desc":"Ground"}]
  val typicalApp: String = "General Electronics",
  val supplyVoltage: String = "5V - 15V",
  val isCustom: Boolean = false
)
