package com.bottlr.app.ui.smartcapture

import android.net.Uri
import app.cash.turbine.test
import com.bottlr.app.ai.AiRecognitionResult
import com.bottlr.app.ai.AvailabilityStatus
import com.bottlr.app.ai.BottleRecognitionService
import com.bottlr.app.ai.RecognitionServiceProvider
import com.bottlr.app.data.model.ConfidenceLevel
import com.bottlr.app.data.model.DataSource
import com.bottlr.app.data.model.EnrichedBottle
import com.bottlr.app.data.model.RecognizedBottle
import com.bottlr.app.data.repository.EnrichmentRepository
import com.bottlr.app.util.MainDispatcherRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for SmartCaptureViewModel.
 *
 * Tests cover:
 * - Initial state (Camera)
 * - State transitions through the capture flow
 * - AI recognition success and error handling
 * - Service unavailability handling
 * - Field updates during review
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SmartCaptureViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var recognitionProvider: RecognitionServiceProvider

    @MockK
    private lateinit var enrichmentRepository: EnrichmentRepository

    @MockK
    private lateinit var mockRecognitionService: BottleRecognitionService

    private lateinit var viewModel: SmartCaptureViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        // Mock Uri.parse for test URIs
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk(relaxed = true)
    }

    private fun createViewModel(): SmartCaptureViewModel {
        return SmartCaptureViewModel(recognitionProvider, enrichmentRepository)
    }

    // === INITIAL STATE TESTS ===

    @Test
    fun `initial state is Camera`() = runTest {
        // Given
        every { recognitionProvider.getAvailabilityStatus() } returns AvailabilityStatus(
            geminiNanoAvailable = false,
            cloudApiAvailable = false,
            activeProvider = null
        )

        // When
        viewModel = createViewModel()

        // Then
        viewModel.state.test {
            assertEquals(SmartCaptureState.Camera, awaitItem())
        }
    }

    // === IMAGE CAPTURE TESTS ===

    @Test
    fun `onImageCaptured updates state to Captured`() = runTest {
        // Given
        viewModel = createViewModel()
        val testUri = mockk<Uri>(relaxed = true)

        // When
        viewModel.onImageCaptured(testUri)

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state is SmartCaptureState.Captured)
            assertEquals(testUri, (state as SmartCaptureState.Captured).imageUri)
        }
    }

    @Test
    fun `onImageCaptured updates capturedPhotoUri`() = runTest {
        // Given
        viewModel = createViewModel()
        val testUri = mockk<Uri>(relaxed = true)

        // When
        viewModel.onImageCaptured(testUri)

        // Then
        viewModel.capturedPhotoUri.test {
            assertEquals(testUri, awaitItem())
        }
    }

    // === RECOGNITION FLOW TESTS ===

    @Test
    fun `startRecognition with no service sets Error state with API key prompt`() = runTest {
        // Given
        viewModel = createViewModel()
        val testUri = mockk<Uri>(relaxed = true)
        viewModel.onImageCaptured(testUri)

        every { recognitionProvider.getService() } returns null

        // When
        viewModel.startRecognition()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state is SmartCaptureState.Error)
            val errorState = state as SmartCaptureState.Error
            assertTrue(errorState.canConfigureApiKey)
            assertFalse(errorState.canRetry)
        }
    }

    @Test
    fun `startRecognition success transitions through Recognizing to Review`() = runTest {
        // Given
        viewModel = createViewModel()
        val testUri = mockk<Uri>(relaxed = true)
        viewModel.onImageCaptured(testUri)

        val recognizedBottle = RecognizedBottle(
            name = "Lagavulin 16",
            distillery = "Lagavulin",
            type = "Single Malt",
            region = "Islay",
            abv = 0.43f,
            age = 16,
            confidence = mapOf("name" to ConfidenceLevel.HIGH)
        )

        val enrichedBottle = EnrichedBottle(
            name = "Lagavulin 16",
            distillery = "Lagavulin",
            type = "Single Malt",
            region = "Islay",
            abv = 0.43f,
            age = 16,
            source = DataSource.AI_ONLY
        )

        every { recognitionProvider.getService() } returns mockRecognitionService
        coEvery { mockRecognitionService.recognizeBottle(any()) } returns AiRecognitionResult.Success(recognizedBottle)
        coEvery { enrichmentRepository.enrich(recognizedBottle) } returns enrichedBottle

        // When
        viewModel.startRecognition()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertTrue("Expected Review state but got $state", state is SmartCaptureState.Review)
            val reviewState = state as SmartCaptureState.Review
            assertEquals("Lagavulin 16", reviewState.bottle.name)
        }
    }

    @Test
    fun `startRecognition error sets Error state`() = runTest {
        // Given
        viewModel = createViewModel()
        val testUri = mockk<Uri>(relaxed = true)
        viewModel.onImageCaptured(testUri)

        every { recognitionProvider.getService() } returns mockRecognitionService
        coEvery { mockRecognitionService.recognizeBottle(any()) } returns AiRecognitionResult.Error(
            message = "Network error",
            isRecoverable = true
        )

        // When
        viewModel.startRecognition()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state is SmartCaptureState.Error)
            val errorState = state as SmartCaptureState.Error
            assertEquals("Network error", errorState.message)
            assertTrue(errorState.canRetry)
        }
    }

    @Test
    fun `startRecognition ServiceUnavailable sets Error state`() = runTest {
        // Given
        viewModel = createViewModel()
        val testUri = mockk<Uri>(relaxed = true)
        viewModel.onImageCaptured(testUri)

        every { recognitionProvider.getService() } returns mockRecognitionService
        coEvery { mockRecognitionService.recognizeBottle(any()) } returns AiRecognitionResult.ServiceUnavailable

        // When
        viewModel.startRecognition()

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state is SmartCaptureState.Error)
            val errorState = state as SmartCaptureState.Error
            assertTrue(errorState.canConfigureApiKey)
        }
    }

    // === RETRY AND RESET TESTS ===

    @Test
    fun `resetToCamera clears state and returns to Camera`() = runTest {
        // Given
        viewModel = createViewModel()
        val testUri = mockk<Uri>(relaxed = true)
        viewModel.onImageCaptured(testUri)

        // When
        viewModel.resetToCamera()

        // Then
        viewModel.state.test {
            assertEquals(SmartCaptureState.Camera, awaitItem())
        }
        viewModel.capturedPhotoUri.test {
            assertNull(awaitItem())
        }
    }

    // === FIELD UPDATE TESTS ===

    @Test
    fun `updateField updates bottle name in Review state`() = runTest {
        // Given
        viewModel = createViewModel()
        val testUri = mockk<Uri>(relaxed = true)
        viewModel.onImageCaptured(testUri)

        val recognizedBottle = RecognizedBottle(name = "Original Name")
        val enrichedBottle = EnrichedBottle(name = "Original Name", source = DataSource.AI_ONLY)

        every { recognitionProvider.getService() } returns mockRecognitionService
        coEvery { mockRecognitionService.recognizeBottle(any()) } returns AiRecognitionResult.Success(recognizedBottle)
        coEvery { enrichmentRepository.enrich(any()) } returns enrichedBottle

        viewModel.startRecognition()

        // When
        viewModel.updateField("name", "Updated Name")

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state is SmartCaptureState.Review)
            assertEquals("Updated Name", (state as SmartCaptureState.Review).bottle.name)
        }
    }

    @Test
    fun `updateField updates ABV with float parsing`() = runTest {
        // Given
        viewModel = createViewModel()
        val testUri = mockk<Uri>(relaxed = true)
        viewModel.onImageCaptured(testUri)

        val enrichedBottle = EnrichedBottle(name = "Test", abv = null, source = DataSource.AI_ONLY)

        every { recognitionProvider.getService() } returns mockRecognitionService
        coEvery { mockRecognitionService.recognizeBottle(any()) } returns AiRecognitionResult.Success(RecognizedBottle())
        coEvery { enrichmentRepository.enrich(any()) } returns enrichedBottle

        viewModel.startRecognition()

        // When
        viewModel.updateField("abv", "46.0")

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state is SmartCaptureState.Review)
            assertEquals(46.0f, (state as SmartCaptureState.Review).bottle.abv)
        }
    }

    @Test
    fun `updateField updates age with int parsing`() = runTest {
        // Given
        viewModel = createViewModel()
        val testUri = mockk<Uri>(relaxed = true)
        viewModel.onImageCaptured(testUri)

        val enrichedBottle = EnrichedBottle(name = "Test", age = null, source = DataSource.AI_ONLY)

        every { recognitionProvider.getService() } returns mockRecognitionService
        coEvery { mockRecognitionService.recognizeBottle(any()) } returns AiRecognitionResult.Success(RecognizedBottle())
        coEvery { enrichmentRepository.enrich(any()) } returns enrichedBottle

        viewModel.startRecognition()

        // When
        viewModel.updateField("age", "12")

        // Then
        viewModel.state.test {
            val state = awaitItem()
            assertTrue(state is SmartCaptureState.Review)
            assertEquals(12, (state as SmartCaptureState.Review).bottle.age)
        }
    }

    // === GET BOTTLE DATA TESTS ===

    @Test
    fun `getBottleData returns null when not in Review state`() = runTest {
        // Given
        viewModel = createViewModel()

        // When/Then
        assertNull(viewModel.getBottleData())
    }

    @Test
    fun `getBottleData returns bottle when in Review state`() = runTest {
        // Given
        viewModel = createViewModel()
        val testUri = mockk<Uri>(relaxed = true)
        viewModel.onImageCaptured(testUri)

        val enrichedBottle = EnrichedBottle(name = "Test Bottle", source = DataSource.AI_ONLY)

        every { recognitionProvider.getService() } returns mockRecognitionService
        coEvery { mockRecognitionService.recognizeBottle(any()) } returns AiRecognitionResult.Success(RecognizedBottle())
        coEvery { enrichmentRepository.enrich(any()) } returns enrichedBottle

        viewModel.startRecognition()

        // When
        val result = viewModel.getBottleData()

        // Then
        assertNotNull(result)
        assertEquals("Test Bottle", result?.name)
    }
}
