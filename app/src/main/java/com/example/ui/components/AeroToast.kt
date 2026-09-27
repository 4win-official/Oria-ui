package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AeroToastData
import com.example.data.model.ToastIconType

/**
 * Sleek, dynamic island style floating heads-up banner replacing stock Android toasts.
 * Provides instant tactile and visual feedback for user actions and system changes.
 */
@Composable
fun AeroToast(
    toast: AeroToastData?,
    primaryColor: Color,
    cardBg: Color,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = toast != null,
        enter = fadeIn(tween(150)) + slideInVertically(
            animationSpec = spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMedium),
            initialOffsetY = { -it }
        ),
        exit = fadeOut(tween(150)) + slideOutVertically(
            animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMedium),
            targetOffsetY = { -it }
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        if (toast != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                val (icon, iconColor) = when (toast.iconType) {
                    ToastIconType.SUCCESS -> Icons.Default.CheckCircle to Color(0xFF00E676)
                    ToastIconType.WARNING -> Icons.Default.Warning to Color(0xFFFFB300)
                    ToastIconType.BATTERY -> Icons.Default.BatteryChargingFull to Color(0xFF69F0AE)
                    ToastIconType.VOLUME -> Icons.Default.VolumeUp to primaryColor
                    ToastIconType.SPEED -> Icons.Default.Speed to Color(0xFF00E5FF)
                    ToastIconType.LOCK -> Icons.Default.Lock to Color(0xFFFF5252)
                    ToastIconType.PIN -> Icons.Default.PushPin to primaryColor
                    ToastIconType.INFO -> Icons.Default.Info to primaryColor
                }

                Row(
                    modifier = Modifier
                        .widthIn(min = 180.dp, max = 360.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(cardBg.copy(alpha = 0.96f))
                        .border(1.dp, iconColor.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                        .padding(horizontal = 14.dp, vertical = 9.dp)
                        .testTag("aero_toast_banner"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(iconColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Text(
                            text = toast.message,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (!toast.subMessage.isNullOrBlank()) {
                            Text(
                                text = toast.subMessage,
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }
        }
    }
}
