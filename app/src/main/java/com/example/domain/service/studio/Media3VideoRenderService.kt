package com.example.domain.service.studio

import android.content.Context
import com.example.domain.model.studio.FallbackResourceMode
import com.example.domain.model.studio.VideoProject
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
    private val compositionEngine: StudioCompositionEngine = StudioCompositionEngine(context)
) : VideoRenderService {

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
            storyBuilder.build(project)
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
            storyBuilder.build(project)
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
