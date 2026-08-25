package com.example.data.repository.home

import com.example.data.repository.MihrabRepository
import com.example.data.repository.PrayerTimeRepository
import com.example.data.repository.StudioRepository
import com.example.domain.model.Ayah
import com.example.domain.model.DailyPrayerTimes
import com.example.domain.model.DailyProgress
import com.example.domain.model.PrayerTime
import com.example.domain.model.PrayerTimeConfig
import com.example.domain.model.Zikr
import com.example.domain.model.content.ContentCategory
import com.example.domain.model.content.ContentItem
import com.example.domain.model.content.ContentValidationResult
import com.example.domain.model.studio.StyleSignature
import com.example.domain.model.studio.VideoProject
import com.example.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import java.util.Date

class DashboardAggregatorTest {

    private lateinit var aggregator: DashboardAggregator
    
    private val studioFlow = MutableStateFlow<List<VideoProject>>(emptyList())
    private val contentFlow = MutableStateFlow<List<ContentItem>>(emptyList())
    private val prayerFlow = MutableStateFlow<DailyPrayerTimes?>(null)
    private val progressFlow = MutableStateFlow<DailyProgress>(DailyProgress(2, 5))
    private val ayahFlow = MutableStateFlow<Ayah>(Ayah("a1", "إِنَّ مَعَ الْعُسْرِ يُسْرًا", "Ease", "الشرح", "Ash-Sharh", 6))

    @Before
    fun setup() {
        val studioRepo = object : StudioRepository {
            override fun getAllProjects(): Flow<List<VideoProject>> = studioFlow
            override suspend fun getProjectById(id: String): VideoProject? = null
            override suspend fun saveProject(project: VideoProject) {}
            override suspend fun deleteProject(id: String) {}
            override fun getAllStyleReferences(): Flow<List<StyleSignature>> = kotlinx.coroutines.flow.flowOf(emptyList())
            override suspend fun getStyleReferenceById(id: String): StyleSignature? = null
            override suspend fun saveStyleReference(signature: StyleSignature) {}
            override suspend fun deleteStyleReference(id: String) {}
        }

        val mihrabRepo = object : MihrabRepository {
            override fun getAzkar(query: String): Flow<List<Zikr>> = MutableStateFlow(emptyList())
            override fun getFavorites(): Flow<List<Zikr>> = MutableStateFlow(emptyList())
            override suspend fun toggleFavorite(zikrId: String) {}
            override fun getDailyAyah(): Flow<Ayah> = ayahFlow
            override fun getPrayerTimes(): Flow<List<PrayerTime>> = MutableStateFlow(emptyList())
            override fun getDailyProgress(): Flow<DailyProgress> = progressFlow
            override suspend fun updateDailyProgress(completed: Int, total: Int) {}
        }

        val prayerRepo = object : PrayerTimeRepository {
            override suspend fun fetchPrayerTimes(lat: Double, lng: Double, config: PrayerTimeConfig, locationNameAr: String?, locationNameEn: String?) = Result.success(DailyPrayerTimes(
                fajr = PrayerTime(id = "1", nameAr = "الفجر", nameEn = "Fajr", timeStr = "05:00"),
                sunrise = PrayerTime(id = "2", nameAr = "الشروق", nameEn = "Sunrise", timeStr = "06:00"),
                dhuhr = PrayerTime(id = "3", nameAr = "الظهر", nameEn = "Dhuhr", timeStr = "12:00"),
                asr = PrayerTime(id = "4", nameAr = "العصر", nameEn = "Asr", timeStr = "15:00"),
                maghrib = PrayerTime(id = "5", nameAr = "المغرب", nameEn = "Maghrib", timeStr = "18:00"),
                isha = PrayerTime(id = "6", nameAr = "العشاء", nameEn = "Isha", timeStr = "19:30"),
                hijriDateStr = "", gregorianDateStr = "", lastUpdated = 0L
            ))
            override fun getCachedPrayerTimes(): Flow<DailyPrayerTimes?> = prayerFlow
        }

        val contentRepo = object : ContentRepository {
            override fun observePublishedContent(): Flow<List<ContentItem>> = contentFlow
            override fun searchContent(query: String): Flow<List<ContentItem>> = MutableStateFlow(emptyList())
            override fun getContentByCategory(category: ContentCategory): Flow<List<ContentItem>> = MutableStateFlow(emptyList())
            override fun getFavoriteContent(): Flow<List<ContentItem>> = MutableStateFlow(emptyList())
            override suspend fun getContentById(id: String): ContentItem? = null
            override suspend fun setFavorite(contentId: String, isFavorite: Boolean) {}
            override suspend fun saveReadingProgress(contentId: String, progress: Float, position: Int) {}
            override suspend fun getReadingProgress(contentId: String): Pair<Float, Int> = 0f to 0
            override fun validateForPublishing(content: ContentItem): ContentValidationResult = ContentValidationResult.Valid
            override suspend fun initializeLocalContent() {}
        }

        aggregator = DashboardAggregator(studioRepo, mihrabRepo, prayerRepo, contentRepo)
    }

    @Test
    fun testEmptyStudioState() = runTest {
        studioFlow.value = emptyList()
        val state = aggregator.getDashboardState().first()
        assertEquals(0, state.studioProjectCount)
    }

    @Test
    fun testStudioProjectCountUpdates() = runTest {
        studioFlow.value = listOf(
            VideoProject(
                id = "1",
                title = "P1",
                status = com.example.domain.model.studio.VideoStatus.COMPLETED,
                createdAt = Date().time
            )
        )
        val state = aggregator.getDashboardState().first()
        assertEquals(1, state.studioProjectCount)
    }
    
