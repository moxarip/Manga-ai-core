package com.example.data.repository

import com.example.data.local.MangaDao
import com.example.data.local.entity.toDomain
import com.example.data.local.entity.toEntity
import com.example.data.remote.GithubConfigService
import com.example.data.remote.MangaScraperService
import com.example.domain.model.Manga
import com.example.domain.model.MangaSource
import com.example.domain.repository.MangaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MangaRepositoryImpl(
    private val mangaDao: MangaDao,
    private val githubService: GithubConfigService,
    private val scraperService: MangaScraperService
) : MangaRepository {

    override fun getAllSources(): Flow<List<MangaSource>> {
        return mangaDao.getAllSources().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getSources(): List<MangaSource> {
        return mangaDao.getSourcesSync().map { it.toDomain() }
    }

    override suspend fun fetchDefaultSources() {
        try {
            val remoteSources = githubService.getDefaultSources()
            val existingSources = mangaDao.getSourcesSync()
            
            // Upsert only taking remote items and keep custom ones
            val mapped = remoteSources.map { it.toEntity() }
            mangaDao.insertSources(mapped)
        } catch (e: Exception) {
            // Mock Fallback if gist fails or it's not setup yet
            val fallback = listOf(
                MangaSource("https://mangadex.org", "MangaDex", "https://mangadex.org"),
                MangaSource("https://mangareader.to", "MangaReader", "https://mangareader.to")
            ).map { it.toEntity() }
            mangaDao.insertSources(fallback)
        }
    }

    override suspend fun addCustomSource(url: String): Result<MangaSource> {
        return try {
            val source = scraperService.extractSourceInfo(url)
            mangaDao.insertSource(source.toEntity())
            Result.success(source)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMangaDetails(sourceId: String, mangaUrl: String): Result<Manga> {
        return try {
            val manga = scraperService.getMangaData(mangaUrl, sourceId)
            mangaDao.insertMangaDetails(manga.toEntity())
            Result.success(manga)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
