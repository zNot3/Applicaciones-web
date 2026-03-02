package com.curso.android.module5.aichef.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.curso.android.module5.aichef.data.firebase.IAuthRepository
import com.curso.android.module5.aichef.data.firebase.IFirestoreRepository
import com.curso.android.module5.aichef.data.firebase.IStorageRepository
import com.curso.android.module5.aichef.data.remote.IAiLogicDataSource
import com.curso.android.module5.aichef.domain.model.AuthState
import com.curso.android.module5.aichef.domain.model.Recipe
import com.curso.android.module5.aichef.domain.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ChefViewModel @Inject constructor(
    private val authRepository: IAuthRepository,
    private val firestoreRepository: IFirestoreRepository,
    private val storageRepository: IStorageRepository,
    private val aiLogicDataSource: IAiLogicDataSource
) : ViewModel() {

    // =========================================================================
    // ESTADO DE AUTENTICACIÓN
    // =========================================================================

    val authState: StateFlow<AuthState> = authRepository.observeAuthState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthState.Loading
        )

    private val _authUiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val authUiState: StateFlow<UiState<Unit>> = _authUiState.asStateFlow()

    // =========================================================================
    // LISTA DE RECETAS
    // =========================================================================

    /** Lista base de recetas del usuario autenticado, sincronizada con Firestore */
    private val _allRecipes: StateFlow<List<Recipe>> = authState
        .flatMapLatest { state ->
            when (state) {
                is AuthState.Authenticated -> firestoreRepository.observeUserRecipes(state.userId)
                else -> flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // =========================================================================
    // CHALLENGE LAB Part 1: Filtro de Favoritos
    // =========================================================================

    /**
     * Controla si la lista muestra solo favoritos o todas las recetas.
     * La UI puede togglear este valor para cambiar la vista.
     */
    private val _showOnlyFavorites = MutableStateFlow(false)
    val showOnlyFavorites: StateFlow<Boolean> = _showOnlyFavorites.asStateFlow()

    /**
     * Lista de recetas filtrada según showOnlyFavorites.
     *
     * CONCEPTO: combine()
     * combine() fusiona dos Flows y emite cada vez que cualquiera de los dos
     * cambia. Aquí combinamos la lista de recetas con el filtro activo,
     * produciendo la lista final que consume la UI.
     */
    val recipes: StateFlow<List<Recipe>> = combine(_allRecipes, _showOnlyFavorites) { recipes, onlyFavorites ->
        if (onlyFavorites) recipes.filter { it.isFavorite } else recipes
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /** Alterna entre mostrar todas las recetas o solo favoritas */
    fun toggleFavoritesFilter() {
        _showOnlyFavorites.value = !_showOnlyFavorites.value
    }

    /**
     * CHALLENGE LAB Part 1: Cambia el estado de favorito de una receta
     *
     * CONCEPTO: Optimistic UI
     * Para que la UI responda instantáneamente:
     * 1. El StateFlow de Firestore ya actualiza la lista automáticamente
     *    en cuanto el backend confirma el cambio (listener en tiempo real).
     * 2. No necesitamos estado local adicional porque el snapshot listener
     *    de Firestore reemite la lista actualizada en milisegundos.
     *
     * Si el update falla, Firestore no emite cambio → la UI revierte sola.
     *
     * @param recipe Receta a togglear
     */
    fun toggleFavorite(recipe: Recipe) {
        viewModelScope.launch {
            firestoreRepository.toggleFavorite(recipe.id, !recipe.isFavorite)
            // Si falla, el listener de Firestore no emite cambio → UI no cambia (revert automático)
        }
    }

    // =========================================================================
    // ESTADO DE GENERACIÓN DE RECETAS
    // =========================================================================

    private val _generationState = MutableStateFlow<UiState<Recipe>>(UiState.Idle)
    val generationState: StateFlow<UiState<Recipe>> = _generationState.asStateFlow()

    // =========================================================================
    // ESTADO DE GENERACIÓN DE IMÁGENES
    // =========================================================================

    private val _imageGenerationState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val imageGenerationState: StateFlow<UiState<String>> = _imageGenerationState.asStateFlow()

    private var imageGenerationJob: Job? = null

    // =========================================================================
    // CHALLENGE LAB Part 2: Estado de Compartir
    // =========================================================================

    private val _shareState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val shareState: StateFlow<UiState<Unit>> = _shareState.asStateFlow()

    // =========================================================================
    // ACCIONES DE AUTENTICACIÓN
    // =========================================================================

    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            _authUiState.value = UiState.Loading("Iniciando sesión...")
            val result = authRepository.signIn(email, password)
            _authUiState.value = result.fold(
                onSuccess = { UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error desconocido") }
            )
        }
    }

    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authUiState.value = UiState.Loading("Creando cuenta...")
            val result = authRepository.signUp(email, password)
            _authUiState.value = result.fold(
                onSuccess = { UiState.Success(Unit) },
                onFailure = { UiState.Error(it.message ?: "Error desconocido") }
            )
        }
    }

    fun signOut() {
        authRepository.signOut()
        _authUiState.value = UiState.Idle
    }

    fun clearAuthUiState() {
        _authUiState.value = UiState.Idle
    }

    // =========================================================================
    // ACCIONES DE GENERACIÓN DE RECETAS
    // =========================================================================

    fun generateRecipe(imageBitmap: Bitmap) {
        val userId = authRepository.currentUserId
        if (userId == null) {
            _generationState.value = UiState.Error("Debes iniciar sesión")
            return
        }

        viewModelScope.launch {
            _generationState.value = UiState.Loading("Analizando imagen con IA...")

            try {
                val generatedRecipe = aiLogicDataSource.generateRecipeFromImage(imageBitmap)

                val recipe = Recipe(
                    userId = userId,
                    title = generatedRecipe.title,
                    ingredients = generatedRecipe.ingredients,
                    steps = generatedRecipe.steps
                )

                val saveResult = firestoreRepository.saveRecipe(recipe)

                saveResult.fold(
                    onSuccess = { recipeId ->
                        _generationState.value = UiState.Success(recipe.copy(id = recipeId))
                    },
                    onFailure = { error ->
                        _generationState.value = UiState.Error(
                            "Receta generada pero no se pudo guardar: ${error.message}"
                        )
                    }
                )

            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("quota", ignoreCase = true) == true ->
                        "Cuota de API excedida. Intenta más tarde."
                    e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                        "Error de permisos. Verifica la configuración de Firebase."
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "Error de conexión. Verifica tu internet."
                    else -> "Error al generar receta: ${e.message}"
                }
                _generationState.value = UiState.Error(errorMessage)
            }
        }
    }

    fun clearGenerationState() {
        _generationState.value = UiState.Idle
    }

    // =========================================================================
    // ACCIONES DE GENERACIÓN DE IMÁGENES CON CACHE
    // =========================================================================

    fun generateRecipeImage(
        recipeId: String,
        existingImageUrl: String,
        recipeTitle: String,
        ingredients: List<String>
    ) {
        imageGenerationJob?.cancel()

        imageGenerationJob = viewModelScope.launch {
            if (existingImageUrl.isNotBlank()) {
                _imageGenerationState.value = UiState.Success(existingImageUrl)
                return@launch
            }

            _imageGenerationState.value = UiState.Loading("Generando imagen del plato...")

            try {
                val bitmap = aiLogicDataSource.generateRecipeImage(recipeTitle, ingredients)

                _imageGenerationState.value = UiState.Loading("Guardando imagen...")

                val uploadResult = storageRepository.uploadRecipeImage(recipeId, bitmap)

                uploadResult.fold(
                    onSuccess = { imageUrl ->
                        firestoreRepository.updateGeneratedImageUrl(recipeId, imageUrl)
                        _imageGenerationState.value = UiState.Success(imageUrl)
                    },
                    onFailure = { error ->
                        _imageGenerationState.value = UiState.Error(
                            "Imagen generada pero no se pudo guardar: ${error.message}"
                        )
                    }
                )

            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("quota", ignoreCase = true) == true ->
                        "Cuota de API excedida. Intenta más tarde."
                    e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
                        "Error de permisos. Verifica la configuración."
                    e.message?.contains("not supported", ignoreCase = true) == true ->
                        "Generación de imágenes no disponible."
                    else -> "Error al generar imagen: ${e.message}"
                }
                _imageGenerationState.value = UiState.Error(errorMessage)
            }
        }
    }

    fun clearImageState() {
        imageGenerationJob?.cancel()
        _imageGenerationState.value = UiState.Idle
    }

    // =========================================================================
    // ACCIONES DE RECETAS
    // =========================================================================

    fun deleteRecipe(recipeId: String) {
        viewModelScope.launch {
            firestoreRepository.deleteRecipe(recipeId)
        }
    }

    // =========================================================================
    // CHALLENGE LAB Part 2: Compartir Receta
    // =========================================================================

    /**
     * Comparte la imagen capturada del detalle de receta usando el share sheet nativo.
     *
     * CONCEPTO: FileProvider
     * Android no permite compartir archivos directamente desde el directorio privado
     * de la app. FileProvider genera una URI temporal con permisos de lectura
     * que otras apps pueden usar para acceder al archivo.
     *
     * FLUJO:
     * 1. Recibir el Bitmap capturado por la UI (PixelMap de Compose)
     * 2. Guardar el Bitmap como archivo JPG temporal en cache
     * 3. Obtener URI segura via FileProvider
     * 4. Lanzar Intent de ACTION_SEND con la URI
     * 5. Limpiar el archivo temporal después de compartir
     *
     * @param context Context para acceder a FileProvider y startActivity
     * @param bitmap  Bitmap capturado del composable RecipeDetailScreen
     * @param recipeTitle Título de la receta (para el texto del share)
     */
    fun shareRecipe(context: Context, bitmap: Bitmap, recipeTitle: String) {
        viewModelScope.launch {
            _shareState.value = UiState.Loading("Preparando imagen para compartir...")

            try {
                // 1. Guardar bitmap en directorio de cache de la app
                val cachePath = File(context.cacheDir, "shared_images").also { it.mkdirs() }
                val imageFile = File(cachePath, "recipe_share.jpg")

                FileOutputStream(imageFile).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }

                // 2. Obtener URI segura mediante FileProvider
                // IMPORTANTE: El authority debe coincidir con el definido en AndroidManifest.xml
                val authority = "${context.packageName}.fileprovider"
                val imageUri: Uri = FileProvider.getUriForFile(context, authority, imageFile)

                // 3. Crear Intent para compartir imagen
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/jpeg"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    putExtra(Intent.EXTRA_TEXT, "¡Mira esta receta que generé con IA: $recipeTitle!")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                // 4. Lanzar el share sheet nativo de Android
                context.startActivity(
                    Intent.createChooser(shareIntent, "Compartir receta via...")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )

                _shareState.value = UiState.Success(Unit)

                // 5. Limpiar archivo temporal después de 30 segundos
                // (tiempo suficiente para que la app receptora lo lea)
                kotlinx.coroutines.delay(30_000)
                imageFile.delete()

            } catch (e: Exception) {
                _shareState.value = UiState.Error("Error al compartir: ${e.message}")
            }
        }
    }

    fun clearShareState() {
        _shareState.value = UiState.Idle
    }
}
