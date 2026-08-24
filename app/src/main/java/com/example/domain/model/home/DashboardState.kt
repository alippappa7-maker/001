package com.example.domain.model.home

data class DailyJourneyState(
    val prayerCompleted: Boolean = false,
    val prayerCompletedCount: Int = 0,
    val totalPrayers: Int = 5,
    val dhikrCompleted: Boolean = false,
    val readingCompleted: Boolean = false,
    val impactCompleted: Boolean = false,
    val overallProgress: Float = 0f,
    val completedStepsCount: Int = 0,
    val totalStepsCount: Int = 4,
    val streakDays: Int = 1,
    val todayDateFormatted: String = "",
    val lastUpdatedAt: Long = 0L
)

enum class CompassStateStatus(val titleAr: String, val titleEn: String) {
    READY("جاهزة", "Ready"),
    PERMISSION_REQUIRED("يلزم إذن الموقع", "Permission Required"),
    LOCATION_UNAVAILABLE("الموقع غير متاح", "Location Unavailable"),
    SENSOR_UNAVAILABLE("الحساس غير متاح", "Sensor Unavailable"),
    CALIBRATING("جارٍ المعايرة", "Calibrating"),
    ERROR("خطأ في البوصلة", "Error"),
    UNAVAILABLE("غير متاح", "Unavailable")
}

data class DashboardState(
    // Compass & Qibla
    val qiblaStatus: String = "جاهزة",
    val compassStatus: CompassStateStatus = CompassStateStatus.READY,
    val qiblaDirection: Float? = null,
    val userDirection: Float? = null,
    val compassErrorMessage: String? = null,

    // Mihrab & Prayer
    val nextPrayer: String? = null,
    val nextPrayerTime: String? = null,
    val prayerProgress: Float = 0f,
    val prayerTimesStatus: String? = null,
    val todayQuote: String? = null,
    val completedPrayersCount: Int = 0,

    // Studio
    val studioProjectCount: Int = 0,
    val studioCompletedProjectsCount: Int = 0,
    val studioProcessingProjectsCount: Int = 0,
    val studioFailedProjectsCount: Int = 0,
    val latestStudioProject: String? = null,
    val latestStudioProjectId: String? = null,
    val studioProjectStatus: String? = null,
    val latestStudioProjectUpdatedAt: Long? = null,
    val studioProgress: Float? = null,

    // Knowledge
    val knowledgeTotalItems: Int = 0,
    val knowledgeCompletedItems: Int = 0,
    val knowledgeProgress: Float = 0f,
    val knowledgeFavoritesCount: Int = 0,
    val latestReadContentTitle: String? = null,
    val latestReadPosition: Int? = null,
    val currentlyReadingTitle: String? = null,

    // Impact
    val impactTotalCount: Int = 0,
    val impactFavoritesCount: Int = 0,
    val impactCompletedCount: Int = 0,
    val latestSavedImpactTitle: String? = null,
    val dailyImpactCompleted: Boolean = false,

    // Daily Journey
    val dailyJourney: DailyJourneyState = DailyJourneyState(),
    val dailyJourneyProgress: Float = 0f,
    val dailyDhikrCompleted: Boolean = false,
    val dailyReadingCompleted: Boolean = false,

    // Meta
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

