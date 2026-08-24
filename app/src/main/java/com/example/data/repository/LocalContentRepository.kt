package com.example.data.repository

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.content.ContentDao
import com.example.data.local.content.ContentEntity
import com.example.domain.model.content.ContentCategory
import com.example.domain.model.content.ContentItem
import com.example.domain.model.content.ContentSource
import com.example.domain.model.content.ContentType
import com.example.domain.model.content.ContentValidationResult
import com.example.domain.repository.ContentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalContentRepository(application: Application) : ContentRepository {

    private val contentDao: ContentDao = AppDatabase.getDatabase(application).contentDao()

    override fun observePublishedContent(): Flow<List<ContentItem>> {
        return contentDao.getPublishedContent().map { list -> list.map { it.toDomainModel() } }
    }

    override fun searchContent(query: String): Flow<List<ContentItem>> {
        return contentDao.searchContent(query).map { list -> list.map { it.toDomainModel() } }
    }

    override fun getContentByCategory(category: ContentCategory): Flow<List<ContentItem>> {
        return contentDao.getContentByCategory(category.name).map { list -> list.map { it.toDomainModel() } }
    }

    override fun getFavoriteContent(): Flow<List<ContentItem>> {
        return contentDao.getFavoriteContent().map { list -> list.map { it.toDomainModel() } }
    }

    override suspend fun getContentById(id: String): ContentItem? {
        return contentDao.getContentById(id)?.toDomainModel()
    }

    override suspend fun setFavorite(contentId: String, isFavorite: Boolean) {
        contentDao.updateFavorite(contentId, isFavorite)
    }

    override suspend fun saveReadingProgress(contentId: String, progress: Float, position: Int) {
        contentDao.updateProgress(contentId, progress, position)
    }

    override suspend fun getReadingProgress(contentId: String): Pair<Float, Int> {
        val data = contentDao.getProgress(contentId)
        return Pair(data?.progressPercent ?: 0f, data?.lastReadPosition ?: 0)
    }

    override fun validateForPublishing(content: ContentItem): ContentValidationResult {
        if (content.category.isReligious) {
            val source = content.source
            if (source == null) {
                return ContentValidationResult.Invalid("Religious content must have a source.")
            }
            if (!source.verified) {
                return ContentValidationResult.Invalid("Religious content source must be verified.")
            }
        }
        return ContentValidationResult.Valid
    }

    override suspend fun initializeLocalContent() {
        if (contentDao.getContentCount() == 0) {
            val demoContent = listOf(
                ContentEntity(
                    id = "demo_quran_1",
                    type = ContentType.ARTICLE.name,
                    titleAr = "مقدمة في تدبر القرآن",
                    titleEn = "Introduction to Quran Reflection",
                    descriptionAr = "مقال تمهيدي عن أهمية التدبر وكيفية البدء به",
                    descriptionEn = "Introductory article on the importance of reflection.",
                    bodyAr = "الحمد لله والصلاة والسلام على رسول الله...\n\nإن تدبر القرآن الكريم من أعظم العبادات التي يتقرب بها المسلم إلى ربه. قال تعالى: (كِتَابٌ أَنزَلْنَاهُ إِلَيْكَ مُبَارَكٌ لِّيَدَّبَّرُوا آيَاتِهِ). \n\nخطوات التدبر:\n١. إخلاص النية لله.\n٢. القراءة بتأنٍ وتمهل.\n٣. فهم المعاني من التفاسير الموثوقة.\n٤. سؤال النفس عن التطبيق العملي للآيات.",
                    bodyEn = "Praise be to Allah...",
                    sourceName = "تفسير السعدي، بتصرف",
                    sourceReference = "تفسير سورة ص، الآية ٢٩",
                    sourceUrl = null,
                    sourceVerified = true, // Must be verified to be published religious content
                    category = ContentCategory.QURAN.name,
                    imageUrl = null,
                    audioUrl = null,
                    isPublished = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    tags = "قرآن,تدبر,تمهيدي",
                    isFavorite = false,
                    sortOrder = 1,
                    localeAvailability = "ar,en",
                    contentVersion = 1,
                    estimatedReadMinutes = 5,
                    progressPercent = 0f,
                    lastReadPosition = 0,
                    isIntroductory = true,
                    videoUrl = null,
                    count = null,
                    effortLevel = null,
                    approximateTimeMinutes = null,
                    detailedSteps = null
                ),
                ContentEntity(
                    id = "demo_impact_1",
                    type = ContentType.IMPACT_INITIATIVE.name,
                    titleAr = "سقيا الماء للطيور والقطط",
                    titleEn = "Watering Birds and Cats",
                    descriptionAr = "توفير أوعية مياه نظيفة للحيوانات والطيور في الأيام الحارة، وهي من أعظم الصدقات.",
                    descriptionEn = "Providing clean water for animals.",
                    bodyAr = "من أعظم القربات سقي الماء.",
                    bodyEn = "Watering is a great charity.",
                    sourceName = "مستوحى من حديث: (في كل كبد رطبة أجر)",
                    sourceReference = "رواه البخاري ومسلم",
                    sourceUrl = null,
                    sourceVerified = true,
                    category = ContentCategory.ENVIRONMENT.name,
                    imageUrl = null,
                    audioUrl = null,
                    isPublished = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    tags = "بيئة,سقيا,حيوانات",
                    isFavorite = false,
                    sortOrder = 2,
                    localeAvailability = "ar,en",
                    contentVersion = 1,
                    estimatedReadMinutes = null,
                    progressPercent = null,
                    lastReadPosition = null,
                    isIntroductory = false,
                    videoUrl = null,
                    count = null,
                    effortLevel = "بسيط",
                    approximateTimeMinutes = 10,
                    detailedSteps = "١. أحضر وعاء بلاستيكياً نظيفاً.\n٢. املأه بماء الشرب.\n٣. ضعه في مكان مظلل بعيداً عن مجرى السيارات.\n٤. احرص على تنظيف الوعاء وتجديد الماء يومياً."
                ),
                ContentEntity(
                    id = "demo_draft_1",
                    type = ContentType.ARTICLE.name,
                    titleAr = "فضل قيام الليل",
                    titleEn = "Virtues of Night Prayer",
                    descriptionAr = "مسودة مقال غير منشور.",
                    descriptionEn = "Unpublished draft.",
                    bodyAr = "يجب أن يحتوي المقال على مصدر.",
                    bodyEn = "Must have a source.",
                    sourceName = null,
                    sourceReference = null,
                    sourceUrl = null,
                    sourceVerified = false,
                    category = ContentCategory.FIQH.name,
                    imageUrl = null,
                    audioUrl = null,
                    isPublished = false, // Not published due to no source
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis(),
                    tags = "مسودة",
                    isFavorite = false,
                    sortOrder = 3,
                    localeAvailability = "ar",
                    contentVersion = 1,
                    estimatedReadMinutes = 2,
                    progressPercent = 0f,
                    lastReadPosition = 0,
                    isIntroductory = false,
                    videoUrl = null,
                    count = null,
                    effortLevel = null,
                    approximateTimeMinutes = null,
                    detailedSteps = null
                )
            )
            contentDao.insertContent(demoContent)
        }
    }

    private fun ContentEntity.toDomainModel(): ContentItem {
        val mappedCategory = try { ContentCategory.valueOf(category) } catch (e: Exception) { ContentCategory.QURAN }
        val mappedType = try { ContentType.valueOf(type) } catch (e: Exception) { ContentType.ARTICLE }
        val mappedSource = if (sourceName != null) {
            ContentSource(sourceName, sourceReference, sourceUrl, sourceVerified)
        } else null
        val mappedTags = tags.split(",").filter { it.isNotBlank() }
        val mappedLocales = localeAvailability.split(",").filter { it.isNotBlank() }

        return when (mappedType) {
            ContentType.ARTICLE -> ContentItem.Article(
                id, titleAr, titleEn, descriptionAr, descriptionEn, bodyAr, bodyEn, mappedSource,
                mappedCategory, imageUrl, audioUrl, isPublished, createdAt, updatedAt, mappedTags,
                isFavorite, sortOrder, mappedLocales, contentVersion,
                estimatedReadMinutes ?: 0, progressPercent ?: 0f, lastReadPosition ?: 0, isIntroductory ?: false
            )
            ContentType.LESSON -> ContentItem.Lesson(
                id, titleAr, titleEn, descriptionAr, descriptionEn, bodyAr, bodyEn, mappedSource,
                mappedCategory, imageUrl, audioUrl, isPublished, createdAt, updatedAt, mappedTags,
                isFavorite, sortOrder, mappedLocales, contentVersion,
                videoUrl
            )
            ContentType.DHIKR -> ContentItem.Dhikr(
                id, titleAr, titleEn, descriptionAr, descriptionEn, bodyAr, bodyEn, mappedSource,
                mappedCategory, imageUrl, audioUrl, isPublished, createdAt, updatedAt, mappedTags,
                isFavorite, sortOrder, mappedLocales, contentVersion,
                count ?: 1
            )
            ContentType.IMPACT_INITIATIVE -> ContentItem.ImpactInitiative(
                id, titleAr, titleEn, descriptionAr, descriptionEn, bodyAr, bodyEn, mappedSource,
                mappedCategory, imageUrl, audioUrl, isPublished, createdAt, updatedAt, mappedTags,
                isFavorite, sortOrder, mappedLocales, contentVersion,
                effortLevel ?: "متوسط", approximateTimeMinutes ?: 0, detailedSteps ?: ""
            )
        }
    }
}
