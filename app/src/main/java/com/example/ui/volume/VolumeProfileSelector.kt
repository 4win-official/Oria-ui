package com.example.ui.volume

import android.media.AudioManager
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Apple iOS sound profile segmented switcher (Ring, Vibrate, Silent).
 */
@Composable
fun VolumeProfileSelector(
    currentRingerMode: Int,
    primaryColor: Color,
    cardBg: Color,
    onSelectMode: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(cardBg)
            .border(0.8.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        ProfileOption(
            title = "Ring",
            icon = Icons.Default.NotificationsActive,
            isSelected = currentRingerMode == AudioManager.RINGER_MODE_NORMAL,
            activeColor = Color(0xFF30D158),
            onClick = { onSelectMode(AudioManager.RINGER_MODE_NORMAL) },
            modifier = Modifier.weight(1f)
        )

        ProfileOption(
            title = "Vibrate",
            icon = Icons.Default.Vibration,
            isSelected = currentRingerMode == AudioManager.RINGER_MODE_VIBRATE,
            activeColor = Color(0xFFFFD60A),
            onClick = { onSelectMode(AudioManager.RINGER_MODE_VIBRATE) },
            modifier = Modifier.weight(1f)
        )

        ProfileOption(
            title = "Silent",
            icon = Icons.Default.VolumeOff,
            isSelected = currentRingerMode == AudioManager.RINGER_MODE_SILENT,
            activeColor = Color(0xFFFF453A),
            onClick = { onSelectMode(AudioManager.RINGER_MODE_SILENT) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ProfileOption(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg by animateColorAsState(
        targetValue = if (isSelected) activeColor.copy(alpha = 0.22f) else Color.Transparent,
        animationSpec = tween(200),
        label = "profileBg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) activeColor else Color.White.copy(alpha = 0.6f),
        animationSpec = tween(200),
        label = "profileContentColor"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
