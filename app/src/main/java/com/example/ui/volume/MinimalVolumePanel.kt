package com.example.ui.volume

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.LauncherSettings
import com.example.data.model.VolumeOverlayState

/**
 * Lightweight, minimalist custom volume control panel component for Jetpack Compose.
 * Intercepts and replaces Android's stock system volume UI with an ultra-responsive,
 * memory-efficient volume slider aligned directly with device hardware volume rockers.
 */
@Composable
fun MinimalVolumePanel(
    volumeState: VolumeOverlayState,
    settings: LauncherSettings,
    onSetVolume: (Int, Float) -> Unit,
    onToggleMute: (Int) -> Unit,
    onSetRingerMode: (Int) -> Unit,
    onSetExpanded: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(settings.theme.primaryColor)
    val cardBg = Color(settings.theme.cardColor).copy(alpha = if (settings.isLowRamMode) 0.98f else 0.92f)

    AnimatedVisibility(
        visible = volumeState.isVisible,
        enter = fadeIn(tween(140)) + slideInHorizontally(
            animationSpec = spring(
                dampingRatio = 0.82f,
                stiffness = Spring.StiffnessMedium
            ),
            initialOffsetX = { it }
        ),
        exit = fadeOut(tween(140)) + slideOutHorizontally(
            animationSpec = spring(
                dampingRatio = 0.9f,
                stiffness = Spring.StiffnessMedium
            ),
            targetOffsetX = { it }
        ),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (volumeState.isExpanded) {
                        Modifier
                            .background(Color.Black.copy(alpha = 0.35f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onSetExpanded(false) }
                    } else {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onDismiss() }
                    }
                )
                .padding(end = 14.dp, top = 72.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        // Hardware layer rendering for zero-latency motion
                        clip = false
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* prevent backdrop dismiss when interacting with panel */ }
            ) {
                if (!volumeState.isExpanded) {
                    // Minimalist Compact Single Slider Bar
                    CompactVolumeSlider(
                        stream = volumeState.mediaStream,
                        primaryColor = primaryColor,
                        cardBg = cardBg,
                        onSetVolume = { onSetVolume(volumeState.mediaStream.streamType, it) },
                        onToggleMute = { onToggleMute(volumeState.mediaStream.streamType) },
                        onExpandMixer = { onSetExpanded(true) }
                    )
                } else {
                    // Multi-Channel Audio Mixer Panel
                    ExpandedVolumeMixer(
                        volumeState = volumeState,
                        primaryColor = primaryColor,
                        cardBg = cardBg,
                        onSetVolume = onSetVolume,
                        onToggleMute = onToggleMute,
                        onSetRingerMode = onSetRingerMode,
                        onCollapse = { onSetExpanded(false) }
                    )
                }
            }
        }
    }
}
