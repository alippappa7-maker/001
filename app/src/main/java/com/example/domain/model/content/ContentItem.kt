package com.example.domain.model.content

sealed interface ContentItem {
    val id: String
    val type: ContentType
    val titleAr: String
    val titleEn: String
    val descriptionAr: String?
    val descriptionEn: String?
    val bodyAr: String?
    val bodyEn: String?
    val source: ContentSource?
    val category: ContentCategory
    val imageUrl: String?
    val audioUrl: String?
    val isPublished: Boolean
    val createdAt: Long
    val updatedAt: Long
    val tags: List<String>
    val isFavorite: Boolean
    val sortOrder: Int
    val localeAvailability: List<String>
    val contentVersion: Int

    data class Article(
        override val id: String,
        override val titleAr: String,
        override val titleEn: String = "",
        override val descriptionAr: String? = null,
        override val descriptionEn: String? = null,
        override val bodyAr: String? = null,
        override val bodyEn: String? = null,
        override val source: ContentSource? = null,
        override val category: ContentCategory,
        override val imageUrl: String? = null,
        override val audioUrl: String? = null,
        override val isPublished: Boolean = false,
        override val createdAt: Long = System.currentTimeMillis(),
        override val updatedAt: Long = System.currentTimeMillis(),
        override val tags: List<String> = emptyList(),
        override val isFavorite: Boolean = false,
        override val sortOrder: Int = 0,
        override val localeAvailability: List<String> = listOf("ar"),
        override val contentVersion: Int = 1,
        
        val estimatedReadMinutes: Int = 0,
        val progressPercent: Float = 0f,
        val lastReadPosition: Int = 0,
        val isIntroductory: Boolean = false
    ) : ContentItem {
        override val type = ContentType.ARTICLE
    }

    data class Lesson(
        override val id: String,
        override val titleAr: String,
        override val titleEn: String = "",
        override val descriptionAr: String? = null,
        override val descriptionEn: String? = null,
        override val bodyAr: String? = null,
        override val bodyEn: String? = null,
        override val source: ContentSource? = null,
        override val category: ContentCategory,
        override val imageUrl: String? = null,
        override val audioUrl: String? = null,
        override val isPublished: Boolean = false,
        override val createdAt: Long = System.currentTimeMillis(),
        override val updatedAt: Long = System.currentTimeMillis(),
        override val tags: List<String> = emptyList(),
        override val isFavorite: Boolean = false,
        override val sortOrder: Int = 0,
        override val localeAvailability: List<String> = listOf("ar"),
        override val contentVersion: Int = 1,
        
        val videoUrl: String? = null
    ) : ContentItem {
        override val type = ContentType.LESSON
    }
    
    data class Dhikr(
        override val id: String,
        override val titleAr: String,
        override val titleEn: String = "",
        override val descriptionAr: String? = null,
        override val descriptionEn: String? = null,
        override val bodyAr: String? = null,
        override val bodyEn: String? = null,
        override val source: ContentSource? = null,
        override val category: ContentCategory,
        override val imageUrl: String? = null,
        override val audioUrl: String? = null,
        override val isPublished: Boolean = false,
        override val createdAt: Long = System.currentTimeMillis(),
        override val updatedAt: Long = System.currentTimeMillis(),
        override val tags: List<String> = emptyList(),
        override val isFavorite: Boolean = false,
        override val sortOrder: Int = 0,
        override val localeAvailability: List<String> = listOf("ar"),
        override val contentVersion: Int = 1,
        
        val count: Int = 1
    ) : ContentItem {
        override val type = ContentType.DHIKR
    }

    data class ImpactInitiative(
        override val id: String,
        override val titleAr: String,
        override val titleEn: String = "",
        override val descriptionAr: String? = null,
        override val descriptionEn: String? = null,
        override val bodyAr: String? = null,
        override val bodyEn: String? = null,
        override val source: ContentSource? = null,
        override val category: ContentCategory,
        override val imageUrl: String? = null,
        override val audioUrl: String? = null,
        override val isPublished: Boolean = false,
        override val createdAt: Long = System.currentTimeMillis(),
        override val updatedAt: Long = System.currentTimeMillis(),
        override val tags: List<String> = emptyList(),
        override val isFavorite: Boolean = false,
        override val sortOrder: Int = 0,
        override val localeAvailability: List<String> = listOf("ar"),
        override val contentVersion: Int = 1,
        
        val effortLevel: String = "متوسط",
        val approximateTimeMinutes: Int = 0,
        val detailedSteps: String = ""
    ) : ContentItem {
        override val type = ContentType.IMPACT_INITIATIVE
    }
}
