package com.example.data.local.content

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "content_items")
data class ContentEntity(
    @PrimaryKey val id: String,
    val type: String,
    val titleAr: String,
    val titleEn: String,
    val descriptionAr: String?,
    val descriptionEn: String?,
    val bodyAr: String?,
    val bodyEn: String?,
    
    // Source flattened
    val sourceName: String?,
    val sourceReference: String?,
    val sourceUrl: String?,
    val sourceVerified: Boolean,
    
    val category: String,
    val imageUrl: String?,
    val audioUrl: String?,
    val isPublished: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val tags: String, 
    val isFavorite: Boolean,
    val sortOrder: Int,
    val localeAvailability: String,
    val contentVersion: Int,
    
    // Article specific
    val estimatedReadMinutes: Int?,
    val progressPercent: Float?,
    val lastReadPosition: Int?,
    val isIntroductory: Boolean?,
    
    // Lesson specific
    val videoUrl: String?,
    
    // Dhikr specific
    val count: Int?,
    
    // Impact specific
    val effortLevel: String?,
    val approximateTimeMinutes: Int?,
    val detailedSteps: String?
)
