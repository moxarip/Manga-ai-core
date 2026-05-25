package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.data.local.MangaDatabase
import com.example.data.remote.MangaScraperService
import com.example.data.remote.RetrofitClient
import com.example.data.repository.MangaRepositoryImpl
import com.example.data.security.SecurityManager
import com.example.domain.repository.MangaRepository
import com.example.domain.usecase.AddCustomSourceUseCase
import com.example.domain.usecase.FetchDefaultSourcesUseCase
import com.example.domain.usecase.FetchMangaDetailsUseCase
import com.example.domain.usecase.GetSourcesUseCase

interface AppContainer {
    val mangaRepository: MangaRepository
    val securityManager: SecurityManager
    val getSourcesUseCase: GetSourcesUseCase
    val fetchDefaultSourcesUseCase: FetchDefaultSourcesUseCase
    val addCustomSourceUseCase: AddCustomSourceUseCase
    val fetchMangaDetailsUseCase: FetchMangaDetailsUseCase
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    private val database: MangaDatabase by lazy {
        Room.databaseBuilder(context, MangaDatabase::class.java, "manga_db")
            .fallbackToDestructiveMigration()
            .build()
    }

    override val securityManager: SecurityManager by lazy {
        SecurityManager(context)
    }

    private val scraperService: MangaScraperService by lazy {
        MangaScraperService(securityManager)
    }

    override val mangaRepository: MangaRepository by lazy {
        MangaRepositoryImpl(
            database.mangaDao(),
            RetrofitClient.githubConfigService,
            scraperService
        )
    }

    override val getSourcesUseCase: GetSourcesUseCase by lazy {
        GetSourcesUseCase(mangaRepository)
    }

    override val fetchDefaultSourcesUseCase: FetchDefaultSourcesUseCase by lazy {
        FetchDefaultSourcesUseCase(mangaRepository)
    }

    override val addCustomSourceUseCase: AddCustomSourceUseCase by lazy {
        AddCustomSourceUseCase(mangaRepository)
    }

    override val fetchMangaDetailsUseCase: FetchMangaDetailsUseCase by lazy {
        FetchMangaDetailsUseCase(mangaRepository)
    }
}
