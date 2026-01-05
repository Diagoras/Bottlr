package com.bottlr.app.ui.smartcapture

import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.bottlr.app.data.model.ConfidenceLevel
import com.bottlr.app.data.model.DataSource
import com.bottlr.app.data.model.EnrichedBottle

/**
 * Screen for reviewing and editing AI-recognized bottle data before saving.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaptureReviewScreen(
    photoUri: Uri,
    onNavigateBack: () -> Unit,
    onConfirm: (EnrichedBottle, Uri) -> Unit,
    onRetake: () -> Unit,
    viewModel: SmartCaptureViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val reviewState = state as? SmartCaptureState.Review

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Photo preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model = photoUri,
                    contentDescription = "Captured bottle photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Source indicator
            reviewState?.bottle?.let { bottle ->
                SourceIndicator(source = bottle.source)
            }

            // Editable fields
            reviewState?.bottle?.let { bottle ->
                FieldWithConfidence(
                    label = "Name",
                    value = bottle.name ?: "",
                    confidence = bottle.fieldMetadata["name"]?.confidence,
                    source = bottle.fieldMetadata["name"]?.source,
                    onValueChange = { viewModel.updateField("name", it) },
                    isRequired = true
                )

                FieldWithConfidence(
                    label = "Distillery",
                    value = bottle.distillery ?: "",
                    confidence = bottle.fieldMetadata["distillery"]?.confidence,
                    source = bottle.fieldMetadata["distillery"]?.source,
                    onValueChange = { viewModel.updateField("distillery", it) }
                )

                FieldWithConfidence(
                    label = "Type",
                    value = bottle.type ?: "",
                    confidence = bottle.fieldMetadata["type"]?.confidence,
                    source = bottle.fieldMetadata["type"]?.source,
                    hint = "e.g., Bourbon, Scotch, Rye",
                    onValueChange = { viewModel.updateField("type", it) }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FieldWithConfidence(
                        label = "ABV %",
                        value = bottle.abv?.toString() ?: "",
                        confidence = bottle.fieldMetadata["abv"]?.confidence,
                        source = bottle.fieldMetadata["abv"]?.source,
                        onValueChange = { viewModel.updateField("abv", it) },
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )

                    FieldWithConfidence(
                        label = "Age",
                        value = bottle.age?.toString() ?: "",
                        confidence = bottle.fieldMetadata["age"]?.confidence,
                        source = bottle.fieldMetadata["age"]?.source,
                        hint = "Years",
                        onValueChange = { viewModel.updateField("age", it) },
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }

                FieldWithConfidence(
                    label = "Region",
                    value = bottle.region ?: "",
                    confidence = bottle.fieldMetadata["region"]?.confidence,
                    source = bottle.fieldMetadata["region"]?.source,
                    onValueChange = { viewModel.updateField("region", it) }
                )

                OutlinedTextField(
                    value = bottle.notes ?: "",
                    onValueChange = { viewModel.updateField("notes", it) },
                    label = { Text("Tasting Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                OutlinedTextField(
                    value = bottle.keywords ?: "",
                    onValueChange = { viewModel.updateField("keywords", it) },
                    label = { Text("Keywords") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("e.g., peaty, smoky, smooth") }
                )
            }

            Spacer(Modifier.height(8.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextButton(
                    onClick = onRetake,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Retake Photo")
                }

                Button(
                    onClick = {
                        reviewState?.let {
                            onConfirm(it.bottle, it.photoUri)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !reviewState?.bottle?.name.isNullOrBlank()
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Use This")
                }
            }
        }
    }
}

@Composable
private fun SourceIndicator(source: DataSource) {
    val (text, color) = when (source) {
        DataSource.AI_ONLY -> "Identified by AI" to MaterialTheme.colorScheme.tertiary
        DataSource.DATABASE_ONLY -> "Found in database" to MaterialTheme.colorScheme.primary
        DataSource.AI_AND_DATABASE -> "AI + Database verified" to MaterialTheme.colorScheme.primary
        DataSource.USER_ENTERED -> "User entered" to MaterialTheme.colorScheme.secondary
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = color
            )
        }
    }
}

@Composable
private fun FieldWithConfidence(
    label: String,
    value: String,
    confidence: ConfidenceLevel?,
    source: DataSource?,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    isRequired: Boolean = false
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isRequired) "$label *" else label)
                    confidence?.let { conf ->
                        Spacer(Modifier.width(8.dp))
                        ConfidenceIndicator(confidence = conf)
                    }
                }
            },
            placeholder = hint?.let { { Text(it) } },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true
        )
    }
}

@Composable
private fun ConfidenceIndicator(confidence: ConfidenceLevel) {
    val (icon, color, description) = when (confidence) {
        ConfidenceLevel.HIGH -> Triple(Icons.Default.Check, Color(0xFF4CAF50), "High confidence")
        ConfidenceLevel.MEDIUM -> Triple(Icons.Default.Info, Color(0xFFFFC107), "Medium confidence")
        ConfidenceLevel.LOW -> Triple(Icons.Default.Warning, Color(0xFFFF9800), "Low confidence")
        ConfidenceLevel.UNKNOWN -> Triple(Icons.Default.Info, Color.Gray, "Unknown confidence")
    }

    Icon(
        imageVector = icon,
        contentDescription = description,
        tint = color,
        modifier = Modifier.size(16.dp)
    )
}
