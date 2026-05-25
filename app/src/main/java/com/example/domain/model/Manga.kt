package com.example.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Manga(
    val id: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val sourceId: String,
    val chaptersListJson: String = "[]" // Storing as JSON string for simplicity as requested
) {
    fun getChapters(): List<Chapter> {
        return try {
            kotlinx.serialization.json.Json.decodeFromString(chaptersListJson)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
