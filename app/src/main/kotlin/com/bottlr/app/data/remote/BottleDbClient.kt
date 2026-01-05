package com.bottlr.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Client for BottleDB.org - an open whisky database.
 * See: https://bottledb.org/
 *
 * Note: This is a simplified implementation. BottleDB's actual API
 * may require registration or have different endpoints.
 */
@Singleton
class BottleDbClient @Inject constructor() : LiquorDatabaseService {

    override val databaseName: String = "BottleDB"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    override fun isAvailable(): Boolean {
        // BottleDB is a public database, always available
        return true
    }

    override suspend fun searchBottle(name: String, distillery: String?): BottleSearchResult? {
        return withContext(Dispatchers.IO) {
            try {
                // Build search query
                val searchQuery = buildString {
                    append(name)
                    if (!distillery.isNullOrBlank()) {
                        append(" ")
                        append(distillery)
                    }
                }.trim()

                // Search BottleDB API
                // Note: This is a placeholder URL - actual API endpoint may differ
                val encodedQuery = java.net.URLEncoder.encode(searchQuery, "UTF-8")
                val request = Request.Builder()
                    .url("https://bottledb.org/api/v1/search?q=$encodedQuery")
                    .addHeader("Accept", "application/json")
                    .build()

                val response = httpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext null
                }

                val responseBody = response.body?.string() ?: return@withContext null
                parseSearchResult(responseBody)
            } catch (e: Exception) {
                // Log error but don't crash
                null
            }
        }
    }

    private fun parseSearchResult(responseBody: String): BottleSearchResult? {
        return try {
            val jsonResponse = json.parseToJsonElement(responseBody).jsonObject
            val results = jsonResponse["results"]?.jsonArray ?: return null

            if (results.isEmpty()) return null

            // Take the first (best) match
            val firstResult = results[0].jsonObject
            parseBottle(firstResult)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseBottle(bottle: JsonObject): BottleSearchResult {
        return BottleSearchResult(
            name = bottle["name"]?.jsonPrimitive?.contentOrNull ?: "",
            distillery = bottle["distillery"]?.jsonPrimitive?.contentOrNull,
            type = bottle["type"]?.jsonPrimitive?.contentOrNull,
            region = bottle["region"]?.jsonPrimitive?.contentOrNull,
            abv = bottle["abv"]?.jsonPrimitive?.floatOrNull,
            age = bottle["age"]?.jsonPrimitive?.intOrNull,
            notes = bottle["notes"]?.jsonPrimitive?.contentOrNull
                ?: bottle["tasting_notes"]?.jsonPrimitive?.contentOrNull,
            keywords = bottle["tags"]?.jsonPrimitive?.contentOrNull,
            source = databaseName
        )
    }
}
