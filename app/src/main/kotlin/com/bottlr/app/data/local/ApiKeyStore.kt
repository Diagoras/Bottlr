package com.bottlr.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Secure storage for user-provided AI API keys.
 * Uses Android's EncryptedSharedPreferences for secure storage.
 */
@Singleton
class ApiKeyStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val encryptedPrefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Store an API key for a provider.
     */
    fun setApiKey(provider: ApiProvider, key: String) {
        encryptedPrefs.edit().putString(provider.prefKey, key).apply()
    }

    /**
     * Retrieve an API key for a provider.
     * @return The API key or null if not set.
     */
    fun getApiKey(provider: ApiProvider): String? {
        return encryptedPrefs.getString(provider.prefKey, null)?.takeIf { it.isNotBlank() }
    }

    /**
     * Remove an API key for a provider.
     */
    fun clearApiKey(provider: ApiProvider) {
        encryptedPrefs.edit().remove(provider.prefKey).apply()
    }

    /**
     * Check if any valid API key is configured.
     */
    fun hasAnyApiKey(): Boolean {
        return ApiProvider.entries.any { getApiKey(it) != null }
    }

    /**
     * Get the first available API key and its provider.
     * @return Pair of provider and key, or null if none configured.
     */
    fun getFirstAvailableKey(): Pair<ApiProvider, String>? {
        for (provider in ApiProvider.entries) {
            val key = getApiKey(provider)
            if (key != null) {
                return provider to key
            }
        }
        return null
    }

    /**
     * Get the preferred provider based on what's configured.
     * Priority: Gemini Cloud > Anthropic > OpenAI
     */
    fun getPreferredProvider(): ApiProvider? {
        return ApiProvider.entries.firstOrNull { getApiKey(it) != null }
    }

    companion object {
        private const val PREFS_NAME = "bottlr_api_keys"
    }
}

/**
 * Supported AI API providers.
 * Order determines priority when multiple keys are configured.
 */
enum class ApiProvider(
    val prefKey: String,
    val displayName: String,
    val description: String,
    val keyPlaceholder: String
) {
    GEMINI_CLOUD(
        prefKey = "gemini_cloud_key",
        displayName = "Google Gemini",
        description = "Get key from ai.google.dev",
        keyPlaceholder = "AIza..."
    ),
    ANTHROPIC(
        prefKey = "anthropic_key",
        displayName = "Anthropic Claude",
        description = "Get key from console.anthropic.com",
        keyPlaceholder = "sk-ant-..."
    ),
    OPENAI(
        prefKey = "openai_key",
        displayName = "OpenAI",
        description = "Get key from platform.openai.com",
        keyPlaceholder = "sk-..."
    )
}
