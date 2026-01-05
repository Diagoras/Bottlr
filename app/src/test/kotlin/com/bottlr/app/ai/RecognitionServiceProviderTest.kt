package com.bottlr.app.ai

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for RecognitionServiceProvider.
 *
 * Tests cover:
 * - Service selection priority (Gemini Nano > Cloud API)
 * - Availability status reporting
 * - Fallback behavior when services unavailable
 */
class RecognitionServiceProviderTest {

    @MockK
    private lateinit var geminiNanoService: GeminiNanoRecognitionService

    @MockK
    private lateinit var cloudApiService: CloudApiRecognitionService

    private lateinit var provider: RecognitionServiceProvider

    @Before
    fun setup() {
        MockKAnnotations.init(this)
    }

    private fun createProvider(): RecognitionServiceProvider {
        return RecognitionServiceProvider(geminiNanoService, cloudApiService)
    }

    // === SERVICE SELECTION TESTS ===

    @Test
    fun `getService returns GeminiNano when available`() {
        // Given
        every { geminiNanoService.isAvailable() } returns true
        every { cloudApiService.isAvailable() } returns true
        provider = createProvider()

        // When
        val service = provider.getService()

        // Then
        assertEquals(geminiNanoService, service)
    }

    @Test
    fun `getService returns CloudApi when GeminiNano unavailable`() {
        // Given
        every { geminiNanoService.isAvailable() } returns false
        every { cloudApiService.isAvailable() } returns true
        provider = createProvider()

        // When
        val service = provider.getService()

        // Then
        assertEquals(cloudApiService, service)
    }

    @Test
    fun `getService returns null when no services available`() {
        // Given
        every { geminiNanoService.isAvailable() } returns false
        every { cloudApiService.isAvailable() } returns false
        provider = createProvider()

        // When
        val service = provider.getService()

        // Then
        assertNull(service)
    }

    // === AVAILABILITY STATUS TESTS ===

    @Test
    fun `getAvailabilityStatus reflects GeminiNano availability`() {
        // Given
        every { geminiNanoService.isAvailable() } returns true
        every { cloudApiService.isAvailable() } returns false
        every { geminiNanoService.providerName } returns "Gemini Nano"
        provider = createProvider()

        // When
        val status = provider.getAvailabilityStatus()

        // Then
        assertTrue(status.geminiNanoAvailable)
        assertFalse(status.cloudApiAvailable)
        assertTrue(status.isAvailable)
        assertEquals("Gemini Nano", status.activeProvider)
    }

    @Test
    fun `getAvailabilityStatus reflects CloudApi availability`() {
        // Given
        every { geminiNanoService.isAvailable() } returns false
        every { cloudApiService.isAvailable() } returns true
        every { cloudApiService.providerName } returns "OpenAI"
        provider = createProvider()

        // When
        val status = provider.getAvailabilityStatus()

        // Then
        assertFalse(status.geminiNanoAvailable)
        assertTrue(status.cloudApiAvailable)
        assertTrue(status.isAvailable)
        assertEquals("OpenAI", status.activeProvider)
    }

    @Test
    fun `getAvailabilityStatus shows requiresApiKey when nothing available`() {
        // Given
        every { geminiNanoService.isAvailable() } returns false
        every { cloudApiService.isAvailable() } returns false
        provider = createProvider()

        // When
        val status = provider.getAvailabilityStatus()

        // Then
        assertFalse(status.isAvailable)
        assertTrue(status.requiresApiKey)
        assertNull(status.activeProvider)
    }

    @Test
    fun `getAvailabilityStatus message for GeminiNano`() {
        // Given
        every { geminiNanoService.isAvailable() } returns true
        every { cloudApiService.isAvailable() } returns false
        every { geminiNanoService.providerName } returns "Gemini Nano"
        provider = createProvider()

        // When
        val status = provider.getAvailabilityStatus()

        // Then
        assertEquals("Using on-device AI (free)", status.message)
    }

    @Test
    fun `getAvailabilityStatus message for CloudApi`() {
        // Given
        every { geminiNanoService.isAvailable() } returns false
        every { cloudApiService.isAvailable() } returns true
        every { cloudApiService.providerName } returns "Anthropic"
        provider = createProvider()

        // When
        val status = provider.getAvailabilityStatus()

        // Then
        assertEquals("Using Anthropic", status.message)
    }

    @Test
    fun `getAvailabilityStatus message when unavailable`() {
        // Given
        every { geminiNanoService.isAvailable() } returns false
        every { cloudApiService.isAvailable() } returns false
        provider = createProvider()

        // When
        val status = provider.getAvailabilityStatus()

        // Then
        assertEquals("Configure an API key in Settings to use Smart Add", status.message)
    }

    // === IS ANY SERVICE AVAILABLE TESTS ===

    @Test
    fun `isAnyServiceAvailable returns true when GeminiNano available`() {
        // Given
        every { geminiNanoService.isAvailable() } returns true
        every { cloudApiService.isAvailable() } returns false
        provider = createProvider()

        // When/Then
        assertTrue(provider.isAnyServiceAvailable())
    }

    @Test
    fun `isAnyServiceAvailable returns true when CloudApi available`() {
        // Given
        every { geminiNanoService.isAvailable() } returns false
        every { cloudApiService.isAvailable() } returns true
        provider = createProvider()

        // When/Then
        assertTrue(provider.isAnyServiceAvailable())
    }

    @Test
    fun `isAnyServiceAvailable returns false when nothing available`() {
        // Given
        every { geminiNanoService.isAvailable() } returns false
        every { cloudApiService.isAvailable() } returns false
        provider = createProvider()

        // When/Then
        assertFalse(provider.isAnyServiceAvailable())
    }
}
