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
import com.bottlr.app.data.local.entities.BottleEntity
import com.bottlr.app.ui.components.BottlrScaffold
import com.bottlr.app.ui.components.BottlrTopBar
import com.bottlr.app.ui.components.EmptyState
import com.bottlr.app.ui.components.NavDestination
import kotlinx.coroutines.launch

@Composable
fun BottleGalleryScreen(
    drawerState: DrawerState,
    onNavigate: (NavDestination) -> Unit,
    onBottleClick: (Long) -> Unit,
    onAddBottle: () -> Unit,
    viewModel: GalleryViewModel = hiltViewModel()
) {
    val bottles by viewModel.bottles.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    BottlrScaffold(
        currentDestination = NavDestination.LiquorCabinet,
        onNavigate = onNavigate,
        drawerState = drawerState,
        topBar = {
            BottlrTopBar(
                title = "Liquor Cabinet",
                onMenuClick = { scope.launch { drawerState.open() } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBottle,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Bottle")
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
                placeholder = { Text("Search bottles...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (bottles.isEmpty()) {
                EmptyState(
                    message = if (searchQuery.isBlank())
                        "No bottles yet.\nTap + to add your first bottle!"
                    else
                        "No bottles match your search."
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(bottles, key = { it.id }) { bottle ->
                        BottleListItem(
                            bottle = bottle,
                            onClick = { onBottleClick(bottle.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottleListItem(
    bottle: BottleEntity,
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
            // Bottle image
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (bottle.photoUri != null) {
                    AsyncImage(
                        model = bottle.photoUri,
                        contentDescription = bottle.name,
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
                            text = bottle.name.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // Bottle info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bottle.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (bottle.distillery.isNotBlank()) {
                    Text(
                        text = bottle.distillery,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (bottle.type.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = bottle.type,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // ABV/Age badge
            if (bottle.abv != null || bottle.age != null) {
                Column(horizontalAlignment = Alignment.End) {
                    bottle.abv?.let {
                        Text(
                            text = "${it}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    bottle.age?.let {
                        Text(
                            text = "${it}yr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
