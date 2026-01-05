package com.bottlr.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.Liquor
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bottlr.app.ui.components.BottlrScaffold
import com.bottlr.app.ui.components.BottlrTopBar
import com.bottlr.app.ui.components.NavDestination
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    drawerState: DrawerState,
    onNavigate: (NavDestination) -> Unit,
    onNavigateToBottleGallery: () -> Unit,
    onNavigateToCocktailGallery: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onAddBottle: () -> Unit,
    onAddCocktail: () -> Unit,
    onSmartAdd: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val bottleCount by viewModel.bottleCount.collectAsStateWithLifecycle()
    val cocktailCount by viewModel.cocktailCount.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    BottlrScaffold(
        currentDestination = NavDestination.Home,
        onNavigate = onNavigate,
        drawerState = drawerState,
        topBar = {
            BottlrTopBar(
                title = "Bottlr",
                onMenuClick = { scope.launch { drawerState.open() } },
                onAccountClick = onNavigateToSettings
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatsCard(
                    title = "Liquor Cabinet",
                    count = bottleCount,
                    unit = if (bottleCount == 1) "bottle" else "bottles",
                    icon = Icons.Default.Liquor,
                    onClick = onNavigateToBottleGallery,
                    modifier = Modifier.weight(1f)
                )
                StatsCard(
                    title = "Cocktail Menu",
                    count = cocktailCount,
                    unit = if (cocktailCount == 1) "cocktail" else "cocktails",
                    icon = Icons.Default.LocalBar,
                    onClick = onNavigateToCocktailGallery,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Quick Actions
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onAddBottle,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Add Bottle")
                }

                Button(
                    onClick = onAddCocktail,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Add Cocktail")
                }
            }

            // Smart Add Button
            Button(
                onClick = onSmartAdd,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Smart Add (Camera)")
            }
        }
    }
}

@Composable
private fun StatsCard(
    title: String,
    count: Int,
    unit: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$count $unit",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
