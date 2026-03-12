package com.curso.android.module3.amiibo.repository

import android.database.sqlite.SQLiteException
import com.curso.android.module3.amiibo.data.local.dao.AmiiboDao
import com.curso.android.module3.amiibo.data.local.entity.AmiiboEntity
import com.curso.android.module3.amiibo.data.remote.api.AmiiboApiService
import com.curso.android.module3.amiibo.data.local.entity.AmiiboDetailEntity
import com.curso.android.module3.amiibo.data.remote.model.AmiiboDetail
import com.curso.android.module3.amiibo.data.remote.model.toDetail
import com.curso.android.module3.amiibo.data.remote.model.toDomainModel
import com.curso.android.module3.amiibo.data.remote.model.toEntities
import com.curso.android.module3.amiibo.data.remote.model.toEntity
import com.curso.android.module3.amiibo.domain.error.AmiiboError
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationException
import java.io.IOException


class AmiiboRepository(
    private val amiiboDao: AmiiboDao,
    private val amiiboApiService: AmiiboApiService
) {

    fun observeAmiibos(): Flow<List<AmiiboEntity>> {
        return amiiboDao.getAllAmiibos()
    }

    // =========================================================================
    // PART 2 - LOCAL SEARCH
    // =========================================================================

    fun searchAmiibos(query: String): Flow<List<AmiiboEntity>> {
        return amiiboDao.searchAmiibos(query)
    }

    suspend fun refreshAmiibos() {
        try {
            val response = amiiboApiService.getAllAmiibos()
            val entities = response.amiibo.toEntities()

            try {
                amiiboDao.replaceAll(entities)
            } catch (e: SQLiteException) {
                throw AmiiboError.Database(cause = e)
            }

        } catch (e: AmiiboError) {
            throw e
        } catch (e: IOException) {
            throw AmiiboError.Network(cause = e)
        } catch (e: SerializationException) {
            throw AmiiboError.Parse(cause = e)
        } catch (e: Exception) {
            throw AmiiboError.Unknown(cause = e)
        }
    }

    suspend fun getAmiibosPage(page: Int, pageSize: Int): List<AmiiboEntity> {
        val offset = page * pageSize
        return amiiboDao.getAmiibosPage(limit = pageSize, offset = offset)
    }

    suspend fun getTotalCount(): Int {
        return amiiboDao.getTotalCount()
    }

    suspend fun hasMorePages(currentPage: Int, pageSize: Int): Boolean {
        val total = getTotalCount()
        val loaded = (currentPage + 1) * pageSize
        return loaded < total
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        val PAGE_SIZE_OPTIONS = listOf(20, 50, 100)
    }

    suspend fun getAmiiboDetail(name: String): AmiiboDetail {
        try {
            val cachedDetail = try {
                amiiboDao.getDetailByName(name)
            } catch (e: SQLiteException) {
                throw AmiiboError.Database(cause = e)
            }

            if (cachedDetail != null) {
                return cachedDetail.toDomainModel()
            }

            val response = amiiboApiService.getAmiiboDetail(name)
            val detail = response.amiibo.first().toDetail()

            try {
                amiiboDao.insertDetail(detail.toEntity())
            } catch (e: SQLiteException) {
                throw AmiiboError.Database(cause = e)
            }

            return detail

        } catch (e: AmiiboError) {
            throw e
        } catch (e: IOException) {
            throw AmiiboError.Network(cause = e)
        } catch (e: SerializationException) {
            throw AmiiboError.Parse(cause = e)
        } catch (e: NoSuchElementException) {
            throw AmiiboError.Parse(
                message = "No se encontró el Amiibo '$name'",
                cause = e
            )
        } catch (e: Exception) {
            throw AmiiboError.Unknown(cause = e)
        }
    }

    fun getAmiiboCount(): Flow<Int> {
        return amiiboDao.getCount()
    }
}
