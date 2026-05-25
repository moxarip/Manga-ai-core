package com.example.data.remote

import com.example.domain.model.MangaSource
import retrofit2.http.GET

interface GithubConfigService {
    // using a mock gist or raw URL placeholder for default sources
    @GET("https://raw.githubusercontent.com/hasandong/MangaAI/main/default_sources.json")
    suspend fun getDefaultSources(): List<MangaSource>
}
