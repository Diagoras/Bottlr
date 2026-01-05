package com.bottlr.app.ui.settings

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.bottlr.app.MainActivity
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.bottlr.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToApiKeys: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val auth = remember { FirebaseAuth.getInstance() }
    val credentialManager = remember { CredentialManager.create(context) }

    var currentUser by remember { mutableStateOf(auth.currentUser) }
    var isSigningIn by remember { mutableStateOf(false) }

    val prefs = remember {
        context.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
    }
    var selectedTheme by remember {
        mutableIntStateOf(prefs.getInt(MainActivity.KEY_THEME_MODE, MainActivity.THEME_SYSTEM))
    }

    fun signIn() {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.bottlr_web_client_id))
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            isSigningIn = true
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                when (val credential = result.credential) {
                    is CustomCredential -> {
                        if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)
                            auth.signInWithCredential(firebaseCredential).await()
                            currentUser = auth.currentUser
                            snackbarHostState.showSnackbar("Signed in successfully")
                        }
                    }
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Sign-in failed: ${e.message}")
            } finally {
                isSigningIn = false
            }
        }
    }

    fun signOut() {
        auth.signOut()
        currentUser = null
        scope.launch {
            snackbarHostState.showSnackbar("Signed out")
        }
    }

    fun setTheme(theme: Int) {
        selectedTheme = theme
        prefs.edit().putInt(MainActivity.KEY_THEME_MODE, theme).apply()
        val nightMode = when (theme) {
            MainActivity.THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            MainActivity.THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
            else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Account section
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
                        text = "Account",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = currentUser?.email ?: "Not signed in",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(Modifier.height(16.dp))

                    if (currentUser == null) {
                        Button(
                            onClick = { signIn() },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isSigningIn
                        ) {
                            Text(if (isSigningIn) "Signing in..." else "Sign in with Google")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { signOut() },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Sign Out")
                        }
                    }
                }
            }

            // Theme section
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
                        text = "Theme",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(12.dp))

                    Column(Modifier.selectableGroup()) {
                        ThemeOption(
                            text = "System default",
                            selected = selectedTheme == MainActivity.THEME_SYSTEM,
                            onClick = { setTheme(MainActivity.THEME_SYSTEM) }
                        )
                        ThemeOption(
                            text = "Light",
                            selected = selectedTheme == MainActivity.THEME_LIGHT,
                            onClick = { setTheme(MainActivity.THEME_LIGHT) }
                        )
                        ThemeOption(
                            text = "Dark",
                            selected = selectedTheme == MainActivity.THEME_DARK,
                            onClick = { setTheme(MainActivity.THEME_DARK) }
                        )
                    }
                }
            }

            // AI API Keys section
            Card(
                onClick = onNavigateToApiKeys,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "AI API Keys",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Configure keys for Smart Bottle Recognition",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Go to API Keys",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Cloud sync info
            if (currentUser != null) {
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
                            text = "Cloud Sync",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Your bottles and cocktails are automatically synced to the cloud when you're signed in.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
