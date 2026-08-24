package com.example.domain.repository

import com.example.domain.model.content.ContentCategory
import com.example.domain.model.content.ContentItem
import com.example.domain.model.content.ContentValidationResult
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    fun observePublishedContent(): Flow<List<ContentItem>>
    fun searchContent(query: String): Flow<List<ContentItem>>
    fun getContentByCategory(category: ContentCategory): Flow<List<ContentItem>>
    fun getFavoriteContent(): Flow<List<ContentItem>>
    suspend fun getContentById(id: String): ContentItem?
    suspend fun setFavorite(contentId: String, isFavorite: Boolean)
    suspend fun saveReadingProgress(contentId: String, progress: Float, position: Int)
    suspend fun getReadingProgress(contentId: String): Pair<Float, Int>
    fun validateForPublishing(content: ContentItem): ContentValidationResult
    suspend fun initializeLocalContent()
}
