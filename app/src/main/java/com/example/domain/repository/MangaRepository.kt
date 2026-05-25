package com.example.domain.repository

import com.example.domain.model.Manga
import com.example.domain.model.MangaSource
import kotlinx.coroutines.flow.Flow

interface MangaRepository {
    fun getAllSources(): Flow<List<MangaSource>>
    suspend fun getSources(): List<MangaSource>
    suspend fun addCustomSource(url: String): Result<MangaSource>
    suspend fun fetchDefaultSources()
    
    suspend fun getMangaDetails(sourceId: String, mangaUrl: String): Result<Manga>
}
