package com.curso.android.module4.cityspots.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.curso.android.module4.cityspots.data.entity.SpotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpotDao {

    @Query("SELECT * FROM spots ORDER BY timestamp DESC")
    fun getAllSpots(): Flow<List<SpotEntity>>

    @Query("SELECT * FROM spots WHERE id = :id")
    suspend fun getSpotById(id: Long): SpotEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertSpot(spot: SpotEntity): Long

    @Query("SELECT COUNT(*) FROM spots")
    suspend fun getSpotCount(): Int

    // =========================================================================
    // Part 2: Eliminación de Spot
    // =========================================================================

    @Query("DELETE FROM spots WHERE id = :id")
    suspend fun deleteSpot(id: Long)
}
