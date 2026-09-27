package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HomeLayoutMode
import com.example.data.model.LauncherSettings
import com.example.data.repository.BatteryInfo
import com.example.data.repository.RamStats

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OriaCyberChronoWidget(
    timeString: String,
    dateString: String,
    batteryInfo: BatteryInfo,
    ramStats: RamStats,
    settings: LauncherSettings,
    onSelectLayoutMode: (HomeLayoutMode) -> Unit,
    onBoostRam: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(settings.theme.primaryColor)
    val secondaryColor = Color(settings.theme.secondaryColor)
    val cardBg = Color(settings.theme.cardColor).copy(alpha = if (settings.isLowRamMode) 0.92f else 0.75f)

    // Pulse animation for the central quantum orb
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val availableMb = (ramStats.totalMb - ramStats.usedMb).coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(cardBg)
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        primaryColor.copy(alpha = 0.45f),
                        secondaryColor.copy(alpha = 0.25f),
                        primaryColor.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(horizontal = 18.dp, vertical = 14.dp)
            .testTag("oria_cyber_chrono_widget")
    ) {
        // Top Row: Cyber Clock + Orbital HUD Gauge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: High-Tech Cyber Digital Clock & Persian Date
            Column(
                modifier = Modifier
                    .weight(1f)
                    .combinedClickable(
                        onClick = {},
                        onLongClick = onOpenSettings
                    )
            ) {
                // Time with Glowing Accent
                val timeParts = timeString.split(":")
                val hours = timeParts.getOrNull(0) ?: timeString
                val minutes = timeParts.getOrNull(1) ?: ""

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = hours,
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = (-1.5).sp
                    )
                    if (minutes.isNotEmpty()) {
                        Text(
                            text = ":",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                        Text(
                            text = minutes,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.Black,
                            color = primaryColor,
                            letterSpacing = (-1.5).sp
                        )
                    }
                }

                // Date Chip
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(primaryColor)
                    )
                    Text(
                        text = dateString,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right: Interactive Orbital HUD Arc (Battery & RAM)
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .clickable { onBoostRam() },
                contentAlignment = Alignment.Center
            ) {
                val batterySweep = (batteryInfo.level / 100f) * 360f
                val ramPercent = ramStats.usedPercentage.toFloat().coerceIn(0f, 100f)
                val ramSweep = (ramPercent / 100f) * 360f

                Canvas(modifier = Modifier.size(86.dp)) {
                    val strokeWidth = 5.dp.toPx()
                    val innerStrokeWidth = 4.dp.toPx()

                    // Background circles
                    drawCircle(
                        color = Color.White.copy(alpha = 0.06f),
                        radius = size.minDimension / 2 - strokeWidth / 2,
                        style = Stroke(width = strokeWidth)
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.04f),
                        radius = size.minDimension / 2 - strokeWidth - 6.dp.toPx(),
                        style = Stroke(width = innerStrokeWidth)
                    )

                    // Outer Battery Arc (Cyan Gradient)
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(primaryColor, secondaryColor, primaryColor)
                        ),
                        startAngle = -90f,
                        sweepAngle = batterySweep,
                        useCenter = false,
                        topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                        size = Size(size.width - strokeWidth, size.height - strokeWidth),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Inner RAM Arc (Electric Violet)
                    val inset = strokeWidth + 6.dp.toPx()
                    drawArc(
                        color = secondaryColor,
                        startAngle = -90f,
                        sweepAngle = ramSweep,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = Size(size.width - inset * 2, size.height - inset * 2),
                        style = Stroke(width = innerStrokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Center Pulsing Core & Boost Trigger
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (batteryInfo.isCharging) Icons.Default.Bolt else Icons.Default.RocketLaunch,
                        contentDescription = "Turbo Boost",
                        tint = primaryColor,
                        modifier = Modifier.size((18 * pulseScale).dp)
                    )
                    Text(
                        text = "${batteryInfo.level}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Middle Telemetry Strip: RAM & Quantum Speed Stats
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "RAM: ${ramStats.usedMb} MB",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "($availableMb MB free)",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            // Quick Boost Action Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(primaryColor.copy(alpha = 0.15f))
                    .clickable { onBoostRam() }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RocketLaunch,
                    contentDescription = "Boost",
                    tint = primaryColor,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = "Boost RAM",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Row: Futuristic Layout Mode Switcher Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HomeLayoutMode.entries.forEach { mode ->
                val isSelected = settings.homeLayoutMode == mode
                val pillBg = if (isSelected) primaryColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f)
                val pillBorder = if (isSelected) primaryColor else Color.White.copy(alpha = 0.08f)
                val icon = when (mode) {
                    HomeLayoutMode.CYBER_ORBIT -> Icons.Default.Hub
                    HomeLayoutMode.MINIMAL_FLOW -> Icons.Default.ViewList
                    HomeLayoutMode.NEO_GLASS_GRID -> Icons.Default.Dashboard
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(pillBg)
                        .border(1.dp, pillBorder, RoundedCornerShape(10.dp))
                        .clickable { onSelectLayoutMode(mode) }
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = mode.title,
                        tint = if (isSelected) primaryColor else Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = mode.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
