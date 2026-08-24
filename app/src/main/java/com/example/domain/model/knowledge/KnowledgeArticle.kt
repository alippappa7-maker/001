package com.example.domain.model.knowledge

enum class KnowledgeCategory(val titleAr: String) {
    QURAN("القرآن وعلومه"),
    HADITH("الحديث والسيرة"),
    FIQH("الفقه والعبادات"),
    MORALS("الأخلاق"),
    SELF_PURIFICATION("تزكية النفس"),
    FAMILY("الأسرة والمجتمع")
}

data class KnowledgeArticle(
    val id: String,
    val title: String,
    val description: String,
    val content: String,
    val category: KnowledgeCategory,
    val source: String,
    val isIntroductory: Boolean,
    val isFavorite: Boolean = false,
    val progressPercent: Float = 0f,
    val lastReadPosition: Int = 0
)
