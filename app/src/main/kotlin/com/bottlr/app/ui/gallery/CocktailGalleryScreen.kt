package com.bottlr.app.ui.gallery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bottlr.app.data.local.entities.CocktailEntity
import com.bottlr.app.ui.components.BottlrScaffold
import com.bottlr.app.ui.components.BottlrTopBar
import com.bottlr.app.ui.components.EmptyState
import com.bottlr.app.ui.components.NavDestination
import kotlinx.coroutines.launch

@Composable
fun CocktailGalleryScreen(
    drawerState: DrawerState,
    onNavigate: (NavDestination) -> Unit,
    onCocktailClick: (Long) -> Unit,
    onAddCocktail: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val cocktails by viewModel.cocktails.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    BottlrScaffold(
        currentDestination = NavDestination.Cocktails,
        onNavigate = onNavigate,
        drawerState = drawerState,
        topBar = {
            BottlrTopBar(
                title = "Cocktail Menu",
                onMenuClick = { scope.launch { drawerState.open() } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddCocktail,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Cocktail")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.setSearchQuery(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search cocktails...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (cocktails.isEmpty()) {
                EmptyState(
                    message = if (searchQuery.isBlank())
                        "No cocktails yet.\nTap + to add your first cocktail!"
                    else
                        "No cocktails match your search."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cocktails, key = { it.id }) { cocktail ->
                        CocktailListItem(
                            cocktail = cocktail,
                            onClick = { onCocktailClick(cocktail.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CocktailListItem(
    cocktail: CocktailEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cocktail image
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (cocktail.photoUri != null) {
                    AsyncImage(
                        model = cocktail.photoUri,
                        contentDescription = cocktail.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cocktail.name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // Cocktail info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cocktail.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (cocktail.base.isNotBlank()) {
                    Text(
                        text = cocktail.base,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Show ingredients summary
                val ingredients = listOfNotNull(
                    cocktail.mixer.takeIf { it.isNotBlank() },
                    cocktail.juice.takeIf { it.isNotBlank() },
                    cocktail.liqueur.takeIf { it.isNotBlank() }
                ).take(2)

                if (ingredients.isNotEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = ingredients.joinToString(" + "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Rating badge
            cocktail.rating?.let { rating ->
                Text(
                    text = "★ ${"%.1f".format(rating)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
