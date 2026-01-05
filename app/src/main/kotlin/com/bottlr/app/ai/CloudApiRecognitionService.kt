package com.bottlr.app.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.bottlr.app.data.local.ApiKeyStore
import com.bottlr.app.data.local.ApiProvider
import com.bottlr.app.data.model.ConfidenceLevel
import com.bottlr.app.data.model.RecognizedBottle
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Bottle recognition using cloud AI APIs (OpenAI, Anthropic, Gemini Cloud).
 * Uses user-provided API keys stored in ApiKeyStore.
 */
@Singleton
class CloudApiRecognitionService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiKeyStore: ApiKeyStore
) : BottleRecognitionService {

    override val providerName: String
        get() = apiKeyStore.getPreferredProvider()?.displayName ?: "Cloud AI"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    override fun isAvailable(): Boolean {
        return apiKeyStore.hasAnyApiKey()
    }

    override suspend fun recognizeBottle(imageUri: Uri): AiRecognitionResult {
        val (provider, apiKey) = apiKeyStore.getFirstAvailableKey()
            ?: return AiRecognitionResult.ServiceUnavailable

        return withContext(Dispatchers.IO) {
            try {
                // Load and encode the image
                val imageBase64 = loadAndEncodeImage(imageUri)
                    ?: return@withContext AiRecognitionResult.Error(
                        "Failed to load image",
                        isRecoverable = false
                    )

                // Call the appropriate API
                when (provider) {
                    ApiProvider.OPENAI -> callOpenAI(apiKey, imageBase64)
                    ApiProvider.ANTHROPIC -> callAnthropic(apiKey, imageBase64)
                    ApiProvider.GEMINI_CLOUD -> callGeminiCloud(apiKey, imageBase64)
                }
            } catch (e: Exception) {
                AiRecognitionResult.Error(
                    message = e.message ?: "Recognition failed",
                    isRecoverable = true
                )
            }
        }
    }

    private fun loadAndEncodeImage(uri: Uri): String? {
        return try {
            val bitmap = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            } ?: return null

            // Resize if too large (max 2048px on longest side)
            val resized = resizeBitmap(bitmap, 2048)

            // Encode to base64 JPEG
            val outputStream = ByteArrayOutputStream()
            resized.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
        } catch (e: Exception) {
            null
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxSize && height <= maxSize) return bitmap

        val ratio = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height)
        val newWidth = (width * ratio).toInt()
        val newHeight = (height * ratio).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private fun callOpenAI(apiKey: String, imageBase64: String): AiRecognitionResult {
        val requestBody = """
            {
                "model": "gpt-4o",
                "messages": [
                    {
                        "role": "user",
                        "content": [
                            {
                                "type": "text",
                                "text": ${json.encodeToString(kotlinx.serialization.serializer(), PromptTemplates.BOTTLE_RECOGNITION)}
                            },
                            {
                                "type": "image_url",
                                "image_url": {
                                    "url": "data:image/jpeg;base64,$imageBase64"
                                }
                            }
                        ]
                    }
                ],
                "max_tokens": 500
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: return AiRecognitionResult.Error(
            "Empty response",
            isRecoverable = true
        )

        if (!response.isSuccessful) {
            return AiRecognitionResult.Error(
                "API error: ${response.code}",
                isRecoverable = response.code >= 500
            )
        }

        // Parse OpenAI response to get the content
        val jsonResponse = json.parseToJsonElement(responseBody).jsonObject
        val content = jsonResponse["choices"]
            ?.jsonObject?.get("0")
            ?.jsonObject?.get("message")
            ?.jsonObject?.get("content")
            ?.jsonPrimitive?.contentOrNull
            ?: return AiRecognitionResult.Error("Failed to parse response", isRecoverable = true)

        return parseBottleResponse(content)
    }

    private fun callAnthropic(apiKey: String, imageBase64: String): AiRecognitionResult {
        val requestBody = """
            {
                "model": "claude-sonnet-4-20250514",
                "max_tokens": 500,
                "messages": [
                    {
                        "role": "user",
                        "content": [
                            {
                                "type": "image",
                                "source": {
                                    "type": "base64",
                                    "media_type": "image/jpeg",
                                    "data": "$imageBase64"
                                }
                            },
                            {
                                "type": "text",
                                "text": ${json.encodeToString(kotlinx.serialization.serializer(), PromptTemplates.BOTTLE_RECOGNITION)}
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", apiKey)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: return AiRecognitionResult.Error(
            "Empty response",
            isRecoverable = true
        )

        if (!response.isSuccessful) {
            return AiRecognitionResult.Error(
                "API error: ${response.code}",
                isRecoverable = response.code >= 500
            )
        }

        // Parse Anthropic response
        val jsonResponse = json.parseToJsonElement(responseBody).jsonObject
        val content = jsonResponse["content"]
            ?.jsonObject?.get("0")
            ?.jsonObject?.get("text")
            ?.jsonPrimitive?.contentOrNull
            ?: return AiRecognitionResult.Error("Failed to parse response", isRecoverable = true)

        return parseBottleResponse(content)
    }

    private fun callGeminiCloud(apiKey: String, imageBase64: String): AiRecognitionResult {
        val requestBody = """
            {
                "contents": [
                    {
                        "parts": [
                            {
                                "text": ${json.encodeToString(kotlinx.serialization.serializer(), PromptTemplates.BOTTLE_RECOGNITION)}
                            },
                            {
                                "inline_data": {
                                    "mime_type": "image/jpeg",
                                    "data": "$imageBase64"
                                }
                            }
                        ]
                    }
                ],
                "generationConfig": {
                    "maxOutputTokens": 500
                }
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1/models/gemini-1.5-flash:generateContent?key=$apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string() ?: return AiRecognitionResult.Error(
            "Empty response",
            isRecoverable = true
        )

        if (!response.isSuccessful) {
            return AiRecognitionResult.Error(
                "API error: ${response.code}",
                isRecoverable = response.code >= 500
            )
        }

        // Parse Gemini response
        val jsonResponse = json.parseToJsonElement(responseBody).jsonObject
        val content = jsonResponse["candidates"]
            ?.jsonObject?.get("0")
            ?.jsonObject?.get("content")
            ?.jsonObject?.get("parts")
            ?.jsonObject?.get("0")
            ?.jsonObject?.get("text")
            ?.jsonPrimitive?.contentOrNull
            ?: return AiRecognitionResult.Error("Failed to parse response", isRecoverable = true)

        return parseBottleResponse(content)
    }

    private fun parseBottleResponse(responseText: String): AiRecognitionResult {
        return try {
            // Extract JSON from response
            val jsonStart = responseText.indexOf('{')
            val jsonEnd = responseText.lastIndexOf('}') + 1
            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                return AiRecognitionResult.Error("Invalid response format", isRecoverable = true)
            }

            val jsonString = responseText.substring(jsonStart, jsonEnd)
            val jsonObj = json.parseToJsonElement(jsonString).jsonObject

            // Check for error
            jsonObj["error"]?.jsonPrimitive?.contentOrNull?.let { error ->
                return AiRecognitionResult.Error(error, isRecoverable = false)
            }

            // Parse bottle
            val bottle = RecognizedBottle(
                name = jsonObj["name"]?.jsonPrimitive?.contentOrNull,
                distillery = jsonObj["distillery"]?.jsonPrimitive?.contentOrNull,
                type = jsonObj["type"]?.jsonPrimitive?.contentOrNull,
                region = jsonObj["region"]?.jsonPrimitive?.contentOrNull,
                abv = jsonObj["abv"]?.jsonPrimitive?.floatOrNull,
                age = jsonObj["age"]?.jsonPrimitive?.intOrNull,
                confidence = parseConfidence(jsonObj["confidence"]?.jsonObject)
            )

            AiRecognitionResult.Success(bottle)
        } catch (e: Exception) {
            AiRecognitionResult.Error(
                message = "Failed to parse response: ${e.message}",
                isRecoverable = true
            )
        }
    }

    private fun parseConfidence(confidenceJson: JsonObject?): Map<String, ConfidenceLevel> {
        if (confidenceJson == null) return emptyMap()

        return confidenceJson.entries.mapNotNull { (key, value) ->
            val level = when (value.jsonPrimitive.contentOrNull?.uppercase()) {
                "HIGH" -> ConfidenceLevel.HIGH
                "MEDIUM" -> ConfidenceLevel.MEDIUM
                "LOW" -> ConfidenceLevel.LOW
                else -> ConfidenceLevel.UNKNOWN
            }
            key to level
        }.toMap()
    }
}
