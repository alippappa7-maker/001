package com.example.data.repository

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.local.impact.ImpactDao
import com.example.data.local.impact.ImpactEntity
import com.example.domain.model.impact.EffortLevel
import com.example.domain.model.impact.ImpactCategory
import com.example.domain.model.impact.ImpactInitiative
import com.example.domain.repository.ImpactRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class ImpactRepositoryImpl(application: Application) : ImpactRepository {

    private val impactDao: ImpactDao = AppDatabase.getDatabase(application).impactDao()

    override fun getAllInitiatives(): Flow<List<ImpactInitiative>> {
        return impactDao.getAllInitiatives().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getInitiativeById(id: String): ImpactInitiative? {
        return impactDao.getInitiativeById(id)?.toDomainModel()
    }

    override suspend fun initializeInitiatives() {
        val currentList = impactDao.getAllInitiatives().first()
        if (currentList.isEmpty()) {
            val predefined = listOf(
                ImpactEntity(
                    id = "1",
                    title = "سقيا الماء للطيور والقطط",
                    description = "توفير أوعية مياه نظيفة للحيوانات والطيور في الأيام الحارة، وهي من أعظم الصدقات.",
                    category = ImpactCategory.ENVIRONMENT.name,
                    effortLevel = EffortLevel.LOW.name,
                    approximateTimeMinutes = 10,
                    detailedSteps = "١. أحضر وعاء بلاستيكياً نظيفاً.\n٢. املأه بماء الشرب.\n٣. ضعه في مكان مظلل بعيداً عن مجرى السيارات.\n٤. احرص على تنظيف الوعاء وتجديد الماء يومياً.",
                    source = "مستوحى من حديث: (في كل كبد رطبة أجر)",
                    isFavorite = false
                ),
                ImpactEntity(
                    id = "2",
                    title = "زيارة دور المسنين",
                    description = "إدخال السرور على كبار السن ومجالستهم والاستماع لقصصهم.",
                    category = ImpactCategory.HELPING_OTHERS.name,
                    effortLevel = EffortLevel.MEDIUM.name,
                    approximateTimeMinutes = 120,
                    detailedSteps = "١. التواصل مع إدارة الدار لأخذ الإذن ومعرفة المواعيد المناسبة.\n٢. شراء بعض الهدايا البسيطة أو الحلويات اللينة (بعد التأكد من مناسبتها صحياً).\n٣. الجلوس معهم باحترام، والابتسامة، والإنصات العميق لما يقولونه.",
                    source = "توجيهات اجتماعية من مبادئ صلة الرحم والتوقير",
                    isFavorite = false
                ),
                ImpactEntity(
                    id = "3",
                    title = "المساعدة في تعليم مهارة نافعة",
                    description = "تخصيص وقت لتعليم الأطفال أو كبار السن مهارة بسيطة كالقراءة أو استخدام التقنية.",
                    category = ImpactCategory.EDUCATION.name,
                    effortLevel = EffortLevel.HIGH.name,
                    approximateTimeMinutes = 60,
                    detailedSteps = "١. حدد المهارة التي تتقنها وتستطيع تبسيطها.\n٢. ابحث عن شخص يحتاج هذه المهارة (أحد أفراد الأسرة أو الجيران).\n٣. ابدأ بتعليم الأساسيات وصبر على التعثر.\n٤. شجع المتعلم واثنِ على تقدمه.",
                    source = "مبادرة مجتمعية تطوعية",
                    isFavorite = false
                )
            )
            impactDao.insertInitiatives(predefined)
        }
    }

    override suspend fun toggleFavorite(id: String, isFavorite: Boolean) {
        impactDao.updateFavoriteStatus(id, isFavorite)
    }

    private fun ImpactEntity.toDomainModel() = ImpactInitiative(
        id = id,
        title = title,
        description = description,
        category = try { ImpactCategory.valueOf(category) } catch (e: Exception) { ImpactCategory.HELPING_OTHERS },
        effortLevel = try { EffortLevel.valueOf(effortLevel) } catch (e: Exception) { EffortLevel.MEDIUM },
        approximateTimeMinutes = approximateTimeMinutes,
        detailedSteps = detailedSteps,
        source = source,
        isFavorite = isFavorite
    )
}
