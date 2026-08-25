package com.example.domain.service.studio

import android.content.Context
import com.example.domain.model.studio.EditingStyle
import com.example.domain.model.studio.FallbackResourceMode
import com.example.domain.model.studio.VideoProject
import com.example.domain.service.studio.template.AnimationTemplate
import com.example.domain.service.studio.template.CanvasOrnamentalFrameProvider
import com.example.domain.service.studio.template.CanvasSunsetBackgroundProvider
import com.example.domain.service.studio.template.CinematicTemplate
import com.example.domain.service.studio.template.CompositionTemplate
import com.example.domain.service.studio.template.DocumentaryTemplate
import com.example.domain.service.studio.template.EducationalTemplate
import com.example.domain.service.studio.template.FastReelsTemplate
import com.example.domain.service.studio.template.MovingQuotesTemplate
import com.example.domain.service.studio.template.NaeemZuhdTemplate
import com.example.domain.service.studio.template.ShortAdTemplate
import com.example.domain.service.studio.template.TadhkirahMawidhaTemplate
import java.io.File

/**
 * تطبيق حقيقي لـ [VideoRenderService] يحل محل [LocalVideoRenderService] المحاكي.
 *
 * يستخدم [StudioCompositionEngine] (الذي يعتمد على Media3 Transformer) لتصدير ملف MP4
 * فعلي على الجهاز، بدون أي خدمة سحابية أو مفتاح API أو إنترنت.
 *
 * الهدف من هذا الملف: تحويل "الاستوديو" من محاكاة إلى أول تصدير حقيقي.
 * النصوص تُرسم عبر [TextBitmapRenderer] لضمان صحة العربية، والتركيب يتم محليًا بالكامل.
 */
