package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PinnedAppDao {
    @Query("SELECT * FROM pinned_apps")
    fun getAllPinnedApps(): Flow<List<PinnedAppEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(app: PinnedAppEntity)

    @Query("DELETE FROM pinned_apps WHERE packageName = :pkg")
    suspend fun delete(pkg: String)

    @Query("UPDATE pinned_apps SET isPinnedToHome = :isPinned WHERE packageName = :pkg")
    suspend fun updateHomePin(pkg: String, isPinned: Boolean)

    @Query("UPDATE pinned_apps SET isPinnedToDock = :isDocked WHERE packageName = :pkg")
    suspend fun updateDockPin(pkg: String, isDocked: Boolean)
}
