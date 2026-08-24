package com.example.data.repository

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.knowledge.KnowledgeDao
import com.example.data.local.knowledge.KnowledgeEntity
import com.example.domain.model.knowledge.KnowledgeArticle
import com.example.domain.model.knowledge.KnowledgeCategory
import com.example.domain.repository.KnowledgeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

class KnowledgeRepositoryImpl(application: Application) : KnowledgeRepository {

    private val knowledgeDao: KnowledgeDao = AppDatabase.getDatabase(application).knowledgeDao()

    override fun getAllArticles(): Flow<List<KnowledgeArticle>> {
        return knowledgeDao.getAllArticles().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getArticleById(id: String): KnowledgeArticle? {
        return knowledgeDao.getArticleById(id)?.toDomainModel()
    }

    override suspend fun initializeLibrary() {
        // Only insert if DB is empty to not override favorites or progress
        val currentList = knowledgeDao.getAllArticles().first()
        if (currentList.isEmpty()) {
            val predefinedArticles = listOf(
                KnowledgeEntity(
                    id = "1",
                    title = "مقدمة في تدبر القرآن",
                    description = "مقال تمهيدي عن أهمية التدبر وكيفية البدء به",
                    content = "الحمد لله والصلاة والسلام على رسول الله...\n\nإن تدبر القرآن الكريم من أعظم العبادات التي يتقرب بها المسلم إلى ربه. قال تعالى: (كِتَابٌ أَنزَلْنَاهُ إِلَيْكَ مُبَارَكٌ لِّيَدَّبَّرُوا آيَاتِهِ). \n\nخطوات التدبر:\n١. إخلاص النية لله.\n٢. القراءة بتأنٍ وتمهل.\n٣. فهم المعاني من التفاسير الموثوقة.\n٤. سؤال النفس عن التطبيق العملي للآيات.",
                    category = KnowledgeCategory.QURAN.name,
                    source = "تفسير السعدي، بتصرف",
                    isIntroductory = true,
                    isFavorite = false,
                    progressPercent = 0f,
                    lastReadPosition = 0
                ),
                KnowledgeEntity(
                    id = "2",
                    title = "كيف نبني أسرة مستقرة؟",
                    description = "نصائح عملية مستمدة من السنة النبوية لتعزيز الترابط الأسري.",
                    content = "الأسرة هي اللبنة الأساسية في المجتمع...\n\nمن أهم عوامل استقرار الأسرة المودة والرحمة، وحسن الخلق، والتعاون على البر والتقوى.\nعن عائشة رضي الله عنها قالت: قال رسول الله صلى الله عليه وسلم: (خيركم خيركم لأهله، وأنا خيركم لأهلي).",
                    category = KnowledgeCategory.FAMILY.name,
                    source = "رياض الصالحين - كتاب أدب المعاشرة",
                    isIntroductory = false,
                    isFavorite = false,
                    progressPercent = 0f,
                    lastReadPosition = 0
                ),
                KnowledgeEntity(
                    id = "3",
                    title = "محاسبة النفس",
                    description = "أهمية محاسبة النفس وخطواتها",
                    content = "عن عمر بن الخطاب رضي الله عنه قال: (حاسبوا أنفسكم قبل أن تحاسبوا)...\n\nمحاسبة النفس طريق لتزكيتها وإصلاح عيوبها. وينبغي للمسلم أن يخصص وقتاً يومياً يراجع فيه أعماله ويستغفر عن زلاته.",
                    category = KnowledgeCategory.SELF_PURIFICATION.name,
                    source = "إحياء علوم الدين - كتاب المراقبة والمحاسبة",
                    isIntroductory = false,
                    isFavorite = false,
                    progressPercent = 0f,
                    lastReadPosition = 0
                )
            )
            knowledgeDao.insertArticles(predefinedArticles)
        }
    }

    override suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        knowledgeDao.updateFavoriteStatus(id, isFavorite)
    }

    override suspend fun updateProgress(id: String, progress: Float, position: Int) {
        knowledgeDao.updateProgress(id, progress, position)
    }

    private fun KnowledgeEntity.toDomainModel() = KnowledgeArticle(
        id = id,
        title = title,
        description = description,
        content = content,
        category = try { KnowledgeCategory.valueOf(category) } catch (e: Exception) { KnowledgeCategory.QURAN },
        source = source,
        isIntroductory = isIntroductory,
        isFavorite = isFavorite,
        progressPercent = progressPercent,
        lastReadPosition = lastReadPosition
    )
}
