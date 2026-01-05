package com.bottlr.app.data.model

import kotlinx.serialization.Serializable

/**
 * Represents bottle information extracted by AI from an image.
 * All fields are nullable since AI recognition may not extract all details.
 */
@Serializable
data class RecognizedBottle(
    val name: String? = null,
    val distillery: String? = null,
    val type: String? = null,
    val region: String? = null,
    val abv: Float? = null,
    val age: Int? = null,
    val confidence: Map<String, ConfidenceLevel> = emptyMap()
)

/**
 * Confidence level for each recognized field.
 * Helps users understand which fields need manual verification.
 */
@Serializable
enum class ConfidenceLevel {
    HIGH,    // Clearly visible, high certainty
    MEDIUM,  // Partially visible or inferred
    LOW,     // Guessed or uncertain
    UNKNOWN  // No confidence data available
}
