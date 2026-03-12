package com.curso.android.module5.aichef.domain.model

/**
 * =============================================================================
 * Recipe - Modelo de dominio para recetas
 * =============================================================================
 *
 * CHALLENGE LAB - Part 1: Se agregó el campo isFavorite para el sistema de favoritos.
 * Este campo se persiste en Firestore y se sincroniza en tiempo real.
 *
 * =============================================================================
 */
data class Recipe(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val ingredients: List<String> = emptyList(),
    val steps: List<String> = emptyList(),
    val imageUri: String = "",
    val generatedImageUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    // CHALLENGE LAB Part 1: Campo para marcar receta como favorita
    val isFavorite: Boolean = false
) {
    fun toMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "title" to title,
        "ingredients" to ingredients,
        "steps" to steps,
        "imageUri" to imageUri,
        "generatedImageUrl" to generatedImageUrl,
        "createdAt" to createdAt,
        // CHALLENGE LAB Part 1: Incluir isFavorite en el documento de Firestore
        "isFavorite" to isFavorite
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromFirestore(id: String, data: Map<String, Any?>): Recipe {
            return Recipe(
                id = id,
                userId = data["userId"] as? String ?: "",
                title = data["title"] as? String ?: "",
                ingredients = (data["ingredients"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                steps = (data["steps"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                imageUri = data["imageUri"] as? String ?: "",
                generatedImageUrl = data["generatedImageUrl"] as? String ?: "",
                createdAt = (data["createdAt"] as? Long) ?: System.currentTimeMillis(),
                // CHALLENGE LAB Part 1: Leer isFavorite desde Firestore (default false)
                isFavorite = data["isFavorite"] as? Boolean ?: false
            )
        }
    }
}

/**
 * =============================================================================
 * GeneratedRecipe - Resultado del análisis de IA
 * =============================================================================
 */
data class GeneratedRecipe(
    val title: String,
    val ingredients: List<String>,
    val steps: List<String>
)