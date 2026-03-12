package com.curso.android.module3.amiibo.data.remote.model

import com.curso.android.module3.amiibo.data.local.entity.AmiiboEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AmiiboResponse(

    val amiibo: List<AmiiboDto>
)

@Serializable
data class AmiiboDto(

    val head: String,

    val tail: String,

    val name: String,

    @SerialName("gameSeries")
    val gameSeries: String,

    val image: String
)

fun AmiiboDto.toEntity(): AmiiboEntity {
    return AmiiboEntity(
        // Combinamos head y tail para crear un ID único
        // Formato: "00000000-00000002"
        id = "$head-$tail",
        name = name,
        gameSeries = gameSeries,
        imageUrl = image
    )
}

fun List<AmiiboDto>.toEntities(): List<AmiiboEntity> {
    return map { it.toEntity() }
}