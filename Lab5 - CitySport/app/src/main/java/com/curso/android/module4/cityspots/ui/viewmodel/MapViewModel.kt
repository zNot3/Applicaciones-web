package com.curso.android.module4.cityspots.ui.viewmodel

import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curso.android.module4.cityspots.data.entity.SpotEntity
import com.curso.android.module4.cityspots.repository.CreateSpotResult
import com.curso.android.module4.cityspots.repository.DeleteSpotResult
import com.curso.android.module4.cityspots.repository.SpotRepository
import com.curso.android.module4.cityspots.utils.CaptureError
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MapViewModel(
    private val repository: SpotRepository
) : ViewModel() {

    // =========================================================================
    // ESTADO DE LA UI
    // =========================================================================

    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val spots: StateFlow<List<SpotEntity>> = repository.getAllSpots()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    private val _captureResult = MutableStateFlow<Boolean?>(null)
    val captureResult: StateFlow<Boolean?> = _captureResult.asStateFlow()

    // =========================================================================
    // ACCIONES DE UBICACIÓN
    // =========================================================================

    fun loadUserLocation() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val location = repository.getCurrentLocation()
                location?.let {
                    _userLocation.value = LatLng(it.latitude, it.longitude)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error obteniendo ubicación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startLocationUpdates() {
        viewModelScope.launch {
            repository.getLocationUpdates()
                .collect { location ->
                    _userLocation.value = LatLng(location.latitude, location.longitude)
                }
        }
    }

    // =========================================================================
    // CREAR SPOT
    // =========================================================================

    fun createSpot(imageCapture: ImageCapture) {
        viewModelScope.launch {
            try {
                _isLoading.value = true

                when (val result = repository.createSpot(imageCapture)) {
                    is CreateSpotResult.Success -> {
                        _captureResult.value = true
                    }

                    is CreateSpotResult.NoLocation -> {
                        _errorMessage.value =
                            "No se pudo obtener la ubicación. Verifica que el GPS esté activado."
                        _captureResult.value = false
                    }

                    is CreateSpotResult.InvalidCoordinates -> {
                        _errorMessage.value = result.message
                        _captureResult.value = false
                    }

                    is CreateSpotResult.PhotoCaptureFailed -> {
                        _errorMessage.value = when (result.error) {
                            is CaptureError.CameraClosed ->
                                "La cámara se cerró inesperadamente. Vuelve a abrir la pantalla e intenta de nuevo."
                            is CaptureError.CaptureFailed ->
                                "Error de hardware al capturar la foto. Intenta de nuevo."
                            is CaptureError.FileIOError ->
                                "No se pudo guardar la foto. Verifica que haya espacio disponible en el dispositivo."
                        }
                        _captureResult.value = false
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error inesperado: ${e.message}"
                _captureResult.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =========================================================================
    // Part 2: ELIMINAR SPOT
    // =========================================================================

    fun deleteSpot(id: Long) {
        viewModelScope.launch {
            when (repository.deleteSpot(id)) {
                is DeleteSpotResult.Success  -> { }
                is DeleteSpotResult.NotFound ->
                    _errorMessage.value = "No se encontró el spot para eliminar."
            }
        }
    }

    // =========================================================================
    // LIMPIEZA DE ESTADO
    // =========================================================================

    fun clearCaptureResult() { _captureResult.value = null }

    fun clearError() { _errorMessage.value = null }
}