package com.curso.android.module4.cityspots.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "spots")
data class SpotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val imageUri: String,

    val latitude: Double,
    val longitude: Double,

    val title: String,

    val timestamp: Long = System.currentTimeMillis()
)