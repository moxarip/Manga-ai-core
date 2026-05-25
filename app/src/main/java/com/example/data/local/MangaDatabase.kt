package com.example.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.data.local.entity.MangaDetailsEntity
import com.example.data.local.entity.MangaSourceEntity

@Database(entities = [MangaSourceEntity::class, MangaDetailsEntity::class], version = 1, exportSchema = false)
abstract class MangaDatabase : RoomDatabase() {
    abstract fun mangaDao(): MangaDao
}
