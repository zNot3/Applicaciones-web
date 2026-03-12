package com.curso.android.module4.cityspots.utils

class CoordinateValidator {

    sealed class ValidationResult {
        data object Valid : ValidationResult()

        data class InvalidLatitude(val value: Double) : ValidationResult()

        data class InvalidLongitude(val value: Double) : ValidationResult()

        data object NotANumber : ValidationResult()

        data object SuspiciousNullIsland : ValidationResult()
    }

    companion object {
        const val MIN_LATITUDE = -90.0
        const val MAX_LATITUDE = 90.0
        const val MIN_LONGITUDE = -180.0
        const val MAX_LONGITUDE = 180.0
    }

    fun validate(
        latitude: Double,
        longitude: Double,
        allowNullIsland: Boolean = false
    ): ValidationResult {
        if (latitude.isNaN() || latitude.isInfinite() ||
            longitude.isNaN() || longitude.isInfinite()) {
            return ValidationResult.NotANumber
        }

        if (latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
            return ValidationResult.InvalidLatitude(latitude)
        }

        if (longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
            return ValidationResult.InvalidLongitude(longitude)
        }

        if (!allowNullIsland && latitude == 0.0 && longitude == 0.0) {
            return ValidationResult.SuspiciousNullIsland
        }

        return ValidationResult.Valid
    }

    fun isValid(latitude: Double, longitude: Double): Boolean {
        return validate(latitude, longitude) == ValidationResult.Valid
    }

    fun getErrorMessage(result: ValidationResult): String {
        return when (result) {
            is ValidationResult.Valid -> ""
            is ValidationResult.InvalidLatitude ->
                "Latitud fuera de rango: ${result.value}. Debe estar entre $MIN_LATITUDE y $MAX_LATITUDE"
            is ValidationResult.InvalidLongitude ->
                "Longitud fuera de rango: ${result.value}. Debe estar entre $MIN_LONGITUDE y $MAX_LONGITUDE"
            is ValidationResult.NotANumber ->
                "Las coordenadas contienen valores no numéricos"
            is ValidationResult.SuspiciousNullIsland ->
                "Las coordenadas (0,0) parecen un error de GPS. Intenta de nuevo."
        }
    }
}