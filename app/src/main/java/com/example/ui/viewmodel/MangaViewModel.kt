package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.Manga
import com.example.domain.usecase.FetchMangaDetailsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class MangaUiState {
    object Idle : MangaUiState()
    object Loading : MangaUiState()
    data class Success(val manga: Manga) : MangaUiState()
    data class Error(val message: String) : MangaUiState()
}

class MangaViewModel(
    private val fetchMangaDetailsUseCase: FetchMangaDetailsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<MangaUiState>(MangaUiState.Idle)
    val uiState: StateFlow<MangaUiState> = _uiState

    fun fetchManga(sourceId: String, url: String) {
        _uiState.value = MangaUiState.Loading
        viewModelScope.launch {
            val result = fetchMangaDetailsUseCase(sourceId, url)
            if (result.isSuccess) {
                _uiState.value = MangaUiState.Success(result.getOrThrow())
            } else {
                _uiState.value = MangaUiState.Error(result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}
