package com.curso.android.module3.amiibo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "amiibos")
data class AmiiboEntity(

    @PrimaryKey
    val id: String,

    val name: String,

    val gameSeries: String,

    val imageUrl: String
)