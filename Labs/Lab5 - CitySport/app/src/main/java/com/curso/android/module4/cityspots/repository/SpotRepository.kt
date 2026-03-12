package com.curso.android.module4.cityspots.repository

import android.location.Location
import android.net.Uri
import androidx.camera.core.ImageCapture
import com.curso.android.module4.cityspots.data.dao.SpotDao
import com.curso.android.module4.cityspots.data.entity.SpotEntity
import com.curso.android.module4.cityspots.utils.CameraUtils
import com.curso.android.module4.cityspots.utils.CaptureError
import com.curso.android.module4.cityspots.utils.CoordinateValidator
import com.curso.android.module4.cityspots.utils.LocationUtils
import kotlinx.coroutines.flow.Flow

class SpotRepository(
    private val spotDao: SpotDao,
    private val cameraUtils: CameraUtils,
    private val locationUtils: LocationUtils,
    private val coordinateValidator: CoordinateValidator
) {

    // =========================================================================
    // OPERACIONES DE BASE DE DATOS (Room)
    // =========================================================================

    fun getAllSpots(): Flow<List<SpotEntity>> = spotDao.getAllSpots()

    suspend fun getSpotById(id: Long): SpotEntity? = spotDao.getSpotById(id)

    suspend fun insertSpot(spot: SpotEntity): Long = spotDao.insertSpot(spot)

    suspend fun getSpotCount(): Int = spotDao.getSpotCount()

    // =========================================================================
    // OPERACIONES DE HARDWARE (Cámara)
    // =========================================================================

    suspend fun capturePhoto(imageCapture: ImageCapture): Uri =
        cameraUtils.capturePhoto(imageCapture)

    // =========================================================================
    // OPERACIONES DE UBICACIÓN (GPS)
    // =========================================================================

    suspend fun getCurrentLocation(): Location? =
        locationUtils.getCurrentLocation() ?: locationUtils.getLastLocation()

    fun getLocationUpdates(intervalMs: Long = 5000L) =
        locationUtils.getLocationUpdates(intervalMs)

    // =========================================================================
    // OPERACIONES COMBINADAS
    // =========================================================================

    suspend fun createSpot(imageCapture: ImageCapture): CreateSpotResult {
        val photoUri = try {
            capturePhoto(imageCapture)
        } catch (e: CaptureError) {
            return CreateSpotResult.PhotoCaptureFailed(e)
        }

        val location = getCurrentLocation()
        if (location == null) {
            cameraUtils.deleteImage(photoUri)
            return CreateSpotResult.NoLocation
        }

        val validationResult = coordinateValidator.validate(
            latitude = location.latitude,
            longitude = location.longitude
        )
        if (validationResult != CoordinateValidator.ValidationResult.Valid) {
            cameraUtils.deleteImage(photoUri)
            val errorMessage = coordinateValidator.getErrorMessage(validationResult)
            return CreateSpotResult.InvalidCoordinates(errorMessage)
        }

        val spotNumber = getSpotCount() + 1
        val spot = SpotEntity(
            imageUri = photoUri.toString(),
            latitude = location.latitude,
            longitude = location.longitude,
            title = "Spot #$spotNumber"
        )
        val id = insertSpot(spot)

        return CreateSpotResult.Success(spot.copy(id = id))
    }

    // =========================================================================
    // Part 2: Eliminación de Spot
    // =========================================================================

    suspend fun deleteSpot(id: Long): DeleteSpotResult {
        val spot = spotDao.getSpotById(id) ?: return DeleteSpotResult.NotFound

        spotDao.deleteSpot(id)

        cameraUtils.deleteImage(Uri.parse(spot.imageUri))

        return DeleteSpotResult.Success
    }
}

// =============================================================================
// Result types
// =============================================================================

sealed class CreateSpotResult {
    data class Success(val spot: SpotEntity) : CreateSpotResult()
    data object NoLocation : CreateSpotResult()
    data class InvalidCoordinates(val message: String) : CreateSpotResult()

    data class PhotoCaptureFailed(val error: CaptureError) : CreateSpotResult()
}

sealed class DeleteSpotResult {
    data object Success : DeleteSpotResult()
    data object NotFound : DeleteSpotResult()
}