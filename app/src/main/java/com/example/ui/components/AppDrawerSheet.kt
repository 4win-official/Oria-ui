package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppCategory
import com.example.data.model.AppItem
import com.example.data.model.LauncherSettings
import kotlinx.coroutines.launch

/**
 * iOS App Library / Full App Drawer Sheet in clean English
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawerSheet(
    isOpen: Boolean,
    apps: List<AppItem>,
    searchQuery: String,
    selectedCategory: AppCategory,
    settings: LauncherSettings,
    onClose: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSelectCategory: (AppCategory) -> Unit,
    onLaunchApp: (AppItem) -> Unit,
    onPinToHome: (AppItem) -> Unit,
    onPinToDock: (AppItem) -> Unit,
    onOpenAppInfo: (String) -> Unit,
    onRequestUninstall: (String) -> Unit,
    onLongPressApp: (AppItem) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val bgColor = Color(0xF2000000)
    val cardBg = Color(0xCC1C1C1E)

    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()

    AnimatedVisibility(
        visible = isOpen,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor)
                .testTag("app_drawer_container")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                // Header with iOS Search Bar and Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("drawer_search_input"),
                        placeholder = {
                            Text(
                                "App Library (${apps.size} apps)",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(19.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear",
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0A84FF),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                            focusedContainerColor = cardBg,
                            unfocusedContainerColor = cardBg,
                            cursorColor = Color(0xFF0A84FF),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(cardBg)
                            .border(0.8.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                            .testTag("drawer_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Category Chips Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(AppCategory.entries.toTypedArray()) { category ->
                        val isSelected = category == selectedCategory
                        val chipBg = if (isSelected) Color(0xFF0A84FF) else cardBg
                        val chipText = Color.White

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(chipBg)
                                .border(
                                    0.8.dp,
                                    if (isSelected) Color(0xFF0A84FF) else Color.White.copy(alpha = 0.12f),
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelectCategory(category) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = category.title,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = chipText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Main App Grid
                if (apps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(cardBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Text(
                                text = "No Apps Found",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            if (searchQuery.isNotEmpty()) {
                                Text(
                                    text = "No results found for \"$searchQuery\"",
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.6f),
                                    textAlign = TextAlign.Center
                                )
                                OutlinedButton(
                                    onClick = { onSearchQueryChange("") },
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, Color(0xFF0A84FF))
                                ) {
                                    Text("Clear Search", color = Color(0xFF0A84FF), fontSize = 12.5.sp)
                                }
                            }
                        }
                    }
                } else {
                    Row(modifier = Modifier.fillMaxSize()) {
                        // Apps Grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(settings.gridDensity.columns),
                            state = gridState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            contentPadding = PaddingValues(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(apps, key = { it.packageName }) { app ->
                                DrawerAppItem(
                                    app = app,
                                    settings = settings,
                                    primaryColor = Color(0xFF0A84FF),
                                    onClick = { onLaunchApp(app) },
                                    onLongClick = { onLongPressApp(app) },
                                    onPinToHome = { onPinToHome(app) },
                                    onPinToDock = { onPinToDock(app) },
                                    onOpenAppInfo = { onOpenAppInfo(app.packageName) },
                                    onRequestUninstall = { onRequestUninstall(app.packageName) }
                                )
                            }
                        }

                        // Alphabet Quick Jump Scroller
                        Column(
                            modifier = Modifier
                                .width(20.dp)
                                .fillMaxHeight()
                                .padding(vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceEvenly
                        ) {
                            val alphabet = listOf("A", "B", "C", "D", "E", "F", "G", "M", "P", "S", "T", "W", "Z")
                            alphabet.forEach { letter ->
                                Text(
                                    text = letter,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier
                                        .clickable {
                                            val index = apps.indexOfFirst {
                                                it.label.startsWith(letter, ignoreCase = true)
                                            }
                                            if (index >= 0) {
                                                coroutineScope.launch {
                                                    gridState.animateScrollToItem(index)
                                                }
                                            }
                                        }
                                        .padding(vertical = 1.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrawerAppItem(
    app: AppItem,
    settings: LauncherSettings,
    primaryColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onPinToHome: () -> Unit,
    onPinToDock: () -> Unit,
    onOpenAppInfo: () -> Unit,
    onRequestUninstall: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(4.dp)
    ) {
        AppIconView(
            app = app,
            iconShape = settings.iconShape,
            size = if (settings.gridDensity.columns >= 5) 44.dp else 52.dp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = app.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            color = Color.White,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
