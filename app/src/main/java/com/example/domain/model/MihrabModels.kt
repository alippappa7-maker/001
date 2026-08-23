package com.example.domain.model

data class Zikr(
    val id: String,
    val textAr: String,
    val textEn: String,
    val sourceAr: String,
    val sourceEn: String,
    val count: Int,
    val category: String,
    val isFavorite: Boolean = false
)

data class Ayah(
    val id: String,
    val textAr: String,
    val textEn: String,
    val surahNameAr: String,
    val surahNameEn: String,
    val ayahNumber: Int
)

data class PrayerTime(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val timeStr: String,
    val isNext: Boolean = false
)

data class DailyProgress(
    val completedTasks: Int,
    val totalTasks: Int
) {
    val progressPercentage: Float
        get() = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
}
