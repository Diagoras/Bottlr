package com.bottlr.app.ui.camera

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * CameraX preview composable with capture support.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onImageCaptured: (Uri) -> Unit,
    onError: (String) -> Unit,
    captureController: CaptureController = rememberCaptureController()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    // Initialize camera provider
    LaunchedEffect(Unit) {
        try {
            cameraProvider = context.getCameraProvider()
        } catch (e: Exception) {
            onError("Failed to initialize camera: ${e.message}")
        }
    }

    // Set up capture action
    LaunchedEffect(captureController, imageCapture) {
        captureController.captureAction = {
            val capture = imageCapture
            if (capture != null) {
                try {
                    val uri = capture.takePicture(context)
                    onImageCaptured(uri)
                } catch (e: Exception) {
                    onError("Failed to capture image: ${e.message}")
                }
            }
        }
    }

    // Clean up on dispose
    DisposableEffect(Unit) {
        onDispose {
            cameraProvider?.unbindAll()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        cameraProvider?.let { provider ->
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { previewView ->
                    bindCameraUseCases(
                        context = context,
                        lifecycleOwner = lifecycleOwner,
                        cameraProvider = provider,
                        previewView = previewView,
                        onImageCaptureReady = { imageCapture = it }
                    )
                }
            )
        }
    }
}

/**
 * Controller for triggering image capture from outside the composable.
 */
class CaptureController {
    internal var captureAction: (suspend () -> Unit)? = null

    suspend fun capture() {
        captureAction?.invoke()
    }
}

@Composable
fun rememberCaptureController(): CaptureController {
    return remember { CaptureController() }
}

private fun bindCameraUseCases(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    cameraProvider: ProcessCameraProvider,
    previewView: PreviewView,
    onImageCaptureReady: (ImageCapture) -> Unit
) {
    // Unbind any existing use cases
    cameraProvider.unbindAll()

    // Build preview use case
    val preview = Preview.Builder()
        .build()
        .also { it.surfaceProvider = previewView.surfaceProvider }

    // Build image capture use case
    val imageCapture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()

    // Select back camera
    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    try {
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageCapture
        )
        onImageCaptureReady(imageCapture)
    } catch (e: Exception) {
        // Handle camera binding failure
    }
}

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { future ->
            future.addListener({
                try {
                    continuation.resume(future.get())
                } catch (e: Exception) {
                    continuation.resumeWithException(e)
                }
            }, ContextCompat.getMainExecutor(this))
        }
    }

private suspend fun ImageCapture.takePicture(context: Context): Uri =
    suspendCoroutine { continuation ->
        val executor: Executor = ContextCompat.getMainExecutor(context)

        // Create output file
        val photoDir = File(context.cacheDir, "photos")
        photoDir.mkdirs()
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val photoFile = File(photoDir, "IMG_$timestamp.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    continuation.resume(Uri.fromFile(photoFile))
                }

                override fun onError(exception: ImageCaptureException) {
                    continuation.resumeWithException(exception)
                }
            }
        )
    }
