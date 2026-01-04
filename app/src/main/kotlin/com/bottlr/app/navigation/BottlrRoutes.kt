package com.bottlr.app.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation routes for Bottlr app.
 * Uses Kotlin Serialization for compile-time safety.
 */

@Serializable
object Home

@Serializable
object BottleGallery

@Serializable
object CocktailGallery

@Serializable
data class BottleDetails(val bottleId: Long)

@Serializable
data class CocktailDetails(val cocktailId: Long)

@Serializable
data class BottleEditor(val bottleId: Long = -1L) // -1 means new bottle

@Serializable
data class CocktailEditor(val cocktailId: Long = -1L) // -1 means new cocktail

@Serializable
object Settings
