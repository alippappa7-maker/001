package com.example.ui.screens.impact

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.ImpactRepositoryImpl
import com.example.domain.model.impact.ImpactCategory
import com.example.domain.model.impact.ImpactInitiative
import com.example.domain.repository.ImpactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ImpactViewModel(
    application: Application,
    private val repository: ImpactRepository = ImpactRepositoryImpl(application)
) : AndroidViewModel(application) {

    val selectedCategory = MutableStateFlow<ImpactCategory?>(null)
    val showOnlyFavorites = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            repository.initializeInitiatives()
        }
    }

    val initiatives: StateFlow<List<ImpactInitiative>> = combine(
        repository.getAllInitiatives(),
        selectedCategory,
        showOnlyFavorites
    ) { allInitiatives, category, favoritesOnly ->
        allInitiatives.filter { initiative ->
            val matchesCategory = if (category == null) true else initiative.category == category
            val matchesFavorites = if (favoritesOnly) initiative.isFavorite else true

            matchesCategory && matchesFavorites
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun selectCategory(category: ImpactCategory?) {
        selectedCategory.value = category
    }

    fun toggleFavoritesFilter() {
        showOnlyFavorites.value = !showOnlyFavorites.value
    }

    fun toggleFavorite(initiativeId: String, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(initiativeId, !currentStatus)
        }
    }
}
