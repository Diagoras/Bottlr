package com.bottlr.app.ui.smartcapture

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bottlr.app.ui.camera.CameraPermissionHandler
import com.bottlr.app.ui.camera.CameraPreview
import com.bottlr.app.ui.camera.rememberCaptureController
import kotlinx.coroutines.launch

/**
 * Main screen for Smart Capture flow.
 * Handles camera, capture, recognition, and review states.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartCaptureScreen(
    onNavigateBack: () -> Unit,
    onNavigateToReview: (Uri) -> Unit,
    onNavigateToApiKeys: () -> Unit,
    viewModel: SmartCaptureViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Smart Add") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black.copy(alpha = 0.7f),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val currentState = state) {
                is SmartCaptureState.Camera -> {
                    CameraContent(
                        onImageCaptured = { uri ->
                            viewModel.onImageCaptured(uri)
                            viewModel.startRecognition()
                        },
                        onNavigateToSettings = {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", context.packageName, null)
                            }
                            context.startActivity(intent)
                        }
                    )
                }

                is SmartCaptureState.Captured,
                is SmartCaptureState.Recognizing,
                is SmartCaptureState.Enriching -> {
                    ProcessingContent(
                        imageUri = viewModel.capturedPhotoUri.collectAsState().value,
                        isRecognizing = currentState is SmartCaptureState.Recognizing,
                        isEnriching = currentState is SmartCaptureState.Enriching
                    )
                }

                is SmartCaptureState.Review -> {
                    // Navigate to review screen with the photo URI
                    onNavigateToReview(currentState.photoUri)
                }

                is SmartCaptureState.Error -> {
                    ErrorContent(
                        message = currentState.message,
                        canRetry = currentState.canRetry,
                        canConfigureApiKey = currentState.canConfigureApiKey,
                        onRetry = { viewModel.retry() },
                        onRetake = { viewModel.resetToCamera() },
                        onConfigureApiKey = onNavigateToApiKeys
                    )
                }
            }
        }
    }
}

@Composable
private fun CameraContent(
    onImageCaptured: (Uri) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val captureController = rememberCaptureController()
    val scope = rememberCoroutineScope()

    CameraPermissionHandler(
        onPermissionGranted = {
            Box(modifier = Modifier.fillMaxSize()) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onImageCaptured = onImageCaptured,
                    onError = { /* Handle error */ },
                    captureController = captureController
                )

                // Capture button at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                captureController.capture()
                            }
                        },
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        containerColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Take photo",
                            tint = Color.Black,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Hint text
                Text(
                    text = "Point at the bottle label",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            shape = MaterialTheme.shapes.small
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        },
        onNavigateToSettings = onNavigateToSettings
    )
}

@Composable
private fun ProcessingContent(
    imageUri: Uri?,
    isRecognizing: Boolean,
    isEnriching: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Show captured image as background
        imageUri?.let { uri ->
            AsyncImage(
                model = uri,
                contentDescription = "Captured photo",
                modifier = Modifier.fillMaxSize()
            )
        }

        // Overlay with progress
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(48.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = when {
                    isRecognizing -> "Analyzing bottle..."
                    isEnriching -> "Looking up details..."
                    else -> "Processing..."
                },
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    canRetry: Boolean,
    canConfigureApiKey: Boolean,
    onRetry: () -> Unit,
    onRetake: () -> Unit,
    onConfigureApiKey: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Recognition Failed",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (canRetry) {
                Button(onClick = onRetry) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Text("  Retry")
                }
            }

            TextButton(onClick = onRetake) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White)
                Text("  Retake", color = Color.White)
            }
        }

        if (canConfigureApiKey) {
            Spacer(Modifier.height(16.dp))

            Button(onClick = onConfigureApiKey) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Text("  Configure API Key")
            }
        }
    }
}
