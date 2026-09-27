package com.example.data.model

/**
 * Represents an open or recently active app in the iOS App Switcher.
 *
 * @param app Metadata for the app item
 * @param memoryUsageMb Estimated RAM footprint in Megabytes
 * @param lastActiveTime Human-readable time label (e.g. "Just now", "2m ago")
 * @param isRunning True if running in background
 */
data class RecentAppItem(
    val app: AppItem,
    val memoryUsageMb: Int = 54,
    val lastActiveTime: String = "Just now",
    val isRunning: Boolean = true
)
