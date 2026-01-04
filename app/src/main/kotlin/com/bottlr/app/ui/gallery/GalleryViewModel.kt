package com.bottlr.app.ui.gallery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bottlr.app.data.local.entities.BottleEntity
import com.bottlr.app.data.local.entities.CocktailEntity
import com.bottlr.app.data.repository.BottleRepository
import com.bottlr.app.data.repository.CocktailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class GalleryViewModel @Inject constructor(
    bottleRepository: BottleRepository,
    cocktailRepository: CocktailRepository
) : ViewModel() {

    // State: Are we viewing bottles (true) or cocktails (false)?
    private val _isDrinkMode = MutableStateFlow(true)
    val isDrinkMode: StateFlow<Boolean> = _isDrinkMode.asStateFlow()

    // Search query for filtering
    private val _searchQuery = MutableStateFlow("")

    // Bottles from Room database (reactive with Flow, sorted newest first, filtered by search)
    val bottles: StateFlow<List<BottleEntity>> = combine(
        bottleRepository.allBottlesNewestFirst,
        _searchQuery
    ) { allBottles, query ->
        if (query.isBlank()) {
            allBottles
        } else {
            allBottles.filter { bottle ->
                bottle.name.contains(query, ignoreCase = true) ||
                bottle.distillery.contains(query, ignoreCase = true) ||
                bottle.type.contains(query, ignoreCase = true) ||
                bottle.keywords.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Cocktails from Room database (sorted newest first, filtered by search)
    val cocktails: StateFlow<List<CocktailEntity>> = combine(
        cocktailRepository.allCocktailsNewestFirst,
        _searchQuery
    ) { allCocktails, query ->
        if (query.isBlank()) {
            allCocktails
        } else {
            allCocktails.filter { cocktail ->
                cocktail.name.contains(query, ignoreCase = true) ||
                cocktail.base.contains(query, ignoreCase = true) ||
                cocktail.keywords.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setDrinkMode(isBottle: Boolean) {
        _isDrinkMode.value = isBottle
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

}
