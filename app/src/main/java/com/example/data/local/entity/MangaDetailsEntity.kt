package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.Manga

@Entity(tableName = "manga_details")
data class MangaDetailsEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val description: String,
    val coverUrl: String,
    val chaptersListJson: String,
    val sourceId: String
)

fun MangaDetailsEntity.toDomain() = Manga(id, title, description, coverUrl, sourceId, chaptersListJson)
fun Manga.toEntity() = MangaDetailsEntity(id, title, description, coverUrl, chaptersListJson, sourceId)
