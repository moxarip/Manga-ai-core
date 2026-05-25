package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Chapter(
    val title: String,
    val url: String
)
