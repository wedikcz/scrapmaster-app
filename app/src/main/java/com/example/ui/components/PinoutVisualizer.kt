package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PinoutEntity
import com.example.ui.theme.LocalScrapMasterColors
import com.example.ui.theme.TechMonoFamily
import org.json.JSONArray
import org.json.JSONObject

data class PinDetail(
  val pinNumber: Int,
  val label: String,
  val type: String,
  val description: String
)

@Composable
fun PinoutVisualizer(
  pinout: PinoutEntity,
  modifier: Modifier = Modifier
) {
  val colors = LocalScrapMasterColors.current

  val pins = remember(pinout.pinsJson) {
    val list = mutableListOf<PinDetail>()
    try {
      val array = JSONArray(pinout.pinsJson)
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          PinDetail(
            pinNumber = obj.optInt("pin", i + 1),
            label = obj.optString("label", "PIN ${i + 1}"),
            type = obj.optString("type", "IO"),
            description = obj.optString("desc", "")
          )
        )
      }
    } catch (_: Exception) {
      // Fallback pins
      for (i in 1..pinout.pinCount) {
        list.add(PinDetail(i, "P$i", "IO", "Pin $i"))
      }
    }
    list
  }

  var selectedPin by remember { mutableStateOf<PinDetail?>(pins.firstOrNull()) }

  Column(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(colors.surfaceVariant.copy(alpha = 0.6f))
      .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
      .padding(12.dp)
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column {
        Text(
          text = pinout.componentModel,
          fontFamily = TechMonoFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          color = colors.primaryAccent
        )
        Text(
          text = "${pinout.packageType} • ${pinout.supplyVoltage}",
          fontFamily = TechMonoFamily,
          fontSize = 11.sp,
          color = colors.textMuted
        )
      }
      IndustrialBadge(
        text = "${pins.size} PINŮ",
        color = colors.secondaryAccent
      )
    }

    Spacer(modifier = Modifier.height(14.dp))

    // Interactive IC Chip Representation
    if (pinout.packageType.startsWith("TO-220")) {
      // 3-Pin inline package
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        pins.forEach { pin ->
          PinBadgeItem(
            pin = pin,
            isSelected = pin == selectedPin,
            onClick = { selectedPin = pin }
          )
        }
      }
    } else {
      // Dual Row DIP Layout
      val half = (pins.size + 1) / 2
      val leftPins = pins.take(half)
      val rightPins = pins.drop(half).reversed()

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Left Pin Row
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          leftPins.forEach { pin ->
            PinBadgeItem(
              pin = pin,
              isSelected = pin == selectedPin,
              onClick = { selectedPin = pin }
            )
          }
        }

        // IC Center Body
        Box(
          modifier = Modifier
            .width(76.dp)
            .height((leftPins.size * 34).dp.coerceAtLeast(80.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(colors.surface)
            .border(1.5.dp, colors.primaryAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Orientation Notch
            Box(
              modifier = Modifier
                .size(16.dp, 8.dp)
                .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                .background(colors.primaryAccent.copy(alpha = 0.3f))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = pinout.componentModel,
              fontFamily = TechMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = colors.textPrimary
            )
            Text(
              text = pinout.packageType,
              fontFamily = TechMonoFamily,
              fontSize = 8.sp,
              color = colors.textMuted
            )
          }
        }

        // Right Pin Row
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          rightPins.forEach { pin ->
            PinBadgeItem(
              pin = pin,
              isSelected = pin == selectedPin,
              onClick = { selectedPin = pin }
            )
          }
        }
      }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Selected Pin Detail Banner
    selectedPin?.let { pin ->
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(8.dp))
          .background(colors.surface)
          .border(1.dp, getPinColor(pin.type, colors).copy(alpha = 0.6f), RoundedCornerShape(8.dp))
          .padding(10.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(CircleShape)
              .background(getPinColor(pin.type, colors).copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "${pin.pinNumber}",
              fontFamily = TechMonoFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = getPinColor(pin.type, colors)
            )
          }

          Spacer(modifier = Modifier.width(10.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = pin.label,
                fontFamily = TechMonoFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = colors.textPrimary
              )
              Spacer(modifier = Modifier.width(6.dp))
              IndustrialBadge(
                text = pin.type,
                color = getPinColor(pin.type, colors)
              )
            }
            Text(
              text = pin.description,
              fontFamily = TechMonoFamily,
              fontSize = 11.sp,
              color = colors.textSecondary
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PinBadgeItem(
  pin: PinDetail,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  val colors = LocalScrapMasterColors.current
  val pinColor = getPinColor(pin.type, colors)

  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(
        if (isSelected) pinColor.copy(alpha = 0.25f)
        else colors.surface.copy(alpha = 0.8f)
      )
      .border(
        width = if (isSelected) 1.5.dp else 0.8.dp,
        color = if (isSelected) pinColor else colors.cardBorder,
        shape = RoundedCornerShape(6.dp)
      )
      .clickable { onClick() }
      .padding(horizontal = 7.dp, vertical = 4.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = "${pin.pinNumber}:",
        fontFamily = TechMonoFamily,
        fontSize = 9.sp,
        color = colors.textMuted
      )
      Spacer(modifier = Modifier.width(3.dp))
      Text(
        text = pin.label,
        fontFamily = TechMonoFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        color = if (isSelected) pinColor else colors.textPrimary
      )
    }
  }
}

@Composable
private fun getPinColor(type: String, colors: com.example.ui.theme.ScrapMasterColors): Color {
  return when (type.uppercase()) {
    "POWER", "POWER_IN", "VCC" -> colors.alertAccent
    "GND", "GROUND" -> Color(0xFF64748B)
    "OUTPUT" -> colors.secondaryAccent
    "INPUT" -> colors.primaryAccent
    "ADC", "ANALOG" -> Color(0xFFA855F7)
    "UART", "TX", "RX" -> Color(0xFF38BDF8)
    else -> colors.primaryAccent
  }
}
