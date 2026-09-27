package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LauncherSettings
import com.example.data.model.StatusBarStyle
import com.example.data.repository.BatteryInfo

@Composable
fun CustomStatusBar(
    timeString: String,
    batteryInfo: BatteryInfo,
    notificationCount: Int,
    isLowRamMode: Boolean,
    settings: LauncherSettings,
    onOpenControlCenter: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isIslandExpanded by remember { mutableStateOf(false) }

    val dragModifier = Modifier.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val down = awaitFirstDown(requireUnconsumed = false)
                val pointerId = down.id
                var totalY = 0f
                var triggered = false
                try {
                    while (true) {
                        val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                        val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                        if (!change.pressed) break
                        val deltaY = change.position.y - change.previousPosition.y
                        totalY += deltaY
                        if (totalY > 10f && !triggered) {
                            triggered = true
                            change.consume()
                            onOpenControlCenter()
                            break
                        }
                    }
                } catch (e: Exception) {
                    // ignore
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(dragModifier)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(horizontal = 18.dp, vertical = 4.dp)
            .testTag("custom_status_bar"),
        contentAlignment = Alignment.Center
    ) {
        // Left: iOS Bold System Clock
        Row(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clickable { onOpenControlCenter() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = timeString,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = (-0.2).sp
            )
        }

        // Center: Interactive Apple Dynamic Island or Minimal Status Pill
        if (settings.statusBarStyle == StatusBarStyle.DYNAMIC_ISLAND) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.Black)
                    .border(0.6.dp, Color(0xFF2C2C2E), RoundedCornerShape(22.dp))
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    )
                    .clickable {
                        isIslandExpanded = !isIslandExpanded
                    }
                    .padding(horizontal = if (isIslandExpanded) 14.dp else 10.dp, vertical = if (isIslandExpanded) 10.dp else 6.dp)
                    .testTag("dynamic_island"),
                contentAlignment = Alignment.Center
            ) {
                if (!isIslandExpanded) {
                    // Compact Dynamic Island
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Left sensor camera dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF151517))
                                .border(0.5.dp, Color(0xFF262628), CircleShape)
                        )

                        // Center Audio or Battery status
                        if (batteryInfo.isCharging) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Charging",
                                    tint = Color(0xFF30D158),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${batteryInfo.level}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF30D158)
                                )
                            }
                        } else if (notificationCount > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF453A))
                                )
                                Text(
                                    text = "$notificationCount",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            // Ambient mini wave
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = "Audio Wave",
                                tint = Color(0xFF0A84FF),
                                modifier = Modifier.size(13.dp)
                            )
                        }

                        // Right micro sensor dot
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF101012))
                        )
                    }
                } else {
                    // Expanded Dynamic Island Bubble
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.width(210.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = "Now Playing",
                                    tint = Color(0xFFFF2D55),
                                    modifier = Modifier.size(16.dp)
                                )
                                Column {
                                    Text(
                                        text = "Now Playing",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Spatial Audio",
                                        fontSize = 9.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Battery",
                                    tint = if (batteryInfo.isCharging) Color(0xFF30D158) else Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "${batteryInfo.level}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (batteryInfo.isCharging) Color(0xFF30D158) else Color.White
                                )
                            }
                        }

                        Text(
                            text = "Swipe down for Control Center",
                            fontSize = 9.sp,
                            color = Color(0xFF0A84FF),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            // Minimal Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.08f))
                    .clickable { onOpenControlCenter() }
                    .padding(horizontal = 10.dp, vertical = 3.dp)
                    .testTag("minimal_status_pill"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Oria UI",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.65f),
                    letterSpacing = 0.4.sp
                )
            }
        }

        // Right: iOS System Glyphs (Signal, 5G / Wi-Fi, Battery capsule)
        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .clickable { onOpenControlCenter() },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Signal Bars (Canvas)
            IosCellularSignalBars(strength = 4)

            // 5G text badge
            Text(
                text = "5G",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Wi-Fi icon
            Icon(
                imageVector = Icons.Default.Wifi,
                contentDescription = "Wi-Fi",
                tint = Color.White,
                modifier = Modifier.size(13.dp)
            )

            // iOS Battery Glyph
            IosBatteryIcon(
                level = batteryInfo.level,
                isCharging = batteryInfo.isCharging
            )
        }
    }
}

@Composable
private fun IosCellularSignalBars(strength: Int = 4) {
    Canvas(modifier = Modifier.size(width = 16.dp, height = 10.dp)) {
        val barWidth = 2.4.dp.toPx()
        val spacing = 1.4.dp.toPx()
        val totalBars = 4

        for (i in 0 until totalBars) {
            val barHeight = size.height * ((i + 1f) / totalBars)
            val left = i * (barWidth + spacing)
            val top = size.height - barHeight
            val color = if (i < strength) Color.White else Color.White.copy(alpha = 0.3f)

            drawRoundRect(
                color = color,
                topLeft = Offset(left, top),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
        }
    }
}

@Composable
private fun IosBatteryIcon(level: Int, isCharging: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        // Main Battery Body
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(11.dp)
                .clip(RoundedCornerShape(3.dp))
                .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(3.dp))
                .padding(1.5.dp)
        ) {
            val fillPercent = (level.coerceIn(0, 100)) / 100f
            val fillColor = if (isCharging) {
                Color(0xFF30D158) // iOS Green
            } else if (level <= 20) {
                Color(0xFFFF453A) // iOS Red
            } else {
                Color.White
            }

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fillPercent)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(fillColor)
            )
        }

        // Battery Nipple
        Box(
            modifier = Modifier
                .width(1.2.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(topEnd = 1.dp, bottomEnd = 1.dp))
                .background(Color.White.copy(alpha = 0.7f))
        )
    }
}
