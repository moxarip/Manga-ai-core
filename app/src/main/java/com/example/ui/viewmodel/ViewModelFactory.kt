package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.MangaApplication

object ViewModelFactory {
    val Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
            val app = checkNotNull(extras[APPLICATION_KEY]) as MangaApplication
            val container = app.container

            return when {
                modelClass.isAssignableFrom(SourcesViewModel::class.java) -> {
                    SourcesViewModel(container.getSourcesUseCase, container.fetchDefaultSourcesUseCase, container.addCustomSourceUseCase) as T
                }
                modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                    SettingsViewModel(container.securityManager) as T
                }
                modelClass.isAssignableFrom(MangaViewModel::class.java) -> {
                    MangaViewModel(container.fetchMangaDetailsUseCase) as T
                }
                else -> throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
