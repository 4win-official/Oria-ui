package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pinned_apps")
data class PinnedAppEntity(
    @PrimaryKey val packageName: String,
    val label: String,
    val isPinnedToHome: Boolean = false,
    val isPinnedToDock: Boolean = false,
    val homeOrder: Int = 0,
    val dockOrder: Int = 0
)
