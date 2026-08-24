package com.example.ui.screens.impact

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.LocalContentRepository
import com.example.domain.model.content.ContentCategory
import com.example.domain.model.content.ContentItem
import com.example.domain.model.content.ContentType
import com.example.domain.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ImpactViewModel(
    application: Application,
    private val repository: ContentRepository = LocalContentRepository(application)
) : AndroidViewModel(application) {

    val selectedCategory = MutableStateFlow<ContentCategory?>(null)
    val showOnlyFavorites = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            repository.initializeLocalContent()
        }
    }

    val initiatives: StateFlow<List<ContentItem.ImpactInitiative>> = combine(
        repository.observePublishedContent(),
        selectedCategory,
        showOnlyFavorites
    ) { allContent, category, favoritesOnly ->
        allContent.filterIsInstance<ContentItem.ImpactInitiative>().filter { initiative ->
            val matchesCategory = if (category == null) true else initiative.category == category
            val matchesFavorites = if (favoritesOnly) initiative.isFavorite else true

            matchesCategory && matchesFavorites
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectCategory(category: ContentCategory?) {
        selectedCategory.value = category
    }

    fun toggleFavoritesFilter() {
        showOnlyFavorites.value = !showOnlyFavorites.value
    }

    fun toggleFavorite(initiativeId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.setFavorite(initiativeId, !currentStatus)
        }
    }
}
