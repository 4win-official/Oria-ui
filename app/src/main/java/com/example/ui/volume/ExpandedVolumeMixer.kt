package com.example.ui.volume

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VolumeOverlayState
import com.example.data.model.VolumeStreamInfo
import kotlin.math.roundToInt

/**
 * Apple iOS Sound & Haptics Multi-Stream Volume Mixer.
 */
@Composable
fun ExpandedVolumeMixer(
    volumeState: VolumeOverlayState,
    primaryColor: Color,
    cardBg: Color,
    onSetVolume: (Int, Float) -> Unit,
    onToggleMute: (Int) -> Unit,
    onSetRingerMode: (Int) -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(300.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(cardBg)
            .border(0.8.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(26.dp))
            .padding(16.dp)
            .testTag("expanded_volume_mixer"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Sound & Haptics",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = null,
                        tint = Color(0xFF0A84FF),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = volumeState.outputDevice,
                        fontSize = 10.5.sp,
                        color = Color(0xFF0A84FF)
                    )
                }
            }

            IconButton(
                onClick = onCollapse,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.08f))
                    .testTag("expanded_volume_close_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White.copy(alpha = 0.75f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Ringer Mode Segmented Switcher
        VolumeProfileSelector(
            currentRingerMode = volumeState.ringerMode,
            primaryColor = primaryColor,
            cardBg = Color.White.copy(alpha = 0.05f),
            onSelectMode = onSetRingerMode
        )

        // Stream Sliders
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Media Stream
            MixerStreamRow(
                stream = volumeState.mediaStream,
                label = "Media",
                defaultIcon = Icons.Default.VolumeUp,
                primaryColor = Color(0xFF0A84FF),
                onProgressChange = { onSetVolume(volumeState.mediaStream.streamType, it) },
                onToggleMute = { onToggleMute(volumeState.mediaStream.streamType) }
            )

            // Ring Stream
            MixerStreamRow(
                stream = volumeState.ringStream,
                label = "Ringtone",
                defaultIcon = Icons.Default.PhoneInTalk,
                primaryColor = Color(0xFF30D158),
                onProgressChange = { onSetVolume(volumeState.ringStream.streamType, it) },
                onToggleMute = { onToggleMute(volumeState.ringStream.streamType) }
            )

            // Notification Stream
            MixerStreamRow(
                stream = volumeState.notificationStream,
                label = "Notifications",
                defaultIcon = Icons.Default.Notifications,
                primaryColor = Color(0xFFFFD60A),
                onProgressChange = { onSetVolume(volumeState.notificationStream.streamType, it) },
                onToggleMute = { onToggleMute(volumeState.notificationStream.streamType) }
            )

            // Alarm Stream
            MixerStreamRow(
                stream = volumeState.alarmStream,
                label = "Alarm",
                defaultIcon = Icons.Default.Alarm,
                primaryColor = Color(0xFFFF9F0A),
                onProgressChange = { onSetVolume(volumeState.alarmStream.streamType, it) },
                onToggleMute = { onToggleMute(volumeState.alarmStream.streamType) }
            )
        }
    }
}

@Composable
private fun MixerStreamRow(
    stream: VolumeStreamInfo,
    label: String,
    defaultIcon: ImageVector,
    primaryColor: Color,
    onProgressChange: (Float) -> Unit,
    onToggleMute: () -> Unit
) {
    val activeIcon = when {
        stream.isMuted || stream.progress <= 0.01f -> Icons.Default.VolumeOff
        stream.progress <= 0.35f -> Icons.Default.VolumeMute
        stream.progress <= 0.70f -> Icons.Default.VolumeDown
        else -> defaultIcon
    }

    val tint = if (stream.isMuted) Color(0xFFFF453A) else primaryColor

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(tint.copy(alpha = 0.15f))
                        .clickable { onToggleMute() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = activeIcon,
                        contentDescription = "Mute $label",
                        tint = tint,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Text(
                text = "${(stream.progress * 100).roundToInt()}%",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = tint
            )
        }

        Slider(
            value = stream.progress,
            onValueChange = onProgressChange,
            colors = SliderDefaults.colors(
                thumbColor = tint,
                activeTrackColor = tint,
                inactiveTrackColor = Color.White.copy(alpha = 0.1f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
        )
    }
}
