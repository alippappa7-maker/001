package com.example.data.local.knowledge

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.knowledge.KnowledgeCategory

@Entity(tableName = "knowledge_articles")
data class KnowledgeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val content: String,
    val category: String, // String representation of KnowledgeCategory
    val source: String,
    val isIntroductory: Boolean,
    val isFavorite: Boolean,
    val progressPercent: Float,
    val lastReadPosition: Int
)
