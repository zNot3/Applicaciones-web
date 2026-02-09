package com.curso.android.module2.stream.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.curso.android.module2.stream.data.model.Song
import com.curso.android.module2.stream.data.repository.MusicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ================================================================================
 * SEARCH VIEW MODEL - Lógica de Búsqueda
 * ================================================================================
 *
 * Este ViewModel demuestra cómo manejar EVENTOS de la UI (input del usuario)
 * y actualizar el ESTADO en respuesta.
 *
 * COMPARACIÓN CON HOME VIEW MODEL:
 * --------------------------------
 * - HomeViewModel: Carga datos una vez al iniciar
 * - SearchViewModel: Responde a eventos continuos (cada tecla del usuario)
 *
 * FLUJO UDF EN BÚSQUEDA:
 * ----------------------
 *
 *     Usuario escribe "rock"
 *            │
 *            ▼
 *     ┌──────────────┐
 *     │  SearchScreen │ ─── onQueryChange("rock") ───▶ ┌────────────────┐
 *     └──────────────┘                                  │ SearchViewModel │
 *            ▲                                          └────────────────┘
 *            │                                                   │
 *            │                                          updateQuery("rock")
 *            │                                                   │
 *            │                                                   ▼
 *            │                                          Filtrar canciones
 *            │                                                   │
 *            │◀─────── StateFlow emite nuevo estado ────────────┘
 *            │         (query="rock", results=[...])
 *            │
 *     UI se recompone con resultados
 *
 * El flujo siempre es UNIDIRECCIONAL:
 * 1. UI envía evento (onQueryChange)
 * 2. ViewModel procesa y actualiza estado
 * 3. UI observa el nuevo estado y se recompone
 */

/**
 * Estado de la pantalla de búsqueda.
 *
 * CONSISTENCIA CON HOMEVIEWMODEL
 * ------------------------------
 * Usamos sealed interface (igual que HomeUiState) para mantener
 * consistencia arquitectónica en toda la aplicación.
 *
 * Aunque el filtrado local es "instantáneo", usar sealed interface:
 * 1. Prepara el código para búsquedas remotas futuras
 * 2. Maneja errores potenciales (corrupted data, excepciones)
 * 3. Mantiene el mismo patrón en todos los ViewModels
 *
 * CUÁNDO USAR CADA ENFOQUE:
 * - Sealed interface: Estados mutuamente excluyentes (Loading vs Success vs Error)
 * - Data class: Valores que coexisten (query + results juntos)
 *
 * Aquí combinamos: Sealed interface con data classes internas para lo mejor de ambos.
 */
sealed interface SearchUiState {
    /**
     * Estado inicial mientras se cargan las canciones.
     */
    data object Loading : SearchUiState

    /**
     * Canciones cargadas y listas para búsqueda.
     *
     * @property query Texto actual de búsqueda
     * @property results Lista de canciones que coinciden con la búsqueda
     * @property allSongs Todas las canciones disponibles
     */
    data class Success(
        val query: String = "",
        val results: List<Song> = emptyList(),
        val allSongs: List<Song> = emptyList()
    ) : SearchUiState {
        /**
         * Propiedad computada: ¿Está el usuario buscando activamente?
         */
        val isSearching: Boolean
            get() = query.isNotBlank()

        /**
         * Canciones a mostrar: resultados si está buscando, todas si no.
         */
        val displayedSongs: List<Song>
            get() = if (isSearching) results else allSongs
    }

    /**
     * Error al cargar o buscar canciones.
     *
     * @property message Mensaje descriptivo del error
     */
    data class Error(
        val message: String
    ) : SearchUiState
}

/**
 * ViewModel para la pantalla de búsqueda.
 *
 * @param repository Repositorio de música (inyectado por Koin)
 *
 * INYECCIÓN DE DEPENDENCIAS CON INTERFACE:
 * ----------------------------------------
 * Igual que HomeViewModel, recibe el repositorio como parámetro,
 * pero ahora depende de la INTERFACE MusicRepository.
 *
 * Koin resuelve automáticamente esta dependencia porque ya está
 * registrado con bind en AppModule.
 *
 * Esto demuestra que MÚLTIPLES ViewModels pueden compartir
 * el MISMO repositorio sin crear instancias duplicadas.
 */
