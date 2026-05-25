package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.MangaSource

@Entity(tableName = "manga_sources")
data class MangaSourceEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val baseUrl: String,
    val status: String
)

fun MangaSourceEntity.toDomain() = MangaSource(id, name, baseUrl, status)
fun MangaSource.toEntity() = MangaSourceEntity(id, name, baseUrl, status)
