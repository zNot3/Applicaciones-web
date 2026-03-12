package com.curso.android.module5.aichef.data.firebase

import com.curso.android.module5.aichef.domain.model.Recipe
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * =============================================================================
 * FirestoreRepository - Wrapper para Cloud Firestore
 * =============================================================================
 *
 * CHALLENGE LAB Part 1: Se agregó toggleFavorite() para actualizar el campo
 * isFavorite de una receta en Firestore usando update() parcial.
 *
 * =============================================================================
 */
class FirestoreRepository @javax.inject.Inject constructor() : IFirestoreRepository {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val recipesCollection = firestore.collection("recipes")

    override fun observeUserRecipes(userId: String): Flow<List<Recipe>> = callbackFlow {
        val query = recipesCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listenerRegistration = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }

            val recipes = snapshot?.documents?.mapNotNull { document ->
                try {
                    val data = document.data ?: return@mapNotNull null
                    Recipe.fromFirestore(document.id, data)
                } catch (e: Exception) {
                    null
                }
            } ?: emptyList()

            trySend(recipes)
        }

        awaitClose {
            listenerRegistration.remove()
        }
    }

    override suspend fun saveRecipe(recipe: Recipe): Result<String> {
        return try {
            val documentRef = recipesCollection.add(recipe.toMap()).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(Exception("Error guardando receta: ${e.message}"))
        }
    }

    override suspend fun getRecipe(recipeId: String): Recipe? {
        return try {
            val document = recipesCollection.document(recipeId).get().await()
            if (document.exists()) {
                val data = document.data ?: return null
                Recipe.fromFirestore(document.id, data)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun deleteRecipe(recipeId: String): Result<Unit> {
        return try {
            recipesCollection.document(recipeId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error eliminando receta: ${e.message}"))
        }
    }

    override suspend fun updateGeneratedImageUrl(recipeId: String, imageUrl: String): Result<Unit> {
        return try {
            recipesCollection.document(recipeId)
                .update("generatedImageUrl", imageUrl)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error actualizando imagen: ${e.message}"))
        }
    }

    /**
     * CHALLENGE LAB Part 1: Actualiza el estado de favorito de una receta
     *
     * CONCEPTO: update() para campos específicos
     * Solo actualizamos el campo isFavorite sin tocar el resto del documento.
     * Esto es más eficiente y evita condiciones de carrera con otros campos.
     *
     * @param recipeId ID del documento a actualizar
     * @param isFavorite Nuevo valor del campo isFavorite
     * @return Result indicando éxito o error
     */
    override suspend fun toggleFavorite(recipeId: String, isFavorite: Boolean): Result<Unit> {
        return try {
            recipesCollection.document(recipeId)
                .update("isFavorite", isFavorite)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Error actualizando favorito: ${e.message}"))
        }
    }
}
