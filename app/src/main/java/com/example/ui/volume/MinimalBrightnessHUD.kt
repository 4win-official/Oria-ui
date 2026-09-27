package com.example.ui.volume

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Brightness5
import androidx.compose.material.icons.filled.Brightness7
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Minimalist floating on-screen brightness HUD overlay.
 * Appears dynamically upon brightness level adjustments to provide real-time tactile feedback.
 */
@Composable
fun MinimalBrightnessHUD(
    isVisible: Boolean,
    brightness: Float,
    primaryColor: Color,
    cardBg: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = brightness.coerceIn(0.05f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
        label = "brightnessProgress"
    )

    val icon = when {
        brightness < 0.33f -> Icons.Default.BrightnessLow
        brightness < 0.66f -> Icons.Default.Brightness5
        else -> Icons.Default.Brightness7
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(tween(140)) + slideInHorizontally(
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
            initialOffsetX = { -it }
        ),
        exit = fadeOut(tween(140)) + slideOutHorizontally(
            animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMedium),
            targetOffsetX = { -it }
        ),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 14.dp, top = 80.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column(
                modifier = Modifier
                    .width(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(cardBg)
                    .border(1.dp, primaryColor.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                    .padding(vertical = 10.dp, horizontal = 5.dp)
                    .testTag("minimal_brightness_hud"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(primaryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = "Display Brightness",
                        tint = primaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Text(
                    text = "${(brightness * 100).roundToInt()}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(110.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(animatedProgress)
                            .clip(RoundedCornerShape(8.dp))
                            .background(primaryColor)
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))
            }
        }
    }
}
