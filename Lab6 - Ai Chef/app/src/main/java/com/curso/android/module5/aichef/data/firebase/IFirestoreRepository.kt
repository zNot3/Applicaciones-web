package com.curso.android.module5.aichef.data.firebase

import com.curso.android.module5.aichef.domain.model.Recipe
import kotlinx.coroutines.flow.Flow

/**
 * CHALLENGE LAB Part 1: Se agrega toggleFavorite() a la interfaz
 */
interface IFirestoreRepository {
    fun observeUserRecipes(userId: String): Flow<List<Recipe>>
    suspend fun saveRecipe(recipe: Recipe): Result<String>
    suspend fun getRecipe(recipeId: String): Recipe?
    suspend fun deleteRecipe(recipeId: String): Result<Unit>
    suspend fun updateGeneratedImageUrl(recipeId: String, imageUrl: String): Result<Unit>

    // CHALLENGE LAB Part 1: Actualiza el campo isFavorite en Firestore
    suspend fun toggleFavorite(recipeId: String, isFavorite: Boolean): Result<Unit>
}
