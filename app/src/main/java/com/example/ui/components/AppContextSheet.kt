package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.data.model.LauncherSettings

/**
 * Apple iOS Haptic Touch Context Menu Platter
 */
@Composable
fun AppContextSheet(
    app: AppItem?,
    settings: LauncherSettings,
    isHomePinned: Boolean = false,
    isDockPinned: Boolean = false,
    onDismiss: () -> Unit,
    onTogglePinHome: () -> Unit,
    onTogglePinDock: () -> Unit = {},
    onOpenAppInfo: () -> Unit,
    onRequestUninstall: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBg = Color(0xE61C1C1E)

    AnimatedVisibility(
        visible = app != null,
        enter = fadeIn(tween(140)) + scaleIn(
            animationSpec = spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMedium),
            initialScale = 0.88f
        ),
        exit = fadeOut(tween(120)) + scaleOut(
            animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMedium),
            targetScale = 0.92f
        ),
        modifier = modifier.fillMaxSize()
    ) {
        if (app != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() }
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .widthIn(max = 320.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp))
                        .background(cardBg)
                        .border(0.8.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(26.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* prevent dismiss */ }
                        .padding(18.dp)
                        .testTag("app_context_sheet"),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // App Identity Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIconView(
                            app = app,
                            iconShape = settings.iconShape,
                            size = 46.dp
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.label,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = app.packageName,
                                fontSize = 10.5.sp,
                                color = Color.White.copy(alpha = 0.5f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // Action Rows in iOS Table Inset Style
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pin / Unpin Home
                        ContextActionRow(
                            title = if (isHomePinned) "Remove from Home Screen" else "Add to Home Screen",
                            icon = Icons.Default.PushPin,
                            tint = Color(0xFF0A84FF),
                            onClick = {
                                onTogglePinHome()
                                onDismiss()
                            }
                        )

                        // Pin / Unpin Dock
                        ContextActionRow(
                            title = if (isDockPinned) "Remove from Dock" else "Add to Dock",
                            icon = Icons.Default.Star,
                            tint = Color(0xFFFFD60A),
                            onClick = {
                                onTogglePinDock()
                                onDismiss()
                            }
                        )

                        // App System Info
                        ContextActionRow(
                            title = "App Info & Permissions",
                            icon = Icons.Default.Info,
                            tint = Color(0xFF30D158),
                            onClick = {
                                onOpenAppInfo()
                                onDismiss()
                            }
                        )

                        // Delete / Uninstall
                        ContextActionRow(
                            title = "Delete App",
                            icon = Icons.Default.Delete,
                            tint = Color(0xFFFF453A),
                            isDestructive = true,
                            onClick = {
                                onRequestUninstall()
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContextActionRow(
    title: String,
    icon: ImageVector,
    tint: Color,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isDestructive) Color(0xFFFF453A).copy(alpha = 0.12f) else Color.White.copy(alpha = 0.06f))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = if (isDestructive) FontWeight.Bold else FontWeight.Medium,
            color = if (isDestructive) Color(0xFFFF453A) else Color.White
        )

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}
