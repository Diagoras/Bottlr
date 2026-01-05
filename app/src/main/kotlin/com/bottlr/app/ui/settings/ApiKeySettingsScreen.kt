package com.bottlr.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bottlr.app.data.local.ApiKeyStore
import com.bottlr.app.data.local.ApiProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApiKeySettingsViewModel @Inject constructor(
    private val apiKeyStore: ApiKeyStore
) : ViewModel() {

    private val _keyStates = MutableStateFlow<Map<ApiProvider, String>>(emptyMap())
    val keyStates: StateFlow<Map<ApiProvider, String>> = _keyStates.asStateFlow()

    init {
        loadKeys()
    }

    private fun loadKeys() {
        val keys = ApiProvider.entries.associateWith { provider ->
            apiKeyStore.getApiKey(provider) ?: ""
        }
        _keyStates.value = keys
    }

    fun updateKey(provider: ApiProvider, key: String) {
        _keyStates.value = _keyStates.value + (provider to key)
    }

    fun saveKey(provider: ApiProvider) {
        val key = _keyStates.value[provider] ?: ""
        if (key.isBlank()) {
            apiKeyStore.clearApiKey(provider)
        } else {
            apiKeyStore.setApiKey(provider, key)
        }
    }

    fun clearKey(provider: ApiProvider) {
        apiKeyStore.clearApiKey(provider)
        _keyStates.value = _keyStates.value + (provider to "")
    }

    fun hasKey(provider: ApiProvider): Boolean {
        return apiKeyStore.getApiKey(provider) != null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApiKeySettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: ApiKeySettingsViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI API Keys") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Smart Bottle Recognition",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "On newer devices (Pixel 9+), Smart Add uses free on-device AI. " +
                                "For other devices, add your own API key below to enable AI-powered bottle recognition.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // API key cards for each provider
            ApiProvider.entries.forEach { provider ->
                ApiKeyCard(
                    provider = provider,
                    currentValue = viewModel.keyStates.value[provider] ?: "",
                    isConfigured = viewModel.hasKey(provider),
                    onValueChange = { viewModel.updateKey(provider, it) },
                    onSave = {
                        viewModel.saveKey(provider)
                        scope.launch {
                            snackbarHostState.showSnackbar("${provider.displayName} key saved")
                        }
                    },
                    onClear = {
                        viewModel.clearKey(provider)
                        scope.launch {
                            snackbarHostState.showSnackbar("${provider.displayName} key removed")
                        }
                    }
                )
            }

            // Privacy note
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Privacy",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "API keys are stored securely on your device using encrypted storage. " +
                                "They are never sent to Bottlr servers.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ApiKeyCard(
    provider: ApiProvider,
    currentValue: String,
    isConfigured: Boolean,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = provider.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (isConfigured) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Configured",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Text(
                text = provider.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = currentValue,
                onValueChange = onValueChange,
                label = { Text("API Key") },
                placeholder = { Text(provider.keyPlaceholder) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = if (isVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    Row {
                        IconButton(onClick = { isVisible = !isVisible }) {
                            Icon(
                                imageVector = if (isVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (isVisible) "Hide" else "Show"
                            )
                        }
                        if (currentValue.isNotBlank()) {
                            IconButton(onClick = onClear) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear"
                                )
                            }
                        }
                    }
                }
            )

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                androidx.compose.material3.TextButton(
                    onClick = onSave,
                    enabled = currentValue.isNotBlank()
                ) {
                    Text("Save")
                }
            }
        }
    }
}
