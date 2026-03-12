package com.curso.android.module3.amiibo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.curso.android.module3.amiibo.data.local.entity.AmiiboDetailEntity
import com.curso.android.module3.amiibo.data.local.entity.AmiiboEntity
import kotlinx.coroutines.flow.Flow

/**
 * ============================================================================
 * AMIIBO DAO - Data Access Object (Room)
 * ============================================================================
 */
@Dao
interface AmiiboDao {

    @Query("SELECT * FROM amiibos ORDER BY name ASC")
    fun getAllAmiibos(): Flow<List<AmiiboEntity>>

    // =========================================================================
    // PART 2 - LOCAL SEARCH
    // =========================================================================
    @Query("SELECT * FROM amiibos WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchAmiibos(query: String): Flow<List<AmiiboEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(amiibos: List<AmiiboEntity>)

    @Query("DELETE FROM amiibos")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(amiibos: List<AmiiboEntity>) {
        deleteAll()
        insertAll(amiibos)
    }

    @Query("SELECT COUNT(*) FROM amiibos")
    fun getCount(): Flow<Int>

    @Query("SELECT * FROM amiibos ORDER BY name ASC LIMIT :limit OFFSET :offset")
    suspend fun getAmiibosPage(limit: Int, offset: Int): List<AmiiboEntity>

    @Query("SELECT COUNT(*) FROM amiibos")
    suspend fun getTotalCount(): Int

    @Query("SELECT * FROM amiibo_details WHERE name = :name LIMIT 1")
    suspend fun getDetailByName(name: String): AmiiboDetailEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetail(detail: AmiiboDetailEntity)
}