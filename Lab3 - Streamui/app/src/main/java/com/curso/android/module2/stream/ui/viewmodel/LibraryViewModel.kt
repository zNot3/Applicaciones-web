package com.curso.android.module2.stream.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.curso.android.module2.stream.data.model.Playlist
import com.curso.android.module2.stream.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ================================================================================
 * LIBRARY VIEW MODEL - Lógica de la Biblioteca del Usuario
 * ================================================================================
 *
 * ViewModel para la pantalla Library que muestra las playlists del usuario.
 *
 * PATRÓN MVVM APLICADO:
 * - Expone el estado de las playlists como StateFlow
 * - La UI observa el estado y se recompone automáticamente
 * - Sigue el mismo patrón que HomeViewModel y SearchViewModel
 *
 * COMPARACIÓN CON TABS:
 * --------------------
 * Este ViewModel es parte del sistema de BottomNavigation.
 * Cada tab (Home, Search, Library) tiene su propio ViewModel
 * que mantiene su estado independiente.
 */

/**
 * Estado de la pantalla Library.
 */
sealed interface LibraryUiState {
    /**
     * Estado inicial mientras se cargan los datos.
     */
    data object Loading : LibraryUiState

    /**
     * Datos cargados exitosamente.
     *
     * @property playlists Lista de playlists del usuario
     */
    data class Success(
        val playlists: List<Playlist>
    ) : LibraryUiState

    /**
     * Error al cargar los datos.
     *
     * @property message Mensaje descriptivo del error
     */
    data class Error(
        val message: String
    ) : LibraryUiState
}

/**
 * ViewModel para la pantalla Library.
 *
 * @param repository Repositorio de música (inyectado por Koin)
 */
class LibraryViewModel(
    private val repository: MusicRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        loadPlaylists()
    }

    /**
     * Carga las playlists del usuario.
     */
    private fun loadPlaylists() {
        _uiState.value = LibraryUiState.Loading

        val playlists = repository.getPlaylists()
        _uiState.value = LibraryUiState.Success(playlists)
    }

    /**
     * Recarga las playlists.
     */
    fun refresh() {
        loadPlaylists()
    }
}
