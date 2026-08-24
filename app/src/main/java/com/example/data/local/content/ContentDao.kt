package com.example.data.local.content

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentDao {
    @Query("SELECT * FROM content_items WHERE isPublished = 1")
    fun getPublishedContent(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content_items")
    fun getAllContent(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content_items WHERE titleAr LIKE '%' || :query || '%' OR descriptionAr LIKE '%' || :query || '%'")
    fun searchContent(query: String): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content_items WHERE category = :category")
    fun getContentByCategory(category: String): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content_items WHERE isFavorite = 1")
    fun getFavoriteContent(): Flow<List<ContentEntity>>

    @Query("SELECT * FROM content_items WHERE id = :id")
    suspend fun getContentById(id: String): ContentEntity?

    @Query("UPDATE content_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE content_items SET progressPercent = :progress, lastReadPosition = :position WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Float, position: Int)

    @Query("SELECT progressPercent, lastReadPosition FROM content_items WHERE id = :id")
    suspend fun getProgress(id: String): ProgressData?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(items: List<ContentEntity>)

    @Query("SELECT COUNT(*) FROM content_items")
    suspend fun getContentCount(): Int
}

data class ProgressData(val progressPercent: Float?, val lastReadPosition: Int?)
