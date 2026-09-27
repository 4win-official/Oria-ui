package com.example.data.model

import android.graphics.Bitmap

enum class AppCategory(val title: String) {
    ALL("All"),
    FAVORITES("Favorites"),
    COMMUNICATION("Social"),
    TOOLS("Utilities"),
    MEDIA("Media"),
    GAMES("Games");

    val faTitle: String get() = title
}

data class AppItem(
    val id: String,
    val label: String,
    val packageName: String,
    val activityName: String? = null,
    val iconBitmap: Bitmap? = null,
    val category: AppCategory = AppCategory.ALL,
    val isPinnedToHome: Boolean = false,
    val isPinnedToDock: Boolean = false,
    val badgeColor: Long = 0xFF0A84FF,
    val installTime: Long = 0L
)
