package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class MangaSource(
    val id: String,
    val name: String,
    val baseUrl: String,
    val status: String = "ACTIVE"
)
