package com.example.data.local.knowledge

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface KnowledgeDao {
    @Query("SELECT * FROM knowledge_articles")
    fun getAllArticles(): Flow<List<KnowledgeEntity>>

    @Query("SELECT * FROM knowledge_articles WHERE id = :id")
    suspend fun getArticleById(id: String): KnowledgeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<KnowledgeEntity>)

    @Query("UPDATE knowledge_articles SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: String, isFavorite: Boolean)

    @Query("UPDATE knowledge_articles SET progressPercent = :progress, lastReadPosition = :position WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Float, position: Int)
}
