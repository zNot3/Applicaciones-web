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

class CameraUtils(private val context: Context) {

    private val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    fun createImageFile(): File {
        val storageDir = context.filesDir
        val timeStamp = fileNameFormat.format(Date())
        val fileName = "spot_$timeStamp.jpg"
        return File(storageDir, fileName)
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
                        val savedUri = Uri.fromFile(photoFile)
                        continuation.resume(savedUri)
                    }

                    override fun onError(exception: ImageCaptureException) {
                        val captureError = when (exception.imageCaptureError) {
                            ImageCapture.ERROR_CAMERA_CLOSED ->
                                CaptureError.CameraClosed
                            ImageCapture.ERROR_CAPTURE_FAILED ->
                                CaptureError.HardwareFailure(exception.imageCaptureError)
                            ImageCapture.ERROR_FILE_IO ->
                                CaptureError.FileIOError(exception)
                            else ->
                                CaptureError.HardwareFailure(exception.imageCaptureError)
                        }
                        if (photoFile.exists()) photoFile.delete()
                        continuation.resumeWithException(CaptureException(captureError))
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
            val file = File(uri.path ?: return false)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }

    fun getImageSize(uri: Uri): Long {
        return try {
            val file = File(uri.path ?: return -1)
            file.length()
        } catch (e: Exception) {
            -1
        }
    }

    fun imageExists(uri: Uri): Boolean {
        return try {
            val file = File(uri.path ?: return false)
            file.exists()
        } catch (e: Exception) {
            false
        }
    }
}

sealed class CaptureError {
    data object CameraClosed : CaptureError()
    data class HardwareFailure(val code: Int) : CaptureError()
    data class FileIOError(val cause: Throwable) : CaptureError()

    fun toUserMessage(): String = when (this) {
        is CameraClosed    -> "La cámara se cerró inesperadamente. Intenta de nuevo."
        is HardwareFailure -> "Error de hardware en la cámara (código $code)."
        is FileIOError     -> "No se pudo guardar la foto. Verifica el almacenamiento."
    }
}

class CaptureException(val error: CaptureError) : Exception(error.toUserMessage())