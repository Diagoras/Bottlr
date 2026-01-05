package com.bottlr.app.ui.camera

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

/**
 * Handles camera permission request with appropriate UI states.
 *
 * @param onPermissionGranted Content to show when permission is granted
 * @param onNavigateToSettings Called when user needs to go to settings
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPermissionHandler(
    modifier: Modifier = Modifier,
    onPermissionGranted: @Composable () -> Unit,
    onNavigateToSettings: (() -> Unit)? = null
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    when {
        cameraPermissionState.status.isGranted -> {
            // Permission granted - show camera
            onPermissionGranted()
        }
        cameraPermissionState.status.shouldShowRationale -> {
            // Show rationale
            PermissionRationale(
                modifier = modifier,
                onRequestPermission = { cameraPermissionState.launchPermissionRequest() }
            )
        }
        else -> {
            // First time or permanently denied
            PermissionRequest(
                modifier = modifier,
                onRequestPermission = { cameraPermissionState.launchPermissionRequest() },
                showSettingsOption = onNavigateToSettings != null,
                onNavigateToSettings = onNavigateToSettings ?: {}
            )
        }
    }

    // Auto-request on first composition if not granted
    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
}

@Composable
private fun PermissionRequest(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit,
    showSettingsOption: Boolean,
    onNavigateToSettings: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Camera Access Required",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "To scan bottle labels, Bottlr needs access to your camera.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        Button(onClick = onRequestPermission) {
            Text("Grant Camera Access")
        }

        if (showSettingsOption) {
            Spacer(Modifier.height(16.dp))

            androidx.compose.material3.TextButton(onClick = onNavigateToSettings) {
                Text("Open Settings")
            }
        }
    }
}

@Composable
private fun PermissionRationale(
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Camera Permission Needed",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Smart Add uses your camera to photograph bottles and automatically identify them. " +
                    "Your photos are processed locally or with your configured AI service.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(24.dp))

        Button(onClick = onRequestPermission) {
            Text("Allow Camera")
        }
    }
}
