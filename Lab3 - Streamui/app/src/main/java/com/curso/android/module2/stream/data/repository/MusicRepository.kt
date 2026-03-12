package com.curso.android.module2.stream.data.repository

import com.curso.android.module2.stream.data.model.Category
import com.curso.android.module2.stream.data.model.Playlist
import com.curso.android.module2.stream.data.model.Song

/**
 * ================================================================================
 * MUSIC REPOSITORY INTERFACE
 * ================================================================================
 *
 * PRINCIPIO DE INVERSIÓN DE DEPENDENCIAS (DIP)
 * ---------------------------------------------
 * El último principio de SOLID establece que:
 * - Los módulos de alto nivel no deben depender de módulos de bajo nivel.
 * - Ambos deben depender de abstracciones (interfaces).
 *
 * ANTES (sin interface):
 * ----------------------
 * ```kotlin
 * class SearchViewModel(
 *     private val repository: MockMusicRepository  // ❌ Depende de implementación
 * )
 * ```
 * Problemas:
 * 1. No puedes cambiar fácilmente a RemoteMusicRepository
 * 2. En tests, estás forzado a usar MockMusicRepository
 * 3. Acoplamiento fuerte entre ViewModel y Repository concreto
 *
 * DESPUÉS (con interface):
 * ------------------------
 * ```kotlin
 * class SearchViewModel(
 *     private val repository: MusicRepository  // ✅ Depende de abstracción
 * )
 * ```
 * Beneficios:
 * 1. En producción: Inyecta RemoteMusicRepository
 * 2. En tests: Inyecta TestMusicRepository o un mock
 * 3. El ViewModel no conoce la implementación concreta
 *
 * TESTABILIDAD
 * ------------
 * Con una interface, puedes crear fácilmente mocks para testing:
 *
 * ```kotlin
 * class FakeMusicRepository : MusicRepository {
 *     override fun getCategories() = listOf(testCategory)
 *     override fun getSongById(songId: String) = testSong
 *     override fun getAllSongs() = listOf(testSong)
 * }
 *
 * @Test
 * fun `test search returns matching songs`() {
 *     val viewModel = SearchViewModel(FakeMusicRepository())
 *     viewModel.updateQuery("test")
 *     // Assert...
 * }
 * ```
 *
 * NOTA: Los tests unitarios están fuera del alcance de este módulo educativo,
 * pero la arquitectura está preparada para agregarlos fácilmente.
 */
interface MusicRepository {

    /**
     * Obtiene todas las categorías con sus canciones.
     *
     * @return Lista de categorías disponibles
     */
    fun getCategories(): List<Category>

    /**
     * Busca una canción por su ID.
     *
     * @param songId ID de la canción a buscar
     * @return La canción encontrada o null si no existe
     */
    fun getSongById(songId: String): Song?

    /**
     * Obtiene todas las canciones de todas las categorías.
     *
     * @return Lista plana de todas las canciones disponibles
     */
    fun getAllSongs(): List<Song>

    /**
     * Obtiene las playlists del usuario (para la pantalla Library).
     *
     * @return Lista de playlists guardadas
     */
    fun getPlaylists(): List<Playlist>
}
