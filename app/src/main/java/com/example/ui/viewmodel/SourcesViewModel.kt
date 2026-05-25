package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.MangaSource
import com.example.domain.usecase.AddCustomSourceUseCase
import com.example.domain.usecase.FetchDefaultSourcesUseCase
import com.example.domain.usecase.GetSourcesUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

sealed class SourcesUiState {
    object Loading : SourcesUiState()
    data class Success(val sources: List<MangaSource>) : SourcesUiState()
    data class Error(val message: String) : SourcesUiState()
}

class SourcesViewModel(
    getSourcesUseCase: GetSourcesUseCase,
    private val fetchDefaultSourcesUseCase: FetchDefaultSourcesUseCase,
    private val addCustomSourceUseCase: AddCustomSourceUseCase
) : ViewModel() {

    val uiState: StateFlow<SourcesUiState> = getSourcesUseCase()
        .map { if (it.isEmpty()) SourcesUiState.Loading else SourcesUiState.Success(it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SourcesUiState.Loading
        )

    private val _addSourceStatus = MutableStateFlow<String?>(null)
    val addSourceStatus: StateFlow<String?> = _addSourceStatus

    init {
        fetchDefaults()
    }

    private fun fetchDefaults() {
        viewModelScope.launch {
            fetchDefaultSourcesUseCase()
        }
    }

    fun addCustomSource(url: String) {
        viewModelScope.launch {
            _addSourceStatus.value = "Loading..."
            val result = addCustomSourceUseCase(url)
            if (result.isSuccess) {
                _addSourceStatus.value = "Success"
            } else {
                _addSourceStatus.value = "Error: ${result.exceptionOrNull()?.message}"
            }
        }
    }
    
    fun clearStatus() {
        _addSourceStatus.value = null
    }
}

