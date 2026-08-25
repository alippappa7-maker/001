package com.example.domain.service.studio

import android.content.Context
import android.media.MediaMetadataRetriever
import com.example.domain.model.quran.RecitationCueDetector
import com.example.domain.model.quran.RecitationTimeline
import com.example.domain.model.quran.SourceCard
import com.example.domain.model.quran.ShariaExportValidator
import com.example.domain.model.studio.EditingStyle
import com.example.domain.model.studio.FallbackResourceMode
import com.example.domain.model.studio.VideoProject
import com.example.domain.service.studio.template.CanvasOrnamentalFrameProvider
import com.example.domain.service.studio.template.CanvasSunsetBackgroundProvider
import com.example.domain.service.studio.template.CompositionTemplate
import com.example.domain.service.studio.template.MovingQuotesTemplate
import com.example.domain.service.studio.template.NaeemZuhdTemplate
import com.example.domain.service.studio.template.QuranRecitationTemplate
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
    private val tadhkirahTemplate: CompositionTemplate =
        TadhkirahMawidhaTemplate(CanvasOrnamentalFrameProvider()),
    private val naeemTemplate: CompositionTemplate =
        NaeemZuhdTemplate(CanvasSunsetBackgroundProvider()),
    private val movingQuotesTemplate: CompositionTemplate = MovingQuotesTemplate()
) : VideoRenderService {

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
     * يختار القالب بناءً على نمط التحرير: "تأملي" ← تذكرة، "قصصي" ← النعيم والزهد،
     * "اقتباسات مؤثرة" ← اقتباسات متحركة، وإلا المسار العام عبر [storyBuilder].
     */
    private fun templateFor(style: EditingStyle): CompositionTemplate? = when (style) {
        EditingStyle.MEDITATIVE -> tadhkirahTemplate
        EditingStyle.STORYTELLING -> naeemTemplate
        EditingStyle.MOVING_QUOTES -> movingQuotesTemplate
        else -> null
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
            resolveStoryboard(project)
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
            resolveStoryboard(project)
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
     * تصدير تلاوة قرآنية مزامَنة كلمة بكلمة بتظليل متعدد الطبقات (وضع قبس الذكي)،
     * مع بطاقة المصدر داخل الفيديو. هذا هو الربط الفعلي للـ Overlay الجديد
     * بمحرك التصدير: يُبنى [PhraseTimeline] عبر [RecitationCueDetector]، ثم
     * يُحوَّل إلى [CompositionStoryboard] عبر [QuranRecitationTemplate]، ثم
     * يُصدَّر كملف MP4 عبر [StudioCompositionEngine.export].
     *
     * بوابة [ShariaExportValidator] تُمنع أي تصدير لا يجتاز التحقق الشرعي:
     * توقيت سليم + ملف صوت فعلي على القرص + مدة مطابقة + بطاقة مصدر كاملة.
     * مدة الصوت الفعلية تُقرأ من الملف عبر [MediaMetadataRetriever]؛ إن تعذّر
     * قراؤها يُمنع التصدير (fail-closed) لا يُسمح به.
     */
    override suspend fun exportRecitation(
        timeline: RecitationTimeline,
        sourceCard: SourceCard,
        audioFile: File
    ): VideoExportResult {
        val report = ShariaExportValidator.validate(
            timeline = timeline,
            audioFile = audioFile,
            actualAudioDurationMs = readAudioDurationMs(audioFile),
            sourceCard = sourceCard
        )
        if (!report.isValid) {
            return VideoExportResult(
                isAvailable = true,
                message = "منع التصدير: لم يجتز التحقق الشرعي.",
                notice = report.errors.joinToString(" | ").ifBlank { "خطأ غير محدد في التحقق." }
            )
        }

        val phraseTimeline = RecitationCueDetector.detect(timeline)
        val textRenderer = TextBitmapRenderer(context)
        val template = QuranRecitationTemplate(
            textRenderer = textRenderer,
            width = 720,
            height = 1280,
            audioUri = audioFile.toURI().toString()
        )

        val storyboard = try {
            template.build(phraseTimeline, sourceCard)
        } catch (t: Throwable) {
            return VideoExportResult(
                isAvailable = true,
                message = "تعذّر بناء لوحة التلاوة: ${t.message ?: "خطأ غير معروف"}",
                notice = "تأكد من صحة خط زمني التلاوة وبيانات بطاقة المصدر."
            )
        }

        val outputFile = File(
            outputDir,
            "qabas_recitation_${System.currentTimeMillis()}.mp4"
        )

        return try {
            compositionEngine.export(storyboard, outputFile)
            VideoExportResult(
                isAvailable = true,
                exportPath = outputFile.absolutePath,
                message = "تم تصدير التلاوة بنجاح إلى مساحة التطبيق.",
                notice = "المسار: ${outputFile.absolutePath}. الحجم: ${formatBytes(outputFile.length())}."
            )
        } catch (t: Throwable) {
            VideoExportResult(
                isAvailable = true,
                message = "تعذّر التصدير: ${t.message ?: "خطأ غير معروف في محرك التركيب"}",
                notice = "تأكد من دعم الجهاز لترميز H.264 ومن صحة ملف الصوت."
            )
        }
    }

    /**
     * على عكس المحاكاة السابقة، التصدير الفعلي متاح الآن.
     */
    override fun isRealExportAvailable(): Boolean = true

    /**
     * يقرأ مدة ملف الصوت الفعلية من القرص. يعيد null إن تعذّر القراءة (fail-closed):
     * أي فشل هنا يجعل [ShariaExportValidator] يرفض التصدير.
     */
    private fun readAudioDurationMs(audioFile: File): Long? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(audioFile.absolutePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
        } catch (t: Throwable) {
            null
        } finally {
            retriever.release()
        }
    }

    private fun formatBytes(bytes: Long): String {
        val kb = bytes / 1024.0
        return if (kb < 1024) {
            String.format(java.util.Locale.US, "%.1f KB", kb)
        } else {
            String.format(java.util.Locale.US, "%.1f MB", kb / 1024.0)
        }
    }
}
