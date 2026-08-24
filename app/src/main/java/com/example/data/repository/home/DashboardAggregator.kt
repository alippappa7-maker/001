package com.example.data.repository.home

import com.example.data.repository.MihrabRepository
import com.example.data.repository.PrayerTimeRepository
import com.example.data.repository.StudioRepository
import com.example.domain.model.content.ContentCategory
import com.example.domain.model.content.ContentItem
import com.example.domain.model.content.ContentType
import com.example.domain.model.Ayah
import com.example.domain.model.DailyPrayerTimes
import com.example.domain.model.DailyProgress
import com.example.domain.model.home.CompassStateStatus
import com.example.domain.model.home.DailyJourneyState
import com.example.domain.model.home.DashboardState
import com.example.domain.model.studio.VideoStatus
import com.example.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardAggregator(
    private val studioRepository: StudioRepository,
    private val mihrabRepository: MihrabRepository,
    private val prayerTimeRepository: PrayerTimeRepository,
    private val contentRepository: ContentRepository
) {
    fun getDashboardState(): Flow<DashboardState> {
        val studioFlow = studioRepository.getAllProjects().catch { emit(emptyList()) }
        val contentFlow = contentRepository.observePublishedContent().catch { emit(emptyList()) }
        val prayerFlow: Flow<DailyPrayerTimes?> = prayerTimeRepository.getCachedPrayerTimes()
            .catch { emit(null) }

        val dailyProgressFlow: Flow<DailyProgress?> = mihrabRepository.getDailyProgress()
            .map { it }
            .catch { emit(null) }

        val dailyAyahFlow: Flow<Ayah?> = mihrabRepository.getDailyAyah()
            .map { it }
            .catch { emit(null) }
        
        return combine(
            studioFlow,
            contentFlow,
            prayerFlow,
            dailyProgressFlow,
            dailyAyahFlow
        ) { projects, contents, prayers, progress, ayah ->
            
            // 1. Studio Metrics
            val totalStudioCount = projects.size
            val completedStudioCount = projects.count { it.status == VideoStatus.COMPLETED }
            val processingStudioCount = projects.count { 
                it.status == VideoStatus.GENERATING || 
                it.status == VideoStatus.ANALYZING || 
                it.status == VideoStatus.PLANNING 
            }
            val failedStudioCount = projects.count { it.status == VideoStatus.FAILED }
            val latestProject = projects.maxByOrNull { it.updatedAt } ?: projects.maxByOrNull { it.createdAt }

            // 2. Knowledge Metrics
            val knowledgeItems = contents.filter { 
                it.category?.isReligious == true || 
                it.type == ContentType.ARTICLE || 
                it.type == ContentType.LESSON 
            }
            val articles = contents.filterIsInstance<ContentItem.Article>()
            val knowledgeTotal = knowledgeItems.size
            val knowledgeCompleted = articles.count { 
                it.progressPercent >= 0.95f || it.isFavorite 
            }
            val knowledgeProgress = if (knowledgeTotal > 0) {
                (knowledgeCompleted.toFloat() / knowledgeTotal.toFloat()).coerceIn(0f, 1f)
            } else 0f
            val knowledgeFavorites = knowledgeItems.count { it.isFavorite }
            val latestReadArticle = articles.filter { it.lastReadPosition > 0 || it.progressPercent > 0f }
                .maxByOrNull { it.updatedAt }
            val latestReadTitle = latestReadArticle?.titleAr
            val latestReadPos = latestReadArticle?.lastReadPosition
            val currentlyReading = latestReadArticle?.takeIf { it.progressPercent < 0.95f }?.titleAr

            // 3. Impact Metrics
            val impactItems = contents.filter { 
                it.type == ContentType.IMPACT_INITIATIVE || 
                it.category?.isReligious == false 
            }
            val impactTotal = impactItems.size
            val impactFavorites = impactItems.count { it.isFavorite }
            val impactCompleted = impactFavorites
            val latestSavedImpact = impactItems.filter { it.isFavorite }.maxByOrNull { it.updatedAt }?.titleAr
            val dailyImpactDone = impactFavorites > 0

            // 4. Prayer & Mihrab Metrics
            val nextPrayerObj = prayers?.toList()?.firstOrNull { it.isNext } ?: prayers?.fajr
            val nextPrayerName = nextPrayerObj?.nameAr ?: "الفجر"
            val nextPrayerTime = nextPrayerObj?.timeStr
            val prayerStatus = when {
                prayers == null -> "غير متوفر"
                prayers.isStale -> "بيانات سابقة"
                else -> "محدّثة"
            }
            val dhikrDone = (progress?.completedTasks ?: 0) > 0

            // 5. Daily Journey Calculation
            val prayerDone = (progress?.completedTasks ?: 0) >= 1
            val readingDone = knowledgeCompleted > 0 || (latestReadPos ?: 0) > 0
            
            var completedSteps = 0
            if (prayerDone) completedSteps++
            if (dhikrDone) completedSteps++
            if (readingDone) completedSteps++
            if (dailyImpactDone) completedSteps++
            val totalSteps = 4
            val journeyOverall = if (totalSteps > 0) {
                (completedSteps.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)
            } else 0f

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = dateFormat.format(Date())

            val dailyJourney = DailyJourneyState(
                prayerCompleted = prayerDone,
                prayerCompletedCount = if (prayerDone) 1 else 0,
                totalPrayers = 5,
                dhikrCompleted = dhikrDone,
                readingCompleted = readingDone,
                impactCompleted = dailyImpactDone,
                overallProgress = journeyOverall,
                completedStepsCount = completedSteps,
                totalStepsCount = totalSteps,
                streakDays = if (completedSteps > 0) 1 else 0,
                todayDateFormatted = todayStr,
                lastUpdatedAt = System.currentTimeMillis()
            )

            DashboardState(
                qiblaStatus = "جاهزة",
                compassStatus = CompassStateStatus.READY,
                qiblaDirection = null,
                userDirection = null,
                compassErrorMessage = null,

                nextPrayer = nextPrayerName,
                nextPrayerTime = nextPrayerTime,
                prayerProgress = (progress?.completedTasks ?: 0).toFloat() / 5f,
                prayerTimesStatus = prayerStatus,
                todayQuote = ayah?.textAr,
                completedPrayersCount = if (prayerDone) 1 else 0,

                studioProjectCount = totalStudioCount,
                studioCompletedProjectsCount = completedStudioCount,
                studioProcessingProjectsCount = processingStudioCount,
                studioFailedProjectsCount = failedStudioCount,
                latestStudioProject = latestProject?.title,
                latestStudioProjectId = latestProject?.id,
                studioProjectStatus = latestProject?.status?.name,
                latestStudioProjectUpdatedAt = latestProject?.updatedAt,
                studioProgress = null,

                knowledgeTotalItems = knowledgeTotal,
                knowledgeCompletedItems = knowledgeCompleted,
                knowledgeProgress = knowledgeProgress,
                knowledgeFavoritesCount = knowledgeFavorites,
                latestReadContentTitle = latestReadTitle,
                latestReadPosition = latestReadPos,
                currentlyReadingTitle = currentlyReading,

                impactTotalCount = impactTotal,
                impactFavoritesCount = impactFavorites,
                impactCompletedCount = impactCompleted,
                latestSavedImpactTitle = latestSavedImpact,
                dailyImpactCompleted = dailyImpactDone,

                dailyJourney = dailyJourney,
                dailyJourneyProgress = journeyOverall,
                dailyDhikrCompleted = dhikrDone,
                dailyReadingCompleted = readingDone,

                isLoading = false,
                errorMessage = null
            )
        }.catch { e ->
            emit(DashboardState(isLoading = false, errorMessage = e.message))
        }
    }
}
