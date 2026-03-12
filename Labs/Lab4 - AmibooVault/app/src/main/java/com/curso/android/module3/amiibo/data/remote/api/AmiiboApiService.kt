package com.curso.android.module3.amiibo.data.remote.api

import com.curso.android.module3.amiibo.data.remote.model.AmiiboDetailResponse
import com.curso.android.module3.amiibo.data.remote.model.AmiiboResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AmiiboApiService {

    @GET("amiibo")
    suspend fun getAllAmiibos(): AmiiboResponse

    @GET("amiibo")
    suspend fun getAmiiboDetail(
        @Query("name") name: String,
        @Query("showgames") showGames: String = ""
    ): AmiiboDetailResponse

    companion object {
        //const val BASE_URL = "https://www.amiiboapi.com/api/"
        const val BASE_URL = "https://amiiboapi-7eg6.onrender.com/api/"
    }
}
