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
    private val bottleRepository: BottleRepository,
    private val cocktailRepository: CocktailRepository
) : ViewModel() {

    // State: Are we viewing bottles (true) or cocktails (false)?
    private val _isDrinkMode = MutableStateFlow(true)
    val isDrinkMode: StateFlow<Boolean> = _isDrinkMode.asStateFlow()

    // Bottles from Room database (reactive with Flow)
    val bottles: StateFlow<List<BottleEntity>> = bottleRepository.allBottles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Cocktails from Room database
    val cocktails: StateFlow<List<CocktailEntity>> = cocktailRepository.allCocktails
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun setDrinkMode(isBottle: Boolean) {
        _isDrinkMode.value = isBottle
    }
}
