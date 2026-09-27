package com.example.data.model

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val appName: String,
    val packageName: String? = null,
    val time: String,
    val isPriority: Boolean = false,
    val category: String = "System",
    val subText: String? = null
)
