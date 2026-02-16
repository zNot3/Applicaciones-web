package com.curso.android.module3.amiibo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curso.android.module3.amiibo.data.local.entity.AmiiboEntity
import com.curso.android.module3.amiibo.repository.AmiiboRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AmiiboUiState {
    data object Loading : AmiiboUiState

    data class Success(
        val amiibos: List<AmiiboEntity>,
        val isRefreshing: Boolean = false
    ) : AmiiboUiState

    data class Error(
        val message: String,
        val isRetryable: Boolean = true,
        val cachedData: List<AmiiboEntity>? = null
    ) : AmiiboUiState
}

class AmiiboViewModel(
    private val repository: AmiiboRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AmiiboUiState>(AmiiboUiState.Loading)
    val uiState: StateFlow<AmiiboUiState> = _uiState.asStateFlow()

    private val _pageSize = MutableStateFlow(AmiiboRepository.DEFAULT_PAGE_SIZE)
    private val _currentPage = MutableStateFlow(0)
    private val _loadedAmiibos = MutableStateFlow<List<AmiiboEntity>>(emptyList())
    private val _hasMorePages = MutableStateFlow(true)
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    init {
        refreshAmiibos()
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

            } catch (e: Exception) {

                _uiState.value = AmiiboUiState.Error(
                    message = e.localizedMessage ?: "Error desconocido",
                    isRetryable = true,
                    cachedData = _loadedAmiibos.value
                )
            }
        }
    }

    fun loadNextPage() {
        if (_isLoadingMore.value || !_hasMorePages.value) return

        viewModelScope.launch {
            _isLoadingMore.value = true
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
                _uiState.value = AmiiboUiState.Error(
                    message = "Error al cargar más: ${e.message}",
                    cachedData = _loadedAmiibos.value
                )
            } finally {
                _isLoadingMore.value = false
            }
        }
    }

    private fun resetPagination() {
        _currentPage.value = 0
        _loadedAmiibos.value = emptyList()
        _hasMorePages.value = true
    }
}