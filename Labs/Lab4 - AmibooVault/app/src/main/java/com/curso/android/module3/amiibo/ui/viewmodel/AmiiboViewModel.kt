package com.curso.android.module3.amiibo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curso.android.module3.amiibo.data.local.entity.AmiiboEntity
import com.curso.android.module3.amiibo.domain.error.AmiiboError
import com.curso.android.module3.amiibo.domain.error.ErrorType
import com.curso.android.module3.amiibo.repository.AmiiboRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


sealed interface AmiiboUiState {
    data object Loading : AmiiboUiState

    data class Success(
        val amiibos: List<AmiiboEntity>,
        val isRefreshing: Boolean = false
    ) : AmiiboUiState

    data class Error(
        val message: String,
        val errorType: ErrorType = ErrorType.UNKNOWN,
        val isRetryable: Boolean = true,
        val cachedAmiibos: List<AmiiboEntity> = emptyList()
    ) : AmiiboUiState
}

sealed interface AmiiboUiEvent {
    data class ShowNetworkErrorSnackbar(val message: String) : AmiiboUiEvent
}

@OptIn(FlowPreview::class)
class AmiiboViewModel(
    private val repository: AmiiboRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AmiiboUiState>(AmiiboUiState.Loading)
    val uiState: StateFlow<AmiiboUiState> = _uiState.asStateFlow()

    // =========================================================================
    // PART 1 - SNACKBAR EVENT (SharedFlow para eventos únicos)
    // =========================================================================
    private val _uiEvent = MutableSharedFlow<AmiiboUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // =========================================================================
    // PART 2 - LOCAL SEARCH
    // =========================================================================

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val searchResultsFlow = _searchQuery
        .debounce(300L)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.observeAmiibos()
            } else {
                repository.searchAmiibos(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // =========================================================================
    // PAGINACIÓN
    // =========================================================================
    private val _pageSize = MutableStateFlow(AmiiboRepository.DEFAULT_PAGE_SIZE)
    val pageSize: StateFlow<Int> = _pageSize.asStateFlow()

    private val _currentPage = MutableStateFlow(0)
    private val _loadedAmiibos = MutableStateFlow<List<AmiiboEntity>>(emptyList())

    private val _hasMorePages = MutableStateFlow(true)
    val hasMorePages: StateFlow<Boolean> = _hasMorePages.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _paginationError = MutableStateFlow<String?>(null)
    val paginationError: StateFlow<String?> = _paginationError.asStateFlow()

    val pageSizeOptions: List<Int> = AmiiboRepository.PAGE_SIZE_OPTIONS

    private val amiibosFromDb: StateFlow<List<AmiiboEntity>> = repository
        .observeAmiibos()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    init {
        observeDatabaseChanges()
        observeSearchResults()
        refreshAmiibos()
    }

    private fun observeDatabaseChanges() {
        viewModelScope.launch {
            amiibosFromDb.collect { amiibos ->
                val currentState = _uiState.value
                if (amiibos.isNotEmpty() && _searchQuery.value.isBlank()) {
                    _uiState.value = AmiiboUiState.Success(
                        amiibos = amiibos,
                        isRefreshing = currentState is AmiiboUiState.Success &&
                                (currentState as? AmiiboUiState.Success)?.isRefreshing == true
                    )
                }
            }
        }
    }

    private fun observeSearchResults() {
        viewModelScope.launch {
            searchResultsFlow.collect { results ->
                // Solo actualizamos el estado si hay una búsqueda activa
                // para no interferir con la paginación cuando el query está vacío
                if (_searchQuery.value.isNotBlank()) {
                    _uiState.value = AmiiboUiState.Success(
                        amiibos = results,
                        isRefreshing = false
                    )
                }
            }
        }
    }

    // =========================================================================
    // PART 2 - SEARCH ACTIONS
    // =========================================================================

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query

        // Si el usuario borró el query, volvemos a mostrar la lista paginada
        if (query.isBlank()) {
            val currentLoaded = _loadedAmiibos.value
            if (currentLoaded.isNotEmpty()) {
                _uiState.value = AmiiboUiState.Success(
                    amiibos = currentLoaded,
                    isRefreshing = false
                )
            }
        }
    }

    fun clearSearch() {
        onSearchQueryChange("")
    }

    // =========================================================================
    // PAGINACIÓN
    // =========================================================================
    fun setPageSize(newSize: Int) {
        if (newSize != _pageSize.value && newSize in pageSizeOptions) {
            _pageSize.value = newSize
            resetPagination()
            loadFirstPage()
        }
    }

    private fun resetPagination() {
        _currentPage.value = 0
        _loadedAmiibos.value = emptyList()
        _hasMorePages.value = true
        _paginationError.value = null
    }

    fun loadNextPage() {
        if (_isLoadingMore.value || !_hasMorePages.value || _paginationError.value != null) return

        viewModelScope.launch {
            _isLoadingMore.value = true
            _paginationError.value = null

            try {
                val nextPage = _currentPage.value + 1
                val newItems = repository.getAmiibosPage(nextPage, _pageSize.value)

                if (newItems.isNotEmpty()) {
                    _currentPage.value = nextPage
                    _loadedAmiibos.value = _loadedAmiibos.value + newItems
                    _hasMorePages.value = repository.hasMorePages(nextPage, _pageSize.value)

                    _uiState.value = AmiiboUiState.Success(
                        amiibos = _loadedAmiibos.value,
                        isRefreshing = false
                    )
                } else {
                    _hasMorePages.value = false
                }
            } catch (e: Exception) {
                _paginationError.value = e.message ?: "Error al cargar más items"
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    fun retryLoadMore() {
        _paginationError.value = null
        loadNextPage()
    }

    private fun loadFirstPage() {
        viewModelScope.launch {
            try {
                val firstPageItems = repository.getAmiibosPage(0, _pageSize.value)
                _currentPage.value = 0
                _loadedAmiibos.value = firstPageItems
                _hasMorePages.value = repository.hasMorePages(0, _pageSize.value)

                _uiState.value = AmiiboUiState.Success(
                    amiibos = firstPageItems,
                    isRefreshing = false
                )
            } catch (e: Exception) {
                _uiState.value = AmiiboUiState.Error(
                    message = "Error al cargar datos",
                    isRetryable = true
                )
            }
        }
    }

    fun refreshAmiibos() {
        viewModelScope.launch {
            val currentAmiibos = _loadedAmiibos.value
            if (currentAmiibos.isEmpty()) {
                _uiState.value = AmiiboUiState.Loading
            } else {
                _uiState.value = AmiiboUiState.Success(
                    amiibos = currentAmiibos,
                    isRefreshing = true
                )
            }

            try {
                repository.refreshAmiibos()

                resetPagination()
                val firstPageItems = repository.getAmiibosPage(0, _pageSize.value)
                _loadedAmiibos.value = firstPageItems
                _hasMorePages.value = repository.hasMorePages(0, _pageSize.value)

                _uiState.value = AmiiboUiState.Success(
                    amiibos = firstPageItems,
                    isRefreshing = false
                )

            } catch (e: AmiiboError) {
                val cachedAmiibos = _loadedAmiibos.value
                val errorType = ErrorType.from(e)

                val isRetryable = when (e) {
                    is AmiiboError.Network -> true
                    is AmiiboError.Parse -> false
                    is AmiiboError.Database -> true
                    is AmiiboError.Unknown -> true
                }

                if (cachedAmiibos.isNotEmpty()) {
                    _uiState.value = AmiiboUiState.Error(
                        message = e.message,
                        errorType = errorType,
                        isRetryable = isRetryable,
                        cachedAmiibos = cachedAmiibos
                    )
                    _uiEvent.emit(AmiiboUiEvent.ShowNetworkErrorSnackbar(e.message))
                } else {
                    _uiState.value = AmiiboUiState.Error(
                        message = e.message,
                        errorType = errorType,
                        isRetryable = isRetryable,
                        cachedAmiibos = emptyList()
                    )
                }

            } catch (e: Exception) {
                val cachedAmiibos = _loadedAmiibos.value

                if (cachedAmiibos.isNotEmpty()) {
                    _uiState.value = AmiiboUiState.Error(
                        message = e.message ?: "Error desconocido al cargar datos",
                        errorType = ErrorType.UNKNOWN,
                        isRetryable = true,
                        cachedAmiibos = cachedAmiibos
                    )
                    _uiEvent.emit(
                        AmiiboUiEvent.ShowNetworkErrorSnackbar(
                            e.message ?: "Error desconocido al cargar datos"
                        )
                    )
                } else {
                    _uiState.value = AmiiboUiState.Error(
                        message = e.message ?: "Error desconocido al cargar datos",
                        errorType = ErrorType.UNKNOWN,
                        isRetryable = true,
                        cachedAmiibos = emptyList()
                    )
                }
            }
        }
    }
}
