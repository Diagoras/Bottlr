package com.bottlr.app.ai

import android.net.Uri
import com.bottlr.app.data.model.RecognizedBottle

/**
 * Interface for AI-powered bottle recognition from images.
 * Implementations may use on-device (Gemini Nano) or cloud APIs.
 */
interface BottleRecognitionService {
    /**
     * Recognize bottle information from an image.
     * @param imageUri URI of the image to analyze
     * @return Recognition result with bottle details or error
     */
    suspend fun recognizeBottle(imageUri: Uri): AiRecognitionResult

    /**
     * Check if this service is available on the current device.
     * For on-device AI, checks hardware/software requirements.
     * For cloud APIs, checks if API key is configured.
     */
    fun isAvailable(): Boolean

    /**
     * Human-readable name of this AI provider.
     */
    val providerName: String
}

/**
 * Result of an AI recognition attempt.
 */
sealed interface AiRecognitionResult {
    /**
     * Recognition succeeded with bottle information.
     */
    data class Success(val bottle: RecognizedBottle) : AiRecognitionResult

    /**
     * Recognition failed with an error.
     * @param message Human-readable error message
     * @param isRecoverable True if retrying might succeed (e.g., network error)
     */
    data class Error(
        val message: String,
        val isRecoverable: Boolean = false
    ) : AiRecognitionResult

    /**
     * Service is not available (e.g., no API key, unsupported device).
     */
    data object ServiceUnavailable : AiRecognitionResult
}
