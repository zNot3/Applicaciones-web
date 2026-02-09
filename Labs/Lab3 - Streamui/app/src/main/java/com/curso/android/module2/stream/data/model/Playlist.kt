package com.curso.android.module2.stream.data.model

import kotlinx.serialization.Serializable

/**
 * ================================================================================
 * PLAYLIST.KT - Modelo de Playlist para Library
 * ================================================================================
 *
 * Representa una playlist del usuario en la biblioteca.
 *
 * Este modelo se agrega para soportar la nueva pantalla Library que muestra
 * las playlists guardadas del usuario.
 */

/**
 * Representa una playlist creada por el usuario.
 *
 * @property id Identificador único de la playlist
 * @property name Nombre de la playlist
 * @property description Descripción opcional
 * @property songCount Número de canciones en la playlist
 * @property colorSeed Semilla para generar el color del cover
 * @property songs Lista de canciones en la playlist (opcional, para detalle)
 */
@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val description: String = "",
    val songCount: Int,
    val colorSeed: Int,
    val songs: List<Song> = emptyList()
)
