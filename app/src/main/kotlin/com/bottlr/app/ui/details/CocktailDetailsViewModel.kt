package com.bottlr.app.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bottlr.app.data.local.entities.CocktailEntity
import com.bottlr.app.data.repository.CocktailRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CocktailDetailsViewModel @Inject constructor(
    private val cocktailRepository: CocktailRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val cocktailId: Long = savedStateHandle.get<Long>("cocktailId") ?: -1L

    val cocktail: StateFlow<CocktailEntity?> = cocktailRepository.getCocktailById(cocktailId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _deleteStatus = MutableStateFlow<DeleteStatus>(DeleteStatus.Idle)
    val deleteStatus: StateFlow<DeleteStatus> = _deleteStatus.asStateFlow()

    fun deleteCocktail() {
        viewModelScope.launch {
            try {
                _deleteStatus.value = DeleteStatus.Deleting
                cocktail.value?.let {
                    cocktailRepository.delete(it)
                    _deleteStatus.value = DeleteStatus.Success
                }
            } catch (e: Exception) {
                _deleteStatus.value = DeleteStatus.Error(e.message ?: "Failed to delete")
            }
        }
    }
}
