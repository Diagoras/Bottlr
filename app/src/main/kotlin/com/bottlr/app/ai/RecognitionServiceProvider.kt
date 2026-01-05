package com.bottlr.app.ai

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides the best available bottle recognition service.
 * Prefers on-device Gemini Nano when available, falls back to cloud APIs.
 */
@Singleton
class RecognitionServiceProvider @Inject constructor(
    private val geminiNanoService: GeminiNanoRecognitionService,
    private val cloudApiService: CloudApiRecognitionService
) {
    /**
     * Get the best available recognition service.
     * @return The service to use, or null if no service is available.
     */
    fun getService(): BottleRecognitionService? {
        return when {
            geminiNanoService.isAvailable() -> geminiNanoService
            cloudApiService.isAvailable() -> cloudApiService
            else -> null
        }
    }

    /**
     * Get the current availability status of all services.
     */
    fun getAvailabilityStatus(): AvailabilityStatus {
        return AvailabilityStatus(
            geminiNanoAvailable = geminiNanoService.isAvailable(),
            cloudApiAvailable = cloudApiService.isAvailable(),
            activeProvider = getService()?.providerName
        )
    }

    /**
     * Check if any recognition service is available.
     */
    fun isAnyServiceAvailable(): Boolean {
        return geminiNanoService.isAvailable() || cloudApiService.isAvailable()
    }
}

/**
 * Status of AI recognition service availability.
 */
data class AvailabilityStatus(
    val geminiNanoAvailable: Boolean,
    val cloudApiAvailable: Boolean,
    val activeProvider: String?
) {
    val isAvailable: Boolean
        get() = geminiNanoAvailable || cloudApiAvailable

    val requiresApiKey: Boolean
        get() = !geminiNanoAvailable && !cloudApiAvailable

    val message: String
        get() = when {
            geminiNanoAvailable -> "Using on-device AI (free)"
            cloudApiAvailable -> "Using ${activeProvider ?: "cloud AI"}"
            else -> "Configure an API key in Settings to use Smart Add"
        }
}
