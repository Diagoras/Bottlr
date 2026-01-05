package com.bottlr.app.data.repository

import com.bottlr.app.data.model.ConfidenceLevel
import com.bottlr.app.data.model.DataSource
import com.bottlr.app.data.model.RecognizedBottle
import com.bottlr.app.data.remote.BottleDbClient
import com.bottlr.app.data.remote.BottleSearchResult
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for EnrichmentRepository.
 *
 * Tests cover:
 * - Enrichment with database results
 * - Handling when database returns no results
 * - Field metadata and source tracking
 * - Merging AI data with database data
 */
class EnrichmentRepositoryTest {

    @MockK
    private lateinit var bottleDbClient: BottleDbClient

    private lateinit var repository: EnrichmentRepository

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        every { bottleDbClient.isAvailable() } returns true
        repository = EnrichmentRepository(bottleDbClient)
    }

    // === BASIC ENRICHMENT TESTS ===

    @Test
    fun `enrich with no name returns AI-only data`() = runTest {
        // Given - Recognized bottle with no name
        val recognized = RecognizedBottle(
            name = null,
            distillery = "Test Distillery"
        )

        // When
        val enriched = repository.enrich(recognized)

        // Then
        assertEquals(DataSource.AI_ONLY, enriched.source)
        assertNull(enriched.name)
        assertEquals("Test Distillery", enriched.distillery)
    }

    @Test
    fun `enrich with blank name returns AI-only data`() = runTest {
        // Given
        val recognized = RecognizedBottle(
            name = "   ",
            distillery = "Test Distillery"
        )

        // When
        val enriched = repository.enrich(recognized)

        // Then
        assertEquals(DataSource.AI_ONLY, enriched.source)
    }

    @Test
    fun `enrich with no database match returns AI-only data`() = runTest {
        // Given
        val recognized = RecognizedBottle(
            name = "Unknown Whisky",
            distillery = "Unknown Distillery"
        )
        coEvery { bottleDbClient.searchBottle(any(), any()) } returns null

        // When
        val enriched = repository.enrich(recognized)

        // Then
        assertEquals(DataSource.AI_ONLY, enriched.source)
        assertEquals("Unknown Whisky", enriched.name)
    }

    @Test
    fun `enrich with database match returns AI_AND_DATABASE source`() = runTest {
        // Given
        val recognized = RecognizedBottle(
            name = "Lagavulin 16",
            distillery = "Lagavulin"
        )
        val dbResult = BottleSearchResult(
            name = "Lagavulin 16 Year Old",
            distillery = "Lagavulin Distillery",
            type = "Single Malt Scotch",
            region = "Islay",
            abv = 43.0f,
            age = 16,
            notes = "Intense peat smoke and rich sweetness",
            keywords = "peaty, smoky, islay",
            source = "BottleDB"
        )
        coEvery { bottleDbClient.searchBottle("Lagavulin 16", "Lagavulin") } returns dbResult

        // When
        val enriched = repository.enrich(recognized)

        // Then
        assertEquals(DataSource.AI_AND_DATABASE, enriched.source)
    }

    // === DATA MERGING TESTS ===

    @Test
    fun `enrich prefers database name over AI name`() = runTest {
        // Given
        val recognized = RecognizedBottle(name = "Lagavulin")
        val dbResult = BottleSearchResult(
            name = "Lagavulin 16 Year Old",
            source = "BottleDB"
        )
        coEvery { bottleDbClient.searchBottle(any(), any()) } returns dbResult

        // When
        val enriched = repository.enrich(recognized)

        // Then
        assertEquals("Lagavulin 16 Year Old", enriched.name)
    }

    @Test
    fun `enrich includes additional database fields`() = runTest {
        // Given
        val recognized = RecognizedBottle(
            name = "Test Whisky"
        )
        val dbResult = BottleSearchResult(
            name = "Test Whisky Premium",
            notes = "Smooth and balanced",
            keywords = "smooth, oak, vanilla",
            source = "BottleDB"
        )
        coEvery { bottleDbClient.searchBottle(any(), any()) } returns dbResult

        // When
        val enriched = repository.enrich(recognized)

        // Then
        assertEquals("Smooth and balanced", enriched.notes)
        assertEquals("smooth, oak, vanilla", enriched.keywords)
    }

    @Test
    fun `enrich falls back to AI values when database has nulls`() = runTest {
        // Given
        val recognized = RecognizedBottle(
            name = "Test Whisky",
            distillery = "AI Distillery",
            type = "Bourbon",
            region = "Kentucky"
        )
        val dbResult = BottleSearchResult(
            name = "Test Whisky",
            distillery = null,  // No distillery in database
            type = null,        // No type in database
            region = "Kentucky, USA",
            source = "BottleDB"
        )
        coEvery { bottleDbClient.searchBottle(any(), any()) } returns dbResult

        // When
        val enriched = repository.enrich(recognized)

        // Then
        assertEquals("AI Distillery", enriched.distillery)  // AI fallback
        assertEquals("Bourbon", enriched.type)               // AI fallback
        assertEquals("Kentucky, USA", enriched.region)       // Database preferred
    }

    // === FIELD METADATA TESTS ===

    @Test
    fun `enrich preserves AI confidence in field metadata`() = runTest {
        // Given
        val recognized = RecognizedBottle(
            name = "Test Whisky",
            confidence = mapOf(
                "name" to ConfidenceLevel.HIGH,
                "distillery" to ConfidenceLevel.MEDIUM,
                "type" to ConfidenceLevel.LOW
            )
        )
        coEvery { bottleDbClient.searchBottle(any(), any()) } returns null

        // When
        val enriched = repository.enrich(recognized)

        // Then
        assertEquals(ConfidenceLevel.HIGH, enriched.fieldMetadata["name"]?.confidence)
        assertEquals(ConfidenceLevel.MEDIUM, enriched.fieldMetadata["distillery"]?.confidence)
        assertEquals(ConfidenceLevel.LOW, enriched.fieldMetadata["type"]?.confidence)
    }

    @Test
    fun `enrich tracks AI and database values in field metadata`() = runTest {
        // Given
        val recognized = RecognizedBottle(
            name = "Lagavulin",
            distillery = "Lag"
        )
        val dbResult = BottleSearchResult(
            name = "Lagavulin 16 Year Old",
            distillery = "Lagavulin Distillery",
            source = "BottleDB"
        )
        coEvery { bottleDbClient.searchBottle(any(), any()) } returns dbResult

        // When
        val enriched = repository.enrich(recognized)

        // Then
        val nameMeta = enriched.fieldMetadata["name"]
        assertEquals("Lagavulin", nameMeta?.aiValue)
        assertEquals("Lagavulin 16 Year Old", nameMeta?.databaseValue)
        assertEquals(DataSource.AI_AND_DATABASE, nameMeta?.source)
    }

    @Test
    fun `enrich sets source to AI_ONLY in metadata when no database value`() = runTest {
        // Given
        val recognized = RecognizedBottle(
            name = "Test Whisky",
            abv = 46.0f
        )
        coEvery { bottleDbClient.searchBottle(any(), any()) } returns null

        // When
        val enriched = repository.enrich(recognized)

        // Then
        assertEquals(DataSource.AI_ONLY, enriched.fieldMetadata["abv"]?.source)
    }

    // === DATABASE CLIENT AVAILABILITY TESTS ===

    @Test
    fun `enrich skips database when client unavailable`() = runTest {
        // Given
        every { bottleDbClient.isAvailable() } returns false
        val recognized = RecognizedBottle(name = "Test Whisky")

        // When
        val enriched = repository.enrich(recognized)

        // Then
        assertEquals(DataSource.AI_ONLY, enriched.source)
    }

    // === NUMERIC FIELD TESTS ===

    @Test
    fun `enrich correctly handles ABV values`() = runTest {
        // Given
        val recognized = RecognizedBottle(
            name = "Test",
            abv = 0.40f  // AI might return as decimal
        )
        val dbResult = BottleSearchResult(
            name = "Test Premium",
            abv = 40.0f,  // Database might return as percentage
            source = "BottleDB"
        )
        coEvery { bottleDbClient.searchBottle(any(), any()) } returns dbResult

        // When
        val enriched = repository.enrich(recognized)

        // Then
        assertEquals(40.0f, enriched.abv)  // Database value preferred
    }

    @Test
    fun `enrich correctly handles age values`() = runTest {
        // Given
        val recognized = RecognizedBottle(
            name = "Test",
            age = 12
        )
        val dbResult = BottleSearchResult(
            name = "Test 15 Year",
            age = 15,
            source = "BottleDB"
        )
        coEvery { bottleDbClient.searchBottle(any(), any()) } returns dbResult

        // When
        val enriched = repository.enrich(recognized)

        // Then
        assertEquals(15, enriched.age)  // Database value preferred
    }
}