class Media3VideoRenderService(
    private val context: Context,
    private val storyBuilder: StoryboardBuilder = StoryboardBuilder(),
    private val compositionEngine: StudioCompositionEngine = StudioCompositionEngine(context),
    private val movingQuotesTemplate: CompositionTemplate = MovingQuotesTemplate(),
    private val tadhkirahTemplate: CompositionTemplate =
        TadhkirahMawidhaTemplate(CanvasOrnamentalFrameProvider()),
    private val naeemTemplate: CompositionTemplate =
        NaeemZuhdTemplate(CanvasSunsetBackgroundProvider())
) : VideoRenderService {

    private val quranAdapter by lazy { QuranRecitationExportAdapter(context) }

    /**
     * يختار مصدر لوحة القصة: قوالب خاصة حسب نمط التحرير (انظر [templateFor])،
     * وإلا المسار العام عبر [StoryboardBuilder]. هذا الربط وحيد ومحصور هنا
     * حتى لا تنتشر تفاصيل القوالب في طبقة العرض.
     */
    private fun resolveStoryboard(project: VideoProject): com.example.domain.model.studio.CompositionStoryboard {
        val template = templateFor(project.idea.editingStyle)
        return template?.build(project) ?: storyBuilder.build(project)
    }

    /**
     * يحل لوحة القصة مع دعم المسار المعلّق: نمط «تلاوة قرآنية» يتطلب بناءً شبكيًا
     * (خط زمني + تخزين صوت + بوابة شرعية) عبر [QuranRecitationExportAdapter]،
     * لأنه يعتمد على جلب التلاوة وتخزينها والتحقق منها. بقية الأنماط تستخدم
     * [resolveStoryboard] المتزامن.
     */
    private suspend fun resolveStoryboardSuspend(project: VideoProject): com.example.domain.model.studio.CompositionStoryboard {
        if (project.idea.editingStyle == EditingStyle.QURAN_RECITATION && project.idea.verseKey.isNotBlank()) {
            // يرمي استثناءً وصفياً عند أي فشل (شبكة/بوابة شرعية)؛ يلتقطه try/catch الخارجي.
            return quranAdapter.buildStoryboard(project.idea.verseKey)
        }
        return resolveStoryboard(project)
    }

    /**
     * يختار القالب بناءً على نمط التحرير: "تأملي" ← تذكرة، "قصصي" ← النعيم والزهد،
     * "اقتباسات مؤثرة" ← اقتباسات متحركة، والأنماط العامة الستة لها قوالبها المتميّزة الآن،
     * وإلا المسار العام عبر [storyBuilder].
     */
    private fun templateFor(style: EditingStyle): CompositionTemplate? = when (style) {
        EditingStyle.MEDITATIVE -> tadhkirahTemplate
        EditingStyle.STORYTELLING -> naeemTemplate
        EditingStyle.MOVING_QUOTES -> movingQuotesTemplate
        // الأنماط العامة الستة تُوجَّه عبر دالة مستقلة قابلة للاختبار خالصة.
        EditingStyle.CINEMATIC,
        EditingStyle.DOCUMENTARY,
        EditingStyle.EDUCATIONAL,
        EditingStyle.FAST_REELS,
        EditingStyle.SHORT_AD,
        EditingStyle.ANIMATION -> templateForStyle(style)
        else -> null
    }

    companion object {
        /**
         * توجيه نمط التحرير إلى القالب المتميّز المقابل للأنماط العامة الستة.
         * مُستخرَجة كدالة مستقلة (تبني القوالب داخليًا) لتمكين اختبار التكامل
         * دون الحاجة إلى Android Context أو مثال الخدمة.
         *
         * ملاحظة: الأنماط الخاصة (MEDITATIVE/STORYTELLING/MOVING_QUOTES) تُبنى داخل
         * الخدمة لأن بعضها يعتمد على مزودات Android (Canvas). الأنماط العامة الستة
         * هنا لا تعتمد على Android، لذا يمكن اختبارها خالصة.
         */
        internal fun templateForStyle(style: EditingStyle): CompositionTemplate? = when (style) {
            EditingStyle.CINEMATIC -> CinematicTemplate()
            EditingStyle.DOCUMENTARY -> DocumentaryTemplate()
            EditingStyle.EDUCATIONAL -> EducationalTemplate()
            EditingStyle.FAST_REELS -> FastReelsTemplate()
            EditingStyle.SHORT_AD -> ShortAdTemplate()
            EditingStyle.ANIMATION -> AnimationTemplate()
            else -> null
        }
    }

    /**
     * مجلد الإخراج الافتراضي ضمن مساحة التطبيق الداخلية (لا يتطلب صلاحية تخزين).
     */
    private val outputDir: File by lazy {
        File(context.filesDir, "qabas_exports").apply { mkdirs() }
    }

    override suspend fun renderStoryboard(
        project: VideoProject,
        fallbackMode: FallbackResourceMode
    ): VideoRenderResult {
        val inputScenes = project.plan.scenes
        val attachedCount = inputScenes.count { !it.attachedAssetId.isNullOrBlank() }

        // نتحقق أن لوحة القصة قابلة للبناء فعليًا قبل التصدير.
        val storyboard = try {
            resolveStoryboardSuspend(project)
        } catch (t: Throwable) {
            return VideoRenderResult(
                isSuccess = false,
                totalScenes = inputScenes.size,
                totalDurationSeconds = 0,
                attachedResourcesCount = attachedCount,
                message = "تعذّر بناء لوحة القصة: ${t.message ?: "خطأ غير معروف"}"
            )
        }

        val totalSecs = (storyboard.totalDurationMs / 1000).toInt()

        return VideoRenderResult(
            isSuccess = storyboard.scenes.isNotEmpty(),
            totalScenes = storyboard.scenes.size,
            totalDurationSeconds = totalSecs,
            attachedResourcesCount = attachedCount,
            message = if (storyboard.scenes.isNotEmpty()) {
                "تم تجهيز لوحة المشاهد بنجاح ($totalSecs ثانية، ${storyboard.scenes.size} مشهد، ${storyboard.textLayerCount} طبقة نص). جاهز للتصدير."
            } else {
                "لا توجد مشاهد مضافة في لوحة القصة"
            }
        )
    }

    override suspend fun exportVideo(project: VideoProject): VideoExportResult {
        val storyboard = try {
            resolveStoryboardSuspend(project)
        } catch (t: Throwable) {
            return VideoExportResult(
                isAvailable = true,
                message = "تعذّر بناء لوحة القصة: ${t.message ?: "خطأ غير معروف"}",
                notice = "تأكد من وجود مشاهد ونصوص صالحة في المشروع."
            )
        }

        val outputFile = File(
            outputDir,
            "qabas_${System.currentTimeMillis()}.mp4"
        )

        return try {
            compositionEngine.export(storyboard, outputFile)
            VideoExportResult(
                isAvailable = true,
                exportPath = outputFile.absolutePath,
                message = "تم تصدير الفيديو بنجاح إلى مساحة التطبيق.",
                notice = "المسار: ${outputFile.absolutePath}. الحجم: ${formatBytes(outputFile.length())}."
            )
        } catch (t: Throwable) {
            VideoExportResult(
                isAvailable = true,
                message = "تعذّر التصدير: ${t.message ?: "خطأ غير معروف في محرك التركيب"}",
                notice = "تأكد من توفر خلفية (صورة/فيديو) صالحة لكل مشهد، ومن دعم الجهاز لترميز H.264."
            )
        }
    }

    /**
     * على عكس المحاكاة السابقة، التصدير الفعلي متاح الآن.
     */
    override fun isRealExportAvailable(): Boolean = true

    private fun formatBytes(bytes: Long): String {
        val kb = bytes / 1024.0
        return if (kb < 1024) {
            String.format(java.util.Locale.US, "%.1f KB", kb)
        } else {
            String.format(java.util.Locale.US, "%.1f MB", kb / 1024.0)
        }
    }
}
