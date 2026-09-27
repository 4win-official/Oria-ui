package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.data.model.HomeLayoutMode
import com.example.data.model.LauncherSettings
import com.example.data.repository.BatteryInfo
import com.example.data.repository.RamStats
import com.example.gesture.LauncherGestureService
import com.example.gesture.launcherGestures

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreenContent(
    timeString: String,
    dateString: String,
    batteryInfo: BatteryInfo,
    ramStats: RamStats,
    homeApps: List<AppItem>,
    dockApps: List<AppItem>,
    settings: LauncherSettings,
    gestureService: LauncherGestureService,
    onLaunchApp: (AppItem) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenRecentApps: () -> Unit = {},
    onOpenNotificationShade: () -> Unit,
    onOpenSettings: () -> Unit,
    onUnpinFromHome: (AppItem) -> Unit,
    onOpenAppInfo: (String) -> Unit,
    onLongPressApp: (AppItem) -> Unit = {},
    onOpenPowerMenu: () -> Unit = {},
    onDoubleTapHome: () -> Unit = onOpenSettings,
    onSelectLayoutMode: (HomeLayoutMode) -> Unit = {},
    onBoostRam: () -> Unit = {},
    onToggleFlashlight: () -> Unit = {},
    isFlashlightOn: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .launcherGestures(
                gestureService = gestureService,
                thresholdDp = settings.gestureSensitivity.thresholdDp,
                enabled = settings.gesturesEnabled,
                onSwipeUp = onOpenDrawer,
                onSwipeDown = onOpenNotificationShade,
                onDoubleTap = onDoubleTapHome,
                onLongPress = onOpenPowerMenu
            )
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(6.dp))

        when (settings.homeLayoutMode) {
            HomeLayoutMode.MINIMAL_FLOW -> {
                // 1. Oria Cyber Deck Mode (Live action nodes & quick cards)
                OriaCyberDeckView(
                    apps = homeApps,
                    settings = settings,
                    isFlashlightOn = isFlashlightOn,
                    onLaunchApp = onLaunchApp,
                    onLongPressApp = onLongPressApp,
                    onOpenRecentApps = onOpenRecentApps,
                    onOpenSettings = onOpenSettings,
                    onBoostRam = onBoostRam,
                    onToggleFlashlight = onToggleFlashlight,
                    modifier = Modifier.weight(1f)
                )
            }
            HomeLayoutMode.NEO_GLASS_GRID -> {
                // 2. Oria Glass Grid Mode (Widgets + Glassmorphic cards)
                Text(
                    text = dateString.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f),
                    letterSpacing = 0.6.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                IosWidgetsRow(
                    batteryInfo = batteryInfo,
                    ramStats = ramStats,
                    settings = settings,
                    onBoostRam = onBoostRam
                )

                Spacer(modifier = Modifier.height(10.dp))

                OriaNeoGlassGridView(
                    apps = homeApps,
                    settings = settings,
                    onLaunchApp = onLaunchApp,
                    onLongPressApp = onLongPressApp,
                    modifier = Modifier.weight(1f)
                )
            }
            HomeLayoutMode.CYBER_ORBIT -> {
                // 3. Oria Dynamic Home (Default: Widgets + Standard App Grid + Search Pill)
                Text(
                    text = dateString.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.85f),
                    letterSpacing = 0.6.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                IosWidgetsRow(
                    batteryInfo = batteryInfo,
                    ramStats = ramStats,
                    settings = settings,
                    onBoostRam = onBoostRam
                )

                Spacer(modifier = Modifier.height(14.dp))

                val gridColumns = settings.gridDensity.columns.coerceIn(4, 5)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(gridColumns),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(homeApps, key = { it.packageName }) { app ->
                            IosAppItem(
                                app = app,
                                settings = settings,
                                onClick = { onLaunchApp(app) },
                                onLongClick = { onLongPressApp(app) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Spotlight Search Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.18f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
                        .clickable { onOpenDrawer() }
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                        .testTag("ios_spotlight_pill"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Search",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 5. Authentic iOS Frosted Glass Floating Dock
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.18f))
                .border(
                    width = 0.8.dp,
                    color = Color.White.copy(alpha = 0.32f),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .testTag("ios_floating_dock")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                dockApps.take(4).forEach { app ->
                    IosDockItem(
                        app = app,
                        settings = settings,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onLongPressApp(app) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 6. iOS Home Bar (Swipe Up for App Switcher / Multitasking)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .pointerInput(Unit) {
                    detectVerticalDragGestures { _, dragAmount ->
                        if (dragAmount < -12f) {
                            onOpenRecentApps()
                        }
                    }
                }
                .clickable { onOpenRecentApps() }
                .testTag("ios_home_bar"),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(138.dp)
                    .height(4.5.dp)
                    .clip(RoundedCornerShape(2.5.dp))
                    .background(Color.White.copy(alpha = 0.95f))
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IosAppItem(
    app: AppItem,
    settings: LauncherSettings,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 2.dp)
            .testTag("app_${app.packageName}")
    ) {
        Box(
            modifier = Modifier.size(58.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            // iOS Squircle Icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                AppIconView(
                    app = app,
                    iconShape = settings.iconShape,
                    size = 54.dp
                )
            }

            // Notification Badge (Red dot)
            if (app.packageName.contains("message", ignoreCase = true) || app.packageName.contains("mail", ignoreCase = true)) {
                Box(
                    modifier = Modifier
                        .size(17.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFF3B30))
                        .border(1.5.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "1",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = app.label,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun IosDockItem(
    app: AppItem,
    settings: LauncherSettings,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        contentAlignment = Alignment.Center
    ) {
        AppIconView(
            app = app,
            iconShape = settings.iconShape,
            size = 52.dp
        )
    }
}
