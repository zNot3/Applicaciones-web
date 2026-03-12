package com.curso.android.module3.amiibo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "amiibo_details")
data class AmiiboDetailEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val character: String,
    val gameSeries: String,
    val amiiboSeries: String,
    val type: String,
    val imageUrl: String,
    val releaseNA: String?,
    val releaseEU: String?,
    val releaseJP: String?,
    val releaseAU: String?,
    // Juegos compatibles guardados como JSON string
    val compatibleGamesJson: String
)
