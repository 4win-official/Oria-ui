package com.example.data.model

enum class LauncherTheme(
    val title: String,
    val primaryColor: Long,
    val secondaryColor: Long,
    val backgroundColor: Long,
    val surfaceColor: Long,
    val cardColor: Long
) {
    IOS_DARK(
        title = "Oria Dark (OLED)",
        primaryColor = 0xFF0A84FF, // Oria Electric Blue
        secondaryColor = 0xFF30D158, // Oria Neon Green
        backgroundColor = 0xFF000000,
        surfaceColor = 0xFF1C1C1E,
        cardColor = 0xCC2C2C2E
    ),
    MIDNIGHT_PRO(
        title = "Midnight Cyber (Navy)",
        primaryColor = 0xFF2997FF,
        secondaryColor = 0xFFBF5AF2,
        backgroundColor = 0xFF050B14,
        surfaceColor = 0xFF101B2B,
        cardColor = 0xCC16253B
    ),
    TITANIUM_GRAY(
        title = "Titanium Pro (Neutral)",
        primaryColor = 0xFFE5E5EA,
        secondaryColor = 0xFF8E8E93,
        backgroundColor = 0xFF121214,
        surfaceColor = 0xFF1C1C1E,
        cardColor = 0xCC2C2C2E
    ),
    SUNSET_GOLD(
        title = "Amber Horizon (Warm)",
        primaryColor = 0xFFFF9F0A,
        secondaryColor = 0xFFFF453A,
        backgroundColor = 0xFF0E0B08,
        surfaceColor = 0xFF1D1712,
        cardColor = 0xCC2C2219
    ),
    EMERALD_NEO(
        title = "Emerald Aurora (Fresh)",
        primaryColor = 0xFF30D158,
        secondaryColor = 0xFF64D2FF,
        backgroundColor = 0xFF03120A,
        surfaceColor = 0xFF0B2616,
        cardColor = 0xCC123822
    )
}

enum class GridDensity(val title: String, val columns: Int, val rows: Int) {
    COMPACT_4X4("4 × 4 Compact", 4, 4),
    BALANCED_4X5("4 × 5 Standard", 4, 5),
    HIGH_5X5("5 × 5 Dense", 5, 5)
}

enum class IconShape(val title: String, val cornerRadiusDp: Int) {
    SQUIRCLE("Oria Squircle", 22),
    ROUNDED_SQUARE("Soft Square", 14),
    CIRCLE("Circle", 50)
}

enum class StatusBarStyle(val title: String) {
    DYNAMIC_ISLAND("Oria Dynamic Island"),
    MINIMAL_STATUS("Minimal Status Pill")
}

enum class GestureSensitivity(val title: String, val thresholdDp: Float) {
    HIGH("Fast (30 dp)", 30f),
    BALANCED("Standard (45 dp)", 45f),
    LOW("Relaxed (60 dp)", 60f)
}

enum class HomeLayoutMode(val title: String, val subtitle: String) {
    CYBER_ORBIT("Oria Dynamic Home", "Widgets, app grid, spotlight search, and frosted dock"),
    MINIMAL_FLOW("Oria Cyber Deck", "Action nodes, hardware telemetry, and fast cards"),
    NEO_GLASS_GRID("Oria Glass Grid", "Translucent glass tiles with dynamic indicators")
}

data class LauncherSettings(
    val theme: LauncherTheme = LauncherTheme.IOS_DARK,
    val homeLayoutMode: HomeLayoutMode = HomeLayoutMode.CYBER_ORBIT,
    val gridDensity: GridDensity = GridDensity.BALANCED_4X5,
    val iconShape: IconShape = IconShape.SQUIRCLE,
    val statusBarStyle: StatusBarStyle = StatusBarStyle.DYNAMIC_ISLAND,
    val gestureSensitivity: GestureSensitivity = GestureSensitivity.BALANCED,
    val gesturesEnabled: Boolean = true,
    val isLowRamMode: Boolean = false,
    val showRamMonitor: Boolean = true,
    val volumePanelOnRight: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val useAmoledWallpaper: Boolean = true
)
