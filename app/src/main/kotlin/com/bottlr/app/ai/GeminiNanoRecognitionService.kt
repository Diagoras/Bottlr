package com.bottlr.app.ai

import android.content.Context
import android.net.Uri
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bottle recognition using on-device Gemini Nano via ML Kit GenAI.
 * Only available on supported devices (Pixel 9+, some Samsung flagships).
 *
 * Note: ML Kit GenAI Prompt API is currently in alpha. This implementation
 * checks for device compatibility but may need updates as the API stabilizes.
 */
@Singleton
class GeminiNanoRecognitionService @Inject constructor(
    @ApplicationContext private val context: Context
) : BottleRecognitionService {

    override val providerName: String = "Gemini Nano (On-Device)"

    private var availabilityChecked = false
    private var isServiceAvailable = false

    override fun isAvailable(): Boolean {
        if (!availabilityChecked) {
            checkAvailability()
        }
        return isServiceAvailable
    }

    private fun checkAvailability() {
        // ML Kit GenAI requires:
        // - Android 14+ (API 34)
        // - Supported device (Pixel 9+, some Samsung flagships)
        // - AICore system service available
        isServiceAvailable = try {
            if (Build.VERSION.SDK_INT < 34) {
                false
            } else {
                // Check if AICore is available by trying to resolve the service
                // This is a simplified check - the actual ML Kit API will do more thorough checks
                val aiCorePackage = "com.google.android.aicore"
                val packageManager = context.packageManager
                try {
                    packageManager.getPackageInfo(aiCorePackage, 0)
                    true
                } catch (e: Exception) {
                    false
                }
            }
        } catch (e: Exception) {
            false
        }
        availabilityChecked = true
    }

    override suspend fun recognizeBottle(imageUri: Uri): AiRecognitionResult {
        if (!isAvailable()) {
            return AiRecognitionResult.ServiceUnavailable
        }

        // TODO: Implement actual ML Kit GenAI Prompt API call when API stabilizes
        // The ML Kit GenAI Prompt API is currently in alpha and the exact API
        // surface may change. For now, we return unavailable and fall back to cloud APIs.
        //
        // When implementing:
        // 1. Use InferenceClient.create() to get a client
        // 2. Build a prompt with text and image
        // 3. Call generateContent() and parse the response
        //
        // See: https://developers.google.com/ml-kit/genai/prompt/android/get-started

        return AiRecognitionResult.Error(
            message = "On-device AI is not yet implemented. Please configure a cloud API key.",
            isRecoverable = false
        )
    }
}
