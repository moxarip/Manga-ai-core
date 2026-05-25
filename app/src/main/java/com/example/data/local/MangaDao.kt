package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.MangaDetailsEntity
import com.example.data.local.entity.MangaSourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MangaDao {
    @Query("SELECT * FROM manga_sources")
    fun getAllSources(): Flow<List<MangaSourceEntity>>

    @Query("SELECT * FROM manga_sources")
    suspend fun getSourcesSync(): List<MangaSourceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSources(sources: List<MangaSourceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSource(source: MangaSourceEntity)

    @Query("SELECT * FROM manga_details WHERE sourceId = :sourceId")
    suspend fun getMangaDetailsBySourceId(sourceId: String): List<MangaDetailsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMangaDetails(manga: MangaDetailsEntity)
}
