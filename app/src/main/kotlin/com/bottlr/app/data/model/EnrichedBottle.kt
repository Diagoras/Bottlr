package com.bottlr.app.data.model

import kotlinx.serialization.Serializable

/**
 * Bottle information enriched with data from external databases.
 * Combines AI-recognized data with verified database information.
 */
@Serializable
data class EnrichedBottle(
    // Core fields (from AI + database)
    val name: String? = null,
    val distillery: String? = null,
    val type: String? = null,
    val region: String? = null,
    val abv: Float? = null,
    val age: Int? = null,

    // Additional fields from database enrichment
    val notes: String? = null,
    val keywords: String? = null,

    // Source tracking
    val source: DataSource = DataSource.AI_ONLY,

    // Per-field confidence and source info
    val fieldMetadata: Map<String, FieldMetadata> = emptyMap()
)

/**
 * Metadata for each field indicating source and confidence.
 */
@Serializable
data class FieldMetadata(
    val confidence: ConfidenceLevel = ConfidenceLevel.UNKNOWN,
    val source: DataSource = DataSource.AI_ONLY,
    val aiValue: String? = null,      // Original AI value (if different from enriched)
    val databaseValue: String? = null  // Database value (if found)
)

/**
 * Source of the data for a field or the overall bottle.
 */
@Serializable
enum class DataSource {
    AI_ONLY,           // Only AI recognition, no database match
    DATABASE_ONLY,     // Only from database (e.g., barcode lookup)
    AI_AND_DATABASE,   // AI recognized, database verified/enriched
    USER_ENTERED       // User manually corrected
}
