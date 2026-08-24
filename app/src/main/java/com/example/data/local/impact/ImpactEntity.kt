package com.example.data.local.impact

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "impact_initiatives")
data class ImpactEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val category: String, // String representation of ImpactCategory
    val effortLevel: String, // String representation of EffortLevel
    val approximateTimeMinutes: Int,
    val detailedSteps: String,
    val source: String,
    val isFavorite: Boolean
)
