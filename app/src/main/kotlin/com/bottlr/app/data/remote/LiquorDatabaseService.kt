package com.bottlr.app.data.remote

import com.bottlr.app.data.model.RecognizedBottle

/**
 * Interface for querying external liquor databases to enrich AI-recognized data.
 */
interface LiquorDatabaseService {
    /**
     * Search for a bottle by name and optionally distillery.
     * @param name The bottle name (required)
     * @param distillery Optional distillery name to narrow search
     * @return Enriched bottle data or null if not found
     */
    suspend fun searchBottle(name: String, distillery: String?): BottleSearchResult?

    /**
     * Check if this service is available (API key configured, service reachable).
     */
    fun isAvailable(): Boolean

    /**
     * Human-readable name of this database.
     */
    val databaseName: String
}

/**
 * Result from a liquor database search.
 */
data class BottleSearchResult(
    val name: String,
    val distillery: String? = null,
    val type: String? = null,
    val region: String? = null,
    val abv: Float? = null,
    val age: Int? = null,
    val notes: String? = null,
    val keywords: String? = null,
    val source: String  // Name of the database this came from
)
