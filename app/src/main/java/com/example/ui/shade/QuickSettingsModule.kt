package com.example.ui.shade

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Apple iOS Control Center Quick Toggles Platter
 */
@Composable
fun QuickSettingsModule(
    isFlashlightOn: Boolean,
    isLowRamMode: Boolean,
    primaryColor: Color,
    cardBg: Color,
    onToggleFlashlight: () -> Unit,
    onToggleLowRamMode: () -> Unit,
    onToggleRingerMode: () -> Unit,
    onOpenThemeSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var wifiActive by remember { mutableStateOf(true) }
    var bluetoothActive by remember { mutableStateOf(true) }
    var autoRotateActive by remember { mutableStateOf(true) }
    var hotspotActive by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ShadeQuickTile(
                icon = Icons.Default.Wifi,
                title = "Wi-Fi",
                subtitle = if (wifiActive) "Connected" else "Off",
                isActive = wifiActive,
                activeColor = Color(0xFF0A84FF),
                cardBg = cardBg,
                onClick = { wifiActive = !wifiActive },
                modifier = Modifier.weight(1f)
            )

            ShadeQuickTile(
                icon = Icons.Default.Bluetooth,
                title = "Bluetooth",
                subtitle = if (bluetoothActive) "On" else "Off",
                isActive = bluetoothActive,
                activeColor = Color(0xFF0A84FF),
                cardBg = cardBg,
                onClick = { bluetoothActive = !bluetoothActive },
                modifier = Modifier.weight(1f)
            )

            ShadeQuickTile(
                icon = Icons.Default.FlashlightOn,
                title = "Flashlight",
                subtitle = if (isFlashlightOn) "On" else "Off",
                isActive = isFlashlightOn,
                activeColor = Color(0xFFFFD60A),
                cardBg = cardBg,
                onClick = onToggleFlashlight,
                modifier = Modifier.weight(1f)
            )

            ShadeQuickTile(
                icon = Icons.Default.VolumeMute,
                title = "Mute",
                subtitle = "Toggle",
                isActive = true,
                activeColor = Color(0xFFFF375F),
                cardBg = cardBg,
                onClick = onToggleRingerMode,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ShadeQuickTile(
                icon = Icons.Default.BatterySaver,
                title = "Low Power",
                subtitle = if (isLowRamMode) "Active" else "Off",
                isActive = isLowRamMode,
                activeColor = Color(0xFFFFD60A),
                cardBg = cardBg,
                onClick = onToggleLowRamMode,
                modifier = Modifier.weight(1f)
            )

            ShadeQuickTile(
                icon = Icons.Default.ScreenRotation,
                title = "Rotation",
                subtitle = if (autoRotateActive) "Portrait" else "Locked",
                isActive = autoRotateActive,
                activeColor = Color(0xFFFF453A),
                cardBg = cardBg,
                onClick = { autoRotateActive = !autoRotateActive },
                modifier = Modifier.weight(1f)
            )

            ShadeQuickTile(
                icon = Icons.Default.DarkMode,
                title = "Themes",
                subtitle = "Appearance",
                isActive = true,
                activeColor = Color(0xFFBF5AF2),
                cardBg = cardBg,
                onClick = onOpenThemeSettings,
                modifier = Modifier.weight(1f)
            )

            ShadeQuickTile(
                icon = Icons.Default.WifiTethering,
                title = "Hotspot",
                subtitle = if (hotspotActive) "Sharing" else "Off",
                isActive = hotspotActive,
                activeColor = Color(0xFF30D158),
                cardBg = cardBg,
                onClick = { hotspotActive = !hotspotActive },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ShadeQuickTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isActive: Boolean,
    activeColor: Color,
    cardBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBg by animateColorAsState(
        targetValue = if (isActive) activeColor.copy(alpha = 0.24f) else cardBg,
        animationSpec = tween(200),
        label = "tileBg"
    )
    val animatedBorder by animateColorAsState(
        targetValue = if (isActive) activeColor.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.12f),
        animationSpec = tween(200),
        label = "tileBorder"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isActive) activeColor else Color.White.copy(alpha = 0.7f),
        animationSpec = tween(200),
        label = "tileIcon"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(animatedBg)
            .border(0.8.dp, animatedBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = title,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = subtitle,
            fontSize = 9.5.sp,
            color = if (isActive) activeColor.copy(alpha = 0.95f) else Color.White.copy(alpha = 0.5f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
