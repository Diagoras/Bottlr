package com.bottlr.app.data.repository

import com.bottlr.app.data.model.ConfidenceLevel
import com.bottlr.app.data.model.DataSource
import com.bottlr.app.data.model.EnrichedBottle
import com.bottlr.app.data.model.FieldMetadata
import com.bottlr.app.data.model.RecognizedBottle
import com.bottlr.app.data.remote.BottleDbClient
import com.bottlr.app.data.remote.BottleSearchResult
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository that enriches AI-recognized bottle data with verified database information.
 */
@Singleton
class EnrichmentRepository @Inject constructor(
    private val bottleDbClient: BottleDbClient
) {
    /**
     * Enrich a recognized bottle with data from external databases.
     * Merges AI recognition with database lookups, preferring database values
     * when available as they're more reliable.
     */
    suspend fun enrich(recognized: RecognizedBottle): EnrichedBottle {
        // If we don't have a name, we can't search databases
        val bottleName = recognized.name
        if (bottleName.isNullOrBlank()) {
            return toEnrichedBottle(recognized, null)
        }

        // Search databases for this bottle
        val dbResult = searchDatabases(bottleName, recognized.distillery)

        // Merge AI recognition with database results
        return toEnrichedBottle(recognized, dbResult)
    }

    private suspend fun searchDatabases(name: String, distillery: String?): BottleSearchResult? {
        // Try BottleDB first (for whisky)
        if (bottleDbClient.isAvailable()) {
            val result = bottleDbClient.searchBottle(name, distillery)
            if (result != null) {
                return result
            }
        }

        // Could add more database clients here (WineVybe, etc.)

        return null
    }

    private fun toEnrichedBottle(
        recognized: RecognizedBottle,
        dbResult: BottleSearchResult?
    ): EnrichedBottle {
        val hasDbResult = dbResult != null

        return EnrichedBottle(
            // Prefer database values when available, fall back to AI
            name = dbResult?.name ?: recognized.name,
            distillery = dbResult?.distillery ?: recognized.distillery,
            type = dbResult?.type ?: recognized.type,
            region = dbResult?.region ?: recognized.region,
            abv = dbResult?.abv ?: recognized.abv,
            age = dbResult?.age ?: recognized.age,

            // Additional fields from database
            notes = dbResult?.notes,
            keywords = dbResult?.keywords,

            // Source tracking
            source = when {
                hasDbResult && recognized.name != null -> DataSource.AI_AND_DATABASE
                hasDbResult -> DataSource.DATABASE_ONLY
                else -> DataSource.AI_ONLY
            },

            // Per-field metadata
            fieldMetadata = buildFieldMetadata(recognized, dbResult)
        )
    }

    private fun buildFieldMetadata(
        recognized: RecognizedBottle,
        dbResult: BottleSearchResult?
    ): Map<String, FieldMetadata> {
        val metadata = mutableMapOf<String, FieldMetadata>()

        // Name
        metadata["name"] = FieldMetadata(
            confidence = recognized.confidence["name"] ?: ConfidenceLevel.UNKNOWN,
            source = if (dbResult?.name != null) DataSource.AI_AND_DATABASE else DataSource.AI_ONLY,
            aiValue = recognized.name,
            databaseValue = dbResult?.name
        )

        // Distillery
        metadata["distillery"] = FieldMetadata(
            confidence = recognized.confidence["distillery"] ?: ConfidenceLevel.UNKNOWN,
            source = if (dbResult?.distillery != null) DataSource.AI_AND_DATABASE else DataSource.AI_ONLY,
            aiValue = recognized.distillery,
            databaseValue = dbResult?.distillery
        )

        // Type
        metadata["type"] = FieldMetadata(
            confidence = recognized.confidence["type"] ?: ConfidenceLevel.UNKNOWN,
            source = if (dbResult?.type != null) DataSource.AI_AND_DATABASE else DataSource.AI_ONLY,
            aiValue = recognized.type,
            databaseValue = dbResult?.type
        )

        // Region
        metadata["region"] = FieldMetadata(
            confidence = recognized.confidence["region"] ?: ConfidenceLevel.UNKNOWN,
            source = if (dbResult?.region != null) DataSource.AI_AND_DATABASE else DataSource.AI_ONLY,
            aiValue = recognized.region,
            databaseValue = dbResult?.region
        )

        // ABV
        metadata["abv"] = FieldMetadata(
            confidence = recognized.confidence["abv"] ?: ConfidenceLevel.UNKNOWN,
            source = if (dbResult?.abv != null) DataSource.AI_AND_DATABASE else DataSource.AI_ONLY,
            aiValue = recognized.abv?.toString(),
            databaseValue = dbResult?.abv?.toString()
        )

        // Age
        metadata["age"] = FieldMetadata(
            confidence = recognized.confidence["age"] ?: ConfidenceLevel.UNKNOWN,
            source = if (dbResult?.age != null) DataSource.AI_AND_DATABASE else DataSource.AI_ONLY,
            aiValue = recognized.age?.toString(),
            databaseValue = dbResult?.age?.toString()
        )

        return metadata
    }
}
