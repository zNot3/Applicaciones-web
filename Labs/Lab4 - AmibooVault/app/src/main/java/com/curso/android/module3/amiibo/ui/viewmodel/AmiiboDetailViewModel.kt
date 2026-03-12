package com.curso.android.module3.amiibo.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curso.android.module3.amiibo.data.remote.model.AmiiboDetail
import com.curso.android.module3.amiibo.repository.AmiiboRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AmiiboDetailUiState {
    data object Loading : AmiiboDetailUiState
    data class Success(val amiibo: AmiiboDetail) : AmiiboDetailUiState
    data class Error(val message: String) : AmiiboDetailUiState
}

class AmiiboDetailViewModel(
    private val amiiboName: String,
    private val repository: AmiiboRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AmiiboDetailUiState>(AmiiboDetailUiState.Loading)
    val uiState: StateFlow<AmiiboDetailUiState> = _uiState.asStateFlow()

    init {
        loadAmiiboDetail()
    }

    fun loadAmiiboDetail() {
        viewModelScope.launch {
            _uiState.value = AmiiboDetailUiState.Loading
            try {
                val detail = repository.getAmiiboDetail(amiiboName)
                _uiState.value = AmiiboDetailUiState.Success(detail)
            } catch (e: Exception) {
                _uiState.value = AmiiboDetailUiState.Error(
                    e.message ?: "Error al cargar el detalle"
                )
            }
        }
    }
}
