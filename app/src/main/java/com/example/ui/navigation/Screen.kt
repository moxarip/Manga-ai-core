package com.example.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object Home

@Serializable
data class MangaDetail(val sourceId: String, val mangaUrl: String)

@Serializable
object Settings
