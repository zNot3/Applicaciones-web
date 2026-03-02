package com.curso.android.module5.aichef.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.curso.android.module5.aichef.domain.model.UiState
import com.curso.android.module5.aichef.ui.viewmodel.ChefViewModel

/**
 * =============================================================================
 * RecipeDetailScreen - Pantalla de detalle con imagen IA, favorito y compartir
 * =============================================================================
 *
 * CHALLENGE LAB Part 1: Ícono de corazón en el TopAppBar para marcar favorito
 * CHALLENGE LAB Part 2: FAB de compartir que captura la vista como Bitmap
 *                       y lanza el share sheet nativo de Android
 *
 * =============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    viewModel: ChefViewModel,
    recipeId: String,
    onNavigateBack: () -> Unit
) {
    val recipes by viewModel.recipes.collectAsStateWithLifecycle()
    val recipe = recipes.find { it.id == recipeId }

    val imageState by viewModel.imageGenerationState.collectAsStateWithLifecycle()

    // CHALLENGE LAB Part 2: Estado de compartir
    val shareState by viewModel.shareState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // CHALLENGE LAB Part 2: Captura del composable como Bitmap
    // Usamos LocalView para obtener la vista raíz y dibujarla en un Canvas
    val view = LocalView.current

    // Side effect para generar/cargar imagen
    LaunchedEffect(recipe) {
        recipe?.let {
            viewModel.generateRecipeImage(
                recipeId = it.id,
                existingImageUrl = it.generatedImageUrl,
                recipeTitle = it.title,
                ingredients = it.ingredients
            )
        }
    }

    // CHALLENGE LAB Part 2: Mostrar Snackbar si hay error al compartir
    LaunchedEffect(shareState) {
        val currentState = shareState
        if (currentState is UiState.Error) {
            snackbarHostState.showSnackbar(currentState.message)
            viewModel.clearShareState()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(recipe?.title ?: "Detalle de Receta") },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearImageState()
                        onNavigateBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                // CHALLENGE LAB Part 1: Ícono de favorito en el TopAppBar
                actions = {
                    recipe?.let { currentRecipe ->
                        IconButton(onClick = { viewModel.toggleFavorite(currentRecipe) }) {
                            Icon(
                                imageVector = if (currentRecipe.isFavorite)
                                    Icons.Default.Favorite
                                else
                                    Icons.Default.FavoriteBorder,
                                contentDescription = if (currentRecipe.isFavorite)
                                    "Quitar de favoritos"
                                else
                                    "Agregar a favoritos",
                                tint = if (currentRecipe.isFavorite)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        // CHALLENGE LAB Part 2: FAB para compartir la receta
        floatingActionButton = {
            recipe?.let {
                ExtendedFloatingActionButton(
                    onClick = {
                        // Capturar la vista actual como Bitmap
                        val bitmap = Bitmap.createBitmap(
                            view.width, view.height, Bitmap.Config.ARGB_8888
                        )
                        val canvas = Canvas(bitmap)
                        view.draw(canvas)

                        viewModel.shareRecipe(context, bitmap, it.title)
                    },
                    icon = {
                        val currentState = shareState
                        if (currentState is UiState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Share, contentDescription = "Compartir")
                        }
                    },
                    text = {
                        val currentState = shareState
                        Text(if (currentState is UiState.Loading) "Preparando..." else "Compartir")
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        if (recipe == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Receta no encontrada")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Imagen generada por IA
            AiImageSection(
                imageState = imageState,
                recipeTitle = recipe.title,
                onRetry = {
                    viewModel.generateRecipeImage(
                        recipeId = recipe.id,
                        existingImageUrl = "",
                        recipeTitle = recipe.title,
                        ingredients = recipe.ingredients
                    )
                }
            )

            // Ingredientes
            IngredientsSection(ingredients = recipe.ingredients)

            // Pasos
            StepsSection(steps = recipe.steps)

            // Espacio extra para que el FAB no tape el último elemento
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// =============================================================================
// AiImageSection - Sección de imagen con estados de carga/error
// =============================================================================
@Composable
private fun AiImageSection(
    imageState: UiState<String>,
    recipeTitle: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when (imageState) {
                is UiState.Idle, is UiState.Loading -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator()
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        val message = if (imageState is UiState.Loading) {
                            imageState.message ?: "Preparando..."
                        } else {
                            "Preparando..."
                        }
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                is UiState.Success -> {
                    AsyncImage(
                        model = imageState.data,
                        contentDescription = "Imagen de $recipeTitle",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                is UiState.Error -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restaurant,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = imageState.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = onRetry) { Text("Reintentar") }
                    }
                }
            }
        }
    }
}

// =============================================================================
// IngredientsSection
// =============================================================================
@Composable
private fun IngredientsSection(ingredients: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Ingredientes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            ingredients.forEach { ingredient ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(text = ingredient, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

// =============================================================================
// StepsSection
// =============================================================================
@Composable
private fun StepsSection(steps: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Preparación",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "${index + 1}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text(
                        text = step,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}