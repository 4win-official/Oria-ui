package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppItem
import com.example.data.model.LauncherSettings
import kotlinx.coroutines.launch

/**
 * 1. CYBER DECK VIEW (Futuristic action nodes and fast access cards)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OriaCyberDeckView(
    apps: List<AppItem>,
    settings: LauncherSettings,
    isFlashlightOn: Boolean,
    onLaunchApp: (AppItem) -> Unit,
    onLongPressApp: (AppItem) -> Unit,
    onOpenRecentApps: () -> Unit,
    onOpenSettings: () -> Unit,
    onBoostRam: () -> Unit,
    onToggleFlashlight: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(settings.theme.primaryColor)
    val secondaryColor = Color(settings.theme.secondaryColor)
    val cardBg = Color(settings.theme.cardColor).copy(alpha = if (settings.isLowRamMode) 0.94f else 0.72f)

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section A: Quick Command Hub (4 Action Nodes)
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(cardBg)
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(primaryColor.copy(alpha = 0.35f), secondaryColor.copy(alpha = 0.2f))
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CyberActionNode(
                    icon = Icons.Default.RocketLaunch,
                    label = "Turbo Boost",
                    accentColor = primaryColor,
                    isActive = false,
                    onClick = onBoostRam
                )
                CyberActionNode(
                    icon = Icons.Default.FlashlightOn,
                    label = "Flashlight",
                    accentColor = if (isFlashlightOn) Color(0xFFFFD600) else primaryColor,
                    isActive = isFlashlightOn,
                    onClick = onToggleFlashlight
                )
                CyberActionNode(
                    icon = Icons.Default.Layers,
                    label = "Recents",
                    accentColor = secondaryColor,
                    isActive = false,
                    onClick = onOpenRecentApps
                )
                CyberActionNode(
                    icon = Icons.Default.Settings,
                    label = "Settings",
                    accentColor = primaryColor,
                    isActive = false,
                    onClick = onOpenSettings
                )
            }
        }

        // Section B: Section Header
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Access Cards",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = "Oria UI",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryColor
                )
            }
        }

        // Section C: Holographic Glass Cards for Home Apps
        items(apps, key = { it.packageName }) { app ->
            HolographicAppCard(
                app = app,
                settings = settings,
                onClick = { onLaunchApp(app) },
                onLongClick = { onLongPressApp(app) }
            )
        }
    }
}

@Composable
private fun CyberActionNode(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isActive) accentColor.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.08f))
                .border(
                    width = 1.dp,
                    color = if (isActive) accentColor else accentColor.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) accentColor else Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = if (isActive) accentColor else Color.White.copy(alpha = 0.75f),
            maxLines = 1
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HolographicAppCard(
    app: AppItem,
    settings: LauncherSettings,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val primaryColor = Color(settings.theme.primaryColor)
    val cardBg = Color(settings.theme.cardColor).copy(alpha = if (settings.isLowRamMode) 0.94f else 0.78f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .border(
                width = 1.dp,
                color = primaryColor.copy(alpha = 0.22f),
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 10.dp, vertical = 10.dp)
            .testTag("cyber_app_card_${app.packageName}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // App Icon with glow backdrop
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.06f)),
            contentAlignment = Alignment.Center
        ) {
            AppIconView(
                app = app,
                iconShape = settings.iconShape,
                size = 36.dp
            )
        }

        // App Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                )
                Text(
                    text = app.category.faTitle,
                    fontSize = 10.sp,
                    color = primaryColor.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }
        }

        // Quick Chevron
        Icon(
            imageVector = Icons.Default.ChevronLeft,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(16.dp)
        )
    }
}

/**
 * 2. MINIMAL ZEN VIEW (High-speed waterfall view with alphabet index)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OriaMinimalZenView(
    apps: List<AppItem>,
    settings: LauncherSettings,
    onLaunchApp: (AppItem) -> Unit,
    onLongPressApp: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(settings.theme.primaryColor)
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    val alphabet = listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#")

    Row(modifier = modifier.fillMaxSize()) {
        // Main Waterfall List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(apps, key = { it.packageName }) { app ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .combinedClickable(
                            onClick = { onLaunchApp(app) },
                            onLongClick = { onLongPressApp(app) }
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AppIconView(
                        app = app,
                        iconShape = settings.iconShape,
                        size = 32.dp
                    )
                    Text(
                        text = app.label,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(primaryColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = app.category.faTitle,
                            fontSize = 9.sp,
                            color = primaryColor
                        )
                    }
                }
            }
        }

        // Side Alphabet Quick Rail
        Column(
            modifier = Modifier
                .padding(start = 4.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            alphabet.forEach { letter ->
                Text(
                    text = letter,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            val targetIndex = apps.indexOfFirst {
                                it.label.startsWith(letter, ignoreCase = true)
                            }
                            if (targetIndex >= 0) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(targetIndex)
                                }
                            }
                        }
                        .padding(horizontal = 3.dp, vertical = 1.dp)
                )
            }
        }
    }
}

/**
 * 3. NEO GLASS GRID VIEW (Glassmorphic cards with subtle glowing borders)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OriaNeoGlassGridView(
    apps: List<AppItem>,
    settings: LauncherSettings,
    onLaunchApp: (AppItem) -> Unit,
    onLongPressApp: (AppItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(settings.theme.primaryColor)
    val cardBg = Color(settings.theme.cardColor).copy(alpha = if (settings.isLowRamMode) 0.94f else 0.75f)

    LazyVerticalGrid(
        columns = GridCells.Fixed(settings.gridDensity.columns),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(apps, key = { it.packageName }) { app ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(cardBg)
                    .border(
                        1.dp,
                        primaryColor.copy(alpha = 0.2f),
                        RoundedCornerShape(18.dp)
                    )
                    .combinedClickable(
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onLongPressApp(app) }
                    )
                    .padding(vertical = 12.dp, horizontal = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AppIconView(
                    app = app,
                    iconShape = settings.iconShape,
                    size = 46.dp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = app.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
