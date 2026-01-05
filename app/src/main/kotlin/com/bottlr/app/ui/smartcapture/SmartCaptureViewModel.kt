package com.bottlr.app.ui.smartcapture

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bottlr.app.ai.AiRecognitionResult
import com.bottlr.app.ai.AvailabilityStatus
import com.bottlr.app.ai.RecognitionServiceProvider
import com.bottlr.app.data.model.EnrichedBottle
import com.bottlr.app.data.repository.EnrichmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Smart Capture flow.
 * Manages the state machine for: Camera -> Capture -> Recognize -> Enrich -> Review
 */
@HiltViewModel
class SmartCaptureViewModel @Inject constructor(
    private val recognitionProvider: RecognitionServiceProvider,
    private val enrichmentRepository: EnrichmentRepository
) : ViewModel() {

    private val _state = MutableStateFlow<SmartCaptureState>(SmartCaptureState.Camera)
    val state: StateFlow<SmartCaptureState> = _state.asStateFlow()

    private val _capturedPhotoUri = MutableStateFlow<Uri?>(null)
    val capturedPhotoUri: StateFlow<Uri?> = _capturedPhotoUri.asStateFlow()

    /**
     * Get the current AI availability status.
     */
    fun getAvailabilityStatus(): AvailabilityStatus {
        return recognitionProvider.getAvailabilityStatus()
    }

    /**
     * Called when an image is captured.
     */
    fun onImageCaptured(uri: Uri) {
        _capturedPhotoUri.value = uri
        _state.value = SmartCaptureState.Captured(uri)
    }

    /**
     * Start the recognition process for the captured image.
     */
    fun startRecognition() {
        val uri = _capturedPhotoUri.value ?: return

        viewModelScope.launch {
            _state.value = SmartCaptureState.Recognizing

            val service = recognitionProvider.getService()
            if (service == null) {
                _state.value = SmartCaptureState.Error(
                    message = "No AI service available. Please configure an API key in Settings.",
                    canRetry = false,
                    canConfigureApiKey = true
                )
                return@launch
            }

            when (val result = service.recognizeBottle(uri)) {
                is AiRecognitionResult.Success -> {
                    // Move to enrichment phase
                    _state.value = SmartCaptureState.Enriching
                    val enriched = enrichmentRepository.enrich(result.bottle)
                    _state.value = SmartCaptureState.Review(enriched, uri)
                }

                is AiRecognitionResult.Error -> {
                    _state.value = SmartCaptureState.Error(
                        message = result.message,
                        canRetry = result.isRecoverable,
                        canConfigureApiKey = false
                    )
                }

                AiRecognitionResult.ServiceUnavailable -> {
                    _state.value = SmartCaptureState.Error(
                        message = "AI service is not available on this device.",
                        canRetry = false,
                        canConfigureApiKey = true
                    )
                }
            }
        }
    }

    /**
     * Retry recognition with the same image.
     */
    fun retry() {
        val uri = _capturedPhotoUri.value
        if (uri != null) {
            _state.value = SmartCaptureState.Captured(uri)
            startRecognition()
        } else {
            resetToCamera()
        }
    }

    /**
     * Go back to camera to retake photo.
     */
    fun resetToCamera() {
        _capturedPhotoUri.value = null
        _state.value = SmartCaptureState.Camera
    }

    /**
     * Update an enriched bottle field during review.
     */
    fun updateField(field: String, value: String?) {
        val currentState = _state.value
        if (currentState is SmartCaptureState.Review) {
            val updatedBottle = when (field) {
                "name" -> currentState.bottle.copy(name = value)
                "distillery" -> currentState.bottle.copy(distillery = value)
                "type" -> currentState.bottle.copy(type = value)
                "region" -> currentState.bottle.copy(region = value)
                "abv" -> currentState.bottle.copy(abv = value?.toFloatOrNull())
                "age" -> currentState.bottle.copy(age = value?.toIntOrNull())
                "notes" -> currentState.bottle.copy(notes = value)
                "keywords" -> currentState.bottle.copy(keywords = value)
                else -> currentState.bottle
            }
            _state.value = SmartCaptureState.Review(updatedBottle, currentState.photoUri)
        }
    }

    /**
     * Get the current enriched bottle data for navigation to editor.
     */
    fun getBottleData(): EnrichedBottle? {
        val currentState = _state.value
        return if (currentState is SmartCaptureState.Review) {
            currentState.bottle
        } else {
            null
        }
    }
}

/**
 * State machine for the Smart Capture flow.
 */
sealed class SmartCaptureState {
    /** Camera is active, ready to capture */
    data object Camera : SmartCaptureState()

    /** Photo captured, ready to process */
    data class Captured(val imageUri: Uri) : SmartCaptureState()

    /** AI is analyzing the image */
    data object Recognizing : SmartCaptureState()

    /** Querying databases for additional info */
    data object Enriching : SmartCaptureState()

    /** Review extracted data before saving */
    data class Review(val bottle: EnrichedBottle, val photoUri: Uri) : SmartCaptureState()

    /** Error occurred */
    data class Error(
        val message: String,
        val canRetry: Boolean,
        val canConfigureApiKey: Boolean = false
    ) : SmartCaptureState()
}