class SearchViewModel(
    private val repository: MusicRepository
) : ViewModel() {

    /**
     * Estado interno mutable.
     *
     * INICIALIZACIÓN CON LOADING
     * --------------------------
     * Igual que HomeViewModel, iniciamos en Loading y luego
     * cargamos las canciones. Esto mantiene consistencia.
     */
    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Loading)

    /**
     * Estado expuesto a la UI (inmutable).
     */
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    /**
     * Inicialización del ViewModel.
     *
     * Cargamos las canciones al iniciar, manejando posibles errores.
     */
    init {
        loadSongs()
    }

    /**
     * Carga todas las canciones desde el repositorio.
     *
     * MANEJO DE ERRORES
     * -----------------
     * Aunque con datos mock es improbable que falle, envolvemos
     * en try-catch para:
     * 1. Demostrar el patrón correcto
     * 2. Estar preparados para implementaciones reales
     * 3. Manejar casos edge (datos corruptos, etc.)
     */
    private fun loadSongs() {
        try {
            val allSongs = repository.getAllSongs()
            _uiState.value = SearchUiState.Success(allSongs = allSongs)
        } catch (e: Exception) {
            _uiState.value = SearchUiState.Error(
                message = e.message ?: "Error al cargar canciones"
            )
        }
    }

    /**
     * Actualiza la consulta de búsqueda.
     *
     * Este método es llamado por la UI cada vez que el usuario
     * escribe o borra caracteres en el campo de búsqueda.
     *
     * @param query Nuevo texto de búsqueda
     *
     * PATRÓN EVENT HANDLER:
     * --------------------
     * Este es un "event handler" - un método público que la UI
     * llama para notificar eventos del usuario.
     *
     * Convención de nombres:
     * - onXxx(): Para eventos de UI (onClick, onChange)
     * - updateXxx(): Para actualizar estado específico
     * - loadXxx(): Para cargar datos
     *
     * ```kotlin
     * // En la UI:
     * TextField(
     *     value = (uiState as? SearchUiState.Success)?.query ?: "",
     *     onValueChange = { viewModel.updateQuery(it) }
     * )
     * ```
     */
    fun updateQuery(query: String) {
        // Solo actualizamos si estamos en estado Success
        val currentState = _uiState.value
        if (currentState !is SearchUiState.Success) return

        try {
            val results = if (query.isBlank()) {
                emptyList()
            } else {
                searchSongs(query)
            }

            /**
             * ACTUALIZACIÓN DE ESTADO CON SEALED INTERFACE
             * --------------------------------------------
             * Con sealed interface, primero verificamos que estamos en Success,
             * luego usamos copy() para crear el nuevo estado.
             *
             * IMPORTANTE: Nunca modifiques el estado directamente.
             * Siempre crea una NUEVA instancia. Esto es esencial para que
             * Compose detecte cambios y recomponga la UI.
             */
            _uiState.value = currentState.copy(
                query = query,
                results = results
            )
        } catch (e: Exception) {
            _uiState.value = SearchUiState.Error(
                message = e.message ?: "Error al buscar"
            )
        }
    }

    /**
     * Busca canciones que coincidan con la consulta.
     *
     * @param query Texto a buscar
     * @return Lista de canciones que coinciden
     *
     * La búsqueda es case-insensitive y busca en título Y artista.
     */
    private fun searchSongs(query: String): List<Song> {
        val lowercaseQuery = query.lowercase()

        return repository.getAllSongs().filter { song ->
            song.title.lowercase().contains(lowercaseQuery) ||
                    song.artist.lowercase().contains(lowercaseQuery)
        }
    }

    /**
     * Limpia la búsqueda actual.
     *
     * Método de conveniencia para resetear el estado.
     * La UI podría llamar esto cuando el usuario presiona
     * el botón "X" del campo de búsqueda.
     */
    fun clearSearch() {
        val currentState = _uiState.value
        if (currentState !is SearchUiState.Success) return

        _uiState.value = currentState.copy(
            query = "",
            results = emptyList()
        )
    }

    /**
     * Reintenta cargar las canciones después de un error.
     *
     * PATRÓN RETRY
     * ------------
     * Es buena práctica ofrecer al usuario la opción de reintentar
     * cuando ocurre un error. Este método permite eso.
     *
     * En la UI:
     * ```kotlin
     * when (val state = uiState) {
     *     is SearchUiState.Error -> {
     *         ErrorScreen(
     *             message = state.message,
     *             onRetry = { viewModel.retry() }
     *         )
     *     }
     *     // ...
     * }
     * ```
     */
    fun retry() {
        loadSongs()
    }
}
