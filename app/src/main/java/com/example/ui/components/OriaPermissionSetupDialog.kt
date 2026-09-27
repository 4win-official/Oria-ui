package com.example.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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

/**
 * Modern, clean permission setup sheet for Oria UI.
 * Requests necessary privileges for total mobile UI transformation (Camera, Notifications, Default Home),
 * while allowing the user to gracefully continue anyway if declined.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OriaPermissionSetupDialog(
    isOpen: Boolean,
    hasCameraPermission: Boolean,
    hasNotificationPermission: Boolean,
    isDefaultLauncher: Boolean,
    onRequestPermissions: () -> Unit,
    onSetDefaultLauncher: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val cardBg = Color(0xCC1C1C1E)
    val bgColor = Color(0xFF000000)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bgColor,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState())
                .testTag("oria_permission_dialog"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "راه‌اندازی Oria UI (Setup)",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "برای تغییر تمام محیط موبایل، دسترسی‌های لازم را مشخص کنید",
                        fontSize = 11.5.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(cardBg)
                        .border(0.8.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .testTag("permission_dialog_close")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Permission Card 1: Camera & Torch
            PermissionItemCard(
                icon = Icons.Default.FlashlightOn,
                title = "دوربین و چراغ‌قوه (Camera & Torch)",
                subtitle = "فعال‌سازی چراغ‌قوه در کنترل سنتر، شورتکات دوربین و صفحه قفل استندبای.",
                isGranted = hasCameraPermission,
                accentColor = Color(0xFFFFD60A),
                cardBg = cardBg
            )

            // Permission Card 2: Notifications
            PermissionItemCard(
                icon = Icons.Default.NotificationsActive,
                title = "اعلان‌ها و هشدارهای پویا (Notifications)",
                subtitle = "ارسال پیام‌ها، وضعیت باتری، کنترل مدیا و هشدارهای نوتیفیکیشن سنتر.",
                isGranted = hasNotificationPermission,
                accentColor = Color(0xFF0A84FF),
                cardBg = cardBg
            )

            // Permission Card 3: Default Home App
            PermissionItemCard(
                icon = Icons.Default.Home,
                title = "لانچر پیش‌فرض (Default Home App)",
                subtitle = "تنظیم Oria UI به عنوان لانچر پیش‌فرض تا تمام محیط گوشی را متحول کند.",
                isGranted = isDefaultLauncher,
                accentColor = Color(0xFF30D158),
                cardBg = cardBg,
                actionLabel = if (!isDefaultLauncher) "تنظیم پیش‌فرض" else null,
                onAction = onSetDefaultLauncher
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Main Grant Action Button
            Button(
                onClick = {
                    onRequestPermissions()
                    if (!isDefaultLauncher) {
                        onSetDefaultLauncher()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("grant_all_permissions_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF))
            ) {
                Text(
                    text = "درخواست و اعطای تمام دسترسی‌ها (Grant All)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Secondary Continue Button (Allows continuing even if user does not want to grant immediately)
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("continue_without_permissions_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.85f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
            ) {
                Text(
                    text = "شروع به کار با لانچر (Continue to Oria UI)",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Text(
                text = "توجه: در صورت عدم اعطای دسترسی، لانچر بدون مشکل شروع به کار می‌کند و در بخش نوتیفیکیشن‌ها برای اعطای دسترسی به شما یادآوری خواهد کرد.",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.5f),
                lineHeight = 16.sp,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PermissionItemCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isGranted: Boolean,
    accentColor: Color,
    cardBg: Color,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(cardBg)
            .border(
                0.8.dp,
                if (isGranted) Color(0xFF30D158).copy(alpha = 0.4f) else Color.White.copy(alpha = 0.12f),
                RoundedCornerShape(18.dp)
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                if (isGranted) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF30D158).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "فعال شد ✓",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF30D158)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFF9F0A).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "لازم است ⚠️",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9F0A)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f),
                lineHeight = 15.sp
            )
        }

        if (actionLabel != null && onAction != null && !isGranted) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A84FF))
                    .clickable { onAction() }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = actionLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
