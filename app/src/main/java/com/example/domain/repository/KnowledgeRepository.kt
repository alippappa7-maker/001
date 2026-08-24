package com.example.domain.repository

import com.example.domain.model.knowledge.KnowledgeArticle
import kotlinx.coroutines.flow.Flow

interface KnowledgeRepository {
    fun getAllArticles(): Flow<List<KnowledgeArticle>>
    suspend fun getArticleById(id: String): KnowledgeArticle?
    suspend fun initializeLibrary() // To populate mock data if empty
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)
    suspend fun updateProgress(id: String, progress: Float, position: Int)
}
