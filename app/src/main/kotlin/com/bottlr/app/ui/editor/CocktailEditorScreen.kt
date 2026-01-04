package com.bottlr.app.ui.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocktailEditorScreen(
    onNavigateBack: () -> Unit,
    viewModel: CocktailEditorViewModel = hiltViewModel()
) {
    val cocktail by viewModel.cocktail.collectAsStateWithLifecycle()
    val photoUri by viewModel.photoUri.collectAsStateWithLifecycle()
    val saveStatus by viewModel.saveStatus.collectAsStateWithLifecycle()
    val deleteStatus by viewModel.deleteStatus.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var base by remember { mutableStateOf("") }
    var mixer by remember { mutableStateOf("") }
    var juice by remember { mutableStateOf("") }
    var liqueur by remember { mutableStateOf("") }
    var garnish by remember { mutableStateOf("") }
    var extra by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var keywords by remember { mutableStateOf("") }
    var rating by remember { mutableFloatStateOf(0f) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Populate fields when editing
    LaunchedEffect(cocktail) {
        cocktail?.let {
            name = it.name
            base = it.base
            mixer = it.mixer
            juice = it.juice
            liqueur = it.liqueur
            garnish = it.garnish
            extra = it.extra
            notes = it.notes
            keywords = it.keywords
            rating = it.rating ?: 0f
        }
    }

    // Handle save/delete success
    LaunchedEffect(saveStatus, deleteStatus) {
        if (saveStatus is SaveStatus.Success || deleteStatus is DeleteStatus.Success) {
            onNavigateBack()
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { viewModel.setPhotoUri(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (viewModel.isEditMode) "Edit Cocktail" else "New Cocktail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (viewModel.isEditMode) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
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
            // Photo picker
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                if (photoUri != null) {
                    AsyncImage(
                        model = photoUri,
                        contentDescription = "Cocktail photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.AddAPhoto,
                            contentDescription = "Add photo",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tap to add photo",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Form fields
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Cocktail Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = base,
                onValueChange = { base = it },
                label = { Text("Base Spirit (e.g., Vodka, Rum)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = mixer,
                onValueChange = { mixer = it },
                label = { Text("Mixer (e.g., Tonic, Cola)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = juice,
                onValueChange = { juice = it },
                label = { Text("Juice (e.g., Lime, Orange)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = liqueur,
                onValueChange = { liqueur = it },
                label = { Text("Liqueur (e.g., Triple Sec)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = garnish,
                onValueChange = { garnish = it },
                label = { Text("Garnish") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = extra,
                onValueChange = { extra = it },
                label = { Text("Extra Ingredients") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes / Instructions") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5
            )

            OutlinedTextField(
                value = keywords,
                onValueChange = { keywords = it },
                label = { Text("Keywords") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Rating slider
            Column {
                Text(
                    text = "Rating: ${"%.1f".format(rating)}",
                    style = MaterialTheme.typography.labelLarge
                )
                Slider(
                    value = rating,
                    onValueChange = { rating = it },
                    valueRange = 0f..5f,
                    steps = 9
                )
            }

            Spacer(Modifier.height(8.dp))

            // Save button
            Button(
                onClick = {
                    viewModel.saveCocktail(
                        name = name,
                        base = base,
                        mixer = mixer,
                        juice = juice,
                        liqueur = liqueur,
                        garnish = garnish,
                        extra = extra,
                        notes = notes,
                        keywords = keywords,
                        rating = if (rating > 0) rating else null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() && saveStatus !is SaveStatus.Saving
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(if (saveStatus is SaveStatus.Saving) "Saving..." else "Save Cocktail")
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Cocktail") },
            text = { Text("Are you sure you want to delete this cocktail?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteCocktail()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
