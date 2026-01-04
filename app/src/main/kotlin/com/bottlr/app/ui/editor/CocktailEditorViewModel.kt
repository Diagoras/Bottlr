package com.bottlr.app.ui.editor

import android.net.Uri
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
class CocktailEditorViewModel @Inject constructor(
    private val cocktailRepository: CocktailRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val cocktailId: Long = savedStateHandle.get<Long>("cocktailId") ?: -1L

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    private val _cocktail = MutableStateFlow<CocktailEntity?>(null)
    val cocktail: StateFlow<CocktailEntity?> = _cocktail.asStateFlow()

    private val _saveStatus = MutableStateFlow<SaveStatus>(SaveStatus.Idle)
    val saveStatus: StateFlow<SaveStatus> = _saveStatus.asStateFlow()

    private val _deleteStatus = MutableStateFlow<DeleteStatus>(DeleteStatus.Idle)
    val deleteStatus: StateFlow<DeleteStatus> = _deleteStatus.asStateFlow()

    val isEditMode: Boolean = cocktailId != -1L

    init {
        if (isEditMode) {
            viewModelScope.launch {
                cocktailRepository.getCocktailById(cocktailId).collectLatest { cocktailEntity ->
                    _cocktail.value = cocktailEntity
                    cocktailEntity?.photoUri?.let { uri ->
                        _photoUri.value = Uri.parse(uri)
                    }
                }
            }
        }
    }

    fun setPhotoUri(uri: Uri) {
        _photoUri.value = uri
    }

    fun saveCocktail(
        name: String,
        base: String,
        mixer: String,
        juice: String,
        liqueur: String,
        garnish: String,
        extra: String,
        notes: String,
        keywords: String,
        rating: Float?
    ) {
        viewModelScope.launch {
            try {
                _saveStatus.value = SaveStatus.Saving

                val cocktail = CocktailEntity(
                    id = if (isEditMode) cocktailId else 0,
                    name = name,
                    base = base,
                    mixer = mixer,
                    juice = juice,
                    liqueur = liqueur,
                    garnish = garnish,
                    extra = extra,
                    photoUri = _photoUri.value?.toString(),
                    notes = notes,
                    keywords = keywords,
                    rating = rating
                )

                val id = if (isEditMode) {
                    cocktailRepository.update(cocktail)
                    cocktailId
                } else {
                    cocktailRepository.insert(cocktail)
                }

                _saveStatus.value = SaveStatus.Success

                try {
                    cocktailRepository.syncToFirestore(id)
                } catch (e: Exception) {
                    // Sync failure shouldn't block local save
                }
            } catch (e: Exception) {
                _saveStatus.value = SaveStatus.Error(e.message ?: "Failed to save cocktail")
            }
        }
    }

    fun deleteCocktail() {
        viewModelScope.launch {
            try {
                _deleteStatus.value = DeleteStatus.Deleting
                _cocktail.value?.let {
                    cocktailRepository.delete(it)
                    _deleteStatus.value = DeleteStatus.Success
                }
            } catch (e: Exception) {
                _deleteStatus.value = DeleteStatus.Error(e.message ?: "Failed to delete")
            }
        }
    }
}
