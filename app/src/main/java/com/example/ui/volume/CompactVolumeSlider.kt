package com.example.ui.volume

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VolumeStreamInfo
import kotlin.math.roundToInt

/**
 * Minimalist vertical pill volume slider.
 * Highly responsive direct-touch control positioned adjacent to device hardware volume keys.
 * Uses lightweight layout passes and hardware layer clipping for ultra-low memory consumption.
 */
@Composable
fun CompactVolumeSlider(
    stream: VolumeStreamInfo,
    primaryColor: Color,
    cardBg: Color,
    onSetVolume: (Float) -> Unit,
    onToggleMute: () -> Unit,
    onExpandMixer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = stream.progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "volumeProgress"
    )

    val activeIcon = when {
        stream.isMuted || stream.progress <= 0.01f -> Icons.Default.VolumeOff
        stream.progress <= 0.35f -> Icons.Default.VolumeMute
        stream.progress <= 0.70f -> Icons.Default.VolumeDown
        else -> Icons.Default.VolumeUp
    }

    val iconTint by animateColorAsState(
        targetValue = if (stream.isMuted) Color(0xFFFF5252) else primaryColor,
        animationSpec = tween(200),
        label = "iconTint"
    )

    Column(
        modifier = modifier
            .width(50.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(cardBg)
            .border(
                1.dp,
                if (stream.isMuted) Color(0xFFFF5252).copy(alpha = 0.5f) else primaryColor.copy(alpha = 0.35f),
                RoundedCornerShape(26.dp)
            )
            .padding(vertical = 10.dp, horizontal = 5.dp)
            .testTag("compact_volume_slider"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Quick Mute / Speaker Icon Button
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (stream.isMuted) Color(0xFFFF5252).copy(alpha = 0.2f) else primaryColor.copy(alpha = 0.15f))
                .clickable { onToggleMute() }
                .testTag("compact_volume_mute_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = activeIcon,
                contentDescription = "Mute Volume",
                tint = iconTint,
                modifier = Modifier.size(19.dp)
            )
        }

        // Percentage readout
        Text(
            text = "${(stream.progress * 100).roundToInt()}%",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        // Interactive Vertical Track Bar
        Box(
            modifier = Modifier
                .width(18.dp)
                .height(130.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(Color.White.copy(alpha = 0.12f))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val ratio = (1f - (offset.y / size.height)).coerceIn(0f, 1f)
                        onSetVolume(ratio)
                    }
                }
                .pointerInput(Unit) {
                    detectVerticalDragGestures { change, _ ->
                        val ratio = (1f - (change.position.y / size.height)).coerceIn(0f, 1f)
                        onSetVolume(ratio)
                    }
                },
            contentAlignment = Alignment.BottomCenter
        ) {
            // Filled progress layer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(animatedProgress)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (stream.isMuted) Color(0xFFFF5252).copy(alpha = 0.85f) else primaryColor)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Expand Multi-Channel Mixer Button
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
                .clickable { onExpandMixer() }
                .testTag("compact_volume_expand_button"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = "Expand Volume Mixer",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(17.dp)
            )
        }
    }
}
