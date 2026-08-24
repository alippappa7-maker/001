package com.example.ui.screens.knowledge

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.LocalContentRepository
import com.example.domain.model.content.ContentItem
import com.example.domain.model.content.ContentCategory
import com.example.domain.model.content.ContentType
import com.example.domain.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KnowledgeViewModel(
    application: Application,
    private val repository: ContentRepository = LocalContentRepository(application)
) : AndroidViewModel(application) {

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<ContentCategory?>(null)
    val showOnlyFavorites = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            repository.initializeLocalContent()
        }
    }

    val articles: StateFlow<List<ContentItem.Article>> = combine(
        repository.observePublishedContent(),
        searchQuery,
        selectedCategory,
        showOnlyFavorites
    ) { allContent, query, category, favoritesOnly ->
        allContent.filterIsInstance<ContentItem.Article>().filter { article ->
            val matchesQuery = if (query.isBlank()) true else {
                article.titleAr.contains(query, ignoreCase = true) ||
                article.titleEn.contains(query, ignoreCase = true) ||
                (article.descriptionAr?.contains(query, ignoreCase = true) ?: false)
            }
            val matchesCategory = if (category == null) true else article.category == category
            val matchesFavorites = if (favoritesOnly) article.isFavorite else true

            matchesQuery && matchesCategory && matchesFavorites
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun selectCategory(category: ContentCategory?) {
        selectedCategory.value = category
    }

    fun toggleFavoritesFilter() {
        showOnlyFavorites.value = !showOnlyFavorites.value
    }

    fun toggleFavorite(articleId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.setFavorite(articleId, !currentStatus)
        }
    }

    fun updateReadingProgress(articleId: String, progress: Float, position: Int) {
        viewModelScope.launch {
            repository.saveReadingProgress(articleId, progress, position)
        }
    }
}
