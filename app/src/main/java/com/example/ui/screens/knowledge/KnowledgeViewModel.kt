package com.example.ui.screens.knowledge

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.KnowledgeRepositoryImpl
import com.example.domain.model.knowledge.KnowledgeArticle
import com.example.domain.model.knowledge.KnowledgeCategory
import com.example.domain.repository.KnowledgeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KnowledgeViewModel(
    application: Application,
    private val repository: KnowledgeRepository = KnowledgeRepositoryImpl(application)
) : AndroidViewModel(application) {

    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<KnowledgeCategory?>(null)
    val showOnlyFavorites = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            repository.initializeLibrary()
        }
    }

    val articles: StateFlow<List<KnowledgeArticle>> = combine(
        repository.getAllArticles(),
        searchQuery,
        selectedCategory,
        showOnlyFavorites
    ) { allArticles, query, category, favoritesOnly ->
        allArticles.filter { article ->
            val matchesQuery = if (query.isBlank()) true else {
                article.title.contains(query, ignoreCase = true) ||
                article.description.contains(query, ignoreCase = true) ||
                article.category.titleAr.contains(query, ignoreCase = true)
            }
            val matchesCategory = if (category == null) true else article.category == category
            val matchesFavorites = if (favoritesOnly) article.isFavorite else true

            matchesQuery && matchesCategory && matchesFavorites
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun selectCategory(category: KnowledgeCategory?) {
        selectedCategory.value = category
    }

    fun toggleFavoritesFilter() {
        showOnlyFavorites.value = !showOnlyFavorites.value
    }

    fun toggleFavorite(articleId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(articleId, !currentStatus)
        }
    }

    fun updateReadingProgress(articleId: String, progress: Float, position: Int) {
        viewModelScope.launch {
            repository.updateProgress(articleId, progress, position)
        }
    }
}
