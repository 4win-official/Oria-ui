package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Wifi
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
import com.example.data.model.LauncherSettings
import com.example.data.repository.BatteryInfo
import com.example.data.repository.RamStats

/**
 * Authentic Apple iOS 18 Style 2x2 Smart Widgets Platter:
 * 1. Weather Widget (Cupertino, 22° Sunny)
 * 2. Batteries & RAM Rings Widget (Device battery & memory)
 */
@Composable
fun IosWidgetsRow(
    batteryInfo: BatteryInfo,
    ramStats: RamStats,
    settings: LauncherSettings,
    onBoostRam: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. iOS Weather Widget (2x2 Squircle)
        IosWeatherWidget(
            modifier = Modifier.weight(1f)
        )

        // 2. iOS Batteries & System Rings Widget (2x2 Squircle)
        IosBatteriesWidget(
            batteryInfo = batteryInfo,
            ramStats = ramStats,
            onBoostRam = onBoostRam,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun IosWeatherWidget(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(138.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF1E5B99),
                        Color(0xFF2E7CC7),
                        Color(0xFF4A90E2)
                    )
                )
            )
            .border(0.6.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(26.dp))
            .padding(14.dp)
            .testTag("ios_weather_widget")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // City & Current Condition
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Cupertino",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Mostly Sunny",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.82f)
                    )
                }
                Icon(
                    imageVector = Icons.Default.WbSunny,
                    contentDescription = "Sunny",
                    tint = Color(0xFFFFD60A),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Temperature
            Column {
                Text(
                    text = "22°",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = "H: 26°  L: 15°",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
fun IosBatteriesWidget(
    batteryInfo: BatteryInfo,
    ramStats: RamStats,
    onBoostRam: () -> Unit,
    modifier: Modifier = Modifier
) {
    val freeRamMb = (ramStats.totalMb - ramStats.usedMb).coerceAtLeast(0)
    val batteryProgress by animateFloatAsState(
        targetValue = batteryInfo.level / 100f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "batAnim"
    )

    Box(
        modifier = modifier
            .height(138.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Color(0xCC1C1C1E))
            .border(0.6.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(26.dp))
            .clickable { onBoostRam() }
            .padding(14.dp)
            .testTag("ios_batteries_widget")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Widget Title & Clean/Boost Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Batteries",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Icon(
                    imageVector = Icons.Default.CleaningServices,
                    contentDescription = "Optimize",
                    tint = Color(0xFF0A84FF),
                    modifier = Modifier.size(16.dp)
                )
            }

            // Dual Rings Row (Device Battery & System Memory)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // iPhone Battery Ring
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier.size(54.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(52.dp)) {
                            val stroke = 5.dp.toPx()
                            // Background ring
                            drawCircle(
                                color = Color.White.copy(alpha = 0.1f),
                                style = Stroke(width = stroke)
                            )
                            // Progress ring
                            val ringColor = if (batteryInfo.isCharging) Color(0xFF30D158) else if (batteryInfo.level <= 20) Color(0xFFFF453A) else Color(0xFF30D158)
                            drawArc(
                                color = ringColor,
                                startAngle = -90f,
                                sweepAngle = batteryProgress * 360f,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                        Icon(
                            imageVector = if (batteryInfo.isCharging) Icons.Default.Bolt else Icons.Default.PhoneIphone,
                            contentDescription = "iPhone",
                            tint = if (batteryInfo.isCharging) Color(0xFF30D158) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "${batteryInfo.level}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // RAM Ring (Oria Optimization)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier.size(54.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val ramProgress = (ramStats.usedPercentage / 100f).coerceIn(0f, 1f)
                        Canvas(modifier = Modifier.size(52.dp)) {
                            val stroke = 5.dp.toPx()
                            drawCircle(
                                color = Color.White.copy(alpha = 0.1f),
                                style = Stroke(width = stroke)
                            )
                            drawArc(
                                color = Color(0xFF0A84FF),
                                startAngle = -90f,
                                sweepAngle = ramProgress * 360f,
                                useCenter = false,
                                style = Stroke(width = stroke, cap = StrokeCap.Round)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = "Memory",
                            tint = Color(0xFF0A84FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "${freeRamMb}MB",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Bottom Subtitle
            Text(
                text = "Tap to clean memory",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF0A84FF),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}
