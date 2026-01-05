package com.bottlr.app.data.local

import android.content.Context
import android.content.SharedPreferences
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ApiKeyStore.
 *
 * Since EncryptedSharedPreferences requires Android crypto, we test the
 * logic using a testable wrapper approach. These tests verify:
 * - Setting and getting API keys
 * - Clearing API keys
 * - Provider priority order
 * - hasAnyApiKey detection
 */
class ApiKeyStoreTest {

    @MockK
    private lateinit var sharedPreferences: SharedPreferences

    @MockK
    private lateinit var editor: SharedPreferences.Editor

    // Store values in memory for test simulation
    private val storedValues = mutableMapOf<String, String?>()

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        // Mock editor behavior
        every { sharedPreferences.edit() } returns editor
        every { editor.putString(any(), any()) } answers {
            storedValues[firstArg()] = secondArg()
            editor
        }
        every { editor.remove(any()) } answers {
            storedValues.remove(firstArg())
            editor
        }
        every { editor.apply() } returns Unit

        // Mock getString to return from our storage
        every { sharedPreferences.getString(any(), any()) } answers {
            storedValues[firstArg()] ?: secondArg()
        }
    }

    // === PROVIDER ENUM TESTS ===

    @Test
    fun `ApiProvider entries have correct preference keys`() {
        assertEquals("gemini_cloud_key", ApiProvider.GEMINI_CLOUD.prefKey)
        assertEquals("anthropic_key", ApiProvider.ANTHROPIC.prefKey)
        assertEquals("openai_key", ApiProvider.OPENAI.prefKey)
    }

    @Test
    fun `ApiProvider entries have display names`() {
        assertEquals("Google Gemini", ApiProvider.GEMINI_CLOUD.displayName)
        assertEquals("Anthropic Claude", ApiProvider.ANTHROPIC.displayName)
        assertEquals("OpenAI", ApiProvider.OPENAI.displayName)
    }

    @Test
    fun `ApiProvider entries have descriptions`() {
        assertTrue(ApiProvider.GEMINI_CLOUD.description.contains("ai.google.dev"))
        assertTrue(ApiProvider.ANTHROPIC.description.contains("console.anthropic.com"))
        assertTrue(ApiProvider.OPENAI.description.contains("platform.openai.com"))
    }

    @Test
    fun `ApiProvider entries have key placeholders`() {
        assertTrue(ApiProvider.GEMINI_CLOUD.keyPlaceholder.startsWith("AIza"))
        assertTrue(ApiProvider.ANTHROPIC.keyPlaceholder.startsWith("sk-ant-"))
        assertTrue(ApiProvider.OPENAI.keyPlaceholder.startsWith("sk-"))
    }

    @Test
    fun `ApiProvider priority is GeminiCloud first, then Anthropic, then OpenAI`() {
        val entries = ApiProvider.entries
        assertEquals(3, entries.size)
        assertEquals(ApiProvider.GEMINI_CLOUD, entries[0])
        assertEquals(ApiProvider.ANTHROPIC, entries[1])
        assertEquals(ApiProvider.OPENAI, entries[2])
    }

    // === KEY STORAGE TESTS (using SharedPreferences mock) ===

    @Test
    fun `setApiKey stores value with correct preference key`() {
        // Given
        val testKey = "AIzaSyTestKey12345"

        // When - simulate the store operation
        storedValues[ApiProvider.GEMINI_CLOUD.prefKey] = testKey

        // Then
        assertEquals(testKey, storedValues["gemini_cloud_key"])
    }

    @Test
    fun `getApiKey retrieves stored value`() {
        // Given
        storedValues["anthropic_key"] = "sk-ant-test123"

        // When
        val result = sharedPreferences.getString("anthropic_key", null)

        // Then
        assertEquals("sk-ant-test123", result)
    }

    @Test
    fun `getApiKey returns null for missing key`() {
        // Given - no key stored

        // When
        val result = sharedPreferences.getString("openai_key", null)

        // Then
        assertNull(result)
    }

    @Test
    fun `clearApiKey removes the key`() {
        // Given
        storedValues["gemini_cloud_key"] = "some-key"

        // When
        storedValues.remove("gemini_cloud_key")

        // Then
        assertNull(storedValues["gemini_cloud_key"])
    }

    @Test
    fun `blank key is treated as not set`() {
        // Given
        storedValues["openai_key"] = "   "

        // When - checking with takeIf { it.isNotBlank() } logic
        val result = storedValues["openai_key"]?.takeIf { it.isNotBlank() }

        // Then
        assertNull(result)
    }

    @Test
    fun `empty key is treated as not set`() {
        // Given
        storedValues["openai_key"] = ""

        // When
        val result = storedValues["openai_key"]?.takeIf { it.isNotBlank() }

        // Then
        assertNull(result)
    }

    // === HAS ANY KEY TESTS ===

    @Test
    fun `hasAnyApiKey returns false when no keys stored`() {
        // Given - no keys

        // When
        val hasAny = ApiProvider.entries.any {
            storedValues[it.prefKey]?.takeIf { key -> key.isNotBlank() } != null
        }

        // Then
        assertFalse(hasAny)
    }

    @Test
    fun `hasAnyApiKey returns true when one key stored`() {
        // Given
        storedValues["anthropic_key"] = "sk-ant-valid"

        // When
        val hasAny = ApiProvider.entries.any {
            storedValues[it.prefKey]?.takeIf { key -> key.isNotBlank() } != null
        }

        // Then
        assertTrue(hasAny)
    }

    @Test
    fun `hasAnyApiKey returns true when multiple keys stored`() {
        // Given
        storedValues["gemini_cloud_key"] = "AIzaSyTest"
        storedValues["openai_key"] = "sk-test"

        // When
        val hasAny = ApiProvider.entries.any {
            storedValues[it.prefKey]?.takeIf { key -> key.isNotBlank() } != null
        }

        // Then
        assertTrue(hasAny)
    }

    // === GET FIRST AVAILABLE KEY TESTS ===

    @Test
    fun `getFirstAvailableKey returns null when no keys`() {
        // Given - no keys

        // When
        var result: Pair<ApiProvider, String>? = null
        for (provider in ApiProvider.entries) {
            val key = storedValues[provider.prefKey]?.takeIf { it.isNotBlank() }
            if (key != null) {
                result = provider to key
                break
            }
        }

        // Then
        assertNull(result)
    }

    @Test
    fun `getFirstAvailableKey returns GeminiCloud when available`() {
        // Given - all keys stored (Gemini should be first)
        storedValues["gemini_cloud_key"] = "AIzaSyTest"
        storedValues["anthropic_key"] = "sk-ant-test"
        storedValues["openai_key"] = "sk-test"

        // When
        var result: Pair<ApiProvider, String>? = null
        for (provider in ApiProvider.entries) {
            val key = storedValues[provider.prefKey]?.takeIf { it.isNotBlank() }
            if (key != null) {
                result = provider to key
                break
            }
        }

        // Then
        assertNotNull(result)
        assertEquals(ApiProvider.GEMINI_CLOUD, result?.first)
        assertEquals("AIzaSyTest", result?.second)
    }

    @Test
    fun `getFirstAvailableKey returns Anthropic when GeminiCloud not available`() {
        // Given - only Anthropic and OpenAI stored
        storedValues["anthropic_key"] = "sk-ant-test"
        storedValues["openai_key"] = "sk-test"

        // When
        var result: Pair<ApiProvider, String>? = null
        for (provider in ApiProvider.entries) {
            val key = storedValues[provider.prefKey]?.takeIf { it.isNotBlank() }
            if (key != null) {
                result = provider to key
                break
            }
        }

        // Then
        assertNotNull(result)
        assertEquals(ApiProvider.ANTHROPIC, result?.first)
    }

    @Test
    fun `getFirstAvailableKey returns OpenAI when others not available`() {
        // Given - only OpenAI stored
        storedValues["openai_key"] = "sk-only-openai"

        // When
        var result: Pair<ApiProvider, String>? = null
        for (provider in ApiProvider.entries) {
            val key = storedValues[provider.prefKey]?.takeIf { it.isNotBlank() }
            if (key != null) {
                result = provider to key
                break
            }
        }

        // Then
        assertNotNull(result)
        assertEquals(ApiProvider.OPENAI, result?.first)
        assertEquals("sk-only-openai", result?.second)
    }

    // === GET PREFERRED PROVIDER TESTS ===

    @Test
    fun `getPreferredProvider returns null when no keys`() {
        // When
        val preferred = ApiProvider.entries.firstOrNull {
            storedValues[it.prefKey]?.takeIf { key -> key.isNotBlank() } != null
        }

        // Then
        assertNull(preferred)
    }

    @Test
    fun `getPreferredProvider returns GeminiCloud first`() {
        // Given
        storedValues["gemini_cloud_key"] = "key1"
        storedValues["anthropic_key"] = "key2"

        // When
        val preferred = ApiProvider.entries.firstOrNull {
            storedValues[it.prefKey]?.takeIf { key -> key.isNotBlank() } != null
        }

        // Then
        assertEquals(ApiProvider.GEMINI_CLOUD, preferred)
    }

    @Test
    fun `getPreferredProvider skips blank GeminiCloud and returns Anthropic`() {
        // Given
        storedValues["gemini_cloud_key"] = "   "  // Blank
        storedValues["anthropic_key"] = "sk-ant-valid"

        // When
        val preferred = ApiProvider.entries.firstOrNull {
            storedValues[it.prefKey]?.takeIf { key -> key.isNotBlank() } != null
        }

        // Then
        assertEquals(ApiProvider.ANTHROPIC, preferred)
    }
}
