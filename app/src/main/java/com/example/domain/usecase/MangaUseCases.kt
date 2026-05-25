package com.example.domain.usecase

import com.example.domain.repository.MangaRepository

class GetSourcesUseCase(private val repository: MangaRepository) {
    operator fun invoke() = repository.getAllSources()
}

class FetchDefaultSourcesUseCase(private val repository: MangaRepository) {
    suspend operator fun invoke() = repository.fetchDefaultSources()
}

class AddCustomSourceUseCase(private val repository: MangaRepository) {
    suspend operator fun invoke(url: String) = repository.addCustomSource(url)
}

class FetchMangaDetailsUseCase(private val repository: MangaRepository) {
    suspend operator fun invoke(sourceId: String, url: String) = repository.getMangaDetails(sourceId, url)
}
