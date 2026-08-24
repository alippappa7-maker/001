package com.example.domain.repository

import com.example.domain.model.impact.ImpactInitiative
import kotlinx.coroutines.flow.Flow

interface ImpactRepository {
    fun getAllInitiatives(): Flow<List<ImpactInitiative>>
    suspend fun getInitiativeById(id: String): ImpactInitiative?
    suspend fun initializeInitiatives()
    suspend fun toggleFavorite(id: String, isFavorite: Boolean)
}
