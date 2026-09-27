package com.example.ui.shade

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LauncherSettings
import com.example.data.model.NotificationItem
import com.example.data.repository.BatteryInfo
import com.example.data.repository.RamStats
import kotlin.math.roundToInt

/**
 * Lightweight, modular replacement for the system notification shade in Jetpack Compose.
 *
 * Performance and Memory Architecture:
 * 1. Low-RAM Mode Aware: Bypasses expensive GPU blur render passes on budget devices,
 *    substituting with high-performance gradient scrims (0 extra GPU texture MBs).
 * 2. Smooth Blur Transition: On devices with Turbo off, animates backdrop blur smoothly from 0 to 22.dp.
 * 3. Modular Composition: Assembles ShadeHeader, QuickSettings, Brightness/Volume,
 *    Media Mini-Player, and Notification Center in a single performant LazyColumn.
 * 4. Interactive Drag-to-Dismiss: Handles vertical drag-up gestures with responsive finger tracking.
 */
@Composable
fun NotificationShadeSheet(
    isOpen: Boolean,
    timeString: String,
    dateString: String,
    batteryInfo: BatteryInfo,
    ramStats: RamStats,
    isFlashlightOn: Boolean,
    isLowRamMode: Boolean,
    brightnessLevel: Float,
    mediaVolumeProgress: Float,
    notifications: List<NotificationItem>,
    settings: LauncherSettings,
    onClose: () -> Unit,
    onToggleFlashlight: () -> Unit,
    onToggleLowRamMode: () -> Unit,
    onToggleRingerMode: () -> Unit,
    onSetBrightness: (Float) -> Unit,
    onSetVolume: (Float) -> Unit,
    onDismissNotification: (String) -> Unit,
    onClearAllNotifications: () -> Unit,
    onAddSampleNotification: (String, String) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenPowerMenu: () -> Unit = {},
    onNotificationAction: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(settings.theme.primaryColor)
    val cardBg = Color(settings.theme.cardColor).copy(alpha = if (isLowRamMode) 0.96f else 0.82f)
    val sheetBg = Color(settings.theme.backgroundColor).copy(alpha = if (isLowRamMode) 0.98f else 0.90f)

    // Smooth blur transition radius: 0.dp when closed or in Low-RAM Mode, 22.dp when open in standard mode
    val animatedBlurRadius by animateDpAsState(
        targetValue = if (isOpen && !isLowRamMode) 22.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "shadeBlur"
    )

    // Backdrop scrim alpha animation
    val animatedScrimAlpha by animateFloatAsState(
        targetValue = if (isOpen) 0.65f else 0f,
        animationSpec = tween(250),
        label = "scrimAlpha"
    )

    // Interactive drag offset to allow sliding up to dismiss
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn(tween(220)) + slideInVertically(
            animationSpec = spring(dampingRatio = 0.85f, stiffness = 420f),
            initialOffsetY = { -it / 2 }
        ),
        exit = fadeOut(tween(180)) + slideOutVertically(
            animationSpec = spring(dampingRatio = 0.9f, stiffness = 480f),
            targetOffsetY = { -it / 2 }
        ),
        modifier = modifier.fillMaxSize()
    ) {
        // Full screen container with smooth blur / scrim backdrop
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!isLowRamMode && animatedBlurRadius > 0.dp) {
                        Modifier.blur(animatedBlurRadius)
                    } else {
                        Modifier
                    }
                )
                .background(Color.Black.copy(alpha = animatedScrimAlpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onClose() }
        ) {
            // Main Shade Sheet container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .offset { IntOffset(0, dragOffsetY.coerceAtMost(0f).roundToInt()) }
                    .graphicsLayer {
                        // Smooth hardware-accelerated translation
                        translationY = dragOffsetY.coerceAtMost(0f)
                    }
                    .draggable(
                        orientation = Orientation.Vertical,
                        state = rememberDraggableState { delta ->
                            // Only allow dragging upwards to dismiss
                            dragOffsetY = (dragOffsetY + delta).coerceAtMost(0f)
                        },
                        onDragStopped = {
                            if (dragOffsetY < -120f) {
                                onClose()
                            }
                            dragOffsetY = 0f
                        }
                    )
                    .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                sheetBg,
                                sheetBg.copy(alpha = if (isLowRamMode) 1f else 0.94f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        primaryColor.copy(alpha = 0.25f),
                        RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* intercept clicks */ }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Top Drag Handle Pill
                    item(key = "drag_handle") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(44.dp)
                                    .height(5.dp)
                                    .clip(CircleShape)
                                    .background(primaryColor.copy(alpha = 0.45f))
                            )
                        }
                    }

                    // 1. Header Module (Clock, Date, Battery, RAM Footprint, Actions)
                    item(key = "header_module") {
                        ShadeHeaderModule(
                            timeString = timeString,
                            dateString = dateString,
                            batteryInfo = batteryInfo,
                            ramStats = ramStats,
                            isLowRamMode = isLowRamMode,
                            primaryColor = primaryColor,
                            cardBg = cardBg,
                            onOpenSettings = onOpenSettings,
                            onAddSampleNotification = {
                                onAddSampleNotification(
                                    "Calendar Alert",
                                    "Team Sync at 3:00 PM in Conference Room"
                                )
                            },
                            onOpenPowerMenu = onOpenPowerMenu,
                            onClose = onClose
                        )
                    }

                    // 2. Quick Settings Toggles Module
                    item(key = "quick_settings_module") {
                        QuickSettingsModule(
                            isFlashlightOn = isFlashlightOn,
                            isLowRamMode = isLowRamMode,
                            primaryColor = primaryColor,
                            cardBg = cardBg,
                            onToggleFlashlight = onToggleFlashlight,
                            onToggleLowRamMode = onToggleLowRamMode,
                            onToggleRingerMode = onToggleRingerMode,
                            onOpenThemeSettings = onOpenSettings
                        )
                    }

                    // 3. Brightness & Media Volume Sliders Module
                    item(key = "brightness_volume_module") {
                        BrightnessVolumeModule(
                            brightnessLevel = brightnessLevel,
                            mediaVolumeProgress = mediaVolumeProgress,
                            primaryColor = primaryColor,
                            cardBg = cardBg,
                            onSetBrightness = onSetBrightness,
                            onSetVolume = onSetVolume
                        )
                    }

                    // 4. Compact Media Player Module
                    item(key = "media_playback_module") {
                        MediaPlaybackModule(
                            primaryColor = primaryColor,
                            cardBg = cardBg
                        )
                    }

                    // 5. Notification Center Module
                    item(key = "notification_center_module") {
                        NotificationCenterModule(
                            notifications = notifications,
                            primaryColor = primaryColor,
                            cardBg = cardBg,
                            onDismissNotification = onDismissNotification,
                            onClearAllNotifications = onClearAllNotifications,
                            onNotificationAction = onNotificationAction
                        )
                    }

                    // Bottom Dismiss Hint & Padding
                    item(key = "bottom_dismiss_hint") {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onClose() }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Close Control Center",
                                tint = primaryColor.copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Swipe up to close",
                                fontSize = 10.sp,
                                color = Color.White.copy(alpha = 0.45f)
                            )
                        }
                    }
                }
            }
        }
    }
}
