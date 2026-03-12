package com.curso.android.module4.cityspots.utils

import android.content.Context
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// =============================================================================
// PART 1: CaptureError - Tipos de error granulares para captura de foto
// =============================================================================

sealed class CaptureError : Exception() {

    data object CameraClosed : CaptureError() {
        override val message: String =
            "La cámara se cerró inesperadamente. Vuelve a abrir la pantalla e intenta de nuevo."
    }

    data object CaptureFailed : CaptureError() {
        override val message: String =
            "Error de hardware al capturar la foto. Intenta de nuevo."
    }

    data class FileIOError(override val cause: Throwable) : CaptureError() {
        override val message: String =
            "No se pudo guardar la foto. Verifica que haya espacio disponible."
    }

    companion object {
        fun from(exception: ImageCaptureException): CaptureError =
            when (exception.imageCaptureError) {
                ImageCapture.ERROR_CAMERA_CLOSED -> CameraClosed
                ImageCapture.ERROR_FILE_IO       -> FileIOError(exception)
                else                             -> CaptureFailed
            }
    }
}

// =============================================================================
// CameraUtils
// =============================================================================

class CameraUtils(private val context: Context) {

    private val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    fun createImageFile(): File {
        val storageDir = context.filesDir
        val timeStamp = fileNameFormat.format(Date())
        return File(storageDir, "spot_$timeStamp.jpg")
    }

    suspend fun capturePhoto(imageCapture: ImageCapture): Uri {
        return suspendCancellableCoroutine { continuation ->
            val photoFile = createImageFile()
            val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

            imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                        continuation.resume(Uri.fromFile(photoFile))
                    }

                    override fun onError(exception: ImageCaptureException) {
                        photoFile.delete()
                        continuation.resumeWithException(CaptureError.from(exception))
                    }
                }
            )

            continuation.invokeOnCancellation {
                if (photoFile.exists()) photoFile.delete()
            }
        }
    }

    fun deleteImage(uri: Uri): Boolean {
        return try {
            File(uri.path ?: return false).delete()
        } catch (e: Exception) {
            false
        }
    }

    fun getImageSize(uri: Uri): Long {
        return try {
            File(uri.path ?: return -1).length()
        } catch (e: Exception) {
            -1
        }
    }

    fun imageExists(uri: Uri): Boolean {
        return try {
            File(uri.path ?: return false).exists()
        } catch (e: Exception) {
            false
        }
    }
}