    @Test
    fun testNextPrayerIsDisplayed() = runTest {
        prayerFlow.value = DailyPrayerTimes(
            fajr = PrayerTime(id = "1", nameAr = "الفجر", nameEn = "Fajr", timeStr = "05:00"),
            sunrise = PrayerTime(id = "2", nameAr = "الشروق", nameEn = "Sunrise", timeStr = "06:00"),
            dhuhr = PrayerTime(id = "3", nameAr = "الظهر", nameEn = "Dhuhr", timeStr = "12:00"),
            asr = PrayerTime(id = "4", nameAr = "العصر", nameEn = "Asr", timeStr = "15:00", isNext = true),
            maghrib = PrayerTime(id = "5", nameAr = "المغرب", nameEn = "Maghrib", timeStr = "18:00"),
            isha = PrayerTime(id = "6", nameAr = "العشاء", nameEn = "Isha", timeStr = "19:30"),
            hijriDateStr = "",
            gregorianDateStr = "",
            lastUpdated = 0L
        )
        val state = aggregator.getDashboardState().first()
        assertEquals("العصر", state.nextPrayer)
    }
    
    private fun createDummyArticle(id: String, isFavorite: Boolean): ContentItem.Article {
        return ContentItem.Article(
            id = id,
            titleAr = "A",
            titleEn = "A",
            category = ContentCategory.QURAN,
            isPublished = true,
            createdAt = 0L,
            updatedAt = 0L,
            tags = emptyList(),
            isFavorite = isFavorite,
            sortOrder = 0,
            localeAvailability = emptyList(),
            contentVersion = 1,
            estimatedReadMinutes = 1
        )
    }

    @Test
    fun testKnowledgeProgress() = runTest {
        contentFlow.value = listOf(
            createDummyArticle("1", true),
            createDummyArticle("2", false)
        )
        val state = aggregator.getDashboardState().first()
        assertEquals(2, state.knowledgeTotalItems)
        assertEquals(1, state.knowledgeCompletedItems)
        assertEquals(0.5f, state.knowledgeProgress)
    }

    @Test
    fun testDailyJourneyStateCalculation() = runTest {
        progressFlow.value = DailyProgress(completedTasks = 2, totalTasks = 5)
        contentFlow.value = listOf(createDummyArticle("1", true))
        val state = aggregator.getDashboardState().first()
        
        assertNotNull(state.dailyJourney)
        assertEquals(true, state.dailyJourney.prayerCompleted)
        assertEquals(true, state.dailyJourney.dhikrCompleted)
        assertEquals(true, state.dailyJourney.readingCompleted)
        assertEquals(3, state.dailyJourney.completedStepsCount)
        assertEquals(0.75f, state.dailyJourney.overallProgress)
    }

    @Test
    fun testEmptyDailyJourneyState() = runTest {
        progressFlow.value = DailyProgress(completedTasks = 0, totalTasks = 5)
        contentFlow.value = emptyList()
        val state = aggregator.getDashboardState().first()
        
        assertEquals(false, state.dailyJourney.prayerCompleted)
        assertEquals(false, state.dailyJourney.dhikrCompleted)
        assertEquals(false, state.dailyJourney.readingCompleted)
        assertEquals(0, state.dailyJourney.completedStepsCount)
        assertEquals(0f, state.dailyJourney.overallProgress)
    }

    @Test
    fun testIsolatedFailureDoesNotCrash() = runTest {
        val failingStudioRepo = object : StudioRepository {
            override fun getAllProjects(): Flow<List<VideoProject>> = kotlinx.coroutines.flow.flow { throw RuntimeException("Studio DB Error") }
            override suspend fun getProjectById(id: String): VideoProject? = null
            override suspend fun saveProject(project: VideoProject) {}
            override suspend fun deleteProject(id: String) {}
            override fun getAllStyleReferences(): Flow<List<StyleSignature>> = kotlinx.coroutines.flow.flowOf(emptyList())
            override suspend fun getStyleReferenceById(id: String): StyleSignature? = null
            override suspend fun saveStyleReference(signature: StyleSignature) {}
            override suspend fun deleteStyleReference(id: String) {}
        }
        val robustAggregator = DashboardAggregator(
            failingStudioRepo,
            aggregator.javaClass.getDeclaredField("mihrabRepository").apply { isAccessible = true }.get(aggregator) as MihrabRepository,
            aggregator.javaClass.getDeclaredField("prayerTimeRepository").apply { isAccessible = true }.get(aggregator) as PrayerTimeRepository,
            aggregator.javaClass.getDeclaredField("contentRepository").apply { isAccessible = true }.get(aggregator) as ContentRepository
        )
        
        prayerFlow.value = DailyPrayerTimes(
            fajr = PrayerTime(id = "1", nameAr = "الفجر", nameEn = "Fajr", timeStr = "05:00", isNext = true),
            sunrise = PrayerTime(id = "2", nameAr = "الشروق", nameEn = "Sunrise", timeStr = "06:00"),
            dhuhr = PrayerTime(id = "3", nameAr = "الظهر", nameEn = "Dhuhr", timeStr = "12:00"),
            asr = PrayerTime(id = "4", nameAr = "العصر", nameEn = "Asr", timeStr = "15:00"),
            maghrib = PrayerTime(id = "5", nameAr = "المغرب", nameEn = "Maghrib", timeStr = "18:00"),
            isha = PrayerTime(id = "6", nameAr = "العشاء", nameEn = "Isha", timeStr = "19:30"),
            hijriDateStr = "", gregorianDateStr = "", lastUpdated = 0L
        )

        val state = robustAggregator.getDashboardState().first()
        assertEquals(0, state.studioProjectCount)
        assertEquals("الفجر", state.nextPrayer)
    }
}
